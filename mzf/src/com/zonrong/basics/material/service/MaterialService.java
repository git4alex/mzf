package com.zonrong.basics.material.service;

import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.MaterialInventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-11-16
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MaterialService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private MaterialInventoryService materialInventoryService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private BusinessLogService businessLogService;
	@Resource
	private Dao dao;

	public int addMaterial(Map<String, Object> material, IUser user) throws BusinessException {
		int materialId = createMaterial(material, user);
		//入库数量为0
		materialInventoryService.warehouse(MzfEnum.BizType.addMaterial, materialId, new BigDecimal(0), new BigDecimal(0), "价格", null, user);
		return materialId;
	}

	public int createMaterial(Map<String, Object> material, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>(material);
		field.put("cost", 0);
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.MATERIAL);
		String type = MapUtils.getString(material, "type");
		field.put("num", "" + generateMaterialNum(type));
		String id = entityService.create(metadata, field, user);
		return Integer.parseInt(id);
	}

	public void updateMaterial(int id, Map<String, Object> material, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.MATERIAL);
		entityService.updateById(metadata, Integer.toString(id), material, user);
	}

	public void addCost(int materialId, BigDecimal cost, int orgId, IUser user) throws BusinessException {
		//只有总部物料才维护记录成本
		if (!mzfOrgService.isHq(orgId))
			return;
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.MATERIAL);
		Map<String, Object> material = entityService.getById(metadata, Integer.toString(materialId), user.asSystem());

		Object dbCostObj = MapUtils.getObject(material, "cost");
		if (dbCostObj == null) {
			String num = MapUtils.getString(material, "num");
			throw new BusinessException("物料[" + num + "]原库存成本为空");
		}
		BigDecimal dbCost = new BigDecimal(dbCostObj.toString());
		dbCost = dbCost.add(cost);
		if (dbCost.doubleValue() < 0) {
//			throw new BusinessException("该物料总价已经低于0");
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("cost", dbCost);
		entityService.updateById(metadata, Integer.toString(materialId), field, user);

	}
	 //恢复已删除的物料
	public void recoverMaterial(Integer[] materialIds, IUser user) throws BusinessException{
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("deleted", 0);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", materialIds);
		entityService.update(MzfEntity.MATERIAL, field, where, user);
	}
	public void deleteMaterial(int materialId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.MATERIAL_INVENTORY_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", materialId);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());

		List<Integer> inventoryIds = new ArrayList<Integer>();
		List<String> orgNames = new ArrayList<String>();
		for (Map<String, Object> inventory : list) {
			Double quantity = MapUtils.getDouble(inventory, "quantity");
			if (quantity == null || quantity == 0) {
				inventoryIds.add(MapUtils.getInteger(inventory, "inventoryId"));
			} else {
				orgNames.add(MapUtils.getString(inventory, "orgName"));
			}
		}

		if (CollectionUtils.isNotEmpty(orgNames)) {
			throw new BusinessException(orgNames + "中尚有库存，不能删除");
		}

		if (CollectionUtils.isNotEmpty(inventoryIds)) {
			where = new HashMap<String, Object>();
			where.put("id", inventoryIds.toArray(new Integer[]{}));
			entityService.delete(MzfEntity.INVENTORY, where, user);
		}

		where = new HashMap<String, Object>();
		//where.put("id", materialId);
		where.put("deleted", 1);
		entityService.updateById(MzfEntity.MATERIAL, String.valueOf(materialId), where, user);
		//entityService.delete(MzfEntity.MATERIAL, where, user);
		businessLogService.log("删除物料", "物料编号为：" + materialId, user);
	}

	/**
	 * 强制删除物料
	 * @param materialId
	 * @param user
	 * @throws BusinessException
	 */
	public void compelDeleteMaterial(int materialId, IUser user)throws BusinessException{
		//删除物料库存
		Map<String, Object> where = new HashMap<String, Object>();
		where = new HashMap<String, Object>();
		where.put("targetId", materialId);
		where.put("targetType", "material");
		entityService.delete(MzfEntity.INVENTORY, where, user);

		//资料入历史物料
		where = new HashMap<String, Object>();
		where.put("deleted", 1);
		entityService.updateById(MzfEntity.MATERIAL, String.valueOf(materialId), where, user);
		businessLogService.log("强制删除物料", "物料编号为：" + materialId, user);
	}

	public String generateMaterialNum(String type) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.MATERIAL);
		if (StringUtils.isBlank(type)) {
			throw new BusinessException("物料类型为空");
		}

		Long baseIndex = new Long("1000");

		StringBuffer sb = new StringBuffer();
		String configInfo = BizCodeService.getBizName("config_materialTypeOnNum", type);
		if (StringUtils.isBlank(configInfo)) {
			throw new BusinessException("未取到对应的物料类型配置信息，无法生成物料条码");
		}
		sb.append(configInfo);

		String col = metadata.getColumnName("num");
		QueryParam qp = new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.addColumn("max(" + col + ")", "num");
		Filter filter = Filter.field("len(" + col + ")").eq(baseIndex.toString().length() - 1 + sb.length());
		filter.and(Filter.field(col).like(sb.toString() + "%"));
		qp.setFilter(filter);

		Map<String, Object> map = dao.get(qp);
		String dbNum = MapUtils.getString(map, "num");
		long index = 0;
		if (StringUtils.isBlank(dbNum)) {
			index = 1;
		} else {
			String temp = dbNum.substring(sb.length());
			index = Integer.parseInt(temp);
			index = index + 1;
			if (index > baseIndex) {
				throw new BusinessException("已经超出" + baseIndex.toString().length() + "位");
			}
		}
		index = index + baseIndex;

		String num = Long.toString(index).substring(1);
		sb.append(num);

		return sb.toString();
	}
}



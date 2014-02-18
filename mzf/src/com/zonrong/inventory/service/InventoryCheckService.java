package com.zonrong.inventory.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.sql.service.SimpleSqlService;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-12-27
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class InventoryCheckService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
    private BusinessLogService businessLogService;
	@Resource
	private SimpleSqlService simpleSqlService;

	public enum InventoryCheckStatus {
		New,
		over
	}

	public int createOrFindOpenedCheck(int orgId, StorageType storageType, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", orgId);
		where.put("storageType", storageType);
		where.put("status", InventoryCheckStatus.New);
		OrderBy orderBy = new OrderBy(new String[]{"id"}, OrderByDir.desc);
		List<Map<String, Object>> list = entityService.list(MzfEntity.INVENTORY_CHECK, where, orderBy, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) {
			Map<String, Object> field = new HashMap<String, Object>();

			field.put("orgId", orgId);
			field.put("status", InventoryCheckStatus.New);
			field.put("storageType", storageType);
			field.put("cuserId", null);
			field.put("cuserName", null);
			field.put("cdate", null);

			String id = entityService.create(MzfEntity.INVENTORY_CHECK, field, user);
			return Integer.parseInt(id);
		}
		//删除由于意外情况产生的垃圾数据
		if (list.size() > 1) {
			List<Integer> delIds = new ArrayList<Integer>();
			for (int i = 1; i < list.size(); i++) {
				Map<String, Object> map = list.get(i);
				delIds.add(MapUtils.getInteger(map, "id"));
			}
			if (CollectionUtils.isNotEmpty(delIds)) {
				where = new HashMap<String, Object>();
				where.put("id", delIds.toArray(new Integer[]{}));
				entityService.delete(MzfEntity.INVENTORY_CHECK, where, User.getSystemUser());
			}
		}
		return MapUtils.getInteger(list.get(0), "id");
	}

	public int saveTargetNum(int checkId, String targetNum, BigDecimal actualQuantity, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("checkId", checkId);
		where.put("targetNum", targetNum);
		List<Map<String, Object>> list = entityService.list(MzfEntity.INVENTORY_CHECK_DETAIL, where, null, User.getSystemUser());
		if (CollectionUtils.isNotEmpty(list)) {
			throw new BusinessException("该条码已扫描");
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("checkId", checkId);
		field.put("targetNum", targetNum);
		field.put("actualQuantity", actualQuantity);
		String id = entityService.create(MzfEntity.INVENTORY_CHECK_DETAIL, field, user);
		return Integer.parseInt(id);
	}

	public void createInventoryCheck(int checkId, IUser user) throws BusinessException {
		Map<String, Object> check = entityService.getById(MzfEntity.INVENTORY_CHECK, Integer.toString(checkId), user);
		StorageType storageType = StorageType.valueOf(MapUtils.getString(check, "storageType"));
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", InventoryCheckStatus.over);
		field.put("cuserId", null);
		field.put("cuserName", null);
		field.put("cdate", null);
		entityService.updateById(MzfEntity.INVENTORY_CHECK, Integer.toString(checkId), field, user);


		//先查询，再删除，后插入
		//1.查询
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("checkId", checkId);
		where.put("storageType", storageType);
		List<Map<String, Object>> fields = simpleSqlService.list("inventoryCheck", storageType.getInventoryCheckSql(), where, user);

		//2.删除
		where = new HashMap<String, Object>();
		where.put("checkId", checkId);
		entityService.delete(MzfEntity.INVENTORY_CHECK_DETAIL, where, user);

		//3.插入
		for (Map<String, Object> map : fields) {
			map.remove("id");
		}
		entityService.batchCreate(MzfEntity.INVENTORY_CHECK_DETAIL, fields, User.getSystemUser());
	}

//	public enum InventoryCheckResult {
//		pass,
//		notPass
//	}
//
	public int create(int orgId, StorageType storageType, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>();

		field.put("orgId", orgId);
		field.put("status", InventoryCheckStatus.over);
		field.put("storageType", storageType);
		field.put("cuserId", null);
		field.put("cuserName", null);
		field.put("cdate", null);

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.INVENTORY_CHECK);
		String id = entityService.create(metadata, field, user);
		int checkId = Integer.parseInt(id);

		for (Map<String, Object> detail : detailList) {
			detail.remove("id");
			detail.put("checkId", checkId);
		}
		entityService.batchCreate(MzfEntity.INVENTORY_CHECK_DETAIL, detailList, user);
		//记录操作日志
		businessLogService.log("盘点" + storageType.getText(), null, user);
		return Integer.parseInt(id);

	}
//
//	public int createForProduct(List<Map<String, Object>> checkList, IUser user) throws BusinessException {
//		InventoryCheckResult result = CollectionUtils.isEmpty(checkList)? InventoryCheckResult.pass:InventoryCheckResult.notPass;
//		int checkId = createInventoryCheck(InventoryCheckType.product, result, user);
//		if (result == InventoryCheckResult.pass) {
//			return checkId;
//		}
//
//		for (Map<String, Object> check : checkList) {
//			check.put("checkId", checkId);;
//			check.put("inventoryQuantity", 1);
//			check.put("actualQuantity", 0);
//		}
//
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.INVENTORY_CHECK_DETAIL);
//		entityService.batchCreate(metadata, checkList, user);
//		if (true) throw new BusinessException("dsfa");
//		//记录操作日志
//		businessLogService.log("商品库盘点", null, user);
//		return checkId;
//	}
//
//	public int createForRawmaterial(List<Map<String, Object>> checkList, IUser user) throws BusinessException {
//		InventoryCheckResult result = CollectionUtils.isEmpty(checkList)? InventoryCheckResult.pass:InventoryCheckResult.notPass;
//		int checkId = createInventoryCheck(InventoryCheckType.rawmaterial, result, user);
//		if (result == InventoryCheckResult.pass) {
//			return checkId;
//		}
//
//		for (Map<String, Object> check : checkList) {
//			check.put("checkId", checkId);;
//		}
//
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.INVENTORY_CHECK_DETAIL);
//		entityService.batchCreate(metadata, checkList, user);
//
//		return checkId;
//	}
//
//	public int createForMaterial(List<Map<String, Object>> checkList, IUser user) throws BusinessException {
//		InventoryCheckResult result = CollectionUtils.isEmpty(checkList)? InventoryCheckResult.pass:InventoryCheckResult.notPass;
//		int checkId = createInventoryCheck(InventoryCheckType.material, result, user);
//		if (result == InventoryCheckResult.pass) {
//			return checkId;
//		}
//
//		for (Map<String, Object> check : checkList) {
//			check.put("checkId", checkId);;
//		}
//
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.INVENTORY_CHECK_DETAIL);
//		entityService.batchCreate(metadata, checkList, user);
//
//		return checkId;
//	}
//
//	public int createForSell(Map<String, Object> check, IUser user) throws BusinessException {
//		InventoryCheckResult result = MapUtils.isEmpty(check)? InventoryCheckResult.pass:InventoryCheckResult.notPass;
//		int checkId = createInventoryCheck(InventoryCheckType.cash, result, user);
//
//		if (result == InventoryCheckResult.pass) {
//			return checkId;
//		}
//
//		check.put("checkId", checkId);
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.INVENTORY_CHECK_DETAIL);
//		entityService.create(metadata, check, user);
//
//		return checkId;
//	}
//
//	public int createForEarnest(Map<String, Object> check, IUser user) throws BusinessException {
//
//		InventoryCheckResult result = MapUtils.isEmpty(check)? InventoryCheckResult.pass:InventoryCheckResult.notPass;
//		int checkId = createInventoryCheck(InventoryCheckType.frontMoney, result, user);
//
//		if (result == InventoryCheckResult.pass) {
//			return checkId;
//		}
//
//		check.put("checkId", checkId);
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.INVENTORY_CHECK_DETAIL);
//		entityService.create(metadata, check, user);
//
//		return checkId;
//	}
//
//	private int createInventoryCheck(InventoryCheckType type, InventoryCheckResult result, IUser user) throws BusinessException {
//		Map<String, Object> field = new HashMap<String, Object>();
//
//		field.put("orgId", user.getOrgId());
//		field.put("orgName", user.getOrgName());
//		field.put("type", type);
//		field.put("result", result);
//		field.put("cuserId", null);
//		field.put("cuserName", null);
//		field.put("cdate", null);
//
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.INVENTORY_CHECK);
//		String id = entityService.create(metadata, field, user);
//
//		return Integer.parseInt(id);
//	}

	public Map<String, Object> getPrintData(int checkId, IUser user) throws BusinessException {
		Map<String, Object> check = entityService.getById(MzfEntity.INVENTORY_CHECK_VIEW, checkId, user);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("checkId", checkId);
		List<Map<String, Object>> detailList = entityService.list(MzfEntity.INVENTORY_CHECK_DETAIL, where, null, user);
		check.put("detailList", detailList);

		if (CollectionUtils.isNotEmpty(detailList)) {
			List<Integer> checkDetailIds = new ArrayList<Integer>();
			for (Map<String, Object> detail : detailList) {
				checkDetailIds.add(MapUtils.getInteger(detail, "id"));
			}
			where = new HashMap<String, Object>();
			where.put("checkDetailId", checkDetailIds.toArray(new Integer[]{}));
			List<Map<String, Object>> remarkList = entityService.list(MzfEntity.INVENTORY_CHECK_DETAIL_REMARK, where, null, user);
			for (Map<String, Object> detail : detailList) {
				List<Map<String, Object>> remarks = (List<Map<String, Object>>) MapUtils.getObject(detail, "remarks");
				if (CollectionUtils.isEmpty(remarks)) {
					remarks = new ArrayList<Map<String,Object>>();
					detail.put("remarks", remarks);
				}
				Integer id = MapUtils.getInteger(detail, "id");
				for (Map<String, Object> remark : remarkList) {
					Integer checkDetailId = MapUtils.getInteger(remark, "checkDetailId");
					if (checkDetailId.intValue() == id) {
						remarks.add(remark);
					}
				}
			}
		}

		return check;
	}
}



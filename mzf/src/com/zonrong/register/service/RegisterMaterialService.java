package com.zonrong.register.service;

import com.zonrong.basics.material.service.MaterialService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.MaterialInventoryService;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-11-2
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class RegisterMaterialService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private MaterialService materialService;
	@Resource
	private MaterialInventoryService materialInventoryService;
	@Resource
	private RegisterService registerService;
	@Resource
	private BusinessLogService businessLogService;

	public int register(int orderDetailId, Map<String, Object> material, Map<String, Object> register, IUser user) throws BusinessException {
		Map<String, Object> dbDetail = entityService.getById(MzfEntity.VENDOR_ORDER_MATERIAL_ORDER_DETAIL, orderDetailId, user.asSystem());

		//物料登记
		Integer materialId = MapUtils.getInteger(material, "materialId");
		if (materialId == null) {
			material.put("wholesalePrice", 0);
			material.put("retailPrice", 0);
			materialId = materialService.createMaterial(material, user);
		}

		//核销订单明细
		cancelMaterialOrderDetail(dbDetail, materialId, user);

		//入库
		BigDecimal quantity = new BigDecimal(MapUtils.getString(material, "quantity"));
		BigDecimal unitPrice = new BigDecimal(MapUtils.getString(material, "unitPrice", "0"));
		String costStr = MapUtils.getString(material, "cost");
		BigDecimal cost = null;
		if (StringUtils.isNotBlank(costStr)) {
			cost = new BigDecimal(costStr);
		}
		if (cost == null) {
			throw new BusinessException("未指定物料成本");
		}
		int orderId = MapUtils.getInteger(dbDetail, "orderId");
		Map<String, Object> dbOrder = entityService.getById(MzfEntity.VENDOR_ORDER, orderId, user.asSystem());
		String remark = "物料入库， 物料采购订单号：" + MapUtils.getString(dbOrder, "num");
		materialInventoryService.warehouse(MzfEnum.BizType.register, materialId, quantity, cost, remark, user);
		materialService.addCost(materialId, cost, user.getOrgId(), user);

		 //记录操作日志
		businessLogService.log("物料收货登记", "物料编号：" + materialId, user);
		register.put("cost", cost);
		register.put("unitPrice", unitPrice);
		return registerService.createRegister(register, TargetType.material, orderId, orderDetailId, materialId, quantity, user);
	}
	public Map<String, Object> getPrintData(Integer[] ids, IUser user)throws BusinessException{
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", ids);
		List<Map<String, Object>> dataList = entityService.list(MzfEntity.REGISTER_VIEW, where, null, user);
		String vendorName = "";
		String vendorNum = "";
		if(CollectionUtils.isNotEmpty(dataList)){
			vendorName = MapUtils.getString(dataList.get(0), "vendorName");
			vendorNum = MapUtils.getString(dataList.get(0), "orderNum");
		}
		for (Map<String, Object> map : dataList) {
			Map<String, Object> materail = entityService.getById(MzfEntity.MATERIAL, MapUtils.getIntValue(map, "targetId", 0), user);
			map.put("materialName", MapUtils.getString(materail, "name"));

		}
		data.put("vendorNum", vendorNum);
		data.put("vendorName", vendorName);
		data.put("dataList", dataList);
		return data;
	}

	private void cancelMaterialOrderDetail(Map<String, Object> detail, final int materialId, IUser user) throws BusinessException {
		CancelDetailTemplete templete = new CancelDetailTemplete(metadataProvider, entityService){
			public IEntityCode getDetailEntityCode(){
				return MzfEntity.VENDOR_ORDER_MATERIAL_ORDER_DETAIL;
			}
			public void putObjectId(Map<String, Object> field) {
				field.put("materialId", materialId);
			}
		};
		templete.cancelDetail(detail, user);
	}
}



package com.zonrong.register.service;

import com.zonrong.basics.rawmaterial.service.RawmaterialService;
import com.zonrong.basics.rawmaterial.service.RawmaterialService.RawmaterialType;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.RawmaterialInventoryService;
import com.zonrong.inventory.service.RawmaterialInventoryService.GoldClass;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.dosing.service.DosingService;
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
 * date: 2010-11-2
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class RegisterRawmaterialService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private RawmaterialService rawmaterialService;
	@Resource
	private RawmaterialInventoryService rawmaterialInventoryService;
	@Resource
	private RegisterService registerService;
	@Resource
	private BusinessLogService businessLogService;
	@Resource
	private BizCodeService bizCodeService;
    @Resource
    private TransactionService transactionService;
    @Resource
    FlowLogService flowLogService;


    //委外原料退库
    public void returnRawmaterialFromOem(String dosingId,Map<String, Object> rawmaterial,IUser user)throws BusinessException{
        Map<String,Object> oemOrderDosing = entityService.getById(new EntityCode("vOemOrderDosing"),dosingId,user);
        if(oemOrderDosing == null){
            throw new BusinessException("未找到指定的委外加工订单，退库操作失败");
        }

        String oemOrderNum = MapUtils.getString(oemOrderDosing,"orderNum");
        String remark = "委外加工订单编号：["+oemOrderNum+"]";
        //更新库存
        RawmaterialType type = RawmaterialType.valueOf(MapUtils.getString(rawmaterial, "rawmaterialType"));
        int rawmaterialId = -1;
        BigDecimal quantity = new BigDecimal(1);
        if(type == RawmaterialType.nakedDiamond){
            rawmaterialId = MapUtils.getInteger(rawmaterial, "rawmaterialId", -1);
            if(rawmaterialId<0){
                throw new BusinessException("原料Id为空，无法退库");
            }

            Map<String,Object> v = new HashMap<String, Object>();
            v.put("status",RawmaterialService.RawmaterialStatus.free);
            v.put("statusRemark","正常");
            entityService.updateById(MzfEntity.RAWMATERIAL,rawmaterialId+"",v,user);

            rawmaterialInventoryService.warehouseDiamond(BizType.oemReturn, rawmaterialId, user.getOrgId(), remark, user);
        }else if(type == RawmaterialType.gold){
            GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(rawmaterial, "goldClass"));
            Integer dbRawmaterialId = rawmaterialService.findGold(goldClass, user);
            if (dbRawmaterialId == null) {
                rawmaterial.put("cost", 0);
                rawmaterialId = rawmaterialService.createRawmaterial(rawmaterial, user);
            } else {
                rawmaterialId = dbRawmaterialId;
            }

            quantity = new BigDecimal(MapUtils.getString(rawmaterial, "dosingQuantity"));
            rawmaterialInventoryService.warehouseGold(BizType.oemReturn, rawmaterialId, quantity,new BigDecimal(0), remark, user);
        }else if(type == RawmaterialType.gravel){
            String gravelStandard = MapUtils.getString(rawmaterial, "gravelStandard");
            Integer dbRawmaterialId = rawmaterialService.findGravel(gravelStandard, user);
            if (dbRawmaterialId == null) {
                rawmaterial.put("cost", 0);
                rawmaterial.put("weight", 0);
                rawmaterialId = rawmaterialService.createRawmaterial(rawmaterial, user);
            } else {
                rawmaterialId = dbRawmaterialId;
            }

            quantity = new BigDecimal(MapUtils.getString(rawmaterial, "dosingQuantity"));
            BigDecimal weight = new BigDecimal(MapUtils.getString(rawmaterial, "dosingWeight"));
            rawmaterialInventoryService.warehouseGravel(BizType.oemReturn, rawmaterialId, quantity, new BigDecimal(0), weight, remark, user);
        }else if(type == RawmaterialType.parts){
            String partsType = MapUtils.getString(rawmaterial, "partsType");
            String partsStandard = MapUtils.getString(rawmaterial, "partsStandard");
            GoldClass goldClass = null;
            try {
                goldClass = GoldClass.valueOf(MapUtils.getString(rawmaterial, "goldClass"));
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage());
                }
            }
            Integer dbRawmaterialId = rawmaterialService.findParts(partsType, goldClass, partsStandard, user);
            if (dbRawmaterialId == null) {
                rawmaterial.put("cost", 0);
                rawmaterialId = rawmaterialService.createRawmaterial(rawmaterial, user);
            } else {
                rawmaterialId = dbRawmaterialId;
            }

            quantity = new BigDecimal(MapUtils.getString(rawmaterial, "dosingQuantity"));
            rawmaterialInventoryService.warehouseParts(BizType.oemReturn, rawmaterialId, quantity, new BigDecimal(0), remark, user);
        }
        //更新配料记录
        Map<String,Object> val = new HashMap<String,Object>();
        val.put("status", DosingService.DosingStatus.returned);
        entityService.updateById(MzfEntity.DOSING,dosingId,val,user);
        //新增收货记录

        Map<String,Object> register = new HashMap<String,Object>();
        int orderId = MapUtils.getInteger(oemOrderDosing,"orderId");
        int orderDetailId = MapUtils.getInteger(oemOrderDosing,"detailId");
        //registerService.createRegister(register, TargetType.rawmaterial, orderId, orderDetailId, rawmaterialId, quantity, user);

        register.put("type", TargetType.rawmaterial);
        register.put("orderId", orderId);
        register.put("orderDetailId", orderDetailId);
        register.put("targetId", rawmaterialId);
        register.put("quantity", quantity);
        register.put("cuserId", null);
        register.put("cdate", null);
        register.put("remark","委外原料退库");
        entityService.create(MzfEntity.REGISTER, register, user);

        //记录流程信息
        int transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER, Integer.toString(orderId), user);
        remark = "原料条码："+MapUtils.getString(oemOrderDosing,"rawmaterialNum");
        flowLogService.createLog(transId, MzfEntity.VENDOR_ORDER, Integer.toString(orderId), "原料退库", TargetType.rawmaterial, rawmaterialId, remark, user);

    }

    public int register(int orderDetailId, Map<String, Object> rawmaterial, Map<String, Object> register, IUser user) throws BusinessException {
		BigDecimal cost = new BigDecimal(MapUtils.getString(rawmaterial, "cost","0"));
		BigDecimal unitPrice = new BigDecimal(MapUtils.getString(rawmaterial, "unitPrice","0"));

		Map<String, Object> dbDetail = entityService.getById(MzfEntity.VENDOR_ORDER_RAWMATERIAL_ORDER_DETAIL, orderDetailId, user.asSystem());

		int orderId = MapUtils.getInteger(dbDetail, "orderId");
		Map<String, Object> dbOrder = entityService.getById(MzfEntity.VENDOR_ORDER, orderId, user.asSystem());
        String orderType = MapUtils.getString(dbOrder,"type");
        String remark;
        BizType bizType;
        if(StringUtils.equalsIgnoreCase(orderType,"splitRawmaterial")){
            bizType = BizType.warehouseOnSplit;
            remark = "拆旧单号：[" + MapUtils.getString(dbOrder, "num")+"]";
        }else{
            bizType = BizType.register;
            remark = "采购订单号：[" + MapUtils.getString(dbOrder, "num")+"]";
        }
//		String remark = "原料入库， 原料采购订单编号：" + MapUtils.getString(dbOrder, "num");
		rawmaterial.put("source", "register");
		rawmaterial.put("sourceId", orderDetailId);
//		int rawmaterialId = register(orderDetailId, rawmaterial, register, user);
		int rawmaterialId = registerRawmaterial(bizType,rawmaterial, remark, user);

		//核销订单明细
		cancelRawmaterialOrderDetail(dbDetail, rawmaterialId, user);

		RawmaterialType type = RawmaterialType.valueOf(MapUtils.getString(rawmaterial, "type"));
		BigDecimal quantity = new BigDecimal(1);
		if (type != RawmaterialType.nakedDiamond) {
			quantity = new BigDecimal(MapUtils.getString(rawmaterial, "quantity"));
		}
		//记录操作日志
		businessLogService.log("原料收货登记", "原料编号: " + rawmaterialId, user);
		register.put("cost", cost);
		register.put("unitPrice", unitPrice);
		return registerService.createRegister(register, TargetType.rawmaterial, orderId, orderDetailId, rawmaterialId, quantity, user);
	}

	public Map<String, Object> getPrintData(Integer[] ids, IUser user)throws BusinessException{
		Map<String, Object> data = new HashMap<String, Object>();
		List<Map<String, Object>> proList = new ArrayList<Map<String,Object>>();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", ids);
		List<Map<String, Object>> dataList = entityService.list(MzfEntity.REGISTER_VIEW, where, null, user);
		String orderNum = "";
		if(CollectionUtils.isNotEmpty(dataList)){
			orderNum = MapUtils.getString(dataList.get(0), "orderNum");
		}

		for (Map<String, Object> map : dataList) {
			Map<String, Object> rawmaterial = entityService.getById(MzfEntity.RAWMATERIAL, MapUtils.getIntValue(map, "targetId", 0), user);
			String orderType = MapUtils.getString(map, "orderType","0");
			if(orderType.equals("splitRawmaterial")){
				orderType = "拆旧";
			}else if(orderType.equals("rawmaterial")){
                orderType = "采购";
			}
			rawmaterial.put("orderType", orderType);
			rawmaterial.put("unitPrice", MapUtils.getDouble(map, "unitPrice"));
			rawmaterial.put("cost", MapUtils.getDouble(map, "cost"));
			rawmaterial.put("quantity", MapUtils.getDouble(map, "quantity"));
			rawmaterial.put("remark", MapUtils.getString(map, "ramark"));
			rawmaterial.put("cdate", MapUtils.getObject(map, "cdateStr"));
			rawmaterial.put("cuserName", MapUtils.getString(map, "cuserName"));
			rawmaterial.put("rawmaterTypeText", BizCodeService.getBizName("rowmaterialType", MapUtils.getString(map, "rawmaterialType", "")));
			rawmaterial.put("goldClassText", BizCodeService.getBizName("goldClass", MapUtils.getString(rawmaterial, "goldClass", "")));
			rawmaterial.put("partsTypeText", BizCodeService.getBizName("partsType", MapUtils.getString(rawmaterial, "partsType", "")));
			rawmaterial.put("partsStandardText", BizCodeService.getBizName("partsStandard", MapUtils.getString(rawmaterial, "partsStandard", "")));

			proList.add(rawmaterial);

		}
		data.put("orderNum", orderNum);
		data.put("dataList", proList);
		return data;
	}

	private int registerRawmaterial(BizType bizType,Map<String, Object> rawmaterial, String remark, IUser user) throws BusinessException {
		RawmaterialType type;
		try {
			type = RawmaterialType.valueOf(MapUtils.getString(rawmaterial, "type"));
		} catch (Exception e) {
			throw new BusinessException("未指定原料类型");
		}

		String costStr = MapUtils.getString(rawmaterial, "cost");
		BigDecimal cost = null;
		if (StringUtils.isNotBlank(costStr)) {
			cost = new BigDecimal(costStr);
		}
		if (cost == null) {
			throw new BusinessException("未指定原料成本");
		}

		//原料登记
		int rawmaterialId;
		if (type == RawmaterialType.nakedDiamond) {
            rawmaterial.put("karatUnitPrice",MapUtils.getFloatValue(rawmaterial,"unitPrice"));
			rawmaterialId = rawmaterialService.createRawmaterial(rawmaterial, user);
			rawmaterialInventoryService.warehouseDiamond(bizType, rawmaterialId, user.getOrgId(), remark, user);
		} else if (type == RawmaterialType.gold) {
			GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(rawmaterial, "goldClass"));
			Integer dbRawmaterialId = rawmaterialService.findGold(goldClass, user);
			if (dbRawmaterialId == null) {
				rawmaterial.put("cost", 0);
				rawmaterialId = rawmaterialService.createRawmaterial(rawmaterial, user);
			} else {
				rawmaterialId = dbRawmaterialId;
			}

			BigDecimal quantity = new BigDecimal(MapUtils.getString(rawmaterial, "quantity"));
			rawmaterialInventoryService.warehouseGold(bizType, rawmaterialId, quantity, cost, remark, user);
		} else if (type == RawmaterialType.parts) {
			String partsType = MapUtils.getString(rawmaterial, "partsType");
			String partsStandard = MapUtils.getString(rawmaterial, "partsStandard");
			GoldClass goldClass = null;
			try {
				goldClass = GoldClass.valueOf(MapUtils.getString(rawmaterial, "goldClass"));
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug(e.getMessage());
				}
			}
			Integer dbRawmaterialId = rawmaterialService.findParts(partsType, goldClass, partsStandard, user);
			if (dbRawmaterialId == null) {
				rawmaterial.put("cost", 0);
				rawmaterialId = rawmaterialService.createRawmaterial(rawmaterial, user);
			} else {
				rawmaterialId = dbRawmaterialId;
			}

			BigDecimal quantity = new BigDecimal(MapUtils.getString(rawmaterial, "quantity"));
			rawmaterialInventoryService.warehouseParts(bizType, rawmaterialId, quantity, cost, remark, user);
		} else if (type == RawmaterialType.gravel) {
			String gravelStandard = MapUtils.getString(rawmaterial, "gravelStandard");
			Integer dbRawmaterialId = rawmaterialService.findGravel(gravelStandard, user);
			if (dbRawmaterialId == null) {
				rawmaterial.put("cost", 0);
				rawmaterial.put("weight", 0);
				rawmaterialId = rawmaterialService.createRawmaterial(rawmaterial, user);
			} else {
				rawmaterialId = dbRawmaterialId;
			}

			BigDecimal quantity = new BigDecimal(MapUtils.getString(rawmaterial, "quantity"));
			BigDecimal weight = new BigDecimal(MapUtils.getString(rawmaterial, "weight"));
			rawmaterialInventoryService.warehouseGravel(bizType, rawmaterialId, quantity, cost, weight, remark, user);
		} else {
			throw new BusinessException("非法的原料类型");
		}

		return rawmaterialId;
	}

	private void cancelRawmaterialOrderDetail(Map<String, Object> detail, final int rawmaterialId, IUser user) throws BusinessException {
		CancelDetailTemplete templete = new CancelDetailTemplete(metadataProvider, entityService) {
            public IEntityCode getDetailEntityCode() {
                return MzfEntity.VENDOR_ORDER_RAWMATERIAL_ORDER_DETAIL;
            }

            public void putObjectId(Map<String, Object> field) {
                field.put("rawmaterialId", rawmaterialId);
            }
        };
		templete.cancelDetail(detail, user);
	}
}



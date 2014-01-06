package com.zonrong.demand.product.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.common.service.BillStatusService;
import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.CusOrderStatus;
import com.zonrong.common.utils.MzfEnum.DemandStatus;
import com.zonrong.common.utils.MzfEnum.ProductType;
import com.zonrong.common.utils.MzfUtils;
import com.zonrong.common.utils.MzfUtils.BillPrefix;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.cusorder.service.CusOrderService;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.service.RawmaterialInventoryService.GoldClass;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.transfer.product.service.TransferProductService;
import com.zonrong.util.TpltUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-10-12
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ProductDemandCRUDService extends BillStatusService<DemandStatus>{
	private static Logger logger = Logger.getLogger(CusOrderService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private CusOrderService cusOrderService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TransferProductService transferProductService;
	@Resource
	private ProductService productService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	/**
	 * 新增要货单
	 *
	 * @param demand
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public void createDemand(Map<String, Object> demand, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND);

		Integer orderId = MapUtils.getInteger(demand, "orderId");
		String orderNum = MapUtils.getString(demand, "orderNum");
		int expectCount = MapUtils.getIntValue(demand, "expectCount", 1);
		if (orderId != null) {
			if (expectCount > 1) {
				throw new BusinessException("要货数量大于1时不能指定客订单");
			}

			Map<String, Object> where = new HashMap<String, Object>();
			where.put("orderId", orderId);
			List list = entityService.list(metadata, where, null, user.asSystem());
			if (list != null && list.size() > 0) {
				throw new BusinessException("客订单[" + orderNum + "]已经生成要货申请！");
			}
		}

		Integer styleId = MapUtils.getInteger(demand, "styleId");
		if (styleId != null) {
			Map<String, Object> dbStyle = entityService.getById(MzfEntity.STYLE, styleId, user.asSystem());
			demand.put("ptype", MapUtils.getString(dbStyle, "ptype"));
			demand.put("pkind", MapUtils.getString(dbStyle, "pkind"));
			if (StringUtils.isEmpty(MapUtils.getString(demand, "weight1"))) {
				demand.put("weight1", MapUtils.getString(dbStyle, "diamondWeight"));
			}
			if (StringUtils.isEmpty(MapUtils.getString(demand, "weight2"))) {
				demand.put("weight2", MapUtils.getString(dbStyle, "diamondWeight2"));
			}
			if (StringUtils.isEmpty(MapUtils.getString(demand, "shape"))) {
				demand.put("shape", MapUtils.getString(dbStyle, "diamondShape"));
			}
			if (StringUtils.isEmpty(MapUtils.getString(demand, "inset"))) {
				demand.put("inset", MapUtils.getString(dbStyle, "inset"));
			}

			if (StringUtils.isEmpty(MapUtils.getString(demand, "goldWeight2"))) {
				String goldClassStr = MapUtils.getString(demand, "goldClass");
				if (StringUtils.isNotBlank(goldClassStr)) {
                    GoldClass goldClass = null;
                    try{
                        goldClass = GoldClass.valueOf(goldClassStr);
                    }catch (Exception e){

                    }

					String goldWeight = null;
					if (goldClass == GoldClass.pt900 || goldClass == GoldClass.pt950) {
						goldWeight = MapUtils.getString(dbStyle, "ptWeight");
					}
					if (goldClass == GoldClass.k750) {
						goldWeight = MapUtils.getString(dbStyle, "kWeight");
					}
					if (goldClass == GoldClass.pd950) {
						goldWeight = MapUtils.getString(dbStyle, "pdWeight");
					}
					if (goldClass == GoldClass.silver) {
						goldWeight = MapUtils.getString(dbStyle, "silverWeight");
					}
					if (StringUtils.isNotBlank(goldWeight)) {
						demand.put("goldWeight2", goldWeight);
					}
				}
			}
		}
		//新建要货申请
		String num = MzfUtils.getBillNum(BillPrefix.SPYH, user);
		demand.put("status", DemandStatus.New);
		demand.put("isCusOrder", Boolean.toString(false));
		demand.put("shopName", user.getOrgName());
		demand.put("num", num);
		demand.put("cuserId", null);
		demand.put("cuserName", null);
		demand.put("cdate", null);
		demand.put("expectCount", 1);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < expectCount; i++) {
			list.add(demand);
		}

		Integer demandId = null;
		if (expectCount > 1) {
			entityService.batchCreate(metadata, list, user);
		} else {
			String id = entityService.create(metadata, demand, user);
			demandId = Integer.parseInt(id);
		}


		//更新客订单状态为要货中
		if (orderId != null) {
			cusOrderService.updateStatus(orderId, CusOrderStatus.New, CusOrderStatus.demanding, new StatusCarrier(){
				@Override
				public void active(Map<String, Object> order) throws BusinessException {
					throw new BusinessException("要货申请与客户订单[" + this.getNum() + "]关联失败");
				}

			}, user);
		}

		//记录流程
		createLog(new String[]{num}, user);
	}

	public void createDemandByStyle(Integer[] styleIds, IUser user) throws BusinessException {
		EntityMetadata styleMetadata = metadataProvider.getEntityMetadata(MzfEntity.STYLE);
		EntityMetadata demandMetadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND);

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put(styleMetadata.getPkCode(), styleIds);
		List<Map<String, Object>> list = entityService.list(styleMetadata, parameter, null, user.asSystem());

		List<String> numList = new ArrayList<String>();
		for (Map<String, Object> demand : list) {
			ProductType ptype = ProductType.valueOf(MapUtils.getString(demand, "ptype"));
			Object styleId = MapUtils.getObject(demand, styleMetadata.getPkCode());
			Object styleCode = MapUtils.getObject(demand, "code");

			String num = MzfUtils.getBillNum(BillPrefix.SPYH, user);
			numList.add(num);
			demand.put("styleId", styleId);
			demand.put("styleCode", styleCode);
			demand.put("weight1", MapUtils.getObject(demand, "diamondWeight"));
			demand.put("weight2", MapUtils.getObject(demand, "diamondWeight2"));
			demand.put("shape", MapUtils.getObject(demand, "diamondShape"));

			demand.put("status", DemandStatus.New);
			demand.put("orgId", user.getOrgId());
			demand.put("orgName", user.getOrgName());
			demand.put("num", num);
			demand.put("expectCount", 1);

			if (ptype == ProductType.pt) {
				demand.put("goldWeight2", MapUtils.getObject(demand, "ptWeight"));
			}
			if (ptype == ProductType.kGold) {
				demand.put("goldWeight2", MapUtils.getObject(demand, "kWeight"));
			}

			demand.put("cuserId", null);
			demand.put("cuserName", null);
			demand.put("cdate", null);
			demand.remove(demandMetadata.getPkCode());
		}
		entityService.batchCreate(demandMetadata, list, user);

		//记录流程
		createLog(numList.toArray(new String[]{}), user);
	}


	//记录流程
	private void createLog(String[] demandNums, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND_VIEW);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("num", demandNums);
		List<Map<String, Object>> dbDemandList = entityService.list(metadata, where, null, user.asSystem());
		for (Map<String, Object> dbDemand : dbDemandList) {
			Integer dbId = MapUtils.getInteger(dbDemand, metadata.getPkCode());
			Integer dbOrderId = MapUtils.getInteger(dbDemand, "orderId");

			int transId;
			String remark = "";
			if (dbOrderId != null) {
				String dbOrderNum = MapUtils.getString(dbDemand, "orderNum");
				transId = transactionService.findTransId(MzfEntity.CUS_ORDER, Integer.toString(dbOrderId), user);
				remark = "与客户订单[" + dbOrderNum + "]关联";
			} else {
				transId = transactionService.createTransId();
			}
			logService.createLog(transId, MzfEntity.DEMAND, dbId.toString(), "新建要货申请", null, null, remark, user);
		}
	}

	public int updateDemand(int id, Map<String, Object> demand, IUser user) throws BusinessException {
		Map<String, Object> dbDemand = entityService.getById(MzfEntity.DEMAND, id, user.asSystem());
		DemandStatus dbStatus = DemandStatus.valueOf(MapUtils.getString(dbDemand, "status"));
		if (DemandStatus.New != dbStatus && DemandStatus.waitMgrProcess != dbStatus  && DemandStatus.reject != dbStatus) {
			throw new BusinessException("此状态不能修改！");
		}

		if (DemandStatus.New == dbStatus
				|| DemandStatus.waitMgrProcess == dbStatus
				|| DemandStatus.reject == dbStatus) {
			demand.remove("status");
			demand.remove("orgId");
			demand.remove("orgName");
			demand.put("muserId", null);
			demand.put("muserName", null);
			demand.put("mdate", null);
			int row = entityService.updateById(MzfEntity.DEMAND, Integer.toString(id), demand, user);
			return row;
		} else {
			String num = MapUtils.getString(dbDemand, "num");
			throw new BusinessException("要货申请[" + num + "]状态为" + dbStatus.getText() + "，不能修改");
		}
	}

	public void deleteDemand(int id, IUser user) throws BusinessException {
		EntityMetadata demandMetadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND);

		Map dbDemand = entityService.getById(demandMetadata, id, user.asSystem());
		String status = MapUtils.getString(dbDemand, "status");
		if (!DemandStatus.New.toString().equals(status)) {
			throw new BusinessException("此状态不能删除！");
		}
		entityService.deleteById(demandMetadata, Integer.toString(id), user);
	}

	public void deleteDemand(Integer[] demandIds, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(demandIds)) {
			throw new BusinessException("未指定要删除的要货申请");
		}
		demandIds = TpltUtils.removeDuplicate(demandIds, Integer.class);
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DEMAND);

        //如果与客订单关联，更新客订单状态为新建
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("id", demandIds);
        List<Map<String,Object>> ds = entityService.list(metadata,where,null,user);
        for(Map<String,Object> d:ds){
            int orderId = MapUtils.getInteger(d,"orderId",-1);
            if (orderId>0) {
                cusOrderService.updateStatus(orderId, CusOrderStatus.demanding, CusOrderStatus.New, new StatusCarrier(){
                    public void active(Map<String, Object> order) throws BusinessException {
                        throw new BusinessException("更新客订单状态失败");
                    }
                }, user);
            }
        }

		where.put("status", DemandStatus.New);

		int row = entityService.delete(metadata, where, user);
		if (row < demandIds.length) {
			throw new BusinessException("请选择状态为【" + DemandStatus.New.getText() + "】的要货申请");
		} else if (row > demandIds.length) {
			throw new BusinessException("删除要货申请发生异常");
		}
	}

	@Override
	protected EntityMetadata getBillMetadata() throws BusinessException {
		return metadataProvider.getEntityMetadata(MzfEntity.DEMAND);
	}

	@Override
	protected String getBillName() {
		return "商品要货申请";
	}
}



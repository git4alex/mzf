package com.zonrong.register.service;

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.metadata.service.MetadataProvider;
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
public class RegisterProductService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private ProductService productService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private RegisterService registerService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	public int register(int orderDetailId, Map<String, Object> product, Map<String, Object> register, IUser user) throws BusinessException {
		Map<String, Object> dbDetail = entityService.getById(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL_VIEW, orderDetailId, user.asSystem());

		Integer demandId = MapUtils.getInteger(dbDetail, "demandId");
		ProductStatus status = ProductStatus.free;
		String statusRemark = null;
		if (demandId != null) {
			//如果商品与要货申请关联，则商品状态为锁定
			status = ProductStatus.locked;
			statusRemark = "要货申请：[" + MapUtils.getString(dbDetail, "demandNum") + "]；";
			String cusOrderNum = MapUtils.getString(dbDetail, "cusOrderNum");
			if (StringUtils.isNotBlank(cusOrderNum)) {
				statusRemark += "客订单：[" + cusOrderNum + "]；";
			}
		}
		//商品登记
		List<Map<String, Object>> diamondList = (List)MapUtils.getObject(product, "diamondList");
		List<Map<String, Object>> certificateList = (List)MapUtils.getObject(product, "certificateList");
		int productId = productService.createProduct(product, diamondList, certificateList, status, statusRemark, user);
//		ProductType ptype = ProductType.valueOf(MapUtils.getString(product, "ptype"));

		//核销订单明细
		cancelProductOrderDetail(dbDetail, productId, user);

		//商品入库（入临时库）
		StorageType storageType = StorageType.product_temporary;
        int orderId = MapUtils.getInteger(dbDetail,"orderId");
        Map<String,Object> order = entityService.getById(MzfEntity.VENDOR_ORDER,orderId,user);
        String orderNum=MapUtils.getString(order,"num");
		productInventoryService.warehouse(MzfEnum.BizType.register, productId,
                user.getOrgId(), storageType, user.getId(), "订单编号：["+orderNum+"]", user);

		//收货记录
//		Integer orderId = MapUtils.getInteger(dbDetail, "orderId");
		int receiveId = registerService.createRegister(register, TargetType.product, orderId, orderDetailId, productId, new BigDecimal(1), user);
		//记录操作日志
		businessLogService.log("商品收货登记", "商品编号：" + productId, user);

		return receiveId;
	}

	public Map<String, Object> getPrintData(Integer[] ids, IUser user)throws BusinessException{
		Map<String, Object> data = new HashMap<String, Object>();
		List<Map<String, Object>> proList = new ArrayList<Map<String,Object>>();
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", ids);
		List<Map<String, Object>> dataList = entityService.list(MzfEntity.REGISTER_VIEW, where, null, user);
		String vendorName = "";
		String orderNum = "";
		String vendorNum = "";
		if(CollectionUtils.isNotEmpty(dataList)){
			vendorName = MapUtils.getString(dataList.get(0), "vendorName");
			orderNum = MapUtils.getString(dataList.get(0), "orderNum");
		}
		for (Map<String, Object> map : dataList) {
			Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, MapUtils.getIntValue(map, "targetId", 0), user);
			vendorNum = MapUtils.getString(product, "vendorOrderNum");
			product.put("tempNum", "L" + MapUtils.getString(product, "id", "0"));
			product.put("cdateStr", MapUtils.getString(map, "cdateStr", ""));
			proList.add(product);

		}
		data.put("orderNum", orderNum);
		data.put("vendorNum", vendorNum);
		data.put("vendorName", vendorName);
		data.put("dataList", proList);
		return data;
	}

	private void cancelProductOrderDetail(Map<String, Object> detail, final int productId, IUser user) throws BusinessException {
		//核销明细
		CancelDetailTemplete templete = new CancelDetailTemplete(metadataProvider, entityService){
			public IEntityCode getDetailEntityCode(){
				return MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL;
			}
			public void putObjectId(Map<String, Object> field) {
				field.put("productId", productId);
			}
		};
		templete.cancelDetail(detail, user);

		int orderDetailId = MapUtils.getInteger(detail, "id");
		int transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, Integer.toString(orderDetailId), user);
		logService.createLog(transId, MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, Integer.toString(orderDetailId), "核销订单明细", TargetType.product, productId, null, user);

	}
}



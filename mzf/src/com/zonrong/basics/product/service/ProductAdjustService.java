package com.zonrong.basics.product.service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;

/**
 * date: 2010-11-2
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ProductAdjustService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	public enum AdjustStatus{
		New,
		waitConfirm,
		canceled,
        sold,
		over;

		public String getText() {
			return BizCodeService.getBizName("adjustStatus", this.toString());
		}
	}

	public void createAdjust(List<Map<String,Object>> queryParams,Float pm,Float pi,Float pt,String remark,IUser user) throws BusinessException {
//		if (ArrayUtils.isEmpty(productIds)) {
//			throw new BusinessException("未指定进行调价的商品");
//		}

//		Map<String, Object> where = new HashMap<String, Object>();
//		where.put("id", productIds);
		//where.put("adjustStatus", Boolean.toString(false));
		//where.put("orgId", user.getOrgId());
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT_INVENTORY_VIEW, queryParams, null, User.getSystemUser());
//		if (list.size() < productIds.length) {
//			throw new BusinessException("系统异常，某些商品无法进行调价，请联系系统管理员进行处理。");
//		}
        //String current = new SimpleDateFormat("yyyy-MM-dd H:m:s").format(new Date());
        Timestamp t = new Timestamp(new Date().getTime());
        for (Map<String, Object> product : list) {
			Integer productId = MapUtils.getInteger(product, "id");
			Map<String, Object> field = new HashMap<String, Object>();
            Float retailBasePrice = MapUtils.getFloat(product, "retailBasePrice");
			Float promotionPrice = MapUtils.getFloat(product, "promotionPrice", retailBasePrice);
            Integer newPromotionPrice = Math.round(pt!=null?pt:retailBasePrice*pm+pi);

			field.put("productId", productId);
			field.put("status", AdjustStatus.waitConfirm);
			field.put("oldPromotionPrice", promotionPrice);
            field.put("adjustPrice", newPromotionPrice);
            field.put("cPrice",MapUtils.getFloatValue(product,"costPrice"));
            field.put("wPrice",MapUtils.getFloatValue(product,"wholesalePrice"));
            field.put("rPrice",retailBasePrice);
			field.put("cuserId", null);
            field.put("cuserName", null);
			field.put("cdate", t);
			field.put("orgId", MapUtils.getInteger(product, "orgId"));
            field.put("remark",remark);

			String id = entityService.create(MzfEntity.PRODUCT_ADJUST, field, user);

			//记录流程
			int transId = transactionService.createTransId();
			logService.createLog(transId, MzfEntity.PRODUCT_ADJUST, id, "批量价格调整", TargetType.product, productId, null, user);
			//记录操作日志
			businessLogService.log("商品申请调价(商品库存)", "商品编号:" + productId, user);
		}
	}

	public void cancelAdjust(Integer[] adjustIds, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(adjustIds)) {
			throw new BusinessException("未指定调价申请");
		}

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", adjustIds);
		where.put("status", new String[]{AdjustStatus.New.toString(), AdjustStatus.waitConfirm.toString()});

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", AdjustStatus.canceled);

		int row = entityService.update(MzfEntity.PRODUCT_ADJUST, field, where, user);
		if (row != adjustIds.length) {
			throw new BusinessException("取消调价发生异常");
		}

		//记录流程
		where = new HashMap<String, Object>();
		where.put("id", adjustIds);
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT_ADJUST_VIEW, where, null, user);
		for (Map<String, Object> adjust : list) {
			Integer adjustId = MapUtils.getInteger(adjust, "id");
			Integer productId = MapUtils.getInteger(adjust, "productId");
			int transId = transactionService.findTransId(MzfEntity.PRODUCT_ADJUST, adjustId.toString(), user);
			logService.createLog(transId, MzfEntity.PRODUCT_ADJUST, adjustId.toString(), "取消调价", TargetType.product, productId, null, user);
		}
	}

	/**
	 * 调价
	 * @param list
	 * @param user
	 * @throws BusinessException
	 */
	public void adjustPrice(List<Map<String, Object>> list, IUser user) throws BusinessException {
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未指定商品");
		}

		List<Integer> adjustIdList = new ArrayList<Integer>();
		for (Map<String, Object> product : list) {
			Object adjustPrice = MapUtils.getObject(product, "adjustPrice");
			Integer id = MapUtils.getInteger(product, "id");
			adjustIdList.add(id);
			if (adjustPrice == null || id == null) {
				throw new BusinessException("缺少参数");
			}

			Integer[] adjustIds = adjustIdList.toArray(new Integer[]{});
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("id", id);
			where.put("status", new String[]{AdjustStatus.New.toString(), AdjustStatus.waitConfirm.toString()});

			Map<String, Object> field = new HashMap<String, Object>();
			field.put("status", AdjustStatus.waitConfirm);
			field.put("adjustPrice", adjustPrice);

			int row = entityService.update(MzfEntity.PRODUCT_ADJUST, field, where, user);
		}
	}

	/**
	 * 确认调价
	 * @param user
	 * @throws BusinessException
	 */
	public void confirm(Integer[] adjustIds, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(adjustIds)) {
			throw new BusinessException("未指定调价记录");
		}

		Map<String, Object> where = new HashMap<String, Object>();
		Map<String, Object> field = new HashMap<String, Object>();
		where.put("id", adjustIds);
		where.put("status", AdjustStatus.waitConfirm);
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT_ADJUST_VIEW, where, null, user);
		for (Map<String, Object> adjust : list) {
			Integer adjustId = MapUtils.getInteger(adjust, "id");
			Integer productId = MapUtils.getInteger(adjust, "productId");
			Object adjustPrice = MapUtils.getFloat(adjust, "adjustPrice");

            String ps = MapUtils.getString(adjust,"productStatus");
            String logRemark;
            if(StringUtils.equalsIgnoreCase(ps, "selled")){
                field.put("status",AdjustStatus.sold);
                field.put("appDate",null);
                entityService.updateById(MzfEntity.PRODUCT_ADJUST, adjustId.toString(), field, user);
                logRemark = "商品已售，价格不需要再进行调整";
            }else{
                field.put("status",AdjustStatus.over);
                field.put("appDate",null);
                entityService.updateById(MzfEntity.PRODUCT_ADJUST, adjustId.toString(), field, user);
                field.clear();
                field.put("promotionPrice", adjustPrice);
                entityService.updateById(MzfEntity.PRODUCT, productId.toString(), field, user);
                logRemark = "新促销一口价为："+adjustPrice;
            }

			int transId = transactionService.findTransId(MzfEntity.PRODUCT_ADJUST, adjustId.toString(), user);
			logService.createLog(transId, MzfEntity.PRODUCT_ADJUST, adjustId.toString(), "确认调价", TargetType.product, productId, logRemark, user);
			//记录操作日志
			businessLogService.log("确认调价", "商品{"+productId+"},调价为:" + adjustPrice, user);
		}


//		where = new HashMap<String, Object>();
//		where.put("id", adjustIds);
//		where.put("status", AdjustStatus.waitConfirm);
//
//		field = new HashMap<String, Object>();
//		field.put("status", AdjustStatus.over);
//		field.put("appDate", null);
//		int row = entityService.update(MzfEntity.PRODUCT_ADJUST, field, where, user);
//		if (row != adjustIds.length) {
//			throw new BusinessException("确认调价发生异常");
//		}
	}
}



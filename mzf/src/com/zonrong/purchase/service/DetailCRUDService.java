package com.zonrong.purchase.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.templete.SaveTemplete;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;

/**
 * date: 2010-10-26
 *
 * version: 1.0
 * commonts: ......
 */
@Service
@Scope("prototype")
public class DetailCRUDService {
	private static Logger logger = Logger.getLogger(DetailCRUDService.class);
	@Resource
	private EntityService entityService;

	public enum VendorOrderDetailStatus {
		New,			//新创建
		waitReceive,	//待收货
		received,		//已收货
        canceled        //已取消
	}

	public EntityMetadata entityMetadata;
	public EntityMetadata getEntityMetadata() throws BusinessException {
		return this.entityMetadata;
	}

	public void setEntityMetadata(EntityMetadata entityMetadata) {
		this.entityMetadata = entityMetadata;
	}

	public void saveDetail(final int orderId, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		for (Map<String, Object> detail : detailList) {
			String status = MapUtils.getString(detail, "status");
			if (StringUtils.isBlank(status)) {
				detail.put("status", VendorOrderDetailStatus.New);
			}
		}

		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return entityMetadata;
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("orderId", orderId);
			}

		};

		templete.save(detailList, user);
	}

	public int createDetail(Map<String, Object> detail, IUser user) throws BusinessException {
		detail.put("status", VendorOrderDetailStatus.New);
		String id = entityService.create(getEntityMetadata(), detail, user);
		return Integer.parseInt(id);
	}

	public int updateDetail(int detailId, Map orderDetail, IUser user) throws BusinessException {
		EntityMetadata metadata = getEntityMetadata();
		orderDetail.remove(metadata.getPkCode());

		return entityService.updateById(metadata, Integer.toString(detailId), orderDetail, user);
	}

	public int updateDetailStatusByOrderIds(Integer[] orderIds, VendorOrderDetailStatus status, IUser user) throws BusinessException {
		EntityMetadata metadata = getEntityMetadata();

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", status);

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orderId", orderIds);

		return entityService.update(metadata, field, where, user);
	}

	public int deleteDetail(int detailId, IUser user) throws BusinessException {
		return entityService.deleteById(getEntityMetadata(), Integer.toString(detailId), user);
	}

	public int deleteDetailByOrderId(int orderId, IUser user) throws BusinessException {
		Map where = new HashMap();
		where.put("orderId", orderId);
		return entityService.delete(getEntityMetadata(), where, user);
	}

}



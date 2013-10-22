package com.zonrong.common.service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;

/**
 * date: 2011-3-12
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public abstract class BillStatusService<T extends Enum> {
	@Resource
	private EntityService entityService;
	
	protected abstract EntityMetadata getBillMetadata() throws BusinessException;
	
	protected abstract String getBillName();
	
	public T getStatus(int billId, Class<T> enumType, IUser user) throws BusinessException {
		Map<String, Object> bill = entityService.getById(getBillMetadata(), billId, user.asSystem());
		if (MapUtils.isEmpty(bill)) {
			throw new BusinessException("未找到" + getBillName());
		}
		String dbStatusStr = MapUtils.getString(bill, "status");
		return (T) Enum.valueOf(enumType, dbStatusStr);
	}
	
	public void updateStatus(int billId, T priorStatus, T nextStatus, StatusCarrier carrier, IUser user) throws BusinessException {
		T[] array = (T[])Array.newInstance(nextStatus.getClass(), 1);
		List<T> priorStatusList = new ArrayList<T>();
		if (priorStatus != null) {			
			priorStatusList.add(priorStatus);
		}
		T[] cusOrderStatus = (T[])priorStatusList.toArray(array);
		updateStatus(billId, cusOrderStatus, nextStatus, carrier, user);		
	}
	
	public void updateStatus(int billId, T[] priorStatus, T nextStatus, StatusCarrier carrier, IUser user) throws BusinessException {
		Map<String, Object> bill = entityService.getById(getBillMetadata(), billId, user.asSystem());
		if (MapUtils.isEmpty(bill)) {
			throw new BusinessException("未找到" + getBillName());
		}
		
		if (!ArrayUtils.isEmpty(priorStatus)) {
			boolean flag = false;
			String dbStatusStr = MapUtils.getString(bill, "status");
			T dbStatus = (T) Enum.valueOf(nextStatus.getClass(), dbStatusStr);
			for (T status : priorStatus) {
				if (status == dbStatus) {
					flag = true;
					break;
				}
			}
			if (!flag && carrier != null) {
				carrier.setCarrier(bill);
				carrier.active(bill);
			}	
		}
		
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", nextStatus);
		field.put("muserId", null);
		field.put("muserName", null);
		field.put("mdate", null);		
		int row = entityService.updateById(getBillMetadata(), Integer.toString(billId), field, user);
		if (row == 0) {
			throw new BusinessException("更新" + getBillName() + "状态失败");
		}
	}
}



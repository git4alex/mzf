package com.zonrong.shiftwork.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.system.service.BizCodeService;

/**
 * date: 2010-12-30
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ShiftWorkService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private EntityService entityService;
	@Resource
	private BusinessLogService businessLogService;
	
	public enum ShiftWorkCode {
		/**
		 * 非门店的班组用户无需接班
		 */
		ignore,
		/**
		 * 当前班组重新登录无需接班
		 */
		sameWorkGroup,
		/**
		 * 上一班组正在销售，等待对方交班
		 */
		denied,
		/**
		 * 未找到交班记录
		 */
		notFoundShiftWork,
		/**
		 * 可正常接班
		 */
		shiftWork
	}
	
	public enum ShiftWorkStatus {
		waiting,
		successful,
		failing
	}
	
	private int create(Map<String, Object> shiftWork, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		//查找接班员工
		Map<String, Object> employee = getWorkGroupEmployee(user);
		//如果是店长则不一定有员工信息
//		if (employee == null) {
//			throw new BusinessException("未找到该用户对应的员工信息");
//		}
		String myWorkGroup = MapUtils.getString(employee, "workGroup");
		
		//查找接班门店
		Map<String, Object> store = getStoreByOrgId(user);
		int storeId = MapUtils.getInteger(store, "id");
		String storeName = MapUtils.getString(store, "name");
		
		//删除删词无效的交班记录
		deleteInvalid(storeId, myWorkGroup, user);

		//创建交班记录
		String fromRemark = MapUtils.getString(shiftWork, "fromRemark");
		shiftWork.clear();
		shiftWork.put("storeId", storeId);
		shiftWork.put("storeName", storeName);
		shiftWork.put("status", ShiftWorkStatus.waiting);
		shiftWork.put("fromWorkGroup", myWorkGroup);
		shiftWork.put("fromRemark", fromRemark);
		shiftWork.put("fromUserId", user.getId());
		shiftWork.put("fromUserName", user.getName());
		shiftWork.put("fromDate", null);		
		String id = entityService.create(MzfEntity.SHIFT_WORK, shiftWork, user);
		Integer shiftWorkId = Integer.parseInt(id);
		
		if (CollectionUtils.isNotEmpty(detailList)) {			
			for (Map<String, Object> detail : detailList) {
				detail.put("shiftWorkId", shiftWorkId);
				entityService.create(MzfEntity.SHIFT_WORK_DETAIL, detail, user);
			}
		}
		return shiftWorkId;
	}
	
	/**
	 * 删除删词无效的交班记录
	 * 
	 * @param user
	 * @throws BusinessException
	 */
	public void deleteInvalid(int storeId, String myWorkGroup, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("storeId", storeId);
		where.put("status", ShiftWorkStatus.waiting);
		where.put("fromWorkGroup", myWorkGroup);
		List<Map<String, Object>> list = entityService.list(MzfEntity.SHIFT_WORK, where, null, user);
		
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		
		List<Integer> shiftWorkIds = new ArrayList<Integer>();
		for (Map<String, Object> shiftWrok : list) {
			shiftWorkIds.add(MapUtils.getInteger(shiftWrok, "id"));
		}
		
		where = new HashMap<String, Object>();
		where.put("shiftWorkId", shiftWorkIds.toArray(new Integer[]{}));
		entityService.delete(MzfEntity.SHIFT_WORK_DETAIL, where, user);
		
		where = new HashMap<String, Object>();
		where.put("id", shiftWorkIds.toArray(new Integer[]{}));
		entityService.delete(MzfEntity.SHIFT_WORK, where, user);
	}
	
	public int handOver(Map<String, Object> shiftWork, List<Map<String, Object>> detailList, IUser user) throws BusinessException {
		//新建交接班信息
		int shiftWorkId = create(shiftWork, detailList, user);
		
		Map<String, Object> store = getStoreByOrgId(user);
		String storeId = MapUtils.getString(store, "id");
		
		//解锁
		store.clear();
		store.put("currWorkGroup", null);
		store.put("shiftWorkId", shiftWorkId);
		entityService.updateById(MzfEntity.STORE, storeId, store, user);
		//记录操作日志
		businessLogService.log("交班", null, user);
		return shiftWorkId;
	}
	
	public void takeOver(Map<String, Object> shiftWork, IUser user) throws BusinessException {
		//查找接班员工
		Map<String, Object> employee = getWorkGroupEmployee(user);
		if (employee == null) {
			throw new BusinessException("未找到该用户对应的员工信息");
		}
		String myWorkGroup = MapUtils.getString(employee, "workGroup");
		
		//查找接班门店
		Map<String, Object> store = getStoreByOrgId(user);
		String storeId = MapUtils.getString(store, "id");
		String shiftWorkId = MapUtils.getString(store, "shiftWorkId");
		
		//更新交接班记录
		ShiftWorkStatus status = ShiftWorkStatus.valueOf(MapUtils.getString(shiftWork, "status"));
		String toRemark = MapUtils.getString(shiftWork, "toRemark");
		shiftWork.clear();
		shiftWork.put("status", status);
		shiftWork.put("toWorkGroup", myWorkGroup);
		shiftWork.put("toRemark", toRemark);
		shiftWork.put("toUserId", user.getId());
		shiftWork.put("toUserName", user.getName());
		shiftWork.put("toDate", null);
		entityService.updateById(MzfEntity.SHIFT_WORK, shiftWorkId, shiftWork, user);
		
		//锁定门店状态(班组信息、交接班信息)
		store.clear();
		store.put("currWorkGroup", myWorkGroup);
		store.put("shiftWorkId", null);
		entityService.updateById(MzfEntity.STORE, storeId, store, user);
		//记录操作日志
		businessLogService.log("接班", null, user);
	}
	
	public boolean isNeedShiftWork(IUser user) throws BusinessException {
		try {
			Map<String, Object> store = getStoreByOrgId(user);	
			Map<String, Object> employee = getWorkGroupEmployee(user);
			String myWorkGroup = (String) employee.get( "workGroup");
			String currWorkGroup = (String) store.get("currWorkGroup");
			String shiftWorkId = (String) store.get("shiftWorkId");
			if (myWorkGroup.equals(currWorkGroup) && StringUtils.isBlank(shiftWorkId)) {
				return true;
			}
		} catch (Exception e) {
			if(logger.isDebugEnabled()) {
				logger.debug(e.getMessage());
			}
		}
		return false;
	}
	
	public Map<String, Object> getShiftWorkCode(IUser user) throws BusinessException {
		Map<String, Object> shiftWork = new HashMap<String, Object>();
		Map<String, Object> employee = getWorkGroupEmployee(user);
		
		String myWorkGroup = MapUtils.getString(employee, "workGroup");	
		if (StringUtils.isBlank(myWorkGroup)) {
			shiftWork.put("code", ShiftWorkCode.ignore);
//			shiftWork.put("isShiftWork", false);
			return shiftWork;
		}
		
		Map<String, Object> store = getStoreByOrgId(user);					
		String currWorkGroup = MapUtils.getString(store, "currWorkGroup");
		if (StringUtils.isNotBlank(currWorkGroup)) {			
			if (currWorkGroup.equalsIgnoreCase(myWorkGroup)) {
				shiftWork.put("code", ShiftWorkCode.sameWorkGroup);
//				shiftWork.put("isShiftWork", true);
			} else {
				shiftWork.put("code", ShiftWorkCode.denied);
//				shiftWork.put("isShiftWork", false);
			}
			return shiftWork;
		} 
		
		String shiftWorkId = MapUtils.getString(store, "shiftWorkId");
		if (StringUtils.isNotBlank(shiftWorkId)) {
			Map<String, Object> dbShiftWork = entityService.getById(MzfEntity.SHIFT_WORK, shiftWorkId, user.asSystem());
			String fromWorkGroup = MapUtils.getString(dbShiftWork, "fromWorkGroup");
			if (myWorkGroup.equalsIgnoreCase(fromWorkGroup)) {
				shiftWork.put("code", ShiftWorkCode.sameWorkGroup);
//				shiftWork.put("isShiftWork", true);
				
				String storeId = MapUtils.getString(store, "id");
				store.clear();
				store.put("currWorkGroup", myWorkGroup);
				store.put("shiftWorkId", null);
				entityService.updateById(MzfEntity.STORE, storeId, store, user);				
				return shiftWork;
			} 				
		} else {
			shiftWork.put("code", ShiftWorkCode.notFoundShiftWork);
//			shiftWork.put("isShiftWork", false);
			return shiftWork;
		}			
		
		shiftWork.put("code", ShiftWorkCode.shiftWork);
		shiftWork.put("shiftWorkId", shiftWorkId);
		shiftWork.put("workGroup", myWorkGroup);
//		shiftWork.put("isShiftWork", true);
		return shiftWork;
	}
	
	public void reset(Map<String, Object> shiftWork, IUser user) throws BusinessException {
		ShiftWorkCode code = (ShiftWorkCode) shiftWork.get("code");
		if (ShiftWorkCode.sameWorkGroup != code) {			
			return;
		}
		
		Map<String, Object> employee = getWorkGroupEmployee(user);
		if (employee == null) {
			throw new BusinessException("未找到该用户对应的员工信息");
		}
		
		String myWorkGroup = MapUtils.getString(employee, "workGroup");		
		Map<String, Object> store = getStoreByOrgId(user);
		String storeId = MapUtils.getString(store, "id");		
		
		store.clear();
		store.put("currWorkGroup", myWorkGroup);
		store.put("shiftWorkId", null);
		entityService.updateById(MzfEntity.STORE, storeId, store, user);		
	}
	
	private Map<String, Object> getWorkGroupEmployee(IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("userId", user.getId());
		String[] workGroup = BizCodeService.getBizVslues("workGroup");
		where.put("workgroup", workGroup);
		List<Map<String, Object>> list = entityService.list(MzfEntity.EMPLOYEE_VIEW, where, null, user.asSystem());
		if (CollectionUtils.isEmpty(list)) {
			return null;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			throw new BusinessException("多个员工对应同一用户");
		}	
	}
	
	public Map<String, Object> getStoreByOrgId(IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("orgId", user.getOrgId());
		
		List<Map<String, Object>> list = entityService.list(MzfEntity.STORE_VIEW, where, null, user);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("该用户非门店人员");
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			throw new BusinessException("多个门店对应同一组织机构");
		}	
	}	
}



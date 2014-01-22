package com.zonrong.summary.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.TreasuryEarnestService;
import com.zonrong.inventory.service.TreasurySaleService;
import com.zonrong.shiftwork.service.ShiftWorkService;

/**
 * date: 2011-3-1
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class SummaryService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private TreasurySaleService treasurySaleService;
	@Resource
	private TreasuryEarnestService treasuryEarnestService;
	@Resource
	private ShiftWorkService shiftWorkService;

	public int doSummary(Map<String, Object> summary, IUser user) throws BusinessException {
		String remark = MapUtils.getString(summary, "remark");
		int id = createSummary(remark, user);

		int orgId = user.getOrgId();
		treasurySaleService.doSummary(orgId, MzfEntity.SUMMARY, id, remark, user);
		treasuryEarnestService.doSummary(orgId, MzfEntity.SUMMARY, id, remark, user);

		return id;
	}

	private int createSummary(String remark, IUser user) throws BusinessException {
		Map<String, Object> store = shiftWorkService.getStoreByOrgId(user);
		Integer storeId = MapUtils.getInteger(store, "id");

		Map<String, Object> summary = new HashMap<String, Object>();
		summary.put("storeId", storeId);
		summary.put("remark", remark);
		summary.put("cuserId", user.getId());
		summary.put("cuserName", user.getName());
		summary.put("cdate", null);

		String id = entityService.create(MzfEntity.SUMMARY, summary, user);

		return Integer.parseInt(id);
	}

	public boolean isNeedSummary(IUser user) throws BusinessException {
		try {
			Map<String, Object> store = shiftWorkService.getStoreByOrgId(user);

			int orgId = user.getOrgId();
			Map<String, Object> where = new HashMap<String, Object>();
			where.put("orgId", orgId);
			List<Map<String, Object>> list = entityService.list(MzfEntity.SUMMARY_TODAY_VIEW, where, null, user);
			if (CollectionUtils.isEmpty(list)) {
				return true;
			}
		} catch (Exception e) {
			if(logger.isDebugEnabled()) {
				logger.debug(e.getMessage());
			}
		}

		return false;
	}
}



package com.zonrong.basics.customer.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * 会员升级规则
 * 
 * @author Administrator 2011-08-25
 */
@Service
public class UpgradeRuleService {

	private Logger logger = Logger.getLogger(this.getClass());
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;

	public String getGradeByCode(int points) throws BusinessException {
		EntityMetadata metadata = metadataProvider
				.getEntityMetadata(MzfEntity.CUSTOMER_UPGRADE_RULE);
		OrderBy orderBy = new OrderBy(new String[] { "points" }, OrderByDir.asc);
		List<Map<String, Object>> grades = entityService.list(metadata,
				new HashMap<String, Object>(), orderBy, User.getSystemUser());

		String gradeName = null; // 等级名称
		for (int i = 0; i < grades.size(); i++) {
			Map<String, Object> grade = grades.get(i);
			if (grade.get("points") != null && grade.get("grade") != null) {
				int start = Integer.parseInt(grade.get("points").toString());
				if ((i + 1) < grades.size()) {
					String nextPoints = grades.get(i + 1).get("points").toString();
					int end = Integer.parseInt(nextPoints);
					if (points >= start && points < end) {
						gradeName = grade.get("grade").toString();
						break;
					}
				} else {
					if (points >= start) {
						gradeName = grade.get("grade").toString();
					}

				}
			}
		}
		if (gradeName != null) {
			return gradeName;
		} else {
			//throw new BusinessException("没有符合该分数的会员等级");
			return null;
		}

	}
}

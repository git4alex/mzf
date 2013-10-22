package com.zonrong.core.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;


/**
 * date: 2010-12-6
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class TransactionService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private Dao dao;

	public int findTransId(IEntityCode entityCode, String entityId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.BIZ_LOG);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("entityId", entityId);
		where.put("entityCode", entityCode);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());

		if (CollectionUtils.isNotEmpty(list)) {
			Map<String, Object> bizLog = list.get(0);
			return MapUtils.getIntValue(bizLog, "transId");
		} else {
			logger.debug("未找到所处流程: " + entityCode);
			return createTransId();
			//throw new BusinessException("未找到所处流程");
		}
	}

	public int createTransId() throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.BIZ_LOG);
		QueryParam qp = new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.addColumn("max(trans_id)", "mzxTransId");
		Map<String, Object> map = dao.get(qp);
		Integer transId = MapUtils.getInteger(map, "mzxTransId");
		if (transId == null) {
			return 1;
		} else {
			return transId + 1;
		}
	}

}



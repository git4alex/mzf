package com.zonrong.inventory.service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.service.MetadataProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * date: 2010-11-3
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class TreasuryEarnestService extends TreasuryService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private TreasurySaleService treasurySaleService;

	public String getStorageName() {
		return "定金库";
	}

	@Override
	public TreasuryType getTreasuryType() {
		return TreasuryType.earnest;
	}

	public void setClass2(Map<String, Object> where, MoneyStorageClass1 class1, String class2) throws BusinessException {
		if (class1 != MoneyStorageClass1.cash && class1 != MoneyStorageClass1.bankCard) {
			throw new BusinessException(getStorageName() + "只支持现金和银行卡");
		}

		if (class1 == MoneyStorageClass1.bankCard) {
			if (StringUtils.isBlank(class2)) {
				throw new BusinessException("操作" + getStorageName() + "时，付款方式为银行卡时必须指定银行");
			}
			where.put("class2", class2);
		}
//        else {
//			if (StringUtils.isNotBlank(class2)) {
//				throw new BusinessException("不必指定银行");
//			}
//		}
	}
}



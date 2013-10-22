package com.zonrong.inventory.treasury.service;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-11-3
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class TreasurySaleService extends TreasuryService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;

	public String getStorageName() {
		return "现金库";
	}

	@Override
	public TreasuryType getTreasuryType() {
		return TreasuryType.sale;
	}

	public void setClass2(Map<String, Object> where, MoneyStorageClass1 class1, String class2) throws BusinessException {
		if (class1 == MoneyStorageClass1.bankCard ||
				class1 == MoneyStorageClass1.valueCard ||
				class1 == MoneyStorageClass1.coBrandedCard ||
				class1 == MoneyStorageClass1.foreignCard) {
			
			if (StringUtils.isBlank(class2)) {
				throw new BusinessException("操作" + getStorageName() + "时，付款方式为" + class1.getName() + "时必须指定银行");
			}
			where.put("class2", class2);
		}
	}
}



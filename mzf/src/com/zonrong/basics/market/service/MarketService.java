package com.zonrong.basics.market.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.templete.SaveTemplete;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-9-28
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class MarketService {
	private Logger logger = Logger.getLogger(this.getClass());
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	
	public int createMarket(Map<String, Object> market, List<Map<String, Object>> linkmanList, IUser user) throws BusinessException {
		EntityMetadata martetMaterial = metadataProvider.getEntityMetadata(MzfEntity.MARKET);
		String id = entityService.create(martetMaterial, market, user);
		int marketId = Integer.parseInt(id);
		
		saveLinkman(marketId, linkmanList, user);
		return marketId;
	}
	
	public void updateMarket(int marketId, Map<String, Object> market, List<Map<String, Object>> linkmanList, IUser user) throws BusinessException {
		EntityMetadata martetMaterial = metadataProvider.getEntityMetadata(MzfEntity.MARKET);
		int row = entityService.updateById(martetMaterial, Integer.toString(marketId), market, user);
		if (row == 0) {
			throw new BusinessException("未能修改商场资料");
		}
		
		saveLinkman(marketId, linkmanList, user);		
	}	
	
	private void saveLinkman(final int marketId, List<Map<String, Object>> linkmanList, IUser user) throws BusinessException {
		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return metadataProvider.getEntityMetadata(MzfEntity.LINKMAN);
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("marketId", marketId);
				map.remove("vendorId");
			}			
		};
		
		templete.save(linkmanList, user);
	}	
}



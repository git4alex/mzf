package com.zonrong.basics.charges.service;

import java.util.ArrayList;
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
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * 2011-08-23
 * @author Administrator
 *  商品工费管理
 */
@Service
public class ChargesService {
	private Logger logger = Logger.getLogger(this.getClass());
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	
	public int createCharges(Map<String,Object> charges,IUser user) throws BusinessException{
		boolean isExis  = isExists(charges, user);
		if (isExis) {
			throw new BusinessException("该项目已经设置");
		}
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PROCESSING_CHARGES);
		charges.put("uuserId", null);
		charges.put("udate", null);
        String id = entityService.create(metadata, charges, user);
		return Integer.parseInt(id);
	}
	
	public int updateCharges(Map<String,Object> charges,IUser user) throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PROCESSING_CHARGES);
		Map<String, Object> where = new HashMap<String, Object>();
		
		where.put("orgId", charges.get("orgId"));
		where.put("pkind", charges.get("pkind"));
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("uuserId", null);
		field.put("udate", null);
		field.put("charges", charges.get("charges"));
		return entityService.update(metadata, field, where, user);
	}
	
	public List<Map> getChargeViewData()throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PROCESSING_CHARGES);
		List<Map<String, Object>> list = entityService.list(metadata, new HashMap<String, Object>(), null, User.getSystemUser());
		
		List<String> pkindIds = new ArrayList<String>();  //所有的商品种类
		List<Map> chargesList = new ArrayList<Map>();     //保存所有数据
		Map<Integer,Map<String,Double>> chargePkindMap = new HashMap<Integer,Map<String,Double>>();
		for(int i = 0;i < list.size();i++){
			
			 Map<String,Object> charges = list.get(i);
			 int orgId = Integer.parseInt(charges.get("orgId").toString());
			 String pkind = charges.get("pkind").toString();
			 Map<String,Double> pkChargesMap =  chargePkindMap.get(orgId);
			 if(pkChargesMap == null || pkChargesMap.size() == 0){
				 pkChargesMap = new HashMap<String,Double>();
				 pkChargesMap.put(pkind, Double.parseDouble(charges.get("charges").toString()));
				 chargePkindMap.put(orgId, pkChargesMap);
			 }else{
				 pkChargesMap.put(pkind, Double.parseDouble(charges.get("charges").toString()));
			 }
			 
			 if(!pkindIds.contains(pkind)){
				 pkindIds.add(pkind);
			 }
			
		}
		Map<Integer,String> orgMap = getOrgs();  //所有的部门
		for(int i = 0;i< list.size();i++){
			 Map<String,Object> charges = list.get(i);
			 Map map = new HashMap();
			 int orgId = Integer.parseInt(charges.get("orgId").toString());
			 String orgName = orgMap.get(orgId);
			 Map<String,Double> pkChargesMap =  chargePkindMap.get(orgId);
			 map.put("orgName", orgName);
			 for(String pId:pkindIds){
				 if(pkChargesMap.containsKey(pId)){
					 map.put(pId, pkChargesMap.get(pId));
				 }else{
					 map.put(pId, null);
				 }
			 }
			 chargesList.add(map);
		}
		
		return chargesList;
		
	}
	
	private Map<Integer,String> getOrgs() throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.ORG);
		List<Map<String, Object>> list = entityService.list(metadata, new HashMap<String, Object>(), null, User.getSystemUser());
		Map<Integer,String> orgs = new HashMap<Integer,String>();
		for(int i = 0;i < list.size();i++){
			Map<String,Object> org = list.get(i);
			if(org != null){
				int id = Integer.parseInt(org.get("id").toString());
				orgs.put(id,org.get("text").toString());	
			}
		}
		return orgs;
		
	}
	private boolean isExists(Map<String,Object> charges,IUser user) throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PROCESSING_CHARGES);
		OrderBy orderBy = new OrderBy(new String[]{"id"},OrderByDir.desc);
		Map<String,Object> where = new HashMap<String,Object>();
		where.put("orgId",charges.get("orgId"));
		where.put("pkind",charges.get("pkind"));
		List existsLis = entityService.list(metadata, where, orderBy, user);
		
		if(existsLis.size() > 0){
			return true;
		}
		return false;
	}
	
	
	
}

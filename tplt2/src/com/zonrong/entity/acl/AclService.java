package com.zonrong.entity.acl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.security.IUser;

@Service
public class AclService {
	@Resource
	private Dao dao;
	
	private static Map<String,List<AclRule>> ruleMap=new HashMap<String,List<AclRule>>();
	
	@PostConstruct
	private void loadAclRules(){
		ruleMap.clear();
		
		QueryParam qp=new QueryParam();
		qp.setTableName("SYS_ACL_RULE");
		qp.orderBy(new String[]{"entity_code", "priority"}).desc();
		List<Map<String,Object>> rules=dao.list(qp);
		
		if(rules!=null){
			for(Map<String,Object> value:rules){
				AclRule rule=new AclRule(value);
				String ec=rule.getEntityCode();
				List<AclRule> rs=ruleMap.get(ec);
				if(rs == null){
					rs=new ArrayList<AclRule>();
					ruleMap.put(ec, rs);
				}
				
				rs.add(rule);
			}
		}
	}
	
	public void validate(IUser user,String entityCode,Map<String,Object> value) throws AclException{
		
	}
	
	public String getAccessFilter(IUser user,String entityCode) throws AclException{
		if(StringUtils.isBlank(entityCode)){
			return null;
		}
		
		List<AclRule> rules=ruleMap.get(entityCode);
		Integer priority = null;
		if(rules!=null && rules.size()>0){
			List<String> ret = new ArrayList<String>();
			for(AclRule rule:rules){
				if(rule.enable(user)){
					String fs = rule.getFilterString(user);
					if (StringUtils.isBlank(fs)) {
						continue;
					}
					
					if (priority != null) {
						if (rule.getPriority() == priority.intValue()) {
							ret.add("(" + fs + ")");								
						}
					} else {
						priority = rule.getPriority();
						ret.add("(" + fs + ")");	
					}
					
				}
			}
			
			if (ret.size() > 0) {
				return "AND (" + StringUtils.join(ret.iterator(), " OR ") + ")";
			}
		}
		
		return null;
	}
	
	public void setDao(Dao dao){
		this.dao=dao;
	}
	
	public void reload(){
		this.loadAclRules();
	}
}

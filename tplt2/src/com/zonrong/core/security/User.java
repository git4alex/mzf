package com.zonrong.core.security;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

public class User implements IUser,Serializable{
	private static final long serialVersionUID = -2716769842682989621L;
	private static User systemUser;
	
	private Map<String,Object> data;
	
	public User(){
		
	}
	
	public User(Map<String,Object> data){
		this.data=data;
	}

	public int getId() {
		return MapUtils.getIntValue(data, "id");
	}

	public String getName() {
		return MapUtils.getString(data, "userName");
	}

	public int getOrgId() {
		return MapUtils.getIntValue(data, "orgId");
	}

	public Integer getOrgPid() {
		return MapUtils.getInteger(data, "orgPid");
	}

	public String getOrgCode() {
		return MapUtils.getString(data, "orgCode");
	}
	
	public String getOrgBizCode() {
		return MapUtils.getString(data, "orgBizCode");
	}	

	public String getOrgName() {
		return MapUtils.getString(data, "orgName");
	}

	public Map<String, Object> getDataMap() {
		return data;
	}
	
	public boolean hasRole(String... roleCodes){
		List<String> roles = (List<String>)MapUtils.getObject(data, "roles");
		if(roles!=null){
			for (int i = 0; i < roleCodes.length; i++) {
				if (roles.contains(StringUtils.trim(roleCodes[i]))) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasnotRole(String... roleCodes){
		List<String> roles = (List<String>)MapUtils.getObject(data, "roles");
		if(roles!=null){
			for (int i = 0; i < roleCodes.length; i++) {
				if (roles.contains(StringUtils.trim(roleCodes[i]))) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isSystem(){
		return false;
	}
	
	public User asSystem(){
		return new User(data){
			private static final long serialVersionUID = 6698583853673297837L;
			public boolean isSystem(){
				return true;
			}
		};
	}
	
	public static User getSystemUser(){
		if(systemUser==null){
			systemUser= new User(){
				private static final long serialVersionUID = 6698583853673297837L;
				public boolean isSystem(){
					return true;
				}
			};
		}
		return systemUser;
	}
}

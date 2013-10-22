package com.zonrong.core.security;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.AntUrlPathMatcher;
import org.springframework.security.web.util.UrlMatcher;

public class InvocationSecurityMetadataSourceService implements	FilterInvocationSecurityMetadataSource {

	private static final Logger logger = Logger.getLogger(InvocationSecurityMetadataSourceService.class);

	private DataSource dataSource;
	private String resourceQuery;
	private UrlMatcher urlMatcher = new AntUrlPathMatcher();
	private static Map<String, Collection<ConfigAttribute>> resourceMap = new LinkedHashMap<String, Collection<ConfigAttribute>>();

	public InvocationSecurityMetadataSourceService(DataSource dataSource,String resourceQuery) {
		this.dataSource = dataSource;
		this.resourceQuery = resourceQuery;
		loadResourceDefine();
	}

	private void loadResourceDefine() {
		if (logger.isDebugEnabled()) {
			logger.debug("load resource defination begin ......"); 
		}
		
		List<Resource> resources = new ResourceQuery(dataSource,resourceQuery).execute();

		for (Resource resource : resources) {
			String us=resource.getUrls();
			if(StringUtils.isBlank(us)){
				continue;
			}
			
			us=us.replaceAll("\n", "");
			String[] urls = us.split(";");
			String code=resource.getCode();
			
			if(StringUtils.isNotBlank(code) && urls != null && !ArrayUtils.isEmpty(urls)){
				for(String url:urls){
					if(StringUtils.isBlank(url)){
						continue;
					}
						
					if(url.indexOf("@")<0){
						url="ALL@"+url;
					}
					
					Collection<ConfigAttribute> configAttrs = resourceMap.get(url);
					if(configAttrs==null){
						configAttrs=new ArrayList<ConfigAttribute>();
						resourceMap.put(url, configAttrs);
					}
					
					configAttrs.add(new SecurityConfig(code));
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("load resource defination complated ......"); 
		}
	}

	public Collection<ConfigAttribute> getAttributes(Object object)	throws IllegalArgumentException {
		String reqUrl = ((FilterInvocation) object).getRequest().getRequestURI();
		String method=((FilterInvocation) object).getRequest().getMethod();
		
		Iterator<String> ite = resourceMap.keySet().iterator();
		Collection<ConfigAttribute> ret= new ArrayList<ConfigAttribute>();
		
		while (ite.hasNext()) {
			String resURL = ite.next();
			String resPath=resURL;
			String url=reqUrl;
			if(resPath.startsWith("ALL@")){
				resPath=resPath.substring(4);
			}else{
				url=method+"@"+url;
			}
			
			if (urlMatcher.pathMatchesUrl(resPath, url)) {
				Collection<ConfigAttribute> returnCollection = resourceMap.get(resURL);
				ret.addAll(returnCollection);
			}
		}
		
		return ret;
	}

	public boolean supports(Class<?> clazz) {
		return true;
	}

	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return null;
	}
	
	private class Resource {
		private String urls;
		private String code;
		public Resource(String urls, String code) {
			this.urls = urls;
			this.code = code;
		}

		public String getUrls() {
			return urls;
		}

		public String getCode() {
			return code;
		}
		
	}

	private class ResourceQuery extends MappingSqlQuery<Resource> {
		protected ResourceQuery(DataSource dataSource, String resourceQuery) {
			super(dataSource, resourceQuery);
			compile();
		}

		protected Resource mapRow(ResultSet rs, int rownum) throws SQLException {
			String urls = rs.getString("urls");
			String code = StringUtils.defaultString(rs.getString("code"));
			Resource resource = new Resource(urls,code);

			return resource;
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getResourceQuery() {
		return resourceQuery;
	}

	public void setResourceQuery(String resourceQuery) {
		this.resourceQuery = resourceQuery;
	}

}
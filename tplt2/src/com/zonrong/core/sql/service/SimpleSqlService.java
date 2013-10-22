package com.zonrong.core.sql.service;

import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.Page;
import com.zonrong.core.dao.dialect.Dialect;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.sql.provider.XmlSqlProvider;
import com.zonrong.core.sql.provider.templete.StringTemplateLoader;
import com.zonrong.entity.acl.AclException;
import com.zonrong.entity.acl.AclService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * date: 2011-7-27
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class SimpleSqlService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private Dao dao;

	@Resource
	private XmlSqlProvider provider;
	@Resource
	private AclService aclService;
	@Resource
	private Dialect dialect;

    public int update(String namespace, String sqlName, Map<String, Object> params, IUser user) throws BusinessException {
        String key = provider.getSqlKey(namespace, sqlName);
        try {
            String accessFilter = aclService.getAccessFilter(user, key);
            if (StringUtils.isNotBlank(accessFilter)) {
                params.put("accessFilter", accessFilter);
            }
        } catch (AclException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        String sql = getSql(key, params, user);
        if (logger.isDebugEnabled()) {
            logger.debug(sql);
        }

        return dao.getSimpleJdbcTemplate().update(sql);
    }

	public List<Map<String, Object>> list(String namespace, String sqlName, Map<String, Object> data, IUser user) throws BusinessException {
		String key = provider.getSqlKey(namespace, sqlName);
		try {
			String accessFilter = aclService.getAccessFilter(user, key);
			if (StringUtils.isNotBlank(accessFilter)) {
				data.put("accessFilter", accessFilter);
			}
		} catch (AclException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		String sql = getSql(key, data, user);
		if (logger.isDebugEnabled()) {
			logger.debug(sql);
		}
		return dao.getJdbcTemplate().queryForList(sql);
	}

	public Page page(String namespace, String sqlName, Map<String, Object> data, int start, int limit, IUser user) throws BusinessException {
		String key = provider.getSqlKey(namespace, sqlName);
		try {
			String accessFilter = aclService.getAccessFilter(user, key);
			if (StringUtils.isNotBlank(accessFilter)) {
				data.put("accessFilter", accessFilter);
			}
		} catch (AclException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		String sql = getSql(key, data, user);

		String countSql = dialect.getCountSqlString(sql);
		String limitSql = dialect.getLimitString(sql, start, limit);
		if (logger.isDebugEnabled()) {
			logger.debug(sql);
			logger.debug(countSql);
			logger.debug(limitSql);
		}

		int totalCount = dao.getSimpleJdbcTemplate().queryForInt(countSql);
		List items = dao.getSimpleJdbcTemplate().queryForList(limitSql);
		Page page = new Page(start,limit);
		page.setItems(items);
		page.setTotalCount(totalCount);

		return page;
	}

	private String getSql(String key, Map<String, Object> data, IUser user) throws BusinessException{
		logger.debug("sql key is " + key);
		String markupSql = provider.getSql(key);

		Configuration cfg = new Configuration();
        cfg.setNumberFormat("#");
		cfg.setTemplateLoader(new StringTemplateLoader(markupSql));
		cfg.setDefaultEncoding("UTF-8");
		StringWriter writer = new StringWriter();
        try {
			Template template = cfg.getTemplate("");
            template.setNumberFormat("#");
//            template.setOutputEncoding("UTF-8");
			template.process(data, writer);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		} catch (TemplateException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw new BusinessException(e.getMessage());
		}

        String ret = writer.toString();
//        logger.debug("before utf-8"+ret);
//        try {
//            ret = new String(ret.getBytes("iso-8859-1"),"UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } finally {
//            return ret;
//        }
        return ret;
    }
}



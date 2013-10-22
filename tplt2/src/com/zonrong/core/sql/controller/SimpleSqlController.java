package com.zonrong.core.sql.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.zonrong.core.dao.Page;
import com.zonrong.core.security.IUser;
import com.zonrong.core.sql.provider.XmlSqlProvider;
import com.zonrong.core.sql.service.SimpleSqlService;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.metadata.MetadataConst;

/**
 * date: 2011-7-27
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/sql")
public class SimpleSqlController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private SimpleSqlService simpleSqlService;
	
	@RequestMapping(value = "/{namespace}/{sqlName}", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> list(@PathVariable String namespace, @PathVariable String sqlName, @RequestParam Map data, HttpServletRequest request) {
		try {		
			IUser user = SessionUtils.getUser(request);			
			if (MapUtils.getInteger(data, "start") != null) {
				Page page = new Page(data);
				if (page != null) {
					return simpleSqlService.page(namespace, sqlName, data, page.getOffSet(), page.getPageSize(), user);
				}	
			}
			
			List<Map<String, Object>> list = simpleSqlService.list(namespace, sqlName, data, SessionUtils.getUser(request));
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/listSqlTitle", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> listSqlTitle(@RequestParam Map data, HttpServletRequest request) {
		List<Map<String, String>> list = XmlSqlProvider.listSqlTitle();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(MetadataConst.ITEMS_ROOT, list);
		return map;
	}	
	
	@RequestMapping(value = "/page/{pageName}", method = RequestMethod.GET)
	public ModelAndView list(@PathVariable String pageName, HttpServletRequest request) {
		Map map = new HashMap();
		
		List wordList = new ArrayList();
		wordList.add("hello");
		wordList.add("world");
		map.put("wordList", wordList);
		return new ModelAndView("xl", map);
	}
}



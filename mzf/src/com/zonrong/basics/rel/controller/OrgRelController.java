package com.zonrong.basics.rel.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.rel.service.OrgRelService;
import com.zonrong.common.utils.MzfEnum.OrgRelType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.Page;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.util.TpltUtils;

/**
 * date: 2010-11-24
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value="/code/orgRel")
public class OrgRelController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private OrgRelService orgRelService;
	
	@RequestMapping(value = "/{type}/{orgId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateRel(@PathVariable final OrgRelType type, @PathVariable final int orgId, @RequestBody final Integer[] orgIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				orgRelService.updateRel(orgId, orgIds, type, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/pageCusOrderSource", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> list(@RequestParam Map parameter, HttpServletRequest request) {
		try {			
			OrderBy orderBy = TpltUtils.refactorOrderByParams(parameter);
			
			List<Map<String,Object>> list = TpltUtils.refactorQueryParams(parameter);
			if (MapUtils.getInteger(parameter, "start") != null) {
				Page page = new Page(parameter);
				if (page != null) {
					return orgRelService.pageCusOrderSource(list, page.getOffSet(), page.getPageSize(), orderBy, SessionUtils.getUser(request));
				}	
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, null);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
}



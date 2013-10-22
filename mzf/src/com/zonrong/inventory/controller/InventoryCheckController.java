package com.zonrong.inventory.controller;

import java.math.BigDecimal;
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

import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.core.dao.Page;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.sql.service.SimpleSqlService;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.inventory.service.InventoryCheckService;
import com.zonrong.metadata.MetadataConst;

/**
 * date: 2010-12-27
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value="/code/invenctoryCheck")
public class InventoryCheckController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private InventoryCheckService inventoryCheckService;
	@Resource
	private SimpleSqlService simpleSqlService;
	
	@RequestMapping(value = "/createOrFindOpenedCheck", method = RequestMethod.POST)
	@ResponseBody
	public Map createOrFindOpenedCheck(@RequestParam final int orgId, @RequestParam final StorageType storageType, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int id = inventoryCheckService.createOrFindOpenedCheck(orgId, storageType, this.getUser());
				this.put("id", id);
				this.put("checkSql", storageType.getInventoryCheckSql());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/saveTargetNum", method = RequestMethod.POST)
	@ResponseBody
	public Map saveTargetNum(@RequestParam final int checkId, @RequestParam final String targetNum, @RequestParam final String actualQuantity, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				int id = inventoryCheckService.saveTargetNum(checkId, targetNum, new BigDecimal(actualQuantity), this.getUser());
				this.put("id", id);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/createInventoryCheck/{checkId}", method = RequestMethod.POST)
	@ResponseBody
	public Map createInventoryCheck(@PathVariable final int checkId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				inventoryCheckService.createInventoryCheck(checkId, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> listInventoryCheck(@RequestParam Map data, HttpServletRequest request) {
		try {		
			String namespace = MapUtils.getString(data, "namespace");
			String sqlName = MapUtils.getString(data, "sqlName");
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
	
//	@RequestMapping(value = "/forProduct", method = RequestMethod.POST)
//	@ResponseBody
//	public Map createForProduct(@RequestBody final List<Map<String, Object>> checkList, HttpServletRequest request) {
//		OperateTemplete templete = new HttpTemplete(request) {
//			protected void doSomething() throws BusinessException {	
//				int id = inventoryCheckService.createForProduct(checkList, this.getUser());
//				this.put("id", id);
//			}			
//		};
//		return templete.operate();			
//	}
//	
//	@RequestMapping(value = "/forRawmaterial", method = RequestMethod.POST)
//	@ResponseBody
//	public Map createForRawmaterial(@RequestBody final List<Map<String, Object>> checkList, HttpServletRequest request) {
//		OperateTemplete templete = new HttpTemplete(request) {
//			protected void doSomething() throws BusinessException {	
//				int id = inventoryCheckService.createForRawmaterial(checkList, this.getUser());
//				this.put("id", id);
//			}			
//		};
//		return templete.operate();			
//	}
//	
//	@RequestMapping(value = "/forMaterial", method = RequestMethod.POST)
//	@ResponseBody
//	public Map createForMaterial(@RequestBody final List<Map<String, Object>> checkList, HttpServletRequest request) {
//		OperateTemplete templete = new HttpTemplete(request) {
//			protected void doSomething() throws BusinessException {	
//				int id = inventoryCheckService.createForMaterial(checkList, this.getUser());
//				this.put("id", id);
//			}			
//		};
//		return templete.operate();			
//	}
//	
//	@RequestMapping(value = "/forSell", method = RequestMethod.POST)
//	@ResponseBody
//	public Map createForSell(@RequestBody final Map<String, Object> check, HttpServletRequest request) {
//		OperateTemplete templete = new HttpTemplete(request) {
//			protected void doSomething() throws BusinessException {	
//				int id = inventoryCheckService.createForSell(check, this.getUser());
//				this.put("id", id);
//			}			
//		};
//		return templete.operate();			
//	}
//	
//	@RequestMapping(value = "/forEarnest", method = RequestMethod.POST)
//	@ResponseBody
//	public Map createForEarnest(@RequestBody final Map<String, Object> check, HttpServletRequest request) {
//		OperateTemplete templete = new HttpTemplete(request) {
//			protected void doSomething() throws BusinessException {	
//				int id = inventoryCheckService.createForEarnest(check, this.getUser());
//				this.put("id", id);
//			}			
//		};
//		return templete.operate();
//	}
	
	@RequestMapping(value = "/{orgId}/{storageType}", method = RequestMethod.POST)
	@ResponseBody
	public Map createForEarnest(@PathVariable final int orgId, @PathVariable final String storageType, @RequestBody final List<Map<String, Object>> detailList, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				int id = inventoryCheckService.create(orgId, StorageType.valueOf(storageType), detailList, this.getUser());
				this.put("id", id);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/getPrintData/{checkId}", method = RequestMethod.GET)
	@ResponseBody
	public Map getPrintData(@PathVariable final int checkId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				Map<String, Object> check = inventoryCheckService.getPrintData(checkId, this.getUser());
				this.put("check", check);
			}			
		};
		return templete.operate();			
	}
}



package com.zonrong.report.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.dao.Page;
import com.zonrong.core.security.IUser;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.report.service.ReportService;

/**
 * date: 2011-9-25
 *
 * version: 1.0
 * commonts: ......
 */
@RequestMapping("/report")
@Controller
public class ReportController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private ReportService reportService;
	
	@RequestMapping(value = "/reportEmployeeRank", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> reportEmployeeRank(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			IUser user = SessionUtils.getUser(request);		
			List<Map<String, Object>> list = reportService.reportEmployeeRank(data, user);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/reportSplitSettlement", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> reportSplitSettlement(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			IUser user = SessionUtils.getUser(request);		
			data.put("reportType", "split");
			List<Map<String, Object>> list = reportService.reportSettlement(data, user);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/reportRenovateSettlement", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> reportRenovateSettlement(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			IUser user = SessionUtils.getUser(request);	
			data.put("reportType", "renovate");
			List<Map<String, Object>> list = reportService.reportSettlement(data, user);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/reportSecondProductSettlement", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> reportSecondProductSettlement(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			IUser user = SessionUtils.getUser(request);	
			List<Map<String, Object>> list = reportService.secondProReportSettlement(data, user);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/materialStore", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> materialStoreReport(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			IUser user = SessionUtils.getUser(request);	
			List<Map<String, Object>> list = reportService.materialStoreReport(data, user);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/sale/saleReportForDiamondSize", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> saleReportForDiamondSize(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			List<Map<String, Object>> list = reportService.saleReportForDiamondSize(data, SessionUtils.getUser(request));
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/sale/saleReportForPrice", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> saleReportForPrice(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			List<Map<String, Object>> list = reportService.saleReportForPrice(data, SessionUtils.getUser(request));
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(value = "/inventory/productHistoryInventory", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> productHistoryInventory(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {			
			if (MapUtils.getInteger(data, "start") != null) {
				Page page = new Page(data);
				if (page != null) {
					return reportService.productHistoryInventoryPage(data, page.getOffSet(), page.getPageSize(), SessionUtils.getUser(request));
				}	
			}
			
			List<Map<String, Object>> list =  reportService.productHistoryInventoryList(data, SessionUtils.getUser(request));
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			map.put(MetadataConst.ITEMS_ROOT, new ArrayList());
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return map;
	}
	
	@RequestMapping(value = "/inventory/stockMovementReportForProduct", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> stockMovementReportForProduct(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			return reportService.stockMovementReportForProduct(data, SessionUtils.getUser(request));				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	@RequestMapping(value = "/sale/reportForDiamondSizeOnCol", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> reportForDiamondSizeOnCol(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			List<Map<String, Object>> list = reportService.reportForDiamondSizeOnCol(data, SessionUtils.getUser(request));
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, list);
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	@RequestMapping(value = "/sale/diamondMovementReport", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> diamondMovementReport(@RequestParam Map<String, Object> data, HttpServletRequest request) {
		try {		
			return reportService.diamondMovementReport(data, SessionUtils.getUser(request));				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
}



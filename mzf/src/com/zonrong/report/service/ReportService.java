package com.zonrong.report.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEnum.ProductType;
import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.Page;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.sql.service.SimpleSqlService;
import com.zonrong.metadata.MetadataConst;

/**
 * date: 2011-9-24
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ReportService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private SimpleSqlService simpleSqlService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private Dao dao;

	public List<Map<String, Object>> reportEmployeeRank(Map<String, Object> data, IUser user) throws BusinessException {
		List<Map<String, Object>> list = simpleSqlService.list("sale", "employeeRank", data, user);

		String quantityString = "Quantity";
		String salesString = "Sales";
		String otherString = "other";
		//处理数据（行转列）
		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		for (Map<String, Object> m : list) {
			String orgName = MapUtils.getString(m, "orgName");
			String employeeName = MapUtils.getString(m, "employeeName");
			String key = orgName + employeeName;

			Map<String, Object> mm = map.get(key);
			if (MapUtils.isEmpty(mm)) {
				mm = new HashMap<String, Object>(m);
			}
			ProductType ptype = ProductType.valueOf(MapUtils.getString(m, "ptype"));
			BigDecimal quantity = new BigDecimal(MapUtils.getInteger(m, "quantity", 0));
			BigDecimal sales = new BigDecimal(MapUtils.getFloat(m, "sales", new Float(0)));

			String quantityKey = ptype + quantityString;
			String salesKey = ptype + salesString;
			if (ptype != ProductType.diamond &&
					ptype != ProductType.kGold &&
					ptype != ProductType.pt) {
				quantityKey = otherString + quantityString;
				salesKey = otherString + salesString;
			}
			BigDecimal q = new BigDecimal(MapUtils.getInteger(mm, quantityKey, 0));
			BigDecimal s = new BigDecimal(MapUtils.getFloat(mm, salesKey, new Float(0)));

			mm.put(quantityKey, quantity.add(q).intValue());
			mm.put(salesKey, sales.add(s).floatValue());

			map.put(key, mm);
		}
		list = new ArrayList<Map<String,Object>>(map.values());

		//处理数据（获取总销售额）
		for (Map<String, Object> m : list) {
			BigDecimal totalSales = new BigDecimal(0);
			totalSales = totalSales.add(new BigDecimal(MapUtils.getFloat(m, ProductType.diamond + salesString, new Float(0))));
			totalSales = totalSales.add(new BigDecimal(MapUtils.getFloat(m, ProductType.pt + salesString, new Float(0))));
			totalSales = totalSales.add(new BigDecimal(MapUtils.getFloat(m, ProductType.kGold + salesString, new Float(0))));
			totalSales = totalSales.add(new BigDecimal(MapUtils.getFloat(m, otherString + salesString, new Float(0))));
			m.put("totalSales", totalSales);

			m.remove("ptype");
			m.remove("ptypeText");
			m.remove("sales");
			m.remove("quantity");
		}

		//排序
		Comparator<Map<String, Object>> c = new Comparator<Map<String,Object>>() {
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Float total1 = MapUtils.getFloat(o1, "totalSales");
				Float total2 = MapUtils.getFloat(o2, "totalSales");
				if (total1 > total2) {
					return -1;
				} else if (total1 < total2) {
					return 1;
				} else {
					return 0;
				}
			}
		};

		Collections.sort(list, c);
		return list;
	}



	public List<Map<String, Object>> reportSettlement(Map<String, Object> data, IUser user) throws BusinessException {
		boolean isStore = mzfOrgService.isStore(user.getOrgId());
		if (isStore) {
			String accessFilter = "and o.code like '" + user.getOrgCode() + "%'";
			data.put("accessFilter", accessFilter);
		}

		List<Map<String, Object>> list = simpleSqlService.list("settlement", "report", data, user);
		if (isStore) {
			for (Map<String, Object> map : list) {
				map.put("costPrice", null);
			}
		}

		return list;
	}

	public List<Map<String, Object>> secondProReportSettlement(Map<String, Object> data, IUser user) throws BusinessException {
		boolean isStore = mzfOrgService.isStore(user.getOrgId());
		if (isStore) {
			String accessFilter = "and o.code like '" + user.getOrgCode() + "%'";
			data.put("accessFilter", accessFilter);
		}

		List<Map<String, Object>> list = simpleSqlService.list("settlement", "secondProductReport", data, user);
		if (isStore) {
			for (Map<String, Object> map : list) {
				map.put("costPrice", null);
				map.put("source", null);
			}
		}

		return list;
	}

	public List<Map<String, Object>> materialStoreReport(Map<String, Object> data, IUser user) throws BusinessException {
		boolean isStore = mzfOrgService.isStore(user.getOrgId());
		if (isStore) {
			String accessFilter = "and o.code like '" + user.getOrgCode() + "%'";
			data.put("accessFilter", accessFilter);
		}

		List<Map<String, Object>> list = simpleSqlService.list("inventory", "material", data, user);
		if (isStore) {
			for (Map<String, Object> map : list) {
				map.put("cost", null);
			}
		} else {
//			for (Map<String, Object> map : list) {
//				map.put("retailPrice", null);
//			}
		}

		return list;
	}

	public List<Map<String, Object>> saleReportForDiamondSize(Map<String, Object> data, IUser user) throws BusinessException {
		String[][] rows = new String[][]{
				{"< 0.2 ct", "0.2", null, null, null},
				{"0.2-0.22999 ct", null, "0.2", "0.22999", null},
				{"0.23-0.39999 ct", null, "0.23", "0.39999", null},
				{"0.4-0.99999 ct", null, "0.4", "0.99999", null},
				{"> 1 ct", null, null, null, "1"},
				};

		return saleReport(rows, "ForDiamondSize", data, user);
	}
	//商品历史存货报表
	public Page productHistoryInventoryPage(Map<String, Object> data,int start, int limit, IUser user) throws BusinessException{
			boolean isStore = mzfOrgService.isStore(user.getOrgId());
			if (isStore) {
				String accessFilter = "and o.code like '" + user.getOrgCode() + "%'";
				data.put("accessFilter", accessFilter);
			}

			return simpleSqlService.page("inventory", "productHistoryInventory", data,start, limit, user);


	}
	public List<Map<String,Object>> productHistoryInventoryList(Map<String, Object> data, IUser user) throws BusinessException{
		boolean isStore = mzfOrgService.isStore(user.getOrgId());
		if (isStore) {
			String accessFilter = "and o.code like '" + user.getOrgCode() + "%'";
			data.put("accessFilter", accessFilter);
		}

		return simpleSqlService.list("inventory", "productHistoryInventory", data, user);
	}
	public List<Map<String, Object>> saleReportForPrice(Map<String, Object> data, IUser user) throws BusinessException {
		String[][] rows = new String[][]{
				{"≤1999元", null, null, "1999", null},
				{"2000元-3999元", null, "2000", "3999", null},
				{"4000元-5999元", null, "4000", "5999", null},
				{"6000元-7999元", null, "6000", "7999", null},
				{"8000元-9999元", null, "8000", "9999", null},
				{"10000元-14999元", null, "10000", "14999", null},
				{"15000元-19999元", null, "15000", "19999", null},
				{"20000元-29999元", null, "20000", "29999", null},
				{"30000元-39999元", null, "30000", "39999", null},
				{"40000元-49999元", null, "40000", "49999", null},
				{"≥50000元", null, "50000", null, null},
				};

		return saleReport(rows, "ForPrice", data, user);
	}

	public List<Map<String, Object>> saleReport(String[][] rows, String sufix, Map<String, Object> data, IUser user) throws BusinessException {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		BigDecimal totalPrice = new BigDecimal(0);
		BigDecimal totalProductCount = new BigDecimal(0);
		BigDecimal totalUnitPrice = new BigDecimal(0);

		String accessFilter = "and o1.code like '" + user.getOrgCode() + "%'";
		data.put("accessFilter", accessFilter);
		for (String[] row : rows) {
			Map<String, Object> _data = new HashMap<String, Object>(data);
			_data.put("rowName", row[0]);
			_data.put("ltPoint" + sufix, row[1]);
			_data.put("gtEqPoint" + sufix, row[2]);
			_data.put("ltEqPoint" + sufix, row[3]);
			_data.put("gtPoint" + sufix, row[4]);
			List<Map<String, Object>> _list = simpleSqlService.list("sale", "report" + sufix, _data, user);
			if (CollectionUtils.isNotEmpty(_list)) {
				Map<String, Object> map = _list.get(0);
				if(MapUtils.getFloat(map, "price") == null){
					return new ArrayList<Map<String,Object>>();
				}
				list.add(map);

				BigDecimal price = new BigDecimal(MapUtils.getString(map, "price", Integer.toString(0)));
				totalPrice = totalPrice.add(price);

				BigDecimal productCount = new BigDecimal(MapUtils.getString(map, "productCount", Integer.toString(0)));
				totalProductCount = totalProductCount.add(productCount);

				BigDecimal unitPrice = new BigDecimal(MapUtils.getString(map, "unitPrice", Integer.toString(0)));
				totalUnitPrice = totalUnitPrice.add(unitPrice);
			}
		}

		for (Map<String, Object> map : list) {
			BigDecimal price = new BigDecimal(MapUtils.getString(map, "price", Integer.toString(0)));
			BigDecimal scaleForPrice = price.divide(totalPrice, 4, BigDecimal.ROUND_HALF_EVEN);
			map.put("scaleForPrice", scaleForPrice);
		}

		Map<String, Object> totalMap = new HashMap<String, Object>();
		String totalRowName = "合计";
		String area = MapUtils.getString(data, "area","");
		String orgName = MapUtils.getString(data, "orgName","");
		if(!area.equals("") || !orgName.equals("")){
			totalRowName = "["+area+" "+orgName+"]合计";
		}
		totalMap.put("rowName", totalRowName);
		totalMap.put("price", totalPrice);
		totalMap.put("scaleForPrice", 1);
		totalMap.put("productCount", totalProductCount);
		totalMap.put("unitPrice", totalUnitPrice);
		list.add(totalMap);

		return list;
	}
	public List<Map<String, Object>> reportForDiamondSizeOnCol(Map<String, Object> data, IUser user) throws BusinessException {
//		boolean isStore = mzfOrgService.isStore(user.getOrgId());
//		if (isStore) {
//		}
		String accessFilter = "and o1.code like '" + user.getOrgCode() + "%'";
		data.put("accessFilter", accessFilter);

		List<Map<String, Object>> list = simpleSqlService.list("sale", "reportForDiamondSizeOnCol", data, user);
//		BigDecimal totalCt2 = new BigDecimal(0);
//		BigDecimal totalCt299 = new BigDecimal(0);
//		BigDecimal totalCt399 = new BigDecimal(0);
//		BigDecimal totalCt999 = new BigDecimal(0);
//		BigDecimal totalCt1 = new BigDecimal(0);
//
//		for (Map<String, Object> map : list) {
//			BigDecimal ct2 = new BigDecimal(MapUtils.getString(map, "ct2", Integer.toString(0)));
//			totalCt2 = totalCt2.add(ct2);
//
//			BigDecimal ct299 = new BigDecimal(MapUtils.getString(map, "ct299", Integer.toString(0)));
//			totalCt299 = totalCt299.add(ct299);
//
//			BigDecimal ct399 = new BigDecimal(MapUtils.getString(map, "ct399", Integer.toString(0)));
//			totalCt399 = totalCt399.add(ct399);
//
//			BigDecimal ct999 = new BigDecimal(MapUtils.getString(map, "ct999", Integer.toString(0)));
//			totalCt999 = totalCt999.add(ct999);
//
//			BigDecimal ct1 = new BigDecimal(MapUtils.getString(map, "ct1", Integer.toString(0)));
//			totalCt1 = totalCt1.add(ct1);
//		}

		for (Map<String, Object> map : list) {
			BigDecimal totalPrice = new BigDecimal(MapUtils.getString(map, "price", Integer.toString(0)));
			BigDecimal ct2 = new BigDecimal(MapUtils.getString(map, "ct2", Integer.toString(0)));
			BigDecimal ct2Rate = new BigDecimal(0);
			if(ct2.doubleValue() > 0){
				ct2Rate = ct2.divide(totalPrice, 4, BigDecimal.ROUND_HALF_EVEN);
			}
			map.put("ct2Rate", ct2Rate);

			BigDecimal ct299 = new BigDecimal(MapUtils.getString(map, "ct299", Integer.toString(0)));
			BigDecimal ct299Rate = new BigDecimal(0);
			if(ct299.doubleValue() > 0){
				ct299Rate = ct299.divide(totalPrice, 4, BigDecimal.ROUND_HALF_EVEN);
			}
			map.put("ct299Rate", ct299Rate);


			BigDecimal ct399 = new BigDecimal(MapUtils.getString(map, "ct399", Integer.toString(0)));
			BigDecimal ct399Rate = new BigDecimal(0);
			if(ct399.doubleValue() > 0){
				ct399Rate = ct399.divide(totalPrice, 4, BigDecimal.ROUND_HALF_EVEN);
			}
			map.put("ct399Rate", ct399Rate);

			BigDecimal ct999 = new BigDecimal(MapUtils.getString(map, "ct999", Integer.toString(0)));
			BigDecimal ct999Rate = new BigDecimal(0);
			if(ct999.doubleValue() > 0){
				ct999Rate = ct999.divide(totalPrice, 4, BigDecimal.ROUND_HALF_EVEN);
			}
			map.put("ct999Rate", ct999Rate);

			BigDecimal ct1 = new BigDecimal(MapUtils.getString(map, "ct1", Integer.toString(0)));
			BigDecimal ct1Rate = new BigDecimal(0);
			if(ct1.doubleValue() > 0){
				ct1Rate = ct1.divide(totalPrice, 4, BigDecimal.ROUND_HALF_EVEN);
			}
			map.put("ct1Rate", ct1Rate);
		}

		return list;

	}
	public Map<String, Object> stockMovementReportForProduct(Map<String, Object> data, IUser user) throws BusinessException {
		Integer year = MapUtils.getInteger(data, "year");
		Integer month = MapUtils.getInteger(data, "month");
		if (year == null || month == null) {
			throw new BusinessException("请提交年份和月份");
		}

		Calendar c1 = Calendar.getInstance();
		if (year >= c1.get(Calendar.YEAR) && month > c1.get(Calendar.MONTH) + 1) {
			return new HashMap<String,Object>();
		}

		Map<String, Object> titleaAndData = new HashMap<String, Object>();

		c1.set(Calendar.YEAR, year);
		c1.set(Calendar.MONTH, month - 2);
		String stYear = Integer.toString(c1.get(Calendar.YEAR));
		String stMonth = Integer.toString(c1.get(Calendar.MONTH) + 1 + 100).substring(1);
		String stTable = "HIST_PRODUCT_INVENTORY_BANK_" + stYear + stMonth;
		if (hasTable(stTable)) {
			data.put("stTable", stTable);
			String stDate = StringUtils.join(new String[]{stYear, stMonth, Integer.toString(c1.getActualMaximum(Calendar.DAY_OF_MONTH))}, "-");
			data.put("stDate", stDate);
			titleaAndData.put("stDate", stDate);
		}


		Calendar c2 = Calendar.getInstance();
		c2.set(Calendar.YEAR, year);
		c2.set(Calendar.MONTH, month - 1);
		String edYear = Integer.toString(year);
		String edMonth = Integer.toString(month + 100).substring(1);
		Calendar currCalendar = Calendar.getInstance();
		//如果是当前月
		String edDate = null;
		if (currCalendar.get(Calendar.YEAR) == year && currCalendar.get(Calendar.MONTH) + 1 == month) {
			edDate = StringUtils.join(new String[]{edYear, edMonth, Integer.toString(c2.get(Calendar.DAY_OF_MONTH))}, "-");
//			data.put("isCurrMonth", true);

//			titleaAndData.put("currMonthDate", StringUtils.join(new String[]{edYear, edMonth, Integer.toString(c2.getInventory(Calendar.DAY_OF_MONTH) - 1)}, "-"));
		} else {
			edDate = StringUtils.join(new String[]{edYear, edMonth, Integer.toString(c2.getActualMaximum(Calendar.DAY_OF_MONTH))}, "-");
//			titleaAndData.put("currMonthDate", edDate);
		}
		data.put("edDate", edDate);
		titleaAndData.put("currMonthDate1", StringUtils.join(new String[]{edYear, edMonth, "01"}, "-"));
		titleaAndData.put("currMonthDate2", edDate);
		titleaAndData.put("currMonthDate", edDate);

		String edTable = "HIST_PRODUCT_INVENTORY_BANK_" + edYear + edMonth;
		if (hasTable(edTable)) {
			data.put("edTable", edTable);
		}
		data.put("orgCode", user.getOrgCode());
		List<Map<String, Object>> list =  simpleSqlService.list("inventory", "stockMovementReportForProduct", data, user);

		titleaAndData.put(MetadataConst.ITEMS_ROOT, list);
		return titleaAndData;
	}

	public Map<String, Object> diamondMovementReport(Map<String, Object> data, IUser user) throws BusinessException {
		Integer year = MapUtils.getInteger(data, "year");
		Integer month = MapUtils.getInteger(data, "month");
		if (year == null || month == null) {
			throw new BusinessException("请提交年份和月份");
		}

		Calendar c1 = Calendar.getInstance();
		if (year >= c1.get(Calendar.YEAR) && month > c1.get(Calendar.MONTH) + 1) {
			return new HashMap<String,Object>();
		}

		Map<String, Object> titleaAndData = new HashMap<String, Object>();

		c1.set(Calendar.YEAR, year);
		c1.set(Calendar.MONTH, month - 2);
		String stYear = Integer.toString(c1.get(Calendar.YEAR));
		String stMonth = Integer.toString(c1.get(Calendar.MONTH) + 1 + 100).substring(1);
		String stTable = "HIST_DIAMOND_INVENTORY_BANK_" + stYear + stMonth;
		if (hasTable(stTable)) {
			data.put("stTable", stTable);
			String stDate = StringUtils.join(new String[]{stYear, stMonth, Integer.toString(c1.getActualMaximum(Calendar.DAY_OF_MONTH))}, "-");
			data.put("stDate", stDate);
			titleaAndData.put("stDate", stDate);
		}


		Calendar c2 = Calendar.getInstance();
		c2.set(Calendar.YEAR, year);
		c2.set(Calendar.MONTH, month - 1);
		String edYear = Integer.toString(year);
		String edMonth = Integer.toString(month + 100).substring(1);
		Calendar currCalendar = Calendar.getInstance();
		//如果是当前月
		String edDate = null;
		if (currCalendar.get(Calendar.YEAR) == year && currCalendar.get(Calendar.MONTH) + 1 == month) {
			edDate = StringUtils.join(new String[]{edYear, edMonth, Integer.toString(c2.get(Calendar.DAY_OF_MONTH)-1)}, "-");

		} else {
			edDate = StringUtils.join(new String[]{edYear, edMonth, Integer.toString(c2.getActualMaximum(Calendar.DAY_OF_MONTH))}, "-");
		}
		data.put("edDate", edDate);
		titleaAndData.put("currMonthDate1", StringUtils.join(new String[]{edYear, edMonth, "01"}, "-"));
		titleaAndData.put("currMonthDate2", edDate);
		titleaAndData.put("currMonthDate", edDate);

		String edTable = "HIST_DIAMOND_INVENTORY_BANK_" + edYear + edMonth;
		if (hasTable(edTable)) {
			data.put("edTable", edTable);
		}
		data.put("orgCode", user.getOrgCode());
		List<Map<String, Object>> list =  simpleSqlService.list("inventory", "diamondMovementReport", data, user);

		titleaAndData.put(MetadataConst.ITEMS_ROOT, list);
		return titleaAndData;
	}

	private boolean hasTable(String table) throws BusinessException {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("table", table);
			int i = dao.getSimpleJdbcTemplate().queryForInt("select isnull(object_id(:table, 'U'), -1)", map);
			if (i != -1) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
		}
		return false;
	}
}



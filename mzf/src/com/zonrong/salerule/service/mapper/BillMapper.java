package com.zonrong.salerule.service.mapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

/**
 * date: 2011-10-20
 *
 * version: 1.0
 * commonts: ......
 */
public class BillMapper<K,V> extends Mapper<String, String> {	
	public Map<String, Object> bill = new HashMap<String, Object>();
	
	public Map<String, Object> getBill() {
		return bill;
	}
	
	public BillMapper(Map<String, Object> bill) {
		super();
		this.bill = bill;
		
		this.put("整单消费总额", "totalAmount");
		this.put("整单购买件数", "productCount");
	}
	
	public void load(Map<String, Object> box) {
		box.put("totalAmount", MapUtils.getDouble(bill, "totalAmount"));
		box.put("productCount", MapUtils.getInteger(bill, "productCount"));
	}
}



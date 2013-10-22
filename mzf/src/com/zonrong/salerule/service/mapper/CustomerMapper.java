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
public class CustomerMapper<K,V> extends Mapper<String, String> {	
	public Map<String, Object> customer = new HashMap<String, Object>();
	
	public Map<String, Object> getCustomer() {
		return customer;
	}
	
	public CustomerMapper(Map<String, Object> customer) {
		super();
		this.customer = customer;
		
		this.put("客户类型", "ptype");
		this.put("客户级别", "pkind");
		this.put("一口价", "retailBasePrice");
		this.put("折扣", "retailBasePrice");
		this.put("实际售价", "retailBasePrice");
	}
	
	public void load(Map<String, Object> box) {
		box.put("ptype", MapUtils.getString(customer, "ptype"));
		box.put("pkind", MapUtils.getString(customer, "pkind"));
		box.put("retailBasePrice", MapUtils.getFloat(customer, "retailBasePrice"));
		box.put("totalDiscount", MapUtils.getFloat(customer, "totalDiscount", new Float(0)));
		box.put("finalPrice", MapUtils.getFloat(customer, "retailBasePrice") - MapUtils.getFloat(customer, "totalDiscount", new Float(0)));
	}
}



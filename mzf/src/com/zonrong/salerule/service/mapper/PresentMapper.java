package com.zonrong.salerule.service.mapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

public class PresentMapper<K,V> extends Mapper<String, String> {

	private Map<String, Object> present = new HashMap<String, Object>();
	
	public PresentMapper(Map<String, Object> present){
		super();
		this.present = present;
	}
	
	@Override
	public void load(Map box) {
		box.put("checkedTotalPrice",  MapUtils.getFloat(present, "checkedTotalPrice"));
		box.put("checkedTotalCount", MapUtils.getFloat(present, "checkedTotalCount"));
		box.put("totalPriceCon", MapUtils.getFloat(present, "totalPriceCon"));
		box.put("totalCountCon", MapUtils.getFloat(present, "totalCountCon"));
		box.put("countOpt", MapUtils.getString(present, "countOpt"));
		box.put("priceOpt", MapUtils.getString(present, "priceOpt"));
		box.put("rateCon", MapUtils.getString(present, "rateCon"));
		box.put("rate", MapUtils.getString(present, "rate"));
		box.put("rateOpt", MapUtils.getString(present, "rateOpt"));

	}

}

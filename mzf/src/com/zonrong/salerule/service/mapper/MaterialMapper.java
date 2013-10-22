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
public class MaterialMapper<K,V> extends Mapper<String, String> {	
	public Map<String, Object> material = new HashMap<String, Object>();
	
	public Map<String, Object> getMaterial() {
		return material;
	}
	
	public MaterialMapper(Map<String, Object> material) {
		super();
		this.material = material;
		
		this.put("物料条码", "num");
		this.put("一口价", "retailPrice");
	}
	
	public void load(Map<String, Object> box) {
		box.put("num", MapUtils.getString(material, "num"));
		box.put("retailPrice", MapUtils.getFloat(material, "retailPrice")==null?0:MapUtils.getFloat(material, "retailPrice"));
	}
}



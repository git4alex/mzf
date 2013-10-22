package com.zonrong.salerule.service.mapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

/**
 * date: 2011-10-20
 *
 * version: 1.0
 * commonts: ......
 */
public class ProductMapper<K,V> extends Mapper<String, String> {	
	public Map<String, Object> product = new HashMap<String, Object>();
	
	public Map<String, Object> getProduct() {
		return product;
	}
	
	public ProductMapper(Map<String, Object> product) {
		super();
		this.product = product;
		
		this.put("商品类型", "ptype");
		this.put("商品种类", "pkind");
		this.put("一口价", "retailBasePrice");
		this.put("折扣", "retailBasePrice");
		this.put("实际售价", "retailBasePrice");
		
		this.put("过程价", "tempPrice");
	}
	
	public void load(Map<String, Object> box) {
		box.put("ptype", MapUtils.getString(product, "ptype"));
		box.put("pkind", MapUtils.getString(product, "pkind"));
		box.put("retailBasePrice", MapUtils.getFloat(product, "retailBasePrice")==null?0:MapUtils.getFloat(product, "retailBasePrice"));
		box.put("totalDiscount", MapUtils.getFloat(product, "totalDiscount", new Float(0)));
		box.put("finalPrice", MapUtils.getFloat(product, "fixedPrice") - MapUtils.getFloat(product, "totalDiscount", new Float(0)));
		
		String tempPrice = MapUtils.getString(product, "fixedPrice");
		box.put("tempPrice", StringUtils.isNotBlank(tempPrice)? new BigDecimal(tempPrice):null);
	}
}



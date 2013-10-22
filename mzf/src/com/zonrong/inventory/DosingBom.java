package com.zonrong.inventory;

import java.math.BigDecimal;

/**
 * date: 2011-3-8
 *
 * version: 1.0
 * commonts: ......
 */
public class DosingBom{
	private Integer rawmaterialId;
	private Integer inventoryId;
	private BigDecimal quantity;
	private BigDecimal weight;

	public Integer getInventoryId() {
		return inventoryId;
	}
	public void setInventoryId(Integer inventoryId) {
		this.inventoryId = inventoryId;
	}
	public BigDecimal getQuantity() {
		return quantity;
	}
	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}
	public Integer getRawmaterialId() {
		return rawmaterialId;
	}
	public void setRawmaterialId(Integer rawmaterialId) {
		this.rawmaterialId = rawmaterialId;
	}
	public BigDecimal getWeight() {
		return weight;
	}
	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}
}



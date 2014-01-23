package com.zonrong.basics.style.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.zonrong.common.utils.MzfEnum;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.basics.vendor.service.VendorService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.templete.SaveTemplete;
import com.zonrong.entity.service.EntityService;
import com.zonrong.common.utils.MzfEnum.GoldClass;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-10-18
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class VendorStyleService{
	private static Logger logger = Logger.getLogger(VendorService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;

	public int createVendorStyle(Map style, List<Map<String, Object>> diamondList, IUser user) throws BusinessException {
		EntityMetadata vStyleMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_STYLE);

		style.put("isDated", Boolean.toString(false));
		String id = entityService.create(vStyleMetadata, style, user);

		int vendorStyleId = Integer.parseInt(id);
		saveDiamond(vendorStyleId, diamondList, user);

		List<Map<String,Object>> bomList = getBomList(vendorStyleId, style, user);

		Map<String,Object> where =new HashMap<String,Object>();
		where.put("vendorStyleId", vendorStyleId);
		List<Map<String,Object>> tempList=entityService.list(MzfEntity.VENDOR_STYLE_DIAMOND, where, null, user.asSystem());
		for(Map<String,Object> item:tempList){
			bomList.addAll(getDiamondBomList(vendorStyleId,item));
		}

		entityService.batchCreate(MzfEntity.BOM, bomList, user);
		return vendorStyleId;
	}

	private Map<String,Object> getMDiamondBomMap(int styleId,Map<String,Object> styleMap){
		Map<String,Object> item=new HashMap<String,Object>();
		//main diamond
		String minorType = "";
		Float minQuantity=MapUtils.getFloatValue(styleMap, "diamondWeight",0);
		Float maxQuantity=MapUtils.getFloatValue(styleMap, "diamondWeight2",0);
		String shape=MapUtils.getString(styleMap, "diamondShape");
		if(maxQuantity >= 0.07){
			minorType = "nakedDiamond";
		}else if(maxQuantity > 0 && maxQuantity < 0.07){
			minorType = "gravel";
		}
//		String quantity = null;
//		if (minQuantity != null && maxQuantity != null) {
//			quantity = minQuantity.toString() + " - " + maxQuantity.toString();
//		} else if (minQuantity != null) {
//			quantity = minQuantity.toString();
//		} else if (maxQuantity != null) {
//			quantity = maxQuantity.toString();
//		}
//		item.put("quantity", quantity);
		item.put("quantity", 1);
		item.put("minQuantity", minQuantity);
		item.put("maxQuantity", maxQuantity);
		item.put("shape", shape);
		item.put("vendorStyleId", styleId);
		item.put("rawmaterialType", "pdiamond");
		item.put("minorType", minorType);
		item.put("unit", "ct");

		return item;
	}

	private List<Map<String,Object>> getDiamondBomList(int styleId, Map<String,Object> diamondMap){
		List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();

		int count=MapUtils.getIntValue(diamondMap, "count");

		for(int i=0;i<count;i++){
			Map<String,Object> item=new HashMap<String,Object>();

			Float weight=MapUtils.getFloat(diamondMap, "weight");
			String shape=MapUtils.getString(diamondMap, "shape");
			int diamondId=MapUtils.getIntValue(diamondMap, "id");
			String minorType = "";
			if(weight >= 0.07){
				minorType = "nakedDiamond";
			}else if(weight > 0 && weight < 0.07){
				minorType = "gravel";
			}
			item.put("quantity", weight);
			item.put("minQuantity", weight);
			item.put("maxQuantity", weight);
			item.put("vendorStyleId", styleId);
			item.put("shape", shape);
			item.put("rawmaterialType", "mdiamond");
			item.put("minorType", minorType);
			item.put("unit", "ct");
			item.put("diamondId", diamondId);

			ret.add(item);
		}
		return ret;
	}

	private Map<String,Object> getGoldBomMap(int styleId, Map<String,Object> styleMap, String styleGoldType, GoldClass bomGoldType){
		Map<String,Object> item = new HashMap<String,Object>();

		Float quantity = MapUtils.getFloat(styleMap, styleGoldType);
		item.put("quantity", quantity);
		item.put("vendorStyleId", styleId);
		item.put("rawmaterialType", MzfEnum.RawmaterialType.gold);
		item.put("minorType", bomGoldType);
		item.put("unit", "g");

		return item;
	}

	private List<Map<String,Object>> getBomList(int styleId,Map<String,Object> styleMap,IUser user){
		List<Map<String,Object>> ret=new ArrayList<Map<String,Object>>();

		ret.add(getMDiamondBomMap(styleId, styleMap));
		ret.add(getGoldBomMap(styleId, styleMap, "ptWeight", MzfEnum.GoldClass.pt900));
		ret.add(getGoldBomMap(styleId, styleMap, "ptWeight", MzfEnum.GoldClass.pt950));
		ret.add(getGoldBomMap(styleId, styleMap, "kWeight", MzfEnum.GoldClass.k750));
		ret.add(getGoldBomMap(styleId, styleMap, "pdWeight", MzfEnum.GoldClass.pt900.pd950));
		ret.add(getGoldBomMap(styleId, styleMap, "silverWeight", MzfEnum.GoldClass.silver));

		return ret;
	}

	public void updateVendorStyle(int vendorStyleId, Map<String, Object> style, List<Map<String, Object>> diamondList, IUser user) throws BusinessException {
		EntityMetadata vStyleMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_STYLE);
		int row = entityService.updateById(vStyleMetadata, Integer.toString(vendorStyleId), style, user);
		if (row < 0) return;

		Map<String,Object> where=new HashMap<String,Object>();
		Map<String,Object> bomMap=new HashMap<String,Object>();

		where.put("vendorStyleId", vendorStyleId);
		where.put("rawmaterialType", MzfEnum.RawmaterialType.nakedDiamond);
		where.put("minorType", "mdiamond");

		Float minQuantity=MapUtils.getFloat(style, "diamondWeight");
		Float maxQuantity=MapUtils.getFloat(style, "diamondWeight2");
		String shape=MapUtils.getString(style, "diamondShape");
		bomMap.put("minQuantity", minQuantity);
		bomMap.put("maxQuantity", maxQuantity);
		bomMap.put("shape", shape);

		entityService.update(MzfEntity.BOM, bomMap, where, user);

		Float quantity=MapUtils.getFloat(style, "ptWeight");

		where.put("rawmaterialType", MzfEnum.RawmaterialType.gold);
		where.put("minorType", MzfEnum.GoldClass.pt950);
		bomMap.clear();
		bomMap.put("quantity", quantity);
		entityService.update(MzfEntity.BOM, bomMap, where, user);

		where.put("minorType", MzfEnum.GoldClass.pt900);
		entityService.update(MzfEntity.BOM, bomMap, where, user);

		quantity=MapUtils.getFloat(style, "kWeight");
		where.put("minorType", MzfEnum.GoldClass.k750);
		bomMap.put("quantity", quantity);
		entityService.update(MzfEntity.BOM, bomMap, where, user);

		quantity=MapUtils.getFloat(style, "pdWeight");
		where.put("minorType", MzfEnum.GoldClass.pd950);
		bomMap.put("quantity", quantity);
		entityService.update(MzfEntity.BOM, bomMap, where, user);

		quantity=MapUtils.getFloat(style, "silverWeight");
		where.put("minorType", MzfEnum.GoldClass.silver);
		bomMap.put("quantity", quantity);
		entityService.update(MzfEntity.BOM, bomMap, where, user);

		saveDiamond(vendorStyleId, diamondList, user);
	}

	public List<Map<String, Object>> getRawmaterial(int vendorStyleId, String goldClass, HttpServletRequest request) throws BusinessException {
		try {
			MzfEnum.GoldClass.valueOf(goldClass);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw new BusinessException("未指定金料成色");
		}
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("vendorStyleId", vendorStyleId);
		where.put("minorType", new String[]{goldClass, MzfEnum.RawmaterialType.nakedDiamond.toString()});
		return entityService.list(MzfEntity.BOM, where, null, User.getSystemUser());
	}

	private void saveDiamond(final int vendorStyleId, List<Map<String, Object>> diamondList, IUser user) throws BusinessException {
		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return metadataProvider.getEntityMetadata(MzfEntity.VENDOR_STYLE_DIAMOND);
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("vendorStyleId", vendorStyleId);
			}
		};

		templete.save(diamondList, user);
	}

	public void updateDated(Integer[] styleIds, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_STYLE);

		updateDated(styleIds, metadata, user);
	}

	void updateDated(Integer[] styleIds, EntityMetadata metadata, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(styleIds)) {
			throw new BusinessException("未指定要更新的款式记录");
		}

		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), styleIds);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user);
		if (list.size() != styleIds.length) {
			throw new BusinessException("参数有误");
		}

		for (Map<String, Object> style : list) {
			Integer id = MapUtils.getInteger(style, metadata.getPkCode());
			boolean isDated = MapUtils.getBooleanValue(style, "isDated");

			Map<String, Object> field = new HashMap<String, Object>();
			field.put("isDated", Boolean.toString(true));
			if (isDated) {
				field.put("isDated", Boolean.toString(false));
			}
			int row = entityService.updateById(metadata, id.toString(), field, user);
		}
	}
}



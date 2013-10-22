package com.zonrong.basics.style.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zonrong.basics.vendor.service.VendorService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.ProductType;
import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.templete.SaveTemplete;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;

/**
 * date: 2010-10-18
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class StyleService{
	private static Logger logger = Logger.getLogger(VendorService.class);
	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private VendorStyleService vendorStyleService;
	@Resource
	private Dao dao;
	
	public void createStyleByAllVendorStyle(IUser user) throws BusinessException {
		List<Map<String, Object>> list = entityService.list(MzfEntity.VENDOR_STYLE, new HashMap<String, Object>(), null, user);
		for (Map<String, Object> vendorStyle : list) {
			Integer id = MapUtils.getInteger(vendorStyle, "id");
			createStyleByVenderStyle(id, user);
		}
	}
	
	public int createStyleByVenderStyle(int id, IUser user) throws BusinessException {
		EntityMetadata vendorStyleMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_STYLE);
		EntityMetadata styleMetadata = metadataProvider.getEntityMetadata(MzfEntity.STYLE);
		Map<String, Object> vendorStyle = entityService.getById(vendorStyleMetadata, id, User.getSystemUser());
		Map<String, Object> style = new HashMap<String, Object>(vendorStyle);
		
		if (style == null || MapUtils.isEmpty(style)) {
			throw new BusinessException("没有找到id【" + id + "】对应的厂家款式");
		}
		
		if (MapUtils.getInteger(style, "styleId", null) != null) {
			throw new BusinessException("已经有对应的MZF款号，不能继续"); 
		}
		
		//图片类型字段特殊处理（不必在此将图片写入数据库）
		String imageId = MapUtils.getString(style, "imageId");	
		String newImageId = copyImage(imageId, user);		
		style.remove("imageId");		
		style.put("imageId1", newImageId);
		
		//新建MZF款式
		style.remove(styleMetadata.getPkCode());
		ProductType ptype = ProductType.valueOf(MapUtils.getString(style, "ptype"));
		String pkind = MapUtils.getString(style, "pkind");
		String styleCode = generateStyleCode(ptype, pkind);
		style.put("code", styleCode);
		String styleId = entityService.create(styleMetadata, style, user);
		
		//将MZF款式回填到厂家款式中
		style.clear();
		style.put("styleId", styleId);	
		entityService.updateById(vendorStyleMetadata, Integer.toString(id), style, user);
		
		//拷贝副石信息
		EntityMetadata vsDiamondMetadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_STYLE_DIAMOND);
		EntityMetadata sDiamondMetadata = metadataProvider.getEntityMetadata(MzfEntity.STYLE_DIAMOND);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("vendorStyleId", id);
		List<Map<String, Object>> diamondList = entityService.list(vsDiamondMetadata, map, null, user.asSystem());
		if (diamondList.size() > 0) {
			for (Map<String, Object> diamond : diamondList) {
				diamond.remove(sDiamondMetadata.getPkCode());
				diamond.remove("vendorStyleId");
				diamond.put("styleId", styleId);				
			}
			entityService.batchCreate(sDiamondMetadata, diamondList, user);
		}

		return Integer.parseInt(styleId);
	}
	
	private String copyImage(String id, IUser user) throws BusinessException {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.UPLOAD);
		Map<String, Object> imageMap = entityService.getById(metadata, id, user.asSystem());
		String ext = id.split("[|]")[1];
		String newImageId = getImageId(ext);
		imageMap.put(metadata.getPkCode(), newImageId);
		entityService.create(MzfEntity.UPLOAD, imageMap, user);
		
		return newImageId;
	}
	
	public static String getImageId(String ext) {
		final StringBuffer fileName = new StringBuffer();
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd"); 
		String dateStr = dateformat.format(new Date());	
		fileName.append(dateStr).append("-");	
		fileName.append(UUID.randomUUID().getMostSignificantBits()).append("|").append(ext);
		return fileName.toString();
	}
	
	public int createStyle(Map<String, Object> style, List<Map<String, Object>> diamondList, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.STYLE);
		
		ProductType ptype = ProductType.valueOf(MapUtils.getString(style, "ptype"));
		String pkind = MapUtils.getString(style, "pkind");
		String styleCode = generateStyleCode(ptype, pkind);
		style.put("code", styleCode);
		style.put("isDated", Boolean.toString(false));
		String id = entityService.create(metadata, style, user);
		
		int styleId = Integer.parseInt(id);
		saveDiamond(styleId, diamondList, user);
		
		return styleId;
	}
	
	public void updateStyle(int styleId, Map<String, Object> style, List<Map<String, Object>> diamondList, IUser user) throws BusinessException {
		EntityMetadata styleMetadata = metadataProvider.getEntityMetadata(MzfEntity.STYLE);		
		int row = entityService.updateById(styleMetadata, Integer.toString(styleId), style, user);
		if (row < 0) return;
		
		saveDiamond(styleId, diamondList, user);
	}	
	
	private void saveDiamond(final int styleId, List<Map<String, Object>> diamondList, IUser user) throws BusinessException {
		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return metadataProvider.getEntityMetadata(MzfEntity.STYLE_DIAMOND);
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("styleId", styleId);
			}			
		};
		
		templete.save(diamondList, user);
	}
	
	//生成MZF款式编码	
	String generateStyleCode(ProductType ptype, String pkind) throws BusinessException {
		String pTypeBizCode = BizCodeService.getBizName("config_productTypeOnStyle", ptype.toString());
		if (StringUtils.isBlank(pTypeBizCode)) {
			throw new BusinessException("未找到此商品类型对应的款式业务编码，生成MZF款号失败");
		}
		
		String pkindBizCode = BizCodeService.getBizName("config_productKindOnStyle", pkind);
		if (StringUtils.isBlank(pkindBizCode)) {
			throw new BusinessException("未找到此商品种类对应的款式业务编码，生成MZF款号失败");
		}		
		String prefix = pTypeBizCode + pkindBizCode;
		
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.STYLE);
		String col = metadata.getColumnName("code");
		QueryParam qp = new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.addColumn("max(" + col + ")", "styleCode");		
		Filter filter = Filter.field(col).like( prefix + "%");
		if (prefix.length() > 2) {
			filter.and(Filter.field("len(" + col + ") > 8"));
		} else if (prefix.length() == 2) {
			filter.and(Filter.field("len(" + col + ") = 8"));
		} else {
			throw new BusinessException("参数不合法，生成MZF款号失败");
		}
		qp.setFilter(filter);
		
		Map<String, Object> map = dao.get(qp);
		String dbCode = MapUtils.getString(map, "styleCode");
		int index = 0;
		if (StringUtils.isBlank(dbCode)) {
			index = 1;
		} else {
			String temp = dbCode.replaceAll("[\\D]", "");
			index = Integer.parseInt(temp);
			index = index + 1;
			if (index > 1000000) {
				throw new BusinessException("已经超出六位");
			}
		}
		index = index + 1000000;
		
		String styleCode = Integer.toString(index).substring(1);
		styleCode = prefix + styleCode;
		
		return styleCode;
	}
	
	private final static String PKIND_NAKEDDIAMOND = "99";
	public Map<String, Object> getNakedDiamondStyle() throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("ptype", ProductType.nakedDiamond);
		where.put("pkind", PKIND_NAKEDDIAMOND);
		
		List<Map<String, Object>> list = entityService.list(MzfEntity.STYLE, where, null, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未找到裸钻的MZF款号");
		}
		if (list.size() > 1) {
			if (logger.isDebugEnabled()) {
				logger.debug("MZF款式库中包含多个裸钻款号");
			}
		}
		
		return list.get(0);
	}
	
	public boolean isNakedDiamond(int styleId) throws BusinessException {
		Map<String, Object> style = entityService.getById(MzfEntity.STYLE, styleId, User.getSystemUser());
		ProductType ptype = ProductType.valueOf(MapUtils.getString(style, "ptype"));
		String pkind = MapUtils.getString(style, "pkind");
		if (ptype == ProductType.nakedDiamond) {
			if (PKIND_NAKEDDIAMOND.equals(pkind)) {
				return true;
			}
		}
		return false;
	}
	
	public void updateDated(Integer[] styleIds, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.STYLE);
		
		vendorStyleService.updateDated(styleIds, metadata, user);
	}
}



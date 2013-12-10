package com.zonrong.basics.rawmaterial.service;

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.basics.style.service.StyleService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.StorageType;
import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.RawmaterialInventoryService;
import com.zonrong.inventory.service.RawmaterialInventoryService.GoldClass;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-11-16
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class RawmaterialService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private ProductService productService;
	@Resource
	private StyleService styleService;
	@Resource
	private RawmaterialInventoryService rawmaterialInventoryService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private BizCodeService bizCodeService;
	@Resource
	private Dao dao;

	public enum RawmaterialType {
		nakedDiamond("裸石", "RD"),			//裸石
		gold("金料", ""),				//金料
		parts("配件", "RP"),				//配件
		gravel("碎石", "RSD"),				//碎石
		secondGold("旧金", "");			//旧金（旧金和原料一同记录在原料表中）

		private String name;
		private String prefix;

		RawmaterialType(String name, String prefix) {
			this.name = name;
			this.prefix = prefix;
		}
		public String getName() {
			return name;
		};

		public String getPrefix() {
			return prefix;
		};
	}

	public enum RawmaterialStatus {
		free,			//正常
		locked,			//锁定
		canedled		//核销
	}

	public int createRawmaterial(Map<String, Object> rawmaterial, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		RawmaterialType type = RawmaterialType.valueOf(MapUtils.getString(rawmaterial, "type"));
		String num = null;
		if (type == RawmaterialType.nakedDiamond) {
			num = generateNakedDiamondNum();
		} else if (type == RawmaterialType.parts) {
			GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(rawmaterial, "goldClass"));
			String partsStandard = MapUtils.getString(rawmaterial, "partsStandard");
			num = generatePartsNum(partsStandard, goldClass);
		} else if (type == RawmaterialType.gravel) {
			String gravelStandard = MapUtils.getString(rawmaterial, "gravelStandard");
			num = generateGravStringNum(gravelStandard);
		} else if (type == RawmaterialType.gold) {
			GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(rawmaterial, "goldClass"));
			num = generateGoldNum(goldClass);
		}  else if (type == RawmaterialType.secondGold) {
			GoldClass goldClass = GoldClass.valueOf(MapUtils.getString(rawmaterial, "goldClass"));
			num = "OLD" + generateGoldNum(goldClass);
		} else {
			throw new BusinessException("不支持该原料类型，无法生成原料条码");
		}

		rawmaterial.put("num", num);
		rawmaterial.put("status", RawmaterialStatus.free);
		String id = entityService.create(metadata, rawmaterial, user);

		return Integer.parseInt(id);
	}

	/**
	 * 根据商品资料创建裸石资料：商品裸钻转化为原料裸石
	 *
	 * @param productId
	 * @param user
	 * @throws BusinessException
	 */
	public int createNakedDiamondFromProduct(int productId, IUser user) throws BusinessException {
		Map<String, Object> product = productService.get(productId, user);
		String num = MapUtils.getString(product, "num");
		String certificate = MapUtils.getString(product, "certificate");

		String remark = "由商品[" + num + "]转成原料裸石, 用于配料";
		Map<String, Object> rawmaterial = new HashMap<String, Object>();
		rawmaterial.put("type", RawmaterialType.nakedDiamond);
		rawmaterial.put("shape", MapUtils.getString(product, "diamondShape"));
		rawmaterial.put("color", MapUtils.getString(product, "diamondColor"));
		rawmaterial.put("clean", MapUtils.getString(product, "diamondClean"));
		rawmaterial.put("cut", MapUtils.getString(product, "diamondCut"));
		rawmaterial.put("spec", MapUtils.getString(product, "diamondSize"));
		rawmaterial.put("cid1", certificate);
		rawmaterial.put("cost", MapUtils.getObject(product, "costPrice", 0));
		rawmaterial.put("remark", remark);
		return createRawmaterial(rawmaterial, user);
	}

	public void updateRawmaterial(int id, Map<String, Object> rawmaterial, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		entityService.updateById(metadata, Integer.toString(id), rawmaterial, user);
	}

	public Integer findGold(GoldClass goldClass, IUser user) throws BusinessException {
		if (goldClass == null) {
			throw new BusinessException("未指定金料成色");
		}
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("type", RawmaterialType.gold);
		where.put("goldClass", goldClass);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());

		if (CollectionUtils.isEmpty(list)) {
			return null;
		} else if (list.size() == 1) {
			return MapUtils.getInteger(list.get(0), metadata.getPkCode());
		} else {
			throw new BusinessException("发现多个金料成色相同的原料");
		}
	}

	public Integer findGravel(String gravelStandard, IUser user) throws BusinessException {
		if (gravelStandard == null) {
			throw new BusinessException("未指定碎石规格");
		}
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("type", RawmaterialType.gravel);
		where.put("gravelStandard", gravelStandard);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());

		if (CollectionUtils.isEmpty(list)) {
			return null;
		} else if (list.size() == 1) {
			return MapUtils.getInteger(list.get(0), metadata.getPkCode());
		} else {
			throw new BusinessException("发现多个规格相同的碎石记录");
		}
	}

	public Integer findParts(String partsType, GoldClass goldClass, String partsStandard, IUser user) throws BusinessException {
		if (StringUtils.isBlank(partsType)) {
			throw new BusinessException("未指定配件类型");
		}
		if (goldClass == null) {
			throw new BusinessException("未指定配件配件的金料成色");
		}
		if (StringUtils.isBlank(partsStandard)) {
			throw new BusinessException("未指定配件规格");
		}
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("type", RawmaterialType.parts);
		where.put("partsType", partsType);
		where.put("goldClass", goldClass);
		where.put("partsStandard", partsStandard);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());

		if (CollectionUtils.isEmpty(list)) {
			return null;
		} else if (list.size() == 1) {
			return MapUtils.getInteger(list.get(0), metadata.getPkCode());
		} else {
			throw new BusinessException("发现多个类型相同的配件记录");
		}
	}

	public Integer findSecondGold(GoldClass goldClass, IUser user) throws BusinessException {
		if (goldClass == null) {
			throw new BusinessException("未指定旧金的金料成色");
		}

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("type", RawmaterialType.secondGold);
		where.put("goldClass", goldClass);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());

		if (CollectionUtils.isEmpty(list)) {
			return null;
		} else if (list.size() == 1) {
			return MapUtils.getInteger(list.get(0), metadata.getPkCode());
		} else {
			throw new BusinessException("发现多个类型相同的金料记录");
		}
	}

	public void updateStatus(Integer[] rawmaterialId, RawmaterialStatus status, String statusRemark, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", status);
		field.put("statusRemark", statusRemark);

		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), rawmaterialId);
		entityService.update(metadata, field, where, user);
	}

	public void freeDiamond(Integer[] rawmaterialId, String remark, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(rawmaterialId)) {
			return;
		}
		updateStatus(rawmaterialId, RawmaterialStatus.free, remark, user);
	}

	public void lockDiamond(Integer[] rawmaterialId, String remark, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(rawmaterialId)) {
			return;
		}
		updateStatus(rawmaterialId, RawmaterialStatus.locked, remark, user);
	}


	/**
	 * 原料裸石转化为商品裸钻
	 *
	 * @param rawmaterialId
	 * @param user
	 * @throws BusinessException
	 */
	public void translateToProduct(int rawmaterialId, Map<String, Object> target, IUser user) throws BusinessException {
		Map<String, Object> source = entityService.getById(MzfEntity.RAWMATERIAL, rawmaterialId, user);
		RawmaterialType type = RawmaterialType.valueOf(MapUtils.getString(source, "type"));
		if (type != RawmaterialType.nakedDiamond) {
			throw new BusinessException("只有裸石才能转化为商品");
		}
		//货品信息转化信息

		target.remove("id");
		Integer styleId = MapUtils.getInteger(target, "styleId");
		if (!styleService.isNakedDiamond(styleId)) {
			throw new BusinessException("所选的MZF款号非裸钻");
		}

		Map<String, Object> style = entityService.getById(MzfEntity.STYLE, styleId, user);
		target.put("ptype", MapUtils.getString(style, "ptype"));
		target.put("pkind", MapUtils.getString(style, "pkind"));
		target.put("styleId", MapUtils.getInteger(style, "id"));
		target.put("sourceType", BizType.translateToProduct);
		target.put("sourceId", MapUtils.getString(source, "num"));
		int productId = productService.createProduct(target, null, null, ProductStatus.free, null, user);
		productService.recreateProductNum(productId, null, user);

		BizType bizType = BizType.translateToProduct;
		//String remark = "原料裸石转化为商品裸钻";
        Map<String,Object> product = productService.get(productId,user);
        String remark = "商品条码：["+MapUtils.getString(product,"num")+"]";
		//原料出库
		rawmaterialInventoryService.deliveryDiamondByRawmaterialId(bizType, rawmaterialId, remark, user);
		//deleteById(rawmaterialId, user);
        Map<String,Object> status = new HashMap<String,Object>();
        status.put("status","toProduct");
        entityService.updateById(MzfEntity.RAWMATERIAL,rawmaterialId+"",status,user);

		//商品入库
        remark = "原料条码：["+MapUtils.getString(source,"num")+"]";
		StorageType storageType = productInventoryService.getDefaultStorageType(productId);
		productInventoryService.warehouse(bizType, productId, user.getOrgId(), storageType, user.getId(), user.getOrgId(), remark, user);
	}

	public void addCost(int rawmaterialId, BigDecimal cost, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		Map<String, Object> rawmaterial = entityService.getById(metadata, Integer.toString(rawmaterialId), user.asSystem());

		BigDecimal dbCost = new BigDecimal(MapUtils.getString(rawmaterial, "cost"));
		dbCost = dbCost.add(cost);
		if (dbCost.doubleValue() < 0) {
			throw new BusinessException("该原料成本已经低于0");
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("cost", dbCost);
		entityService.updateById(metadata, Integer.toString(rawmaterialId), field, user);
	}

	public void addWeight(int rawmaterialId, BigDecimal weight, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);
		Map<String, Object> rawmaterial = entityService.getById(metadata, Integer.toString(rawmaterialId), user.asSystem());

		BigDecimal dbWeight = new BigDecimal(MapUtils.getString(rawmaterial, "weight"));
		dbWeight = dbWeight.add(weight);
		if (dbWeight.doubleValue() < 0) {
			throw new BusinessException("该原料总重量已经低于0");
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("weight", dbWeight);
		entityService.updateById(metadata, Integer.toString(rawmaterialId), field, user);
	}

	public void deleteById(int rawmaterialId, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("rawmaterialId", rawmaterialId);
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("rawmaterialId", null);
		entityService.update(MzfEntity.VENDOR_ORDER_RAWMATERIAL_ORDER_DETAIL, field, where, user);

		entityService.deleteById(MzfEntity.RAWMATERIAL, Integer.toString(rawmaterialId), user);
	}

	//RD + 12位流水号
	private String generateNakedDiamondNum() throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.RAWMATERIAL);

		String prefix = RawmaterialType.nakedDiamond.getPrefix();
		String col = metadata.getColumnName("num");
		String typeCol = metadata.getColumnName("type");
		QueryParam qp = new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.addColumn("max(" + col + ")", "num");
		Filter filter = Filter.field(col).like(prefix + "%");
		filter.and(Filter.field(typeCol).eq(RawmaterialType.nakedDiamond));
		qp.setFilter(filter);

		Map<String, Object> map = dao.get(qp);
		String dbNum = MapUtils.getString(map, "num");
		long index = 0;
		Long baseIndex = new Long("100000000");
		if (StringUtils.isBlank(dbNum)) {
			index = 1;
		} else {
			String temp = dbNum.substring(dbNum.length() - (baseIndex.toString().length() - 1));
			index = Integer.parseInt(temp);
			index = index + 1;
			if (index > baseIndex) {
				throw new BusinessException("已经超出" + baseIndex.toString().length() + "位");
			}
		}
		index = index + baseIndex;

		String num = Long.toString(index).substring(1);
		num = prefix + num;

		return num;
	}

	//RP + 六位金料成色 + A[, B, C, D]
	private String generatePartsNum(String partsStandard, GoldClass goldClass) throws BusinessException {
		if (StringUtils.isBlank(partsStandard)) {
			throw new BusinessException("未指定配件规格");
		}
		if (goldClass == null) {
			throw new BusinessException("未指定金料成色");
		}
		String prefix = RawmaterialType.parts.getPrefix();
		String goldClassName = bizCodeService.getBizName("config_goldClass", goldClass.toString());
		if (StringUtils.isBlank(goldClassName)) {
			throw new BusinessException("未取到对应的金料成色编码，无法生成原料条码");
		}
		String partsStandardName = bizCodeService.getBizName("config_partsStandard", partsStandard);
		if (StringUtils.isBlank(partsStandardName)) {
			throw new BusinessException("未取到对应的配件规格编码，无法生成原料条码");
		}
		return prefix + goldClassName + partsStandardName;
	}

	//RSD + A[, B, C, D]
	private String generateGravStringNum(String gravelStandard) throws BusinessException {
		if (StringUtils.isBlank(gravelStandard)) {
			throw new BusinessException("未指定碎石规格");
		}
		String prefix = RawmaterialType.gravel.getPrefix();
		String gravelStandardName = bizCodeService.getBizName("config_gravelStandard", gravelStandard);
		if (StringUtils.isBlank(gravelStandardName)) {
			throw new BusinessException("未取到对应的碎石规格编码，无法生成原料条码");
		}
		return prefix + gravelStandardName;
	}

	//取六位金料成色
	private String generateGoldNum(GoldClass goldClass) throws BusinessException {
		if (goldClass == null) {
			throw new BusinessException("未指定金料成色");
		}
		String goldClassName = bizCodeService.getBizName("config_goldClass", goldClass.toString());
		if (StringUtils.isBlank(goldClassName)) {
			throw new BusinessException("未取到对应的金料成色编码，无法生成原料条码");
		}
		return goldClassName;
	}
}



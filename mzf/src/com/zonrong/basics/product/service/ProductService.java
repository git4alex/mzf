package com.zonrong.basics.product.service;

import com.zonrong.basics.StatusCarrier;
import com.zonrong.basics.rawmaterial.service.RawmaterialService;
import com.zonrong.basics.rawmaterial.service.RawmaterialService.RawmaterialType;
import com.zonrong.basics.style.service.StyleService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.InventoryStatus;
import com.zonrong.common.utils.MzfEnum.ProductType;
import com.zonrong.common.utils.MzfEnum.SaleDetailType;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.dao.Dao;
import com.zonrong.core.dao.QueryParam;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.core.templete.SaveTemplete;
import com.zonrong.core.util.Carrier;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.product.service.ProductInventoryService;
import com.zonrong.inventory.service.InventoryService.BizType;
import com.zonrong.inventory.service.RawmaterialInventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * date: 2010-11-2
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class ProductService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private TransactionService transactionService;
	@Resource
	private StyleService styleService;
	@Resource
	private RawmaterialInventoryService rawmaterialInventoryService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private RawmaterialService rawmaterialService;
	@Resource
	private FlowLogService logService;
	@Resource
	private Dao dao;
	@Resource
	private BusinessLogService businessLogService;

	public enum ProductStatus{
		free,			//正常
		locked,			//锁定
		toRawmater,     //转成原料
		selled;			//已售

		public String getText() {
			return BizCodeService.getBizName("productStatus", this.toString());
		}
	}

	public List<Map<String, Object>> list(Integer[] productId, IUser user) throws BusinessException {
		if (ArrayUtils.isEmpty(productId)) {
			return new ArrayList<Map<String,Object>>();
		}

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put(metadata.getPkCode(), productId);
		return entityService.list(metadata, where, null, user);
	}

	//补录商品
	public int supplyProduct(Map<String, Object> product,
			List<Map<String, Object>> diamondList,
			List<Map<String, Object>> certificateList,
			ProductStatus status, String statusRemark, IUser user)throws BusinessException{
		String num = MapUtils.getString(product, "num");
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("num", num);
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT, where, null, User.getSystemUser());
		if(list.size() >= 1){
			throw new BusinessException("商品["+ num +"]已经存在" );
		}else{
			return createProduct(product, diamondList, certificateList, status, statusRemark, user);
		}

	}
	public int createProduct(Map<String, Object> product,
			List<Map<String, Object>> diamondList,
			List<Map<String, Object>> certificateList,
			ProductStatus status, String statusRemark, IUser user)
			throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);

		String isQc = MapUtils.getString(product, "isQc", "false");
		String isCid = MapUtils.getString(product, "isCid", "false");
		product.put("isQc", isQc);
		product.put("isCid", isCid);

		String promotionPrice = MapUtils.getString(product, "promotionPrice");
		if (StringUtils.isBlank(promotionPrice)) {
			String retailBasePrice = MapUtils.getString(product, "retailBasePrice");
			product.put("promotionPrice", retailBasePrice);
		}

		product.put("status", status);
		product.put("statusRemark", statusRemark);
		String id = entityService.create(metadata, product, user);

		int productId = Integer.parseInt(id);
		saveDiamond(productId, diamondList, user);
		saveCertificate(productId, certificateList, user);

		//记录流程
		int transId = transactionService.createTransId();
		logService.createLog(transId, MzfEntity.PRODUCT, id, "新建商品", TargetType.product, id, null, user);
		return productId;
	}

	public boolean hasNum(int productId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);
		Map<String, Object> dbProduct = entityService.getById(metadata, productId, user.asSystem());

		if (MapUtils.isEmpty(dbProduct)) {
			throw new BusinessException("商品[id=" + productId + "]不存在");
		}

		String num = MapUtils.getString(dbProduct, "num");
		if (StringUtils.isNotBlank(num)) {
			return true;
		}
		return false;
	}

	public String generateProductNum(EntityMetadata metadata, int productId, String prefix) throws BusinessException {
		if (metadata == null) {
			throw new BusinessException("未指定元数据");
		}
		if (StringUtils.isBlank(prefix)) {
			prefix = "";
		}
		Map<String, Object> product = entityService.getById(metadata, productId, User.getSystemUser());
		String pkind = MapUtils.getString(product, "pkind");
		if (StringUtils.isBlank(pkind)) {
			throw new BusinessException("商品种类为空");
		}

		ProductType ptype = ProductType.valueOf(MapUtils.getString(product, "ptype"));
		String goldClassStr = MapUtils.getString(product, "goldClass");
		String key = ptype.toString();
		if (ptype == ProductType.diamond) {
			if (StringUtils.isNotBlank(goldClassStr)) {
				key = key + "," + goldClassStr;
			} else {
				throw new BusinessException("商品类型为" + ptype.getText() + "，但未指定金料成色，无法生成商品条码");
			}
		}

		Long baseIndex = new Long("100000000");

		StringBuffer sb = new StringBuffer(prefix);
		sb.append(pkind);
		String configInfo = BizCodeService.getBizName("config_productTypeOnProductNum", key);
		if (StringUtils.isBlank(configInfo)) {
			throw new BusinessException("未取到对应的商品类型配置信息，无法生成商品条码");
		}
		sb.append(configInfo);

		String col = metadata.getColumnName("num");
		QueryParam qp = new QueryParam();
		qp.setTableName(metadata.getTableName());
		qp.addColumn("max(" + col + ")", "num");
		Filter filter = Filter.field("len(" + col + ")").eq(baseIndex.toString().length() - 1 + sb.length());
		filter.and(Filter.field(col).like(sb.toString() + "%"));
//		Filter filter = Filter.field(col).like( prefix + "%");
		qp.setFilter(filter);

		Map<String, Object> map = dao.get(qp);
		String dbNum = MapUtils.getString(map, "num");
		long index = 0;
		if (StringUtils.isBlank(dbNum)) {
			index = 1;
		} else {
			String temp = dbNum.substring(sb.length());
			index = Integer.parseInt(temp);
			index = index + 1;
			if (index > baseIndex) {
				throw new BusinessException("已经超出" + baseIndex.toString().length() + "位");
			}
		}
		index = index + baseIndex;

		String num = Long.toString(index).substring(1);
		sb.append(num);

		return sb.toString();
	}

	public void recreateProductNum(int productId, String num, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);
		Map<String, Object> product = entityService.getById(metadata, productId, user.asSystem());

		if (StringUtils.isBlank(num)) {
			num = generateProductNum(metadata, productId, null);
		}

		product.clear();
		product.put("num", num);

		entityService.updateById(metadata, Integer.toString(productId), product, user);
	}

	public List<Map<String, Object>> createProductNum(Integer[] productIds, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", productIds);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, user);
		for (Map<String, Object> product : list) {
			Integer productId = MapUtils.getInteger(product, "id");
			String num = MapUtils.getString(product, "num");
			if (StringUtils.isBlank(num)) {
				num = generateProductNum(metadata, productId, null);
				Map<String, Object> field = new HashMap<String, Object>();
				field.put("num", num);
				entityService.updateById(metadata, productId.toString(), field, user);
			}
		}

		return entityService.list(metadata, where, null, user);
	}

	public boolean isNakedDiamond(int productId) throws BusinessException {
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, productId, User.getSystemUser());
		ProductType ptype = ProductType.valueOf(MapUtils.getString(product, "ptype"));
		if (ptype != ProductType.nakedDiamond) {
			return false;
		}
		String pkind = MapUtils.getString(product, "pkind");
		if (!"99".equals(pkind)) {
			return false;
		}

		return true;
	}

	public boolean isBargains(int productId) throws BusinessException {
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, productId, User.getSystemUser());
		String isQc = MapUtils.getString(product, "isBargains");
		if (new Boolean(isQc)) {
			return true;
		}
		return false;
	}
	//权限折扣是否小于9折
    //改为：总折扣小于8.5折
	public boolean isAuthorityDiscount(Map<String, Object> saleDetail) throws BusinessException{
		BigDecimal price = new BigDecimal(MapUtils.getString(saleDetail, "price","0"));
		if(price.doubleValue() > 0){
			//BigDecimal authorityDiscount = new BigDecimal(MapUtils.getString(saleDetail, "authorityDiscount","0"));
            BigDecimal totalDiscount = new BigDecimal(MapUtils.getString(saleDetail, "totalDiscount","0"));
            BigDecimal tmp = price.subtract(totalDiscount);
			double discountRate = tmp.divide(price,3, BigDecimal.ROUND_HALF_UP).doubleValue();
			if(discountRate < 0.85){
				return true;
			}
		}

		return false;
	}

	public boolean isQc(int productId) throws BusinessException {
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, productId, User.getSystemUser());
		return isQc(product);
	}

	public boolean isCid(int productId) throws BusinessException {
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, productId, User.getSystemUser());
		return isCid(product);
	}

	public boolean isQc(Map<String, Object> product) throws BusinessException {
		String isQc = MapUtils.getString(product, "isQc");
		if (new Boolean(isQc)) {
			return true;
		}
		return false;
	}

	public boolean isCid(Map<String, Object> product) throws BusinessException {
		String isCid = MapUtils.getString(product, "isCid");
		if (new Boolean(isCid)) {
			return true;
		}
		return false;
	}

	public Map<String, Object> get(int productId, IUser user) throws BusinessException {
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, Integer.toString(productId), user);

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("productId", productId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT_CERTIFICATE, where, null, user);
		List<String> certificates = new ArrayList<String>();
		for (Map<String, Object> certificate : list) {
			String code = MapUtils.getString(certificate, "code");
			certificates.add(code);
		}

		product.put("certificate", StringUtils.join(certificates.iterator(), ", "));
		return product;
	}

	public Map<String, Object> findByProductNum(String productNum, IUser user) throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("num", productNum);
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT, where, null, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException("未找到该商品[" + productNum + "]");
		} else if (list.size() > 1) {
			throw new BusinessException("库存中找到多件该商品[" + productNum + "]");
		}

		return list.get(0);
	}

	public Map<String, Object> findAllByProductNum(String num, IUser user) throws BusinessException {
		if (StringUtils.isBlank(num)) {
			return null;
		}
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("num", num);
		List<Map<String, Object>> list = entityService.list(MzfEntity.VIEW_PRODUCT, where, null, user);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		if (list.size() > 1) {
			throw new BusinessException("找到多件商品，请输入正确的商品条码");
		}
		Map<String, Object> product = list.get(0);
		int productId = MapUtils.getInteger(product, "id");

		//销售单、退货单
		where = new HashMap<String, Object>();
		where.put("type", SaleDetailType.product);
		where.put("targetId", productId);
		list = entityService.list(MzfEntity.SALE_DETAIL_VIEW, where, null, user);
		Set<Integer> saleIds = new HashSet<Integer>();
		Set<Integer> returnsIds = new HashSet<Integer>();
		for (Map<String, Object> detail : list) {
			Integer saleId = MapUtils.getInteger(detail, "saleId");
			Integer returnsSaleId = MapUtils.getInteger(detail, "returnsSaleId");
			saleIds.add(saleId);
			if (returnsSaleId != null)
				returnsIds.add(returnsSaleId);
		}

		if (CollectionUtils.isNotEmpty(saleIds)) {
			product.put("saleIds", saleIds.toArray(new Integer[]{}));
		}
		if (CollectionUtils.isNotEmpty(returnsIds)) {
			product.put("returnsIds", returnsIds.toArray(new Integer[]{}));
		}

		//客订单
		where = new HashMap<String, Object>();
		where.put("productId", productId);
		list = entityService.list(MzfEntity.CUS_ORDER, where, null, user);
		Set<Integer> cusOrderIds = new HashSet<Integer>();
		for (Map<String, Object> order : list) {
			Integer cusOrderId = MapUtils.getInteger(order, "id");
			cusOrderIds.add(cusOrderId);
		}
		if (CollectionUtils.isNotEmpty(cusOrderIds)) {
			product.put("cusOrderIds", cusOrderIds.toArray(new Integer[]{}));
		}

		//维修单
		where = new HashMap<String, Object>();
		where.put("productId", productId);
		list = entityService.list(MzfEntity.MAINTAIN, where, null, user);
		Set<Integer> maintainIds = new HashSet<Integer>();
		for (Map<String, Object> order : list) {
			Integer maintainId = MapUtils.getInteger(order, "id");
			maintainIds.add(maintainId);
		}
		if (CollectionUtils.isNotEmpty(maintainIds)) {
			product.put("maintainIds", maintainIds.toArray(new Integer[]{}));
		}

		return product;
	}

	public String getProductNum(int productId, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);
		Map<String, Object> dbProduct = entityService.getById(metadata, productId, user.asSystem());

		if (MapUtils.isEmpty(dbProduct)) {
			throw new BusinessException("商品[id=" + productId + "]不存在");
		}

		return MapUtils.getString(dbProduct, "num");
	}

	public ProductStatus getStatus(int productId) throws BusinessException {
		Map<String, Object> product = entityService.getById(MzfEntity.PRODUCT, productId, User.getSystemUser());
		return ProductStatus.valueOf(MapUtils.getString(product, "status"));
	}

	public void updateProductById(int productId, Map<String, Object> product,
			List<Map<String, Object>> diamondList,
			List<Map<String, Object>> certificateList, IUser user)
			throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);
		product.remove("num");
		product.remove("status");


		String promotionPrice = MapUtils.getString(product, "promotionPrice");
		if (StringUtils.isBlank(promotionPrice)) {
			String retailBasePrice = MapUtils.getString(product, "retailBasePrice");
			product.put("promotionPrice", retailBasePrice);
		}

		entityService.updateById(metadata, Integer.toString(productId), product, user);

		saveDiamond(productId, diamondList, user);
		saveCertificate(productId, certificateList, user);

		//记录流程
		int transId = transactionService.findTransId(MzfEntity.PRODUCT, Integer.toString(productId), user);
		logService.createLog(transId, MzfEntity.PRODUCT, Integer.toString(productId), "修改商品档案", TargetType.product, Integer.toString(productId), null, user);
		//记录操作流程
		businessLogService.log("修改商品档案", "商品编号：" + productId, user);
	}

	public void updatePTProductPrice(int productId,BigDecimal ptPrice,IUser user)throws BusinessException{
		Map<String, Object> product = new HashMap<String, Object>();
		product.put("retailBasePrice", ptPrice);
		product.put("promotionPrice", ptPrice);
		entityService.updateById(MzfEntity.PRODUCT, Integer.toString(productId), product, user);

		//记录流程
		int transId = transactionService.findTransId(MzfEntity.PRODUCT, Integer.toString(productId), user);
		logService.createLog(transId, MzfEntity.PRODUCT, Integer.toString(productId), "修改商品一口价，销售一口价（铂金）档案", TargetType.product, Integer.toString(productId), null, user);
	}

	public void deleteById(int productId, IUser user) throws BusinessException {
		saveDiamond(productId, null, user);
		saveCertificate(productId, null, user);

		entityService.deleteById(MzfEntity.PRODUCT, Integer.toString(productId), user);
	}

	public void updateStatus(Integer productId, ProductStatus status, String statusRemark, StatusCarrier carrier, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);

		Map<String, Object> field = entityService.getById(MzfEntity.PRODUCT, productId, user.asSystem());
		if (carrier != null) {
			carrier.setCarrier(field);
			carrier.active(field);
		}
		field = new HashMap<String, Object>();
		field.put("status", status);
		field.put("statusRemark", statusRemark);
		int row = entityService.updateById(metadata, productId.toString(), field, user);
		if (row == 0) {
			throw new BusinessException("更新商品状态失败。原因：未找到相应商品[" + productId +"]");
		}
	}

//	public void updateStatus(Integer productId, ProductStatus status, String statusRemark, IUser user) throws BusinessException {
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(BizEntity.PRODUCT);
//
//		Map<String, Object> field = new HashMap<String, Object>();
//		field.put("status", status);
//		field.put("statusRemark", statusRemark);
//		int row = entityService.updateById(metadata, productId.toString(), field, user);
//		if (row == 0) {
//			throw new BusinessException("更新商品状态失败。原因：未找到相应商品[" + productId +"]");
//		}
//	}

	public void free(int productId, String remark, IUser user) throws BusinessException {
		updateStatus(productId, ProductStatus.free, remark, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				ProductStatus status = this.getStatus(ProductStatus.class);
				if (status != ProductStatus.locked) {
					throw new BusinessException("商品[" + this.getNum() + "]状态为" + status.getText() + ", 不能解锁");
				}
			}
		}, user);
		//记录操作日志
		businessLogService.log("解锁(商品库存)", "商品编号:" + productId, user);
	}

	public void lock(Integer productId, String remark, IUser user) throws BusinessException {
		updateStatus(productId, ProductStatus.locked, remark, new StatusCarrier(){
			@Override
			public void active(Map<String, Object> carrier) throws BusinessException {
				ProductStatus status = this.getStatus(ProductStatus.class);
				if (status != ProductStatus.free) {
					throw new BusinessException("商品[" + this.getNum() + "]状态为" + status.getText() + ", 该操作不能继续");
				}
			}
		}, user);
	}

//	public void check(Integer[] productId, ProductStatus status, String message) throws BusinessException {
//		if (status == null) {
//			throw new BusinessException("ProductStatus is null");
//		}
//		Map<String, Object> where = new HashMap<String, Object>();
//		where.put("id", productId);
//		List<Map<String, Object>> list = entityService.list(BizEntity.PRODUCT, where, null, User.getSystemUser());
//		if (CollectionUtils.isEmpty(list)) return;
//
//		for (Map<String, Object> product : list) {
//			ProductStatus dbStatus = ProductStatus.valueOf(MapUtils.getString(product, "status"));
//			if (dbStatus != status) {
//				throw new BusinessException(message);
//			}
//		}
//	}

	public void check(Integer[] productId, ProductStatus[] statuss, Carrier carrier) throws BusinessException {
		if (statuss == null) {
			throw new BusinessException("ProductStatus is null");
		}
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", productId);
		List<Map<String, Object>> list = entityService.list(MzfEntity.PRODUCT, where, null, User.getSystemUser());
		if (CollectionUtils.isEmpty(list)) return;

		for (Map<String, Object> product : list) {
			ProductStatus dbStatus = ProductStatus.valueOf(MapUtils.getString(product, "status"));
			if (!ArrayUtils.contains(statuss, dbStatus)) {
				carrier.active(product);
			}
		}
	}

	public void check(Integer[] producdId, ProductStatus status, Carrier carrier) throws BusinessException {
		if (status == null) {
			throw new BusinessException("ProductStatus is null");
		}
		check(producdId, new ProductStatus[]{status}, carrier);
	}

	/**
	 * 商品裸钻转化为原料裸石
	 *
	 * @param productId
	 * @param user
	 * @throws BusinessException
	 */
	public void translateToRawmaterial(int productId, Map<String, Object> target, IUser user) throws BusinessException {
		Map<String, Object> source = entityService.getById(MzfEntity.PRODUCT, productId, user);
		ProductStatus status = ProductStatus.valueOf(MapUtils.getString(source, "status"));
		if (status != ProductStatus.free) {
			throw new BusinessException("该商品不能转化为原料");
		}
//		Integer styleId = MapUtils.getInteger(source, "styleId");
//		if (!styleService.isNakedDiamond(styleId)) {
//			throw new BusinessException("只有裸钻才能转化为原料");
//		}
		String ptype = MapUtils.getString(source, "ptype", "");
		if(!ptype.equals("") && !ptype.equals("nakedDiamond")){
			throw new BusinessException("只有裸钻才能转化为原料");
		}

		//货品信息转化信息
		target.remove("id");
		target.put("sourceType", BizType.translateToRawmaterial);
		target.put("sourceId", productId);
		target.put("type", RawmaterialType.nakedDiamond);
		Integer rawmaterialId = rawmaterialService.createRawmaterial(target, user);

		BizType bizType = BizType.translateToRawmaterial;
		//String remark = "商品裸钻转化为原料裸石";
        Map<String,Object> rawmaterial = entityService.getById(MzfEntity.RAWMATERIAL,rawmaterialId,user);
        String remark = "原料条码：["+MapUtils.getString(rawmaterial,"num")+"]";
		//商品出库
		productInventoryService.deliveryByProductId(bizType, productId, remark, InventoryStatus.onStorage, user);
		//修改商品状态
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", ProductStatus.toRawmater);
		entityService.updateById(MzfEntity.PRODUCT, productId + "", field, user);
		//deleteById(productId, user);

		//原料入库
        remark = "商品条码：["+MapUtils.getString(source,"num")+"]";
		rawmaterialInventoryService.warehouseDiamond(BizType.translateToRawmaterial, rawmaterialId, user.getOrgId(), remark, user);
	}
	public HSSFWorkbook exportExcel(Integer[] ids,String[] filedCodes,IEntityCode entity,OutputStream stream) throws BusinessException{
		    Map<String,String> filedNameMap = getFiled(filedCodes,entity);
		try {
			HSSFWorkbook  book =  new HSSFWorkbook();
			HSSFSheet sheet = book.createSheet();
			 //excel表头（2003）
			HSSFRow titleRow = sheet.createRow(0);
			for (int i = 0;i < filedCodes.length;i++) {
			  String code = filedCodes[i];
			  HSSFCell cell = titleRow.createCell(i);
			  String title = filedNameMap.get(code);
			  cell.setCellValue(title);
			}
			Map<String,Object> where = new HashMap<String,Object>();
			where.put("id", ids);
			List<Map<String,Object>>  entitys = entityService.list(entity, where, null, User.getSystemUser());
			for (int j = 0;j < entitys.size();j++) {
				Map<String,Object> map = entitys.get(j);
				HSSFRow row = sheet.createRow(j+1);
				for (int i = 0;i < filedCodes.length;i++) {
					 String code = filedCodes[i];
					 HSSFCell cell = row.createCell(i);
					 String value = "";
					 if(map.get(code) != null){
						 value = map.get(code).toString();
					 }
					 cell.setCellValue(value);
				}
			}
			//book.write(stream);
			return book;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new BusinessException("导出商品数据出现错误"+e.getMessage());
		}

	}

	private Map<String,String> getFiled(String[] filedCodes,IEntityCode entity)throws BusinessException{
		EntityMetadata entityMetadata = metadataProvider.getEntityMetadata(entity);
		Map<String,String> filedMap = new HashMap<String,String>();
		for (String code : filedCodes) {
			String fileName = entityMetadata.getColumnTitle(code);
			filedMap.put(code,fileName);
		}
		return filedMap;
	}
	private void saveDiamond(final int productId, List<Map<String, Object>> diamondList, IUser user) throws BusinessException {
		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return metadataProvider.getEntityMetadata(MzfEntity.PRODUCT_DIAMOND);
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("productId", productId);
			}
		};

		templete.save(diamondList, user);
	}

	private void saveCertificate(final int productId, List<Map<String, Object>> certificateList, IUser user) throws BusinessException {
		SaveTemplete templete = new SaveTemplete(entityService) {
			protected EntityMetadata getEntityMetadata() throws BusinessException {
				return metadataProvider.getEntityMetadata(MzfEntity.PRODUCT_CERTIFICATE);
			}
			protected void setForeignKey(Map<String, Object> map) throws BusinessException {
				map.put("productId", productId);
			}
		};

		templete.save(certificateList, user);
	}
}




package com.zonrong.sale.rule.service;

import com.zonrong.basics.chit.service.ChitService;
import com.zonrong.basics.product.service.ProductService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.ChitStatus;
import com.zonrong.common.utils.MzfEnum.ProductType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.MaterialInventoryService;
import com.zonrong.inventory.service.ProductInventoryService;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RuleSingleAppService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	private ProductService productService;
	@Resource
	private ProductInventoryService productInventoryService;
	@Resource
	private MaterialInventoryService materialInventoryService;
	@Resource
	private ChitService chitService;

	public Object matchRule(String productNum, IUser user) throws BusinessException {
		Random rd = new Random(10);

		Map<String, Object> product = productService.findAllByProductNum(productNum, user);
		if (MapUtils.isEmpty(product)) {
			throw new BusinessException("未找到该条码[" + productNum + "]对应的商品");
		}
		int productId = MapUtils.getIntValue(product, "id");
		List<Map<String,Object>> rules = entityService.list(MzfEntity.SALERULE, new HashMap(), null, user);
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();

		//TODO匹配规则
		if (rd.nextBoolean()) {
			int i = 0;
			for (Map map:rules) {
				i++;
				map.put("productId", productId);
				map.put("productName", "商品" + i);
				map.put("ruleId", MapUtils.getInteger(map,"id"));
				map.put("ruleName",MapUtils.getString(map,"name"));
				map.put("remark", "促销规则备注" + (i + 1));
				list.add(map);
			}
			return list;
		} else {
			//匹配到一条规则
			if (rd.nextBoolean()) {
				return appRule(productId, 1, user);
			} else {
				return productInventoryService.getInventory(productId, user.getOrgId());
			}
		}

	}

	//TODO 赠送商品  赠送单件商品
	private List<Map<String, Object>> listGiveSingleProduct(Map<String, Object> context, IUser user) throws BusinessException {
		List<Map<String, Object>> giveSingleProductList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> giveSingleProductSource = entityService.list(MzfEntity.PRODUCT_INVENTORY_VIEW, new HashMap<String, Object>(), null, user);
		for (int i = 0; i < 50; i++) {
			if (giveSingleProductSource.size() < i) break;

			Map<String, Object> map = giveSingleProductSource.get(i);
			giveSingleProductList.add(map);
		}

		return giveSingleProductList;
	}

	//TODO 赠送商品  赠送商品商品组合（多个商品组合）
	private List<Map<String, Object>> listGiveProductGroup(Map<String, Object> context, IUser user) throws BusinessException {
		Random rd = new Random(10);

		int count = rd.nextInt(10);
		count += 1;
		List<Map<String, Object>> giveProductGroupList = new ArrayList<Map<String,Object>>();
		Map<String, Object> where = new HashMap<String, Object>();
		if (rd.nextBoolean()) {
			where.put("ptype", ProductType.pt);
		} else {
			where.put("ptype", ProductType.kGold);
		}
		List<Map<String, Object>> giveProductGroupSource = entityService.list(MzfEntity.PRODUCT_INVENTORY_VIEW, where, null, user);
		Float fixedPrice = MapUtils.getFloat(context, "fixedPrice");

		if(giveProductGroupSource != null && !giveProductGroupSource.isEmpty())
		{
			 String uuid = UUID.randomUUID().toString();
			 Integer maxCount = (int)Math.random()*10+1;
			 float maxPrice = Float.parseFloat(fixedPrice * 0.5+"");
			for (int i = 0; i < giveProductGroupSource.size(); i++) {

				Map<String, Object> map =  giveProductGroupSource.get(i);
				rd = new Random(3);

//				if (rd.nextBoolean()) {
//					maxCount = null;
//					maxPrice = null;
//				}
				map.put("maxCount", maxCount);
				map.put("maxPrice", maxPrice);
				if(i % 10 == 0)
				{   maxCount = maxCount+1;
				    maxPrice = (Math.round(maxPrice*rd.nextDouble()));
					uuid = UUID.randomUUID().toString();
				}
				map.put("groupText", uuid);

				giveProductGroupList.add(map);
			}

		}


		return giveProductGroupList;
	}

	//TODO 赠送物料：赠送单件物料
	private List<Map<String, Object>> listGiveSingleMaterial(Map<String, Object> context, IUser user) throws BusinessException {
		List<Map<String, Object>> giveSingleMaterialList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> giveSingleMaterialSource = entityService.list(MzfEntity.MATERIAL_INVENTORY_VIEW, new HashMap<String, Object>(), null, user);
		for (int i = 0; i < 50; i++) {
			if (giveSingleMaterialSource.size() < i) break;

			Map<String, Object> map = giveSingleMaterialSource.get(i);
			giveSingleMaterialList.add(map);
		}

		return giveSingleMaterialList;
	}

	//TODO 赠送物料：赠送物料组合（多个物料组合）
	private List<Map<String, Object>> listGiveMaterialGroup(Map<String, Object> context, IUser user) throws BusinessException {
		Random rd = new Random(10);
		int count = rd.nextInt();
		count += 1;
		List<Map<String, Object>> giveMaterialGroupList = new ArrayList<Map<String,Object>>();
		Map<String, Object> where = new HashMap<String, Object>();
		if (rd.nextBoolean()) {
			where.put("type", "赠品");
		} else {
			where.put("type", "手册");
		}
		List<Map<String, Object>> giveMaterialGroupSource = entityService.list(MzfEntity.MATERIAL_INVENTORY_VIEW, new HashMap<String, Object>(), null, user);
		Float fixedPrice = MapUtils.getFloat(context, "fixedPrice");
		for (int i = 0; i < count; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			rd = new Random(3);
			Integer maxCount = rd.nextInt() + 1;
			Double maxPrice = fixedPrice * 0.5;
			if (rd.nextBoolean()) {
				maxCount = null;
				maxPrice = null;
			}
			map.put("maxCount", maxCount);
			map.put("maxPrice", maxPrice);
			map.put("materialList", giveMaterialGroupSource);

			giveMaterialGroupList.add(map);
		}

		return giveMaterialGroupList;
	}

	//TODO 赠送代金券
	private List<Map<String, Object>> listGetGiveChit(Map<String, Object> context, IUser user) throws BusinessException {
		Random rd = new Random(3);
		Map<String, Object> where = new HashMap<String, Object>();
		//where.put("orgId", user.getOrgId());
		where.put("status", ChitStatus.normal);
		List<Map<String, Object>> giveChitSrouce = entityService.list(MzfEntity.CHIT_VIEW, where, null, user);
		int count = (int)Math.random()*10;
		count += 1;
		List<Map<String, Object>> giveChitList = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < count; i++) {
			if (giveChitSrouce.size() < i) break;

			giveChitList.add(giveChitSrouce.get(i));
		}

		return giveChitList;
	}

	public Map<String, Object> appRule(int productId, int ruleId, IUser user) throws BusinessException {
		boolean flag = new Random(10).nextBoolean();
		Map<String, Object> dataBox = productInventoryService.getInventory(productId, user.getOrgId());
		//TODO 处理价格折扣
		dataBox.put("saleDiscount", 1);

		//TODO 处理积分
		Float fixedPrice = MapUtils.getFloat(dataBox, "fixedPrice");
		int points = 0;
		if (fixedPrice != null) {
			points = fixedPrice.intValue() / 1000;
		}
		dataBox.put("points", points);

		//赠送商品
		//1. 赠送单件商品
		if (flag) {
			dataBox.put("giveSingleProductList", listGiveSingleProduct(dataBox, user));
		}

		//2. 赠送商品商品组合（多个商品组合）
		if (flag) {
			dataBox.put("giveProductGroupList", listGiveProductGroup(dataBox, user));
		}


		//赠送物料
		//1. 赠送单件物料
		if (flag) {
			dataBox.put("giveSingleMaterialList", listGiveSingleMaterial(dataBox, user));;
		}
		//2. 赠送物料组合（多个物料组合）
		if (flag) {
			dataBox.put("giveMaterialGroupList", listGiveMaterialGroup(dataBox, user));
		}

		//赠送代金券
		if (flag) {
			dataBox.put("giveChitList", listGetGiveChit(dataBox, user));
		}
		return dataBox;
	}
	public List<Map<String,Object>> getGiveProduct(int ruleId,int productId,boolean isGroup,IUser user)throws BusinessException{
		Map<String, Object> dataBox = productInventoryService.getInventory(productId, user.getOrgId());
		if(isGroup){
			return listGiveProductGroup(dataBox, user);
		}else{
			return listGiveSingleProduct(dataBox, user);
		}
	}
	public List<Map<String,Object>> getGiveMaterial(int ruleId,int productId,boolean isGroup,IUser user)throws BusinessException{
		Map<String, Object> dataBox = productInventoryService.getInventory(productId, user.getOrgId());
		if(isGroup){
			return listGiveMaterialGroup(dataBox, user);
		}else{
			return listGiveSingleMaterial(dataBox, user);
		}
	}
	public Map<String,Object> getPointAndDiscount(int ruleId,int productId,IUser user)throws BusinessException{
		 Map<String,Object> map = new HashMap<String,Object>();
		 Map<String, Object> dataBox = productInventoryService.getInventory(productId, user.getOrgId());
		 Float fixedPrice = MapUtils.getFloat(dataBox, "fixedPrice");
			int points = 0;
			if (fixedPrice != null) {
				points = fixedPrice.intValue() / 1000;
			}
		 map.put("points", points);
		 map.put("disCount", 1);
		 return map;
	}

	public List<Map<String,Object>> getGiveChit(int ruleId,int productId,IUser user)throws BusinessException{
		Map<String, Object> dataBox = productInventoryService.getInventory(productId, user.getOrgId());
		return listGetGiveChit(dataBox, user);
	}


	public List<Map<String,Object>> getGiftTree(int productId,int ruleId,IUser user)throws BusinessException{
		Random rd = new Random(10);

		List<Map<String,Object>> treeList = new ArrayList<Map<String,Object>>();
		boolean flag = true;
		 //商品
		boolean isProduct = rd.nextBoolean();
		if(isProduct){
			Map<String,Object> tree = new HashMap<String,Object>();
			tree.put("nodeName", "商品");
			tree.put("nodeCode", "product");
			tree.put("productId", productId);
			tree.put("ruleId", ruleId);
			tree.put("isSingle", rd.nextBoolean());
			tree.put("isGroup", isProduct);
			treeList.add(tree);
		}
		//物料
		rd = new Random(10);
		boolean isMaterial = rd.nextBoolean();
		if(isMaterial){
			Map<String,Object> tree = new HashMap<String,Object>();
			tree.put("nodeName", "物料");
			tree.put("nodeCode", "material");
			tree.put("productId", productId);
			tree.put("ruleId", ruleId);
			tree.put("isSingle", isMaterial);
			tree.put("isGroup", rd.nextBoolean());
			treeList.add(tree);
		}
		rd = new Random(10);
		if(rd.nextBoolean()){
			Map<String,Object> tree = new HashMap<String,Object>();
			tree.put("nodeName", "代金券");
			tree.put("nodeCode", "chit");
			tree.put("productId", productId);
			tree.put("ruleId", ruleId);
			treeList.add(tree);
		}
		return treeList;

	}
}

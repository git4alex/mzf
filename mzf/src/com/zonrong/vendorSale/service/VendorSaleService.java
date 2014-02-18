package com.zonrong.vendorSale.service;

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.inventory.service.RawmaterialInventoryService;
import com.zonrong.inventory.service.SecondGoldInventoryService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Alex
 * Date: 13-12-29
 * Time: 下午6:01
 */
@Service
public class VendorSaleService {
    private Logger logger = Logger.getLogger(VendorSaleService.class);

    @Resource
    private EntityService entityService;
    @Resource
    private ProductInventoryService productInventoryService;
    @Resource
    private RawmaterialInventoryService rawmaterialInventoryService;
    @Resource
    private SecondGoldInventoryService secondGoldInventoryService;

    public void createProductOrder(Map<String,Object> order,List details,IUser user) throws BusinessException {
        createOrder(order,"product",user);

        String num = MapUtils.getString(order,"num");
        String orderId = MapUtils.getString(order,"id");

        for (Object detail : details) {
            String id = ObjectUtils.toString(detail);
            if(StringUtils.isBlank(id)){
                throw new BusinessException("商品ID不能为空");
            }

            //商品出库
            productInventoryService.deliveryByProductId(MzfEnum.BizType.vendorSell,
                    Integer.parseInt(id),"销售单号：["+num+"]", MzfEnum.InventoryStatus.onStorage,user);

            //保存销售单明细
            Map<String,Object> values = entityService.getById(MzfEntity.PRODUCT,id,user);
            values.put("orderId",orderId);
            entityService.create(new EntityCode("vendorSaleProductDetail"), values, user);

            //更新商品状态为“已售”
            values.clear();
            values.put("status", ProductService.ProductStatus.selled);
            entityService.updateById(new EntityCode("product"),id,values,user);
        }
    }

    public void createDiamondOrder(Map<String,Object> order,List details,IUser user) throws BusinessException {
        createOrder(order, "diamond", user);

        String num = MapUtils.getString(order,"num");
        String orderId = MapUtils.getString(order, "id");

        for (Object detail : details) {
            String id = ObjectUtils.toString(detail);
            if(StringUtils.isBlank(id)){
                throw new BusinessException("裸石ID不能为空");
            }

            //裸石出库
            rawmaterialInventoryService.deliveryDiamond(MzfEnum.BizType.vendorSell,
                    Integer.parseInt(id), "销售单号：[" + num + "]", user);

            //保存销售单明细
            Map<String,Object> values = entityService.getById(MzfEntity.RAWMATERIAL,id,user);
            values.put("orderId",orderId);
            entityService.create(new EntityCode("vendorSaleDiamondDetail"), values, user);//在实体定义中进行字段对应

            //更新裸石状态为“已售”
            values.clear();
            values.put("status", MzfEnum.RawmaterialStatus.sold);
            entityService.updateById(MzfEntity.RAWMATERIAL,id,values,user);
        }
    }

    public void createGoldOrder(Map<String,Object> order,List details,IUser user) throws BusinessException {
        createOrder(order, "gold", user);

        String num = MapUtils.getString(order,"num");
        String orderId = MapUtils.getString(order,"id");

        for(Object detail:details){
            Map<String,Object> d = (Map<String, Object>) detail;
            Integer id = MapUtils.getInteger(d,"targetId");
            if(id == null){
                throw new BusinessException("原料Id为空");
            }
            float quantity = MapUtils.getFloatValue(d,"quantity");
            if(quantity<=0){
                throw new BusinessException("销售数量为空");
            }

            //保存销售单明细
            d.put("orderId",orderId);
            entityService.create(new EntityCode("vendorSaleGoldDetail"), d, user);//在实体定义中进行字段对应

            //金料出库
            String remark = "订单号：["+num+"]";
            rawmaterialInventoryService.deliveryById(MzfEnum.BizType.vendorSell, new BigDecimal(quantity), new BigDecimal(quantity), id, remark, user);
        }
    }

    public void createSecondGoldOrder(Map<String,Object> order,List details,IUser user) throws BusinessException {
        createOrder(order, "secondGold", user);

        String num = MapUtils.getString(order,"num");
        String orderId = MapUtils.getString(order,"id");

        for(Object detail:details){
            Map<String,Object> d = (Map<String, Object>) detail;
            Integer id = MapUtils.getInteger(d, "targetId");
            if(id == null){
                throw new BusinessException("原料Id为空");
            }
            Double quantity = MapUtils.getDouble(d,"quantity");
            if(quantity<=0){
                throw new BusinessException("销售数量为空");
            }

            //保存销售单明细
            d.put("orderId",orderId);
            entityService.create(new EntityCode("vendorSaleGoldDetail"), d, user);//在实体定义中进行字段对应

            //旧金出库
            String remark = "订单号：["+num+"]";
            secondGoldInventoryService.delivery(MzfEnum.BizType.vendorSell,id,user.getOrgId(),quantity,remark,user);
        }
    }

    public void createPartsOrder(Map<String,Object> order,List details,IUser user) throws BusinessException {
        createOrder(order, "parts", user);

        String num = MapUtils.getString(order,"num");
        String orderId = MapUtils.getString(order,"id");

        for(Object detail:details){
            Map<String,Object> d = (Map<String, Object>) detail;
            Integer id = MapUtils.getInteger(d, "targetId");
            if(id == null){
                throw new BusinessException("配件Id为空");
            }
            Integer quantity = MapUtils.getInteger(d, "quantity");
            if(quantity<=0){
                throw new BusinessException("销售数量为空");
            }
            Map<String,Object> parts = entityService.getById(MzfEntity.RAWMATERIAL,id,user);
            if(parts == null){
                throw new BusinessException("配件不存在，ID：["+id+"]");
            }
            d.put("targetName",MapUtils.getString(parts,"name"));
            d.put("targetNum",MapUtils.getString(parts,"num"));
            //保存销售单明细
            d.put("orderId",orderId);
            entityService.create(new EntityCode("vendorSalePartsDetail"), d, user);//在实体定义中进行字段对应

            //配件出库
            String remark = "订单号：["+num+"]";
            rawmaterialInventoryService.deliveryParts(MzfEnum.BizType.vendorSell,id,quantity,remark,user);
        }
    }

    public void createGravelOrder(Map<String,Object> order, List details, IUser user) throws BusinessException {
        createOrder(order,"gravel",user);

        String num = MapUtils.getString(order,"num");
        String orderId = MapUtils.getString(order,"id");

        for(Object detail:details){
            Map<String,Object> d = (Map<String, Object>) detail;
            Integer id = MapUtils.getInteger(d, "targetId");
            if(id == null){
                throw new BusinessException("碎石Id为空");
            }
            Integer quantity = MapUtils.getInteger(d, "quantity");
            if(quantity<=0){
                throw new BusinessException("销售数量为空");
            }

            Float weight = MapUtils.getFloat(d, "weight");
            if(weight<=0){
                throw new BusinessException("销售重量为空");
            }

            Map<String,Object> gravel = entityService.getById(MzfEntity.RAWMATERIAL,id,user);
            if(gravel == null){
                throw new BusinessException("碎石不存在，ID：["+id+"]");
            }

            d.put("targetName",MapUtils.getString(gravel,"name"));
            d.put("targetNum",MapUtils.getString(gravel,"num"));
            //保存销售单明细
            d.put("orderId",orderId);
            entityService.create(new EntityCode("vendorSaleGravelDetail"), d, user);//在实体定义中进行字段对应

            //配件出库
            String remark = "订单号：["+num+"]";
            rawmaterialInventoryService.deliveryGravel(MzfEnum.BizType.vendorSell,id,quantity,weight,remark,user);
        }

    }

    public void cancelProductOrder(String id,IUser user) throws BusinessException {
        Map<String,Object> where = new HashMap<String, Object>();
        where.put("orderId",id);
        List<Map<String,Object>> details = entityService.list(new EntityCode("vendorSaleDetail"),where,null,user);
        for(Map<String,Object> detail:details){
            String targetId = MapUtils.getString(detail,"targetId");
        }
    }

    private void createOrder(Map<String,Object> order,String type,IUser user) throws BusinessException {
        checkOrderValue(order);

        String num = "VS"+new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        order.put("type",type);
        order.put("num",num);
        order.put("cdate",null);
        order.put("cuser",null);

        String orderId = entityService.create(new EntityCode("vendorSale"),order,user);
        order.put("id",orderId);
    }

    private void checkOrderValue(Map<String,Object> order) throws BusinessException {
        final String vendorId = MapUtils.getString(order, "vendorId");
        if(StringUtils.isBlank(vendorId)){
            throw new BusinessException("供应商ID不能为空");
        }
        final String vendorOrderNum = MapUtils.getString(order,"vendorOrderNum");
        if(StringUtils.isBlank(vendorOrderNum)){
            throw new BusinessException("供应商单号不能为空");
        }
        final float totalAmount = MapUtils.getFloatValue(order,"totalAmount",0);
        if(totalAmount<=0){
            throw new BusinessException("总金额不能为空");
        }
    }


}

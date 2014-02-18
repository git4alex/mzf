package com.zonrong.purchase.dosing.service;

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.common.utils.MzfEnum.RawmaterialType;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum;
import com.zonrong.common.utils.MzfEnum.VendorOrderStatus;
import com.zonrong.common.utils.MzfEnum.VendorOrderType;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.entity.code.EntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.DosingBom;
import com.zonrong.inventory.service.RawmaterialInventoryService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.purchase.service.PurchaseOrderService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * date: 2010-12-9
 * <p/>
 * version: 1.0
 * commonts: ......
 */
@Service
public class DosingService {
    private Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private MetadataProvider metadataProvider;
    @Resource
    private EntityService entityService;
    @Resource
    private RawmaterialInventoryService rawmaterialInventoryService;
    @Resource
    private ProductService productService;
    @Resource
    private PurchaseOrderService purchaseOrderService;
    @Resource
    private FlowLogService flowLogService;
    @Resource
    private TransactionService transactionService;

    public enum DosingStatus {
        New,        //新建
        outStore,   //已出库
        canceled,    //核销
        unReturn,    //待退库
        returned     //已退库
    }

//    private int findOrderIdByDosingId(int dosingId, IUser user) throws BusinessException {
//        Map<String, Object> where = new HashMap<String, Object>();
//        where.put("dosingId", dosingId);
//        List<Map<String, Object>> list = entityService.list(MzfEntity.BOM_DOSING_VIEW, where, null, user.asSystem());
//        if (CollectionUtils.isEmpty(list)) {
//            throw new BusinessException("");
//        } else if (list.size() > 1) {
//            throw new BusinessException("");
//        }
//
//        Map<String, Object> map = list.getInventory(0);
//        return MapUtils.getInteger(map, "orderId");
//    }

    public int createDosing(int detailId, Integer bomId, Map<String, Object> dosing, IUser user) throws BusinessException {
//		int orderId = purchaseOrderService.findOrderIdByDetailId(detailId, user);
//		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
//		if (VendorOrderStatus.New != status) {
//			throw new BusinessException("当前不允许配料");
//		}

        Filter f = Filter.field("detail_id").eq(detailId).and(Filter.field("bom_id").eq(bomId)).and(Filter.field("status").notIn(new String[]{"unReturn","returned"}));
        List<Map<String, Object>> list = entityService.list(MzfEntity.DOSING, f, null, user.asSystem());
        if (CollectionUtils.isNotEmpty(list)) {
            throw new BusinessException("已经配料");
        }

        //创建一条空记录(没有原料信息)
        int dosingId = createDefaultDosing(detailId, bomId, dosing, user);

        //修改配料
        dosing(dosingId, dosing, user);

        //锁定新配料
        lockRawmaterialInventory(dosingId, user);

        return dosingId;
    }

    public void updateDosing(int dosingId, Map<String, Object> dosing, IUser user) throws BusinessException {
//		int orderId = findOrderIdByDosingId(dosingId, user);
//		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
//		if (VendorOrderStatus.New != status) {
//			throw new BusinessException("当前不允许修改配料");
//		}

        Map<String,Object> cur = entityService.getById(MzfEntity.DOSING,dosingId,user);
        DosingStatus curStatus = DosingStatus.valueOf(MapUtils.getString(cur, "status"));

        if(curStatus == DosingStatus.New){
            //未出库，解锁原配料
            freeRawmaterialInventoryByDosingId(dosingId, user);
        }else if(curStatus == DosingStatus.outStore){
            //配料已出库，更新原配料为待退库
            Map<String,Object> v = new HashMap<String, Object>();
            v.put("status",DosingStatus.unReturn);
            entityService.updateById(MzfEntity.DOSING,dosingId+"",v,user);
            //重新配料
            int detailId = MapUtils.getInteger(dosing,"detailId");
            int bomId = MapUtils.getInteger(dosing,"bomId");
            dosingId = createDefaultDosing(detailId,bomId,dosing,user);
        }

        //修改配料
        dosing(dosingId, dosing, user);
        //锁定新配料
        lockRawmaterialInventory(dosingId, user);
    }

    public int patchDosing(int detailId, Integer bomId, Map<String, Object> dosing, IUser user) throws BusinessException {
        //如果订单状态为提交或者收获中才可配料
        int orderId = purchaseOrderService.findOrderIdByDetailId(detailId, user);
        Map<String, Object> dbOrder = entityService.getById(MzfEntity.VENDOR_ORDER, orderId, user.asSystem());
        String status = MapUtils.getString(dbOrder, "status");
        if (!VendorOrderStatus.submit.toString().equalsIgnoreCase(status) &&
                !VendorOrderStatus.receiving.toString().equalsIgnoreCase(status)) {
            throw new BusinessException("订单状态为\"提交\"或\"收货中\"时, 方可补料");
        }

        //创建一条空记录(没有原料信息)
        int dosingId = createDefaultDosing(detailId, bomId, dosing, user);
        //修改配料
        dosing(dosingId, dosing, user);
        //锁定新配料
        lockRawmaterialInventory(dosingId, user);
        //补料记录
        Map<String, Object> patch = new HashMap<String, Object>();
        patch.put("dosingId", dosingId);
        patch.put("cuserId", null);
        patch.put("cdate", null);
        entityService.create(MzfEntity.PATCH, patch, user);
        return dosingId;
    }

    public void deleteDosing(int dosingId, IUser user) throws BusinessException {
//		int orderId = findOrderIdByDosingId(dosingId, user);
//		VendorOrderStatus status = orderCRUDService.getStatus(orderId, user);
//		if (VendorOrderStatus.New != status) {
//			throw new BusinessException("当前不允许删除配料");
//		}
        Map<String,Object> cur = entityService.getById(MzfEntity.DOSING,dosingId,user);
        DosingStatus curStatus = DosingStatus.valueOf(MapUtils.getString(cur, "status"));
        if(curStatus == DosingStatus.New){ // 原料未出库
            //解锁原料
            freeRawmaterialInventoryByDosingId(dosingId, user);
            //修改配料
            entityService.deleteById(MzfEntity.DOSING, Integer.toString(dosingId), user);
        }else if(curStatus == DosingStatus.outStore){ // 原料已出库
            Map<String,Object> v = new HashMap<String, Object>();
            v.put("status",DosingStatus.unReturn);
            entityService.updateById(MzfEntity.DOSING,dosingId+"",v,user);
        }
    }

    private int createDefaultDosing(int detailId, Integer bomId, Map<String, Object> dosing, IUser user) throws BusinessException {
        Map<String, Object> field = new HashMap<String, Object>(dosing);

        String minorType = MapUtils.getString(field, "minorType");

        field.put("detailId", detailId);
        field.put("bomId", bomId);
        field.put("status", DosingStatus.New);
        field.put("minorType", minorType);
        field.put("dosingQuantity", null);
        field.put("minorQuantity", null);
        String id = entityService.create(MzfEntity.DOSING, field, user);

        return Integer.parseInt(id);
    }

    private void dosing(int dosingId, Map<String, Object> dosing, IUser user) throws BusinessException {
        Integer rawmaterialId = MapUtils.getInteger(dosing, "dosingRawmaterialId");
        if (rawmaterialId == null) {
            throw new BusinessException("数据不完整，缺少：原料ID");
        }

        Map<String, Object> dosing1 = new HashMap<String, Object>();
        try {
            RawmaterialType type = MzfEnum.RawmaterialType.valueOf(MapUtils.getString(dosing, "rawmaterialType"));
            BigDecimal dosingQuantity = new BigDecimal(1);
            if (type == MzfEnum.RawmaterialType.gold || type == MzfEnum.RawmaterialType.parts || type == MzfEnum.RawmaterialType.gravel) {
                String dosingQuantityStr = MapUtils.getString(dosing, "dosingQuantity");
                dosingQuantity = new BigDecimal(dosingQuantityStr);
            }
            dosing1.put("dosingQuantity", dosingQuantity);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            throw new BusinessException("未指定原料类型或者未提交实配量");
        }
        dosing1.put("dosingWeight", MapUtils.getObject(dosing, "dosingWeight"));
        dosing1.put("rawmaterialId", rawmaterialId);

        entityService.updateById(MzfEntity.DOSING, Integer.toString(dosingId), dosing1, user);
    }

    public void deleteDosingByDetailId(int detailId, IUser user) throws BusinessException {
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("detailId", detailId);
        entityService.delete(MzfEntity.DOSING, where, user);
    }

    public void deleteDetailByOrderId(int orderId, IUser user) throws BusinessException {
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("orderId", orderId);
        entityService.delete(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, where, user);
    }

    public void deleteDosingByOrderId(int orderId, IUser user) throws BusinessException {
        EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL);
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("orderId", orderId);
        List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
        if(CollectionUtils.isNotEmpty(list)){
            List<Integer> ids = new ArrayList<Integer>();
            for (Map<String, Object> dbDetail : list) {
                Integer detailId = MapUtils.getInteger(dbDetail, metadata.getPkCode());
                ids.add(detailId);
            }

            where.clear();
            where.put("detailId", ids.toArray(new Integer[ids.size()]));
            entityService.delete(MzfEntity.DOSING, where, user);
        }
    }

    private void freeRawmaterialInventoryByDosingId(int dosingId, IUser user) throws BusinessException {
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("id", dosingId);
        List<Map<String, Object>> dbDosingList = entityService.list(MzfEntity.DOSING_VIEW, where, null, user.asSystem());
        if (CollectionUtils.isEmpty(dbDosingList)) {
            throw new BusinessException("未找到配料信息");
        } else if (dbDosingList.size() > 1) {
            throw new BusinessException("找到多条配料信息");
        }

        freeRawmaterialInventory(dbDosingList, user);
    }

    public void freeRawmaterialInventoryByDetailId(int detailId, IUser user) throws BusinessException {
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("detailId", detailId);
        List<Map<String, Object>> dbDosingList = entityService.list(MzfEntity.DOSING_VIEW, where, null, user.asSystem());

        freeRawmaterialInventory(dbDosingList, user);
    }

    public void freeRawmaterialInventory(int orderId, IUser user) throws BusinessException {
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("orderId", orderId);
        List<Map<String, Object>> dbDosingList = entityService.list(MzfEntity.DOSING_VIEW, where, null, user.asSystem());
        freeRawmaterialInventory(dbDosingList, user);
    }

    private void freeRawmaterialInventory(List<Map<String, Object>> dosingList, IUser user) throws BusinessException {
        List<Integer> diamondList = new ArrayList<Integer>();
        Map<Integer, BigDecimal> dosingMap = new HashMap<Integer, BigDecimal>();
        for (Map<String, Object> dbDosing : dosingList) {
            Integer rawmaterialId = MapUtils.getInteger(dbDosing, "rawmaterialId");
            if (rawmaterialId == null) {
                continue;
            }
            RawmaterialType type = MzfEnum.RawmaterialType.valueOf(MapUtils.getString(dbDosing, "rawmaterialType"));
            String dosingQuantity = MapUtils.getString(dbDosing, "dosingQuantity");
            if (type == MzfEnum.RawmaterialType.nakedDiamond) {
                diamondList.add(rawmaterialId);
            } else if (type == MzfEnum.RawmaterialType.gold || type == MzfEnum.RawmaterialType.parts || type == MzfEnum.RawmaterialType.gravel) {
                if (StringUtils.isBlank(dosingQuantity)) {
                    throw new BusinessException("未指找到" + type.getName() + "实配量");
                }
                BigDecimal quantity = new BigDecimal(dosingQuantity);
                dosingMap.put(rawmaterialId, quantity);
            }
        }

        //解锁原料
        String remark = "委外加工订单删除配料，解锁原料";
        rawmaterialInventoryService.freeDiamond(diamondList.toArray(new Integer[diamondList.size()]), remark, user);
        rawmaterialInventoryService.free(dosingMap, user);
    }

    private void lockRawmaterialInventory(int dosingId, IUser user) throws BusinessException {
        Map<String, Object> dbDosing = entityService.getById(MzfEntity.DOSING_VIEW, Integer.toString(dosingId), user.asSystem());
        Integer rawmaterialId = MapUtils.getInteger(dbDosing, "rawmaterialId");
        if (rawmaterialId == null) {
            throw new BusinessException("未指定原料");
        }
        RawmaterialType type = MzfEnum.RawmaterialType.valueOf(MapUtils.getString(dbDosing, "rawmaterialType"));
        String dosingQuantity = MapUtils.getString(dbDosing, "dosingQuantity");

        List<Integer> diamondList = new ArrayList<Integer>();
        Map<Integer, BigDecimal> dosingMap = new HashMap<Integer, BigDecimal>();
        if (type == MzfEnum.RawmaterialType.nakedDiamond) {
            diamondList.add(rawmaterialId);
        } else if (type == MzfEnum.RawmaterialType.gold || type == MzfEnum.RawmaterialType.parts || type == MzfEnum.RawmaterialType.gravel) {
            if (StringUtils.isBlank(dosingQuantity)) {
                throw new BusinessException("未找到" + type.getName() + "实配量");
            }
            BigDecimal quantity = new BigDecimal(dosingQuantity);
            dosingMap.put(rawmaterialId, quantity);
        }

        //锁定原料
        String remark = "委外加工订单锁定原料";
        rawmaterialInventoryService.lockDiamond(diamondList.toArray(new Integer[diamondList.size()]), remark, user);
        rawmaterialInventoryService.lock(dosingMap, user);
    }

    public void validDosing(Integer[] dosingId, IUser user) throws BusinessException {
        if (dosingId == null) {
            throw new BusinessException("未指定要核销的原料");
        }
        EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.DOSING);
        Map<String, Object> where = new HashMap<String, Object>();
        where.put(metadata.getPkCode(), dosingId);
        where.put("status", DosingStatus.New);
        List<Map<String, Object>> list = entityService.list(metadata, where, null, user.asSystem());
        if (list.size() != dosingId.length) {
            throw new BusinessException("部分原料不须核销");
        }

        Map<String, Object> dosing = list.get(0);
        Integer detailId = MapUtils.getInteger(dosing, "detailId");
        Map<String, Object> detail = entityService.getById(MzfEntity.VENDOR_ORDER_PRODUCT_ORDER_DETAIL, detailId, user.asSystem());
        Integer orderId = MapUtils.getInteger(detail, "orderId");
        Map<String, Object> order = entityService.getById(MzfEntity.VENDOR_ORDER_VIEW, orderId.toString(), user);
        VendorOrderType type = VendorOrderType.valueOf(MapUtils.getString(order, "type"));
        if (type == VendorOrderType.OEM) {
            Integer productId = MapUtils.getInteger(detail, "productId");
            if (!productService.isQc(productId)) {
                throw new BusinessException("该商品尚未QC， 不能核销原料");
            }
            if (!productService.isCid(productId)) {
                throw new BusinessException("该商品尚未做证书， 不能核销原料");
            }
        }


        Map<String, Object> field = new HashMap<String, Object>();
        field.put("status", DosingStatus.canceled);
        where.clear();
        where.put(metadata.getPkCode(), dosingId);
        entityService.update(metadata, field, where, user);
    }

    public void lockMaterial(int mid, float quantity, IUser user) throws BusinessException {
        Map<String, Object> m = rawmaterialInventoryService.getInventory(mid, user.getOrgId(), user);
        RawmaterialType type = MzfEnum.RawmaterialType.valueOf(MapUtils.getString(m, "type"));

        if (type == MzfEnum.RawmaterialType.nakedDiamond) {
            rawmaterialInventoryService.lockDiamond(new Integer[]{mid}, "配料锁定", user);
        } else {
            Map<Integer, BigDecimal> tmp = new HashMap<Integer, BigDecimal>();
            tmp.put(mid, new BigDecimal(quantity));
            rawmaterialInventoryService.lock(tmp, user);
        }
    }

    public void freeMaterial(int mid, float quantity, IUser user) throws BusinessException {
        Map<String, Object> m = rawmaterialInventoryService.getInventory(mid, user.getOrgId(), user);
        RawmaterialType type = MzfEnum.RawmaterialType.valueOf(MapUtils.getString(m, "type"));

        if (type == MzfEnum.RawmaterialType.nakedDiamond) {
            rawmaterialInventoryService.freeDiamond(new Integer[]{mid}, "配料解锁", user);
        } else {
            Map<Integer, BigDecimal> tmp = new HashMap<Integer, BigDecimal>();
            tmp.put(mid, new BigDecimal(quantity));
            rawmaterialInventoryService.free(tmp, user);
        }
    }

    /**
     * 取消委外加工订单配料
     *
     * @param oid   订单ID
     * @param user  当前用户
     * @throws BusinessException
     */
    public void cancelOEMDosing(int oid, IUser user) throws BusinessException {
        //查找所有状态为New的配料记录
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("orderId", oid);
        where.put("status", DosingStatus.New);
        List<Map<String, Object>> dbDosingList = entityService.list(MzfEntity.DOSING_VIEW, where, null, user.asSystem());
        if (!CollectionUtils.isEmpty(dbDosingList)) {
            //取消冻结
            freeRawmaterialInventory(dbDosingList, user);

            //删除配料记录
            List<Integer> ids = new ArrayList<Integer>();
            for (Map<String, Object> detail : dbDosingList) {
                Integer detailId = MapUtils.getInteger(detail, "id");
                ids.add(detailId);
            }
            where.clear();
            where.put("id", ids.toArray(new Integer[ids.size()]));
            entityService.delete(MzfEntity.DOSING, where, user);
        }
    }

    /**
     * 委外加工订单配料出库
     *
     * @param oid 订单ID
     * @param user 当前用户
     * @throws BusinessException
     */
    public void confirmOEMDosing(int oid, IUser user) throws BusinessException {
        Map<String,Object> oemMaterial = new HashMap<String, Object>();
        oemMaterial.put("orderId",oid);
        oemMaterial.put("cuser",user.getId());
        oemMaterial.put("cdate",new Date());
        String oemMaterialId = entityService.create(new EntityCode("oemMaterial"),oemMaterial,user);

        //查找所有状态为New（待出库）的配料记录
        Map<String, Object> where = new HashMap<String, Object>();
        where.put("orderId", oid);
        where.put("status", DosingStatus.New);
        List<Map<String, Object>> dbDosingList = entityService.list(MzfEntity.DOSING_VIEW, where, null, user);
        if (!CollectionUtils.isEmpty(dbDosingList)) {

            //原料出库
            deliveryOEMDosing(dbDosingList, getOEMOrderNum(oid,user), user);

            //更新配料记录为已出库（outStore）
            Map<String, Object> v = new HashMap<String, Object>();
            v.put("status",DosingStatus.outStore);
            v.put("oemMaterialId",oemMaterialId);
            for (Map<String, Object> detail : dbDosingList) {
                String dosingId = MapUtils.getString(detail,"id");
                entityService.updateById(MzfEntity.DOSING,dosingId,v,user.asSystem());
            }
        }

        int transId = transactionService.findTransId(MzfEntity.VENDOR_ORDER, oid+"", user);
        flowLogService.createLog(transId, MzfEntity.VENDOR_ORDER, oid+"", "配料出库", null, null, null, user);
    }

    private void deliveryOEMDosing(List<Map<String, Object>> dosingList,String orderNum,IUser user) throws BusinessException {
        Map<Integer, DosingBom> rawmaterialQuantityMap = new HashMap<Integer, DosingBom>();
        String remark = "委外加工订单["+orderNum+"]配料出库";

        for (Map<String, Object> dosing : dosingList) {
            Integer rawmaterialId = MapUtils.getInteger(dosing, "rawmaterialId");

            if (rawmaterialId != null) {
                RawmaterialType rawmaterialType = MzfEnum.RawmaterialType.valueOf(MapUtils.getString(dosing, "rawmaterialType"));
                if (rawmaterialType != MzfEnum.RawmaterialType.nakedDiamond) {
                    DosingBom dosingBom = new DosingBom();
                    dosingBom.setRawmaterialId(rawmaterialId);
                    dosingBom.setQuantity(new BigDecimal(MapUtils.getString(dosing, "dosingQuantity")));
                    dosingBom.setWeight(new BigDecimal(MapUtils.getString(dosing, "dosingWeight","0")));
                    rawmaterialQuantityMap.put(rawmaterialId, dosingBom);
                } else {
                    rawmaterialInventoryService.deliveryDiamond(MzfEnum.BizType.OEM, rawmaterialId, remark, user);
                    int transId = transactionService.findTransId(MzfEntity.RAWMATERIAL, rawmaterialId+"", user);
                    flowLogService.createLog(transId, MzfEntity.RAWMATERIAL, rawmaterialId+"", "配料出库", MzfEnum.TargetType.rawmaterial, rawmaterialId, "委外订单号："+orderNum, user);
                }
            }
        }

        if (MapUtils.isNotEmpty(rawmaterialQuantityMap)) {
            rawmaterialInventoryService.deliveryOnOem(MzfEnum.BizType.OEM, rawmaterialQuantityMap, user.getOrgId(), remark, user);
        }
    }

    private String getOEMOrderNum(int orderId,IUser user){
        String ret = "";
        try {
            Map<String,Object> order = entityService.getById(MzfEntity.VENDOR_ORDER,orderId, user);
            ret = MapUtils.getString(order,"num","");
        } catch (BusinessException e) {
            e.printStackTrace();
        }

        return ret;
    }
}



package com.zonrong.vendorSale.controller;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.vendorSale.service.VendorSaleService;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * User: Alex
 * Date: 13-12-29
 * Time: 下午6:00
 */
@Controller
@RequestMapping(value = "/code/vendorSale")
public class VendorSaleController {
    private Logger logger = Logger.getLogger(VendorSaleController.class);

    @Resource
    private VendorSaleService vendorSaleService;

    @RequestMapping(value = "/product",method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> vendorSaleProduct(@RequestBody final Map<String, Object> params,HttpServletRequest request) throws BusinessException {
        final List details = getOrderDetails(params);

        OperateTemplete opt = new HttpTemplete(request) {
            @Override
            protected void doSomething() throws BusinessException {
                vendorSaleService.createProductOrder(params,details,this.getUser());
            }
        };

        return opt.operate();
    }

    @RequestMapping(value = "/diamond",method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> vendorSaleDiamond(@RequestBody final Map<String, Object> params,HttpServletRequest request) throws BusinessException {
        final List details = getOrderDetails(params);
        OperateTemplete opt = new HttpTemplete(request) {
            @Override
            protected void doSomething() throws BusinessException {
                vendorSaleService.createDiamondOrder(params,details,this.getUser());
            }
        };

        return opt.operate();
    }

    @RequestMapping(value = "/gold",method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> vendorSaleGold(@RequestBody final Map<String,Object> params,HttpServletRequest request) throws BusinessException {
        final List details = getOrderDetails(params);

        OperateTemplete opt = new HttpTemplete(request) {
            @Override
            protected void doSomething() throws BusinessException {
                vendorSaleService.createGoldOrder(params, details, this.getUser());
            }
        };

        return opt.operate();
    }

    @RequestMapping(value = "/secondGold",method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> vendorSaleSecondGold(@RequestBody final Map<String,Object> params,HttpServletRequest request) throws BusinessException {
        final List details = getOrderDetails(params);

        OperateTemplete opt = new HttpTemplete(request) {
            @Override
            protected void doSomething() throws BusinessException {
                vendorSaleService.createSecondGoldOrder(params, details, this.getUser());
            }
        };

        return opt.operate();
    }

    @RequestMapping(value = "/parts",method = RequestMethod.POST)
         @ResponseBody
         public Map<String,Object> vendorSaleParts(@RequestBody final Map<String,Object> params,HttpServletRequest request) throws BusinessException {
        final List details = getOrderDetails(params);

        OperateTemplete opt = new HttpTemplete(request) {
            @Override
            protected void doSomething() throws BusinessException {
                vendorSaleService.createPartsOrder(params, details, this.getUser());
            }
        };

        return opt.operate();
    }

    @RequestMapping(value = "/gravel",method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> vendorSaleGravel(@RequestBody final Map<String,Object> params,HttpServletRequest request) throws BusinessException {
        final List details = getOrderDetails(params);

        OperateTemplete opt = new HttpTemplete(request) {
            @Override
            protected void doSomething() throws BusinessException {
                vendorSaleService.createGravelOrder(params, details, this.getUser());
            }
        };

        return opt.operate();
    }

    private List getOrderDetails(Map<String,Object> params) throws BusinessException {
        final List details = (List) MapUtils.getObject(params,"details");
        if(details == null || details.size()==0){
            throw new BusinessException("订单明细不能为空");
        }

        params.remove("details");

        return details;
    }
}

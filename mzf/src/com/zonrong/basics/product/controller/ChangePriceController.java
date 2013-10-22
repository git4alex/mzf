package com.zonrong.basics.product.controller;

import com.zonrong.basics.product.service.ChangePriceService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * User: Alex
 * Date: 13-8-8
 * Time: 下午10:07
 */

@Controller
@RequestMapping(value = "/code/changePrice")
public class ChangePriceController {
    private Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private ChangePriceService changePriceService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Map changePrice(@RequestBody final Map<String,Object> params, HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                changePriceService.updatePrice(params, this.getUser());
            }
        };
        return templete.operate();
    }
}

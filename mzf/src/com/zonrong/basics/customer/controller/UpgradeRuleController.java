package com.zonrong.basics.customer.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.customer.service.UpgradeRuleService;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;

/**
 * 2011-08-26
 * @author Administrator
 * 会员升级规则
 */
@Controller
@RequestMapping("/code/upgraderule")
public class UpgradeRuleController {

	@Resource
	private UpgradeRuleService upgradeRuleService;
	
	
	@RequestMapping(value="/getGrade",method=RequestMethod.GET)
	@ResponseBody
	public Map getGrade(@RequestBody final Integer points,HttpServletRequest request){
		 OperateTemplete templete = new HttpTemplete(request) {
				protected void doSomething() throws BusinessException {				
				 String grade =	upgradeRuleService.getGradeByCode(points);
				 this.put("grade", grade);
				}			
			};

			return templete.operate();		
	}
}

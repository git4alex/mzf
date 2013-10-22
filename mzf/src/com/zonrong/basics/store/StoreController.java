package com.zonrong.basics.store;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;

/**
 * 门店管理
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value="/code/store")
public class StoreController {
	
	@Resource
	private StoreService storeService;
	
	@RequestMapping(value="/create",method=RequestMethod.POST)
	@ResponseBody
	public Map createStore(@RequestBody final Map<String,Object> store,HttpServletRequest request){
		 OperateTemplete templete = new HttpTemplete(request) {
				protected void doSomething() throws BusinessException {		
					storeService.createStore(store, this.getUser());
				}					
			 };
			return templete.operate();
	}

}

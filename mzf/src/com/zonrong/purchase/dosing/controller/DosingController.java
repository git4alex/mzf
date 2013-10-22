package com.zonrong.purchase.dosing.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.zonrong.core.util.SessionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.purchase.dosing.service.DosingService;

/**
 * date: 2010-12-9
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/dosing")
public class DosingController {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private DosingService dosingService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map createDosing(@RequestBody final Map<String, Object> dosing, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Integer detailId = MapUtils.getInteger(dosing, "detailId");
				Integer bomId = MapUtils.getInteger(dosing, "bomId");
				dosing.remove("id");

				int id = dosingService.createDosing(detailId, bomId, dosing, this.getUser());
				this.put("id", id);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/{dosingId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map updateDosing(@PathVariable final int dosingId, @RequestBody final Map<String, Object> dosing, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				dosingService.updateDosing(dosingId, dosing, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/{dosingId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map deleteDosing(@PathVariable final int dosingId, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				dosingService.deleteDosing(dosingId, this.getUser());
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/patch", method = RequestMethod.POST)
	@ResponseBody
	public Map patchDosing(@RequestBody final Map<String, Object> dosing, HttpServletRequest request) throws BusinessException {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Integer detailId = MapUtils.getInteger(dosing, "detailId");
				Integer bomId = MapUtils.getInteger(dosing, "id");
				dosing.remove("id");

				int id = dosingService.patchDosing(detailId, bomId, dosing, this.getUser());
				this.put("id", id);
			}
		};
		return templete.operate();
	}

	@RequestMapping(value = "/cancelDosing", method = RequestMethod.POST)
	@ResponseBody
	public Map receiveProductFormOEM(@RequestBody final Integer[] dosingId, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				dosingService.validDosing(dosingId, this.getUser());
			}
		};
		return templete.operate();
	}

    @RequestMapping(value = "/lockMaterial/{mid}/{quantity}", method = RequestMethod.PUT)
    @ResponseBody
    public Map lockMaterial(@PathVariable final int mid, @PathVariable final float quantity,HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                dosingService.lockMaterial(mid,quantity,this.getUser());
            }
        };
        return templete.operate();
    }

    @RequestMapping(value = "/freeMaterial/{mid}/{quantity}", method = RequestMethod.PUT)
    @ResponseBody
    public Map freeMaterial(@PathVariable final int mid, @PathVariable final float quantity,HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                dosingService.freeMaterial(mid,quantity,this.getUser());
            }
        };
        return templete.operate();
    }

    //取消配料出库
    @RequestMapping(value = "/cancelOEMDosing/{oid}", method = RequestMethod.PUT)
    @ResponseBody
    public Map cancelOEMDosing(@PathVariable final int oid,final HttpServletRequest request){
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                dosingService.cancelOEMDosing(oid, SessionUtils.getUser(request));
            }
        };
        return templete.operate();
    }

    //确认配料出库
    @RequestMapping(value = "/confirmOEMDosing/{oid}", method = RequestMethod.PUT)
    @ResponseBody
    public Map confirmOEMDosing(@PathVariable final int oid,final HttpServletRequest request){
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                dosingService.confirmOEMDosing(oid, SessionUtils.getUser(request));
            }
        };
        return templete.operate();
    }
}



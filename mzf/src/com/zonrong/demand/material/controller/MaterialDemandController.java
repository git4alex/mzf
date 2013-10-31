package com.zonrong.demand.material.controller;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.demand.material.service.MaterialDemandService;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * date: 2011-8-16
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping("/code/materialDemand")
public class MaterialDemandController {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MaterialDemandService materialDemandService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> create(@RequestBody final Map<String, Object> demand, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				materialDemandService.create(demand, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/{demandId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> update(@PathVariable final int demandId, @RequestBody final Map<String, Object> demand, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				materialDemandService.update(demandId, demand, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/{demandId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map<String, Object> delete(@PathVariable final int demandId, @RequestBody final Map<String, Object> demand, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				materialDemandService.delete(demandId, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/submit", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> submit(@RequestBody final Integer[] demandIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				materialDemandService.submit(demandIds, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/reject/{demandId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> reject(@PathVariable final int demandId, @RequestBody final Map<String, Object> allocate, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				materialDemandService.reject(demandId, allocate, this.getUser());
			}
		};

		return templete.operate();
	}

    @RequestMapping(value = "/bManagerProcess", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, Object> bManagerProcess(@RequestBody final Map<String, Object> process, HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                boolean isPass = MapUtils.getBooleanValue(process, "checkedResult");
                List ids = (List) MapUtils.getObject(process, "ids");
                String remark = MapUtils.getString(process,"remark");
                if(isPass){
                    materialDemandService.bManagerProcess(ids, remark, this.getUser());
                }else{
                    materialDemandService.bReject(ids, remark, this.getUser());
                }
            }
        };

        return templete.operate();
    }

	@RequestMapping(value = "/managerProcess/{demandId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> managerProcess(@PathVariable final int demandId, @RequestBody final Map<String, Object> process, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				boolean isPass = MapUtils.getBooleanValue(process, "checkedResult");
				if(isPass){
					materialDemandService.managerProcess(demandId, process, this.getUser());
				}else{
					materialDemandService.reject(demandId, process, this.getUser());
				}

			}
		};

		return templete.operate();
	}

    @RequestMapping(value = "/bMgrProcess", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, Object> bMgrProcess(@RequestBody final Map<String, Object> process, HttpServletRequest request) {
        OperateTemplete templete = new HttpTemplete(request) {
            protected void doSomething() throws BusinessException {
                boolean isPass = MapUtils.getBooleanValue(process, "checkedResult");
                final List ids = (List) MapUtils.getObject(process,"ids",new ArrayList());
                final String remark = MapUtils.getString(process, "remark");
                if(isPass){
                    materialDemandService.bMgrProcess(ids, remark, this.getUser());
                }else{
                    materialDemandService.bReject(ids, remark, this.getUser());
                }

            }
        };

        return templete.operate();
    }

	@RequestMapping(value = "/mgrProcess/{demandId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> mgrProcess(@PathVariable final int demandId, @RequestBody final Map<String, Object> process, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				boolean isPass = MapUtils.getBooleanValue(process, "checkedResult");
				if(isPass){
					 materialDemandService.mgrProcess(demandId, process, this.getUser());
				}else{
					materialDemandService.reject(demandId, process, this.getUser());
				}

			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/allcoate/{demandId}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> allcoate(@PathVariable final int demandId, @RequestBody final Map<String, Object> allocate, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				materialDemandService.allcoate(demandId, allocate, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/send", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> send(@RequestBody final Integer[] demandIds, HttpServletRequest request) {
		final String remark = request.getParameter("remark");
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				materialDemandService.send(demandIds, remark, this.getUser());
			}
		};

		return templete.operate();
	}

	@RequestMapping(value = "/printData", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> printData(HttpServletRequest request) {
		final String remark = request.getParameter("remark");
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Map<String, Object>> dataList = materialDemandService.getPrintData(this.getUser());
				this.put("dataList", dataList);
			}
		};

		return templete.operate();
	}

}



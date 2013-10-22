package com.zonrong.demand.product.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.DemandStatus;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.Page;
import com.zonrong.core.dao.filter.Filter;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.core.util.SessionUtils;
import com.zonrong.demand.product.service.ProductDemandService;
import com.zonrong.demand.product.service.ProductDemandService.Actor;
import com.zonrong.entity.code.IEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.MetadataConst;
import com.zonrong.util.TpltUtils;

/**
 * date: 2010-10-12
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/demand")
public class ProductDemandController {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private ProductDemandService productDemandService;
	
	@Resource
	private EntityService entityService;	
	
	@RequestMapping(value = "/waitProcess", method = RequestMethod.GET)
	@ResponseBody	
	public Map<String, Object> list(@RequestParam Map<String, Object> parameter, HttpServletRequest request) {
		try {
			IEntityCode code = MzfEntity.DEMAND_PROCESS_VIEW;
			OrderBy orderBy = TpltUtils.refactorOrderByParams(parameter);
			List<Map<String, Object>> list = TpltUtils.refactorQueryParams(parameter);
			Map<String, Object> filter = new HashMap<String, Object>();
			filter.put(EntityService.FIELD_CODE_KEY, "status");
			filter.put(EntityService.OPERATOR_KEY, Filter.NOT_EQ1);
			filter.put(EntityService.VALUE_KEY, DemandStatus.New);
			list.add(filter);
			filter = new HashMap<String, Object>();
			filter.put(EntityService.FIELD_CODE_KEY, "status");
			filter.put(EntityService.OPERATOR_KEY, Filter.NOT_EQ1);
			filter.put(EntityService.VALUE_KEY, DemandStatus.waitMgrProcess);
			list.add(filter);
			if (MapUtils.getInteger(parameter, "start") != null) {
				Page page = new Page(parameter);
				if (page != null) {		
					return entityService.page(code, list, page.getOffSet(), page.getPageSize(), orderBy, SessionUtils.getUser(request));
				}	
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(MetadataConst.ITEMS_ROOT, entityService.list(code, list, orderBy, SessionUtils.getUser(request)));
			return map;				
		}  catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		return new HashMap();
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> createDemand(@RequestBody final Map<String, Object> demand, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				productDemandService.createDemand(demand, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/byStyle", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> createDemandByStyleIds(@RequestParam final Integer[] styleIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {				
				productDemandService.createDemandByStyle(styleIds, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/submitToManager")
	@ResponseBody
	public Map<String, Object> submitToManager(@RequestParam final Integer[] demandIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {												
				productDemandService.submitToManager(demandIds, null, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/toFinance", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> toFinance(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer[]> list = (List) MapUtils.getObject(map, "demandIds");
				Integer[] demandIds = list.toArray(new Integer[]{});
				String remark = MapUtils.getString(map, "remark");
				productDemandService.pass(demandIds, remark, Actor.saleMgr, new DemandStatus[]{DemandStatus.waitMgrProcess}, DemandStatus.waitFinanceProcess, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/toHQByFinance", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> toHQByFinance(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer[]> list = (List) MapUtils.getObject(map, "demandIds");
				Integer[] demandIds = list.toArray(new Integer[]{});
				String remark = MapUtils.getString(map, "remark");
				productDemandService.pass(demandIds, remark, Actor.FinanceMgr, new DemandStatus[]{DemandStatus.waitFinanceProcess}, DemandStatus.waitProcess, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/toHQByFranchisee", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> toHQByFranchisee(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer[]> list = (List) MapUtils.getObject(map, "demandIds");
				Integer[] demandIds = list.toArray(new Integer[]{});
				String remark = MapUtils.getString(map, "remark");
				productDemandService.pass(demandIds, remark, Actor.franchisee, new DemandStatus[]{DemandStatus.waitFranchiseeProcess}, DemandStatus.waitProcess, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/rejectByFranchisee", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> rejectByMgr1(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer[]> list = (List) MapUtils.getObject(map, "demandIds");
				Integer[] demandIds = list.toArray(new Integer[]{});
				String remark = MapUtils.getString(map, "remark");
				productDemandService.reject(demandIds, remark, Actor.franchisee, DemandStatus.waitFranchiseeProcess, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/rejectByMgr", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> rejectByMgr(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer[]> list = (List) MapUtils.getObject(map, "demandIds");
				Integer[] demandIds = list.toArray(new Integer[]{});
				String remark = MapUtils.getString(map, "remark");
				productDemandService.reject(demandIds, remark, Actor.saleMgr, DemandStatus.waitMgrProcess, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	
	@RequestMapping(value = "/rejectByFinance", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> rejectByFinance(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Integer[]> list = (List) MapUtils.getObject(map, "demandIds");
				Integer[] demandIds = list.toArray(new Integer[]{});
				String remark = MapUtils.getString(map, "remark");
				productDemandService.reject(demandIds, remark, Actor.FinanceMgr, DemandStatus.waitFinanceProcess, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map<String, Object> update(@PathVariable final int id, @RequestBody final Map<String, Object> order, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {	
				order.remove("status");
				productDemandService.updateDemand(id, order, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public Map<String, Object> delete(@PathVariable final int id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandService.deleteDemand(id, this.getUser());
			}			
		};

		return templete.operate();			
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@ResponseBody
	public Map<String, Object> delete(@RequestBody final Integer[] ids, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productDemandService.deleteDemand(ids, this.getUser());
			}			
		};

		return templete.operate();			
	}
}



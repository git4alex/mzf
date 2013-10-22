package com.zonrong.basics.product.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.zonrong.basics.product.service.ProductService;
import com.zonrong.basics.product.service.ProductService.ProductStatus;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.templete.HttpTemplete;
import com.zonrong.core.templete.OperateTemplete;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2010-8-23
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
@RequestMapping(value = "/code/product")
public class ProductController {	
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MetadataProvider metadataProvider;	 
	@Resource
	private ProductService productService;
	
	@RequestMapping(value = "/findByProductNum/{productNum}", method = RequestMethod.GET)
	@ResponseBody
	public Map findByProductNum(@PathVariable final String productNum, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> product = productService.findByProductNum(productNum, this.getUser());
				this.put("product", product);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/findAllByProductNum/{productNum}", method = RequestMethod.GET)
	@ResponseBody
	public Map findAllByProductNum(@PathVariable final String productNum, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				Map<String, Object> product = productService.findAllByProductNum(productNum, this.getUser());
				this.put("product", product);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map update(@PathVariable final int id, @RequestBody final Map<String, Object> product, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				List<Map<String, Object>> diamondList = (List) MapUtils.getObject(product, "diamondList");
				List<Map<String, Object>> certificateList = (List) MapUtils.getObject(product, "certificateList");
				product.remove("diamondList");
				product.remove("certificateList");
				
				productService.updateProductById(id, product, diamondList, certificateList, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/generateProductNum/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Map generateProductNum(@PathVariable final int id, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.PRODUCT);;
				String num = productService.generateProductNum(metadata, id, null);
				this.put("num", num);
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/recreateProductNum/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map recreateProductNum(@PathVariable final int id, @RequestBody final Map<String, Object> product, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				String num = MapUtils.getString(product, "num");
				if (StringUtils.isBlank(num)) {
					throw new BusinessException("未提交商品条码");
				}
				
				productService.recreateProductNum(id, num, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/createProductNum", method = RequestMethod.PUT)
	@ResponseBody
	public Map createProductNum(@RequestBody final Integer[] productIds, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productService.createProductNum(productIds, this.getUser());
			}			
		};
		return templete.operate();			
	}
	@RequestMapping(value = "/supplyProduct", method = RequestMethod.PUT)
	@ResponseBody
	public Map supplyProduct(@RequestBody final Map<String, Object> map, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				//Map<String, Object> product = MapUtils.getMap(map, "product");
				map.put("source", "supply");
				map.put("sourceId", this.getUser().getName());
				List<Map<String, Object>> diamondList = (List)MapUtils.getObject(map, "diamondList");
				List<Map<String, Object>> certificateList = (List)MapUtils.getObject(map, "certificateList");
				productService.supplyProduct(map, diamondList, certificateList, ProductStatus.selled, null, this.getUser());
			}			
		};
		return templete.operate();			
	}
	
	@RequestMapping(value = "/translateToRawmaterial/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Map translateToRawmaterial(@PathVariable final int id, @RequestBody final Map<String, Object> rawmaterial, HttpServletRequest request) {
		OperateTemplete templete = new HttpTemplete(request) {
			protected void doSomething() throws BusinessException {
				productService.translateToRawmaterial(id, rawmaterial, this.getUser());
			}			
		};
		return templete.operate();			
	}
	@RequestMapping(value="/exportProduct",method = RequestMethod.POST)
	public void exportProductData(@RequestBody final Map<String,Object> param,HttpServletResponse response,HttpServletRequest request){
		String[] filedCodes = MapUtils.getString(param, "fieldCodes").split(",");
		List<Integer> productIdsList = (List<Integer>) MapUtils.getObject(param, "productIds");
		Integer[] productIds = productIdsList.toArray(new Integer[]{}); 
		try {
			String filePath = request.getContextPath();
			String url = "/uploadTmp/productData.xls";
			File file = new File(this.getClass().getClassLoader().getResource("").toURI().getPath());
			file = file.getParentFile().getParentFile();
			file = new File(file.getPath() + url);
//			response.setContentType("application/vnd.ms-excel;charset=uft-8"); 
//			response.setHeader("Content-Disposition","attachment; filename=" + fileName); 
			OutputStream outStream = new FileOutputStream(file);
			HSSFWorkbook book = productService.exportExcel(productIds, filedCodes, MzfEntity.PRODUCT, outStream);
			outStream.write(book.getBytes());
			outStream.flush();
			request.getRequestDispatcher(url).forward(request, response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}
}



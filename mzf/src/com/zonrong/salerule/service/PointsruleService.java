package com.zonrong.salerule.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.service.ProductInventoryService;
import com.zonrong.salerule.service.expression.ExpressionProcessor;
import com.zonrong.salerule.service.expression.RuleEvaluationContext;
import com.zonrong.salerule.service.mapper.ProductMapper;

/**
 * date: 2011-10-19
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class PointsruleService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private EntityService entityService;
	@Resource
	public ProductInventoryService productInventoryService;

	private List<Map<String, Object>> rules = new ArrayList<Map<String,Object>>();

	@PostConstruct
	public void load() {
		try {
			rules = entityService.list(MzfEntity.POINTS_RULE, new ArrayList(), null, User.getSystemUser());
		} catch (BusinessException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}

	public int getPoints(int productId, BigDecimal pointsPrice, BigDecimal otherCharges, BigDecimal totalDiscount, int orgId) throws BusinessException {
		Map<String, Object> product = productInventoryService.getProductInventory(productId, orgId);
		BigDecimal price = new BigDecimal(MapUtils.getString(product, "fixedPrice"));
		if (pointsPrice != null) {
			price = pointsPrice;
		}
		if (otherCharges != null) {
			price = price.add(otherCharges);
		}
		if (totalDiscount != null) {
			price = price.subtract(totalDiscount);
		}

		ProductMapper<String, String> productMapper = new ProductMapper<String, String>(product);

		RuleEvaluationContext context = new RuleEvaluationContext();
		context.set(productMapper);

		boolean flag = false;
		List<Integer> points = new ArrayList<Integer>();
		for (Map<String, Object> rule : rules) {
			String expression = MapUtils.getString(rule, "expression");
			if (StringUtils.isBlank(expression)) {
				throw new BusinessException("积分规则表达式为空");
			}
			ExpressionProcessor processor = new ExpressionProcessor(expression);
			expression = processor.replace(productMapper).getExpression();
			System.out.println(expression);
			ExpressionParser p = new SpelExpressionParser();
			try {
				flag = p.parseExpression(expression).getValue(context,Boolean.class);
			} catch (Exception e) {
				throw new BusinessException("积分规则表达式[" + MapUtils.getString(rule, "expression") + "]有误");
			}
			if (flag) {
				BigDecimal r = new BigDecimal(MapUtils.getString(rule, "pointsRule"));
				points.add(price.divide(r).intValue());
			}
		}

		if (CollectionUtils.isEmpty(points)) {
			return 0;
		} else if (points.size() == 1) {
			return points.get(0);
		} else {
			throw new BusinessException("匹配到多个商品积分规则");
		}
	}
}



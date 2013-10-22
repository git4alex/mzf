package com.zonrong.common.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;

/**
 * date: 2011-1-25
 *
 * version: 1.0
 * commonts: ......
 */
public class MzfUtils {
	public enum BillPrefix {
		/**
		 * 客订单
		 */
		KD,
		/**
		 * 要货申请
		 */
		SPYH,
		/**
		 * 要货申请
		 */
		WLYH,		
		/**
		 * 收货单
		 */
		SH,
		/**
		 * 出库
		 */
		CK,
		/**
		 * 维修单
		 */
		WX,
		/**
		 * 销售单
		 */
		XS,
		/**
		 * 发货单
		 */
		FH,
		/**
		 * 退货
		 */
		TH,
		/**
		 * 翻新
		 */
		FX,
		/**
		 * 拆旧
		 */
		CJ
	}
	
	public static String getBillNum(BillPrefix prefix, IUser user) throws BusinessException {
		return getBillNum(prefix.toString(), user);
	}
	
	public static String getBillNum(String prefix, IUser user) throws BusinessException {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyMMddHHmmss");   
        String dateStr = dateformat.format(new Date());	
//        String res = prefix + "-" + user.getOrgBizCode() + "-" + dateStr;
        String res = prefix + user.getOrgBizCode() + dateStr;
        System.out.println(res);
        return res;
	}	
	
	public static BigDecimal getAvgByWeighted(Double allQuantity, Double allCost, BigDecimal outQuantity) {					
		BigDecimal unitCost = new BigDecimal(allCost.doubleValue()/allQuantity.doubleValue());
		BigDecimal outCost = unitCost.multiply(outQuantity, new MathContext(6));
		return outCost;
	}
	
	public static void main(String[] args) {
		BigDecimal c = getAvgByWeighted(3.0, 100.0, new BigDecimal(2.0));
		System.out.println(c);
	}
}



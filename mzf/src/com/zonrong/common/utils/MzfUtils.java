package com.zonrong.common.utils;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * date: 2011-1-25
 *
 * version: 1.0
 * commonts: ......
 */
public class MzfUtils {
	public enum BillPrefix {
		KD,//客订单
		SPYH,//商品要货申请
		WLYH,//物料要货申请
		SH,//收货单
		CK,//出库
		WX,//维修单
		XS,//销售单
		FH,//发货单
		TH,//退货
		FX,//翻新
		CJ//拆旧
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

	public static Double getAvgByWeighted(Double allQuantity, Double allCost, BigDecimal outQuantity) {
		BigDecimal unitCost = new BigDecimal(allCost / allQuantity);
        return unitCost.multiply(outQuantity, new MathContext(6)).doubleValue();
	}

	public static void main(String[] args) {
		Double c = getAvgByWeighted(3.0, 100.0, new BigDecimal(2.0));
		System.out.println(c);
	}
}



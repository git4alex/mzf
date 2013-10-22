package com.zonrong.basics.product.service;

import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.security.IUser;
import com.zonrong.core.sql.service.SimpleSqlService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * User: Alex
 * Date: 13-8-8
 * Time: 下午10:08
 */

@Service
public class ChangePriceService {
    private Logger logger = Logger.getLogger(this.getClass());

    @Resource
    private SimpleSqlService sqlService;

    public void updatePrice(Map<String, Object> params, IUser user) throws BusinessException {
        params.put("ouser",user.getId());
        params.put("ouserName",user.getName());
        params.put("cdate",new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        sqlService.update("changePrice", "insertPriceChangeLog", params, user);
        sqlService.update("changePrice", "updateProduct", params, user);
    }
}

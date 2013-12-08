package com.zonrong.basics.customer.service;

import com.zonrong.common.service.MzfOrgService;
import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.CustomerLogType;
import com.zonrong.common.utils.MzfEnum.CustomerPointsType;
import com.zonrong.common.utils.MzfEnum.CustomerType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.service.EntityService;
import com.zonrong.inventory.treasury.service.TreasuryService.BizType;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;
import com.zonrong.system.service.BizCodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * date: 2010-11-27
 *
 * version: 1.0
 * commonts: ......
 */
@Service
public class CustomerService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private UpgradeRuleService upgradeRuleService;
	@Resource
	private MzfOrgService mzfOrgService;
	@Resource
	private BusinessLogService businessLogService;
	@Resource
	private CustomerCardService customerCardService;

	public int create(Map<String, Object> customer, IUser user) throws BusinessException {
		//判断客户资料是否重复
		String name = MapUtils.getString(customer, "name", StringUtils.EMPTY);
		String mobile = MapUtils.getString(customer, "mobile", StringUtils.EMPTY);
		if (StringUtils.isBlank(name) || StringUtils.isBlank(mobile)) {
			throw new BusinessException("客户姓名或手机为空");
		}

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUSTOMER);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("name", name);
		where.put("mobile", mobile);
		List<Map<String, Object>> list = entityService.list(MzfEntity.CUSTOMER, where, null, User.getSystemUser());
		if (CollectionUtils.isNotEmpty(list)) {
			throw new BusinessException("已经存在姓名与手机完全相同的客户");
		}

		//新增客户资料
		String id = entityService.create(metadata, customer, user);
		return Integer.parseInt(id);
	}

	public void update(int id, Map<String, Object> customer, IUser user) throws BusinessException {
		//判断客户资料是否重复
		String name = MapUtils.getString(customer, "name", StringUtils.EMPTY);
		String mobile = MapUtils.getString(customer, "mobile", StringUtils.EMPTY);
		if (StringUtils.isBlank(name) || StringUtils.isBlank(mobile)) {
			throw new BusinessException("客户姓名或手机为空");
		}

		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUSTOMER);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("name", name);
		where.put("mobile", mobile);
		List<Map<String, Object>> list = entityService.list(metadata, where, null, User.getSystemUser());
		if (CollectionUtils.isNotEmpty(list)) {
			String s = "已经存在姓名与手机完全相同的客户";
			if (list.size() == 1) {
				if (MapUtils.getInteger(list.get(0), metadata.getPkCode()) != id) {
					throw new BusinessException(s);
				}
			} else {
				throw new BusinessException(s);
			}
		}

		//新增客户资料
		Map<String, Object> field = new HashMap<String, Object>(customer);
		field.remove("id");
		int row = entityService.updateById(metadata, Integer.toString(id), field, user);
		if (row != 1) {
			throw new BusinessException("发生数据错误");
		}
	}


	//修改客户级别
	private void upCustomerGrade(Map<String, Object> dbCustomer, int points, IUser user) throws BusinessException {
		String message = "自动升级";
		CustomerLogType type = CustomerLogType.upgrade;
		if(points < 0){
			message = "自动降级";
			type = CustomerLogType.downgrade;
		}
		//EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUSTOMER);
		//Map<String, Object> dbCustomer = entityService.getById(metadata, cusId, user.asSystem());

		int dbPoints = MapUtils.getIntValue(dbCustomer, "historyPoints", 0);
		String dbGrade = MapUtils.getString(dbCustomer, "grade");
		String cardNum = MapUtils.getString(dbCustomer, "cardNo");
		Integer cusId = MapUtils.getInteger(dbCustomer, "id");
		int cardId = customerCardService.getCardIdByNum(cardNum, user);
		String cusType = MapUtils.getString(dbCustomer, "type");
		dbPoints = dbPoints + points;
		Map<String, Object> field = new HashMap<String, Object>();
		//field.put("points", dbPoints);
			try {
				String grade = upgradeRuleService.getGradeByCode(dbPoints);
				if(grade != null){
					String  gradeText = BizCodeService.getBizName("cusGrade", grade);
					 //vip客户自动升级
					if(cusType.equals(CustomerType.vip.toString())){
						if (!grade.equals(dbGrade)) {
							field.put("grade", grade);
							createLog(cusId, cardId, type, message+"为" + gradeText, user);
							entityService.updateById(MzfEntity.CUSTOMER, Integer.toString(cusId), field, user);
						}
					}else{
						//其他类型会员生成提醒日志
						if(!grade.equals(dbGrade)){
							if(points > 0){
								createLog(cusId, cardId, CustomerLogType.upgradeWarn, "会员升级为："+ gradeText, user);
							}else{
								createLog(cusId, cardId, CustomerLogType.downgradeWarn, "会员降级为："+ gradeText, user);
							}
						}

					}
				}else{
					if(points <= 0){
						createLog(cusId, cardId, CustomerLogType.downgradeWarn, "会员降级，当前积分为：" + dbPoints, user);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
			}


	}
	//增加兑换积分
//	public void addExchangePoints(int cusId, int exchangePoints,IUser user) throws BusinessException{
//		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUSTOMER);
//		Map<String, Object> dbCustomer = entityService.getById(metadata, cusId, user.asSystem());
//		int dbPoints = MapUtils.getIntValue(dbCustomer, "exchangePoints", 0);
//
//		dbPoints = dbPoints+exchangePoints;
//		Map<String, Object> field = new HashMap<String, Object>();
//		field.put("exchangePoints", dbPoints);
//		int row = entityService.updateById(metadata, Integer.toString(cusId), field, user);
//		createPointLog(cusId, CustomerPointsType.exchangePoints, dbPoints, "积分兑换物料", user);
//		if (row == 0) {
//			throw new BusinessException("未找到客户[" + cusId + "]");
//		}
//
//	}
	 //增加客户积分
	public void updatePoints(int cusId, int exchangePoints, int curPoints, int payPoints,IUser user, String saleNum) throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUSTOMER);
		Map<String, Object> dbCustomer = entityService.getById(metadata, cusId, user.asSystem());
        //剩余积分
        int oldCusPoints = MapUtils.getIntValue(dbCustomer, "points", 0);
        //历史积分
        int oldCusHisPoints = MapUtils.getIntValue(dbCustomer, "historyPoints", 0);

        Map<String, Object> field = new HashMap<String, Object>();

        if(payPoints>0){
            oldCusPoints = oldCusPoints - payPoints;

            //积分流水
            String remark = "剩余积分："+oldCusPoints+";销售单号：" + saleNum;
            createPointLog(cusId, CustomerPointsType.payPoints, payPoints, remark, user);

            field.put("points", oldCusPoints); //更新剩余积分
        }

        if(exchangePoints>0){
            //兑换积分
            int oldCusExchangePoints = MapUtils.getIntValue(dbCustomer, "exchangePoints", 0);
            oldCusExchangePoints = oldCusExchangePoints + exchangePoints;

            //锁定积分
            int lockedPoints = MapUtils.getIntValue(dbCustomer, "lockedPoints", 0);
            lockedPoints = lockedPoints - exchangePoints;

            field.put("exchangePoints", oldCusExchangePoints);
            field.put("lockedPoints", lockedPoints);

            String remark = "剩余积分："+oldCusPoints+";销售单号：" + saleNum;
            createPointLog(cusId, CustomerPointsType.exchangePoints, exchangePoints, remark, user);
        }

        //本次积分 > 0
        if(curPoints>0){
            //修改客户级别
            upCustomerGrade(dbCustomer, curPoints, user);

            oldCusPoints = oldCusPoints + curPoints;
            oldCusHisPoints = oldCusHisPoints + curPoints;

            //积分流水
            String remark = "剩余积分："+oldCusPoints+";销售单号：" + saleNum;
            createPointLog(cusId, CustomerPointsType.points, curPoints, remark, user);

            field.put("points", oldCusPoints); //更新剩余积分
            field.put("historyPoints", oldCusHisPoints);//更新历史积分
        }

		int row = entityService.updateById(metadata, Integer.toString(cusId), field, user);
		if (row == 0) {
			throw new BusinessException("更新客户积分时未找到客户[" + cusId + "]");
		}
	}

	/**
	 * 锁定积分
	 * @param cusId
	 * @param lockedPoints
	 * @throws BusinessException
	 */
	public void lockedPoints(int cusId, int lockedPoints, IUser user)throws BusinessException{
		Map<String, Object>  customer = entityService.getById(MzfEntity.CUSTOMER, cusId, user);
		 //剩余积分
		int points = MapUtils.getIntValue(customer, "points");
		int cur_locked = MapUtils.getIntValue(customer, "lockedPoints", 0);
		if(lockedPoints > points){
			throw new BusinessException("剩余积分不足！");
		}
		points = points - lockedPoints;
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("points", points);
		field.put("lockedPoints", lockedPoints+cur_locked);
		entityService.updateById(MzfEntity.CUSTOMER, cusId + "", field, user);
		createPointLog(cusId, CustomerPointsType.lockedPoints, lockedPoints, "积分锁定", user);
	}
	/**
	 * 解锁积分
	 * @param cusId
	 * @throws BusinessException
	 */
	public void unlockedPoints(int cusId, IUser user)throws BusinessException{
		Map<String, Object>  customer = entityService.getById(MzfEntity.CUSTOMER, cusId, user);
		 //剩余积分
		int points = MapUtils.getIntValue(customer, "points");
		int lockedPoints = MapUtils.getIntValue(customer, "lockedPoints");
		points = points + lockedPoints;
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("points", points);
		field.put("lockedPoints", 0);
		entityService.updateById(MzfEntity.CUSTOMER, cusId + "", field, user);
		//记录流水
		createPointLog(cusId, CustomerPointsType.unlockedPoints, lockedPoints, "积分解锁", user);
	}
	 //修改客户积分有效期
	public void updatePointsIndate(int cusId, IUser user)throws BusinessException{
		Calendar now = Calendar.getInstance();
		now.add(Calendar.YEAR, 2);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String pointsIndate = sdf.format(now.getTime());
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("pointsIndate", pointsIndate);
		entityService.updateById(MzfEntity.CUSTOMER, cusId +"", field, user);

	}
	 //退货减分
	public void subtractPoints(int cusId, int subPoints, int exchangePoints, IUser user, String returnsNum) throws BusinessException{
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUSTOMER);
		Map<String, Object> dbCustomer = entityService.getById(metadata, cusId, user.asSystem());
		//修改客户级别
		upCustomerGrade(dbCustomer, subPoints, user);
		//剩余积分
		int oldCusPoints = MapUtils.getIntValue(dbCustomer, "points", 0);
		oldCusPoints = oldCusPoints+subPoints+exchangePoints;

		//历史积分
		int oldCusHisPoints = MapUtils.getIntValue(dbCustomer, "historyPoints", 0);
		oldCusHisPoints = oldCusHisPoints+subPoints;

		//兑换积分
		int oldCusExchangePoints = MapUtils.getIntValue(dbCustomer, "exchangePoints", 0);
		oldCusExchangePoints = oldCusExchangePoints-exchangePoints;


		Map<String, Object> field = new HashMap<String, Object>();
		field.put("points", oldCusPoints); //剩余积分
		field.put("historyPoints", oldCusHisPoints);//历史积分
		field.put("exchangePoints", oldCusExchangePoints); //兑换积分
		int row = entityService.updateById(metadata, Integer.toString(cusId), field, user);
		if (row == 0) {
			throw new BusinessException("未找到客户[" + cusId + "]");
		}

        //积分流水
        String remark = "剩余积分：" + oldCusPoints + ";退货单号：" + returnsNum;
        createPointLog(cusId, CustomerPointsType.points, subPoints+exchangePoints, remark, user);
    }

	public void upPoints(int cusId, int points,int historyPoints,int exchangePoints, String remark, IUser user) throws BusinessException {
		EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUSTOMER);
		Map<String, Object> dbCustomer = entityService.getById(metadata, cusId, user.asSystem());
		int oldHistoryPoints = MapUtils.getIntValue(dbCustomer, "historyPoints", 0);
		 //修改客户级别
		upCustomerGrade(dbCustomer,historyPoints-oldHistoryPoints, user);

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("points", points);
		field.put("historyPoints", historyPoints);
		field.put("exchangePoints", exchangePoints);
		int row = entityService.updateById(metadata, Integer.toString(cusId), field, user);
		//createLog(cusId, null, CustomerLogType.upPoints, remark, user);
		if (row == 0) {
			throw new BusinessException("未找到客户[" + cusId + "]");
		}
	}

	public int createOrgRel(int cusId, BizType bizType, int billId, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("cusId", cusId);
		field.put("orgId", user.getOrgId());
		field.put("bizType", bizType);
		field.put("billId", billId);
		field.put("cuserId", user.getId());
		field.put("cdate", null);

		String id = entityService.create(MzfEntity.CUSTOMER_ORG_REL, field, user);

		//如果该客户为潜在客户，在升级为普通客户
		Map<String, Object> customer = entityService.getById(MzfEntity.CUSTOMER, cusId, user.asSystem());
		CustomerType type = CustomerType.valueOf(MapUtils.getString(customer, "type"));
		if (type == CustomerType.potential) {
			field = new HashMap<String, Object>();
			field.put("type", CustomerType.normal);
			entityService.updateById(MzfEntity.CUSTOMER, Integer.toString(cusId), field, user);
		}

		return Integer.parseInt(id);
	}

	public Map<String, Object> getCustomerById(int cusId, IUser user) throws BusinessException {
		Map<String, Object> customer = entityService.getById(MzfEntity.CUSTOMER_VIEW, cusId, user.asSystem());

		Map<String, Object> where = new HashMap<String, Object>();
		where.put("cusId", cusId);
		where.put("orgId", user.getOrgId());
		List<Map<String, Object>> list = entityService.list(MzfEntity.CUSTOMER_ORG_REL, where, null, user.asSystem());
		boolean isShowTrade = false;
		if (CollectionUtils.isNotEmpty(list) || mzfOrgService.isHq(user.getOrgId())) {
			isShowTrade = true;
		}


		if (MapUtils.isNotEmpty(customer)) {
			customer.put("isShowTrade", isShowTrade);
		}

		return customer;
	}

	public int grantCard(int cusId, int cardId, String remark, IUser user) throws BusinessException {
		Map<String, Object> card = entityService.getById(MzfEntity.CUSTOMER_CARD, Integer.toString(cardId), user.asSystem());
		Integer ownerOrgId = MapUtils.getInteger(card, "ownerOrgId");

		if (ownerOrgId != user.getOrgId()) {
			String num = MapUtils.getString(card, "num");
			throw new BusinessException("该卡[" + num + "]不属于本部门");
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("cusId", cusId);
		field.put("cardId", cardId);
		field.put("orgId", user.getOrgId());
		field.put("remark", remark);
		field.put("cdate", null);
		field.put("cuserId", null);

		String id = entityService.create(MzfEntity.GRANT_CARD, field, user);

		//如果该客户不是vip，在升级为vip客户
		Map<String, Object> customer = entityService.getById(MzfEntity.CUSTOMER, cusId, user.asSystem());
		CustomerType type = CustomerType.valueOf(MapUtils.getString(customer, "type"));
		field = new HashMap<String, Object>();
		field.put("cardNo", MapUtils.getString(card, "num"));
		if (type != CustomerType.vip) {
			field.put("type", CustomerType.vip);
		}
		entityService.updateById(MzfEntity.CUSTOMER, Integer.toString(cusId), field, user);

		createLog(cusId, cardId, CustomerLogType.grant, remark, user);
		//记录操作日志
		businessLogService.log("发放会员卡", "客户名称:" + MapUtils.getString(customer, "name"), user);
		return Integer.parseInt(id);
	}

	public int createLog(Integer cusId, Integer cardId, CustomerLogType type, String remark, IUser user) throws BusinessException {
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("cusId", cusId);
		field.put("cardId", cardId);
		field.put("type", type);
		field.put("remark", remark);
		field.put("cuserId", null);
		field.put("cdate", null);

		String id = entityService.create(MzfEntity.CUSTOMER_LOG, field, user);
		return Integer.parseInt(id);
	}

	public void createPointLog(Integer cusId,  CustomerPointsType customerPointsType,int points, String remark, IUser user) throws BusinessException{
		if(points != 0){
			Map<String, Object> field = new HashMap<String, Object>();
			field.put("cusId", cusId);
			field.put("cuserId", null);
			//field.put("operate", customerPointsOperateType);
			field.put("pointsType", customerPointsType);
			field.put("points", points);
			field.put("cdate", null);
			field.put("remark", remark);
			entityService.create(MzfEntity.CUSTOMER_POINTS_FLOW, field, user);
		}
	}


	public String findCustomerGrade(int customerId)throws BusinessException{
		Map<String,Object> customer = entityService.getById(MzfEntity.CUSTOMER_VIEW, customerId, User.getSystemUser());
		if(customer != null && customer.get("grade") != null){
			return customer.get("grade").toString();
		}

		return null;
	}

}



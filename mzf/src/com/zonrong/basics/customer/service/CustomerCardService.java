package com.zonrong.basics.customer.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.CustomerCardStatus;
import com.zonrong.common.utils.MzfEnum.CustomerLogType;
import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * date: 2011-8-25
 * 
 * version: 1.0 commonts: ......
 */
@Service
public class CustomerCardService {
	private Logger logger = Logger.getLogger(this.getClass());

	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private CustomerService customerService;
	@Resource
	private BusinessLogService businessLogService;

	public void lock(int cardId, String ramark, IUser user)
			throws BusinessException {
		Map<String, Object> card = getCard(cardId);
		CustomerCardStatus status = CustomerCardStatus.valueOf(MapUtils
				.getString(card, "status"));
		CustomerCardStatus targetStatus = CustomerCardStatus.lock;
		String num = "";
		if (status != CustomerCardStatus.free) {
			num = MapUtils.getString(card, "num");
			throw new BusinessException("会员卡[" + num + "]状态为"
					+ status.getText() + ", 不能" + targetStatus.getText());
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", targetStatus);
		entityService.updateById(MzfEntity.CUSTOMER_CARD, Integer
				.toString(cardId), field, user);

		Integer cusId = MapUtils.getInteger(card, "grantCusId");
		customerService.createLog(cusId, cardId, CustomerLogType.frozen,
				ramark, user);
		//记录操作日志
		businessLogService.log("会员卡冻结", "会员卡号为：" + num, user);
	}

	public void free(int cardId, String ramark, IUser user)
			throws BusinessException {
		Map<String, Object> card = getCard(cardId);
		CustomerCardStatus status = CustomerCardStatus.valueOf(MapUtils
				.getString(card, "status"));
		CustomerCardStatus targetStatus = CustomerCardStatus.free;
		String num = "";
		if (status != CustomerCardStatus.lock) {
			num = MapUtils.getString(card, "num");
			throw new BusinessException("会员卡[" + num + "]状态为"
					+ status.getText() + ", 不能" + targetStatus.getText());
		}

		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", targetStatus);
		entityService.updateById(MzfEntity.CUSTOMER_CARD, Integer
				.toString(cardId), field, user);

		Integer cusId = MapUtils.getInteger(card, "grantCusId");
		customerService.createLog(cusId, cardId, CustomerLogType.unfrozen,
				ramark, user);
		//记录操作日志
		businessLogService.log("会员卡解冻", "会员卡号为：" + num, user);
	}

	public void obsolete(Integer[] cardIds, String remark, IUser user)
			throws BusinessException {
		for (Integer cardId : cardIds) {
			Map<String, Object> card = getCard(cardId);
			// CustomerCardStatus status =
			// CustomerCardStatus.valueOf(MapUtils.getString(card, "status"));
			CustomerCardStatus targetStatus = CustomerCardStatus.obsolete;
			// if (status != CustomerCardStatus.lock) {
		    String num = MapUtils.getString(card, "num");
			// throw new BusinessException("会员卡[" + num + "]状态为" +
			// status.getText() + ", 不能" + targetStatus.getText());
			// }

			Map<String, Object> field = new HashMap<String, Object>();
			field.put("status", targetStatus);
			entityService.updateById(MzfEntity.CUSTOMER_CARD, Integer
					.toString(cardId), field, user);

			Integer cusId = MapUtils.getInteger(card, "grantCusId");
			customerService.createLog(cusId, cardId, CustomerLogType.obsolete,
					remark, user);
			//记录操作日志
			businessLogService.log("会员卡作废", "会员卡号为：" + num, user);
		}
	}

	private Map<String, Object> getCard(int cardId) throws BusinessException {
		Map<String, Object> card = entityService.getById(
				MzfEntity.CUSTOMER_CARD_VIEW, Integer.toString(cardId), User
						.getSystemUser());

		if (MapUtils.isEmpty(card)) {
			throw new BusinessException("未找到会员卡");
		}

		return card;
	}
	
	public int getCardIdByNum(String num, IUser user) throws BusinessException{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("num",num);
		List<Map<String, Object>> cards = entityService.list(MzfEntity.CUSTOMER_CARD_VIEW, where, null, user);
		if(CollectionUtils.isNotEmpty(cards)){
			Map<String, Object> card = cards.get(0);
			return MapUtils.getIntValue(card, "id");
		}
		return 0;
	}

	public List<Map<String, Object>> loadData(File cardFile, boolean isDelete)
			throws BusinessException {
		if (!cardFile.exists()) {
			throw new BusinessException("要读取的文件不存在");
		}

		List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
		boolean deleteFlag = false; //捕获异常删除文件
		InputStream stream = null;
		Set<String> cardNumSet = new HashSet<String>();
		Map<String, Integer> orgs = getOrgs(); // 所有部门
		Map<String, String> cardTypes = getCardTypeValue(); // 所有会员卡的类型
		try {
			stream = new FileInputStream(cardFile);
			Workbook wb = null;
			if(cardFile.getName().endsWith("xls")){
				wb = new HSSFWorkbook(stream);
			}else{
				wb = new XSSFWorkbook(stream);
			}
			if (wb.getNumberOfSheets() == 0) {
				throw new BusinessException("要导入的Excel文件中不存在数据");
			}
			Sheet sheet = wb.getSheetAt(0);
			for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
				Map<String, Object> card = new HashMap<String, Object>();
				Row row = sheet.getRow(i);
				 String cardNum = "";
				   if(row.getCell(0) != null){
					   if(row.getCell(0).getCellType() == Cell.CELL_TYPE_STRING){
						   cardNum = row.getCell(0).getStringCellValue();
					   }else{
						   cardNum = row.getCell(0).getNumericCellValue()+"";
					   }
				   }
					cardNumSet.add(cardNum);
					card.put("num", cardNum);
					// 会员卡类型
					String typeText = row.getCell(1)!= null?row.getCell(1).getStringCellValue():"";
					if (cardTypes.containsKey(typeText)) {
						String typeValue = cardTypes.get(typeText);
						card.put("typeValue", typeValue);
					} else {
						throw new BusinessException("导入数据的会员卡类别{" + typeText
								+ "}不存在");
					}
					card.put("typeName", typeText);
					// 所属部门编号
					String orgName = row.getCell(2)!= null?row.getCell(2).getStringCellValue():"";
					if (orgs.containsKey(orgName)) {
						int orgId = orgs.get(orgName);
						card.put("ownerOrgId", orgId);
					} else {
						throw new BusinessException("导入数据中的部门{" + orgName
								+ "}不存在");
					}

					card.put("ownerOrgName",orgName);
//					String indate = dateFormat(row.getCell(3).getDateCellValue());
//					if (indate == null) {
//						throw new BusinessException("导入数据的时间格式有误");
//					} else {
//						card.put("indate", indate);
//					}
					card.put("remark", row.getCell(3)!= null?row.getCell(3).getStringCellValue():"");
					cards.add(card);

				

			}
			if (sheet.getPhysicalNumberOfRows()-1 > cardNumSet.size()) {
				throw new BusinessException("会员卡号不能重复");
			}
		} catch (BusinessException e) {
			deleteFlag = true;
			throw e;
		} catch (Exception e) {
			deleteFlag = true;
			throw new BusinessException("导入数据库的格式有误");
		} finally {
			if (isDelete || deleteFlag) {
				try {
					stream.close();
					cardFile.delete();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.error(e.getMessage(), e);
				}
			}
		}

		return cards;
	}

	private String dateFormat(Date date) {
		String time = null;
		try {
//			DateCell dateCell = (DateCell) cell;
//			Date date = dateCell.getDate();
			time = new SimpleDateFormat("yyyy-MM-dd").format(date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return null;
		}
		return time;
	}

	private Map<String, Integer> getOrgs() throws BusinessException {
		EntityMetadata metadata = metadataProvider
				.getEntityMetadata(TpltEnumEntityCode.ORG);
		List<Map<String, Object>> list = entityService.list(metadata,
				new HashMap<String, Object>(), null, User.getSystemUser());
		Map<String, Integer> orgs = new HashMap<String, Integer>();
		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> org = list.get(i);
			if (org != null) {
				int id = Integer.parseInt(org.get("id").toString());
				orgs.put(org.get("text").toString(), id);
			}
		}
		return orgs;

	}

	private Map<String, String> getCardTypeValue() throws BusinessException {
		EntityMetadata metadata = metadataProvider
				.getEntityMetadata(TpltEnumEntityCode.BIZ_CODE);
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("typeCode", "customerCardType");
		OrderBy orderBy = new OrderBy(new String[] { "id" }, OrderByDir.asc);
		List<Map<String, Object>> typeValues = entityService.list(metadata,
				where, orderBy, User.getSystemUser());
		Map<String, String> cardTypes = new HashMap<String, String>();
		for (int i = 0; i < typeValues.size(); i++) {
			Map<String, Object> typeValue = typeValues.get(i);
			if (typeValue != null) {
				cardTypes.put(typeValue.get("text").toString(), typeValue.get(
						"value").toString());
			}

		}
		return cardTypes;
	}

	public void batchCreate(List<Map<String, Object>> data, IUser user)throws BusinessException {
		try {
			EntityMetadata metadata = metadataProvider.getEntityMetadata(MzfEntity.CUSTOMER_CARD);
			
			for (Map<String, Object> map : data) {
				map.put("status", CustomerCardStatus.free);
				map.put("cdate", null);
				map.put("cuserId", null);
				 
			} 
			if(!data.isEmpty()){
				entityService.batchCreate(metadata, data, user);  
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			if (e.getMessage().indexOf("UK_CUSTOMER_CARD_NUM") > 0) {
				throw new BusinessException("卡号重复");
			}
		}
	}
}

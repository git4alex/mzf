package com.zonrong.basics.chit.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.zonrong.common.utils.MzfEntity;
import com.zonrong.common.utils.MzfEnum.ChitStatus;
import com.zonrong.common.utils.MzfEnum.TargetType;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.core.log.BusinessLogService;
import com.zonrong.core.log.FlowLogService;
import com.zonrong.core.log.TransactionService;
import com.zonrong.core.security.IUser;
import com.zonrong.core.security.User;
import com.zonrong.entity.code.TpltEnumEntityCode;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.service.MetadataProvider;

/**
 * 2011-09-6
 * 
 * @author Administrator 代金券
 */
@Service
public class ChitService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Resource
	private MetadataProvider metadataProvider;
	@Resource
	private EntityService entityService;
	@Resource
	private TransactionService transactionService;
	@Resource
	private FlowLogService logService;
	@Resource
	private BusinessLogService businessLogService;

	public int batchCreate(List<Map<String, Object>> data, IUser user)
			throws BusinessException {
		for (Map<String, Object> map : data) {
			map.put("status", ChitStatus.normal);
			map.put("cdate", null);
		}
		try {
			entityService.batchCreate(MzfEntity.CHIT, data, user);
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage().indexOf("UK_CHIT_NUM") > 0) {
				throw new BusinessException("代金券条码重复");
			}
			logger.error(e.getMessage(), e);
		}

		return 0;
	}

	public List<Map<String, Object>> loadChitData(File excelFile,
			boolean isDelete) throws BusinessException {
		if (!excelFile.exists()) {
			throw new BusinessException("要读取的文件不存在");
		}

		List<Map<String, Object>> chits = new ArrayList<Map<String, Object>>();
		InputStream stream = null;
		boolean errFlag = false;
		try {
			stream = new FileInputStream(excelFile);
			Map<String, Integer> orgs = getOrgs(); // 所有部门
			Workbook wb = null;
			if(excelFile.getName().endsWith("xls")){
				wb = new HSSFWorkbook(stream);
			}else{
				wb = new XSSFWorkbook(stream);
			}
			if (wb.getNumberOfSheets() == 0) {
				throw new BusinessException("要导入的Excel文件中不存在数据");
			}
			Sheet sheet = wb.getSheetAt(0);
			
			Set<String> numSet = new HashSet<String>();
			int rowIndex = 1;
		    boolean isNext = true;
			  while(isNext){
				int lineNo = rowIndex+1;
				Row row = sheet.getRow(rowIndex);
				
				Map<String, Object> chit = new HashMap<String, Object>();
				if (row.getCell(0) == null || row.getCell(0).getStringCellValue().equals("")) {
					throw new BusinessException("导入的代金券第" + lineNo + "行名称为空");
				} else {
					String name = row.getCell(0).getStringCellValue();
					chit.put("name", name);
				}

				
				if (row.getCell(1) == null || row.getCell(1).getStringCellValue().equals("")) {
					throw new BusinessException("导入的代金券第" + lineNo + "行条码为空");
				}else{
					String num = row.getCell(1).getStringCellValue();
					numSet.add(num);
					chit.put("num", num); // 代金券面值
				}
				
				if (row.getCell(2) == null || row.getCell(2).getNumericCellValue() == 0.0) {
					throw new BusinessException("导入的代金券第" + lineNo + "行面值为空");
				} else {
					String faceValue = row.getCell(2).getNumericCellValue()+"";
					BigDecimal DecFaceValue = new BigDecimal(faceValue);
					chit.put("faceValue", DecFaceValue);
				}

				// 所属部门编号
				String orgName = row.getCell(3)!=null?row.getCell(3).getStringCellValue():"";
				
				if (orgs.containsKey(orgName)) {
					int orgId = orgs.get(orgName);
					chit.put("orgId", orgId);
				} else {
					chit.put("orgId", null);
				}

				chit.put("orgName",orgName);

				String indate = dateFormat(row.getCell(4).getDateCellValue());
				if (indate == null) {
					throw new BusinessException("导入的代金券第" + lineNo + "行有效期为空");
				} else {
					
					chit.put("indate", indate);
				}
				String onsetDate = dateFormat(row.getCell(5).getDateCellValue());
				if (onsetDate == null) {
					throw new BusinessException("导入的代金券第" + lineNo + "行生效时间为空");
				} else {
					
					chit.put("onsetDate", onsetDate);
				}
				chit.put("remark", row.getCell(6)!=null?row.getCell(6).getStringCellValue():"");
				chits.add(chit);
				rowIndex = rowIndex + 1;
				row = sheet.getRow(rowIndex);
				if(row == null){
					isNext = false;
					rowIndex = rowIndex - 1;
				}
			}

			if (rowIndex > numSet.size()) {
				throw new BusinessException("导入代金券条码不能重复");
			}
		} catch (BusinessException e) {
			errFlag = true;
			throw e;
		} catch (Exception e) {
			errFlag = true;
			throw new BusinessException(e.getMessage());
		}

		finally {
			if (isDelete || errFlag) {
				// 关闭文件流
				try {
					if (stream != null) {
						stream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				excelFile.delete();
			}
		}

		return chits;
	}

	public int updateChitOrg(int orgId, Integer[] chitIds, IUser user)
			throws BusinessException {
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", chitIds);
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("orgId", orgId);
		return entityService.update(MzfEntity.CHIT, field, where, user);
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
		EntityMetadata metadata = metadataProvider.getEntityMetadata(TpltEnumEntityCode.ORG);
		List<Map<String, Object>> list = entityService.list(metadata,new HashMap<String, Object>(), null, User.getSystemUser());
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

	public void sellChit(int chitId, IUser user)
			throws BusinessException {
		// 验证有效性(状态为未发放)
		Map<String, Object> chit = entityService.getById(MzfEntity.CHIT_VIEW,Integer.toString(chitId), user.asSystem());
		ChitStatus status = ChitStatus.valueOf(MapUtils.getString(chit,"status"));
		if (status != ChitStatus.normal) {
			String num = MapUtils.getString(chit, "num");
			throw new BusinessException("代金券[" + num + "]状态为"+ status.getText() + "，不能销售");
		}

		// 激活(状态改为已激活)
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", ChitStatus.activate);
		entityService.updateById(MzfEntity.CHIT, Integer.toString(chitId),field, user);

		// 记录日志
		int transId = transactionService.findTransId(MzfEntity.CHIT, Integer.toString(chitId), user);
		logService.createLog(transId, MzfEntity.CHIT, Integer.toString(chitId),"销售", TargetType.chit, chitId, null, user);
	}
	
	//退回已销售的代金券
	public void backSellChit(int chitId, IUser user)throws BusinessException {
	   // 验证有效性(状态为未发放)
		Map<String, Object> chit = entityService.getById(MzfEntity.CHIT_VIEW,Integer.toString(chitId), user.asSystem());
		ChitStatus status = ChitStatus.valueOf(MapUtils.getString(chit,"status"));
		if (status != ChitStatus.activate) {
			String num = MapUtils.getString(chit, "num");
			throw new BusinessException("代金券[" + num + "]状态为"+ status.getText() + "，不能退回");
		}
	
		// 激活(状态改为已激活)
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", ChitStatus.normal);
		entityService.updateById(MzfEntity.CHIT, Integer.toString(chitId),field, user);
	
		// 记录日志
		int transId = transactionService.findTransId(MzfEntity.CHIT, Integer.toString(chitId), user);
		logService.createLog(transId, MzfEntity.CHIT, Integer.toString(chitId),"退回", TargetType.chit, chitId, null, user);
	}
	
	public void sellChit(int chitId, IUser user,String remark) throws BusinessException {
		// 验证有效性(状态为未发放)
		Map<String, Object> chit = entityService.getById(MzfEntity.CHIT_VIEW,
				Integer.toString(chitId), user.asSystem());
		ChitStatus status = ChitStatus.valueOf(MapUtils.getString(chit,
				"status"));
		if (status != ChitStatus.normal) {
			String num = MapUtils.getString(chit, "num");
			throw new BusinessException("代金券[" + num + "]状态为"
					+ status.getText() + "，不能销售");
		}

		// 激活(状态改为已激活)
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", ChitStatus.activate);
		entityService.updateById(MzfEntity.CHIT, Integer.toString(chitId),
				field, user);

		// 记录日志
		int transId = transactionService.findTransId(MzfEntity.CHIT, Integer
				.toString(chitId), user);
		logService.createLog(transId, MzfEntity.CHIT, Integer.toString(chitId),
				"销售", TargetType.chit, chitId, remark, user);
	}
	
	 //批量发放代金券
	public void batchSellChit(Integer[] chitIds,IUser user,String remark) throws BusinessException{
		for (Integer chitId : chitIds) {
			sellChit(chitId,user,remark);
		}
	}

	//回收代金券
	public void returnsChit(int chitId, int saleId, IUser user)
			throws BusinessException {
		// 验证有效性(状态为已激活)
		Map<String, Object> chit = entityService.getById(MzfEntity.CHIT_VIEW,Integer.toString(chitId), user.asSystem());
		ChitStatus status = ChitStatus.valueOf(MapUtils.getString(chit,"status"));
		if (status != ChitStatus.activate) {
			String num = MapUtils.getString(chit, "num");
			throw new BusinessException("代金券[" + num + "]状态为"+ status.getText() + "，不能回收");
		}

		// 激活(状态改为已回收)
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", ChitStatus.returns);
		entityService.updateById(MzfEntity.CHIT, Integer.toString(chitId),field, user);

		// 记录日志
		int transId = transactionService.findTransId(MzfEntity.CHIT, Integer.toString(chitId), user);
		logService.createLog(transId, MzfEntity.CHIT, Integer.toString(chitId),"回收", TargetType.chit, chitId, null, user);
	}
	
	 //退货时，退回回收的代金券
	public void returnBackChit(int chitId, IUser user) 	throws BusinessException{
		// 验证有效性(状态为已激活)
		Map<String, Object> chit = entityService.getById(MzfEntity.CHIT_VIEW,Integer.toString(chitId), user.asSystem());
		ChitStatus status = ChitStatus.valueOf(MapUtils.getString(chit,"status"));
		if (status != ChitStatus.returns) {
			String num = MapUtils.getString(chit, "num");
			throw new BusinessException("代金券[" + num + "]状态为"+ status.getText() + "，不能退还");
		}

		//退回代金券（状态为已激活）
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", ChitStatus.activate);
		entityService.updateById(MzfEntity.CHIT, Integer.toString(chitId),field, user);

		// 记录日志
		int transId = transactionService.findTransId(MzfEntity.CHIT, Integer.toString(chitId), user);
		logService.createLog(transId, MzfEntity.CHIT, Integer.toString(chitId),"退回客户", TargetType.chit, chitId, null, user);
	}

	// 作废
	public void obsoleteChit(int chitId, IUser user) throws BusinessException {
		// 验证有效性(状态为已未发放)
		Map<String, Object> chit = entityService.getById(MzfEntity.CHIT_VIEW,Integer.toString(chitId), user.asSystem());
		ChitStatus status = ChitStatus.valueOf(MapUtils.getString(chit,"status"));
		String num = "";
		if (status != ChitStatus.normal) {
		    num = MapUtils.getString(chit, "num");
			throw new BusinessException("代金券[" + num + "]状态为"+ status.getText() + "，不能作废");
		}

		// 激活(状态改为已作废)
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", ChitStatus.invalid);
		entityService.updateById(MzfEntity.CHIT, Integer.toString(chitId),
				field, user);

		// 记录日志
		int transId = transactionService.findTransId(MzfEntity.CHIT, Integer.toString(chitId), user);
		logService.createLog(transId, MzfEntity.CHIT, Integer.toString(chitId),"作废", TargetType.chit, chitId, null, user);
		//记录操作日志
		businessLogService.log("代金券作废", "代金券条码为：" + num, user);
	}

	// 冻结
	public void lockChit(int chitId, IUser user) throws BusinessException {
		// 验证有效性(状态为已激活)
		Map<String, Object> chit = entityService.getById(MzfEntity.CHIT_VIEW,Integer.toString(chitId), user.asSystem());
		ChitStatus status = ChitStatus.valueOf(MapUtils.getString(chit,"status"));
		String num = "";
		if (status != ChitStatus.activate) {
			num = MapUtils.getString(chit, "num");
			throw new BusinessException("代金券[" + num + "]状态为"+ status.getText() + "，不能冻结");
		}

		// 激活(状态改为已冻结)
		Map<String, Object> field = new HashMap<String, Object>();
		field.put("status", ChitStatus.freeze);
		entityService.updateById(MzfEntity.CHIT, Integer.toString(chitId),
				field, user);

		// 记录日志
		int transId = transactionService.findTransId(MzfEntity.CHIT, Integer.toString(chitId), user);
		logService.createLog(transId, MzfEntity.CHIT, Integer.toString(chitId),"冻结", TargetType.chit, chitId, null, user);
		//记录操作日志
		businessLogService.log("代金券冻结", "代金券条码为：" + num, user);
	}
}

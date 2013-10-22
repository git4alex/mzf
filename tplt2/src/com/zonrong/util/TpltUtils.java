package com.zonrong.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;

import com.zonrong.core.dao.OrderBy;
import com.zonrong.core.dao.OrderBy.OrderByDir;
import com.zonrong.core.exception.BusinessException;
import com.zonrong.entity.service.EntityService;
import com.zonrong.metadata.EntityMetadata;
import com.zonrong.metadata.FieldMetadata;
import com.zonrong.metadata.MetadataConst.DataType;
import com.zonrong.system.service.BizCodeService;

/**
 * date: 2011-1-30
 * 
 * version: 1.0 commonts: ......
 */
public class TpltUtils {
	public static Logger logger = Logger.getLogger(TpltUtils.class);

	private final static String[] CN_Digits = { "零", "壹", "貳", "叁", "肆", "伍",
			"陆", "柒", "捌", "玖", };

	/**
	 * 将阿拉伯数字转成中文大写
	 * 
	 * @param moneyValue
	 *            要转的数字
	 * @return
	 */
	public static String convertDigits(String moneyValue) {
		// 使用正则表达式，去除前面的零及数字中的逗号
		String value = moneyValue.replaceFirst("^0+", "");
		value = value.replaceAll(",", "");
		// 分割小数部分与整数部分
		int dot_pos = value.indexOf('.');
		String int_value;
		String fraction_value;
		if (dot_pos == -1) {
			int_value = value;
			fraction_value = "00";
		} else {
			int_value = value.substring(0, dot_pos);
			fraction_value = value.substring(dot_pos + 1, value.length())
					+ "00".substring(0, 2);// 也加两个0，便于后面统一处理
		}

		int len = int_value.length();
		if (len > 16) {
			return "要转换的数字过大";
		}
		StringBuffer cn_currency = new StringBuffer();
		String[] CN_Carry = new String[] { "", "万", "亿", "万" };
		// 数字分组处理，计数组数
		int cnt = len / 4 + (len % 4 == 0 ? 0 : 1);
		// 左边第一组的长度
		int partLen = len - (cnt - 1) * 4;
		String partValue = null;
		boolean bZero = false;// 有过零
		String curCN = null;
		for (int i = 0; i < cnt; i++) {
			partValue = int_value.substring(0, partLen);
			int_value = int_value.substring(partLen);
			curCN = Part2CN(partValue, i != 0 && !"零".equals(curCN));
			// 若上次为零，这次不为零，则加入零
			if (bZero && !"零".equals(curCN)) {
				cn_currency.append("零");
				bZero = false;
			}
			if ("零".equals(curCN))
				bZero = true;
			// 若数字不是零，加入中文数字及单位
			if (!"零".equals(curCN)) {
				cn_currency.append(curCN);
				cn_currency.append(CN_Carry[cnt - 1 - i]);
			}
			// 除最左边一组长度不定外，其它长度都为4
			partLen = 4;
			partValue = null;
		}
		cn_currency.append("元");
		// 处理小数部分
		int fv1 = Integer.parseInt(fraction_value.substring(0, 1));
		int fv2 = Integer.parseInt(fraction_value.substring(1, 2));
		if (fv1 + fv2 == 0) {
			cn_currency.append("整");
		} else {
			cn_currency.append(CN_Digits[fv1]).append("角");
			cn_currency.append(CN_Digits[fv2]).append("分");
		}
		return cn_currency.toString();
	}

	private static String Part2CN(String partValue, boolean bInsertZero) {
		// 使用正则表达式，去除前面的0
		partValue = partValue.replaceFirst("^0+", "");
		int len = partValue.length();
		if (len == 0)
			return "零";
		StringBuffer sbResult = new StringBuffer();
		int digit;
		String[] CN_Carry = new String[] { "", "拾", "佰", "仟" };
		for (int i = 0; i < len; i++) {
			digit = Integer.parseInt(partValue.substring(i, i + 1));
			if (digit != 0) {
				sbResult.append(CN_Digits[digit]);
				sbResult.append(CN_Carry[len - 1 - i]);
			} else {
				// 若不是最后一位，且下不位不为零，追加零
				if (i != len - 1
						&& Integer.parseInt(partValue.substring(i + 1, i + 2)) != 0)
					sbResult.append("零");
			}
		}
		if (bInsertZero && len != 4)
			sbResult.insert(0, "零");
		return sbResult.toString();
	}

	public static boolean isContains(Enum[] c, Enum e) {
		if (c == null || e == null)
			return false;

		for (Enum c1 : c) {
			if (c1 == e) {
				return true;
			}
		}

		return false;
	}

	public static <T> T[] removeDuplicate(T[] objs, Class<T> componentType) {
		if (ArrayUtils.isEmpty(objs)) {
			return objs;
		}
		Set<Object> set = new HashSet<Object>();
		set.addAll(Arrays.asList(objs));
		T[] t = (T[]) Array.newInstance(componentType, 0);
		return set.toArray(t);
	}

	public static Date getDate(String billDateStr, String dateFormat)
			throws BusinessException {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Date billDate = sdf.parse(billDateStr);
			return billDate;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);

			throw new BusinessException("时间格式有误");
		}
	}

	public static OrderBy refactorOrderByParams(Map<String, Object> parameter)
			throws BusinessException {
		try {
			String sort = MapUtils.getString(parameter, "sort");
			String dir = MapUtils.getString(parameter, "dir");

			if (StringUtils.isBlank(sort)) {
				return null;
			}

			String[] fields = StringUtils.split(sort, ",");
			if (ArrayUtils.isEmpty(fields)) {
				return null;
			}

			OrderByDir orderByDir = null;
			if (OrderByDir.asc.toString().equalsIgnoreCase(dir)
					|| OrderByDir.desc.toString().equalsIgnoreCase(dir)) {

				orderByDir = OrderByDir.valueOf(dir.toLowerCase());
			}

			return new OrderBy(fields, orderByDir);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			// throw new BusinessException("parameter error");
		}
		return null;
	}

	public static List<Map<String, Object>> refactorQueryParams(Map parameter)
			throws BusinessException {
		try {
			String params = MapUtils.getString(parameter, "queryParams", "[]");
			List<Map<String, Object>> list = new ObjectMapper().configure(
					Feature.ALLOW_UNQUOTED_FIELD_NAMES, true).readValue(params,
					List.class);
			if (parameter != null) {
				parameter.remove("queryParams");
				Iterator<String> it = parameter.keySet().iterator();
				while (it.hasNext()) {
					Object key = it.next();
					Object value = parameter.get(key);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(EntityService.FIELD_CODE_KEY, key);
					map.put(EntityService.OPERATOR_KEY, "=");
					map.put(EntityService.VALUE_KEY, value);

					list.add(map);
				}
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw new BusinessException("parameter error");
		}
	}

	public static List<Map<String, Object>> refactorQueryParams(
			HttpServletRequest request) throws BusinessException {
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			Map<String, Object> params = request.getParameterMap();
			for (Iterator<Entry<String, Object>> it = params.entrySet()
					.iterator(); it.hasNext();) {
				Entry<String, Object> entry = it.next();
				String key = entry.getKey();
				Object value = entry.getValue();

				Map<String, Object> map = new HashMap<String, Object>();
				map.put(EntityService.FIELD_CODE_KEY, key);
				map.put(EntityService.OPERATOR_KEY, "in");
				map.put(EntityService.VALUE_KEY, value);
				list.add(map);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			throw new BusinessException("parameter error");
		}
	}

	public static List<Map<String, Object>> readExcel(File file, int sheetIndex, EntityMetadata entityMetadata, boolean isCheck, Integer returnRows) throws BusinessException {
		if(!file.getName().endsWith("xls") && !file.getName().endsWith("xlsx")){
			throw new BusinessException("要读取的文件非Excel文件");
		}
		List<FieldMetadata> fieldList = entityMetadata.getFieldList();
		for (FieldMetadata fieldMetadata : fieldList) {
			 if(fieldMetadata.getCode().equals(entityMetadata.getPkCode())){
				 fieldList.remove(fieldMetadata);
				 break;
			 }
		}
		
		
		InputStream inputStream = null;
		List<Map<String, Object>> datas = new ArrayList<Map<String,Object>>();
		try {
			inputStream = new FileInputStream(file);
			Workbook book = null;
			     //excel 2003
			if(file.getName().endsWith("xls")){
				book = new HSSFWorkbook(inputStream);
			}else{
				//excel 2007
				book = new XSSFWorkbook(inputStream);
			}
			if(book.getNumberOfSheets()-1 < sheetIndex){
				throw new BusinessException("sheet" + sheetIndex + "不存在");
			}
			Sheet sheet = book.getSheetAt(sheetIndex);
			if(isCheck){
				Iterator<Row> rows = sheet.iterator();
			    while(rows.hasNext()){
			    	Row row = rows.next();
			    	Map<String, Object> data = new HashMap<String, Object>();
					if(row == null){
						throw new BusinessException(sheet.getSheetName() + "的第 "+ row.getRowNum() + " 行没有数据");
					}
					int cellIndex = 0;
					for (FieldMetadata metadata : fieldList) {
						// data.put(metadata.getCode(), row.getCell(cellIndex));
						 if(metadata.getBizTypeCode() != null){
							 String bizText = BizCodeService.getBizName(metadata.getBizTypeCode(), row.getCell(cellIndex).toString());
							// data.put(metadata.getCode()+"Text", bizText);
						 }
						 cellIndex ++;
					}
					//datas.add(data);
			    }
			}
			
			if (returnRows != null) {				
				for(int i = 1;i <= returnRows; i++){
					Map<String, Object> data = new HashMap<String, Object>();
					Row row = sheet.getRow(i);
					if(row == null){
						throw new BusinessException(sheet.getSheetName() + "的第" + i + "行没有数据");
					}
					int cellIndex = 0;
					for (FieldMetadata metadata : fieldList) {
						 data.put(metadata.getCode(), getCellValue(metadata,row.getCell(cellIndex)));
						 if(metadata.getBizTypeCode() != null){
							 String bizText = BizCodeService.getBizName(metadata.getBizTypeCode(), row.getCell(cellIndex)!=null?row.getCell(cellIndex)+"":"");
							 data.put(metadata.getCode()+"Text", bizText);
						 }
						 cellIndex ++;
					}
					datas.add(data);
					
				}
			}else{
				int rowLength = sheet.getPhysicalNumberOfRows();
				for(int i = 1;i < rowLength;i++){
			    	Row row = sheet.getRow(i);
			    	Map<String, Object> data = new HashMap<String, Object>();
					if(row == null){
						throw new BusinessException(sheet.getSheetName() + "的第 "+ row.getRowNum() + " 行没有数据");
					}
					int cellIndex = 0;
					for (FieldMetadata metadata : fieldList) {
						 data.put(metadata.getCode(), getCellValue(metadata,row.getCell(cellIndex)));
						 if(metadata.getBizTypeCode() != null){
							 String bizText = BizCodeService.getBizName(metadata.getBizTypeCode(), row.getCell(cellIndex)!=null?row.getCell(cellIndex)+"":"");
							 data.put(metadata.getCode()+"Text", bizText);
						 }
						 cellIndex ++;
					}
					datas.add(data);
			    }
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new BusinessException(file.getName()+"不存在");
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try {
				if(inputStream != null){
					inputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return datas;
	}
	private static Object getCellValue(FieldMetadata field,Cell cell){
		if(cell == null){
			return "";
		}
		if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
			if(field.getDataType().equals("float")){
				return cell.getNumericCellValue();
			}else if(field.getDataType().equals("timestamp")){
				return cell.getDateCellValue();
			}else {
				return (int)cell.getNumericCellValue();
			}
		}else{
			return cell.getStringCellValue();
		}
	}
	
	public static DataType getSystemDataType(int sqlType) {			
		if (sqlType == Types.FLOAT || sqlType == Types.REAL
				|| sqlType == Types.DOUBLE || sqlType == Types.NUMERIC
				|| sqlType == Types.DECIMAL) { // 数字（小数）

			return DataType.DATATYPE_FLOAT;
		} else if (sqlType == Types.TINYINT || sqlType == Types.SMALLINT
				|| sqlType == Types.INTEGER || sqlType == Types.BIGINT) { // 数字（整数）

			return DataType.DATATYPE_INTEGER;
		} else if (sqlType == Types.BOOLEAN) {
			return DataType.DATATYPE_BOOLEAN;
		} else if (sqlType == Types.DATE || sqlType == Types.TIME
				|| sqlType == Types.TIMESTAMP) { // 日期

			return DataType.DATATYPE_TIMESTAMP;
		} else {

			return DataType.DATATYPE_STRING;
		}
	}
	
	public static void main(String[] args) {
		// Integer[] a = new Integer[]{1, 1, 2};
		// Integer[] a1 = removeDuplicate(a, Integer.class);
		// for (Integer i : a1) {
		// System.out.println(i);
		// }

		//double moneyValue = 45367.09;
		//System.out.println(convertDigits("0.0"));
		List<String> columns = new ArrayList<String>();
		columns.add("名称");
		columns.add("条码");
		//columns.add("面值");
		//columns.add("所属部门");
		//columns.add("有效期");
		//columns.add("备注");
		File file = new File("E:/test.txt");
		try {
//			List<Map<String, Object>> datas = readExcel(file,0,columns, true, 1);
//			System.out.println(datas);
//			System.out.println(datas.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

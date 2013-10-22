package com.zonrong.core.sql.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Repository;

import com.zonrong.core.exception.BusinessException;


/**
 * version: 1.0
 * commonts: ......
 */
@Repository
public class XmlSqlProvider {
	private static Logger logger = Logger.getLogger(XmlSqlProvider.class);

	private static Map<String, String> xmlSqlMap = new HashMap<String, String>();
	private static List<Map<String, String>> sqlTitleList = new ArrayList<Map<String,String>>();

	@PostConstruct
	public void load() {
		List<File> files = null;
		try {
			String path = this.getClass().getClassLoader().getResource("/").toURI().getPath() + "sql";
//			String path = this.getClass().getClassLoader().getSystemResource("").getPath() + "sql";
			files = getSqlFiles(path);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		if (CollectionUtils.isEmpty(files)) {
			return;
		}

		try {
			for (File file : files) {
				loadFile(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

	}

	private void loadFile(File file) throws DocumentException, BusinessException {
		if (logger.isInfoEnabled()) {
			logger.info("load sql...");
		}
		SAXReader reader = new SAXReader();
		Document doc = reader.read(file);
		Element root = doc.getRootElement();
		String namespace = root.attributeValue("namespace");
		if (StringUtils.isBlank(namespace)) {
			throw new BusinessException("no namespace in " + file.getName());
		}
		Element foo;
		for (Iterator i = root.elementIterator("sql"); i.hasNext();) {
			foo = (Element) i.next();
			String name = foo.attributeValue("name");
			String title = foo.attributeValue("title");
			String text = foo.getText();

			if (StringUtils.isBlank(name) ||
					StringUtils.isBlank(title) ||
					StringUtils.isBlank(text)) {
				throw new BusinessException("resolver sql xml failed in " + file.getName());
			}

			String[] names = new String[]{name};
			String[] titles = new String[]{title};
			if (name.indexOf(",") > 0) {
				names = name.split(",");
				titles = title.split(",");
			}
			if (titles.length != names.length) {
				throw new BusinessException("name match title failed in " + file.getName());
			}

			for (int j = 0; j < names.length; j++) {
				String key = namespace + "." + StringUtils.trim(names[j]);
				xmlSqlMap.put(key, text);
				Map<String, String> map = new HashMap<String, String>();
				map.put("key", key);
				map.put("title", StringUtils.trim(titles[j]));
				sqlTitleList.add(map);
//				if (logger.isDebugEnabled()) {
//					logger.debug("\ntitle:" + StringUtils.trim(titles[j]) + "; sqlKey:" + key + "; sql: " + text);
//				}
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("load sql finished, total count is " + xmlSqlMap.size());
		}
	}

	private List<File> getSqlFiles(String path) {
		List<File> list = new ArrayList<File>();
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				List<File> clist = getSqlFiles(f.getPath());
				list.addAll(clist);
			}
			if (f.isFile() && f.getName().endsWith(".xml")) {
				list.add(f);
                if (logger.isInfoEnabled()) {
                    logger.info("load sql map file: " + f.getAbsolutePath());
                }
			}
		}

		return list;
	}

	public String getSql(String key) throws BusinessException {
		load();
		String sql = xmlSqlMap.get(key);
		if (sql == null) {
			throw new BusinessException("can't found sql by " + key);
		}
		return sql;
	}

	public String getSqlKey(String namespace, String sqlName) {
		String key = namespace + "." + sqlName;
		return key;
	}

	public static List<Map<String, String>>	listSqlTitle() {
		return sqlTitleList;
	}

	public static void main(String[] args) {
		XmlSqlProvider provider = new XmlSqlProvider();
		provider.load();

		Iterator<String> it = xmlSqlMap.keySet().iterator();
	}
}


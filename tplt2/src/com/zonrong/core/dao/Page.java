package com.zonrong.core.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.zonrong.metadata.MetadataConst;

public class Page extends HashMap{
	public static final int DEFAULT_PAGINATION_ROWS = 20;
	public static final String IS_PAGE = "isPage";
	
	private final String offset_key = "start";
	private final String pageSize_key = "limit";
	private final String totalCount_key = "totalCount";
	private final String items_key = MetadataConst.ITEMS_ROOT;

	/**
	 * 构造方法
	 * 
	 * @param items
	 *            数据集合对象
	 * @param totalCount
	 *            总记录数
	 * @param pageSize
	 *            每页显示条数
	 * @param startRow
	 *            开始行数
	 */
	public Page(List items, int totalCount, int pageSize,
			int startIndex) {
		setPageSize(pageSize);
		setTotalCount(totalCount);
		setItems(items);
		setOffset(startIndex);
	}

	/**
	 * 构造方法
	 * 
	 * @param pageSize
	 *            每页显示条数
	 * @param startRow
	 *            开始行数
	 */
	public Page(int start, int limit) {
		setPageSize(limit);
		setOffset(start);
	}
	
	/**
	 * 构造方法
	 * 从 Map 中取得当前页索引和页大小，对应的键值为 "pageNo"、"pageSize"
	 * 如果没有取到，则设置默认值
	 * 
	 * @param parameters
	 */
	public Page(Map parameters) {
		int pageSize = MapUtils.getIntValue(parameters, pageSize_key, DEFAULT_PAGINATION_ROWS);
		this.setPageSize(pageSize);
		
		int offset = MapUtils.getIntValue(parameters, offset_key, 0);
		this.setOffset(offset);
	}
	
	public List getItems() {
		return (List)this.get(items_key);
	}

	public void setItems(List items) {
		this.put(items_key, items);
	}

	public int getPageSize() {
		Integer pageSize = (Integer) this.get(pageSize_key);
		return pageSize != null? pageSize:DEFAULT_PAGINATION_ROWS;
	}

	public void setPageSize(int pageSize) {
		this.put(pageSize_key, pageSize);
	}

	public int getTotalCount() {
		Integer totalCount = (Integer) this.get(totalCount_key);
		return totalCount != null? totalCount:0;
	}

	public void setTotalCount(int totalCount) {
		this.put(totalCount_key, totalCount);
	}

	public int getOffSet() {
		Integer curPageIndex = (Integer) this.get(offset_key);
		return curPageIndex != null? curPageIndex:0;
	}

	public void setOffset(int curPageIndex) {
		this.put(offset_key, curPageIndex);
	}

	/**
	 * 获取前一页开始行索引
	 * 
	 * @return
	 */
	public int getPrePageIndex() {
		int preIndex = this.getOffSet() - this.getPageSize();
		if(preIndex<0){
			return 0;
		}
		return preIndex;
	}
	
	/**
	 * 获取下一页开始行索引
	 * 
	 * @return
	 */
	public int getNextPageIndex() {
		int nextIndex = this.getOffSet() + this.getPageSize();
		if(nextIndex>=this.getTotalCount()){
			return this.getOffSet();
		}
		return nextIndex;
	}

	/**
	 * 获取最后一页开始行索引
	 * 
	 * @return
	 */
	public int getLastPageIndex() {
		int lastIndex=((this.getTotalCount() + this.getPageSize() - 1)/this.getPageSize() - 1) * this.getPageSize();
		return lastIndex;
	}

	/**
	 * 获取第一页开始行索引
	 * 
	 * @return
	 */
	public int getFirstPageIndex() {
		return 0;
	}
	
	/**
	 * 获取总条目数
	 * 
	 * @return
	 */
	public int getTotalPageCount(){
		int totalPage=this.getTotalCount()/this.getPageSize();
		int residual=this.getTotalCount()%this.getPageSize();
		if(residual>0){
			totalPage++;
		}
		return totalPage;
	}
	
	/**
	 * 获取某页的起始行索引
	 * 
	 * @return
	 */
	public int getIndexByPage(int pageNo){
		return (pageNo-1)*this.getPageSize();
	}
}

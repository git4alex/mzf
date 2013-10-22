function ajaxFailure(resp) {
	Ext.Msg.show({
				title : '错误',
				msg : 'status:' + resp.status + '<br/> statusText:'
						+ resp.statusText,
				buttons : Ext.Msg.OK,
				fn : Ext.emptyFn,
				icon : Ext.Msg.ERROR
			});
}

/**
 * 判断对象是否空对象
 * 
 * @param obj
 * @returns {Boolean}
 */
function isNotEmptyOfObject(obj) {
	if (typeof obj == 'object') {
		for (var name in obj) {
			if (name) {
				return true;
			}
		}
	}
	return false;
}
/**
 * 判断数字类型的字段
 * 
 * @param value
 * @returns {Boolean}
 */
function isEmptyOfNum(value) {
	if (value == undefined) {
		return true;
	}
	if (value == null) {
		return true;
	}
	if (value === "") {
		return true;
	}
	return false
}
/**
 * 根据字段包装成查询对象
 * 
 * @param fieldName
 * @param fieldValue
 * @param operate
 * @returns jsonData
 */
function wrapParam(fieldName, operate, fieldValue) {
	if (operate && operate.toUpperCase() == "LIKE") {
		fieldValue = "%" + fieldValue + "%";
	}
	return {
		field : fieldName,
		operate : operate,
		value : fieldValue
	};
}
/**
 * 根据date类型字段包装成查询对象
 * 
 * @param fieldName
 * @param operate
 * @param fieldValue
 * @param format
 * @returns jsonData
 */
function wrapDateParam(fieldName, operate, fieldValue, format) {
	format = format || "Y-m-d";
	if (fieldValue) {
		fieldValue = fieldValue.format(format);
	}
	return {
		field : fieldName,
		operate : operate,
		value : fieldValue
	};
}

/**
 * 根据 pagingBar.pageSize 分页查询
 * 
 * @param gridpanel
 * @param isReload
 */
function doLoadForGrid(grid, isReload, func) {
	var pageSize = grid.getBottomToolbar().pageSize;
	var data = {
		params : {
			start : 0,
			limit : pageSize
		}
	};
	if (func) {
		data.callback = func;
	}
	if (isReload) {
		grid.getStore().reload(data);
	} else {
		grid.getStore().load(data);
	}
}

/**
 * 根据 pagingBar.pageSize 分页查询
 * doLoadForView(view, 'pageId', false, func)
 * @param dataview
 * @param pageId
 * @param isReload
 * @param func
 */
function doLoadForView(view, pageId, isReload, func) {
	var page = Ext.getCmp(pageId);
	if (!page) {
		Ext.Msg.alert('提示', '没有分页组件')
		return;
	}
	var pageSize = page.pageSize;
	var data = {
		params : {
			start : 0,
			limit : pageSize
		}
	};
	if (func) {
		data.callback = func;
	}
	if (isReload) {
		view.getStore().reload(data);
	} else {
		view.getStore().load(data);
	}
}
/**
 * 获取store定义字段
 * 
 * @param store
 * @returns {Array}
 */
function getStoreFields(store) {
	var arr = [];
	if (store) {
		var fields = store.fields;
		if (!Ext.isEmpty(fields)) {
			var reg = /^[a-zA-Z]+$/;
			fields.each(function(item, index, length) {
						if (item && (!reg.test(item.text))) {
							var data = {
								code : item.name,
								title : item.text
							};
							if(item.permissionId && od.hasPermission(item.permissionId)){
							   arr.push(data);
							}
							if(!item.permissionId){
							   arr.push(data);
							}
							
						}
					});
		}
	}
	return arr;
}

/**
 * 打印功能
 */
function printBody(bodyId) {
	var html = Ext.get(bodyId).dom.innerHTML;
	var frame = document.createElement('iframe');
	frame.setAttribute('id', 'printframe');
	frame.setAttribute('name', 'printframe');
	frame.setAttribute('style', "display:none;");
	document.body.appendChild(frame);

	frame.contentWindow.document.open();
	var nb = '<br/><br/><br/>';
	frame.contentWindow.document.write(nb + html);
	frame.contentWindow.document.close();
	frame.contentWindow.document.execCommand('print');
}

/**
 * 导出Excel 如： exportExcel('/entity/org', null, [{code:text, title:'名称',
 * bizCode:...}, ...]);
 */
function exportExcelOfBasic(url, params, cols, readOnly) {
	Ext.Ajax.request({
				url : url,
				params : params,
				headers : {
					accept : 'application/msexcel'
				},
				method : 'GET',
				success : function(resp) {
					var result = Ext.decode(resp.responseText);
					var downloadUrl = '/entity/download/' + result.id
							+ '?cols=' + Ext.util.JSON.encode(cols)+'&readOnly= ' +readOnly;
					document.getElementById('x-history-frame').src = downloadUrl;
				}
			});
}

/**
 * 报表导出
 * 
 * @param url
 * @param params
 * @param cols
 */
function exportExcel(url, params, cols) {
	if (params) {
		if (!isEmptyOfNum(params.start)) {
			delete params.start;
		}
		params.limit && (delete params.limit);
	}
	
	exportExcelOfBasic(url, params, cols, false);
}
/**
 * 目前系统报表功能
 * 
 * @param view
 * @param btn
 */
function exportExcelAction(view, btn) {
	if (view) {
		var arr = getStoreFields(view.getStore());
		var data = view.getStore().baseParams;
		exportExcel(view.getStore().url, data, arr);

		if (btn) {
			btn.disable();
			btn.enable.defer(3000, btn);
		}
	}
}

/**
 * 导出标签
 * 
 * @param url
 * @param params
 * @param cols
 */
function exportLabel(url, params, cols) {
	var data = {
		'queryParams' : Ext.encode([{
					field : params.key,
					operate : 'in',
					value : params.value
				}])
	};
	exportExcelOfBasic(url, data, Ext.decode(cols), true);
}
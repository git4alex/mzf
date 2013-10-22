Ext.ns('Ext.ux.grid');

Ext.ux.grid.GroupGridPanel = Ext.extend(Ext.grid.GridPanel, {

	/**
	 * @cfg {String} 得到数据的URL
	 */
	dataUrl : "",
	loadMask : true,
	singleSelect : true,
	pageSize : 20,
	autoLoad : this.dataUrl,
	height : 500,
	storeFields : [],
	defaultSort : [],
	view : new Ext.grid.GroupingView({
		forceFit : true,
		groupTextTpl : '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "条" : "条"]})'
	}),
	animCollapse : false,
	viewConfig : {
		autoFill : true,
		forceFit : true
	},
	groupField : '',
	requestMethod : 'GET',
	initComponent : function() {

		// 如果存在这个URl，那么就生成默认的Store
		if (this.dataUrl) {

			var storeFields = this.storeFields;
			Ext.each(this.columns, function(v) {
						storeFields.push({
									name : v.dataIndex
								});
					});

			this.sm = new Ext.grid.CheckboxSelectionModel({
						singleSelect : this.singleSelect
					});

			this.cm = new Ext.grid.ColumnModel(new Array(this.sm)
					.concat(this.columns));

			this.store = new Ext.data.GroupingStore({
						proxy : new Ext.data.HttpProxy({
									url : this.dataUrl,
									method : this.requestMethod
								}),
						reader : new Ext.data.JsonReader({
									root : 'root',
									totalProperty : 'totalCount',
									id : 'id',
									fields : this.storeFields
								}),
						groupField : this.groupField,
						sortInfo : {
							field : this.groupField,
							direction : "ASC"
						}
					});
		};
		// 自动加载
		if (this.store)
			this.store.load({
						params : {
							start : 0,
							limit : this.pageSize
						}
					});

		this.bbar = this.bbar || new Ext.PagingToolbar({
					pageSize : 20,
					store : this.store,
					displayInfo : true,
					displayMsg : '第{0} 到 {1} 条数据 共{2}条',
					emptyMsg : "没有数据",
					items : []
				});

		Ext.ux.grid.GroupGridPanel.superclass.initComponent.call(this);
	},
	getSelectedRows : function(cfg) {
		cfg = cfg || {};
		Ext.applyIf(cfg, {
					multiSelect : true,
					alertType : 'suggest',
					err1 : '请选择您所要操作的记录行',
					err2 : '一次只能操作一条记录'
				});

		if (this.selModel.hasSelection()) {

			var selectedNum = this.selModel.getCount();

			if (!cfg.multiSelect && selectedNum > 1) {
				Ext.MessageBox.alert("操作提示", cfg.err2);

			} else {
				if (cfg.multiSelect) {
					return this.selModel.getSelections();
				} else {
					return this.selModel.getSelected();
				}
			}
		} else {
			Ext.MessageBox.alert("操作提示", cfg.err1);
		}
	},
	/**
	 * 返回选择的单列ID，如果选择多列将会进行错误提示
	 */
	getSelectedId : function() {
		var rs = this.getSelectedRows({
					multiSelect : false
				});
		if (rs) {
			return rs.id;
		}
	},
	/**
	 * 返回选择的列的ID列表，如果没有选择会进行错误提示
	 */
	getSelectionIds : function() {
		var rs = this.getSelectedRows({
					multiSelect : true
				});
		if (rs) {
			var ids = [];
			for (var i = 0; i < rs.length; i++) {
				ids[ids.length] = rs[i].id;
			}
			return ids;
		}
		return [];
	},
	/**
	 * 返回选择列的ID字符串，以逗号分割，如果没有选择会进行错误提示
	 */
	getIdsFromSelectedRows : function() {
		if (this.getSelectionIds()) {
			return this.getSelectionIds().join(',');
		}

	}
});
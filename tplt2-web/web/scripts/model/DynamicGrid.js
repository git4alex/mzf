DynamicGrid = Ext.extend(Ext.grid.GridPanel, {
			frame : false,
			border : false,
			singleSelect : true,
			useCheckbox : false,
			dataUrl : '',
			fieldUrl : '',
			pageSize : 20,
			layout : 'column',
			stripeRows : true,
			autoWidth : true,
			stateful : true,
			autoDestroy : true,
			trackMouseOver : true,
			autoLoad : this.fieldUrl,
			viewConfig : {
				autoFill : true,
				forceFit : true
			},
			fieldConfig : null,
			initComponent : function() {

				if (this.fieldUrl) {
					this.loadConfig();
				}
				this.columns = this.getColumns() || [];
				// 是否是多选择框
				if (this.useCheckbox) {
					this.sm = new Ext.grid.CheckboxSelectionModel({
								singleSelect : this.singleSelect
							});
					this.cm = new Ext.grid.ColumnModel(new Array(this.sm)
							.concat(this.columns));
				} else
					this.cm = new Ext.grid.ColumnModel(this.columns);
				this.store = this.getDataStore();
				this.bbar = this.getPageBar();
				DynamicGrid.superclass.initComponent.call(this);
			},

			loadConfig : function() {
				var config = {};
				Ext.Ajax.request({
							url : this.fieldUrl,
							async : false,
							success : function(resp) {
								config = Ext.decode(resp.responseText);
							},
							failure : function(resp) {
								alert('DynamicGrid加载配置失败！');
							}
						});
				this.fieldConfig = config;
			},

			doLoad : function() {
				if (this.store) {
					this.store.load({
								params : {
									start : 0,
									limit : this.pageSize
								}
							});
				}
			},
			getPageBar : function() {
				return new Ext.PagingToolbar({
							pageSize : 20,
							store : this.store,
							displayInfo : true,
							displayMsg : '第{0} 到 {1} 条数据 共{2}条',
							emptyMsg : "没有数据"
						});
			},
			getDataStore : function() {
				return new Ext.data.Store({
							autoDestroy : true,
							proxy : new Ext.data.HttpProxy({
										url : this.dataUrl
									}),
							reader : new Ext.data.JsonReader({
										root : 'root',
										totalProperty : 'totalCount',
										fields : this.getFields()
									}),
							remoteSort : true,
							autoLoad : false
						});
			},
			getColumns : function() {
				var fieldCfg = new Array();
				if (this.fieldConfig) {
					if (Ext.isArray(this.fieldConfig.properties)) {
						var properties = this.fieldConfig.properties;
						Ext.each(properties, function(prop) {
									if (Ext.isEmpty(prop.code))
										return;
									var field = {
										id : prop.code,
										dataIndex : prop.code,
										hidden : prop.hidden,
										header : prop.name || 'property',
										width : prop.length || 150,
										editor : prop.editor
									}
									Ext.apply(field, FormUtil.getGridCol(
													prop.type, prop.data))
									fieldCfg.push(field);
								});
					}
				}
				return fieldCfg;
			},

			// private use to fields of getDataStore
			getFields : function() {
				var fieldCfg = new Array();
				if (this.fieldConfig) {
					if (Ext.isArray(this.fieldConfig.properties)) {
						var properties = this.fieldConfig.properties;
						Ext.each(properties, function(prop) {
									if (Ext.isEmpty(prop.code))
										return;
									var field = {
										name : prop.code
									}
									fieldCfg.push(field);
								});
					}
				}
				return fieldCfg;
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
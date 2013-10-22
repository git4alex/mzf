EntityFieldGrid = Ext.extend(Ext.grid.GridPanel, {
	region : 'center',
	frame : false,
	border : false,
	autoDestroy : true,
	trackMouseOver : true,
	stripeRows : true,
	stateful : true,
	viewConfig : {
		autoFill : true,
		forceFit : true
	},
	margins : '0 5 5 5',
	autoExpandColumn : 'name',
	autoRead : false,
	rowEditor : null,
	singleSelect : true,
	loadMask : {
		msg : '加载数据中，请稍候...'
	},
	initComponent : function() {

		if (!this.rowEditor)
			this.rowEditor = new Ext.ux.grid.RowEditor({
				saveText : '保存',
				cancelText : '取消'
			});

		if (this.rowEditable) {
			Ext.apply(this, {
				plugins : this.rowEditor
			});
		}
		this.columns = [new Ext.grid.RowNumberer(), {
			header : 'id',
			hidden : true,
			dataIndex : 'id'
		}, {
			header : '字段中文名',
			dataIndex : 'name',
			editor : {
				xtype : 'textfield',
				allowBlank : false
			}
		}, {
			header : 'entityId',
			dataIndex : 'entityId',
			hidden : true
		}, {
			header : '字段名',
			dataIndex : 'columnName',
			editor : {
				xtype : 'textfield',
				allowBlank : false
			}
		}, {
			header : 'code',
			dataIndex : 'code',
			editor : {
				xtype : 'textfield',
				allowBlank : false
			}
		}, {
			header : '数据类型',
			dataIndex : 'dataType',
			editor : {
				xtype : 'combo',
				triggerAction : 'all',
				typeAhead : true,
				store : {
					xtype : 'jsonstore',
					fields : ['text', 'value'],
					data : od.AppInstance.appConfig.bizCode.DATA_TYPE
				},
				displayField : 'text',
				valueField : 'value',
				editable : false,
				mode : 'local'
			},
			renderer : function(val) {
				var items = od.AppInstance.appConfig.bizCode.DATA_TYPE;
				for (i = 0; i < items.length; i++) {
					var item = items[i];
					if (item.value == val) {
						return item.text;
					}
				}
				return val;
			}
		}, {
			header : '是否主键',
			dataIndex : 'primaryKey',
			renderer : doRenderer,
			editor : {
				xtype : 'combo',
				triggerAction : 'all',
				typeAhead : true,
				store : {
					xtype : 'jsonstore',
					fields : ['text', 'value'],
					data : [{
						text : '是',
						value : true
					}, {
						text : '否',
						value : false
					}]
				},
				displayField : 'text',
				valueField : 'value',
				editable : false,
				mode : 'local'
			}
		}, {
			header : '字段长度',
			dataIndex : 'length',
			editor : {
				xtype : 'numberfield',
				allowNegative : false,
				decimalPrecision : 0,
				minValue : 0,
				maxValue : 100000
			}
		}, {
			header : '精确度',
			dataIndex : 'precision',
			editor : {
				xtype : 'numberfield',
				allowNegative : false,
				decimalPrecision : 0,
				minValue : 0,
				maxValue : 10
			}
		}, {
			header : '数据字典',
			dataIndex : 'bizTypeCode',
			editor : {
				xtype : 'textfield',
				allowBlank : true
			}
		}, {
			header : '必填',
			dataIndex : 'mandatory',
			renderer : doRenderer,
			editor : {
				xtype : 'combo',
				triggerAction : 'all',
				typeAhead : true,
				store : {
					xtype : 'jsonstore',
					fields : ['text', 'value'],
					data : [{
						text : '是',
						value : true
					}, {
						text : '否',
						value : false
					}]
				},
				displayField : 'text',
				valueField : 'value',
				editable : false,
				mode : 'local'
			}
		}];

		function doRenderer(val) {
			return val ? '是' : '否';
		}

		this.store = new Ext.data.Store({
			proxy : new Ext.data.HttpProxy({
				url : this.dataUrl,
				restful: true
			}),
			reader : new Ext.data.JsonReader({
				root : 'root',
				totalProperty : 'totalCount',
				id : 'id',
				fields : [{
					name : 'entityId'
				}, {
					name : 'id'

				}, {
					name : 'name'
				}, {
					name : 'columnName'
				}, {
					name : 'code'
				}, {
					name : 'dataType'
				}, {
					name : 'primaryKey'
				}, {
					name : 'length'
				}, {
					name : 'mandatory'
				}, {
					name : 'precision'
				}, {
					name : 'bizTypeCode'
				}, {
					name : 'orderBy'
				}]
			}),
			remoteSort : true,
			autoLoad : false
		});

		// this.sm = new Ext.grid.CheckboxSelectionModel({
		// singleSelect : this.singleSelect
		// });

		// this.cm = new Ext.grid.ColumnModel(new Array(this.sm)
		// .concat(this.columns));
		this.bbar = new Ext.PagingToolbar({
			pageSize : this.pageSize,
			store : this.store,
			displayInfo : true,
			displayMsg : '第{0} 到 {1} 条数据 共{2}条',
			emptyMsg : "没有数据",
			items : []
		});

		if (this.rowEditable) {
			Ext.apply(this, {
				plugins : this.rowEditor || this.getRowEditor()
			});
		}
		EntityFieldGrid.superclass.initComponent.call(this);
	},
	getRowEditor : function() {
		return new Ext.ux.grid.RowEditor({
			saveText : '保存',
			cancelText : '取消'
		});
	},
	doLoad : function() {
		if (this.autoRead) {
			if (this.store) {
				this.store.load({
					params : {
						start : 0,
						limit : this.pageSize
					}
				});
			}
		}
	},

	getRecord : function() {
		var Record = Ext.data.Record.create([{
			name : 'name',
			type : 'string'
		}, {
			name : 'columnName',
			type : 'string'

		}, {
			name : 'code',
			type : 'string'
		}, {
			name : 'dataType',
			type : 'int'
		}, {
			name : 'length',
			type : 'int'
		}, {
			name : 'primaryKey',
			type : 'int'
		}, {
			name : 'mandatory',
			type : 'int'
		}, {
			name : 'bizTypeCode',
			type : 'string'
		}, {
			name : 'precision',
			type : 'int'
		}]);

		return new Record();
	}
});
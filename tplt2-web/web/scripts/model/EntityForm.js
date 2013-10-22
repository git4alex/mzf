EntityForm = Ext.extend(Ext.form.FormPanel, {
	buttonAlign : 'right',
	border : false,
	labelWidth : 80,
	autoHeight : true, 
	entityId: null,
	bodyStyle : 'padding:6px 10px 6px 20px;',
	defaults : {
		anchor : '-20',
		xtype : 'textfield'
	},
	formConfig : {
		layoutConfig : {
			columns : 1
		}
	},
	initComponent : function() {
		var tmp = {
			xtype : 'hidden'
		};
		if (!this.updOpt) {
			tmp = {
				fieldLabel : '实体分组',
				xtype : 'combo',
				allowBlank : false,
				hiddenName : 'entityGroup',
				triggerAction : 'all',
				displayField : 'text',
				valueField : 'value',
				editable : false,
				mode : 'local',
				value : 1,
				store : {
					xtype : 'jsonstore',
					fields : [ 'text', 'value' ],
					data : od.AppInstance.appConfig.bizCode.ENTITY_GROUP
				}
			};
		}

		this.items = [ {
			name : 'id',
			xtype : 'hidden'
		}, { 
			name : 'pid',
			xtype : 'hidden',
			value : this.entityId
		}, {
			fieldLabel : '名称',
			name : 'name',
			allowBlank : false,
			blankText : '名称不能为空'
		}, {
			fieldLabel : 'Code',
			name : 'code',
			allowBlank : false,
			blankText : 'Code不能为空'
		}, {
			fieldLabel : 'aliasCode',
			name : 'aliasCode'
		}, {
			fieldLabel : '表名',
			name : 'tableName',
			allowBlank : false,
			blankText : '表名不能为空'
		}, tmp, {
			fieldLabel : '删除标识字段',
			name : 'delField',
			allowBlank : true
		} ];
		EntityForm.superclass.initComponent.call(this);
	}
});
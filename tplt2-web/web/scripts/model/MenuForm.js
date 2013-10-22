MenuForm = Ext.extend(Ext.form.FormPanel, {
			autoHeight : false,
			buttonAlign : 'right',
			frame : false,
			border : false,
			autoWidth : true,
			bodyStyle : 'padding:6px 20px 6px 10px;',
			labelWidth : 60,
			defaults : {
				anchor : '-30',
				xtype : 'textfield'
			},
			formConfig : {
				layoutConfig : {
					columns : 1
				},
				bodyStyle : 'padding:0 3px',
				labelAlign : 'top',
				border : false
			},
			initComponent : function() {
				this.items = [{
							name : 'id',
							xtype : 'hidden'
						}, {
							name : 'pid',
							xtype : 'hidden',
							value : this.parentId
						}, {
							fieldLabel : '菜单名称',
							name : 'text',
							allowBlank : false							 
						}, {
							fieldLabel : '提示信息',
							name : 'toolTip',
							allowBlank : false
						}, {
							fieldLabel : '模块编号',
							name : 'moduleId'
						}, {
							xtype : 'combo',
							fieldLabel : '状       态',
							hiddenName : 'enable',
							triggerAction : 'all',
							value : 1,
							store : {
								xtype : 'jsonstore',
								fields : ['code', 'value'],
								data : [{
											code : 1,
											value : '启用'
										}, {
											code : 0,
											value : '禁止'
										}]
							},
							displayField : 'value',
							valueField : 'code',
							editable : false,
							mode : 'local'
						}, {
							name : 'iconCls',
							fieldLabel : '图标地址'
						}];
				MenuForm.superclass.initComponent.call(this);
			}
		});
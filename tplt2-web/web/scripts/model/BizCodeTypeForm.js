BizCodeTypeForm = Ext.extend(Ext.form.FormPanel, {
			autoHeight : false, 
			buttonAlign : 'right',
			frame : false,
			border : false,
			autoWidth : true,
			bodyStyle : 'padding:6px 20px 6px 10px;',
			labelWidth : 70,
			defaults : {
				anchor : '-10',
				xtype : 'textfield'
			},
			layoutConfig : {
				columns : 1
			},
			initComponent : function() {
				this.items = [{
							name : 'id',
							xtype : 'hidden'
						}, {
							name : 'pid',
							xtype : 'hidden'
						}, {
							fieldLabel : '名称',
							name : 'name'
						}, {
							fieldLabel : '编码值',
							name : 'code'
						}, {
							fieldLabel : '显示名称',
							name : 'text'
						}, {
							xtype : 'combo',
							fieldLabel : '是否系统级',
							hiddenName : 'isSystem',
							triggerAction : 'all',
							value : 1,
							store : {
								xtype : 'jsonstore',
								fields : ['code', 'value'],
								data : [{
											code : 1,
											value : '是'
										}, {
											code : 0,
											value : '否'
										}]
							},
							displayField : 'value',
							valueField : 'code',
							editable : false,
							mode : 'local'
						}];
				BizCodeTypeForm.superclass.initComponent.call(this);
			}
		});
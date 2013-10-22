BizCodeForm = Ext.extend(Ext.form.FormPanel, {
			autoHeight : false,
			buttonAlign : 'right',
			frame : false,
			border : false,
			autoWidth : true,
			bodyStyle : 'padding:6px 20px 6px 10px;',
			labelWidth : 60,
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
							fieldLabel : '名称',
							name : 'text',
							allowBlank: false
						}, {
							fieldLabel : '值',
							name : 'value',
							allowBlank: false
						}, {
							fieldLabel : 'pid',
							xtype:'hidden',
							name : 'pid'
						},{
							fieldLabel : 'typeCode',
							xtype:'hidden',
							name : 'typeCode'
						}, {
							xtype : 'combo',
							fieldLabel : '状态',
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
						},{
							fieldLabel : '备注',
							name:'remark',
							xtype:'textarea'						
						}];
				BizCodeForm.superclass.initComponent.call(this);
			}
		});
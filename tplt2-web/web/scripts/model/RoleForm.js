RoleForm = Ext.extend(Ext.form.FormPanel, {
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
							fieldLabel : '角色名称',
							name : 'name',
							allowBlank : false
						}, {
							fieldLabel : '角色代码',
							name : 'roleCode',
							allowBlank : false
						}, {
							name : 'remark',
							xtype : 'textarea',
							fieldLabel : '说明'
						}];
				RoleForm.superclass.initComponent.call(this);
			}
		});
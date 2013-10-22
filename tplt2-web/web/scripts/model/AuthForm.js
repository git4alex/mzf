AuthForm = Ext.extend(Ext.form.FormPanel, {
			autoHeight : false,
			buttonAlign : 'right',
			frame : false,
			border : false,
			autoWidth : true,
			bodyStyle : 'padding:6px 20px 6px 10px;',
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
							fieldLabel : '权限名称',
							name : 'name',
							allowBlank : false						 
						}, {
							fieldLabel : '类型',
							name : 'type',
							allowBlank : false
						}, {
							fieldLabel : '说明',
							xtype : 'textarea',
							name : 'remark'
						}];
				AuthForm.superclass.initComponent.call(this);
			}
		});
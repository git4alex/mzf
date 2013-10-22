ResForm = Ext.extend(Ext.form.FormPanel, {
			autoHeight : false,
			buttonAlign : 'right',
			frame : false,
			border : false,
			autoWidth : true, 
			bodyStyle : 'padding:6px 20px 6px 10px;',
			labelWidth : 60,
			defaults : {
				anchor:'-30',
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
							fieldLabel : '资源名称',
							name : 'name',
							allowBlank : false						 
						}, {
							fieldLabel : '模块名称',
							name : 'moduleId',
							allowBlank : false
						}, {
							fieldLabel : 'URL',
							name : 'url',
							allowBlank : false
						}, {
							fieldLabel : '说明',
							xtype : 'textarea',
							name : 'remark'
						}];
				ResForm.superclass.initComponent.call(this);
			}
		});
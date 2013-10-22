OrgForm = Ext.extend(Ext.form.FormPanel, {
			autoHeight : false,
			 
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
							fieldLabel : '机构简称',
							name : 'name',
							allowBlank : false						 
						}, {
							fieldLabel : '机构全称',
							name : 'fullName',
							allowBlank : false
						}, {
							fieldLabel : '业务编码',
							name : 'bizCode',
							allowBlank : false
						}, {
							fieldLabel : '领导名',
							name : 'managerName',
							allowBlank : false
						}, {
							name : 'remark',
							xtype : 'textarea',
							fieldLabel : '说明'
						}];
				OrgForm.superclass.initComponent.call(this);
			}
		});
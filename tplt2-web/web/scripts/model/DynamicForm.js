/**
 * 动态表单包装类 params:fieldConfig 是表单配置信息
 * 
 * @class DynamicForm
 * @extends Ext.ux.SimpleFormPanel
 */
DynamicForm = Ext.extend(Ext.form.FormPanel, {
			autoHeight : false,
			closable : true,
			buttonAlign : 'right',
			border : false,
			bodyStyle : 'padding:6px 10px 6px 10px;',
			labelWidth : 80,
			defaults : { 
				anchor:'80%',
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
				var fieldCfg = new Array();
				if (this.fieldConfig) {
					if (Ext.isArray(this.fieldConfig.properties)) {
						var properties = this.fieldConfig.properties;
						Ext.each(properties, function(prop) {
									if (Ext.isEmpty(prop.code))
										return;
									var field = getDefaultField(prop);
									field = Ext.apply(field, FormUtil
													.getDisType(prop.type,
															prop.data))
									fieldCfg.push(field);
								});
					}
				};

				function getDefaultField(prop) {
					var tempName;
					if (prop.type == 'combo')
						tempName = {
							hiddenName : prop.code
						};
					else
						tempName = {
							name : prop.code
						}
					var field = {
						fieldLabel : prop.name || '',
						allowBlank : prop.allowBlank,
						vtype : prop.vType,
						readOnly : prop.readOnly,
						anchor:prop.anchor
					};

					field = Ext.apply(tempName, field);
					if (prop.allowBlank == false)
						field = Ext.apply(field, {
									blankText : prop.name + '不能为空'
								});
					return field;
				};
				this.items = fieldCfg || [];
				DynamicForm.superclass.initComponent.call(this);
			}
		});

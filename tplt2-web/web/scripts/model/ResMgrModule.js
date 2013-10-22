ResUtil={};
ResUtil.getParams=function(form, opt){
	data = form.getForm().getValues();	
	if(opt)
		delete data.id;
	return data;
}
ResMgrModule = Ext.extend(Ext.Panel, {
	layout : 'fit',
	initComponent : function() {
		var resource = new ResGrid({
			title : '资源管理',
			groupField : 'moduleId',
			border : false,
			dataUrl : Constants.resmgr,
			tbar : [{
				text : '新增资源',
				iconCls : 'icon-user-add',
				scope : this,
				handler : function() {
					form = new ResForm();
					Framework.showWin({
						title : '新增资源',
						form : form,
						height : 240,
						width : 400,
						submitHandler : function() {
							if (form.getForm().isValid()) {
								Ext.Ajax.request({
											url : Constants.resmgr,
											jsonData :  ResUtil.getParams(form, true),
											method : 'POST',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
													resource.getStore()
															.reload();
												} else {
													Ext.Msg.alert('错误', "添加失败");
												}
											},
											failure : function(resp) {
												Ext.Msg.alert('错误', "添加失败");
											}
										});
							}
						}
					});
				}
			}, {
				text : '修改资源',
				iconCls : 'icon-user-edit',
				scope : this,
				handler : function() {
					var record = resource.getSelectionModel().getSelected();
					if (record) {
						form = new ResForm();
						form.getForm().loadRecord(record);
						var resId = record.get('id');
						Framework.showWin({
							title : '修改资源',
							form : form,
							height : 240,
							width : 400,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.resmgr + "/" + resId ,
												jsonData :   ResUtil.getParams(form),
												method : 'PUT',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Framework.closeWin();
														resource.getStore()
																.reload();
													} else {
														Ext.Msg.alert('错误',
																"添加失败");
													}
												},
												failure : function(resp) {
													Ext.Msg.alert('错误', "添加失败");
												}
											});
								}
							}							 
						});
					} else {
						Ext.Msg.alert('提示', "请选择一行");
					}
				}
			}, {
				text : '删除资源',
				iconCls : 'icon-user-del',
				scope : this,
				handler : function() {
					var record = resource.getSelectionModel().getSelected();
					if (record) {
						Ajax.request({
									url : Constants.resmgr + "/"
											+ record.get('id'),
									method : 'DELETE',
									confirm : '是否确定删除？',
									success : function(result) {
										if (result.success) {
											resource.getStore().reload();
										} else {
											Ext.Msg.alert('消息', '删除失败!');
										}
									},
									failure : function(result) {
										Ext.Msg.alert('消息', '删除失败!');
									}
								});

					} else {
						Ext.Msg.alert('提示', '请选择一行');
					}
				}
			}, {
				text : '刷新',
				iconCls : 'comm-upd',
				handler : function() {
					resource.getStore().reload();
				}
			}]
		});

		this.items = [resource]
		ResMgrModule.superclass.initComponent.call(this);
	}
});

ResModule = Ext.extend(od.Module, {
			moduleId : 'resmgr',
			iconCls : 'icon-xds',
			components : [ResMgrModule],
			createDefaultComponent : function() {
				return new ResMgrModule();
			}
		});

od.ModuleMgr.registerType('resmgr', ResModule);
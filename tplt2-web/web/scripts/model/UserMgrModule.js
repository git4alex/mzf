/**
 * 用户管理模块 
 */

UserUtil = {};
UserUtil.getParams = function(form, opt) {
	var data = form.getForm().getValues();
	delete data.password2;
	if(opt){
		delete data.id;
	}
	return data;
}
UserMgrModule = Ext.extend(Ext.Panel, {
	layout : 'fit',
	initComponent : function() {
		var tbar = [{
			text : '新增用户',
			iconCls : 'icon-user-add',
			scope : this,
			handler : function() { 
				form = new UserInfoForm();
				Framework.showWin({
					title : '新增用户',
					form : form,
					height : 260,
					width : 600,
					submitHandler : function() { 
						if (form.getForm().isValid()) {  
							Ext.Ajax.request({
										url : Constants.usermgr,
										jsonData : UserUtil.getParams(form,true),
										method : 'POST',
										success : function(resp) {
											result = Ext
													.decode(resp.responseText)
											if (result.success) {
												Ext.Msg.alert('提示', result.msg);
												Framework.closeWin();
												userMgr.getStore().reload();
											} else {
												Ext.Msg.alert('提示', result.msg);
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误', "操作失败");
										}
									});
						}
					} 
				});
			}

		}, {
			text : '修改用户',
			iconCls : 'icon-user-edit',
			scope : this,
			handler : function() {
				var record = userMgr.getSelectionModel().getSelected();
				if (record) {
					form = new UserInfoForm();
					form.getForm().findField('password').setDisabled(true);
					form.getForm().findField('password2').setDisabled(true);
					form.getForm().loadRecord(record);
					var userId = record.get('id');
					Framework.showWin({
						title : '修改用户',
						form : form,
						height : 260,
						width : 600,
						submitHandler : function() {
							if (form.getForm().isValid()) { 
								Ext.Ajax.request({
											url : Constants.usermgr + "/" + userId,
											jsonData :  UserUtil.getParams(form),
											method : 'PUT',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Ext.Msg.alert('提示', result.msg);
													Framework.closeWin();
													userMgr.getStore().reload();
												} else {
													Ext.Msg.alert('错误', "操作失败");
												}
											},
											failure : function(resp) {
												Ext.Msg.alert('错误', "操作失败");
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
			text : '重置密码',
			iconCls : 'icon-user-del',
			scope : this,
			handler : function() {
				var record = userMgr.getSelectionModel().getSelected();
				if (record) {
					form = new PwdForm();
					var uid =  record.get('id');
					form.getForm().setValues({
								id :uid,
								userName : record.get('userName')
							});
					Framework.showWin({
						title : '重置密码',
						form : form,
						height : 180,
						width : 400,
						submitHandler : function() {
							if (form.getForm().isValid()) { 
								Ext.Ajax.request({
											url : Constants.usermgr + "/" + uid,
											jsonData :  UserUtil.getParams(form),
											method : 'PUT',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
													userMgr.getStore().reload();
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
				} else {
					Ext.Msg.alert('提示', "请选择一行");
				}
			}
		}, {
			text : '删除用户',
			iconCls : 'icon-user-del',
			scope : this,
			handler : function() {
				var record = userMgr.getSelectionModel().getSelected();
				if (record) {
					Ajax.request({
								url : Constants.usermgr + "/"
										+ record.get('id'),
								method : 'DELETE',
								confirm : '确定要删除吗？',
								success : function(result) {
									if (result.success) {
										userMgr.getStore().reload();
									} else {
										Ext.Msg.alert('消息', '删除失败!');
									}
								}

							});
				} else {
					Ext.Msg.alert('提示', '请选择一行');
				}
			}
		}, {
			text : '分配角色',
			iconCls : 'icon-default-content',
			scope : this,
			handler : function() {
				var record = userMgr.getSelectionModel().getSelected();
				if (!record) {
					Ext.Msg.alert('提示', '至少选择一个用户');
					return;
				}
				var userId = record.get('id');
				var roleMgr = new RoleGrid({
							border : false,
							singleSelect : false,
							dataUrl : Constants.roleAllocate,
							baseParams:{userId:userId}
						});
				Framework.showWin({
							title : '用户分配角色',
							items : roleMgr,
							width : 700,
							height : 450,
							buttons : [{
								text : '确定',
								handler : function() {
									var records = roleMgr.getSelectionModel()
											.getSelections();
									if (!records) {
										Ext.Msg.alert('提示', '请至少选择一个角色');
										return;
									}
									ids = [];
									for (i = 0; i < records.length; i++) {
										ids.push(records[i].get('id'));

									}
									Ext.Ajax.request({
												url : Constants.allotrole + "/"
														+ userId,
												method : 'POST',
												jsonData : ids,
												success : function(resp) {
													result = Ext
															.decode(resp.responseText);
													if (result.success) {
														roleMgr.getStore().reload();
														Ext.Msg.alert('提示',
																'操作成功'); 
													} else {
														Ext.Msg.alert('提示',
																'操作失败');
													}
												},
												failure : function(resp) {
													Ext.Msg.alert('提示', '操作失败');
												}
											});
								}
							}, {
								text : '取消',
								handler : function() {
									Framework.closeWin();
								}
							}]
						});

			}
		}, {
			text : '移除角色',
			iconCls : 'icon-default-content',
			scope : this,
			handler : function() {
				var record = userMgr.getSelectionModel().getSelected();
				if (!record) {
					Ext.Msg.alert('提示', '至少选择一个用户');
					return;
				}
				var userId = record.get('id');
				var roleMgr = new RoleGrid({
							border : false,
							singleSelect : false,
							dataUrl : Constants.roleAllocated,
							baseParams:{userId:userId}
						});
				Framework.showWin({
							title : '用户移除角色',
							items : roleMgr,
							width : 700,
							height : 450,
							buttons : [{
								text : '确定',
								handler : function() {
									var records = roleMgr.getSelectionModel()
											.getSelections();
									if (!records) {
										Ext.Msg.alert('提示', '请至少选择一个角色');
										return;
									}
									ids = [];
									for (i = 0; i < records.length; i++) {
										ids.push(records[i].get('allocatedId'));

									}
									Ext.Ajax.request({
												url : Constants.allotrole, 
												method:'DELETE',
												params : {"ids":ids},
												jsonData:{},
												success : function(resp) {
													result = Ext
															.decode(resp.responseText);
													if (result.success) {
														Ext.Msg.alert('提示',
																'操作成功'); 
														roleMgr.getStore().reload();
													} else {
														Ext.Msg.alert('提示',
																'操作失败');
													}
												},
												failure : function(resp) {
													Ext.Msg.alert('提示', '操作失败');
												}
											});
								}
							}, {
								text : '取消',
								handler : function() {
									Framework.closeWin();
								}
							}]
						});
			}
		}];

		var userMgr = new UserMgrGrid({
					title : '用户管理',
					border : false,
					dataUrl : Constants.usermgr,
					tbar : tbar
				});

		this.items = [userMgr];
		UserMgrModule.superclass.initComponent.call(this);
	}
});

UserModule = Ext.extend(od.Module, {
			moduleId : 'usermgr',
			iconCls : 'icon-xds',
			components : [UserMgrModule],
			createDefaultComponent : function() {
				return new UserMgrModule();
			}
		});

od.ModuleMgr.registerType('usermgr', UserModule);
/**
 * 角色管理模块
 * @class RoleMgrModule
 * @extends Ext.Panel
 */
RoleMgrModule = Ext.extend(Ext.Panel, {
	layout : 'fit',
	initComponent : function() {
		var roleMgr = new RoleGrid({
			title : '角色管理',
			border : false,
			dataUrl : Constants.rolemgr,
			tbar : [{
				text : '新增角色',
				iconCls : 'icon-user-add',
				scope : this,
				handler : function() {
					form = new RoleForm();
					Framework.showWin({
						title : '新增资源',
						form : form,
						height : 220,
						width : 400,
						submitHandler : function() {
							if (form.getForm().isValid()) {
								Ext.Ajax.request({
											url : Constants.rolemgr,
											jsonData : RoleUtil.getParams(form,
													true),
											method : 'POST',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
													roleMgr.getStore().reload();
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
				}
			}, {
				text : '修改角色',
				iconCls : 'icon-user-edit',
				scope : this,
				handler : function() {
					var record = roleMgr.getSelectionModel().getSelected();
					if (record) {
						form = new RoleForm();
						form.getForm().loadRecord(record);
						var roleId = record.get('id');
						Framework.showWin({
							title : '修改角色',
							form : form,
							height : 220,
							width : 400,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.rolemgr + "/"
														+ roleId,
												jsonData : RoleUtil
														.getParams(form),
												method : 'PUT',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Framework.closeWin();
														roleMgr.getStore()
																.reload();
													} else {
														Ext.Msg.alert('错误',
																"操作失败");
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
				text : '删除角色',
				iconCls : 'icon-user-del',
				scope : this,
				handler : function() {
					var record = roleMgr.getSelectionModel().getSelected();
					if (record) {
						Ajax.request({
									url : Constants.rolemgr + "/"
											+ record.get('id'),
									method : 'DELETE',
									confirm : '是否确定删除？',
									success : function(result) {
										if (result.success) {
											roleMgr.getStore().reload();
										} else {
											Ext.Msg.alert('消息', '删除失败!');
										}
									},
									failure : function(resp) {
										Ext.Msg.alert('消息', '删除失败!');
									}
								});

					} else {
						Ext.Msg.alert('提示', '请选择一行');
					}
				}
			}, {
				text : '分配权限',
				iconCls : 'icon-user-del',
				scope : this,
				handler : function() {
					var record = roleMgr.getSelectionModel().getSelected();
					if (record) {
						var roleId = record.get('id');
						var treePanel = new Ext.ux.tree.CheckTreePanel({
									frame : false,
									border : false,
									columnWidth : 0.7,
									height : 600,
									useArrows : false,
									autoScroll : true,
									animate : true,
									enableDD : true,
									containerScroll : true,
									lines : true,
									checkModel : 'multiple',
									root : new Ext.tree.AsyncTreeNode({
												id : '-1',
												text : '权限管理'
											}),
									dataUrl : Constants.permissionAllocate,
									baseParams : {
										roleId : roleId
									},
									createNode : function(attr) {
										//attr.text = attr.name;
										return Ext.tree.TreeLoader.prototype.createNode
												.call(this, attr);
									}
								});
						treePanel.getRootNode().expand(); 
						Framework.showWin({
							title : '权限管理',
							items : treePanel,
							height : 400,
							width : 400,
							submitHandler : function() {  
								var nodeIds = treePanel.getChecked(['id']);
								Ext.Ajax.request({
											url : Constants.roleallotauth + "/" + roleId,
											jsonData : nodeIds,
											method:'POST',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
												} else {
													Ext.Msg.alert('消息', result.msg);
												}
											},
											failure : function(resp) {
												Ext.Msg.alert('消息', result.msg);
											}
										});
							}
						});
					} else {
						Ext.Msg.alert('提示', "请选择一行");
					}
				}
			}, {
				text : '分配菜单',
				iconCls : 'icon-user-del',
				scope : this,
				handler : function() {

				}
			}]
		});

		this.items = [roleMgr];
		RoleMgrModule.superclass.initComponent.call(this);
	}
});

RoleUtil = {};
RoleUtil.getParams = function(form, opt) {
	data = form.getForm().getValues();
	if (opt)
		delete data.id;
	return data;
}


RoleModule = Ext.extend(od.Module, {
			moduleId : 'rolemgr',
			iconCls : 'icon-xds',
			components : [RoleMgrModule],
			createDefaultComponent : function() {
				return new RoleMgrModule();
			}
		});

od.ModuleMgr.registerType('rolemgr', RoleModule);

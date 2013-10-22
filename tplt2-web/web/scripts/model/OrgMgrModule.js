/**
 * 组织结构管理模块
 * @class OrgMgrModule
 * @extends Ext.Panel
 */

OrgMgrModule = Ext.extend(Ext.Panel, {
	initComponent : function() {
		var root = new Ext.tree.AsyncTreeNode({
					id : '-1',
					text : '组织机构管理'
				});
		var treePanel = new Ext.tree.TreePanel({
			frame : false,
			border : false,
			columnWidth : 0.7,
			height : 600,
			xtype : 'treepanel',
			useArrows : false, // true:使用箭头显示树结构,false:使用垂直线显示树结构
			autoScroll : true,
			animate : true,
			enableDD : true,
			containerScroll : true,
			lines : true,
			root : root,
			loader : new Ext.tree.TreeLoader({
						requestMethod : 'GET',
						dataUrl : Constants.orgmgr,
//						createNode : function(attr) {
//							attr.text = attr.name;
//							return Ext.tree.TreeLoader.prototype.createNode
//									.call(this, attr);
//						},
						listeners : {
							scope : this,
							load : function(node) {
								treePanel.expandAll();
							}
						}
					}),
			tbar : [{
				text : '新增机构',
				iconCls : 'comm-add',
				scope : this,
				handler : function() {
					var node = treePanel.getSelectionModel().getSelectedNode();
					if (node == undefined || node == null)
						parentId = '-1';
					else
						parentId = node.attributes.ID || node.id;
					var form = new OrgForm({
								parentId : parentId
							});

					Framework.showWin({
						title : '新增机构',
						form : form,
						height : 260,
						width : 400,
						submitHandler : function() {
							if (form.getForm().isValid()) {
								Ext.Ajax.request({
											url : Constants.orgmgr,
											method : 'POST',
											jsonData : OrgUtil.getParam(form,
													true),
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
													root.reload();
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
				text : '修改机构',
				iconCls : 'comm-edit',
				scope : this,
				handler : function() {
					var node = treePanel.getSelectionModel().getSelectedNode();
					if (!node)
						Ext.Msg.alert('提示', '请选择一个节点');
					else {
						var form = new OrgForm();
						this.setFormValuesByNode(form.getForm(), node);
						Framework.showWin({
							title : '修改机构',
							form : form,
							height : 260,
							width : 400,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.orgmgr + "/"
														+ node.id,
												jsonData : OrgUtil
														.getParam(form),
												method : 'PUT',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Framework.closeWin();
														root.reload();
													} else {
														Ext.Msg.alert('错误',
																"修改失败");
													}
												},
												failure : function(resp) {
													Ext.Msg.alert('错误', "修改失败");
												}
											});
								}

							}
						});
					}

				}
			}, {
				text : '删除机构',
				iconCls : 'comm-del',
				scope : this,
				handler : function() {
					var node = treePanel.getSelectionModel().getSelectedNode();
					if (!node) {
						Ext.Msg.alert('提示', '请选择一个节点');
					} else {
						Ajax.request({
									url : Constants.orgmgr + "/" + node.id,
									method : 'DELETE',
									confirm : '是否确定删除？',
									success : function(result) {
										if (result.success) {
											root.reload();
										} else {
											Ext.Msg.alert('消息', '删除失败!');
										}
									},
									failure : function(result) {
										Ext.Msg.alert('消息', '删除失败!');
									}
								});
					}
				}
			}, {
				text : '刷新',
				iconCls : 'comm-upd',
				handler : function() {
					treePanel.root.reload();
				}
			}]

		});

		treePanel.on('contextmenu', function(node, event) {
			event.preventDefault(); // 关闭默认的，以避免弹出两个
			var nodemenu = new Ext.menu.Menu({
				items : [{
					text : "新增同级机构",
					iconCls : 'comm-add',
					handler : function() {
						if (node == undefined || node == null)
							parentId = '-1';
						else
							parentId = node.attributes.pid || node.id;
						var form = new OrgForm({
									parentId : parentId
								});
						Framework.showWin({
							title : '新增同级机构',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
										url : Constants.orgmgr,
										jsonData : OrgUtil.getParam(form, true),
										method : 'POST',
										success : function(resp) {
											result = Ext
													.decode(resp.responseText)
											if (result.success) {
												Framework.closeWin();
												root.reload();
											} else {
												Ext.Msg.alert('错误', "添加失败");
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误', "添加失败");
										}
									});
								}
							},
							height : 260,
							width : 400
						});
					}
				}, {

					text : "新增子机构",
					iconCls : 'comm-add',
					handler : function() {
						if (node == undefined || node == null)
							parentId = '-1';
						else
							parentId = node.attributes.pid || node.id;
						var form = new OrgForm({
									parentId : parentId
								});
						Framework.showWin({
							title : '新增子机构',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
										url : Constants.orgmgr,
										jsonData : OrgUtil.getParam(form, true),
										method : 'POST',
										success : function(resp) {
											result = Ext
													.decode(resp.responseText)
											if (result.success) {
												Framework.closeWin();
												root.reload();
											} else {
												Ext.Msg.alert('错误', "添加失败");
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误', "添加失败");
										}
									});
								}
							},
							height : 260,
							width : 400
						});
					}
				}, {
					text : "修改机构",
					iconCls : 'comm-edit',
					scope : this,
					handler : function() {
						var form = new OrgForm();
						this.setFormValuesByNode(form.getForm(), node);
						Framework.showWin({
							title : '修改机构',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.orgmgr + "/"
														+ node.id,
												jsonData : OrgUtil
														.getParam(form),
												method : 'PUT',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Framework.closeWin();
														root.reload();
													} else {
														Ext.Msg.alert('错误',
																"修改失败");
													}
												},
												failure : function(resp) {
													Ext.Msg.alert('错误', "修改失败");
												}
											});
								}
							},
							height : 260,
							width : 400
						});

					}
				}, {
					text : "删除机构",
					iconCls : 'comm-del',
					handler : function() {
						Ext.Ajax.request({
									url : Constants.orgmgr + "/" + node.id,
									method : 'DELETE',
									success : function(resp) {
										result = Ext.decode(resp.responseText);
										if (result.success) {
											root.reload();
										} else {
											Ext.Msg.alert('消息', '删除失败!');
										}
									},
									failure : function(resp) {
										Ext.Msg.alert('消息', '删除失败!');
									}
								});
					}
				}]
			});
			nodemenu.showAt(event.getXY()); // 取得鼠标点击坐标，展示菜单
		}, this);
		// 移动tree节点
		treePanel.on('movenode', function(tree, node, srcParent, tarParent,
						index) {
					var orderBy = node.attributes.orderBy;
					var nodeId = node.attributes.ID || node.id;
					var loadMask = new Ext.LoadMask(Ext.getBody());
					loadMask.show();
					var pid = tarParent.attributes.ID || tarParent.id;
					var url = Constants.orgmgr_moveNode + "/" + nodeId + "/"
							+ pid + "/" + index;
					Ext.Ajax.request({
								url : url,
								method : 'PUT',
								success : function(resp) {
									result = Ext.decode(resp.responseText);
									if (result.success) {
										loadMask.hide();
										root.expand();
									} else {
										Ext.Msg.alert('消息', '节点移动失败!');
									}
								},
								failure : function(resp) {
									Ext.Msg.alert('消息', '节点移动失败!');
								}
							});
				}, this);

		root.expand();
		this.items = [treePanel];
		OrgMgrModule.superclass.initComponent.call(this);

	},
	setFormValuesByNode : function(form, node) {
		form.setValues({
					id : node.id,
					name : node.text,
					fullName : node.attributes.fullName,
					pid : node.attributes.pid,
					bizCode : node.attributes.bizCode,
					managerName : node.attributes.managerName,
					remark : node.attributes.remark
				});
	}
});


OrgUtil = {};

OrgUtil.getParam = function(form, opt) {
	formdata = form.getForm().getValues();
	if (opt)
		delete formdata.id;
	return formdata;
};


OrgModule = Ext.extend(od.Module, {
			moduleId : 'orgmgr',
			iconCls : 'icon-xds',
			components : [OrgMgrModule],
			createDefaultComponent : function() {
				return new OrgMgrModule();
			}
		});

od.ModuleMgr.registerType('orgmgr', OrgModule);
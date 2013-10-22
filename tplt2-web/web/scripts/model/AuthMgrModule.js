AuthUtil={};
AuthUtil.getParams=function(form, opt)
{
	data = form.getForm().getValues();
	if(opt)
		delete data.id;
	return data;
}

AuthMgrModule = Ext.extend(Ext.Panel, {
	title:'权限管理',
	layout:'fit',
	initComponent : function() { 
		var root = new Ext.tree.AsyncTreeNode({
					id : '-1',
					text : '权限管理'
				});
		var treePanel = new Ext.tree.TreePanel({
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
			root : root,
			loader : new Ext.tree.TreeLoader({
						requestMethod : 'GET',
						dataUrl : Constants.authmgr,
						createNode : function(attr) {
							//attr.text = attr.name;
							return Ext.tree.TreeLoader.prototype.createNode
									.call(this, attr);
						},
						listeners:{
							load:function(node)
							{
								treePanel.expandAll();
							}
						}
					}),
			tbar : [{
				text : '新增权限',
				iconCls : 'comm-add',
				scope : this,
				handler : function() {
					var node = treePanel.getSelectionModel().getSelectedNode();
					if (node == undefined || node == null)
						parentId = '-1';
					else
						parentId = node.attributes.ID || node.id;
					var form = new AuthForm({
								parentId : parentId
							});

					Framework.showWin({
						title : '新增权限',
						form : form,
						height : 220,
						width : 400,
						submitHandler : function() {
							if (form.getForm().isValid()) {
								Ext.Ajax.request({
											url : Constants.authmgr,
											jsonData : AuthUtil.getParams(form, true),
											method:'POST',
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
				text : '修改权限',
				iconCls : 'comm-edit',
				scope : this,
				handler : function() {
					var node = treePanel.getSelectionModel().getSelectedNode();
					if (!node)
						Ext.Msg.alert('提示', '请选择一个节点');
					else {
						var form = new AuthForm();
						this.setFormValuesByNode(form.getForm(), node);
						Framework.showWin({
							title : '修改权限',
							form : form,
							height : 220,
							width : 400,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.authmgr + "/" + node.id,
												jsonData : AuthUtil.getParams(form),
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
				text : '删除权限',
				iconCls : 'comm-del',
				scope : this,
				handler : function() {
					var node = treePanel.getSelectionModel().getSelectedNode();
					if (!node) {
						Ext.Msg.alert('提示', '请选择一个节点');
					} else {
						Ajax.request({
									url : Constants.authmgr + "/"
											+ node.id,
									method : 'DELETE',
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
				iconCls:'comm-upd',
				handler : function() { 
					treePanel.getRootNode().reload();
				} 
			},{
				text : '分配资源',
				handler : function() { 
					var node = treePanel.getSelectionModel().getSelectedNode();
					if (!node || node.id == -1) {
						Ext.Msg.alert('提示', '请选择一个权限');
						return;
					}
					var authId = node.id;
					var res = new ResGrid({
								groupField : 'moduleId',
								border : false,
								singleSelect : false,
								dataUrl : Constants.resourceAllocate,
								baseParams:{"permissionId":authId}
							});
					Framework.showWin({
						title : '资源管理',
						items : res,
						width : 700,
						height : 450,
						buttons : [{
							text : '确定',
							handler : function() {
								var records = res.getSelectionModel()
										.getSelections();
								if (!records) {
									Ext.Msg.alert('提示', '请至少选择一个资源');
									return;
								}
								ids = [];
								for (i = 0; i < records.length; i++) {
									ids.push(records[i].get('id'));

								}
								Ext.Ajax.request({
											url : Constants.allotres + "/"
													+ authId,
											method : 'POST',
											jsonData : ids,
											success : function(resp) {
												result = Ext
														.decode(resp.responseText);
												if (result.success) {
													Ext.Msg.alert('提示', '操作成功');
													res.getStore().reload();
												} else {
													Ext.Msg.alert('提示', '操作失败');
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
				text : '移除资源',
				handler : function() {
					var node = treePanel.getSelectionModel().getSelectedNode();
					if (!node || node.id == -1) {
						Ext.Msg.alert('提示', '请选择一个权限');
						return;
					}
					var authId = node.id;
					var res = new ResGrid({
								groupField : 'MODULE_ID',
								border : false,
								singleSelect : false,
								dataUrl : Constants.resourceAllocated,
								baseParams:{"permissionId":authId}
							});
					Framework.showWin({
						title : '资源管理',
						items : res,
						width : 700,
						height : 450,
						buttons : [{
							text : '确定',
							handler : function() {
								var records = res.getSelectionModel()
										.getSelections();
								if (!records) {
									Ext.Msg.alert('提示', '请至少选择一个资源');
									return;
								}
								ids = [];
								for (i = 0; i < records.length; i++) {
									ids.push(records[i].get('allocatedId'));

								}
								Ext.Ajax.request({
											url : Constants.allotres,
											method : 'DELETE',
											params:{"ids":ids},
											jsonData : {}, 
											success : function(resp) {
												result = Ext
														.decode(resp.responseText);
												if (result.success) {
													Ext.Msg.alert('提示', '操作成功');
													res.getStore().reload();
												} else {
													Ext.Msg.alert('提示', '操作失败');
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
			}]

		});
		treePanel.getRootNode().expand();
		treePanel.on('contextmenu', function(node, event) {
			event.preventDefault(); // 关闭默认的，以避免弹出两个
			var nodemenu = new Ext.menu.Menu({
				items : [{
					text : "新增同级权限",
					iconCls : 'comm-add',
					handler : function() {
						if (node == undefined || node == null)
							parentId = '-1';
						else
							parentId = node.attributes.pid || node.id;
						var form = new AuthForm({
									parentId : parentId
								});
						Framework.showWin({
							title : '新增同级权限',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.authmgr,
												jsonData : AuthUtil.getParams(form,true),
												method:'POST',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Framework.closeWin();
														root.reload();
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
							}, 
							height : 220,
							width : 400
						});
					}
				}, {

					text : "新增子权限",
					iconCls : 'comm-add',
					handler : function() {
						if (node == undefined || node == null)
							parentId = '-1';
						else
							parentId = node.id;
						var form = new AuthForm({
									parentId : parentId
								});
						Framework.showWin({
							title : '新增子权限',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.authmgr,
												jsonData : AuthUtil.getParams(form,true),
												method:'POST',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Framework.closeWin();
														root.reload();
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
							}, 
							height : 220,
							width : 400
						});
					}
				}, {
					text : "修改权限",
					iconCls : 'comm-edit',
					scope : this,
					handler : function() {
						var form = new AuthForm();
						this.setFormValuesByNode(form.getForm(), node);
						Framework.showWin({
							title : '修改权限',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.authmgr + "/" + node.id,
												jsonData : AuthUtil.getParams(form),
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
							height : 220,
							width : 400
						});

					}
				}, {
					text : "删除机构",
					iconCls : 'comm-del',
					handler : function() {
						Ext.Ajax.request({
									url : Constants.authmgr + "/"
											+ node.id,
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
					var url = Constants.authmgr_moveNode + "/"
							+ nodeId + "/" + pid + "/" + index;
					Ext.Ajax.request({
								url : url,
								method : 'PUT',
								success : function(resp) {
									result = Ext.decode(resp.responseText);
									if (result.success) {
										loadMask.hide();
										root.reload();
									} else {
										Ext.Msg.alert('消息', '节点移动失败!');
									}
								},
								failure : function(resp) {
									Ext.Msg.alert('消息', '节点移动失败!');
								}
							});
				}, this);
 
		this.items=[treePanel]; 
		AuthMgrModule.superclass.initComponent.call(this);

	},
	setFormValuesByNode : function(form, node) {
		form.setValues({
					id : node.id,
					name : node.text,
					type : node.attributes.type,
					pid : node.attributes.pid,
					remark : node.attributes.remark
				});
	}
});
AuthModule = Ext.extend(od.Module, {
			moduleId : 'authmgr',
			iconCls : 'icon-xds',
			components : [AuthMgrModule],
			createDefaultComponent : function() {
				return new AuthMgrModule();
			}
		});

od.ModuleMgr.registerType('authmgr', AuthModule);
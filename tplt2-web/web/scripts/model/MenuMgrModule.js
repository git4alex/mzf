/**
 * 菜单管理模块
 * 
 * @class MenuMgrModule
 * @extends Ext.app.Module
 */

MenuUtil = {};
MenuUtil.getParam = function(form, opt) {
	formdata = form.getForm().getValues();
	formdata.enable = parseInt(formdata.enable);
	if (opt)
		delete formdata.id;
	return formdata;
};

MenuMgrModule = Ext.extend(Ext.tree.TreePanel, {
	title : '菜单管理',
	useArrows : false, // true:使用箭头显示树结构,false:使用垂直线显示树结构
	autoScroll : true,
	animate : true,
	enableDD : true,
	containerScroll : true,
	lines : true,
	rootVisible : false,
	initComponent : function() {
		this.root = new Ext.tree.AsyncTreeNode({
					id : '-1',
					expanded : true,
					text : '菜单管理'
				});

		this.loader = new Ext.tree.TreeLoader({
					requestMethod : 'GET',
					dataUrl : Constants.queryDataForTree,
					createNode : function(attr) {
						attr.icon_Cls = attr.iconCls;
						delete attr.iconCls;
						return Ext.tree.TreeLoader.prototype.createNode.call(
								this, attr);
					},
					listeners : {
						scope : this,
						load : function(node) {
							this.expandAll();
						}
					}
				});
		this.tbar = [{
			text : '新增菜单',
			iconCls : 'comm-add',
			scope : this,
			handler : function() {
				var node = this.getSelectionModel().getSelectedNode();

				if (node == undefined || node == null)
					parentId = '-1';
				else
					parentId = node.id;
				var form = new MenuForm({
							parentId : parentId
						});

				Framework.showWin({
							title : '新增菜单',
							form : form,
							height : 230,
							width : 400,
							scope : this,
							submitHandler : function() {
								cur = this;
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.saveTreeData,
												jsonData : MenuUtil.getParam(
														form, true),
												method : 'POST',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Framework.closeWin();
														cur.getRootNode()
																.reload();
														cur.getRootNode()
																.expand(true);
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
			}
		}, {
			text : '修改菜单',
			iconCls : 'comm-edit',
			scope : this,
			handler : function() {
				var node = this.getSelectionModel().getSelectedNode();
				if (!node)
					Ext.Msg.alert('提示', '请选择一个节点');
				else {
					var form = new MenuForm();
					this.setFormValuesByNode(form.getForm(), node);
					Framework.showWin({
						title : '修改菜单',
						form : form,
						height : 230,
						width : 400,
						scope : this,
						submitHandler : function() {
							cur = this;
							if (form.getForm().isValid()) {
								Ext.Ajax.request({
											url : Constants.updateTreeData
													+ '/' + node.id,
											jsonData : MenuUtil.getParam(form),
											method : 'PUT',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Ext.Msg
															.alert('消息',
																	'修改成功!');
													Framework.closeWin();
													cur.getRootNode().reload();
													cur.getRootNode()
															.expand(true);
												} else {
													Ext.Msg.alert('错误', "修改失败");
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
			text : '删除菜单',
			iconCls : 'comm-del',
			scope : this,
			handler : function() {
				cur = this;
				var node = this.getSelectionModel().getSelectedNode();
				if (!node) {
					Ext.Msg.alert('提示', '请选择一个节点');
				} else {
					Ext.Ajax.request({
								url : Constants.deleteTreeData + "/" + node.id,
								method : 'DELETE',
								success : function(resp) {
									result = Ext.decode(resp.responseText)
									if (result.success) {
										cur.getRootNode().reload();
										cur.getRootNode().expand(true);
									} else {
										Ext.Msg.alert('消息', '删除失败!');
									}
								},
								failure : function(resp) {
									Ext.Msg.alert('消息', '删除失败!');
								}
							});
				}
			}
		}, {
			text : '刷新',
			iconCls : 'comm-upd',
			scope : this,
			handler : function() {
				this.getRootNode().reload();
				this.getRootNode().expand(true);
			}
		}, {
			text : '添加分隔符',
			scope : this,
			handler : function() {
				cur = this;
				var node = this.getSelectionModel().getSelectedNode();
				if (!node)
					Ext.Msg.alert('提示', "请选择一个菜单")
				else {

					var sep = {
						xtype : 'tbseparator',
						text : '分隔符',
						orderBy : node.attributes.orderBy,
						pid : node.attributes.pid,
						enable : true
					};
					Ext.Ajax.request({
								url : Constants.saveTreeData,
								method : 'POST',
								jsonData : sep,
								success : function(resp) {
									result = Ext.decode(resp.responseText)
									cur.getRootNode().reload();
									cur.getRootNode().expand(true);
								},
								failure : function(resp) {

								}
							});
				}

			}
		}, {
			text : '添加占位符',
			scope : this,
			handler : function() {
				cur = this;
				var node = this.getSelectionModel().getSelectedNode();
				if (!node)
					Ext.Msg.alert('提示', "请选择一个菜单")
				else {
					var sep = {
						xtype : 'tbfill',
						text : '占位符',
						orderBy : node.attributes.orderBy,
						pid : node.attributes.pid,
						enable : true
					};
					Ext.Ajax.request({
								url : Constants.saveTreeData,
								method : 'POST',
								jsonData : sep,
								success : function(resp) {
									result = Ext.decode(resp.responseText)
									cur.getRootNode().reload();
									cur.getRootNode().expand(true);
								},
								failure : function(resp) {

								}
							});

				}
			}
		}];

		this.on('contextmenu', function(node, event) {
			event.preventDefault(); // 关闭默认的菜单，以避免弹出两个菜单
			cur = this;

			if (!node) {
				Ext.Msg.alert('提示', '根节点无法新增同级节点');
				return false;
			}
			console.log(node);
			var nodemenu = new Ext.menu.Menu({
				items : [{
					text : "新增同级菜单",
					iconCls : 'comm-add',
					handler : function() {
						if (node == undefined || node == null)
							parentId = '-1';
						else
							parentId = node.attributes.pid;
						var form = new MenuForm({
									parentId : parentId
								});

						if (parentId == undefined) {
							Ext.Msg.alert('提示', '根节点无法新增同级节点');
							return;
						}
						Framework.showWin({
							title : '新增同级菜单',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.saveTreeData,
												jsonData : MenuUtil.getParam(
														form, true),
												method : 'POST',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Ext.Msg.alert('消息',
																'新增成功!');
														Framework.closeWin();
														cur.getRootNode()
																.reload();
														cur.getRootNode()
																.expand(true);
														od.AppInstance
																.updateMenu();
													}
												},
												failure : function(resp) {
													Ext.Msg.alert('错误', "添加失败");
												}
											});
								}
							},
							height : 230,
							width : 400
						});
					}
				}, {

					text : "新增子菜单",
					iconCls : 'comm-add',
					handler : function() {
						if (node == undefined || node == null)
							parentId = '-1';
						else
							parentId = node.id;
						var form = new MenuForm({
									parentId : parentId
								});
						Framework.showWin({
							title : '新增子菜单',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.saveTreeData,
												jsonData : MenuUtil.getParam(
														form, true),
												method : 'POST',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Ext.Msg.alert('消息',
																'新增成功!');
														Framework.closeWin();
														cur.getRootNode()
																.reload();
														cur.getRootNode()
																.expand(true);
														od.AppInstance
																.updateMenu();
													}
												},
												failure : function(resp) {
													Ext.Msg.alert('错误', "添加失败");
												}
											});
								}
							},
							height : 230,
							width : 400
						});
					}
				}, {
					text : "修改菜单",
					iconCls : 'comm-edit',
					scope : this,
					handler : function() {
						var form = new MenuForm();
						this.setFormValuesByNode(form.getForm(), node);
						Framework.showWin({
							title : '修改菜单',
							form : form,
							submitHandler : function() {
								if (form.getForm().isValid()) {
									Ext.Ajax.request({
												url : Constants.updateTreeData
														+ '/' + node.id,
												jsonData : MenuUtil
														.getParam(form),
												method : 'PUT',
												success : function(resp) {
													result = Ext
															.decode(resp.responseText)
													if (result.success) {
														Ext.Msg.alert('消息',
																'修改成功!');
														Framework.closeWin();
														cur.getRootNode()
																.reload();
														cur.getRootNode()
																.expand(true);
														od.AppInstance
																.updateMenu();
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
							height : 230,
							width : 400
						});

					}
				}, {
					text : "删除菜单",
					iconCls : 'comm-del',
					scope : this,
					handler : function() {
						cur = this;
						Ext.Ajax.request({
									url : Constants.deleteTreeData + "/"
											+ node.id,
									method : 'DELETE',
									success : function(resp) {
										result = Ext.decode(resp.responseText)
										if (result.success) {
											cur.getRootNode().reload();
											cur.getRootNode().expand(true);
											od.AppInstance.updateMenu();
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
		this.on('movenode', function(tree, node, srcParent, tarParent, index) {
					cur = this;
					var orderBy = node.attributes.orderBy;
					var loadMask = new Ext.LoadMask(Ext.getBody());
					// loadMask.show();
					var pid = tarParent.id;
					var url = Constants.moveNode + "/" + node.id + "/" + pid
							+ "/" + index;
					Ext.Ajax.request({
								url : url,
								method : 'PUT',
								success : function(resp) {
									result = Ext.decode(resp.responseText);
									if (result.success) {
										loadMask.hide();
										cur.getRootNode().expand();
										od.AppInstance.updateMenu();
									} else {
										Ext.Msg.alert('消息', '节点移动失败!');
										cur.getRootNode().expand();
									}
								},
								failure : function(resp) {
									Ext.Msg.alert('消息', '节点移动失败!');
								}
							});
				}, this);

		MenuMgrModule.superclass.initComponent.call(this);
	},
	setFormValuesByNode : function(form, node) {
		form.setValues({
					id : node.id,
					text : node.text,
					moduleId : node.attributes.moduleId,
					pid : node.attributes.pid,
					enable : node.attributes.enable == true ? 1 : 0,
					toolTip : node.attributes.toolTip,
					iconCls : node.attributes.icon_Cls
				});
	}
});

MenuModule = Ext.extend(od.Module, {
			moduleId : 'menumgr',
			iconCls : 'icon-xds',
			components : [MenuMgrModule],
			createDefaultComponent : function() {
				return new MenuMgrModule();
			}
		});

od.ModuleMgr.registerType('menumgr', MenuModule);
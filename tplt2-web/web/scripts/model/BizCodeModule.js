BizUtil = {};
BizUtil.getParams = function(form, opt) {
	data = form.getForm().getValues();
	if (opt)
		delete data.id;
	return data;
}

BizCodeModule = Ext.extend(Ext.Panel, {
	title : '业务编码管理',
	layout : 'border',
	initComponent : function() {
		var tree = new Ext.tree.TreePanel({
			region : 'west',
			frame : false,
			border : false,
			width : 280,
			height : 600,
			split : true,
			useArrows : true,
			autoScroll : true,
			animate : true,
			enableDD : true,
			// rootVisible:false,
			containerScroll : true,
			cls : 'rightBorder',
			tbar : [{
						xtype : 'tbseparator'
					}, {
						text : '新增',
						iconCls : 'comm-add',
						handler : function() {
							var form = new BizCodeTypeForm();
							form.getForm().setValues({
										pid : -1
									});
							Framework.showWin({
								title : '新增分类',
								form : form,
								height : 200,
								width : 380,
								submitHandler : function() {
									if (form.getForm().isValid()) {
										Ext.Ajax.request({
											url : Constants.biztype
													+ '?pidCode=pid&indexCode=orderBy',
											jsonData : BizUtil.getParams(form,
													true),
											method : 'POST',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
													tree.getRootNode().reload();
												} else {
													Ext.Msg.alert('提示',
															result.msg);
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
						text : '修改',
						iconCls : 'comm-edit',
						handler : function() {
							var node = tree.getSelectionModel()
									.getSelectedNode();
							var formdata = {};
							if (node) {
								formdata = {
									id : node.id,
									text : node.text,
									name : node.attributes.name,
									pid : node.attributes.pid,
									code : node.attributes.code,
									isSystem : node.attributes.isSystem
								};
							} else {
								Ext.Msg.alert('提示', '请选一个节点');
								return false;
							}
							var form = new BizCodeTypeForm();
							form.getForm().setValues(formdata);
							Framework.showWin({
								title : '修改分类',
								form : form,
								height : 200,
								width : 380,
								submitHandler : function() {
									if (form.getForm().isValid()) {
										Ext.Ajax.request({
											url : "/code/biz/bizType/"
													+ node.id,
											jsonData : BizUtil.getParams(form),
											method : 'PUT',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
													tree.getRootNode().reload();
												} else {
													Ext.Msg.alert('提示',
															result.msg);
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
						text : '删除',
						iconCls : 'comm-del',
						handler : function() {
							var node = tree.getSelectionModel()
									.getSelectedNode();
							if (!node) {
								Ext.Msg.alert('提示', '请选一个节点');
								return false;
							}
							Ext.Msg.confirm('提示', '确认要删除吗？', function(btn) {
								if (btn == 'yes') {
									Ext.Ajax.request({
										url : Constants.biztype
												+ "/"
												+ node.id
												+ '?pidCode=pid&indexCode=orderBy',
										method : 'DELETE', 
										success : function(resp) {
											var result = Ext
													.decode(resp.responseText);
											if (result.success) {
												tree.getRootNode().reload();
											} else {
												Ext.Msg.alert('提示', result.msg);
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误', "操作失败");
										}
									});
								}
							});
						}
					}, {
						xtype : 'tbseparator'
					}, {
						text : '查看',
						iconCls : 'comm-edit',
						handler : function() {
							var node = tree.getSelectionModel()
									.getSelectedNode();
							var formdata = {};
							if (node) {
								formdata = {
									id : node.id,
									text : node.text,
									name : node.attributes.name,
									pid : node.attributes.pid,
									code : node.attributes.code,
									isSystem : node.attributes.isSystem
								};
							} else {
								Ext.Msg.alert('提示', '请选一个节点');
								return false;
							}
							var form = new BizCodeTypeForm();
							form.getForm().setValues(formdata);
							Framework.showWin({
										title : '查看分类',
										form : form,
										height : 200,
										width : 380
									});
						}
					}, {
						text : '刷新',
						iconCls : 'comm-upd',
						handler : function() {
							tree.root.reload();
						}
					}],
			root : new Ext.tree.AsyncTreeNode({
						text : '编码分类',
						expanded : true,
						id : '-1'
					}),
			loader : new Ext.tree.TreeLoader({
						dataUrl : Constants.biztype
								+ '?pidCode=pid&indexCode=orderBy',
						/*
						 * baseParams : { pidCode : 'pid', indexCode : 'orderBy' },
						 */
						requestMethod : 'GET',
						createNode : function(attrs) {
							attrs.allowChildren = true;

							if (attrs.leaf == true) {
								attrs.leaf = false;
								attrs.loaded = true;
							}

							if (attrs.type == 'MENU') {
								attrs.iconCls = 'icon-menu';
							} else if (attrs.type == 'FUNC') {
								attrs.iconCls = 'icon-form';
							} else if (attrs.type == 'URL') {

							}

							return Ext.tree.TreeLoader.prototype.createNode
									.call(this, attrs);
						}
					}),
			listeners : {
				movenode : function(tree, node, oldParent, newParent, orderBy) {
					var pid = null;

					if (newParent.id) {
						pid = newParent.id;
					} else {
						pid = -1;
					}
					var url = 'entity/tree/bizType' + "/" + node.id + "/" + pid
							+ "/" + orderBy;
					var path = node.getPath();
					var loadMask = new Ext.LoadMask(Ext.getBody());
					loadMask.show();
					Ext.Ajax.request({
								url : url,
								method : 'PUT',
								jsonData : {
									pidCode : 'pid',
									indexCode : 'orderBy'
								},
								loadMask : loadMask,
								success : function(resp) {
									loadMask.hide();
									var result = Ext.decode(resp.responseText);
									if (result.success) {
										tree.getRootNode().reload(function() {
													tree.selectPath(path);
												});
									} else {
										Ext.Msg.alert('消息', '节点移动失败!');
										tree.getRootNode().reload();
									}
								},
								failure : function(resp) {
									loadMask.hide();
									Ext.Msg.alert('消息', '节点移动失败!');
								}
							});
				},
				scope : this
			}
		});

		var treeGrid = new Ext.ux.tree.TreeGrid({
			region : 'center',
			height : 600,
			border : false,
			useArrows : true,
			autoScroll : true,
			animate : true,
			lines : true,
			useArrows : true,
			containerScroll : true,
			enableSort : false,
			enableHdMenu : false,
			enableDD : true,
			cls : 'leftBorder',
			tbar : [{
						xtype : 'tbseparator'
					}, {
						text : '新增',
						iconCls : 'comm-add',
						handler : function() {
							var gridNode = treeGrid.getSelectionModel()
									.getSelectedNode();
							var treeNode = tree.getSelectionModel()
									.getSelectedNode();

							/**
							 * var formConfig = {}; if (gridNode) { formConfig = {
							 * typeCode : gridNode.attributes.typeCode, pid :
							 * gridNode.attributes.id }; } else if (treeNode) {
							 * formConfig = { typeCode :
							 * treeNode.attributes.code, pid : -1 }; } else {
							 * Ext.Msg.alert('提示', '请选一个节点'); return false; }
							 */

							var formConfig = {
								typeCode : treeNode.attributes.code,
								pid : -1
							};

							var form = new BizCodeForm();
							form.getForm().setValues(formConfig);
							Framework.showWin({
								title : '新增业务编码',
								form : form,
								height : 220,
								width : 380,
								submitHandler : function() {
									if (form.getForm().isValid()) {
										Ext.Ajax.request({
											url : Constants.bizcode
													+ '?pidCode=pid&indexCode=orderBy',
											jsonData : BizUtil.getParams(form,
													true),
											method : 'POST',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
													treeGrid.getRootNode()
															.reload();
													/*
													 * Ext.Ajax.request({ url :
													 * 'reloadBix.do', success :
													 * function(resp) { treeGrid
													 * .getRootNode() .reload(); }
													 * });
													 */

												} else {
													Ext.Msg.alert('提示',
															result.msg);
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
						text : '修改',
						iconCls : 'comm-edit',
						handler : function() {
							var gridNode = treeGrid.getSelectionModel()
									.getSelectedNode();
							var formdata = {};
							if (gridNode) {
								formdata = {
									id : gridNode.id,
									text : gridNode.text,
									value : gridNode.attributes.value,
									enable : gridNode.attributes.enable,
									typeCode : gridNode.attributes.typeCode,
									pid : gridNode.attributes.pid,
									remark : gridNode.attributes.remark
								};
							} else {
								Ext.Msg.alert('提示', '请选一个节点');
								return false;
							}
							var form = new BizCodeForm();
							form.getForm().setValues(formdata);
							Framework.showWin({
								title : '修改业务编码',
								form : form,
								height : 220,
								width : 380,
								submitHandler : function() {
									if (form.getForm().isValid()) {
										Ext.Ajax.request({
											url : Constants.bizcode + "/"
													+ gridNode.id,
											jsonData : BizUtil.getParams(form),
											method : 'PUT',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													Framework.closeWin();
													treeGrid.getRootNode()
															.reload();
													/*
													 * Ext.Ajax.request({ url :
													 * 'reloadBix.do', success :
													 * function(resp) { treeGrid
													 * .getRootNode() .reload(); }
													 * });
													 */

												} else {
													Ext.Msg.alert('提示',
															result.msg);
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
						text : '删除',
						iconCls : 'comm-del',
						handler : function() {
							var gridNode = treeGrid.getSelectionModel()
									.getSelectedNode();
							if (!gridNode) {
								Ext.Msg.alert('提示', '请选一个节点');
								return false;
							}
							Ext.Ajax.request({
										url : Constants.bizcode
												+ "/"
												+ gridNode.id
												+ '?pidCode=pid&indexCode=orderBy',
										method : 'DELETE',
										confirm : '是否确定删除？',
										success : function(resp) {
											var result = Ext
													.decode(resp.responseText);
											if (result.success) {
												treeGrid.getRootNode().reload();
											} else {
												Ext.Msg.alert('提示', result.msg);
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误', "操作失败");
										}
									});
						}
					}, {
						text : '更新状态',
						iconCls : 'comm-edit',
						handler : function() {
							var gridNode = treeGrid.getSelectionModel()
									.getSelectedNode();
							if (!gridNode) {
								Ext.Msg.alert('提示', '请选一个节点');
								return false;
							}
							Ext.Ajax.request({
										url : Constants.bizcode + "/"
												+ gridNode.id,
										method : 'PUT',
										jsonData : {
											enable : !gridNode.attributes.enable,
											mdate : null
										},
										success : function(resp) {
											var result = Ext
													.decode(resp.responseText);
											if (result.success) {
												treeGrid.getRootNode().reload();
											} else {
												Ext.Msg.alert('提示', result.msg);
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误', "操作失败");
										}
									});
						}
					}, {
						text : '更新缓存',
						iconCls : 'comm-edit',
						handler : function() {
							Ext.Ajax.request({
										url : 'reloadBiz',
										method : 'PUT',
										success : function(resp) {
											var result = Ext
													.decode(resp.responseText);
											Ext.Msg.alert('提示', result.msg);
										},
										failure : function(resp) {
											Ext.Msg
													.alert('错误',
															resp.statusText);
										}
									});

						}
					}, {
						text : '上移',
						iconCls : 'comm-edit',
						handler : function() {
							var gridNode = treeGrid.getSelectionModel()
									.getSelectedNode();
							if (!gridNode) {
								Ext.Msg.alert('提示', '请选一个节点');
								return false;
							}
							var id = gridNode.id;
							var pid = gridNode.parentNode.id;
							var code = treeGrid.node.attributes.code;
							Ext.Ajax.request({
										url : 'code/biz/bizCode/' + code + '/'
												+ id + '?step=-1&pid=' + pid,
										method : 'PUT',
										success : function(resp) {
											var result = Ext
													.decode(resp.responseText);
											if (result.success) {
												treeGrid.getRootNode().reload();
											} else {
												Ext.Msg.alert('提示', result.msg);
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误', "操作失败");
										}
									});

						}
					}, {
						text : '下移',
						iconCls : 'comm-edit',
						handler : function() {
							var gridNode = treeGrid.getSelectionModel()
									.getSelectedNode();
							if (!gridNode) {
								Ext.Msg.alert('提示', '请选一个节点');
								return false;
							}

							var id = gridNode.id;
							var pid = gridNode.parentNode.id;
							var code = treeGrid.node.attributes.code;

							Ext.Ajax.request({
										url : 'code/biz/bizCode/' + code + '/'
												+ id + '?step=1&pid=' + pid,
										method : 'PUT',
										success : function(resp) {
											var result = Ext
													.decode(resp.responseText);
											if (result.success) {
												treeGrid.getRootNode().reload();
											} else {
												Ext.Msg.alert('提示', result.msg);
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误', "操作失败");
										}
									});
						}
					}],
			columns : [{
						header : '名称',
						width : 200,
						dataIndex : 'text'
					}, {
						header : '值',
						width : 200,
						dataIndex : 'value'
					}, {
						header : '备注',
						width : 280,
						dataIndex : 'remark'
					}, {
						header : '状态',
						width : 150,
						dataIndex : 'enable',
						tpl : new Ext.XTemplate('{enable:this.renderer}', {
							renderer : function(v) {
								if (v) {
									return "<span style='color:blue'>启用</span>";
								} else {
									return "<span style='color:red'>禁止</span>";
								}
							}
						})
					}, {
						header : '修改时间',
						width : 150,
						dataIndex : 'mdate',
						tpl : new Ext.XTemplate('{mdate:this.renderer}', {
									renderer : function(v) {
										if (v) {
											return new Date(v)
													.format('Y-m-d H:i:s');
										}
										return null;
									}
								})
					}],
			root : new Ext.tree.AsyncTreeNode({
						text : '编码',
						expanded : true,
						id : '-1'
					}),
			loader : new Ext.ux.tree.TreeGridLoader({
						dataUrl : Constants.bizcode
								+ '?pidCode=pid&indexCode=orderBy',
						baseParams : {
							id : '-1'
						},
						requestMethod : 'GET'
					})
		});

		tree.on('click', function(node) {
					var type = node.attributes.code;
					treeGrid.getLoader().baseParams = {
						typeCode : type
					};
					treeGrid.getRootNode().reload();
					treeGrid.node = node;
				}, this);

		this.items = [tree, treeGrid];

		BizCodeModule.superclass.initComponent.call(this);
	}
});

BizCodeMgrModule = Ext.extend(od.Module, {
			moduleName : '业务编码管理',
			moduleId : 'bizmgr',
			iconCls : 'icon-xds',
			components : [BizCodeModule],
			createDefaultComponent : function() {
				return new BizCodeModule();
			}
		});

od.ModuleMgr.registerType('bizmgr', BizCodeMgrModule);
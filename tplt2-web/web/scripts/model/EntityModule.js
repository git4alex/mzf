/**
 * 实体管理
 * 
 * @class EntityMgrModule
 * @extends Ext.Panel
 */
EntityMgrModule = Ext.extend(Ext.Panel, {
	layout : 'border',
	title : '实体管理',
	initComponent : function() {
		// var entityGroup =
		// od.AppInstance.appConfig.bizCode.ENTITY_GROUP;
		// for (var i in entityGroup) {
		// delete entityGroup[i].leaf;
		// }
		this.entityTree = new EntityTree({
			region : 'west',
			cls : 'rightBorder',
			root : new Ext.tree.AsyncTreeNode({
				text : '实体管理',
				expanded : true
					// ,children : entityGroup

				}),
			tbar : [{
						xtype : 'tbseparator'
					}, {
						text : '新增',
						iconCls : 'comm-add',
						scope : this,
						handler : function() {
							try {
								this.addEntity();
							} catch (e) {
								alert(e);
							}
						}
					}, {
						text : '更新',
						iconCls : 'comm-edit',
						scope : this,
						handler : function() {
							try {
								this.editEntity();
							} catch (e) {
								alert(e);
							}
						}
					}, {
						text : '删除',
						iconCls : 'comm-del',
						scope : this,
						handler : function() {
							var current = this;
							var node = this.entityTree.getSelectionModel()
									.getSelectedNode();
							if (node) {
								Ext.Ajax.request({
									url : Constants.updateEntity + node.id,
									scope : this,
									method : 'DELETE',
									success : function(response) {
										result = Ext
												.decode(response.responseText);
										if (result.success) {
											Ext.Msg.alert('消息', "删除成功！",
													function(btn) {
														if (btn == 'ok')
															current.entityTree.root
																	.reload();
													});
										}
									},
									failure : function() {
										Ext.Msg.alert('消息', '操作出错！');
									}
								});
							} else {
								Ext.Msg.alert('提示', '请选择一行！');
							}
						}
					}, {
						xtype : 'tbseparator'
					}, {
						text : '查看',
						iconCls : 'comm-edit',
						scope : this,
						handler : function() {

							var node = this.entityTree.getSelectionModel()
									.getSelectedNode();
							if (!node) {
								Ext.Msg.alert('提示', '请针对某个实体进行操作！')
								return;
							}
							var form = new EntityForm();
							this.setFormValues(form, node);
							Framework.showWin({
										title : '查看实体',
										items : form,
										autoHeight : true,
										scope : this
									});
						}

					}, {
						text : '刷新',
						scope : this,
						iconCls : 'comm-upd',
						handler : function() {
							this.entityTree.root.reload();
						}
					}]
		});

		this.entityTree.getRootNode().expand();

		this.entityTree.on('beforeload', function(node) {
					if (node.attributes.value) {
						this.entityTree.loader.baseParams = {
							entityGroup : node.attributes.value
						};
					}
				}, this);

		this.entityTree.on('click', function(node) {
					if (node.attributes.tableName) {
						this.fieldGrid.entityId = node.id;
						this.fieldGrid.tableName = node.attributes.tableName;
						this.fieldGrid.getStore().baseParams['entityId'] = node.id;
						this.fieldGrid.autoRead = true;
						this.fieldGrid.doLoad();
					} else {
						return false;
					}
				}, this);

		this.entityTree.on('contextmenu', this.doConextMenu, this);

		this.fieldGrid = new EntityFieldGrid({
					rowEditable : true,
					tbar : this.getMenuBar(),
					dataUrl : Constants.queryFieldForPage,
					pageSize : 20
				});
		this.fieldGrid.rowEditor.on('afteredit', this.doAfteredit, this);

		var content = new Ext.Panel({
					border : false,
					region : 'center',
					layout : 'fit',
					cls : 'leftBorder',
					width : 400,
					items : [this.fieldGrid]
				});

		this.items = [this.entityTree, content];
		EntityMgrModule.superclass.initComponent.call(this);

	},

	// 新增删除字段
	doAfteredit : function(roweditor, changes, record, rowIndex) {
		var id = record.get('id');
		entityId = this.fieldGrid.entityId;
		if (!entityId) {
			Ext.Msg.alert('提示', '请针对某个实体进行操作！')
			this.fieldGrid.getStore().reload();
			return;
		}
		record.set('entityId', entityId);
		data = record.data;
		if (!data.length) {
			data.length = null;
		}
		data.orderBy = rowIndex + 2;
		if (id) {
			delete data.entityId;
			Ext.Ajax.request({
						url : Constants.updateField + id,
						jsonData : data,
						method : 'PUT',
						success : function(resp) {
							var result = Ext.decode(resp.responseText);
							if (!result.success)
								Ext.Msg.alert('消息', result.msg);
						},
						failure : function(resp) {
							Ext.Msg.alert('消息', '操作失败！');
						}
					});
		} else {
			Ext.Ajax.request({
						url : Constants.saveField,
						jsonData : data,
						method : 'POST',
						success : function(resp) {
							var result = Ext.decode(resp.responseText);
							if (result.success) {
								record.set('id', result.id);
							} else {
								Ext.Msg.alert('消息', result.msg);
							}
						},
						failure : function(resp) {
							Ext.Msg.alert('消息', '操作失败！');
						}
					});
		}
	},
	doConextMenu : function(node, event) {
		cur = this;
		event.preventDefault();
		if (!node.leaf) {
			return false;
		}
		var nodemenu = new Ext.menu.Menu({
			items : [{
						text : '查看实体',
						scope : this,
						handler : function() {
							var form = new EntityForm();
							cur.setFormValues(form, node);
							Framework.showWin({
										title : '查看实体',
										items : form,
										autoHeight : true,
										scope : this
									});
						}
					}, {
						text : '新增',
						iconCls : 'comm-add',
						scope : this,
						handler : function() {
							this.addEntity();
						}
					}, {
						text : '更新',
						iconCls : 'comm-edit',
						scope : this,
						handler : function() {
							this.editEntity(node);
						}
					}, {
						text : '删除',
						iconCls : 'comm-del',
						scope : this,
						handler : function() {
							var current = this;
							if (node) {
								Ext.Ajax.request({
									url : Constants.updateEntity + node.id,
									scope : this,
									method : 'DELETE',
									success : function(response) {
										result = Ext
												.decode(response.responseText);
										if (result.success) {
											Ext.Msg.alert('消息', "删除成功！",
													function(btn) {
														if (btn == 'ok')
															current.entityTree.root
																	.reload();
													});
										}
									},
									failure : function() {
										Ext.Msg.alert('消息', '操作出错！');
									}
								});
							} else {
								Ext.Msg.alert('提示', '请选择一行！');
							}
						}
					}]
		});
		nodemenu.showAt(event.getXY());
	},
	addEntity : function() {
		var cur = this;
		var entityId = null;
		var node = this.entityTree.getSelectionModel().getSelectedNode();
		if (node) {
			entityId = node.attributes.id;
		}

		var form = new EntityForm({
					entityId : entityId
				});

		Framework.showWin({
					title : '新增实体',
					items : form,
					autoHeight : true,
					scope : this,
					submitHandler : function() {
						if (form.getForm().isValid()) {
							Ext.Ajax.request({
										url : Constants.saveEntity,
										jsonData : EntityUtil.getParam(form,
												true),
										method : 'POST',
										success : function(resp) {
											var result = Ext
													.decode(resp.responseText);
											if (result.success) {
												cur.entityTree.root.reload();
												Framework.closeWin();
											} else {
												Ext.Msg.alert('消息', result.msg);
											}
										},
										failure : function(resp) {
											Ext.Msg.alert('错误',
													resp.responseText);
										}
									});
						}
					}
				});
	},
	editEntity : function(vnode) {
		cur = this;
		var node = null;
		if (vnode)
			node = vnode;
		else
			node = this.entityTree.getSelectionModel().getSelectedNode();
		if (node) {
			var form = new EntityForm({
						updOpt : true,
						entityId : node.parentNode.attributes.id
					});
			this.setFormValues(form, node);
			Framework.showWin({
						title : '更新实体',
						autoHeight : true,
						items : form,
						submitHandler : function() {
							if (form.getForm().isValid()) {
								Ext.Ajax.request({
											url : Constants.updateEntity
													+ node.attributes.id,
											jsonData : EntityUtil
													.getParam(form),
											method : 'PUT',
											success : function(resp) {
												var result = Ext
														.decode(resp.responseText);
												if (result.success) {
													cur.entityTree.root
															.reload();
													Framework.closeWin();
												} else {
													Ext.Msg.alert('消息',
															result.msg);
												}
											},
											failure : function(resp) {
												Ext.Msg.alert('错误',
														resp.responseText);
											}
										});
							}

						}
					});
		} else {
			Ext.Msg.alert('提示', '请选择一行！');
		}
	},

	getMenuBar : function() {
		var saveBtn = {
			text : '新增字段',
			iconCls : 'comm-add',
			scope : this,
			handler : function() {
				this.fieldGrid.rowEditor.stopEditing();
				this.fieldGrid.store.insert(0, this.fieldGrid.getRecord());
				this.fieldGrid.getView().refresh();
				this.fieldGrid.getSelectionModel().selectRow(0);
				this.fieldGrid.rowEditor.startEditing(0);
			}
		};
		var getBtn = {
			text : '从数据库获取',
			iconCls : 'comm-get',
			scope : this,
			handler : function() {
				var tableName = this.fieldGrid.tableName;
				url = Constants.queryListColumsByEntityId + tableName;
				var dataStore = new Ext.data.Store({
							proxy : new Ext.data.HttpProxy({
										url : url
									}),
							reader : new Ext.data.ArrayReader({
										root : 'root'
									}, [{
												name : 'columnName',
												mapping : 'columnName'
											}, {
												name : 'dataType',
												mapping : 'dataType'
											}, {
												name : 'code',
												mapping : 'code'
											}]),
							remoteSort : true,
							autoLoad : true
						});
				dataStore.on('load', function(store, records, options) {
					vstore = this.fieldGrid.store;
					for (j = 0; j < vstore.getCount(); j++) {
						reco = vstore.getAt(j);
						for (i = 0; i < records.length; i++) {
							var rec = records[i];
							if (rec.get('columnName') && reco.get('columnName')) {
								if (rec.get('columnName').toLowerCase() == reco
										.get('columnName').toLowerCase())
									records.remove(rec);
							}
						}
					}
					if (Ext.isEmpty(records)) {
						Ext.Msg.alert('提示', '字段已经全部加载')
						return;
					}
					this.fieldGrid.store.add(records);
				}, this);

				this.fieldGrid.autoRead = true;
				this.fieldGrid.doLoad();
			}
		};
		var deleteBtn = {
			text : '删除字段',
			iconCls : 'comm-del',
			scope : this,
			handler : function() {
				var current = this;
				var record = this.fieldGrid.getSelectionModel().getSelected();
				var rowid = record.data.id
				if (rowid) {
					Ext.Msg.confirm('提示', '是否确定要删除？', function(btn) {
						if (btn == 'yes') {
							Ext.Ajax.request({
										url : Constants.updateField + rowid,
										method : 'DELETE',
										success : function(response) {
											var result = Ext
													.decode(response.responseText);
											if (result.success) {
												current.fieldGrid.store
														.reload();
											}
										},
										failure : function() {
											Ext.Msg.alert('消息', '操作出错！');
										}
									});

						}

					});

				} else {
					Ext.Msg.alert('提示', '请选择一行！');
				}
			}
		};

		var clearCache = {
			text : '清空缓存',
			iconCls : 'comm-edit',
			scope : this,
			handler : function() {
				Ext.Ajax.request({
							url : 'clearMetadataCache',
							method : 'POST',
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result.success) {
									Ext.Msg.alert('提示', result.msg);

								} else {
									Ext.Msg.alert('提示', result.msg)
								}

							},
							failure : function(resp) {
								Ext.Msg.show({
											title : '错误',
											msg : 'status:' + resp.status
													+ '<br/> statusText:'
													+ resp.statusText,
											buttons : Ext.Msg.OK,
											fn : Ext.emptyFn,
											icon : Ext.Msg.ERROR
										});
							}

						});

			}

		};

		function moveUp(grid, vid, step) {
			Ext.Ajax.request({
				url : 'fieldMetadata/move/' + vid + '?step=' + step,
				method : 'PUT',
				scope : this,
				success : function(resp) {
					var result = Ext.decode(resp.responseText);
					if (result.success) {
						grid.getStore().reload();
					} else {
						Ext.Msg.alert('提示', result.msg)
					}
				},
				failure : function(resp) {
					Ext.Msg.show({
								title : '错误',
								msg : 'status:' + resp.status
										+ '<br/> statusText:' + resp.statusText,
								buttons : Ext.Msg.OK,
								fn : Ext.emptyFn,
								icon : Ext.Msg.ERROR
							});
				}
			});
		}

		var upBtn = {
			scope : this,
			text : '上移',
			iconCls : 'comm-edit',
			menu : {
				items : [{
					text : '上移1行',
					scope : this,
					handler : function() {
						var record = this.fieldGrid.getSelectionModel()
								.getSelected();
						if (!record) {
							Ext.Msg.alert('提示', '请选择一行数据进行操作');
							return;
						}
						moveUp(this.fieldGrid, record.data.id, -1);
					}
				}, {
					text : '上移5行',
					scope : this,
					handler : function() {
						var record = this.fieldGrid.getSelectionModel()
								.getSelected();
						if (!record) {
							Ext.Msg.alert('提示', '请选择一行数据进行操作');
							return;
						}
						moveUp(this.fieldGrid, record.data.id, -5);
					}
				}, {
					text : '上移10行',
					scope : this,
					handler : function() {
						var record = this.fieldGrid.getSelectionModel()
								.getSelected();
						if (!record) {
							Ext.Msg.alert('提示', '请选择一行数据进行操作');
							return;
						}
						moveUp(this.fieldGrid, record.data.id, -10);
					}
				}, {
					text : '移至首行',
					scope : this,
					handler : function() {
						var record = this.fieldGrid.getSelectionModel()
								.getSelected();
						if (!record) {
							Ext.Msg.alert('提示', '请选择一行数据进行操作');
							return;
						}
						var rowIndex = this.fieldGrid.getStore()
								.indexOf(record);
						var step = 0 - rowIndex;
						moveUp(this.fieldGrid, record.data.id, step);
					}
				}]
			}
		};

		function moveDown(grid, vid, step) {
			Ext.Ajax.request({
				url : 'fieldMetadata/move/' + vid + '?step=' + step,
				method : 'PUT',
				success : function(resp) {
					var result = Ext.decode(resp.responseText);
					if (result.success) {
						grid.getStore().reload();
					} else {
						Ext.Msg.alert('提示', result.msg)
					}
				},
				failure : function(resp) {
					Ext.Msg.show({
								title : '错误',
								msg : 'status:' + resp.status
										+ '<br/> statusText:' + resp.statusText,
								buttons : Ext.Msg.OK,
								fn : Ext.emptyFn,
								icon : Ext.Msg.ERROR
							});
				}
			});
		}
		var downBtn = {
			scope : this,
			text : '下移',
			iconCls : 'comm-edit',
			menu : {
				items : [{
					text : '下移1行',
					scope : this,
					handler : function() {
						var record = this.fieldGrid.getSelectionModel()
								.getSelected();
						if (!record) {
							Ext.Msg.alert('提示', '请选择一行数据进行操作');
							return;
						}
						moveDown(this.fieldGrid, record.data.id, 1);
					}
				}, {
					text : '下移5行',
					scope : this,
					handler : function() {
						var record = this.fieldGrid.getSelectionModel()
								.getSelected();
						if (!record) {
							Ext.Msg.alert('提示', '请选择一行数据进行操作');
							return;
						}
						moveDown(this.fieldGrid, record.data.id, 5);
					}
				}, {
					text : '下移10行',
					scope : this,
					handler : function() {
						var record = this.fieldGrid.getSelectionModel()
								.getSelected();
						if (!record) {
							Ext.Msg.alert('提示', '请选择一行数据进行操作');
							return;
						}
						moveDown(this.fieldGrid, record.data.id, 10);
					}
				}, {
					text : '移至末行',
					scope : this,
					handler : function() {
						var record = this.fieldGrid.getSelectionModel()
								.getSelected();
						if (!record) {
							Ext.Msg.alert('提示', '请选择一行数据进行操作');
							return;
						}
						var totalCount = this.fieldGrid.getStore()
								.getTotalCount();
						var rowIndex = this.fieldGrid.getStore()
								.indexOf(record);
						var step = totalCount - rowIndex - 1;
						moveDown(this.fieldGrid, record.data.id, step);
					}
				}]
			}
		};

		var updBtn = {
			text : '刷新',
			scope : this,
			iconCls : 'comm-upd',
			handler : function() {
				this.fieldGrid.store.reload();
			}
		};
		return [{
					xtype : 'tbseparator'
				}, saveBtn, deleteBtn, {
					xtype : 'tbseparator'
				}, getBtn, clearCache, {
					xtype : 'tbseparator'
				}, upBtn, downBtn, updBtn];
	},
	setFormValues : function(form, node) {
		form.getForm().setValues({
					id : node.id,
					name : node.text,
					code : node.attributes.code,
					aliasCode : node.attributes.aliasCode,
					entityType : node.attributes.entityType,
					entityGroup : node.attributes.entityGroup,
					entityAttribute : node.attributes.entityAttribute,
					tableName : node.attributes.tableName,
					delField : node.attributes.delField
				});
	}
});
// 实体表单辅助类
EntityUtil = {};
EntityUtil.getParam = function(form, opt) {
	formdata = form.getForm().getValues();
	if (opt == true)
		delete formdata.id;
	if (!formdata.pid && formdata.entityGroup) {
		formdata.pid = formdata.entityGroup;
	}
	return formdata;
};

// 注册实体
EntityModule = Ext.extend(od.Module, {
			moduleId : 'entitymgr',
			moduleName : '实体管理',
			components : [EntityMgrModule],
			createDefaultComponent : function() {
				return new EntityMgrModule();
			}
		});

od.ModuleMgr.registerType('entitymgr', EntityModule);
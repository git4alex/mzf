Ext.ns("com.mzf.common");

com.mzf.common.productInfoWin = {
	xtype : "window",
	width : 710,
	height : 680,
	title : "查看商品详情",
	constrain : true,
	layout : "fit",
	id : "productInfoWin",
	modal : true,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确认",
					ref : "../btnAccept",
					listeners : {
						click : function(btn, evt) {
							btn.refOwner.close();
						}
					}
				}, {
					xtype : "button",
					text : "取消",
					ref : "../btnCancel",
					hidden : true,
					listeners : {
						click : function(btn, evt) {
							if (this.refOwner) {
								this.refOwner.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "panel",
		layout : "border",
		height : 520,
		width : 814,
		tabTip : "商品档案登记",
		border : false,
		items : [{
			xtype : "form",
			labelWidth : 80,
			labelAlign : "left",
			layout : "form",
			border : false,
			autoHeight : true,
			padding : "6",
			region : "north",
			ref : "../form",
			height : 450,
			items : [{
				xtype : "container",
				anchor : "100%",
				layout : "column",
				items : [{
					xtype : "container",
					columnWidth : 1,
					layout : "auto",
					items : [{
						xtype : "container",
						layout : "column",
						autoHeight : true,
						items : [{
							xtype : "container",
							layout : "form",
							columnWidth : 0.5,
							autoHeight : true,
							labelWidth : 80,
							items : [{
										xtype : "textfield",
										fieldLabel : "商品条码",
										anchor : "-20",
										name : "num",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "推广货名称",
										anchor : "-20",
										name : "styleName",
										clearable : true,
										bizType : "productSource",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "商品来源",
										editable : false,
										name : "source",
										readOnly : true,
										bizType : "productSource"
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "商品类型",
										editable : false,
										name : "ptype",
										clearable : true,
										bizType : "productType",
										allowBlank : false,
										blankText : "商品类型是必选项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "商品种类",
										editable : false,
										name : "pkind",
										bizType : "productKind",
										clearable : true,
										allowBlank : false,
										blankText : "商品种类是必选项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "钻石小类",
										editable : false,
										name : "stoneKind",
										bizType : "productStoneKind",
										clearable : true,
										allowBlank : false,
										blankText : "商品种类是必选项",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "商品重量",
										anchor : "-20",
										name : "weight",
										allowBlank : true,
										blankText : "金重是必填项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "主石形状",
										editable : false,
										name : "diamondShape",
										clearable : true,
										bizType : "diamondShape",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "主石重量",
										anchor : "-20",
										name : "diamondSize",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "主石颜色",
										editable : false,
										name : "diamondColor",
										clearable : true,
										bizType : "diamondColor",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "主石净度",
										editable : false,
										name : "diamondClean",
										clearable : true,
										bizType : "diamondClean",
										readOnly : true,
										allowBlank : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "主石切工",
										editable : false,
										name : "diamondCut",
										bizType : "diamondCut",
										clearable : true,
										allowBlank : true,
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "抛光性",
										editable : false,
										name : "polishing",
										clearable : true,
										bizType : "polishing",
										allowBlank : true,
										blankText : "镶嵌方式是必选项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "对称性",
										editable : false,
										name : "symmetry",
										clearable : false,
										bizType : "symmetry",
										allowBlank : true,
										blankText : "镶嵌方式是必选项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "镶嵌方式",
										editable : false,
										name : "inset",
										clearable : true,
										bizType : "insetType",
										allowBlank : true,
										blankText : "镶嵌方式是必选项",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "主石成本",
										anchor : "-20",
										name : "diamondCost",
										readOnly : true,
										id : "diamondCost",
										permissionId : "product.show.diamondCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "证书成本",
										anchor : "-20",
										name : "certCost",
										readOnly : true,
										id : "certCost",
										permissionId : "product.show.certCost"
									},{
										xtype : "numberfield",
										fieldLabel : "其他成本",
										anchor : "-20",
										name : "otherCost",
										readOnly : true,
										id : "certCost",
										permissionId : "product.show.otherCost"
									},{
										xtype : "numberfield",
										fieldLabel : "副石成本",
										anchor : "-20",
										name : "mdiamondCost",
										readOnly : true,
										permissionId : "product.show.mdiamondCost"
									}]
						}, {
							xtype : "container",
							layout : "form",
							columnWidth : 0.5,
							autoHeight : true,
							labelWidth : 80,
							items : [{
										xtype : "textfield",
										fieldLabel : "商品名称",
										anchor : "-20",
										name : "name",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "推广货主题",
										editable : false,
										name : "stylePromotionGroup",
										readOnly : true,
										bizType : "stylePromotionGroup"
									}, {
										xtype : "textfield",
										fieldLabel : "来源标识",
										anchor : "-20",
										name : "sourceId",
										clearable : true,
										bizType : "productSource",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "MZF款号",
										anchor : "-20",
										name : "styleCode",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "厂家款号",
										anchor : "-20",
										name : "vendorStyleCode",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "商品尺寸",
										anchor : "-20",
										name : "size",
										blankText : "尺寸是必填项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "金料成色",
										editable : false,
										name : "goldClass",
										clearable : true,
										bizType : "goldClass",
										allowBlank : true,
										blankText : "金料成色是必选项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "K金颜色",
										editable : false,
										name : "kgoldColor",
										clearable : true,
										bizType : "kGoldColor",
										allowBlank : true,
										blankText : "金料成色是必选项",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "金料重量",
										anchor : "-20",
										name : "goldWeight",
										readOnly : true,
										allowBlank : true
									}, {
										xtype : "numberfield",
										fieldLabel : "计价金重",
										anchor : "-20",
										name : "jjGoldWeight",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "金料成本",
										anchor : "-20",
										name : "goldCost",
										readOnly : true,
										permissionId : "product.show.goldCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "损耗",
										anchor : "-20",
										name : "loss",
										readOnly : true,
										allowBlank : true,
										permissionId : "product.show.loss"
									}, {
										xtype : "numberfield",
										fieldLabel : "基本工费",
										anchor : "-20",
										name : "baseCost",
										readOnly : true,
										permissionId : "product.show.baseCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "工艺工费",
										anchor : "-20",
										name : "craftCost",
										readOnly : true,
										permissionId : "product.show.craftCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "超石工费",
										anchor : "-20",
										name : "beyondDiamontCost",
										readOnly : true,
										permissionId : "product.show.beyondDiamontCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "配件成本",
										anchor : "-20",
										name : "partsCost",
										readOnly : true,
										permissionId : "product.show.partsCost"
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "制作工艺",
										editable : false,
										name : "craft",
										checkable : true,
										bizType : "craft",
										readOnly : true
									},{
										xtype : "textfield",
										fieldLabel : "供应商名称",
										anchor : "-20",
										name : "vendorName",
										readOnly : true
									}]
						}]
					}]
				}, {
					xtype : "container",
					layout : "auto",
					width : 280,
					items : [{
						xtype : "container",
						columnWidth : 0.5,
						layout : "form",
						autoHeight : true,
						labelWidth : 65,
						items : [{
							xtype : "fieldset",
							layout : "fit",
							width : 182,
							autoWidth : true,
							height : 250,
							html : "<img id='imgView' height='240' width='250' src='images/noImage.jpg'>",
							anchor : "-20"
						}, {
							xtype : "trigger",
							anchor : "100%",
							fieldLabel : "商品图片",
							buttonText : "浏览",
							name : "imageId",
							triggerClass : "x-form-search-trigger",
							editable : false,
							readOnly : true,
							listeners : {
								triggerclick : function() {
									var form = Ext.getCmp('regProdForm');
									if (form) {
										form.upload = function(img) {
											form.getForm().setValues({
														imageId : img
													});
											var imgdom = Ext.get('imgView');
											if (imgdom) {
												imgdom.dom.src = 'image/' + img
											}
										}
									}
									od.showWindow('uploadWin');
								}
							}
						}]
					}, {
						xtype : "container",
						layout : "column",
						items : [{
							xtype : "container",
							layout : "form",
							columnWidth : 0.52,
							labelWidth : 65,
							items : [{
										xtype : "numberfield",
										fieldLabel : "核算价",
										anchor : "-10",
										name : "realPrice",
										readOnly : true,
										permissionId : "product.show.realPrice"
									}, {
										xtype : "numberfield",
										fieldLabel : "批发价",
										anchor : "-10",
										name : "wholesalePrice",
										readOnly : true,
										permissionId : "product.show.wholesalePrice"
									}, {
										xtype : "numberfield",
										fieldLabel : "促销一口价",
										anchor : "-10",
										name : "promotionPrice",
										readOnly : true,
										id : "promotionPrice",
										permissionId : "product.show.promotionPrice"
									}, {
										xtype : "checkboxgroup",
										fieldLabel : "不许调换",
										anchor : "100%",
										name : "isTrans",
										readOnly : true,
										items : [{
													xtype : "checkbox",
													boxLabel : " ",
													name : "isTrans",
													value : "true",
													inputValue : "true"
												}]
									}]
						}, {
							xtype : "container",
							layout : "form",
							columnWidth : 0.48,
							labelWidth : 65,
							items : [{
										xtype : "numberfield",
										fieldLabel : "成本价",
										anchor : "100%",
										name : "costPrice",
										readOnly : true,
										permissionId : "product.show.costPrice"
									}, {
										xtype : "numberfield",
										fieldLabel : "一口价",
										anchor : "100%",
										name : "retailBasePrice",
										blankText : "一口价是必填项",
										readOnly : true,
										permissionId : "product.show.retailBasePrice"
									}, {
										xtype : "bizcodebox",
										anchor : "100%",
										fieldLabel : "是否特价",
										editable : false,
										readOnly : true,
										name : "isBargains",
										bizType : "COMMON_BOOL"
									}, {
										xtype : "bizcodebox",
										anchor : "100%",
										fieldLabel : "是否特惠",
										editable : false,
										readOnly : true,
										name : "isPrivilege",
										bizType : "COMMON_BOOL"
									}]
						}, {
							xtype : "container",
							layout : "form",
							columnWidth : 1,
							labelWidth : 65,
							items : [{
										xtype : "textfield",
										fieldLabel : "商品备注",
										anchor : "100%",
										name : "remark",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "其它备注",
										anchor : "100%",
										name : "otherRemark",
										readOnly : true,
										permissionId : "product.show.otherRemark"
									}, {
										xtype : "textfield",
										fieldLabel : "厂家单号",
										anchor : "100%",
										name : "vendorOrderNum",
										readOnly : true
									}]
						}]
					}]
				}]
			}]
		}, {
			xtype : "tabpanel",
			activeTab : 0,
			tabPosition : "bottom",
			border : false,
			region : "center",
			width : 120,
			id : "tabs",
			ref : "../tabs",
			height : 400,
			cls : "topBorder",
			items : [{
				xtype : "editorgrid",
				store : {
					xtype : "jsonstore",
					storeId : "MyStore3",
					url : "entity/certificate",
					requestMethod : "GET",
					root : "root",
					idProperty : "id",
					restful : true,
					autoLoad : true,
					fields : [{
								name : "id",
								type : "integer",
								text : "主键"
							}, {
								name : "productId",
								type : "integer",
								text : "商品_主键"
							}, {
								name : "code",
								type : "string",
								text : "编号"
							}, {
								name : "type",
								type : "string",
								text : "类型"
							}, {
								name : "cost",
								type : "float",
								text : "成本"
							}, {
								name : "remark",
								type : "string",
								text : "备注"
							}],
					listeners : {
						beforeload : function(store, options) {
							var win = Ext.getCmp('productInfoWin');
							store.baseParams = {
								productId : win.proId
							};
						}
					}
				},
				autoExpandColumn : "remark",
				title : "证书信息",
				id : "tab-1",
				columns : [{
							header : "主键",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "id",
							editable : true,
							id : "id",
							hidden : true,
							editor : {
								xtype : "textfield",
								fieldLabel : "Label"
							}
						}, {
							header : "商品_主键",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "productId",
							editable : true,
							id : "productId",
							hidden : true,
							editor : {
								xtype : "textfield",
								fieldLabel : "Label"
							}
						}, {
							header : "证书编号",
							sortable : false,
							resizable : true,
							width : 120,
							menuDisabled : true,
							dataIndex : "code",
							editable : true,
							id : "code",
							editor : {
								xtype : "textfield",
								fieldLabel : "Label"
							}
						}, {
							xtype : "bizcodecolumn",
							header : "证书类型",
							sortable : true,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "type",
							editable : true,
							id : "type",
							bizType : "papersType",
							editor : {
								xtype : "bizcodebox",
								anchor : "100%",
								fieldLabel : "Label",
								editable : false,
								bizType : "papersType"
							}
						}, {
							header : "证书成本",
							sortable : false,
							resizable : true,
							width : 120,
							menuDisabled : true,
							dataIndex : "cost",
							editable : true,
							id : "cost",
							editor : {
								xtype : "numberfield",
								fieldLabel : "Label",
								listeners : {
									change : function(textfield, newValue,
											oldValue) {
										if (newValue != oldValue) {
											if (newValue) {
												var certCost = Ext
														.getCmp('certCost');
												if (certCost) {
													var val = 0;
													if (!Ext.isEmpty(certCost
															.getValue())) {
														val = certCost
																.getValue();
													}
													certCost
															.setValue(parseInt(val)
																	+ parseInt(newValue
																			- oldValue));
												}
											}

										}
									}
								}
							}
						}, {
							header : "备注",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "remark",
							editable : true,
							id : "remark",
							editor : {
								xtype : "textfield",
								fieldLabel : "Label"
							}
						}]
			}, {
				xtype : "grid",
				store : {
					xtype : "jsonstore",
					storeId : "MyStore4",
					url : "entity/diamond",
					requestMethod : "GET",
					root : "root",
					idProperty : "id",
					restful : true,
					autoLoad : true,
					fields : [{
								name : "id",
								type : "integer",
								text : "主键"
							}, {
								name : "productId",
								type : "integer",
								text : "商品ID"
							}, {
								name : "rawmaterialNum",
								type : "string",
								text : "条码"
							}, {
								name : "count",
								type : "integer",
								text : "数量"
							}, {
								name : "cid",
								type : "string",
								text : "证书编号"
							}, {
								name : "weight",
								type : "float",
								text : "大小"
							}, {
								name : "color",
								type : "string",
								text : "颜色"
							}, {
								name : "clean",
								type : "string",
								text : "净度"
							}, {
								name : "cut",
								type : "string",
								text : "切工"
							}, {
								name : "shape",
								type : "string",
								text : "形状"
							}, {
								name : "cost",
								type : "float",
								text : "成本"
							}, {
								name : "remark",
								type : "string",
								text : "备注"
							}, {
								name : "rawmaterialId",
								type : "integer",
								text : "原料ID"
							}],
					listeners : {
						beforeload : function(store, options) {
							var win = Ext.getCmp('productInfoWin');
							store.baseParams = {
								productId : win.proId
							};
						}
					}
				},
				title : "副石信息",
				id : "tab-2",
				columns : [{
							header : "证书编号",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "cid",
							id : "cid"
						}, {
							xtype : "bizcodecolumn",
							header : "形状",
							sortable : true,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "shape",
							id : "shape",
							bizType : "diamondShape"
						}, {
							xtype : "bizcodecolumn",
							header : "颜色",
							sortable : true,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "color",
							id : "color",
							bizType : "diamondColor"
						}, {
							xtype : "bizcodecolumn",
							header : "净度",
							sortable : true,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "clean",
							id : "clean",
							bizType : "diamondClean"
						}, {
							xtype : "bizcodecolumn",
							header : "切工",
							sortable : true,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "cut",
							id : "cut",
							bizType : "masterCut"
						}, {
							header : "重量",
							sortable : false,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "weight",
							id : "weight",
							align : "right"
						}, {
							header : "数量",
							sortable : false,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "count",
							id : "count",
							align : "right"
						}, {
							xtype : "numbercolumn",
							header : "成本",
							sortable : true,
							resizable : true,
							width : 70,
							format : "0,000.00",
							menuDisabled : true,
							dataIndex : "cost",
							id : "cost",
							align : "right"
						}, {
							header : "备注",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "remark",
							id : "remark",
							hidden : true
						}]
			}, {
				xtype : "grid",
				store : {
					xtype : "jsonstore",
					storeId : "MyStore5",
					url : "entity/vBizLog",
					requestMethod : "GET",
					root : "root",
					idProperty : "id",
					restful : true,
					autoLoad : true,
					fields : [{
								name : "id",
								type : "integer",
								text : "主键"
							}, {
								name : "transId",
								type : "integer",
								text : "流程ID"
							}, {
								name : "entityCode",
								type : "string",
								text : "实体Code"
							}, {
								name : "entityId",
								type : "integer",
								text : "实体ID"
							}, {
								name : "targetId",
								type : "integer",
								text : "对象ID"
							}, {
								name : "operate",
								type : "string",
								text : "操作内容"
							}, {
								name : "remark",
								type : "string",
								text : "备注"
							}, {
								name : "cuserId",
								type : "uid",
								text : "操作人ID"
							}, {
								name : "cuserName",
								type : "uname",
								text : "操作人"
							}, {
								name : "cdate",
								type : "date",
								text : "操作时间",
								dateFormat : "time"
							}, {
								name : "billCode",
								type : "string",
								text : "表单Code"
							}, {
								name : "billId",
								type : "integer",
								text : "表单ID"
							}, {
								name : "targetType",
								type : "string",
								text : "对象类型"
							}],
					listeners : {
						beforeload : function(store, options) {
							var win = Ext.getCmp('productInfoWin');
							store.baseParams = {
								targetId : win.proId,
								targetType : 'product'
							};
						}
					}
				},
				title : "流程信息",
				autoExpandColumn : "remark",
				columns : [{
							header : "操作人",
							sortable : false,
							resizable : true,
							width : 120,
							menuDisabled : true,
							dataIndex : "cuserName",
							id : "cuserName"
						}, {
							xtype : "datecolumn",
							header : "操作时间",
							sortable : true,
							resizable : true,
							width : 150,
							format : "Y-m-d H:i:s",
							menuDisabled : true,
							dataIndex : "cdate",
							id : "cdate"
						}, {
							header : "操作内容",
							sortable : false,
							resizable : true,
							width : 150,
							menuDisabled : true,
							dataIndex : "operate",
							id : "operate"
						}, {
							header : "备注",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "remark",
							id : "remark"
						}]
			}]
		}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vProduct',
							method : 'GET',
							params : {
								id : vid
							},
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result) {
									var form = win.form;
									if (form) {
										var data = result.root[0];
										if (data.ownerOrgName) {
											win.setTitle(win.title + '( 所属部门：'
													+ data.ownerOrgName + ')');
										}

										form.getForm().setValues(data);
										var el = Ext.get('imgView');
										if (data.imageId) {
											el.dom.src = 'image/'
													+ data.imageId;
										}
									}
								}
							},
							failure : function(resp) {
								Ext.Msg.alert('错误', resp.responseText);
							}
						});
			}
		}
	}
};

com.mzf.common.secondProductInfoWin = {
	xtype : "window",
	width : 710,
	height : 630,
	title : "查看旧饰详情",
	constrain : true,
	layout : "fit",
	id : "secondProductInfoWin",
	modal : true,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确认",
					ref : "../btnAccept",
					listeners : {
						click : function(btn, evt) {
							btn.refOwner.close();
						}
					}
				}, {
					xtype : "button",
					text : "取消",
					ref : "../btnCancel",
					hidden : true,
					listeners : {
						click : function(btn, evt) {
							if (this.refOwner) {
								this.refOwner.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "panel",
		layout : "border",
		height : 520,
		width : 814,
		tabTip : "商品档案登记",
		border : false,
		items : [{
			xtype : "form",
			labelWidth : 80,
			labelAlign : "left",
			layout : "form",
			border : false,
			autoHeight : true,
			padding : "6",
			region : "north",
			ref : "../form",
			height : 450,
			items : [{
				xtype : "container",
				anchor : "100%",
				layout : "column",
				items : [{
					xtype : "container",
					columnWidth : 1,
					layout : "auto",
					items : [{
						xtype : "container",
						layout : "column",
						autoHeight : true,
						items : [{
							xtype : "container",
							layout : "form",
							columnWidth : 0.5,
							autoHeight : true,
							labelWidth : 80,
							items : [{
										xtype : "textfield",
										fieldLabel : "商品条码",
										anchor : "-20",
										name : "num",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "推广货名称",
										anchor : "-20",
										name : "styleName",
										clearable : true,
										bizType : "productSource",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "商品来源",
										editable : false,
										name : "source",
										readOnly : true,
										bizType : "productSource"
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "商品类型",
										editable : false,
										name : "ptype",
										clearable : true,
										bizType : "productType",
										blankText : "商品类型是必选项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "商品种类",
										editable : false,
										name : "pkind",
										bizType : "productKind",
										clearable : true,
										blankText : "商品种类是必选项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "钻石小类",
										editable : false,
										name : "stoneKind",
										bizType : "productStoneKind",
										blankText : "商品种类是必选项",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "商品重量",
										anchor : "-20",
										name : "weight",
										blankText : "金重是必填项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "主石形状",
										editable : false,
										name : "diamondShape",
										clearable : true,
										bizType : "diamondShape",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "主石重量",
										anchor : "-20",
										name : "diamondSize",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "主石颜色",
										editable : false,
										name : "diamondColor",
										clearable : true,
										bizType : "diamondColor",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "主石净度",
										editable : false,
										name : "diamondClean",
										clearable : true,
										bizType : "diamondClean",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "主石切工",
										editable : false,
										name : "diamondCut",
										bizType : "diamondCut",
										clearable : true,
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "抛光性",
										editable : false,
										name : "polishing",
										clearable : true,
										bizType : "polishing",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "对称性",
										editable : false,
										name : "symmetry",
										clearable : false,
										bizType : "symmetry",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "镶嵌方式",
										editable : false,
										name : "inset",
										clearable : true,
										bizType : "insetType",
										blankText : "镶嵌方式是必选项",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "主石成本",
										anchor : "-20",
										name : "diamondCost",
										readOnly : true,
										id : "diamondCost",
										permissionId : "product.show.diamondCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "证书成本",
										anchor : "-20",
										name : "certCost",
										readOnly : true,
										id : "certCost",
										permissionId : "product.show.certCost"
									}]
						}, {
							xtype : "container",
							layout : "form",
							columnWidth : 0.5,
							autoHeight : true,
							labelWidth : 80,
							items : [{
										xtype : "textfield",
										fieldLabel : "商品名称",
										anchor : "-20",
										name : "name",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "推广货主题",
										editable : false,
										name : "stylePromotionGroup",
										readOnly : true,
										bizType : "stylePromotionGroup"
									}, {
										xtype : "textfield",
										fieldLabel : "来源标识",
										anchor : "-20",
										name : "sourceId",
										clearable : true,
										bizType : "productSource",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "MZF款号",
										anchor : "-20",
										name : "styleCode",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "厂家款号",
										anchor : "-20",
										name : "vendorStyleCode",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "商品尺寸",
										anchor : "-20",
										name : "size",
										blankText : "尺寸是必填项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "金料成色",
										editable : false,
										name : "goldClass",
										clearable : true,
										bizType : "goldClass",
										blankText : "金料成色是必选项",
										readOnly : true
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "K金颜色",
										editable : false,
										name : "kgoldColor",
										clearable : true,
										bizType : "kGoldColor",
										blankText : "金料成色是必选项",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "金料重量",
										anchor : "-20",
										name : "goldWeight",
										readOnly : true
									}, {
										xtype : "numberfield",
										fieldLabel : "计价金重",
										anchor : "-20",
										name : "jjGoldWeight",
										readOnly : true,
										allowBlank : true
									}, {
										xtype : "numberfield",
										fieldLabel : "金料成本",
										anchor : "-20",
										name : "goldCost",
										allowBlank : true,
										readOnly : true,
										permissionId : "product.show.goldCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "损耗",
										anchor : "-20",
										name : "loss",
										readOnly : true,
										allowBlank : true,
										permissionId : "product.show.loss"
									}, {
										xtype : "numberfield",
										fieldLabel : "基本工费",
										anchor : "-20",
										name : "baseCost",
										readOnly : true,
										allowBlank : true,
										permissionId : "product.show.baseCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "工艺工费",
										anchor : "-20",
										name : "craftCost",
										readOnly : true,
										allowBlank : true,
										permissionId : "product.show.craftCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "超石工费",
										anchor : "-20",
										name : "beyondDiamontCost",
										readOnly : true,
										permissionId : "product.show.beyondDiamontCost"
									}, {
										xtype : "numberfield",
										fieldLabel : "配件成本",
										anchor : "-20",
										name : "partsCost",
										readOnly : true,
										allowBlank : true,
										permissionId : "product.show.partsCost"
									}, {
										xtype : "bizcodebox",
										anchor : "-20",
										fieldLabel : "制作工艺",
										editable : false,
										name : "craft",
										checkable : true,
										bizType : "craft",
										readOnly : true
									}]
						}]
					}]
				}, {
					xtype : "container",
					layout : "auto",
					width : 280,
					items : [{
						xtype : "container",
						columnWidth : 0.5,
						layout : "form",
						autoHeight : true,
						labelWidth : 65,
						items : [{
							xtype : "fieldset",
							layout : "fit",
							width : 182,
							autoWidth : true,
							height : 250,
							html : "<img id='imgView' height='240' width='250' src='images/noImage.jpg'>",
							anchor : "100%"
						}, {
							xtype : "trigger",
							anchor : "100%",
							fieldLabel : "商品图片",
							buttonText : "浏览",
							name : "imageId",
							triggerClass : "x-form-search-trigger",
							editable : false,
							readOnly : true,
							listeners : {
								triggerclick : function() {
									var form = Ext.getCmp('regProdForm');
									if (form) {
										form.upload = function(img) {
											form.getForm().setValues({
														imageId : img
													});
											var imgdom = Ext.get('imgView');
											if (imgdom) {
												imgdom.dom.src = 'image/' + img
											}
										}
									}
									od.showWindow('uploadWin');
								}
							}
						}]
					}, {
						xtype : "container",
						layout : "column",
						items : [{
							xtype : "container",
							layout : "form",
							columnWidth : 0.52,
							labelWidth : 65,
							items : [{
										xtype : "numberfield",
										fieldLabel : "核算价",
										anchor : "-10",
										name : "realPrice",
										allowBlank : true,
										readOnly : true,
										permissionId : "product.show.realPrice"
									}, {
										xtype : "numberfield",
										fieldLabel : "批发价",
										anchor : "-10",
										name : "wholesalePrice",
										readOnly : true,
										allowBlank : true,
										permissionId : "product.show.wholesalePrice"
									}, {
										xtype : "numberfield",
										fieldLabel : "原售价",
										anchor : "-10",
										name : "finalSelledPrice",
										readOnly : true,
										id : "finalSelledPrice"
									}]
						}, {
							xtype : "container",
							layout : "form",
							columnWidth : 0.48,
							labelWidth : 65,
							items : [{
										xtype : "numberfield",
										fieldLabel : "成本价",
										anchor : "100%",
										name : "costPrice",
										readOnly : true,
										allowBlank : true,
										permissionId : "product.show.costPrice"
									}, {
										xtype : "numberfield",
										fieldLabel : "一口价",
										anchor : "100%",
										name : "retailBasePrice",
										allowBlank : true,
										blankText : "一口价是必填项",
										readOnly : true,
										permissionId : "product.show.retailBasePrice"
									}, {
										xtype : "bizcodebox",
										anchor : "100%",
										fieldLabel : "是否特价",
										editable : false,
										readOnly : true,
										name : "isBargains",
										bizType : "COMMON_BOOL"
									}]
						}, {
							xtype : "container",
							layout : "form",
							columnWidth : 1,
							labelWidth : 65,
							items : [{
										xtype : "numberfield",
										fieldLabel : "回收价",
										anchor : "100%",
										name : "buyPrice",
										readOnly : true,
										id : "buyPrice",
										allowBlank : true
									}, {
										xtype : "textfield",
										fieldLabel : "商品备注",
										anchor : "100%",
										name : "remark",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "其它备注",
										anchor : "100%",
										name : "otherRemark",
										readOnly : true
									}]
						}]
					}]
				}]
			}]
		}, {
			xtype : "tabpanel",
			activeTab : 0,
			tabPosition : "bottom",
			border : false,
			region : "center",
			width : 120,
			id : "tabs",
			ref : "../tabs",
			height : 300,
			cls : "topBorder",
			items : [{
				xtype : "editorgrid",
				store : {
					xtype : "jsonstore",
					storeId : "MyStore7",
					url : "entity/certificate",
					requestMethod : "GET",
					root : "root",
					idProperty : "id",
					restful : true,
					autoLoad : true,
					fields : [{
								name : "id",
								type : "integer",
								text : "主键"
							}, {
								name : "productId",
								type : "integer",
								text : "商品_主键"
							}, {
								name : "code",
								type : "string",
								text : "编号"
							}, {
								name : "type",
								type : "string",
								text : "类型"
							}, {
								name : "cost",
								type : "float",
								text : "成本"
							}, {
								name : "remark",
								type : "string",
								text : "备注"
							}],
					listeners : {
						beforeload : function(store, options) {
							var win = Ext.getCmp('secondProductInfoWin');
							store.baseParams = {
								productId : win.proId
							};
						}
					}
				},
				autoExpandColumn : "remark",
				title : "证书信息",
				id : "tab-1",
				columns : [{
							header : "主键",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "id",
							editable : true,
							id : "id",
							hidden : true,
							editor : {
								xtype : "textfield",
								fieldLabel : "Label"
							}
						}, {
							header : "商品_主键",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "productId",
							editable : true,
							id : "productId",
							hidden : true,
							editor : {
								xtype : "textfield",
								fieldLabel : "Label"
							}
						}, {
							header : "证书编号",
							sortable : false,
							resizable : true,
							width : 120,
							menuDisabled : true,
							dataIndex : "code",
							editable : true,
							id : "code",
							editor : {
								xtype : "textfield",
								fieldLabel : "Label"
							}
						}, {
							xtype : "bizcodecolumn",
							header : "证书类型",
							sortable : true,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "type",
							editable : true,
							id : "type",
							bizType : "papersType",
							editor : {
								xtype : "bizcodebox",
								anchor : "100%",
								fieldLabel : "Label",
								editable : false,
								bizType : "papersType"
							}
						}, {
							header : "证书成本",
							sortable : false,
							resizable : true,
							width : 120,
							menuDisabled : true,
							dataIndex : "cost",
							editable : true,
							id : "cost",
							editor : {
								xtype : "numberfield",
								fieldLabel : "Label",
								listeners : {
									change : function(textfield, newValue,
											oldValue) {
										if (newValue != oldValue) {
											if (newValue) {
												var certCost = Ext
														.getCmp('certCost');
												if (certCost) {
													var val = 0;
													if (!Ext.isEmpty(certCost
															.getValue())) {
														val = certCost
																.getValue();
													}
													certCost
															.setValue(parseInt(val)
																	+ parseInt(newValue
																			- oldValue));
												}
											}

										}
									}
								}
							}
						}, {
							header : "备注",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "remark",
							editable : true,
							id : "remark",
							editor : {
								xtype : "textfield",
								fieldLabel : "Label"
							}
						}]
			}, {
				xtype : "grid",
				store : {
					xtype : "jsonstore",
					storeId : "MyStore8",
					url : "entity/diamond",
					requestMethod : "GET",
					root : "root",
					idProperty : "id",
					restful : true,
					autoLoad : true,
					fields : [{
								name : "id",
								type : "integer",
								text : "主键"
							}, {
								name : "productId",
								type : "integer",
								text : "商品ID"
							}, {
								name : "rawmaterialNum",
								type : "string",
								text : "条码"
							}, {
								name : "count",
								type : "integer",
								text : "数量"
							}, {
								name : "cid",
								type : "string",
								text : "证书编号"
							}, {
								name : "weight",
								type : "float",
								text : "大小"
							}, {
								name : "color",
								type : "string",
								text : "颜色"
							}, {
								name : "clean",
								type : "string",
								text : "净度"
							}, {
								name : "cut",
								type : "string",
								text : "切工"
							}, {
								name : "shape",
								type : "string",
								text : "形状"
							}, {
								name : "cost",
								type : "float",
								text : "成本"
							}, {
								name : "remark",
								type : "string",
								text : "备注"
							}, {
								name : "rawmaterialId",
								type : "integer",
								text : "原料ID"
							}],
					listeners : {
						beforeload : function(store, options) {
							var win = Ext.getCmp('secondProductInfoWin');
							store.baseParams = {
								productId : win.proId
							};
						}
					}
				},
				title : "副石信息",
				id : "tab-2",
				columns : [{
							header : "证书编号",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "cid",
							id : "cid"
						}, {
							xtype : "bizcodecolumn",
							header : "形状",
							sortable : true,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "shape",
							id : "shape",
							bizType : "diamondShape"
						}, {
							xtype : "bizcodecolumn",
							header : "颜色",
							sortable : true,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "color",
							id : "color",
							bizType : "diamondColor"
						}, {
							xtype : "bizcodecolumn",
							header : "净度",
							sortable : true,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "clean",
							id : "clean",
							bizType : "diamondClean"
						}, {
							xtype : "bizcodecolumn",
							header : "切工",
							sortable : true,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "cut",
							id : "cut",
							bizType : "masterCut"
						}, {
							header : "重量",
							sortable : false,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "weight",
							id : "weight",
							align : "right"
						}, {
							header : "数量",
							sortable : false,
							resizable : true,
							width : 70,
							menuDisabled : true,
							dataIndex : "count",
							id : "count",
							align : "right"
						}, {
							xtype : "numbercolumn",
							header : "成本",
							sortable : true,
							resizable : true,
							width : 70,
							format : "0,000.00",
							menuDisabled : true,
							dataIndex : "cost",
							id : "cost",
							align : "right"
						}, {
							header : "备注",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "remark",
							id : "remark",
							hidden : true
						}]
			}, {
				xtype : "grid",
				store : {
					xtype : "jsonstore",
					storeId : "MyStore9",
					url : "entity/vBizLog",
					requestMethod : "GET",
					root : "root",
					idProperty : "id",
					restful : true,
					autoLoad : true,
					fields : [{
								name : "id",
								type : "integer",
								text : "主键"
							}, {
								name : "transId",
								type : "integer",
								text : "流程ID"
							}, {
								name : "entityCode",
								type : "string",
								text : "实体Code"
							}, {
								name : "entityId",
								type : "integer",
								text : "实体ID"
							}, {
								name : "targetId",
								type : "integer",
								text : "对象ID"
							}, {
								name : "operate",
								type : "string",
								text : "操作内容"
							}, {
								name : "remark",
								type : "string",
								text : "备注"
							}, {
								name : "cuserId",
								type : "uid",
								text : "操作人ID"
							}, {
								name : "cuserName",
								type : "uname",
								text : "操作人"
							}, {
								name : "cdate",
								type : "date",
								text : "操作时间",
								dateFormat : "time"
							}, {
								name : "billCode",
								type : "string",
								text : "表单Code"
							}, {
								name : "billId",
								type : "integer",
								text : "表单ID"
							}, {
								name : "targetType",
								type : "string",
								text : "对象类型"
							}],
					listeners : {
						beforeload : function(store, options) {
							var win = Ext.getCmp('secondProductInfoWin');
							store.baseParams = {
								targetId : win.proId,
								targetType : 'product'
							};
						}
					}
				},
				title : "流程信息",
				autoExpandColumn : "remark",
				columns : [{
							header : "操作人",
							sortable : false,
							resizable : true,
							width : 120,
							menuDisabled : true,
							dataIndex : "cuserName",
							id : "cuserName"
						}, {
							xtype : "datecolumn",
							header : "操作时间",
							sortable : true,
							resizable : true,
							width : 150,
							format : "Y-m-d H:i:s",
							menuDisabled : true,
							dataIndex : "cdate",
							id : "cdate"
						}, {
							header : "操作内容",
							sortable : false,
							resizable : true,
							width : 150,
							menuDisabled : true,
							dataIndex : "operate",
							id : "operate"
						}, {
							header : "备注",
							sortable : false,
							resizable : true,
							width : 100,
							menuDisabled : true,
							dataIndex : "remark",
							id : "remark"
						}]
			}]
		}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vSecondProduct',
							method : 'GET',
							params : {
								id : vid
							},
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result) {
									var form = win.form;
									if (form) {
										var data = result.root[0];
										if (data) {
											form.getForm().setValues(data);
											var el = Ext.get('imgView');
											if (data.imageId) {
												el.dom.src = "image/"
														+ data.imageId;
											}
										}
									}
								}
							},
							failure : function(resp) {
								Ext.Msg.alert('错误', resp.responseText);
							}
						});
			}
		}
	}
};

com.mzf.common.custDetailWin = {
	xtype : "window",
	width : 791,
	height : 600,
	title : "查看详情",
	constrain : true,
	layout : "fit",
	id : "custDetailWin",
	autoHeight : true,
	stateful : false,
	modal : true,
	resizable : false,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确定",
					ref : "../btnCancel",
					listeners : {
						click : function(btn, evt) {
							this.refOwner.close();
						}
					}
				}]
	},
	items : [{
				xtype : "form",
				labelWidth : 80,
				labelAlign : "left",
				layout : "form",
				border : false,
				id : "customerInfoForm",
				autoHeight : true,
				padding : "6",
				ref : "form",
				stateful : false,
				items : [{
							xtype : "container",
							anchor : "100%",
							layout : "column",
							items : [{
										xtype : "container",
										layout : "form",
										columnWidth : 0.25,
										items : [{
													xtype : "hidden",
													fieldLabel : "Label",
													anchor : "100%",
													name : "id"
												}, {
													xtype : "textfield",
													fieldLabel : "门店名称",
													anchor : "-10",
													name : "orgName",
													readOnly : true
												}, {
													xtype : "textfield",
													fieldLabel : "会员卡号",
													anchor : "-10",
													name : "cardNo",
													readOnly : true
												}, {
													xtype : "textfield",
													fieldLabel : "客户姓名",
													anchor : "-10",
													name : "name",
													allowBlank : false,
													blankText : "名称不能为空",
													readOnly : true
												}, {
													xtype : "bizcodebox",
													anchor : "-10",
													fieldLabel : "职业",
													editable : false,
													name : "vocation",
													bizType : "vocation",
													readOnly : true
												}, {
													xtype : "textfield",
													fieldLabel : "当月消费额",
													anchor : "-10",
													name : "currentMonthAmount",
													readOnly : true
												}, {
													xtype : "textfield",
													fieldLabel : "总积分",
													anchor : "-10",
													name : "points",
													readOnly : true
												}]
									}, {
										xtype : "container",
										layout : "form",
										columnWidth : 0.25,
										labelWidth : 80,
										items : [{
													xtype : "textfield",
													fieldLabel : "珠宝顾问",
													anchor : "-10",
													valueField : "id",
													displayField : "name",
													name : "salesmanName",
													id : "salesmanId",
													triggerAction : "all",
													readOnly : true
												}, {
													xtype : "datefield",
													anchor : "-10",
													fieldLabel : "办卡时间",
													name : "grantDate",
													format : "Y-m-d",
													readOnly : true
												}, {
													xtype : "bizcodebox",
													anchor : "-10",
													fieldLabel : "性别",
													editable : false,
													name : "sex",
													bizType : "personSex",
													readOnly : true
												}, {
													xtype : "textfield",
													fieldLabel : "职位",
													anchor : "-10",
													name : "post",
													readOnly : true
												}, {
													xtype : "textfield",
													fieldLabel : "总消费金额",
													anchor : "-10",
													name : "totalAmount",
													readOnly : true
												}, {
													xtype : "datefield",
													anchor : "-10",
													fieldLabel : "积分有效期",
													name : "pointsIndate",
													readOnly : true,
													format : "Y-m-d"
												}]
									}, {
										xtype : "container",
										layout : "form",
										columnWidth : 0.25,
										labelWidth : 80,
										items : [{
													xtype : "bizcodebox",
													anchor : "-10",
													fieldLabel : "客户类型",
													editable : false,
													name : "type",
													bizType : "cusType",
													clearable : false,
													readOnly : true
												}, {
													xtype : "datefield",
													anchor : "-10",
													fieldLabel : "卡有效期",
													name : "cardIndate",
													format : "Y-m-d",
													readOnly : true
												}, {
													xtype : "bizcodebox",
													anchor : "-10",
													fieldLabel : "婚姻状况",
													editable : false,
													name : "marriage",
													bizType : "marriage",
													readOnly : true
												}, {
													xtype : "textfield",
													fieldLabel : "购买目的",
													anchor : "-10",
													readOnly : true,
													name : "buyTarget"
												}, {
													xtype : "textfield",
													fieldLabel : "当月积分",
													anchor : "-10",
													name : "currentMonthPoints",
													readOnly : true,
													format : "Y-m-d"
												}]
									}, {
										xtype : "container",
										layout : "form",
										columnWidth : 0.25,
										labelWidth : 80,
										items : [{
													xtype : "bizcodebox",
													anchor : "100%",
													fieldLabel : "级别",
													editable : false,
													name : "grade",
													bizType : "cusGrade",
													clearable : true,
													readOnly : true
												}, {
													xtype : "textfield",
													fieldLabel : "客户来源",
													anchor : "100%",
													name : "source",
													readOnly : true
												}, {
													xtype : "bizcodebox",
													anchor : "100%",
													fieldLabel : "月收入",
													editable : false,
													readOnly : true,
													name : "salary",
													bizType : "salary"
												}, {
													xtype : "numberfield",
													fieldLabel : "当年消费额",
													anchor : "100%",
													readOnly : true,
													name : "currentYearAmount"
												}, {
													xtype : "textfield",
													fieldLabel : "当年积分",
													anchor : "100%",
													readOnly : true,
													name : "currentYearPoints"
												}]
									}]
						}]
			}, {
				xtype : "tabpanel",
				activeTab : 0,
				height : 200,
				tabPosition : "bottom",
				border : false,
				ref : "tabs",
				cls : "topBorder",
				hidden : true,
				items : [{
							xtype : "form",
							labelWidth : 70,
							labelAlign : "left",
							layout : "form",
							border : false,
							title : "敏感信息",
							padding : "6",
							ref : "../cusForm",
							items : [{
										xtype : "container",
										anchor : "100%",
										layout : "column",
										items : [{
													xtype : "container",
													layout : "form",
													columnWidth : 0.25,
													items : [{
																xtype : "numberfield",
																fieldLabel : "年龄",
																anchor : "-10",
																readOnly : true,
																name : "age",
																width : 120
															}, {
																xtype : "textfield",
																fieldLabel : "电话",
																anchor : "-10",
																name : "tel",
																blankText : "电话是必填项",
																readOnly : true
															}, {
																xtype : "textfield",
																fieldLabel : "单位",
																anchor : "-10",
																name : "company",
																readOnly : true
															}]
												}, {
													xtype : "container",
													layout : "form",
													columnWidth : 0.25,
													labelWidth : 80,
													items : [{
																xtype : "datefield",
																anchor : "-10",
																fieldLabel : "生日",
																name : "birthday",
																format : "Y-m-d",
																readOnly : true
															}, {
																xtype : "textfield",
																fieldLabel : "手机",
																anchor : "-10",
																name : "mobile",
																allowBlank : true,
																readOnly : true
															}, {
																xtype : "textfield",
																fieldLabel : "邮编",
																anchor : "-10",
																name : "postcode",
																readOnly : true
															}]
												}, {
													xtype : "container",
													layout : "form",
													columnWidth : 0.25,
													labelWidth : 80,
													items : [{
																xtype : "textfield",
																fieldLabel : "身份证",
																anchor : "-10",
																name : "cid",
																readOnly : true
															}, {
																xtype : "textfield",
																fieldLabel : "电子邮件",
																anchor : "-10",
																name : "email",
																readOnly : true
															}]
												}, {
													xtype : "container",
													layout : "form",
													columnWidth : 0.25,
													labelWidth : 80,
													items : [{
																xtype : "datefield",
																anchor : "100%",
																fieldLabel : "结婚纪念日",
																name : "comDay",
																format : "Y-m-d",
																readOnly : true
															}, {
																xtype : "textfield",
																fieldLabel : "QQ/MSN",
																anchor : "100%",
																name : "im",
																readOnly : true
															}]
												}]
									}, {
										xtype : "textfield",
										fieldLabel : "地址",
										anchor : "100%",
										name : "address",
										readOnly : true
									}, {
										xtype : "textarea",
										fieldLabel : "备注",
										anchor : "100%",
										readOnly : true,
										name : "remark"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore2",
								url : "entity/vSale?type=sale",
								requestMethod : "GET",
								root : "root",
								idProperty : "id",
								remoteSort : false,
								restful : true,
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "num",
											type : "string",
											text : "编号"
										}, {
											name : "saleId",
											type : "integer",
											text : "销售单ID"
										}, {
											name : "saleNum",
											type : "string",
											text : "退货单对应的销售单"
										}, {
											name : "orgId",
											type : "integer",
											text : "组织机构ID"
										}, {
											name : "orgName",
											type : "string",
											text : "门店"
										}, {
											name : "type",
											type : "string",
											text : "销售类型"
										}, {
											name : "orgCode",
											type : "string",
											text : "组织机构Code"
										}, {
											name : "orgTel",
											type : "string",
											text : "门店电话"
										}, {
											name : "orgAddress",
											type : "string",
											text : "门店地址"
										}, {
											name : "employeeId",
											type : "integer",
											text : "销售顾问ID"
										}, {
											name : "employeeName",
											type : "string",
											text : "销售顾问"
										}, {
											name : "checkerId",
											type : "integer",
											text : "维修人员ID"
										}, {
											name : "checkerName",
											type : "string",
											text : "维修人员"
										}, {
											name : "goldCheckerId",
											type : "integer",
											text : "验金人ID"
										}, {
											name : "goldCheckerName",
											type : "string",
											text : "验金人"
										}, {
											name : "cusId",
											type : "integer",
											text : "客户ID"
										}, {
											name : "cusName",
											type : "string",
											text : "客户姓名"
										}, {
											name : "mobile",
											type : "string",
											text : "客户手机"
										}, {
											name : "cardNo",
											type : "string",
											text : "会员卡号"
										}, {
											name : "grade",
											type : "string",
											text : "会员级别"
										}, {
											name : "historyPoints",
											type : "float",
											text : "历史积分"
										}, {
											name : "points",
											type : "float",
											text : "本次积分"
										}, {
											name : "exchangePoints",
											type : "float",
											text : "兑换积分"
										}, {
											name : "totalAmount",
											type : "float",
											text : "应收金额"
										}, {
											name : "amount",
											type : "float",
											text : "实收金额"
										}, {
											name : "discount",
											type : "string",
											text : "折扣"
										}, {
											name : "frontMoney",
											type : "string",
											text : "预付定金"
										}, {
											name : "goldPay",
											type : "float",
											text : "旧金支付"
										}, {
											name : "productPay",
											type : "float",
											text : "旧饰支付"
										}, {
											name : "marketProxy",
											type : "float",
											text : "商场代收"
										}, {
											name : "marketProxyCash",
											type : "float",
											text : "商场代收现金"
										}, {
											name : "marketProxyBankCard",
											type : "float",
											text : "商场代收银联卡"
										}, {
											name : "marketProxyChit",
											type : "float",
											text : "商场代收代金券"
										}, {
											name : "marketProxyValueCard",
											type : "float",
											text : "商场代收储值卡"
										}, {
											name : "marketProxyOther",
											type : "float",
											text : "商场代收其它"
										}, {
											name : "marketProxyRemark",
											type : "string",
											text : "商场代收备注"
										}, {
											name : "bankCard",
											type : "float",
											text : "银联卡卡支付"
										}, {
											name : "bankCard1",
											type : "float",
											text : "银联卡1"
										}, {
											name : "bankCardBank1",
											type : "string",
											text : "银联卡1银行"
										}, {
											name : "bankCard2",
											type : "float",
											text : "银联卡2"
										}, {
											name : "bankCardBank2",
											type : "string",
											text : "银联卡2银行"
										}, {
											name : "bankCard3",
											type : "float",
											text : "银联卡3"
										}, {
											name : "bankCardBank3",
											type : "string",
											text : "银联卡3银行"
										}, {
											name : "valueCard",
											type : "float",
											text : "储值卡支付"
										}, {
											name : "valueCard1",
											type : "float",
											text : "储值卡1"
										}, {
											name : "valueCardType1",
											type : "string",
											text : "储值卡1类型"
										}, {
											name : "valueCard2",
											type : "float",
											text : "储值卡2"
										}, {
											name : "valueCardType2",
											type : "string",
											text : "储值卡2类型"
										}, {
											name : "valueCard3",
											type : "float",
											text : "储值卡3"
										}, {
											name : "valueCardType3",
											type : "string",
											text : "储值卡3类型"
										}, {
											name : "coBrandedCard",
											type : "float",
											text : "联名卡"
										}, {
											name : "coBrandedCard1",
											type : "float",
											text : "联名卡1"
										}, {
											name : "coBrandedCardBank1",
											type : "string",
											text : "联名卡1银行"
										}, {
											name : "coBrandedCard2",
											type : "float",
											text : "联名卡2"
										}, {
											name : "coBrandedCardBank2",
											type : "string",
											text : "联名卡2银行"
										}, {
											name : "coBrandedCard3",
											type : "float",
											text : "联名卡3"
										}, {
											name : "coBrandedCardBank3",
											type : "string",
											text : "联名卡3银行"
										}, {
											name : "foreignCard",
											type : "float",
											text : "外卡"
										}, {
											name : "foreignCard1",
											type : "float",
											text : "外卡1"
										}, {
											name : "foreignCardType1",
											type : "string",
											text : "外卡1类型"
										}, {
											name : "foreignCard2",
											type : "float",
											text : "外卡2"
										}, {
											name : "foreignCardType2",
											type : "string",
											text : "外卡2类型"
										}, {
											name : "foreignCard3",
											type : "float",
											text : "外卡3"
										}, {
											name : "foreignCardType3",
											type : "string",
											text : "外卡3类型"
										}, {
											name : "cash",
											type : "float",
											text : "现金支付"
										}, {
											name : "chit",
											type : "float",
											text : "代金券"
										}, {
											name : "transfer",
											type : "float",
											text : "转账支付"
										}, {
											name : "other",
											type : "float",
											text : "其它"
										}, {
											name : "status",
											type : "string",
											text : "状态"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cuserId",
											type : "uid",
											text : "操作人ID"
										}, {
											name : "isReturns",
											type : "string",
											text : "是否全部退货"
										}, {
											name : "cdate",
											type : "date",
											text : "操作时间",
											dateFormat : "time"
										}, {
											name : "isReturns2",
											type : "string",
											text : "是否全部退货2"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('custDetailWin');
										store.setBaseParam('cusId', win.cusId);
									}
								}
							},
							autoExpandColumn : "remark",
							id : "salesGrid",
							border : false,
							title : "销售记录",
							ref : "../saleGrid",
							columns : [{
										xtype : "datecolumn",
										header : "销售时间",
										sortable : true,
										resizable : true,
										width : 90,
										format : "Y-m-d",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "门店",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "orgName",
										id : "orgName"
									}, {
										header : "销售单号",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "num",
										id : "num"
									}, {
										header : "销售顾问",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "employeeName",
										id : "employeeName"
									}, {
										header : "验货人",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "checkerName",
										id : "checkerName"
									}, {
										xtype : "numbercolumn",
										header : "应收金额",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "totalAmount",
										id : "totalAmount",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "旧金支付",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "goldPay",
										id : "goldPay",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "旧饰支付",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "productPay",
										id : "productPay",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "银联卡支付",
										sortable : true,
										resizable : true,
										width : 85,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "bankCard",
										id : "bankCard",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "储值卡支付",
										sortable : true,
										resizable : true,
										width : 85,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "valueCard",
										id : "valueCard",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "现金支付",
										sortable : true,
										resizable : true,
										width : 85,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "cash",
										id : "cash",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "代金券支付",
										sortable : true,
										resizable : true,
										width : 85,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "chit",
										id : "chit",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "转账支付",
										sortable : true,
										resizable : true,
										width : 85,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "transfer",
										id : "transfer",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "其它",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "other",
										id : "other",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "商场代收",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "marketProxy",
										id : "marketProxy",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "预付定金",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "frontMoney",
										id : "frontMoney",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "折扣",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "discount",
										id : "discount",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "历史积分",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "historyPoints",
										id : "historyPoints",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "本次积分",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "points",
										id : "points",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "积分抵",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "exchangePoints",
										id : "exchangePoints",
										align : "right"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}],
							tbar : {
								xtype : "toolbar",
								items : [{
											xtype : "tbseparator"
										}, {
											xtype : "button",
											text : "查看详情",
											ref : "../lookInfo",
											listeners : {
												click : function(btn, evt) {
													var grid = btn.refOwner;
													if (grid) {
														var record = grid
																.getSelectionModel()
																.getSelected();
														if (!record) {
															Ext.Msg
																	.alert(
																			'提示',
																			'请选择一条销售记录进行查看');
															return;
														}
														var win = Ext
																.create(com.mzf.common.saleDetailInfoWin);
														if (win) {
															win.saleId = record.data.id;
															win.show();
															win
																	.preview(record.data.id);
														}
													}
												}
											}
										}]
							}
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore3",
								url : "entity/vCusOrder",
								requestMethod : "GET",
								root : "root",
								idProperty : "id",
								remoteSort : false,
								restful : true,
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "cusId",
											type : "integer",
											text : "客户_主键"
										}, {
											name : "productId",
											type : "integer",
											text : "商品_主键"
										}, {
											name : "deliveryDate",
											type : "string",
											text : "取货时间"
										}, {
											name : "tel",
											type : "string",
											text : "联系电话"
										}, {
											name : "productNum",
											type : "string",
											text : "商品条码"
										}, {
											name : "styleId",
											type : "integer",
											text : "MZF款号ID"
										}, {
											name : "styleCode",
											type : "string",
											text : "MZF款号"
										}, {
											name : "ptype",
											type : "string",
											text : "商品类型"
										}, {
											name : "pkind",
											type : "string",
											text : "商品种类"
										}, {
											name : "color",
											type : "string",
											text : "主石颜色"
										}, {
											name : "price",
											type : "string",
											text : "一口价"
										}, {
											name : "clean",
											type : "string",
											text : "主石净度"
										}, {
											name : "size",
											type : "string",
											text : "商品尺寸"
										}, {
											name : "cut",
											type : "string",
											text : "主石切工"
										}, {
											name : "polishing",
											type : "string",
											text : "抛光性"
										}, {
											name : "symmetry",
											type : "string",
											text : "对称性"
										}, {
											name : "goldClass",
											type : "string",
											text : "金料成色"
										}, {
											name : "craft",
											type : "string",
											text : "制作工艺"
										}, {
											name : "diamondId",
											type : "integer",
											text : "裸钻ID"
										}, {
											name : "diamondNum",
											type : "string",
											text : "裸石编码"
										}, {
											name : "cid",
											type : "string",
											text : "证书编码"
										}, {
											name : "otherNum",
											type : "string",
											text : "其它编码"
										}, {
											name : "employeeId",
											type : "integer",
											text : "销售顾问ID"
										}, {
											name : "employeeName",
											type : "string",
											text : "销售顾问"
										}, {
											name : "status",
											type : "string",
											text : "订单状态"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cuserId",
											type : "uid",
											text : "创建人ID"
										}, {
											name : "cuserName",
											type : "uname",
											text : "创建人"
										}, {
											name : "cdate",
											type : "date",
											text : "创建时间",
											dateFormat : "time"
										}, {
											name : "muserId",
											type : "uid",
											text : "修改人D"
										}, {
											name : "muserName",
											type : "uname",
											text : "修改人"
										}, {
											name : "mdate",
											type : "timestamp",
											text : "修改时间"
										}, {
											name : "amountId",
											type : "integer",
											text : "定金ID"
										}, {
											name : "orderId",
											type : "integer",
											text : "订单ID"
										}, {
											name : "payType",
											type : "string",
											text : "收款方式"
										}, {
											name : "bank",
											type : "string",
											text : "银行"
										}, {
											name : "isAgent",
											type : "string",
											text : "是否商场代收"
										}, {
											name : "amount",
											type : "float",
											text : "金额"
										}, {
											name : "cusName",
											type : "string",
											text : "客户名称"
										}, {
											name : "cancelReason",
											type : "string",
											text : "取消原因"
										}, {
											name : "cancelRemark",
											type : "string",
											text : "取消备注"
										}, {
											name : "cancelDate",
											type : "timestamp",
											text : "取消时间"
										}, {
											name : "orgId",
											type : "integer",
											text : "门店ID"
										}, {
											name : "orgName",
											type : "string",
											text : "门店名称"
										}, {
											name : "orgTel",
											type : "string",
											text : "门店电话"
										}, {
											name : "orgAddress",
											type : "string",
											text : "门店地址"
										}, {
											name : "orgCode",
											type : "string",
											text : "组织机构Code"
										}, {
											name : "imageId",
											type : "string",
											text : "图片ID"
										}, {
											name : "num",
											type : "string",
											text : "单号"
										}, {
											name : "insetCost",
											type : "float",
											text : "镶嵌工费"
										}, {
											name : "baseCost",
											type : "float",
											text : "加工费"
										}, {
											name : "demandId",
											type : "integer",
											text : "生成的要货单ID"
										}, {
											name : "demandNum",
											type : "string",
											text : "要货单编号"
										}, {
											name : "isGenerated",
											type : "string",
											text : "是否已经生成要货单"
										}, {
											name : "productName",
											type : "string",
											text : "商品名称"
										}, {
											name : "type",
											type : "string",
											text : "订单类型"
										}, {
											name : "sourceOrgId",
											type : "integer",
											text : "商品所在组织机构的ID"
										}, {
											name : "sourceOrgName",
											type : "string",
											text : "商品所在组织机构的名称"
										}, {
											name : "cardNo",
											type : "string",
											text : "会员卡号"
										}, {
											name : "discount",
											type : "string",
											text : "折扣金额"
										}, {
											name : "actualAccounts",
											type : "float",
											text : "应收金额"
										}, {
											name : "kgoldColor",
											type : "string",
											text : "K金颜色"
										}, {
											name : "totalAmount",
											type : "string",
											text : "定金总额"
										}, {
											name : "priceDetailDiamond",
											type : "float",
											text : "裸钻一口价"
										}, {
											name : "priceDetailProduct",
											type : "float",
											text : "金托一口价"
										}, {
											name : "priceDetailOther",
											type : "float",
											text : "其它一口价"
										}, {
											name : "discountDetailDiamond",
											type : "float",
											text : "裸钻折扣"
										}, {
											name : "discountDetailProduct",
											type : "float",
											text : "金托折扣"
										}, {
											name : "discountDetailOther",
											type : "float",
											text : "其它折扣"
										}, {
											name : "diamondWeight",
											type : "float",
											text : "主石重量"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('custDetailWin');
										store.setBaseParam('cusId', win.cusId);
									}
								}
							},
							autoExpandColumn : "remark",
							id : "cusOrderGrid",
							border : false,
							title : "客订记录",
							ref : "../cusGrid",
							columns : [{
										xtype : "datecolumn",
										header : "客订时间",
										sortable : true,
										resizable : true,
										width : 90,
										format : "Y-m-d",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "门店",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "orgName",
										id : "orgName"
									}, {
										header : "客订单号",
										sortable : false,
										resizable : true,
										width : 80,
										menuDisabled : true,
										dataIndex : "num",
										id : "num"
									}, {
										header : "销售顾问",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "employeeName",
										id : "employeeName"
									}, {
										header : "商品条码",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "productNum",
										id : "productNum"
									}, {
										header : "商品名称",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "productName",
										id : "productName"
									}, {
										xtype : "bizcodecolumn",
										header : "商品类型",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "ptype",
										id : "ptype",
										bizType : "productType"
									}, {
										xtype : "bizcodecolumn",
										header : "商品种类",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "pkind",
										id : "pkind",
										bizType : "productKind"
									}, {
										xtype : "numbercolumn",
										header : "商品尺寸",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "size",
										id : "size",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "一口价",
										sortable : true,
										resizable : true,
										width : 70,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "price",
										id : "price",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "折扣",
										sortable : true,
										resizable : true,
										width : 70,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "discount",
										id : "discount",
										align : "right"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore4",
								url : "entity/vMaintain",
								requestMethod : "GET",
								root : "root",
								idProperty : "id",
								remoteSort : false,
								restful : true,
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "num",
											type : "string",
											text : "编码"
										}, {
											name : "status",
											type : "string",
											text : "状态"
										}, {
											name : "productId",
											type : "integer",
											text : "商品ID"
										}, {
											name : "ptype",
											type : "string",
											text : "商品类型"
										}, {
											name : "pkind",
											type : "string",
											text : "商品种类"
										}, {
											name : "size",
											type : "float",
											text : "商品尺寸"
										}, {
											name : "productSource",
											type : "string",
											text : "商品来源"
										}, {
											name : "productNum",
											type : "string",
											text : "商品条码"
										}, {
											name : "productName",
											type : "string",
											text : "商品名称"
										}, {
											name : "cusId",
											type : "integer",
											text : "客户ID"
										}, {
											name : "cusName",
											type : "string",
											text : "客户名称"
										}, {
											name : "cardNo",
											type : "string",
											text : "会员卡号"
										}, {
											name : "tel",
											type : "string",
											text : "联系电话"
										}, {
											name : "orgId",
											type : "integer",
											text : "部门D"
										}, {
											name : "orgName",
											type : "string",
											text : "部门名称"
										}, {
											name : "orgCode",
											type : "string",
											text : "组织结构Code"
										}, {
											name : "deliveryDate",
											type : "string",
											text : "取货时间"
										}, {
											name : "price",
											type : "string",
											text : "维修费"
										}, {
											name : "maintainType",
											type : "string",
											text : "维修类型"
										}, {
											name : "employeeId",
											type : "integer",
											text : "接单人ID"
										}, {
											name : "employeeName",
											type : "string",
											text : "接单人名称"
										}, {
											name : "imageId",
											type : "string",
											text : "图片ID"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cuserId",
											type : "integer",
											text : "创建人ID"
										}, {
											name : "cuserName",
											type : "string",
											text : "创建人"
										}, {
											name : "cdate",
											type : "date",
											text : "创建时间",
											dateFormat : "time"
										}, {
											name : "amount",
											type : "string",
											text : "预付金额"
										}, {
											name : "payType",
											type : "string",
											text : "付款方式"
										}, {
											name : "bank",
											type : "string",
											text : "银行"
										}, {
											name : "isAgent",
											type : "string",
											text : "是否商场代收"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('custDetailWin');
										store.setBaseParam('cusId', win.cusId);
									}
								}
							},
							autoExpandColumn : "remark",
							id : "maintainGrid",
							border : false,
							title : "维修记录",
							ref : "../mtGrid",
							columns : [{
										xtype : "datecolumn",
										header : "维修时间",
										sortable : true,
										resizable : true,
										width : 90,
										format : "Y-m-d",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "门店",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "orgName",
										id : "orgName"
									}, {
										header : "维修单号",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "num",
										id : "num"
									}, {
										xtype : "bizcodecolumn",
										header : "维修类型",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "maintainType",
										id : "maintainType",
										bizType : "maintainType"
									}, {
										header : "商品条码",
										sortable : false,
										resizable : true,
										width : 80,
										menuDisabled : true,
										dataIndex : "productNum",
										id : "productNum"
									}, {
										header : "商品名称",
										sortable : false,
										resizable : true,
										width : 80,
										menuDisabled : true,
										dataIndex : "productName",
										id : "productName"
									}, {
										xtype : "bizcodecolumn",
										header : "商品类型",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "ptype",
										id : "ptype",
										bizType : "productType"
									}, {
										xtype : "bizcodecolumn",
										header : "商品种类",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "pkind",
										id : "pkind",
										bizType : "productKind"
									}, {
										xtype : "numbercolumn",
										header : "商品尺寸",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "size",
										id : "size",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "预付金额",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "amount",
										id : "amount",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "维修费",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "price",
										id : "price",
										align : "right"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore5",
								url : "entity/vSale?type=returns",
								requestMethod : "GET",
								root : "root",
								idProperty : "id",
								remoteSort : false,
								restful : true,
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "num",
											type : "string",
											text : "编号"
										}, {
											name : "saleId",
											type : "integer",
											text : "销售单ID"
										}, {
											name : "saleNum",
											type : "string",
											text : "退货单对应的销售单"
										}, {
											name : "orgId",
											type : "integer",
											text : "组织机构ID"
										}, {
											name : "orgName",
											type : "string",
											text : "门店"
										}, {
											name : "type",
											type : "string",
											text : "销售类型"
										}, {
											name : "orgCode",
											type : "string",
											text : "组织机构Code"
										}, {
											name : "orgTel",
											type : "string",
											text : "门店电话"
										}, {
											name : "orgAddress",
											type : "string",
											text : "门店地址"
										}, {
											name : "employeeId",
											type : "integer",
											text : "销售顾问ID"
										}, {
											name : "employeeName",
											type : "string",
											text : "销售顾问"
										}, {
											name : "checkerId",
											type : "integer",
											text : "维修人员ID"
										}, {
											name : "checkerName",
											type : "string",
											text : "维修人员"
										}, {
											name : "goldCheckerId",
											type : "integer",
											text : "验金人ID"
										}, {
											name : "goldCheckerName",
											type : "string",
											text : "验金人"
										}, {
											name : "cusId",
											type : "integer",
											text : "客户ID"
										}, {
											name : "cusName",
											type : "string",
											text : "客户姓名"
										}, {
											name : "mobile",
											type : "string",
											text : "客户手机"
										}, {
											name : "cardNo",
											type : "string",
											text : "会员卡号"
										}, {
											name : "grade",
											type : "string",
											text : "会员级别"
										}, {
											name : "historyPoints",
											type : "float",
											text : "历史积分"
										}, {
											name : "points",
											type : "string",
											text : "本次积分"
										}, {
											name : "totalAmount",
											type : "string",
											text : "应收金额"
										}, {
											name : "amount",
											type : "string",
											text : "实收金额"
										}, {
											name : "discount",
											type : "string",
											text : "折扣"
										}, {
											name : "frontMoney",
											type : "float",
											text : "预付定金"
										}, {
											name : "goldPay",
											type : "float",
											text : "旧金支付"
										}, {
											name : "productPay",
											type : "float",
											text : "旧饰支付"
										}, {
											name : "marketProxy",
											type : "float",
											text : "商场代收"
										}, {
											name : "marketProxyCash",
											type : "float",
											text : "商场代收现金"
										}, {
											name : "marketProxyBankCard",
											type : "float",
											text : "商场代收银联卡"
										}, {
											name : "marketProxyChit",
											type : "float",
											text : "商场代收代金券"
										}, {
											name : "marketProxyValueCard",
											type : "float",
											text : "商场代收储值卡"
										}, {
											name : "marketProxyOther",
											type : "float",
											text : "商场代收其它"
										}, {
											name : "marketProxyRemark",
											type : "string",
											text : "商场代收备注"
										}, {
											name : "bankCard",
											type : "float",
											text : "银联卡卡支付"
										}, {
											name : "bankCard1",
											type : "float",
											text : "银联卡1"
										}, {
											name : "bankCardBank1",
											type : "string",
											text : "银联卡1银行"
										}, {
											name : "bankCard2",
											type : "float",
											text : "银联卡2"
										}, {
											name : "bankCardBank2",
											type : "string",
											text : "银联卡2银行"
										}, {
											name : "bankCard3",
											type : "float",
											text : "银联卡3"
										}, {
											name : "bankCardBank3",
											type : "string",
											text : "银联卡3银行"
										}, {
											name : "valueCard",
											type : "float",
											text : "储值卡支付"
										}, {
											name : "valueCard1",
											type : "float",
											text : "储值卡1"
										}, {
											name : "valueCardType1",
											type : "string",
											text : "储值卡1类型"
										}, {
											name : "valueCard2",
											type : "float",
											text : "储值卡2"
										}, {
											name : "valueCardType2",
											type : "string",
											text : "储值卡2类型"
										}, {
											name : "valueCard3",
											type : "float",
											text : "储值卡3"
										}, {
											name : "valueCardType3",
											type : "string",
											text : "储值卡3类型"
										}, {
											name : "coBrandedCard",
											type : "float",
											text : "联名卡"
										}, {
											name : "coBrandedCard1",
											type : "float",
											text : "联名卡1"
										}, {
											name : "coBrandedCardBank1",
											type : "string",
											text : "联名卡1银行"
										}, {
											name : "coBrandedCard2",
											type : "float",
											text : "联名卡2"
										}, {
											name : "coBrandedCardBank2",
											type : "string",
											text : "联名卡2银行"
										}, {
											name : "coBrandedCard3",
											type : "float",
											text : "联名卡3"
										}, {
											name : "coBrandedCardBank3",
											type : "string",
											text : "联名卡3银行"
										}, {
											name : "foreignCard",
											type : "float",
											text : "外卡"
										}, {
											name : "foreignCard1",
											type : "float",
											text : "外卡1"
										}, {
											name : "foreignCardType1",
											type : "string",
											text : "外卡1类型"
										}, {
											name : "foreignCard2",
											type : "float",
											text : "外卡2"
										}, {
											name : "foreignCardType2",
											type : "string",
											text : "外卡2类型"
										}, {
											name : "foreignCard3",
											type : "float",
											text : "外卡3"
										}, {
											name : "foreignCardType3",
											type : "string",
											text : "外卡3类型"
										}, {
											name : "cash",
											type : "float",
											text : "现金支付"
										}, {
											name : "chit",
											type : "float",
											text : "代金券"
										}, {
											name : "transfer",
											type : "float",
											text : "转账支付"
										}, {
											name : "other",
											type : "float",
											text : "其它"
										}, {
											name : "status",
											type : "string",
											text : "状态"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cuserId",
											type : "uid",
											text : "操作人ID"
										}, {
											name : "isReturns",
											type : "string",
											text : "是否全部退货"
										}, {
											name : "cdate",
											type : "date",
											text : "操作时间",
											dateFormat : "time"
										}, {
											name : "isReturns2",
											type : "string",
											text : "是否全部退货2"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('custDetailWin');
										store.setBaseParam('cusId', win.cusId);
									}
								}
							},
							autoExpandColumn : "remark",
							id : "returnsGrid",
							border : false,
							title : "退货记录",
							ref : "../returnGrid",
							columns : [{
										xtype : "datecolumn",
										header : "退货时间",
										sortable : true,
										resizable : true,
										width : 90,
										format : "Y-m-d",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "门店",
										sortable : false,
										resizable : true,
										width : 80,
										menuDisabled : true,
										dataIndex : "orgName",
										id : "orgName"
									}, {
										header : "退货单号",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "num",
										id : "num"
									}, {
										header : "销售顾问",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "employeeName",
										id : "employeeName"
									}, {
										header : "验货人",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "checkerName",
										id : "checkerName"
									}, {
										xtype : "numbercolumn",
										header : "应退金额",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "totalAmount",
										id : "totalAmount",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "扣除金额",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "discount",
										id : "discount",
										align : "right"
									}, {
										xtype : "numbercolumn",
										header : "扣除积分",
										sortable : true,
										resizable : true,
										width : 75,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "points",
										id : "points",
										align : "right"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore6",
								url : "sql/customer/cusProductRecord",
								requestMethod : "GET",
								root : "root",
								idProperty : "id",
								remoteSort : false,
								restful : true,
								fields : [{
											name : "typeText",
											type : "string",
											text : "销售类型"
										}, {
											name : "cusId",
											type : "integer",
											text : "客户ID"
										}, {
											name : "num",
											type : "string",
											text : "销售单号"
										}, {
											name : "cdate",
											type : "date",
											text : "销售日期",
											dateFormat : "time"
										}, {
											name : "targetNum",
											type : "string",
											text : "商品条码"
										}, {
											name : "targetName",
											type : "string",
											text : "商品名称"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('custDetailWin');
										store.setBaseParam('cusId', win.cusId);
									}
								}
							},
							id : "prodsGrid",
							border : false,
							title : "商品记录",
							ref : "../prodRecord",
							autoExpandColumn : "num",
							columns : [{
										header : "销售单号",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "num",
										id : "num"
									}, {
										xtype : "bizcodecolumn",
										header : "销售类型",
										sortable : true,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "typeText",
										id : "typeText",
										bizType : "saleDetailType"
									}, {
										xtype : "datecolumn",
										header : "销售日期",
										sortable : true,
										resizable : true,
										width : 130,
										format : "Y-m-d H:i:s",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "商品条码",
										sortable : false,
										resizable : true,
										width : 120,
										menuDisabled : true,
										dataIndex : "targetNum",
										id : "targetNum"
									}, {
										header : "商品名称",
										sortable : false,
										resizable : true,
										width : 120,
										menuDisabled : true,
										dataIndex : "targetName",
										id : "targetName"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore7",
								url : "sql/customer/cusMaterialRecord",
								requestMethod : "GET",
								root : "root",
								idProperty : "id",
								remoteSort : false,
								restful : true,
								fields : [{
											name : "typeText",
											type : "string",
											text : "销售类型"
										}, {
											name : "cusId",
											type : "integer",
											text : "客户ID"
										}, {
											name : "quantity",
											type : "string",
											text : "数量"
										}, {
											name : "cdate",
											type : "date",
											text : "销售日期",
											dateFormat : "time"
										}, {
											name : "targetNum",
											type : "string",
											text : "物料条码"
										}, {
											name : "targetName",
											type : "string",
											text : "物料名称"
										}, {
											name : "targetType",
											type : "string",
											text : "物料类型"
										}, {
											name : "exchangePoints",
											type : "string",
											text : "兑换积分"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('custDetailWin');
										store.setBaseParam('cusId', win.cusId);
									}
								}
							},
							id : "matersGrid",
							border : false,
							title : "物料记录",
							ref : "../materRecord",
							autoExpandColumn : "targetNum",
							columns : [{
										header : "销售类型",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "typeText",
										id : "typeText"
									}, {
										xtype : "datecolumn",
										header : "销售日期",
										sortable : true,
										resizable : true,
										width : 100,
										format : "Y-m-d",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "物料条码",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "targetNum",
										id : "targetNum"
									}, {
										header : "物料名称",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "targetName",
										id : "targetName"
									}, {
										header : "物料类型",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "targetType",
										id : "targetType"
									}, {
										header : "数量",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "quantity",
										id : "quantity"
									}, {
										header : "兑换积分",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "exchangePoints",
										id : "exchangePoints"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore8",
								url : "entity/vCustomerLog",
								requestMethod : "GET",
								root : "root",
								idProperty : "id",
								remoteSort : false,
								restful : true,
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "cusName",
											type : "string",
											text : "客户"
										}, {
											name : "cusMobile",
											type : "string",
											text : "客户手机"
										}, {
											name : "cusId",
											type : "integer",
											text : "客户ID"
										}, {
											name : "cardNum",
											type : "string",
											text : "会员卡号"
										}, {
											name : "cardId",
											type : "integer",
											text : "会员卡ID"
										}, {
											name : "type",
											type : "string",
											text : "操作类型"
										}, {
											name : "cuserId",
											type : "uid",
											text : "操作人ID"
										}, {
											name : "cdate",
											type : "date",
											text : "操作时间",
											dateFormat : "time"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cdateStr",
											type : "string",
											text : "操作时间（查询）"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('custDetailWin');
										store.setBaseParam('cusId', win.cusId);
										store.setBaseParam('type', 'grant');
									}
								}
							},
							id : "cardGrid",
							border : false,
							title : "发卡记录",
							ref : "../cardRecord",
							autoExpandColumn : "remark",
							columns : [{
										header : "客户姓名",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "cusName",
										id : "cusName"
									}, {
										header : "客户手机",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "cusMobile",
										id : "cusMobile"
									}, {
										header : "会员卡号",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "cardNum",
										id : "cardNum"
									}, {
										xtype : "datecolumn",
										header : "操作时间",
										sortable : true,
										resizable : true,
										width : 130,
										format : "Y-m-d H:i:s",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore9",
								url : "entity/vCustomerPointsFlow",
								requestMethod : "GET",
								root : "root",
								idProperty : "id",
								remoteSort : false,
								restful : true,
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "cusId",
											type : "integer",
											text : "客户ID"
										}, {
											name : "cuserId",
											type : "integer",
											text : "用户ID"
										}, {
											name : "customerName",
											type : "string",
											text : "客户名称"
										}, {
											name : "userName",
											type : "string",
											text : "用户名称"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cdate",
											type : "date",
											text : "创建时间",
											dateFormat : "time"
										}, {
											name : "pointsType",
											type : "string",
											text : "积分类型"
										}, {
											name : "points",
											type : "float",
											text : "积分"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('custDetailWin');
										store.setBaseParam('cusId', win.cusId);
										store.setBaseParam('pointsType',
												'points');
									}
								}
							},
							id : "pointInDeGrid",
							border : false,
							title : "积分增减记录",
							ref : "../pointInDeGrid",
							autoExpandColumn : "remark",
							columns : [{
										header : "客户姓名",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "customerName",
										id : "customerName"
									}, {
										xtype : "numbercolumn",
										header : "积分",
										sortable : true,
										resizable : true,
										width : 120,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "points",
										id : "points"
									}, {
										xtype : "datecolumn",
										header : "操作时间",
										sortable : true,
										resizable : true,
										width : 130,
										format : "Y-m-d H:i:s",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "操作人",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "userName",
										id : "userName"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}]
						}]
			}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'code/customer/' + vid,
							method : 'GET',
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result.success) {
									var data = result.customer;
									if (data.grantDate) {
										data.grantDate = new Date(data.grantDate)
												.format('Y-m-d');
									}
									if (data.cardIndate) {
										data.cardIndate = new Date(data.cardIndate)
												.format('Y-m-d');
									}
									win.form.getForm().setValues(data);
									if (data.isShowTrade == true) {
										win.tabs.show();
										win.center();
										win.cusForm.getForm().setValues(data);
										win.saleGrid.getStore().load();
										win.cusGrid.getStore().load();
										win.mtGrid.getStore().load();
										win.returnGrid.getStore().load();
										win.prodRecord.getStore().load();
										win.materRecord.getStore().load();
										win.cardRecord.getStore().load();
										win.pointInDeGrid.getStore().load();
									}
								}
							},
							failure : function(resp) {
								Ext.Msg.alert('提示', resp.responseText);
							}
						});
			}
		}
	}
};

com.mzf.common.materTransInfoWin = {
	xtype : "window",
	width : 650,
	height : 450,
	title : "查看详情",
	constrain : true,
	layout : "border",
	buttonAlign : "left",
	id : "transformWin",
	autoHeight : false,
	modal : true,
	stateful : false,
	resizable : true,
	bodyBorder : true,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "tbfill"
				}, {
					xtype : "button",
					text : "确定",
					ref : "../btnOk",
					listeners : {
						click : function(btn, evt) {
							var win = btn.refOwner;
							if (win) {
								win.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 85,
		labelAlign : "left",
		layout : "form",
		border : false,
		autoHeight : true,
		padding : "6",
		region : "north",
		height : 200,
		autoWidth : false,
		ref : "form",
		items : [{
					xtype : "container",
					anchor : "100%",
					layout : "column",
					items : [{
								xtype : "container",
								layout : "form",
								columnWidth : 0.5,
								items : [{
											xtype : "textfield",
											fieldLabel : "物料条码",
											anchor : "-10",
											name : "targetNum",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "调拨数量",
											anchor : "-10",
											name : "quantity",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "调出部门",
											anchor : "-10",
											name : "sourceOrgName",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "发起人",
											anchor : "-10",
											name : "cuserName",
											readOnly : true
										}]
							}, {
								xtype : "container",
								layout : "form",
								columnWidth : 0.5,
								items : [{
											xtype : "textfield",
											fieldLabel : "物料名称",
											anchor : "100%",
											name : "targetName",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "收货数量",
											anchor : "100%",
											name : "actualQuantity",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "调入部门",
											anchor : "100%",
											name : "targetOrgName",
											readOnly : true
										}, {
											xtype : "datefield",
											anchor : "100%",
											fieldLabel : "发起时间",
											triggerClass : "x-form-search-trigger",
											name : "cdate",
											readOnly : true,
											format : "Y-m-d H:i:s"
										}]
							}]
				}, {
					xtype : "textarea",
					fieldLabel : "备注",
					anchor : "100%",
					name : "remark",
					readOnly : true
				}]
	}, {
		xtype : "tabpanel",
		activeTab : 0,
		region : "center",
		width : 100,
		tabPosition : "bottom",
		border : false,
		cls : "topBorder",
		items : [{
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore1",
						url : "entity/vBizLog",
						root : "root",
						idProperty : "id",
						autoLoad : true,
						restful : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "transId",
									type : "integer",
									text : "流程ID"
								}, {
									name : "entityCode",
									type : "string",
									text : "实体Code"
								}, {
									name : "entityId",
									type : "integer",
									text : "实体ID"
								}, {
									name : "targetId",
									type : "integer",
									text : "对象ID"
								}, {
									name : "operate",
									type : "string",
									text : "操作内容"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}, {
									name : "cuserId",
									type : "uid",
									text : "操作人ID"
								}, {
									name : "cuserName",
									type : "uname",
									text : "操作人"
								}, {
									name : "cdate",
									type : "date",
									text : "操作时间",
									dateFormat : "time"
								}, {
									name : "billCode",
									type : "string",
									text : "表单Code"
								}, {
									name : "billId",
									type : "integer",
									text : "表单ID"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('transformWin');
								store.baseParams = {
									billId : win.orderId,
									billCode : 'transfer'
								};
							}
						}
					},
					autoHeight : false,
					region : "center",
					width : 100,
					border : false,
					title : "操作记录",
					autoExpandColumn : "remark",
					columns : [{
								header : "操作人",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "cuserName",
								id : "cuserName"
							}, {
								xtype : "datecolumn",
								header : "操作时间",
								sortable : true,
								resizable : true,
								width : 140,
								format : "Y-m-d H:i:s",
								menuDisabled : true,
								dataIndex : "cdate",
								id : "cdate"
							}, {
								header : "操作内容",
								sortable : false,
								resizable : true,
								width : 120,
								menuDisabled : true,
								dataIndex : "operate",
								id : "operate"
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark"
							}]
				}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vTransfer?targetType=material',
							method : 'GET',
							params : {
								id : vid
							},
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result) {
									var form = win.form;
									if (form) {
										var data = result.root[0];
										data.actualQuantity = data.quantity;
										data.cdate
												&& (data.cdate = new Date(data.cdate)
														.format('Y-m-d H:i:s'));
										form.getForm().setValues(data);
										win.billId = data.id;
										if (data.imageId) {
											var imgdoc = Ext.get('imgView');
											imgdoc.dom.src = 'image/'
													+ data.imageId;
										}
									}

								}
							},
							failure : function(resp) {
								Ext.Msg.alert('错误', resp.responseText);
							}
						});
			}
		}
	}
};

com.mzf.common.prodSplitInfoWin = {
	xtype : "window",
	width : 837,
	height : 477,
	title : "查看详情",
	constrain : true,
	layout : "border",
	id : "detailWin",
	modal : true,
	autoScroll : false,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确定",
					ref : "../btnCancel",
					listeners : {
						click : function(btn, evt) {
							if (this.refOwner) {
								this.refOwner.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 90,
		labelAlign : "left",
		layout : "column",
		border : false,
		region : "north",
		width : 1056,
		autoHeight : true,
		padding : "6px",
		ref : "form",
		items : [{
			xtype : "container",
			layout : "form",
			columnWidth : 0.25,
			items : [{
						xtype : "hidden",
						fieldLabel : "商品来源",
						anchor : "100%",
						name : "productSource",
						id : "productSource"
					}, {
						xtype : "hidden",
						fieldLabel : "商品ID",
						anchor : "100%",
						name : "productId",
						id : "productId"
					}, {
						xtype : "trigger",
						anchor : "-10",
						fieldLabel : "商品条码",
						triggerClass : "x-form-search-trigger",
						name : "productNum",
						editable : false,
						readOnly : true,
						listeners : {
							triggerclick : function(field) {
								var ps = Ext.getCmp('productSource');
								if (ps.getValue() == 'secondProduct') {
									var win = od.showWindow('productWin');
									if (win) {
										win.getProd = function(record) {
											var win2 = Ext.getCmp('recycleWin');
											if (win2) {
												var form = win2.form;
												if (form) {
													form.getForm().setValues({
														productId : record.data.id,
														productNum : record.data.num,
														sourceOrgId : record.data.sourceOrgId,
														rightDiamondSize : record.data.diamondSize,
														sourceOrgName : record.data.sourceOrgName,
														wholesalePrice : record.data.wholesalePrice,
														rightGoldWeight : record.data.goldWeight
																|| record.data.weight
													});
												}
												win2.splitSum();
												win.close();
												Ext.getCmp('detailId').show();
											}

										}
									}
								} else if (ps.getValue() == 'product') {
									var win = od.showWindow('productStoreWin');
									if (win) {
										win.getProd = function(record) {
											var win2 = Ext.getCmp('recycleWin');
											if (win2) {
												var form = win2.form;
												if (form) {
													form.getForm().setValues({
														productId : record.data.id,
														productNum : record.data.num,
														sourceOrgId : record.data.sourceOrgId,
														sourceOrgName : record.data.sourceOrgName,
														wholesalePrice : record.data.wholesalePrice,
														rightGoldWeight : record.data.goldWeight
													});
												}
												win2.splitSum();
												win.close();
												Ext.getCmp('detailId').show();
											}

										}
									}
								} else if (ps.getValue() == 'maintainProduct') {
									var win = od.showWindow('maintainStoreWin');
									if (win) {
										win.getProd = function(record) {
											var win2 = Ext.getCmp('recycleWin');
											if (win2) {
												var form = win2.form;
												if (form) {
													form.getForm().setValues({
														productId : record.data.id,
														productNum : record.data.num,
														sourceOrgId : record.data.sourceOrgId,
														sourceOrgName : record.data.sourceOrgName,
														wholesalePrice : record.data.wholesalePrice,
														rightGoldWeight : record.data.goldWeight
													});
												}
												win2.splitSum();
												win.close();
											}

										}
									}
								} else {
									Ext.Msg.alert('提示', '没有指定商品来源');
								}
							}
						}
					}, {
						xtype : "numberfield",
						fieldLabel : "应返金重",
						anchor : "-10",
						name : "rightGoldWeight",
						id : "rightGoldWeight",
						allowDecimals : true,
						allowNegative : true,
						decimalPrecision : 3,
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "应返石重",
						anchor : "-10",
						name : "rightDiamondSize",
						id : "rightDiamondSize",
						allowDecimals : true,
						allowNegative : true,
						decimalPrecision : 3,
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "丢失耳壁数量",
						anchor : "-10",
						name : "lossEb",
						id : "lossEb",
						readOnly : true
					}]
		}, {
			xtype : "container",
			layout : "form",
			columnWidth : 0.25,
			items : [{
						xtype : "hidden",
						fieldLabel : "来源部门ID",
						anchor : "100%",
						name : "sourceOrgId"
					}, {
						xtype : "textfield",
						fieldLabel : "来源部门",
						anchor : "-10",
						readOnly : true,
						name : "sourceOrgName"
					}, {
						xtype : "numberfield",
						fieldLabel : "实返金重",
						anchor : "-10",
						name : "actualGoldWeight",
						id : "actualGoldWeight",
						allowDecimals : true,
						allowNegative : true,
						decimalPrecision : 3,
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "丢失钻石克拉数",
						anchor : "-10",
						name : "lossDiamond",
						id : "lossDiamond",
						allowDecimals : true,
						allowNegative : true,
						decimalPrecision : 3,
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "丢失耳壁扣钱",
						anchor : "-10",
						name : "deductMoneyEb",
						id : "deductMoneyEb",
						blankText : "请输入丢失耳壁扣钱",
						allowBlank : false,
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}]
		}, {
			xtype : "container",
			layout : "form",
			columnWidth : 0.25,
			items : [{
						xtype : "numberfield",
						fieldLabel : "批发价",
						anchor : "-10",
						name : "wholesalePrice",
						readOnly : true,
						id : "wholesalePrice"
					}, {
						xtype : "combo",
						anchor : "-10",
						fieldLabel : "金料成色",
						queryAction : "all",
						name : "rightGoldClass",
						displayField : "name",
						editable : false,
						mode : "remote",
						triggerAction : "all",
						valueField : "goldClass",
						blankText : "旧金成色是必填项",
						id : "goldClassId",
						region : "west",
						width : 100,
						readOnly : true,
						store : {
							xtype : "jsonstore",
							storeId : "MyStore3",
							url : "entity/buyGoldUnitPrice",
							requestMethod : "GET",
							root : "root",
							restful : true,
							fields : [{
										name : "unitPrice",
										type : "float",
										text : "回收单价"
									}, {
										name : "orgId",
										type : "integer",
										text : "组织机构ID"
									}, {
										name : "goldClass",
										type : "string",
										text : "金料成色"
									}, {
										name : "name",
										type : "string",
										text : "金料成色"
									}],
							listeners : {
								beforeload : function(store, options) {
									store
											.setBaseParam(
													'orgId',
													od.AppInstance.appConfig.user.orgId);
								}
							}
						},
						listeners : {
							select : function(combo, record, index) {
								var cmb = Ext.getCmp('goldClassId');
								if (cmb) {
									var index = cmb.getStore().find(
											'goldClass', record.data.goldClass);
									var record = cmb.getStore().getAt(index);
									if (record) {
										var p = Ext
												.getCmp('deductMoneyGoldWeight');
										var ac = Ext.num(
												Ext.getCmp('actualGoldWeight')
														.getValue(), 0);
										if (p) {
											p.setValue(Ext.num(
													record.data.unitPrice, 0)
													* ac);
										}
									}
								}
							}
						}
					}, {
						xtype : "textfield",
						fieldLabel : "丢失钻石备注",
						anchor : "-10",
						name : "lossDiamondRemark",
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "链尾损坏扣钱",
						anchor : "-10",
						name : "deductMoneyChain1",
						id : "deductMoneyChain1",
						allowBlank : false,
						blankText : "请输入链尾损坏扣钱",
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}]
		}, {
			xtype : "container",
			layout : "form",
			columnWidth : 0.25,
			items : [{
						xtype : "numberfield",
						fieldLabel : "折算金额",
						anchor : "0",
						name : "settlementPrice",
						allowBlank : false,
						blankText : "折算金额是必填项",
						id : "settlementPrice",
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "金重结算扣钱",
						anchor : "100%",
						name : "deductMoneyGoldWeight",
						id : "deductMoneyGoldWeight",
						allowBlank : false,
						blankText : "请输入金重结算扣钱",
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}, {
						xtype : "numberfield",
						fieldLabel : "丢失钻石扣钱",
						anchor : "100%",
						name : "deductMoneyDiamond",
						id : "deductMoneyDiamond",
						blankText : "请输入丢失钻石扣钱",
						allowBlank : false,
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}, {
						xtype : "numberfield",
						fieldLabel : "链扣损坏扣钱",
						anchor : "100%",
						name : "deductMoneyChain2",
						id : "deductMoneyChain2",
						allowBlank : false,
						blankText : "请输入链扣损坏扣钱",
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}]
		}, {
			xtype : "container",
			layout : "form",
			columnWidth : 1,
			items : [{
						xtype : "textarea",
						fieldLabel : "备注",
						anchor : "100%",
						name : "remark",
						height : 60
					}]
		}]
	}, {
		xtype : "grid",
		store : {
			xtype : "jsonstore",
			storeId : "MyStore4",
			url : "entity/splitDetail",
			requestMethod : "GET",
			root : "root",
			idProperty : "id",
			restful : true,
			autoLoad : true,
			fields : [{
						name : "id",
						type : "string",
						text : "主键"
					}, {
						name : "splitId",
						type : "string",
						text : "外键"
					}, {
						name : "type",
						type : "string",
						text : "原料类型"
					}, {
						name : "goldClass",
						type : "string",
						text : "金料成色"
					}, {
						name : "partsType",
						type : "string",
						text : "配件类型"
					}, {
						name : "partsStandard",
						type : "string",
						text : "配件规格"
					}, {
						name : "weight",
						type : "string",
						text : "配件重量"
					}, {
						name : "spec",
						type : "string",
						text : "裸钻重量"
					}, {
						name : "gravelStandard",
						type : "string",
						text : "碎石规格"
					}, {
						name : "cid1",
						type : "string",
						text : "钻石证书一"
					}, {
						name : "cid2",
						type : "string",
						text : "钻石证书二"
					}, {
						name : "cut",
						type : "string",
						text : "钻石切工"
					}, {
						name : "clean",
						type : "string",
						text : "钻石净度"
					}, {
						name : "shape",
						type : "string",
						text : "钻石形状"
					}, {
						name : "color",
						type : "string",
						text : "钻石颜色"
					}, {
						name : "cost",
						type : "string",
						text : "成本"
					}, {
						name : "quantity",
						type : "string",
						text : "数量"
					}, {
						name : "remark",
						type : "string",
						text : "备注"
					}],
			listeners : {
				beforeload : function(store, options) {
					var win = Ext.getCmp('detailWin');
					store.setBaseParam('splitId', win.splitId);
				}
			}
		},
		region : "center",
		border : false,
		ref : "grid",
		autoExpandColumn : "remark",
		id : "rawmateGrid",
		height : 276,
		width : 863,
		cls : "topBorder",
		columns : [{
					xtype : "bizcodecolumn",
					header : "原料类型",
					sortable : true,
					resizable : true,
					width : 60,
					menuDisabled : true,
					dataIndex : "type",
					id : "type",
					bizType : "rowmaterialType"
				}, {
					xtype : "bizcodecolumn",
					header : "钻石形状",
					sortable : true,
					resizable : true,
					width : 60,
					menuDisabled : true,
					dataIndex : "shape",
					id : "shape",
					bizType : "diamondShape"
				}, {
					xtype : "bizcodecolumn",
					header : "钻石颜色",
					sortable : true,
					resizable : true,
					width : 60,
					menuDisabled : true,
					dataIndex : "color",
					id : "color",
					bizType : "diamondColor"
				}, {
					xtype : "bizcodecolumn",
					header : "钻石净度",
					sortable : true,
					resizable : true,
					width : 60,
					menuDisabled : true,
					dataIndex : "clean",
					id : "clean",
					bizType : "diamondClean"
				}, {
					xtype : "bizcodecolumn",
					header : "钻石切工",
					sortable : true,
					resizable : true,
					width : 60,
					menuDisabled : true,
					dataIndex : "cut",
					id : "cut",
					bizType : "diamondCut"
				}, {
					xtype : "bizcodecolumn",
					header : "碎石规格",
					sortable : true,
					resizable : true,
					width : 65,
					menuDisabled : true,
					dataIndex : "gravelStandard",
					id : "gravelStandard",
					bizType : "gravelStandard"
				}, {
					xtype : "bizcodecolumn",
					header : "金料成色",
					sortable : true,
					resizable : true,
					width : 60,
					menuDisabled : true,
					dataIndex : "goldClass",
					id : "goldClass",
					bizType : "goldClass"
				}, {
					xtype : "bizcodecolumn",
					header : "配件类型",
					sortable : true,
					resizable : true,
					width : 60,
					menuDisabled : true,
					dataIndex : "partsType",
					id : "partsType",
					bizType : "partsType"
				}, {
					xtype : "bizcodecolumn",
					header : "配件规格",
					sortable : true,
					resizable : true,
					width : 60,
					menuDisabled : true,
					dataIndex : "partsStandard",
					id : "partsStandard",
					bizType : "partsStandard"
				}, {
					xtype : "numbercolumn",
					header : "裸钻重量",
					sortable : true,
					resizable : true,
					width : 60,
					format : "0,000.000",
					menuDisabled : true,
					dataIndex : "spec",
					id : "spec"
				}, {
					xtype : "numbercolumn",
					header : "钻石重量",
					sortable : true,
					resizable : true,
					width : 75,
					format : "0,000.000",
					menuDisabled : true,
					dataIndex : "weight",
					id : "weight",
					align : "right"
				}, {
					xtype : "numbercolumn",
					header : "数量/重量",
					sortable : true,
					resizable : true,
					width : 75,
					format : "0,000.00",
					menuDisabled : true,
					dataIndex : "quantity",
					id : "quantity",
					align : "right"
				}, {
					xtype : "numbercolumn",
					header : "成本",
					sortable : true,
					resizable : true,
					width : 60,
					format : "0,000.00",
					menuDisabled : true,
					dataIndex : "cost",
					id : "cost",
					align : "right",
					hidden : true
				}, {
					header : "备注",
					sortable : false,
					resizable : true,
					width : 100,
					menuDisabled : true,
					dataIndex : "remark",
					id : "remark"
				}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vSplit',
							method : 'GET',
							params : {
								id : vid
							},
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result) {
									var form = win.form;
									if (form) {
										var data = result.root[0];
										form.getForm().setValues(data);
										Ext.getCmp('goldClassId')
												.setValue(data.rightGoldClass);
										Ext
												.getCmp('goldClassId')
												.setRawValue(data.rightGoldClassText);
									}
								}
							},
							failure : function(resp) {
								Ext.Msg.alert('错误', resp.responseText);
							}
						});
			}
		}
	}
};

com.mzf.common.prodPurchaseInfoWin = {
	xtype : "window",
	width : 662,
	height : 460,
	title : "查看详情",
	constrain : true,
	id : "orderDetailWin",
	layout : "border",
	modal : true,
	stateful : false,
	autoHeight : false,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确定",
					ref : "../btnOK",
					listeners : {
						click : function(btn, evt) {
							var win = btn.refOwner;
							if (win) {
								win.close();
							}
						}
					}
				}]
	},
	items : [{
				xtype : "form",
				labelWidth : 70,
				labelAlign : "left",
				layout : "form",
				border : false,
				region : "north",
				width : 100,
				padding : "6",
				autoHeight : true,
				ref : "form",
				items : [{
							xtype : "container",
							anchor : "100%",
							layout : "column",
							items : [{
										xtype : "container",
										layout : "form",
										columnWidth : 0.5,
										labelWidth : 70,
										items : [{
													xtype : "textfield",
													fieldLabel : "订单编号",
													anchor : "-20",
													readOnly : true,
													name : "num"
												}, {
													xtype : "textfield",
													fieldLabel : "供应商名称",
													anchor : "-20",
													readOnly : true,
													name : "vendorName"
												}, {
													xtype : "textfield",
													fieldLabel : "创建人",
													anchor : "-20",
													readOnly : true,
													name : "cuserName"
												}]
									}, {
										xtype : "container",
										layout : "form",
										columnWidth : 0.5,
										labelWidth : 80,
										items : [{
													xtype : "bizcodebox",
													anchor : "0",
													fieldLabel : "类型",
													editable : false,
													readOnly : true,
													name : "isDiamond",
													bizType : "vendorOrderProductType"
												}, {
													xtype : "datefield",
													anchor : "100%",
													fieldLabel : "订单日期",
													readOnly : true,
													name : "cdate",
													format : "Y-m-d"
												}, {
													xtype : "datefield",
													anchor : "0",
													fieldLabel : "希望到货日期",
													readOnly : true,
													name : "expectDate",
													format : "Y-m-d"
												}]
									}]
						}, {
							xtype : "textarea",
							fieldLabel : "备注",
							anchor : "100%",
							readOnly : true,
							name : "remark"
						}]
			}, {
				xtype : "tabpanel",
				activeTab : 0,
				region : "center",
				border : false,
				tabPosition : "bottom",
				ref : "tabs",
				cls : "topBorder",
				items : [{
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore2",
								url : "entity/vendorProductOrderDetail",
								root : "root",
								idProperty : "id",
								autoLoad : true,
								restful : true,
								requestMethod : "GET",
								fields : [{
											name : "polishing",
											type : "string",
											text : "抛光性"
										}, {
											name : "symmetry",
											type : "string",
											text : "对称性"
										}, {
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "orderId",
											type : "integer",
											text : "供应商订单ID"
										}, {
											name : "ptype",
											type : "string",
											text : "商品类型"
										}, {
											name : "pkind",
											type : "string",
											text : "商品种类"
										}, {
											name : "styleCode",
											type : "string",
											text : "款号"
										}, {
											name : "vendorStyleCode",
											type : "string",
											text : "厂家款号"
										}, {
											name : "color",
											type : "string",
											text : "主石颜色"
										}, {
											name : "clean",
											type : "string",
											text : "主石净度"
										}, {
											name : "cut",
											type : "string",
											text : "主石切工"
										}, {
											name : "goldClass",
											type : "string",
											text : "金料成色"
										}, {
											name : "craft",
											type : "string",
											text : "制作工艺"
										}, {
											name : "size",
											type : "float",
											text : "尺寸"
										}, {
											name : "status",
											type : "string",
											text : "状态"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "demandId",
											type : "integer",
											text : "要货单ID"
										}, {
											name : "vendorStyleId",
											type : "integer",
											text : "厂家款号ID"
										}, {
											name : "productId",
											type : "integer",
											text : "商品ID"
										}, {
											name : "styleId",
											type : "integer",
											text : "款式ID "
										}, {
											name : "inset",
											type : "string",
											text : "镶嵌方式"
										}, {
											name : "weight",
											type : "float",
											text : "主石大小"
										}, {
											name : "kgoldColor",
											type : "string",
											text : "K金颜色"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('orderDetailWin');
										if (win) {
											if (win.orderId) {
												store.baseParams = {
													orderId : win.orderId
												};
											}
										}
									}
								}
							},
							border : false,
							height : 250,
							title : "商品列表",
							id : "productGrid",
							ref : "../productGrid",
							autoExpandColumn : "vendorStyleCode",
							columns : [{
										xtype : "bizcodecolumn",
										header : "商品类型",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "ptype",
										id : "ptype",
										bizType : "productType"
									}, {
										xtype : "bizcodecolumn",
										header : "商品种类",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "pkind",
										id : "pkind",
										bizType : "productKind"
									}, {
										header : "厂家款号",
										sortable : false,
										resizable : true,
										width : 90,
										menuDisabled : true,
										dataIndex : "vendorStyleCode",
										id : "vendorStyleCode"
									}, {
										xtype : "bizcodecolumn",
										header : "主石颜色",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "color",
										id : "color",
										bizType : "masterColor"
									}, {
										xtype : "bizcodecolumn",
										header : "主石净度",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "clean",
										id : "clean",
										bizType : "masterClean"
									}, {
										xtype : "bizcodecolumn",
										header : "主石切工",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "cut",
										id : "cut",
										bizType : "masterCut"
									}, {
										xtype : "bizcodecolumn",
										header : "金料成色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "goldClass",
										id : "goldClass",
										bizType : "goldClass"
									}, {
										header : "尺寸",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "size",
										id : "size"
									}, {
										xtype : "bizcodecolumn",
										header : "状态",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "status",
										id : "status",
										bizType : "vendorOrderDetailStatus"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore3",
								url : "entity/vBizLog",
								requestMethod : "GET",
								root : "root",
								remoteSort : false,
								restful : true,
								autoLoad : true,
								fields : [{
											name : "cdate",
											type : "date",
											text : "操作时间",
											dateFormat : "time"
										}, {
											name : "billCode",
											type : "string",
											text : "表单Code"
										}, {
											name : "billId",
											type : "integer",
											text : "表单ID"
										}, {
											name : "transId",
											type : "integer",
											text : "流程ID"
										}, {
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "operate",
											type : "string",
											text : "操作内容"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cuserName",
											type : "uname",
											text : "操作人"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('orderDetailWin');
										if (win) {
											if (win.orderId) {
												store.baseParams = {
													billId : win.orderId,
													billCode : 'vendorOrder'
												};
											}
										}
									}
								}
							},
							title : "处理流程",
							autoExpandColumn : "remark",
							columns : [{
										header : "表单Code",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "billCode",
										id : "billCode",
										hidden : true
									}, {
										header : "表单ID",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "billId",
										id : "billId",
										hidden : true
									}, {
										header : "流程ID",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "transId",
										id : "transId",
										hidden : true
									}, {
										header : "操作人",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "cuserName",
										id : "cuserName"
									}, {
										xtype : "datecolumn",
										header : "操作时间",
										sortable : true,
										resizable : true,
										width : 150,
										format : "Y-m-d H:i:s",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "操作内容",
										sortable : false,
										resizable : true,
										width : 200,
										menuDisabled : true,
										dataIndex : "operate",
										id : "operate"
									}, {
										header : "主键",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "id",
										id : "id",
										hidden : true
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}]
						}]
			}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vendorOrder/' + vid
									+ '?type=purchase',
							method : 'GET',
							success : function(resp) {
								var data = Ext.decode(resp.responseText);
								if (data.cdate) {
									data.cdate = Date
											.parseDate(data.cdate, 'u');
								}
								win.form.getForm().setValues(data);
							}
						});
			}
		}
	}
};

com.mzf.rawMaterOrderInfoWin = {
	xtype : "window",
	width : 771,
	height : 449,
	title : "查看详情",
	constrain : true,
	id : "rowMaterialOrderInofWin",
	layout : "border",
	modal : true,
	stateful : false,
	autoHeight : false,
	maximizable : true,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确定",
					ref : "../btnCancel",
					listeners : {
						click : function(btn, evt) {
							var win = btn.refOwner;
							if (win) {
								win.close();
							}
						}
					}
				}]
	},
	items : [{
				xtype : "form",
				labelWidth : 80,
				labelAlign : "left",
				layout : "form",
				border : false,
				region : "north",
				width : 100,
				padding : "6",
				autoHeight : true,
				ref : "form",
				items : [{
							xtype : "container",
							anchor : "100%",
							layout : "column",
							items : [{
										xtype : "container",
										layout : "form",
										columnWidth : 0.5,
										items : [{
													xtype : "textfield",
													fieldLabel : "供应商名称",
													anchor : "-20",
													readOnly : true,
													name : "vendorName"
												}, {
													xtype : "textfield",
													fieldLabel : "订单编号",
													anchor : "-20",
													readOnly : true,
													name : "num"
												}]
									}, {
										xtype : "container",
										layout : "form",
										columnWidth : 0.5,
										items : [{
													xtype : "textfield",
													fieldLabel : "希望到货日期",
													anchor : "100%",
													readOnly : true,
													name : "expectDate"
												}, {
													xtype : "datefield",
													anchor : "100%",
													fieldLabel : "订单日期",
													name : "cdate",
													format : "Y-m-d",
													readOnly : true
												}]
									}]
						}, {
							xtype : "textarea",
							fieldLabel : "备注",
							anchor : "100%",
							readOnly : true,
							name : "remark"
						}]
			}, {
				xtype : "tabpanel",
				activeTab : 0,
				region : "center",
				border : false,
				tabPosition : "bottom",
				ref : "tabInfo",
				cls : "topBorder",
				items : [{
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore2",
						url : "entity/rawmaterialOrderDetail",
						requestMethod : "GET",
						root : "root",
						idProperty : "id",
						autoLoad : true,
						restful : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "orderId",
									type : "integer",
									text : "原料采购订单ID"
								}, {
									name : "planId",
									type : "integer",
									text : "原料采购计划ID"
								}, {
									name : "source",
									type : "string",
									text : "来源"
								}, {
									name : "sourceId",
									type : "string",
									text : "来源标识"
								}, {
									name : "type",
									type : "string",
									text : "类型"
								}, {
									name : "goldClass",
									type : "string",
									text : "金料成色"
								}, {
									name : "cut",
									type : "string",
									text : "钻石切工"
								}, {
									name : "clean",
									type : "string",
									text : "钻石净度"
								}, {
									name : "shape",
									type : "string",
									text : "钻石形状"
								}, {
									name : "color",
									type : "string",
									text : "钻石颜色"
								}, {
									name : "rawmaterialId",
									type : "integer",
									text : "原料ID"
								}, {
									name : "partsType",
									type : "string",
									text : "配件类型"
								}, {
									name : "partsStandard",
									type : "string",
									text : "配件规格"
								}, {
									name : "gravelStandard",
									type : "string",
									text : "碎钻规格"
								}, {
									name : "spec",
									type : "string",
									text : "规格"
								}, {
									name : "quantity",
									type : "float",
									text : "数量"
								}, {
									name : "unit",
									type : "string",
									text : "单位"
								}, {
									name : "status",
									type : "string",
									text : "状态"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('rowMaterialOrderInofWin');
								store.baseParams = {
									orderId : win.orderId
								};
							}
						}
					},
					border : false,
					autoExpandColumn : "remark",
					title : "原料列表",
					ref : "./grid",
					columns : [{
								xtype : "bizcodecolumn",
								header : "原料类型",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "type",
								id : "type",
								bizType : "rowmaterialType"
							}, {
								xtype : "bizcodecolumn",
								header : "钻石形状",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "shape",
								id : "shape",
								bizType : "diamondShape"
							}, {
								xtype : "bizcodecolumn",
								header : "钻石颜色",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "color",
								id : "color",
								bizType : "diamondColor"
							}, {
								xtype : "bizcodecolumn",
								header : "钻石切工",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "cut",
								id : "cut",
								bizType : "diamondCut"
							}, {
								xtype : "bizcodecolumn",
								header : "钻石净度",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "clean",
								id : "clean",
								bizType : "diamondClean"
							}, {
								xtype : "bizcodecolumn",
								header : "金料成色",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "goldClass",
								id : "goldClass",
								bizType : "goldClass"
							}, {
								xtype : "bizcodecolumn",
								header : "配件类型",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "partsType",
								id : "partsType",
								bizType : "partsType"
							}, {
								xtype : "bizcodecolumn",
								header : "碎石规格",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "gravelStandard",
								id : "gravelStandard",
								bizType : "gravelStandard"
							}, {
								xtype : "bizcodecolumn",
								header : "配件规格",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "partsStandard",
								id : "partsStandard",
								bizType : "partsStandard"
							}, {
								header : "重量",
								sortable : false,
								resizable : true,
								width : 50,
								menuDisabled : true,
								dataIndex : "spec",
								id : "spec"
							}, {
								header : "数量",
								sortable : false,
								resizable : true,
								width : 50,
								menuDisabled : true,
								dataIndex : "quantity",
								id : "quantity"
							}, {
								header : "单位",
								sortable : false,
								resizable : true,
								width : 45,
								menuDisabled : true,
								dataIndex : "unit",
								id : "unit"
							}, {
								xtype : "bizcodecolumn",
								header : "状态",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "status",
								id : "status",
								hidden : false,
								bizType : "vendorOrderDetailStatus"
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark",
								hidden : true
							}]
				}, {
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore3",
						url : "entity/vBizLog",
						requestMethod : "GET",
						root : "root",
						idProperty : "id",
						restful : true,
						autoLoad : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "transId",
									type : "integer",
									text : "流程ID"
								}, {
									name : "entityCode",
									type : "string",
									text : "实体Code"
								}, {
									name : "entityId",
									type : "integer",
									text : "实体ID"
								}, {
									name : "targetId",
									type : "integer",
									text : "对象ID"
								}, {
									name : "operate",
									type : "string",
									text : "操作内容"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}, {
									name : "cuserId",
									type : "uid",
									text : "操作人ID"
								}, {
									name : "cuserName",
									type : "uname",
									text : "操作人"
								}, {
									name : "cdate",
									type : "date",
									text : "操作时间",
									dateFormat : "time"
								}, {
									name : "billCode",
									type : "string",
									text : "表单Code"
								}, {
									name : "billId",
									type : "integer",
									text : "表单ID"
								}, {
									name : "targetType",
									type : "string",
									text : "对象类型"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('rowMaterialOrderInofWin');
								store.baseParams = {
									billId : win.orderId,
									billCode : 'vendorOrder'
								};
							}
						}
					},
					title : "处理流程",
					autoExpandColumn : "remark",
					columns : [{
								header : "主键",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "id",
								id : "id",
								hidden : true
							}, {
								header : "流程ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "transId",
								id : "transId",
								hidden : true
							}, {
								header : "实体Code",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "entityCode",
								id : "entityCode",
								hidden : true
							}, {
								header : "实体ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "entityId",
								id : "entityId",
								hidden : true
							}, {
								header : "对象ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "targetId",
								id : "targetId",
								hidden : true
							}, {
								header : "操作人ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "cuserId",
								id : "cuserId",
								hidden : true
							}, {
								header : "操作人",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "cuserName",
								id : "cuserName"
							}, {
								xtype : "datecolumn",
								header : "操作时间",
								sortable : true,
								resizable : true,
								width : 150,
								format : "Y-m-d H:i:s",
								menuDisabled : true,
								dataIndex : "cdate",
								id : "cdate"
							}, {
								header : "表单Code",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "billCode",
								id : "billCode",
								hidden : true
							}, {
								header : "操作内容",
								sortable : false,
								resizable : true,
								width : 200,
								menuDisabled : true,
								dataIndex : "operate",
								id : "operate"
							}, {
								header : "表单ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "billId",
								id : "billId",
								hidden : true
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark"
							}]
				}],
				listeners : {
					tabchange : function(tabpanel, tab) {
					}
				}
			}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vendorOrder/' + vid
									+ '?type=rawmaterial',
							method : 'GET',
							success : function(resp) {
								var data = Ext.decode(resp.responseText);
								if (data.cdate) {
									data.cdate = Date
											.parseDate(data.cdate, 'u');
								}
								win.form.getForm().setValues(data);
							}
						});
			}
		}
	}
};

com.mzf.common.materOrderInfoWin = {
	xtype : "window",
	width : 624,
	height : 500,
	title : "查看详情",
	constrain : true,
	id : "mateOrderInfoWin",
	layout : "fit",
	modal : true,
	resizable : true,
	buttonAlign : "left",
	autoHeight : true,
	stateful : false,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "tbfill"
				}, {
					xtype : "button",
					text : "确定",
					ref : "../btnCancel",
					listeners : {
						click : function(btn, evt) {
							btn.refOwner.close();
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 80,
		labelAlign : "left",
		layout : "form",
		border : false,
		padding : "6",
		autoHeight : true,
		ref : "form",
		items : [{
					xtype : "container",
					anchor : "100%",
					layout : "column",
					items : [{
								xtype : "container",
								layout : "form",
								columnWidth : 0.5,
								items : [{
											xtype : "hidden",
											fieldLabel : "Label",
											anchor : "100%",
											id : "vendorId",
											name : "vendorId"
										}, {
											xtype : "trigger",
											anchor : "-20",
											fieldLabel : "供应商",
											id : "vendorName",
											name : "vendorName",
											triggerClass : "x-form-search-trigger",
											readOnly : true
										}]
							}, {
								xtype : "container",
								layout : "form",
								columnWidth : 0.5,
								items : [{
											xtype : "datefield",
											anchor : "100%",
											fieldLabel : "希望到货日期",
											readOnly : true,
											name : "expectDate",
											format : "Y-m-d"
										}]
							}]
				}, {
					xtype : "textarea",
					fieldLabel : "备注",
					anchor : "100%",
					name : "remark",
					readOnly : true,
					height : 60
				}]
	}, {
		xtype : "tabpanel",
		activeTab : 0,
		height : 240,
		tabPosition : "bottom",
		border : false,
		ref : "tabs",
		autoHeight : true,
		cls : "topBorder",
		items : [{
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore2",
						url : "entity/materialOrderDetail",
						requestMethod : "GET",
						root : "root",
						idProperty : "id",
						restful : true,
						autoLoad : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "orderId",
									type : "integer",
									text : "物料采购订单ID"
								}, {
									name : "materialId",
									type : "integer",
									text : "物料ID"
								}, {
									name : "type",
									type : "string",
									text : "类型"
								}, {
									name : "name",
									type : "string",
									text : "名称"
								}, {
									name : "cost",
									type : "float",
									text : "总成本"
								}, {
									name : "spec",
									type : "string",
									text : "规格"
								}, {
									name : "model",
									type : "string",
									text : "型号"
								}, {
									name : "quantity",
									type : "float",
									text : "数量"
								}, {
									name : "imageId",
									type : "image",
									text : "图片"
								}, {
									name : "imageId2",
									type : "string",
									text : "图片2"
								}, {
									name : "unit",
									type : "string",
									text : "单位"
								}, {
									name : "status",
									type : "string",
									text : "状态"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('mateOrderInfoWin');
								store.baseParams = {
									orderId : win.orderId
								};
							}
						}
					},
					height : 200,
					autoExpandColumn : "remark",
					autoHeight : false,
					border : false,
					title : "物料列表",
					ref : "./grid",
					columns : [{
								header : "名称",
								sortable : false,
								resizable : true,
								width : 90,
								menuDisabled : true,
								dataIndex : "name",
								id : "name"
							}, {
								xtype : "bizcodecolumn",
								header : "类型",
								sortable : true,
								resizable : true,
								width : 70,
								menuDisabled : true,
								dataIndex : "type",
								id : "type",
								bizType : "materialType"
							}, {
								header : "规格",
								sortable : false,
								resizable : true,
								width : 70,
								menuDisabled : true,
								dataIndex : "spec",
								id : "spec"
							}, {
								header : "型号",
								sortable : false,
								resizable : true,
								width : 70,
								menuDisabled : true,
								dataIndex : "model",
								id : "model"
							}, {
								xtype : "numbercolumn",
								header : "数量",
								sortable : true,
								resizable : true,
								width : 70,
								format : "0,000.00",
								menuDisabled : true,
								dataIndex : "quantity",
								id : "quantity",
								align : "right"
							}, {
								header : "单位",
								sortable : false,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "unit",
								id : "unit"
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark"
							}]
				}, {
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore3",
						url : "entity/vBizLog",
						requestMethod : "GET",
						root : "root",
						idProperty : "id",
						restful : true,
						autoLoad : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "transId",
									type : "integer",
									text : "流程ID"
								}, {
									name : "entityCode",
									type : "string",
									text : "实体Code"
								}, {
									name : "entityId",
									type : "integer",
									text : "实体ID"
								}, {
									name : "targetId",
									type : "integer",
									text : "对象ID"
								}, {
									name : "operate",
									type : "string",
									text : "操作内容"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}, {
									name : "cuserId",
									type : "uid",
									text : "操作人ID"
								}, {
									name : "cuserName",
									type : "uname",
									text : "操作人"
								}, {
									name : "cdate",
									type : "date",
									text : "操作时间",
									dateFormat : "time"
								}, {
									name : "billCode",
									type : "string",
									text : "表单Code"
								}, {
									name : "billId",
									type : "integer",
									text : "表单ID"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('mateOrderInfoWin');
								store.baseParams = {
									billId : win.orderId,
									billCode : 'vendorOrder'
								};
							}
						}
					},
					title : "处理记录",
					autoExpandColumn : "remark",
					height : 200,
					columns : [{
								header : "流程ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "transId",
								id : "transId",
								hidden : true
							}, {
								header : "主键",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "id",
								id : "id",
								hidden : true
							}, {
								header : "实体Code",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "entityCode",
								id : "entityCode",
								hidden : true
							}, {
								header : "实体ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "entityId",
								id : "entityId",
								hidden : true
							}, {
								header : "对象ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "targetId",
								id : "targetId",
								hidden : true
							}, {
								header : "操作人ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "cuserId",
								id : "cuserId",
								hidden : true
							}, {
								header : "操作人",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "cuserName",
								id : "cuserName"
							}, {
								header : "表单ID",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "billId",
								id : "billId",
								hidden : true
							}, {
								xtype : "datecolumn",
								header : "操作时间",
								sortable : true,
								resizable : true,
								width : 150,
								format : "Y-m-d H:i:s",
								menuDisabled : true,
								dataIndex : "cdate",
								id : "cdate"
							}, {
								header : "表单Code",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "billCode",
								id : "billCode",
								hidden : true
							}, {
								header : "操作内容",
								sortable : false,
								resizable : true,
								width : 150,
								menuDisabled : true,
								dataIndex : "operate",
								id : "operate"
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark"
							}]
				}],
		listeners : {
			tabchange : function(tabpanel, tab) {
			}
		}
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vendorOrder/' + vid
									+ '?type=material',
							method : 'GET',
							success : function(resp) {
								var data = Ext.decode(resp.responseText);
								win.form.getForm().setValues(data);
							}
						});
			}
		}
	}
};

com.mzf.common.oemOrderInfoWin = {
	xtype : "window",
	width : 666,
	height : 450,
	title : "查看详情",
	constrain : true,
	id : "orderDetailWin",
	layout : "border",
	modal : true,
	autoHeight : false,
	stateful : false,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确定",
					ref : "../btnOk",
					listeners : {
						click : function(btn, evt) {
							if (btn.refOwner) {
								btn.refOwner.close();
							}
						}
					}
				}]
	},
	items : [{
				xtype : "form",
				labelWidth : 80,
				labelAlign : "left",
				layout : "form",
				border : false,
				region : "north",
				padding : "6",
				autoHeight : true,
				id : "outSourceOrderForm",
				ref : "form",
				items : [{
							xtype : "container",
							anchor : "100%",
							layout : "column",
							items : [{
										xtype : "container",
										layout : "form",
										columnWidth : 0.5,
										items : [{
													xtype : "textfield",
													fieldLabel : "供应商",
													anchor : "-10",
													readOnly : true,
													name : "vendorName"
												}, {
													xtype : "textfield",
													fieldLabel : "订单编号",
													anchor : "-10",
													readOnly : true,
													name : "num"
												}, {
													xtype : "textfield",
													fieldLabel : "创建人",
													anchor : "-10",
													readOnly : true,
													name : "cuserName"
												}]
									}, {
										xtype : "container",
										layout : "form",
										columnWidth : 0.5,
										items : [{
													xtype : "bizcodebox",
													anchor : "100%",
													fieldLabel : "类型",
													editable : false,
													readOnly : true,
													name : "isDiamond",
													bizType : "vendorOrderProductType"
												}, {
													xtype : "textfield",
													fieldLabel : "希望到货日期",
													anchor : "100%",
													readOnly : true,
													name : "expectDate"
												}, {
													xtype : "datefield",
													anchor : "100%",
													fieldLabel : "订单日期",
													name : "cdate",
													readOnly : true,
													format : "Y-m-d"
												}]
									}]
						}, {
							xtype : "textarea",
							fieldLabel : "备注",
							anchor : "100%",
							readOnly : true,
							name : "remark"
						}]
			}, {
				xtype : "tabpanel",
				activeTab : 0,
				region : "center",
				border : false,
				tabPosition : "bottom",
				height : 250,
				id : "orderInfoTab",
				ref : "tabs",
				cls : "topBorder",
				items : [{
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore3",
								url : "entity/vendorProductOrderDetail",
								root : "root",
								idProperty : "id",
								restful : true,
								autoLoad : true,
								requestMethod : "GET",
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "orderId",
											type : "integer",
											text : "供应商订单ID"
										}, {
											name : "ptype",
											type : "string",
											text : "商品类型"
										}, {
											name : "pkind",
											type : "string",
											text : "商品种类"
										}, {
											name : "styleId",
											type : "integer",
											text : "款号ID "
										}, {
											name : "styleCode",
											type : "string",
											text : "款号"
										}, {
											name : "vendorStyleId",
											type : "integer",
											text : "厂家款号ID"
										}, {
											name : "vendorStyleCode",
											type : "string",
											text : "厂家款号"
										}, {
											name : "imageId",
											type : "image",
											text : "图片ID"
										}, {
											name : "weight",
											type : "string",
											text : "主石大小"
										}, {
											name : "color",
											type : "string",
											text : "主石颜色"
										}, {
											name : "clean",
											type : "string",
											text : "主石净度"
										}, {
											name : "cut",
											type : "string",
											text : "主石切工"
										}, {
											name : "goldWeight",
											type : "string",
											text : "金重"
										}, {
											name : "goldClass",
											type : "string",
											text : "金料成色"
										}, {
											name : "kgoldColor",
											type : "string",
											text : "K金颜色"
										}, {
											name : "inset",
											type : "string",
											text : "镶嵌方式"
										}, {
											name : "craft",
											type : "string",
											text : "制作工艺"
										}, {
											name : "size",
											type : "string",
											text : "尺寸"
										}, {
											name : "polishing",
											type : "string",
											text : "抛光性"
										}, {
											name : "symmetry",
											type : "string",
											text : "对称性"
										}, {
											name : "demandId",
											type : "integer",
											text : "要货单ID"
										}, {
											name : "productId",
											type : "integer",
											text : "商品ID"
										}, {
											name : "status",
											type : "string",
											text : "状态"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('orderDetailWin');

										store.baseParams = {
											orderId : win.orderId
										};
									}
								}
							},
							border : false,
							autoExpandColumn : "remark",
							title : "商品记录",
							id : "orderDetailGrid",
							width : 724,
							columns : [{
										xtype : "bizcodecolumn",
										header : "商品类型",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "ptype",
										id : "ptype",
										bizType : "productType"
									}, {
										xtype : "bizcodecolumn",
										header : "商品种类",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "pkind",
										id : "pkind",
										bizType : "productKind"
									}, {
										header : "款号",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "styleCode",
										id : "styleCode",
										hidden : true
									}, {
										header : "厂家款号",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "vendorStyleCode",
										id : "vendorStyleCode"
									}, {
										xtype : "bizcodecolumn",
										header : "主石颜色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "color",
										id : "color",
										hidden : false,
										bizType : "masterColor"
									}, {
										xtype : "bizcodecolumn",
										header : "主石净度",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "clean",
										id : "clean",
										hidden : false,
										bizType : "diamondClean"
									}, {
										xtype : "bizcodecolumn",
										header : "主石切工",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "cut",
										id : "cut",
										hidden : false,
										bizType : "masterCut"
									}, {
										xtype : "bizcodecolumn",
										header : "金料成色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "goldClass",
										id : "goldClass",
										bizType : "goldClass"
									}, {
										xtype : "numbercolumn",
										header : "尺寸",
										sortable : true,
										resizable : true,
										width : 50,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "size",
										id : "size",
										hidden : false
									}, {
										xtype : "bizcodecolumn",
										header : "状态",
										sortable : true,
										resizable : true,
										width : 50,
										menuDisabled : true,
										dataIndex : "status",
										id : "status",
										hidden : false,
										bizType : "vendorOrderDetailStatus"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark",
										hidden : false
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore4",
								url : "entity/vDosing",
								root : "root",
								idProperty : "id",
								restful : true,
								autoLoad : true,
								requestMethod : "GET",
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "detailId",
											type : "integer",
											text : "委外订单明细ID"
										}, {
											name : "rawmaterialId",
											type : "integer",
											text : "原料ID"
										}, {
											name : "status",
											type : "string",
											text : "状态"
										}, {
											name : "minorType",
											type : "string",
											text : "原料小类"
										}, {
											name : "minorTypeName",
											type : "string",
											text : "原料小类"
										}, {
											name : "bomId",
											type : "integer",
											text : "原料需求ID"
										}, {
											name : "dosingQuantity",
											type : "string",
											text : "配料量"
										}, {
											name : "orderId",
											type : "integer",
											text : "订单ID"
										}, {
											name : "rawmaterialNum",
											type : "string",
											text : "原料条码"
										}, {
											name : "rawmaterialType",
											type : "string",
											text : "原料类型"
										}, {
											name : "goldClass",
											type : "string",
											text : "金料成色"
										}, {
											name : "partsType",
											type : "string",
											text : "配件类型"
										}, {
											name : "spec",
											type : "string",
											text : "钻石重量"
										}, {
											name : "color",
											type : "string",
											text : "钻石颜色"
										}, {
											name : "clean",
											type : "string",
											text : "钻石净度"
										}, {
											name : "cut",
											type : "string",
											text : "钻石切工"
										}, {
											name : "shape",
											type : "string",
											text : "钻石形状"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('orderDetailWin');

										store.baseParams = {
											orderId : win.orderId
										};
									}
								}
							},
							title : "原料记录",
							autoExpandColumn : "rawmaterialNum",
							columns : [{
										header : "原料条码",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "rawmaterialNum",
										id : "rawmaterialNum"
									}, {
										xtype : "bizcodecolumn",
										header : "原料类型",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "rawmaterialType",
										id : "rawmaterialType",
										bizType : "rowmaterialType"
									}, {
										header : "原料小类",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "minorTypeName",
										id : "minorTypeName"
									}, {
										xtype : "bizcodecolumn",
										header : "钻石形状",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "shape",
										id : "shape",
										bizType : "diamondShape"
									}, {
										xtype : "bizcodecolumn",
										header : "钻石颜色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "color",
										id : "color",
										bizType : "diamondColor"
									}, {
										header : "钻石切工",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "cut",
										id : "cut",
										hidden : true
									}, {
										header : "钻石净度",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "clean",
										id : "clean",
										hidden : true
									}, {
										header : "钻石重量",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "spec",
										id : "spec"
									}, {
										xtype : "bizcodecolumn",
										header : "金料成色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "goldClass",
										id : "goldClass",
										bizType : "goldClass"
									}, {
										xtype : "bizcodecolumn",
										header : "配件类型",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "partsType",
										id : "partsType",
										bizType : "partsType"
									}, {
										header : "配料量",
										sortable : false,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "dosingQuantity",
										id : "dosingQuantity",
										align : "right"
									}, {
										xtype : "bizcodecolumn",
										header : "状态",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "status",
										id : "status",
										bizType : "rowmaterialVerificationStatus",
										hidden : true
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore5",
								url : "entity/vBizLog",
								root : "root",
								idProperty : "id",
								restful : true,
								autoLoad : true,
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "transId",
											type : "integer",
											text : "流程ID"
										}, {
											name : "entityCode",
											type : "string",
											text : "实体Code"
										}, {
											name : "entityId",
											type : "integer",
											text : "实体ID"
										}, {
											name : "targetId",
											type : "integer",
											text : "对象ID"
										}, {
											name : "operate",
											type : "string",
											text : "操作内容"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cuserId",
											type : "uid",
											text : "操作人ID"
										}, {
											name : "cuserName",
											type : "uname",
											text : "操作人"
										}, {
											name : "cdate",
											type : "date",
											text : "操作时间",
											dateFormat : "time"
										}, {
											name : "billCode",
											type : "string",
											text : "表单Code"
										}, {
											name : "billId",
											type : "integer",
											text : "表单ID"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('orderDetailWin');

										store.baseParams = {
											billId : win.orderId,
											billCode : 'vendorOrder'
										};
									}
								}
							},
							title : "处理流程",
							autoExpandColumn : "remark",
							id : "procGrid",
							columns : [{
										header : "操作人",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "cuserName",
										id : "cuserName"
									}, {
										xtype : "datecolumn",
										header : "操作时间",
										sortable : true,
										resizable : true,
										width : 150,
										format : "Y-m-d H:i:s",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "操作内容",
										sortable : false,
										resizable : true,
										width : 200,
										menuDisabled : true,
										dataIndex : "operate",
										id : "operate"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore6",
								url : "entity/vDosingSummary",
								requestMethod : "GET",
								root : "root",
								restful : true,
								autoLoad : true,
								fields : [{
											name : "orderId",
											type : "integer",
											text : "订单ID"
										}, {
											name : "partsType",
											type : "string",
											text : "配件类型"
										}, {
											name : "rawmaterialNum",
											type : "string",
											text : "原料条码"
										}, {
											name : "gravelStandard",
											type : "string",
											text : "碎石规格"
										}, {
											name : "goldClass",
											type : "string",
											text : "金料成色"
										}, {
											name : "rawmaterialType",
											type : "string",
											text : "原料类型"
										}, {
											name : "partsStandard",
											type : "string",
											text : "配件规格"
										}, {
											name : "totalQuantity",
											type : "float",
											text : "合计量"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext.getCmp('orderDetailWin');
										store.baseParams = {
											orderId : win.orderId
										};
									}
								}
							},
							flex : 0.5,
							border : false,
							ref : "../detailGrid",
							autoExpandColumn : "rawmaterialNum",
							autoScroll : true,
							title : "配料汇总",
							region : "west",
							width : 100,
							columns : [{
										header : "原料条码",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "rawmaterialNum",
										id : "rawmaterialNum"
									}, {
										xtype : "bizcodecolumn",
										header : "原料类型",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "rawmaterialType",
										id : "rawmaterialType",
										bizType : "rowmaterialType"
									}, {
										xtype : "bizcodecolumn",
										header : "碎石规格",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "gravelStandard",
										id : "gravelStandard",
										bizType : "gravelStandard"
									}, {
										xtype : "bizcodecolumn",
										header : "金料成色",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "goldClass",
										id : "goldClass",
										bizType : "goldClass"
									}, {
										xtype : "bizcodecolumn",
										header : "配件类型",
										sortable : true,
										resizable : true,
										width : 70,
										menuDisabled : true,
										dataIndex : "partsType",
										id : "partsType",
										bizType : "partsType"
									}, {
										xtype : "bizcodecolumn",
										header : "配件规格",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "partsStandard",
										id : "partsStandard",
										bizType : "partsStandard"
									}, {
										xtype : "numbercolumn",
										header : "合计量",
										sortable : true,
										resizable : true,
										width : 100,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "totalQuantity",
										id : "totalQuantity",
										align : "right"
									}]
						}]
			}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vendorOrder/' + vid + '?type=OEM',
							method : 'GET',
							success : function(resp) {
								var data = Ext.decode(resp.responseText);
								if (data.cdate) {
									data.cdate = Date
											.parseDate(data.cdate, 'u');
								}
								win.form.getForm().setValues(data);
							}
						});
			}
		}
	}
};

com.mzf.common.maintainOemInfoWin = {
	xtype : "window",
	width : 764,
	height : 450,
	title : "查看详情",
	constrain : true,
	id : "mtorderDetailWin",
	layout : "border",
	modal : true,
	autoHeight : false,
	stateful : false,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确定",
					ref : "../btnOk",
					listeners : {
						click : function(btn, evt) {
							if (btn.refOwner) {
								btn.refOwner.close();
							}
						}
					}
				}]
	},
	items : [{
				xtype : "form",
				labelWidth : 80,
				labelAlign : "left",
				layout : "form",
				border : false,
				region : "north",
				padding : "6",
				autoHeight : true,
				id : "outSourceOrderForm",
				ref : "form",
				items : [{
							xtype : "container",
							anchor : "100%",
							layout : "column",
							items : [{
										xtype : "container",
										layout : "form",
										columnWidth : 0.5,
										items : [{
													xtype : "textfield",
													fieldLabel : "供应商",
													anchor : "-10",
													readOnly : true,
													name : "vendorName"
												}, {
													xtype : "textfield",
													fieldLabel : "订单编号",
													anchor : "-10",
													readOnly : true,
													name : "num"
												}, {
													xtype : "textfield",
													fieldLabel : "创建人",
													anchor : "-10",
													readOnly : true,
													name : "cuserName"
												}]
									}, {
										xtype : "container",
										layout : "form",
										columnWidth : 0.5,
										items : [{
													xtype : "bizcodebox",
													anchor : "100%",
													fieldLabel : "类型",
													editable : false,
													readOnly : true,
													name : "isDiamond",
													bizType : "vendorOrderProductType"
												}, {
													xtype : "textfield",
													fieldLabel : "希望到货日期",
													anchor : "100%",
													readOnly : true,
													name : "expectDate"
												}, {
													xtype : "datefield",
													anchor : "100%",
													fieldLabel : "订单日期",
													name : "cdate",
													readOnly : true,
													format : "Y-m-d"
												}]
									}]
						}, {
							xtype : "textarea",
							fieldLabel : "备注",
							anchor : "100%",
							readOnly : true,
							name : "remark"
						}]
			}, {
				xtype : "tabpanel",
				activeTab : 0,
				region : "center",
				border : false,
				tabPosition : "bottom",
				height : 250,
				id : "orderInfoTab",
				ref : "tabs",
				cls : "topBorder",
				items : [{
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore3",
								url : "entity/vVendorProductOrderDetail",
								root : "root",
								idProperty : "id",
								restful : true,
								autoLoad : true,
								requestMethod : "GET",
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "orderId",
											type : "integer",
											text : "供应商订单ID"
										}, {
											name : "ptype",
											type : "string",
											text : "商品类型"
										}, {
											name : "pkind",
											type : "string",
											text : "商品种类"
										}, {
											name : "styleId",
											type : "integer",
											text : "款号ID "
										}, {
											name : "styleCode",
											type : "string",
											text : "款号"
										}, {
											name : "vendorStyleId",
											type : "integer",
											text : "厂家款号ID"
										}, {
											name : "vendorStyleCode",
											type : "string",
											text : "厂家款号"
										}, {
											name : "imageId",
											type : "image",
											text : "图片ID"
										}, {
											name : "weight",
											type : "string",
											text : "主石大小"
										}, {
											name : "color",
											type : "string",
											text : "主石颜色"
										}, {
											name : "clean",
											type : "string",
											text : "主石净度"
										}, {
											name : "cut",
											type : "string",
											text : "主石切工"
										}, {
											name : "goldWeight",
											type : "string",
											text : "金重"
										}, {
											name : "goldClass",
											type : "string",
											text : "金料成色"
										}, {
											name : "kgoldColor",
											type : "string",
											text : "K金颜色"
										}, {
											name : "inset",
											type : "string",
											text : "镶嵌方式"
										}, {
											name : "craft",
											type : "string",
											text : "制作工艺"
										}, {
											name : "size",
											type : "string",
											text : "尺寸"
										}, {
											name : "maintainFee",
											type : "string",
											text : "维修费"
										}, {
											name : "polishing",
											type : "string",
											text : "抛光性"
										}, {
											name : "symmetry",
											type : "string",
											text : "对称性"
										}, {
											name : "demandId",
											type : "integer",
											text : "要货单ID"
										}, {
											name : "productId",
											type : "integer",
											text : "商品ID"
										}, {
											name : "productNum",
											type : "string",
											text : "商品条码"
										}, {
											name : "status",
											type : "string",
											text : "状态"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext
												.getCmp('mtorderDetailWin');

										store.baseParams = {
											orderId : win.orderId
										};
									}
								}
							},
							border : false,
							autoExpandColumn : "remark",
							title : "商品记录",
							id : "orderDetailGrid",
							width : 724,
							columns : [{
										header : "商品条码",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "productNum"
									}, {
										xtype : "bizcodecolumn",
										header : "商品类型",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "ptype",
										id : "ptype",
										bizType : "productType"
									}, {
										xtype : "bizcodecolumn",
										header : "商品种类",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "pkind",
										id : "pkind",
										bizType : "productKind"
									}, {
										header : "款号",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "styleCode",
										id : "styleCode",
										hidden : true
									}, {
										header : "厂家款号",
										sortable : false,
										resizable : true,
										width : 90,
										menuDisabled : true,
										dataIndex : "vendorStyleCode",
										id : "vendorStyleCode"
									}, {
										xtype : "bizcodecolumn",
										header : "主石颜色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "color",
										id : "color",
										hidden : false,
										bizType : "masterColor"
									}, {
										xtype : "bizcodecolumn",
										header : "主石净度",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "clean",
										id : "clean",
										hidden : false,
										bizType : "diamondClean"
									}, {
										xtype : "bizcodecolumn",
										header : "主石切工",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "cut",
										id : "cut",
										hidden : false,
										bizType : "masterCut"
									}, {
										xtype : "bizcodecolumn",
										header : "金料成色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "goldClass",
										id : "goldClass",
										bizType : "goldClass"
									}, {
										xtype : "numbercolumn",
										header : "尺寸",
										sortable : true,
										resizable : true,
										width : 50,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "size",
										id : "size",
										hidden : false
									}, {
										xtype : "numbercolumn",
										header : "维修费",
										sortable : true,
										resizable : true,
										width : 50,
										format : "0,000.00",
										menuDisabled : true,
										dataIndex : "maintainFee",
										hidden : false
									}, {
										xtype : "bizcodecolumn",
										header : "状态",
										sortable : true,
										resizable : true,
										width : 50,
										menuDisabled : true,
										dataIndex : "status",
										id : "status",
										hidden : false,
										bizType : "vendorOrderDetailStatus"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark",
										hidden : false
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore4",
								url : "entity/vDosing",
								root : "root",
								idProperty : "id",
								restful : true,
								autoLoad : true,
								requestMethod : "GET",
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "detailId",
											type : "integer",
											text : "委外订单明细ID"
										}, {
											name : "rawmaterialId",
											type : "integer",
											text : "原料ID"
										}, {
											name : "status",
											type : "string",
											text : "状态"
										}, {
											name : "minorType",
											type : "string",
											text : "原料小类"
										}, {
											name : "bomId",
											type : "integer",
											text : "原料需求ID"
										}, {
											name : "dosingQuantity",
											type : "float",
											text : "配料量"
										}, {
											name : "orderId",
											type : "integer",
											text : "订单ID"
										}, {
											name : "rawmaterialNum",
											type : "string",
											text : "原料条码"
										}, {
											name : "rawmaterialType",
											type : "string",
											text : "原料类型"
										}, {
											name : "goldClass",
											type : "string",
											text : "金料成色"
										}, {
											name : "partsType",
											type : "string",
											text : "配件类型"
										}, {
											name : "spec",
											type : "float",
											text : "钻石规格"
										}, {
											name : "color",
											type : "string",
											text : "钻石颜色"
										}, {
											name : "clean",
											type : "string",
											text : "钻石净度"
										}, {
											name : "cut",
											type : "string",
											text : "钻石切工"
										}, {
											name : "shape",
											type : "string",
											text : "钻石形状"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext
												.getCmp('mtorderDetailWin');

										store.baseParams = {
											orderId : win.orderId
										};
									}
								}
							},
							title : "原料记录",
							columns : [{
										header : "原料条码",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "rawmaterialNum",
										id : "rawmaterialNum"
									}, {
										header : "原料小类",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "minorType",
										id : "minorType"
									}, {
										xtype : "bizcodecolumn",
										header : "原料类型",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "rawmaterialType",
										id : "rawmaterialType",
										bizType : "rowmaterialType"
									}, {
										xtype : "bizcodecolumn",
										header : "钻石形状",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "shape",
										id : "shape",
										bizType : "diamondShape"
									}, {
										xtype : "bizcodecolumn",
										header : "钻石颜色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "color",
										id : "color",
										bizType : "diamondColor"
									}, {
										header : "钻石切工",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "cut",
										id : "cut",
										hidden : true
									}, {
										header : "钻石净度",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "clean",
										id : "clean",
										hidden : true
									}, {
										header : "钻石重量",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "spec",
										id : "spec"
									}, {
										xtype : "bizcodecolumn",
										header : "金料成色",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "goldClass",
										id : "goldClass",
										bizType : "goldClass"
									}, {
										header : "配料量",
										sortable : false,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "dosingQuantity",
										id : "dosingQuantity"
									}, {
										xtype : "bizcodecolumn",
										header : "配件类型",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "partsType",
										id : "partsType",
										bizType : "partsType"
									}, {
										xtype : "bizcodecolumn",
										header : "状态",
										sortable : true,
										resizable : true,
										width : 60,
										menuDisabled : true,
										dataIndex : "status",
										id : "status",
										bizType : "rowmaterialVerificationStatus"
									}]
						}, {
							xtype : "grid",
							store : {
								xtype : "jsonstore",
								storeId : "MyStore5",
								url : "entity/vBizLog",
								root : "root",
								idProperty : "id",
								restful : true,
								autoLoad : true,
								fields : [{
											name : "id",
											type : "integer",
											text : "主键"
										}, {
											name : "transId",
											type : "integer",
											text : "流程ID"
										}, {
											name : "entityCode",
											type : "string",
											text : "实体Code"
										}, {
											name : "entityId",
											type : "integer",
											text : "实体ID"
										}, {
											name : "targetId",
											type : "integer",
											text : "对象ID"
										}, {
											name : "operate",
											type : "string",
											text : "操作内容"
										}, {
											name : "remark",
											type : "string",
											text : "备注"
										}, {
											name : "cuserId",
											type : "uid",
											text : "操作人ID"
										}, {
											name : "cuserName",
											type : "uname",
											text : "操作人"
										}, {
											name : "cdate",
											type : "date",
											text : "操作时间",
											dateFormat : "time"
										}, {
											name : "billCode",
											type : "string",
											text : "表单Code"
										}, {
											name : "billId",
											type : "integer",
											text : "表单ID"
										}],
								listeners : {
									beforeload : function(store, options) {
										var win = Ext
												.getCmp('mtorderDetailWin');

										store.baseParams = {
											billId : win.orderId,
											billCode : 'vendorOrder'
										};
									}
								}
							},
							title : "处理流程",
							autoExpandColumn : "remark",
							id : "procGrid",
							columns : [{
										header : "操作人",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "cuserName",
										id : "cuserName"
									}, {
										xtype : "datecolumn",
										header : "操作时间",
										sortable : true,
										resizable : true,
										width : 150,
										format : "Y-m-d H:i:s",
										menuDisabled : true,
										dataIndex : "cdate",
										id : "cdate"
									}, {
										header : "操作内容",
										sortable : false,
										resizable : true,
										width : 200,
										menuDisabled : true,
										dataIndex : "operate",
										id : "operate"
									}, {
										header : "备注",
										sortable : false,
										resizable : true,
										width : 100,
										menuDisabled : true,
										dataIndex : "remark",
										id : "remark"
									}]
						}]
			}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vendorOrder/' + vid
									+ '?type=maintainOEM',
							method : 'GET',
							success : function(resp) {
								var data = Ext.decode(resp.responseText);
								if (data.cdate) {
									data.cdate = Date
											.parseDate(data.cdate, 'u');
								}
								win.form.getForm().setValues(data);
							}
						});
			}
		}
	}
};
com.mzf.common.secondGoldTransInfoWin = {
	xtype : "window",
	width : 611,
	height : 450,
	title : "查看详情",
	constrain : true,
	layout : "border",
	buttonAlign : "left",
	id : "transformWin",
	autoHeight : false,
	modal : true,
	stateful : false,
	resizable : true,
	bodyBorder : true,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "tbfill"
				}, {
					xtype : "button",
					text : "确定",
					ref : "../btnOk",
					listeners : {
						click : function(btn, evt) {
							var win = btn.refOwner;
							if (win) {
								win.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 85,
		labelAlign : "left",
		layout : "form",
		border : false,
		autoHeight : true,
		padding : "6",
		region : "north",
		height : 200,
		autoWidth : false,
		ref : "form",
		items : [{
					xtype : "container",
					anchor : "100%",
					layout : "column",
					items : [{
								xtype : "container",
								layout : "form",
								columnWidth : 0.5,
								items : [{
											xtype : "textfield",
											fieldLabel : "旧金条码",
											anchor : "-10",
											name : "targetNum",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "调出部门",
											anchor : "-10",
											name : "sourceOrgName",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "发起人",
											anchor : "-10",
											name : "cuserName",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "调拨重量",
											anchor : "-10",
											name : "quantity",
											readOnly : true
										}]
							}, {
								xtype : "container",
								layout : "form",
								columnWidth : 0.5,
								items : [{
											xtype : "bizcodebox",
											anchor : "100%",
											fieldLabel : "旧金成色",
											editable : false,
											name : "targetName",
											readOnly : true,
											bizType : "secondGoldClass"
										}, {
											xtype : "textfield",
											fieldLabel : "调入部门",
											anchor : "100%",
											name : "targetOrgName",
											readOnly : true
										}, {
											xtype : "datefield",
											anchor : "100%",
											fieldLabel : "发起时间",
											triggerClass : "x-form-search-trigger",
											name : "cdate",
											readOnly : true,
											format : "Y-m-d H:i:s"
										}, {
											xtype : "textfield",
											fieldLabel : "收货重量",
											anchor : "100%",
											name : "actualQuantity",
											readOnly : true
										}]
							}]
				}, {
					xtype : "textarea",
					fieldLabel : "备注",
					anchor : "100%",
					name : "remark",
					readOnly : true
				}]
	}, {
		xtype : "tabpanel",
		activeTab : 0,
		region : "center",
		width : 100,
		tabPosition : "bottom",
		border : false,
		cls : "topBorder",
		items : [{
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore1",
						url : "entity/vBizLog",
						root : "root",
						idProperty : "id",
						autoLoad : true,
						restful : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "transId",
									type : "integer",
									text : "流程ID"
								}, {
									name : "entityCode",
									type : "string",
									text : "实体Code"
								}, {
									name : "entityId",
									type : "integer",
									text : "实体ID"
								}, {
									name : "targetId",
									type : "integer",
									text : "对象ID"
								}, {
									name : "operate",
									type : "string",
									text : "操作内容"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}, {
									name : "cuserId",
									type : "uid",
									text : "操作人ID"
								}, {
									name : "cuserName",
									type : "uname",
									text : "操作人"
								}, {
									name : "cdate",
									type : "date",
									text : "操作时间",
									dateFormat : "time"
								}, {
									name : "billCode",
									type : "string",
									text : "表单Code"
								}, {
									name : "billId",
									type : "integer",
									text : "表单ID"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('transformWin');
								store.baseParams = {
									billId : win.orderId,
									billCode : 'transfer'
								};
							}
						}
					},
					autoHeight : false,
					region : "center",
					width : 100,
					border : false,
					title : "操作记录",
					autoExpandColumn : "remark",
					columns : [{
								header : "操作人",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "cuserName",
								id : "cuserName"
							}, {
								xtype : "datecolumn",
								header : "操作时间",
								sortable : true,
								resizable : true,
								width : 140,
								format : "Y-m-d H:i:s",
								menuDisabled : true,
								dataIndex : "cdate",
								id : "cdate"
							}, {
								header : "操作内容",
								sortable : false,
								resizable : true,
								width : 120,
								menuDisabled : true,
								dataIndex : "operate",
								id : "operate"
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark"
							}]
				}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vTransfer?targetType=secondGold',
							method : 'GET',
							params : {
								id : vid
							},
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result) {
									var form = win.form;
									if (form) {
										var data = result.root[0];
										data.cdate
												&& (data.cdate = new Date(data.cdate)
														.format('Y-m-d H:i:s'));
										form.getForm().setValues(data);
									}

								}
							},
							failure : function(resp) {
								Ext.Msg.alert('错误', resp.responseText);
							}
						});
			}
		}
	}
};

com.mzf.common.maintainInfoWin = {
	xtype : "window",
	width : 750,
	height : 600,
	title : "查看详情",
	constrain : true,
	id : "maintainInfoWin",
	layout : "border",
	buttonAlign : "left",
	resizable : true,
	modal : true,
	stateful : false,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "tbfill"
				}, {
					xtype : "button",
					text : "确定",
					ref : "../btnCancel",
					listeners : {
						click : function(btn, evt) {
							btn.refOwner.close();
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 70,
		labelAlign : "left",
		layout : "form",
		border : false,
		padding : "6",
		region : "north",
		height : 400,
		items : [{
			xtype : "fieldset",
			layout : "column",
			columnWidth : 1,
			autoHeight : true,
			style : "padding:6px;",
			items : [{
				xtype : "container",
				columnWidth : 0.33,
				layout : "form",
				labelWidth : 60,
				items : [{
							xtype : "hidden",
							fieldLabel : "Label",
							anchor : "100%",
							name : "orgId"
						}, {
							xtype : "textfield",
							fieldLabel : "维修部门",
							anchor : "-20",
							name : "orgName",
							readOnly : true,
							tabIndex : 1
						}, {
							xtype : "hidden",
							fieldLabel : "Label",
							anchor : "100%",
							name : "cusId",
							id : "cusId"
						}, {
							xtype : "trigger",
							anchor : "-20",
							fieldLabel : "客户姓名",
							triggerClass : "x-form-search-trigger",
							name : "cusName",
							id : "cusName",
							allowBlank : true,
							editable : false,
							blankText : "会员编号是必填项",
							tabIndex : 4,
							readOnly : true,
							listeners : {
								triggerclick : function(field) {
									var win = od.showWindow('queryCustWin');
									if (win) {
										var grid = win.custGrid;
										if (grid) {
											grid.getStore().load({
														params : {
															start : 0,
															limit : 30
														}
													});
										}

										win.setCust = function(record) {
											var win = Ext
													.getCmp('newMaintainWin');
											if (win) {
												var form = win.get(0);
												if (form) {
													form.getForm().setValues({
														cusId : record
																.get('id'),
														cusName : record
																.get('name'),
														tel : record.get('tel'),
														cardNo : record
																.get('cardNo')
													});
												}
											}

										}

									}
								}
							}
						}]
			}, {
				xtype : "container",
				columnWidth : 0.34,
				layout : "form",
				labelWidth : 60,
				items : [{
					xtype : "combo",
					anchor : "-20",
					fieldLabel : "接单人",
					queryAction : "all",
					triggerAction : "all",
					mode : "remote",
					valueField : "id",
					displayField : "name",
					name : "employeeId",
					allowBlank : true,
					editable : false,
					clearFilterOnReset : false,
					id : "empId",
					blankText : "接单人是必填项",
					tabIndex : 2,
					readOnly : true,
					store : {
						xtype : "jsonstore",
						storeId : "MyStore2",
						root : "root",
						url : "entity/employee",
						restful : true,
						requestMethod : "GET",
						autoLoad : false,
						beforeload : function(store, options) {
							store.setBaseParam('shopId',
									od.AppInstance.appConfig.user.shopId);
						},
						fields : ['id', 'name'],
						listeners : {
							beforeload : function(store, options) {
								store.setBaseParam('orgId',
										od.AppInstance.appConfig.user.orgId);
							}
						}
					}
				}, {
					xtype : "textfield",
					fieldLabel : "会员卡号",
					anchor : "-20",
					name : "cardNo",
					readOnly : true,
					allowBlank : true,
					blankText : "客户名称是必填项",
					tabIndex : 5
				}]
			}, {
				xtype : "container",
				columnWidth : 0.33,
				layout : "form",
				items : [{
							xtype : "datefield",
							anchor : "-20",
							fieldLabel : "取货时间",
							name : "deliveryDate",
							format : "Y-m-d",
							allowBlank : true,
							blankText : "取货时间不能为空",
							tabIndex : 3,
							readOnly : true
						}, {
							xtype : "textfield",
							fieldLabel : "手机",
							anchor : "-20",
							name : "tel",
							readOnly : true,
							tabIndex : 6,
							allowBlank : true
						}]
			}]
		}, {
			xtype : "fieldset",
			layout : "column",
			columnWidth : 0.5,
			autoHeight : true,
			style : "padding:6px;",
			items : [{
				xtype : "container",
				columnWidth : 1,
				layout : "column",
				items : [{
					xtype : "container",
					columnWidth : 1,
					layout : "form",
					labelWidth : 60,
					items : [{
								xtype : "hidden",
								fieldLabel : "Label",
								anchor : "100%",
								name : "productId"
							}, {
								xtype : "trigger",
								anchor : "-20",
								fieldLabel : "商品条码",
								triggerClass : "x-form-search-trigger",
								editable : false,
								name : "productNum",
								readOnly : true,
								tabIndex : 7,
								allowBlank : true,
								listeners : {
									triggerclick : function(field) {
										var win2 = od.showWindow('scanProdWin');
										if (win2) {
											win2.setProd = function(data) {
												var win = Ext
														.getCmp('newMaintainWin');
												if (win) {
													var form = win.get(0);
													if (form) {
														data.productNum = data.num;
														data.productId = data.id;
														data.productName = data.name;
														data.diamondWeight = data.diamondSize;
														data.discount = null;
														data.actualAccounts = null;
														form
																.getForm()
																.setValues(data);
														if (data.imageId) {
															var imgdom = Ext
																	.get('imgV');
															imgdom.dom.src = 'image/'
																	+ data.imageId;
														}
														win2.get(0).getForm()
																.reset();
													}
												}
											}
										}
									}
								}
							}]
				}, {
					xtype : "container",
					columnWidth : 0.5,
					layout : "form",
					labelWidth : 60,
					items : [{
								xtype : "textfield",
								fieldLabel : "商品名称",
								anchor : "-10",
								name : "productName",
								tabIndex : 17,
								readOnly : true,
								allowBlank : true
							}, {
								xtype : "bizcodebox",
								anchor : "-10",
								fieldLabel : "商品类型",
								editable : false,
								name : "ptype",
								bizType : "productType",
								tabIndex : 9,
								clearable : true,
								readOnly : true,
								allowBlank : true
							}, {
								xtype : "bizcodebox",
								anchor : "-10",
								fieldLabel : "金料成色",
								editable : false,
								name : "goldClass",
								bizType : "goldClass",
								tabIndex : 9,
								clearable : true,
								readOnly : true,
								allowBlank : true
							}, {
								xtype : "bizcodebox",
								anchor : "-10",
								fieldLabel : "主石颜色",
								editable : false,
								name : "diamondColor",
								bizType : "diamondColor",
								tabIndex : 11,
								clearable : true,
								readOnly : true,
								allowBlank : true
							}, {
								xtype : "bizcodebox",
								anchor : "-10",
								fieldLabel : "主石切工",
								editable : false,
								name : "diamondCut",
								bizType : "diamondCut",
								tabIndex : 13,
								clearable : true,
								readOnly : true,
								allowBlank : true
							}, {
								xtype : "bizcodebox",
								anchor : "-10",
								fieldLabel : "抛光性",
								editable : false,
								name : "polishing",
								bizType : "polishing",
								tabIndex : 15,
								clearable : true,
								readOnly : true,
								allowBlank : true
							}, {
								xtype : "numberfield",
								fieldLabel : "一口价",
								anchor : "-10",
								name : "retailBasePrice",
								maxValue : 99999999,
								minValue : 0,
								decimalPrecision : 2,
								allowBlank : true,
								blankText : "商品价格是必填项",
								tabIndex : 22,
								id : "priceId",
								readOnly : true
							}, {
								xtype : "bizcodebox",
								anchor : "-10",
								fieldLabel : "维修类型",
								editable : false,
								name : "maintainType",
								maxValue : 99999999,
								minValue : 0,
								decimalPrecision : 2,
								allowBlank : true,
								tabIndex : 22,
								readOnly : true,
								clearable : true,
								bizType : "maintain_type",
								blankText : "维修类型是必填项",
								checkable : true
							}]
				}, {
					xtype : "container",
					columnWidth : 0.5,
					layout : "form",
					labelWidth : 60,
					items : [{
								xtype : "numberfield",
								fieldLabel : "商品尺寸",
								anchor : "-20",
								name : "size",
								maxValue : 99999999,
								minValue : 0,
								decimalPrecision : 2,
								allowBlank : true,
								blankText : "商品尺寸是必填项",
								tabIndex : 23,
								readOnly : true
							}, {
								xtype : "bizcodebox",
								anchor : "-20",
								fieldLabel : "商品种类",
								editable : false,
								name : "pkind",
								bizType : "productKind",
								tabIndex : 9,
								clearable : true,
								readOnly : true,
								allowBlank : true
							}, {
								xtype : "bizcodebox",
								anchor : "-20",
								fieldLabel : "K金颜色",
								editable : false,
								name : "kgoldColor",
								bizType : "kGoldColor",
								tabIndex : 10,
								clearable : true,
								readOnly : true
							}, {
								xtype : "bizcodebox",
								anchor : "-20",
								fieldLabel : "主石净度",
								editable : false,
								name : "diamondClean",
								bizType : "diamondClean",
								tabIndex : 12,
								clearable : true,
								readOnly : true
							}, {
								xtype : "numberfield",
								fieldLabel : "主石重量",
								anchor : "-20",
								name : "diamondWeight",
								tabIndex : 14,
								id : "diamondWeight",
								blankText : "主石重量是必填项",
								allowBlank : true,
								readOnly : true
							}, {
								xtype : "hidden",
								fieldLabel : "Label",
								anchor : "100%",
								name : "diamondId"
							}, {
								xtype : "bizcodebox",
								anchor : "-20",
								fieldLabel : "对称性",
								editable : false,
								name : "symmetry",
								bizType : "symmetry",
								tabIndex : 16,
								clearable : true,
								readOnly : true
							}, {
								xtype : "numberfield",
								fieldLabel : "折扣金额",
								anchor : "-20",
								name : "discount",
								maxValue : 99999999,
								minValue : 0,
								decimalPrecision : 2,
								allowBlank : true,
								blankText : "商品价格是必填项",
								tabIndex : 24,
								id : "discountId",
								readOnly : true
							}, {
								xtype : "bizcodebox",
								anchor : "-20",
								fieldLabel : "商品来源",
								editable : false,
								name : "productSource",
								maxValue : 99999999,
								minValue : 0,
								decimalPrecision : 2,
								allowBlank : true,
								tabIndex : 22,
								readOnly : true,
								clearable : true,
								bizType : "maintainProductSource",
								blankText : "维修类型是必填项"
							}]
				}]
			}, {
				xtype : "container",
				layout : "form",
				width : 250,
				autoWidth : false,
				items : [{
					xtype : "fieldset",
					layout : "form",
					html : "<img id='imgV' width='230' height='175' src='images/noImage.jpg'>",
					id : "viewImg",
					autoHeight : true,
					autoWidth : true
				}, {
					xtype : "trigger",
					anchor : "100%",
					fieldLabel : "商品图片",
					triggerClass : "x-form-search-trigger",
					editable : false,
					name : "imageId",
					readOnly : true,
					allowBlank : true,
					listeners : {
						triggerclick : function(field) {
							var win = od.showWindow('uploadWin');
							if (win) {
								win.upload = function(img) {
									var win2 = Ext.getCmp('newMaintainWin');
									if (win2) {
										var form = win2.get(0);
										if (form) {
											form.getForm().setValues({
														imageId : img
													});
											var imgdoc = Ext.get('imgV');
											imgdoc.dom.src = 'image/' + img;
										}
									}

								}
							}
						}
					}
				}]
			}]
		}, {
			xtype : "fieldset",
			layout : "column",
			columnWidth : 1,
			style : "padding:6px;",
			items : [{
						xtype : "container",
						layout : "form",
						columnWidth : 0.32,
						labelWidth : 60,
						items : [{
									xtype : "textfield",
									fieldLabel : "预收定金",
									anchor : "-20",
									name : "amount",
									allowBlank : true,
									blankText : "预收定金不能为空",
									readOnly : true
								}]
					}, {
						xtype : "container",
						layout : "form",
						columnWidth : 0.25,
						labelWidth : 60,
						items : [{
									xtype : "bizcodebox",
									anchor : "-20",
									fieldLabel : "收款方式",
									editable : false,
									name : "payType",
									bizType : "cusOrderPaymentWay",
									clearable : true,
									blankText : "收款方式是必填项",
									allowBlank : true,
									readOnly : true
								}]
					}, {
						xtype : "container",
						layout : "form",
						columnWidth : 0.28,
						labelWidth : 60,
						items : [{
									xtype : "bizcodebox",
									anchor : "-10",
									fieldLabel : "银行",
									editable : false,
									name : "bank",
									bizType : "bankType",
									clearable : true,
									readOnly : true,
									allowBlank : true
								}]
					}, {
						xtype : "container",
						layout : "form",
						columnWidth : 0.15,
						labelWidth : 80,
						items : [{
									xtype : "checkbox",
									boxLabel : " ",
									fieldLabel : "是否商场代收",
									id : "isAgent",
									name : "isAgent",
									inputValue : "true",
									readOnly : true
								}]
					}]
		}]
	}, {
		xtype : "tabpanel",
		activeTab : 0,
		region : "center",
		tabPosition : "bottom",
		border : false,
		cls : "topBorder",
		items : [{
					xtype : "panel",
					columnWidth : 1,
					layout : "form",
					title : "备注",
					border : false,
					padding : "10",
					items : [{
								xtype : "textarea",
								fieldLabel : "备注",
								anchor : "100%",
								blankText : "其他要求特别需求在备注中说明",
								name : "remark",
								id : "remark",
								readOnly : true,
								allowBlank : true
							}]
				}, {
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore3",
						url : "entity/vBizLog",
						requestMethod : "GET",
						root : "root",
						idProperty : "id",
						restful : true,
						autoLoad : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "transId",
									type : "integer",
									text : "流程ID"
								}, {
									name : "entityCode",
									type : "string",
									text : "实体Code"
								}, {
									name : "entityId",
									type : "integer",
									text : "实体ID"
								}, {
									name : "targetId",
									type : "integer",
									text : "对象ID"
								}, {
									name : "operate",
									type : "string",
									text : "操作内容"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}, {
									name : "cuserId",
									type : "uid",
									text : "操作人ID"
								}, {
									name : "cuserName",
									type : "uname",
									text : "操作人"
								}, {
									name : "cdate",
									type : "date",
									text : "操作时间",
									dateFormat : "time"
								}, {
									name : "billCode",
									type : "string",
									text : "表单Code"
								}, {
									name : "billId",
									type : "integer",
									text : "表单ID"
								}, {
									name : "targetType",
									type : "string",
									text : "对象类型"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('maintainInfoWin');
								store.setBaseParam('billId', win.orderId);
								store.setBaseParam('billCode', 'maintain');
							}
						}
					},
					width : 100,
					border : false,
					autoExpandColumn : "remark",
					title : "操作记录",
					region : "west",
					columns : [{
								xtype : "datecolumn",
								header : "操作时间",
								sortable : true,
								resizable : true,
								width : 150,
								format : "Y-m-d H:i:s",
								menuDisabled : true,
								dataIndex : "cdate",
								id : "cdate"
							}, {
								header : "操作人",
								sortable : false,
								resizable : true,
								width : 120,
								menuDisabled : true,
								dataIndex : "cuserName",
								id : "cuserName"
							}, {
								header : "操作内容",
								sortable : false,
								resizable : true,
								width : 160,
								menuDisabled : true,
								dataIndex : "operate",
								id : "operate"
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark"
							}]
				}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vMaintain/' + vid,
							method : 'GET',
							success : function(resp) {
								var data = Ext.decode(resp.responseText);
								if (data) {
									var form = win.get(0);
									form.getForm().setValues(data);
									data.remark
											&& Ext.getCmp('remark')
													.setValue(data.remark);
									var emp = Ext.getCmp('empId');
									emp
											&& emp.setValue(data.employeeId)
											&& emp
													.setRawValue(data.employeeName);
									if (data.imageId) {
										var imgdom = Ext.get('imgV');
										imgdom.dom.src = 'image/'
												+ data.imageId;
									}
								}
							}
						});
			}
		}
	}
};

com.mzf.common.renovateInfoWin = {
	xtype : "window",
	width : 837,
	height : 250,
	title : "查看详情",
	constrain : true,
	layout : "fit",
	id : "detailWin",
	modal : true,
	autoScroll : false,
	autoHeight : true,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "button",
					text : "确定",
					ref : "../btnCancel",
					listeners : {
						click : function(btn, evt) {
							if (this.refOwner) {
								this.refOwner.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 90,
		labelAlign : "left",
		layout : "column",
		border : false,
		region : "north",
		width : 1056,
		autoHeight : true,
		padding : "6px",
		ref : "form",
		items : [{
			xtype : "container",
			layout : "form",
			columnWidth : 0.25,
			items : [{
						xtype : "hidden",
						fieldLabel : "商品来源",
						anchor : "100%",
						name : "productSource",
						id : "productSource"
					}, {
						xtype : "hidden",
						fieldLabel : "商品ID",
						anchor : "100%",
						name : "productId",
						id : "productId"
					}, {
						xtype : "trigger",
						anchor : "-10",
						fieldLabel : "商品条码",
						triggerClass : "x-form-search-trigger",
						name : "productNum",
						editable : false,
						readOnly : true,
						listeners : {
							triggerclick : function(field) {
								var ps = Ext.getCmp('productSource');
								if (ps.getValue() == 'secondProduct') {
									var win = od.showWindow('productWin');
									if (win) {
										win.getProd = function(record) {
											var win2 = Ext.getCmp('recycleWin');
											if (win2) {
												var form = win2.form;
												if (form) {
													form.getForm().setValues({
														productId : record.data.id,
														productNum : record.data.num,
														sourceOrgId : record.data.sourceOrgId,
														rightDiamondSize : record.data.diamondSize,
														sourceOrgName : record.data.sourceOrgName,
														wholesalePrice : record.data.wholesalePrice,
														rightGoldWeight : record.data.goldWeight
																|| record.data.weight
													});
												}
												win2.splitSum();
												win.close();
												Ext.getCmp('detailId').show();
											}

										}
									}
								} else if (ps.getValue() == 'product') {
									var win = od.showWindow('productStoreWin');
									if (win) {
										win.getProd = function(record) {
											var win2 = Ext.getCmp('recycleWin');
											if (win2) {
												var form = win2.form;
												if (form) {
													form.getForm().setValues({
														productId : record.data.id,
														productNum : record.data.num,
														sourceOrgId : record.data.sourceOrgId,
														sourceOrgName : record.data.sourceOrgName,
														wholesalePrice : record.data.wholesalePrice,
														rightGoldWeight : record.data.goldWeight
													});
												}
												win2.splitSum();
												win.close();
												Ext.getCmp('detailId').show();
											}

										}
									}
								} else if (ps.getValue() == 'maintainProduct') {
									var win = od.showWindow('maintainStoreWin');
									if (win) {
										win.getProd = function(record) {
											var win2 = Ext.getCmp('recycleWin');
											if (win2) {
												var form = win2.form;
												if (form) {
													form.getForm().setValues({
														productId : record.data.id,
														productNum : record.data.num,
														sourceOrgId : record.data.sourceOrgId,
														sourceOrgName : record.data.sourceOrgName,
														wholesalePrice : record.data.wholesalePrice,
														rightGoldWeight : record.data.goldWeight
													});
												}
												win2.splitSum();
												win.close();
											}

										}
									}
								} else {
									Ext.Msg.alert('提示', '没有指定商品来源');
								}
							}
						}
					}, {
						xtype : "numberfield",
						fieldLabel : "应返金重",
						anchor : "-10",
						name : "rightGoldWeight",
						id : "rightGoldWeight",
						allowDecimals : true,
						allowNegative : true,
						decimalPrecision : 3,
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "应返石重",
						anchor : "-10",
						name : "rightDiamondSize",
						id : "rightDiamondSize",
						allowDecimals : true,
						allowNegative : true,
						decimalPrecision : 3,
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "丢失耳壁数量",
						anchor : "-10",
						name : "lossEb",
						id : "lossEb",
						readOnly : true
					}]
		}, {
			xtype : "container",
			layout : "form",
			columnWidth : 0.25,
			items : [{
						xtype : "hidden",
						fieldLabel : "来源部门ID",
						anchor : "100%",
						name : "sourceOrgId"
					}, {
						xtype : "textfield",
						fieldLabel : "来源部门",
						anchor : "-10",
						readOnly : true,
						name : "sourceOrgName"
					}, {
						xtype : "numberfield",
						fieldLabel : "实返金重",
						anchor : "-10",
						name : "actualGoldWeight",
						id : "actualGoldWeight",
						allowDecimals : true,
						allowNegative : true,
						decimalPrecision : 3,
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "丢失钻石克拉数",
						anchor : "-10",
						name : "lossDiamond",
						id : "lossDiamond",
						allowDecimals : true,
						allowNegative : true,
						decimalPrecision : 3,
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "丢失耳壁扣钱",
						anchor : "-10",
						name : "deductMoneyEb",
						id : "deductMoneyEb",
						blankText : "请输入丢失耳壁扣钱",
						allowBlank : false,
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}]
		}, {
			xtype : "container",
			layout : "form",
			columnWidth : 0.25,
			items : [{
						xtype : "numberfield",
						fieldLabel : "批发价",
						anchor : "-10",
						name : "wholesalePrice",
						readOnly : true,
						id : "wholesalePrice"
					}, {
						xtype : "combo",
						anchor : "-10",
						fieldLabel : "金料成色",
						queryAction : "all",
						name : "rightGoldClass",
						displayField : "name",
						editable : false,
						mode : "remote",
						triggerAction : "all",
						valueField : "goldClass",
						blankText : "旧金成色是必填项",
						id : "goldClassId",
						region : "west",
						width : 100,
						readOnly : true,
						store : {
							xtype : "jsonstore",
							storeId : "MyStore1",
							url : "entity/buyGoldUnitPrice",
							requestMethod : "GET",
							root : "root",
							restful : true,
							fields : [{
										name : "unitPrice",
										type : "float",
										text : "回收单价"
									}, {
										name : "orgId",
										type : "integer",
										text : "组织机构ID"
									}, {
										name : "goldClass",
										type : "string",
										text : "金料成色"
									}, {
										name : "name",
										type : "string",
										text : "金料成色"
									}],
							listeners : {
								beforeload : function(store, options) {
									store
											.setBaseParam(
													'orgId',
													od.AppInstance.appConfig.user.orgId);
								}
							}
						},
						listeners : {
							select : function(combo, record, index) {
								var cmb = Ext.getCmp('goldClassId');
								if (cmb) {
									var index = cmb.getStore().find(
											'goldClass', record.data.goldClass);
									var record = cmb.getStore().getAt(index);
									if (record) {
										var p = Ext
												.getCmp('deductMoneyGoldWeight');
										var ac = Ext.num(
												Ext.getCmp('actualGoldWeight')
														.getValue(), 0);
										if (p) {
											p.setValue(Ext.num(
													record.data.unitPrice, 0)
													* ac);
										}
									}
								}
							}
						}
					}, {
						xtype : "textfield",
						fieldLabel : "丢失钻石备注",
						anchor : "-10",
						name : "lossDiamondRemark",
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "链尾损坏扣钱",
						anchor : "-10",
						name : "deductMoneyChain1",
						id : "deductMoneyChain1",
						allowBlank : false,
						blankText : "请输入链尾损坏扣钱",
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}]
		}, {
			xtype : "container",
			layout : "form",
			columnWidth : 0.25,
			items : [{
						xtype : "numberfield",
						fieldLabel : "折算金额",
						anchor : "0",
						name : "settlementPrice",
						allowBlank : false,
						blankText : "折算金额是必填项",
						id : "settlementPrice",
						readOnly : true
					}, {
						xtype : "numberfield",
						fieldLabel : "金重结算扣钱",
						anchor : "100%",
						name : "deductMoneyGoldWeight",
						id : "deductMoneyGoldWeight",
						allowBlank : false,
						blankText : "请输入金重结算扣钱",
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}, {
						xtype : "numberfield",
						fieldLabel : "丢失钻石扣钱",
						anchor : "100%",
						name : "deductMoneyDiamond",
						id : "deductMoneyDiamond",
						blankText : "请输入丢失钻石扣钱",
						allowBlank : false,
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}, {
						xtype : "numberfield",
						fieldLabel : "链扣损坏扣钱",
						anchor : "100%",
						name : "deductMoneyChain2",
						id : "deductMoneyChain2",
						allowBlank : false,
						blankText : "请输入链扣损坏扣钱",
						value : "0",
						readOnly : true,
						listeners : {
							change : function(textfield, newValue, oldValue) {
								Ext.getCmp('recycleWin').splitSum();
							}
						}
					}]
		}, {
			xtype : "container",
			layout : "form",
			columnWidth : 1,
			items : [{
						xtype : "textarea",
						fieldLabel : "备注",
						anchor : "100%",
						name : "remark",
						height : 60
					}]
		}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vRenovate',
							method : 'GET',
							params : {
								id : vid
							},
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result) {
									var form = win.form;
									if (form) {
										var data = result.root[0];
										data.productNum = data.oldProductNum;
										form.getForm().setValues(data);
										Ext.getCmp('goldClassId')
												.setValue(data.rightGoldClass);
										Ext
												.getCmp('goldClassId')
												.setRawValue(data.rightGoldClassText);
									}
								}
							},
							failure : function(resp) {
								Ext.Msg.alert('错误', resp.responseText);
							}
						});
			}
		}
	}
};

com.mzf.common.prodTransInfoWin = {
	xtype : "window",
	width : 650,
	height : 480,
	title : "查看详情",
	constrain : true,
	layout : "border",
	buttonAlign : "left",
	id : "transformWin",
	autoHeight : false,
	modal : true,
	stateful : false,
	resizable : true,
	bodyBorder : true,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "tbfill"
				}, {
					xtype : "button",
					text : "确定",
					ref : "../btnOk",
					listeners : {
						click : function(btn, evt) {
							var win = btn.refOwner;
							if (win) {
								win.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 85,
		labelAlign : "left",
		layout : "form",
		border : false,
		autoHeight : true,
		padding : "6",
		region : "north",
		height : 200,
		autoWidth : false,
		ref : "form",
		items : [{
			xtype : "container",
			anchor : "100%",
			layout : "column",
			items : [{
						xtype : "container",
						layout : "form",
						columnWidth : 0.5,
						items : [{
									xtype : "textfield",
									fieldLabel : "商品条码",
									anchor : "-10",
									name : "targetNum",
									readOnly : true
								}, {
									xtype : "textfield",
									fieldLabel : "调拨单号",
									anchor : "-10",
									name : "dispatchNum",
									readOnly : true
								}, {
									xtype : "textfield",
									fieldLabel : "调拨数量",
									anchor : "-10",
									name : "quantity",
									readOnly : true
								}, {
									xtype : "textfield",
									fieldLabel : "调出部门",
									anchor : "-10",
									name : "sourceOrgName",
									readOnly : true
								}, {
									xtype : "textfield",
									fieldLabel : "发起人",
									anchor : "-10",
									name : "cuserName",
									readOnly : true
								}, {
									xtype : "textfield",
									fieldLabel : "收货人",
									anchor : "-10",
									name : "ruserName",
									readOnly : true
								}, {
									xtype : "numberfield",
									fieldLabel : "成本价",
									anchor : "-10",
									name : "costPrice",
									readOnly : true,
									permissionId : "product.show.costPrice"
								}, {
									xtype : "numberfield",
									fieldLabel : "一口价",
									anchor : "-10",
									name : "retailBasePrice",
									readOnly : true,
									permissionId : "product.show.retailBasePrice"
								}]
					}, {
						xtype : "container",
						layout : "form",
						columnWidth : 0.5,
						items : [{
									xtype : "textfield",
									fieldLabel : "商品名称",
									anchor : "100%",
									name : "targetName",
									readOnly : true
								}, {
									xtype : "bizcodebox",
									anchor : "100%",
									fieldLabel : "回仓类型",
									editable : false,
									name : "transferType",
									readOnly : true,
									bizType : "transferType"
								}, {
									xtype : "textfield",
									fieldLabel : "确认数量",
									anchor : "100%",
									name : "actualQuantity",
									readOnly : true
								}, {
									xtype : "textfield",
									fieldLabel : "调入部门",
									anchor : "100%",
									name : "targetOrgName",
									readOnly : true
								}, {
									xtype : "datefield",
									anchor : "100%",
									fieldLabel : "发起时间",
									triggerClass : "x-form-search-trigger",
									name : "cdate",
									readOnly : true,
									format : "Y-m-d H:i:s"
								}, {
									xtype : "datefield",
									anchor : "100%",
									fieldLabel : "收货时间",
									triggerClass : "x-form-search-trigger",
									name : "rdate",
									readOnly : true,
									format : "Y-m-d H:i:s"
								}, {
									xtype : "numberfield",
									fieldLabel : "批发价",
									anchor : "100%",
									triggerClass : "x-form-search-trigger",
									name : "wholesalePrice",
									readOnly : true,
									format : "Y-m-d H:i:s",
									permissionId : "product.show.wholesalePrice"
								}, {
									xtype : "numberfield",
									fieldLabel : "促销一口价",
									anchor : "100%",
									triggerClass : "x-form-search-trigger",
									name : "promotionPrice",
									readOnly : true,
									format : "Y-m-d H:i:s",
									permissionId : "product.show.promotionPrice"
								}]
					}]
		}, {
			xtype : "textarea",
			fieldLabel : "备注",
			anchor : "100%",
			name : "remark",
			readOnly : true
		}]
	}, {
		xtype : "tabpanel",
		activeTab : 0,
		region : "center",
		width : 100,
		tabPosition : "bottom",
		border : false,
		cls : "topBorder",
		items : [{
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore1",
						url : "entity/vBizLog",
						root : "root",
						idProperty : "id",
						autoLoad : true,
						restful : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "transId",
									type : "integer",
									text : "流程ID"
								}, {
									name : "entityCode",
									type : "string",
									text : "实体Code"
								}, {
									name : "entityId",
									type : "integer",
									text : "实体ID"
								}, {
									name : "targetId",
									type : "integer",
									text : "对象ID"
								}, {
									name : "operate",
									type : "string",
									text : "操作内容"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}, {
									name : "cuserId",
									type : "uid",
									text : "操作人ID"
								}, {
									name : "cuserName",
									type : "uname",
									text : "操作人"
								}, {
									name : "cdate",
									type : "date",
									text : "操作时间",
									dateFormat : "time"
								}, {
									name : "billCode",
									type : "string",
									text : "表单Code"
								}, {
									name : "billId",
									type : "integer",
									text : "表单ID"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('transformWin');
								store.baseParams = {
									billId : win.orderId,
									billCode : 'transfer'
								};
							}
						}
					},
					autoHeight : false,
					region : "center",
					width : 100,
					border : false,
					title : "操作记录",
					autoExpandColumn : "remark",
					columns : [{
								header : "操作人",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "cuserName",
								id : "cuserName"
							}, {
								xtype : "datecolumn",
								header : "操作时间",
								sortable : true,
								resizable : true,
								width : 140,
								format : "Y-m-d H:i:s",
								menuDisabled : true,
								dataIndex : "cdate",
								id : "cdate"
							}, {
								header : "操作内容",
								sortable : false,
								resizable : true,
								width : 120,
								menuDisabled : true,
								dataIndex : "operate",
								id : "operate"
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark"
							}]
				}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
					url : 'entity/vTransfer?targetType=product',
					method : 'GET',
					params : {
						id : vid
					},
					success : function(resp) {
						var result = Ext.decode(resp.responseText);
						if (result) {
							var data = result.root[0];
							if (data) {
								Ext.Ajax.request({
									url : 'entity/vProduct',
									method : 'GET',
									params : {
										id : data.targetId
									},
									success : function(resp) {
										var result = Ext
												.decode(resp.responseText);
										if (result) {
											var prod = result.root[0];
											if (prod) {
												data.costPrice = prod.costPrice;
												data.wholesalePrice = prod.wholesalePrice;
												data.retailBasePrice = prod.retailBasePrice;
												data.promotionPrice = prod.promotionPrice;
												win.form.getForm()
														.setValues(data);
											}
										}
									},
									failure : function(resp) {
										Ext.Msg.alert('错误', resp.responseText);
									}
								});
							} else {
								Ext.Msg.alert('提示', '没有找到调拨单信息');
							}
						}
					},
					failure : function(resp) {
						Ext.Msg.alert('错误', resp.responseText);
					}
				});
			}
		}
	}
};

com.mzf.common.sendProdTransInfoWin = {
	xtype : "window",
	width : 650,
	height : 450,
	title : "查看详情",
	constrain : true,
	layout : "border",
	buttonAlign : "left",
	id : "transformWin",
	autoHeight : false,
	modal : true,
	stateful : false,
	resizable : true,
	bodyBorder : true,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "tbfill"
				}, {
					xtype : "button",
					text : "确定",
					ref : "../btnOk",
					listeners : {
						click : function(btn, evt) {
							var win = btn.refOwner;
							if (win) {
								win.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 85,
		labelAlign : "left",
		layout : "form",
		border : false,
		autoHeight : true,
		padding : "6",
		region : "north",
		height : 200,
		autoWidth : false,
		ref : "form",
		items : [{
					xtype : "container",
					anchor : "100%",
					layout : "column",
					items : [{
								xtype : "container",
								layout : "form",
								columnWidth : 0.5,
								items : [{
											xtype : "textfield",
											fieldLabel : "旧饰条码",
											anchor : "-10",
											name : "targetNum",
											readOnly : true
										}, {
											xtype : "hidden",
											fieldLabel : "调拨数量",
											anchor : "-10",
											name : "quantity",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "调出部门",
											anchor : "-10",
											name : "sourceOrgName",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "发起人",
											anchor : "-10",
											name : "cuserName",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "收货人",
											anchor : "-10",
											name : "ruserName",
											readOnly : true
										}]
							}, {
								xtype : "container",
								layout : "form",
								columnWidth : 0.5,
								items : [{
											xtype : "textfield",
											fieldLabel : "旧饰名称",
											anchor : "100%",
											name : "targetName",
											readOnly : true
										}, {
											xtype : "hidden",
											fieldLabel : "确认收货数量",
											anchor : "100%",
											name : "actualQuantity",
											readOnly : true
										}, {
											xtype : "textfield",
											fieldLabel : "调入部门",
											anchor : "100%",
											name : "targetOrgName",
											readOnly : true
										}, {
											xtype : "datefield",
											anchor : "100%",
											fieldLabel : "发起时间",
											triggerClass : "x-form-search-trigger",
											name : "cdate",
											readOnly : true,
											format : "Y-m-d H:i:s"
										}, {
											xtype : "datefield",
											anchor : "100%",
											fieldLabel : "收货时间",
											triggerClass : "x-form-search-trigger",
											name : "rdate",
											readOnly : true,
											format : "Y-m-d H:i:s"
										}]
							}]
				}, {
					xtype : "textarea",
					fieldLabel : "备注",
					anchor : "100%",
					name : "remark",
					readOnly : true
				}]
	}, {
		xtype : "tabpanel",
		activeTab : 0,
		region : "center",
		width : 100,
		tabPosition : "bottom",
		border : false,
		cls : "topBorder",
		items : [{
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore1",
						url : "entity/vBizLog",
						root : "root",
						idProperty : "id",
						autoLoad : true,
						restful : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "transId",
									type : "integer",
									text : "流程ID"
								}, {
									name : "entityCode",
									type : "string",
									text : "实体Code"
								}, {
									name : "entityId",
									type : "integer",
									text : "实体ID"
								}, {
									name : "targetId",
									type : "integer",
									text : "对象ID"
								}, {
									name : "operate",
									type : "string",
									text : "操作内容"
								}, {
									name : "remark",
									type : "string",
									text : "备注"
								}, {
									name : "cuserId",
									type : "uid",
									text : "操作人ID"
								}, {
									name : "cuserName",
									type : "uname",
									text : "操作人"
								}, {
									name : "cdate",
									type : "date",
									text : "操作时间",
									dateFormat : "time"
								}, {
									name : "billCode",
									type : "string",
									text : "表单Code"
								}, {
									name : "billId",
									type : "integer",
									text : "表单ID"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('transformWin');
								store.baseParams = {
									billId : win.orderId,
									billCode : 'transfer'
								};
							}
						}
					},
					autoHeight : false,
					region : "center",
					width : 100,
					border : false,
					title : "操作记录",
					autoExpandColumn : "remark",
					columns : [{
								header : "操作人",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "cuserName",
								id : "cuserName"
							}, {
								xtype : "datecolumn",
								header : "操作时间",
								sortable : true,
								resizable : true,
								width : 140,
								format : "Y-m-d H:i:s",
								menuDisabled : true,
								dataIndex : "cdate",
								id : "cdate"
							}, {
								header : "操作内容",
								sortable : false,
								resizable : true,
								width : 120,
								menuDisabled : true,
								dataIndex : "operate",
								id : "operate"
							}, {
								header : "备注",
								sortable : false,
								resizable : true,
								width : 100,
								menuDisabled : true,
								dataIndex : "remark",
								id : "remark"
							}]
				}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vTransfer?targetType=secondProduct',
							method : 'GET',
							params : {
								id : vid
							},
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								if (result) {
									var form = win.form;
									if (form) {
										var data = result.root[0];
										data.cdate
												&& (data.cdate = new Date(data.cdate)
														.format('Y-m-d H:i:s'));
										data.rdate
												&& (data.rdate = new Date(data.rdate)
														.format('Y-m-d H:i:s'));
										form.getForm().setValues(data);
									}
								}
							},
							failure : function(resp) {
								Ext.Msg.alert('错误', resp.responseText);
							}
						});
			}
		}
	}
};

com.mzf.common.saleDetailInfoWin = {
	xtype : "window",
	width : 795,
	height : 528,
	title : "查看详情",
	constrain : true,
	layout : "border",
	buttonAlign : "left",
	id : "saleInfoWin",
	modal : true,
	stateful : false,
	fbar : {
		xtype : "toolbar",
		items : [{
					xtype : "tbfill"
				}, {
					xtype : "button",
					text : "确定",
					ref : "../btnCancel",
					listeners : {
						click : function(btn, evt) {
							var win = btn.refOwner;
							if (win) {
								win.close();
							}
						}
					}
				}]
	},
	items : [{
		xtype : "form",
		labelWidth : 65,
		labelAlign : "left",
		layout : "form",
		border : false,
		padding : "6",
		autoHeight : true,
		region : "north",
		items : [{
			xtype : "container",
			anchor : "100%",
			layout : "column",
			items : [{
						xtype : "container",
						layout : "form",
						columnWidth : 0.25,
						items : [{
									xtype : "textfield",
									fieldLabel : "销售门店",
									anchor : "-10",
									readOnly : true,
									name : "orgName"
								}, {
									xtype : "textfield",
									fieldLabel : "客户姓名",
									anchor : "-10",
									readOnly : true,
									name : "cusName",
									bizType : "cusGrade"
								}, {
									xtype : "textfield",
									fieldLabel : "历史积分",
									anchor : "-10",
									readOnly : true,
									name : "historyPoints"
								}]
					}, {
						xtype : "container",
						layout : "form",
						columnWidth : 0.25,
						items : [{
									xtype : "textfield",
									fieldLabel : "销售顾问",
									anchor : "-10",
									displayField : "name",
									mode : "remote",
									valueField : "url",
									triggerAction : "all",
									editable : false,
									readOnly : true,
									name : "employeeName",
									resizable : false,
									id : "empId"
								}, {
									xtype : "textfield",
									fieldLabel : "会员卡号",
									anchor : "-10",
									triggerClass : "x-form-search-trigger",
									editable : false,
									readOnly : true,
									name : "cardNo",
									listeners : {
										triggerclick : function() {
											var win = od.AppInstance.active
													.createComponent('queryCustWin');
											if (win) {
												win.show();
											}
										}
									}
								}, {
									xtype : "textfield",
									fieldLabel : "积分抵",
									anchor : "-10",
									name : "exchangePoints",
									readOnly : true
								}]
					}, {
						xtype : "container",
						columnWidth : 0.25,
						layout : "form",
						labelWidth : 80,
						items : [{
									xtype : "textfield",
									fieldLabel : "辅助珠宝顾问",
									anchor : "-10",
									displayField : "name",
									mode : "remote",
									valueField : "url",
									triggerAction : "all",
									editable : false,
									readOnly : true,
									name : "assistantEmployeeName",
									resizable : false,
									id : "assistantEmployeeId"
								}, {
									xtype : "bizcodebox",
									anchor : "-10",
									fieldLabel : "会员级别",
									editable : false,
									name : "grade",
									readOnly : true,
									bizType : "cusGrade"
								}]
					}, {
						xtype : "container",
						columnWidth : 0.25,
						layout : "form",
						items : [{
									xtype : "textfield",
									fieldLabel : "验货人",
									anchor : "-10",
									name : "checkerName",
									readOnly : true
								}, {
									xtype : "textfield",
									fieldLabel : "手机",
									anchor : "-10",
									triggerClass : "x-form-search-trigger",
									editable : false,
									readOnly : true,
									name : "mobile",
									listeners : {
										triggerclick : function() {
											var win = od.AppInstance.active
													.createComponent('queryCustWin');
											if (win) {
												win.show();
											}
										}
									}
								}]
					}]
		}, {
			xtype : "container",
			anchor : "100%",
			layout : "column",
			padding : "6",
			style : "margin-top:6px",
			items : [{
				xtype : "fieldset",
				layout : "form",
				padding : "6",
				columnWidth : 0.35,
				style : "margin-right:6px;background-color:ffffee;padding-bottom:0;",
				items : [{
							xtype : "textfield",
							fieldLabel : "预收订金",
							anchor : "-10",
							name : "frontMoney",
							readOnly : true
						}, {
							xtype : "textfield",
							fieldLabel : "商场代收",
							anchor : "-10",
							name : "marketProxy",
							readOnly : true
						}, {
							xtype : "textfield",
							fieldLabel : "旧饰支付",
							anchor : "-10",
							name : "productPay",
							readOnly : true
						}, {
							xtype : "textfield",
							fieldLabel : "旧金支付",
							anchor : "-10",
							name : "goldPay",
							readOnly : true
						}]
			}, {
				xtype : "fieldset",
				layout : "column",
				columnWidth : 0.65,
				padding : "6",
				style : "background-color:#eeffee;padding-bottom:0;",
				items : [{
							xtype : "container",
							columnWidth : 0.5,
							layout : "form",
							labelWidth : 75,
							items : [{
										xtype : "textfield",
										fieldLabel : "银行卡支付",
										anchor : "-10",
										name : "bankCard",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "联名卡支付",
										anchor : "-10",
										name : "coBrandedCard",
										readOnly : true,
										hidden : true,
										hideLabel : true
									}, {
										xtype : "textfield",
										fieldLabel : "现金支付",
										anchor : "-10",
										name : "cash",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "转账支付",
										anchor : "-10",
										name : "transfer",
										readOnly : true
									}]
						}, {
							xtype : "container",
							columnWidth : 0.5,
							layout : "form",
							labelWidth : 75,
							items : [{
										xtype : "textfield",
										fieldLabel : "储值卡支付",
										anchor : "-10",
										editable : false,
										readOnly : true,
										name : "valueCard",
										bizType : "bankType"
									}, {
										xtype : "bizcodebox",
										anchor : "-10",
										fieldLabel : "外卡支付",
										editable : false,
										readOnly : true,
										name : "foreignCard",
										bizType : "bankType",
										hidden : true,
										hideLabel : true
									}, {
										xtype : "textfield",
										fieldLabel : "代金券支付",
										anchor : "-10",
										name : "chit",
										readOnly : true
									}, {
										xtype : "textfield",
										fieldLabel : "其它",
										anchor : "-10",
										name : "other",
										readOnly : true
									}]
						}]
			}, {
				xtype : "fieldset",
				layout : "column",
				columnWidth : 1,
				padding : "6 6 0 6",
				style : "margin:6 0 6 0;background-color:#ffeeee;padding-bottom:0;",
				items : [{
							xtype : "container",
							columnWidth : 0.33,
							layout : "form",
							labelWidth : 65,
							items : [{
										xtype : "textfield",
										fieldLabel : "应收金额",
										anchor : "-10",
										name : "totalAmount",
										readOnly : true
									}]
						}, {
							xtype : "container",
							columnWidth : 0.33,
							layout : "form",
							labelWidth : 65,
							items : [{
										xtype : "textfield",
										fieldLabel : "实收金额",
										anchor : "-10",
										name : "amount",
										readOnly : true
									}]
						}, {
							xtype : "container",
							columnWidth : 0.33,
							layout : "form",
							labelWidth : 65,
							items : [{
										xtype : "textfield",
										fieldLabel : "折扣金额",
										anchor : "100%",
										name : "discount",
										readOnly : true
									}]
						}]
			}]
		}, {
			xtype : "textarea",
			fieldLabel : "备注",
			anchor : "100%",
			name : "remark",
			readOnly : true
		}]
	}, {
		xtype : "tabpanel",
		activeTab : 0,
		tabPosition : "bottom",
		region : "center",
		width : 100,
		border : false,
		cls : "topBorder",
		items : [{
					xtype : "grid",
					store : {
						xtype : "jsonstore",
						storeId : "MyStore5",
						autoDestory : true,
						url : "entity/saleDetail",
						requestMethod : "GET",
						root : "root",
						restful : true,
						idProperty : "id",
						autoLoad : true,
						fields : [{
									name : "id",
									type : "integer",
									text : "主键"
								}, {
									name : "saleId",
									type : "integer",
									text : "销售单ID"
								}, {
									name : "type",
									type : "string",
									text : "销售类型"
								}, {
									name : "targetId",
									type : "integer",
									text : "货品ID"
								}, {
									name : "targetNum",
									type : "string",
									text : "商品条码"
								}, {
									name : "targetName",
									type : "string",
									text : "商品名称"
								}, {
									name : "ptype",
									type : "string",
									text : "商品类型"
								}, {
									name : "pkind",
									type : "string",
									text : "商品种类"
								}, {
									name : "goldClass",
									type : "integer",
									text : "金料成色"
								}, {
									name : "quantity",
									type : "string",
									text : "数量"
								}, {
									name : "goldWeight",
									type : "string",
									text : "金重"
								}, {
									name : "diamondSize",
									type : "string",
									text : "石重"
								}, {
									name : "totalProcessingCharges",
									type : "string",
									text : "总工费"
								}, {
									name : "price",
									type : "string",
									text : "商品售价"
								}, {
									name : "totalDiscount",
									type : "string",
									text : "折扣"
								}, {
									name : "authorityDiscount",
									type : "string",
									text : "权限折扣"
								}, {
									name : "saleDiscount",
									type : "string",
									text : "促销折扣"
								}, {
									name : "orderId",
									type : "integer",
									text : "客订单ID"
								}, {
									name : "orderCode",
									type : "string",
									text : "客订单号"
								}, {
									name : "frontMoney",
									type : "string",
									text : "预收定金"
								}, {
									name : "onsaleType",
									type : "string",
									text : "促销类型"
								}, {
									name : "points",
									type : "string",
									text : "积分"
								}, {
									name : "exchangePoints",
									type : "string",
									text : "消费积分"
								}],
						listeners : {
							beforeload : function(store, options) {
								var win = Ext.getCmp('saleInfoWin');
								store.setBaseParam('saleId', win.saleId);
							}
						}
					},
					clickToEdit : 1,
					clicksToEdit : 1,
					title : "商品记录",
					id : "saleProdDetailGrid",
					height : 110,
					columns : [{
								xtype : "bizcodecolumn",
								header : "销售类型",
								sortable : true,
								resizable : true,
								width : 70,
								menuDisabled : true,
								dataIndex : "type",
								id : "type",
								bizType : "saleDetailType"
							}, {
								header : "商品条码",
								sortable : false,
								resizable : true,
								width : 90,
								menuDisabled : true,
								dataIndex : "targetNum"
							}, {
								header : "商品名称",
								sortable : false,
								resizable : true,
								width : 80,
								menuDisabled : true,
								dataIndex : "targetName"
							}, {
								header : "客订单号",
								sortable : false,
								resizable : true,
								width : 90,
								menuDisabled : true,
								dataIndex : "orderCode",
								id : "orderCode"
							}, {
								header : "数量",
								sortable : false,
								resizable : true,
								width : 50,
								menuDisabled : true,
								dataIndex : "quantity",
								id : "quantity",
								align : "right"
							}, {
								header : "金重",
								sortable : false,
								resizable : true,
								width : 50,
								menuDisabled : true,
								dataIndex : "goldWeight",
								align : "right"
							}, {
								header : "石重",
								sortable : false,
								resizable : true,
								width : 50,
								menuDisabled : true,
								dataIndex : "diamondSize",
								align : "right"
							}, {
								xtype : "numbercolumn",
								header : " 总工费",
								sortable : true,
								resizable : true,
								width : 65,
								format : "0,000.00",
								menuDisabled : true,
								dataIndex : "totalProcessingCharges",
								id : "cost",
								align : "right"
							}, {
								xtype : "numbercolumn",
								header : "一口价",
								sortable : true,
								resizable : true,
								width : 70,
								format : "0,000.00",
								menuDisabled : true,
								dataIndex : "price",
								id : "price",
								align : "right"
							}, {
								xtype : "numbercolumn",
								header : "折扣",
								sortable : true,
								resizable : true,
								width : 55,
								format : "0,000.00",
								menuDisabled : true,
								dataIndex : "totalDiscount",
								id : "totalDiscount",
								align : "right"
							}, {
								xtype : "numbercolumn",
								header : "预收定金",
								sortable : true,
								resizable : true,
								width : 75,
								format : "0,000.00",
								menuDisabled : true,
								dataIndex : "frontMoney",
								id : "frontMoney",
								align : "right"
							}, {
								xtype : "bizcodecolumn",
								header : "促销类型",
								sortable : true,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "onsaleType",
								id : "onsaleType",
								bizType : "onsaleType"
							}, {
								header : "积分",
								sortable : false,
								resizable : true,
								width : 60,
								menuDisabled : true,
								dataIndex : "points",
								id : "points",
								align : "right"
							}, {
								header : "积分抵",
								sortable : false,
								resizable : true,
								width : 75,
								menuDisabled : true,
								dataIndex : "exchangePoints",
								id : "exchangePoints",
								align : "right"
							}]
				}]
	}],
	listeners : {
		show : function(win) {
			win.preview = function(vid) {
				Ext.Ajax.request({
							url : 'entity/vSale',
							method : 'GET',
							params : {
								id : vid
							},
							success : function(resp) {
								var result = Ext.decode(resp.responseText);
								var data = result.root[0];
								if (data) {
									win.setTitle(win.title + "(" + data.num
											+ ")");
									var form = win.get(0);
									if (form) {
										form.getForm().setValues(data);
									}
								}
							},
							failure : function(resp) {
								Ext.Msg.alert('错误', resp.responseText);
							}
						});
			}
		}
	}
}; 
com.mzf.common.demandDetailWin = {
    xtype: "window",
    width: 660,
    height: 610,
    title: "查看详情",
    constrain: true,
    layout: "fit",
    autoHeight: true,
    modal: true,
    stateful: false,
    id: "demandDetailWin",
    resizable: false,
    buttonAlign: "left",
    fbar: {
        xtype: "toolbar",
        items: [{
            xtype: "button",
            text: "查看客订单",
            ref: "../lookOrder",
            hidden: true,
            listeners: {
                click: function(btn, evt) {
                    var id = Ext.getCmp('orderId').getValue();
                    var win = od.showWindow('orderDetailWin');
                    win.preview(id);
                }
            }
        },{
            xtype: "button",
            text: "配货款式详情",
            ref: "../infactDedail",
            hidden: true,
            listeners: {
                click: function(btn, evt) {
                    var data = btn.refOwner.detailData;
                    if (data) {
                        var win = od.showWindow('allocateDetailWin');
                        win.preview(data);
                    }
                }
            }
        },{
            xtype: "tbfill"
        },{
            xtype: "button",
            text: "确定",
            ref: "../btnCancel",
            listeners: {
                click: function(btn, evt) {
                    if (this.refOwner) {
                        this.refOwner.close();
                    }
                }
            }
        }]
    },
    items: [{
        xtype: "form",
        labelWidth: 80,
        labelAlign: "left",
        layout: "form",
        border: false,
        autoHeight: true,
        padding: "6",
        region: "center",
        ref: "form",
        items: [{
            xtype: "container",
            anchor: "100%",
            layout: "column",
            autoHeight: true,
            items: [{
                xtype: "container",
                columnWidth: 0.5,
                layout: "form",
                labelWidth: 75,
                autoHeight: true,
                items: [{
                    xtype: "bizcodebox",
                    anchor: "-10",
                    fieldLabel: "商品类型",
                    editable: false,
                    bizType: "productType",
                    name: "ptype",
                    readOnly: true
                },{
                    xtype: "bizcodebox",
                    anchor: "-10",
                    fieldLabel: "商品种类",
                    editable: false,
                    bizType: "productKind",
                    name: "pkind",
                    treeHeight: 160,
                    readOnly: true
                }]
            },{
                xtype: "container",
                columnWidth: 0.5,
                layout: "form",
                autoHeight: true,
                items: [{
                    xtype: "trigger",
                    anchor: "0",
                    fieldLabel: "客订单号",
                    triggerClass: "x-form-search-trigger",
                    name: "orderNum",
                    editable: false,
                    readOnly: true
                },{
                    xtype: "datefield",
                    anchor: "0",
                    fieldLabel: "客订取货时间",
                    format: "Y-m-d",
                    name: "deliveryDate",
                    allowBlank: true,
                    readOnly: true,
                    emptyText: " "
                },{
                    xtype: "hidden",
                    fieldLabel: "Label",
                    anchor: "100%",
                    name: "orderId",
                    id: "orderId"
                },{
                    xtype: "hidden",
                    fieldLabel: "要货数量",
                    anchor: "0",
                    name: "expectCount",
                    readOnly: true
                }]
            }]
        },{
            xtype: "container",
            anchor: "100%",
            layout: "column",
            autoHeight: true,
            items: [{
                xtype: "container",
                layout: "column",
                padding: "6",
                columnWidth: 1,
                autoHeight: true,
                items: [{
                    xtype: "container",
                    layout: "form",
                    columnWidth: 1,
                    labelWidth: 75,
                    items: [{
                        xtype: "trigger",
                        anchor: "-10",
                        fieldLabel: "要货MZF款号",
                        triggerClass: "x-form-search-trigger",
                        name: "styleCode",
                        editable: false,
                        readOnly: true,
                        listeners: {
                            triggerclick: function() {
                                var grid = Ext.getCmp('demandBillGrid');
                                if (grid) {
                                    grid.setOrderStyle = function(record) {
                                        var win = Ext.getCmp('demandBillWin');
                                        if (win) {
                                            var form = win.get(0);
                                            if (form) {
                                                form.getForm().setValues({
                                                    styleCode: record.get('code'),
                                                    imageId: record.get('imageId')
                                                });
                                            }
                                            var img = Ext.get('imgView2');
                                            if (img && record.get('imageId')) {
                                                img.dom.src = 'image/' + record.get('imageId');

                                            }


                                        }

                                    }

                                }


                                od.showWindow('queryStyleWin');
                            }
                        }
                    }]
                },{
                    xtype: "container",
                    columnWidth: 0.52,
                    layout: "form",
                    labelWidth: 75,
                    autoHeight: true,
                    items: [{
                        xtype: "numberfield",
                        fieldLabel: "主石重量",
                        anchor: "-10",
                        name: "weight1",
                        readOnly: true
                    },{
                        xtype: "numberfield",
                        fieldLabel: "商品尺寸",
                        anchor: "-10",
                        name: "size1",
                        readOnly: true
                    },{
                        xtype: "numberfield",
                        fieldLabel: "商品价格",
                        anchor: "-10",
                        name: "price1",
                        readOnly: true
                    },{
                        xtype: "numberfield",
                        fieldLabel: "金料重量",
                        anchor: "-10",
                        name: "goldWeight1",
                        readOnly: true
                    }]
                },{
                    xtype: "container",
                    columnWidth: 0.48,
                    layout: "form",
                    labelWidth: 55,
                    autoHeight: true,
                    items: [{
                        xtype: "numberfield",
                        fieldLabel: "至",
                        anchor: "-10",
                        name: "weight2",
                        readOnly: true
                    },{
                        xtype: "numberfield",
                        fieldLabel: "至",
                        anchor: "-10",
                        name: "size2",
                        readOnly: true
                    },{
                        xtype: "numberfield",
                        fieldLabel: "至",
                        anchor: "-10",
                        name: "price2",
                        readOnly: true
                    },{
                        xtype: "numberfield",
                        fieldLabel: "至",
                        anchor: "-10",
                        name: "goldWeight2",
                        readOnly: true
                    }]
                },{
                    xtype: "container",
                    columnWidth: 1,
                    layout: "form",
                    labelWidth: 75,
                    autoHeight: true,
                    items: [{
                        xtype: "container",
                        anchor: "100%",
                        layout: "column",
                        items: [{
                            xtype: "container",
                            columnWidth: 0.52,
                            layout: "form",
                            items: [{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "金料成色",
                                editable: false,
                                name: "goldClass",
                                bizType: "goldClass",
                                clearable: true,
                                readOnly: true
                            },{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "主石形状",
                                editable: false,
                                name: "shape",
                                bizType: "diamondShape",
                                clearable: true,
                                readOnly: true
                            }]
                        },{
                            xtype: "container",
                            columnWidth: 0.48,
                            layout: "form",
                            labelWidth: 55,
                            items: [{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "K金颜色",
                                editable: false,
                                name: "kgoldColor",
                                bizType: "kGoldColor",
                                clearable: true,
                                readOnly: true
                            },{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "主石切工",
                                editable: false,
                                name: "cut",
                                bizType: "diamondCut",
                                clearable: true,
                                readOnly: true
                            }]
                        }]
                    },{
                        xtype: "container",
                        anchor: "100%",
                        layout: "column",
                        items: [{
                            xtype: "container",
                            columnWidth: 0.52,
                            layout: "form",
                            items: [{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "主石颜色",
                                editable: false,
                                name: "color1",
                                bizType: "diamondColor",
                                clearable: true,
                                readOnly: true
                            },{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "主石净度",
                                editable: false,
                                name: "clean1",
                                bizType: "diamondClean",
                                clearable: true,
                                readOnly: true
                            },{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "抛光性",
                                editable: false,
                                name: "polishing",
                                bizType: "polishing",
                                clearable: true,
                                readOnly: true
                            }]
                        },{
                            xtype: "container",
                            layout: "form",
                            columnWidth: 0.48,
                            labelWidth: 55,
                            items: [{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "至",
                                editable: false,
                                bizType: "diamondColor",
                                readOnly: true,
                                name: "color2"
                            },{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "至",
                                editable: false,
                                bizType: "diamondClean",
                                readOnly: true,
                                name: "clean2"
                            },{
                                xtype: "bizcodebox",
                                anchor: "-10",
                                fieldLabel: "对称性",
                                editable: false,
                                bizType: "symmetry",
                                readOnly: true,
                                name: "symmetry",
                                clearable: false
                            }]
                        }]
                    },{
                        xtype: "bizcodebox",
                        anchor: "-10",
                        fieldLabel: "镶嵌方式",
                        editable: false,
                        bizType: "insetType",
                        name: "inset",
                        clearable: true,
                        treeHeight: 200,
                        readOnly: true
                    }]
                }]
            },{
                xtype: "container",
                width: 280,
                layout: "form",
                autoHeight: false,
                items: [{
                    xtype: "fieldset",
                    layout: "column",
                    html: "<img id='imgView2' height='255' width='285' src='images/noImage.jpg'>",
                    style: "padding:0px;",
                    autoHeight: false,
                    height: 250
                },{
                    xtype: "hidden",
                    fieldLabel: "Label",
                    anchor: "100%",
                    name: "imageId"
                },{
                    xtype: "bizcodebox",
                    anchor: "100%",
                    fieldLabel: "制作工艺",
                    editable: false,
                    name: "craft",
                    checkable: true,
                    bizType: "craft",
                    readOnly: true
                }]
            }]
        },{
            xtype: "container",
            anchor: "100%",
            layout: "form",
            labelWidth: 75,
            autoHeight: true,
            items: [{
                xtype: "textarea",
                fieldLabel: "备注",
                anchor: "0",
                name: "remark",
                readOnly: true
            }]
        }]
    },{
        xtype: "tabpanel",
        activeTab: 0,
        region: "south",
        height: 130,
        border: false,
        tabPosition: "bottom",
        cls: "topBorder",
        items: [{
            xtype: "grid",
            store: {
                xtype: "jsonstore",
                storeId: "MyStore4",
                url: "entity/vBizLog",
                requestMethod: "GET",
                root: "root",
                idProperty: "id",
                autoLoad: false,
                restful: true,
                fields: [
                    {
                    name: "id",
                    type: "integer",
                    text: "主键"
                },
                    {
                    name: "transId",
                    type: "integer",
                    text: "流程ID"
                },
                    {
                    name: "entityCode",
                    type: "string",
                    text: "实体Code"
                },
                    {
                    name: "entityId",
                    type: "integer",
                    text: "实体ID"
                },
                    {
                    name: "targetId",
                    type: "integer",
                    text: "对象ID"
                },
                    {
                    name: "operate",
                    type: "string",
                    text: "操作内容"
                },
                    {
                    name: "remark",
                    type: "string",
                    text: "备注"
                },
                    {
                    name: "cuserId",
                    type: "uid",
                    text: "操作人ID"
                },
                    {
                    name: "cuserName",
                    type: "uname",
                    text: "操作人"
                },
                    {
                    name: "cdate",
                    type: "date",
                    text: "操作时间",
                    dateFormat: "time"
                },
                    {
                    name: "billCode",
                    type: "string",
                    text: "表单Code"
                },
                    {
                    name: "billId",
                    type: "integer",
                    text: "表单ID"
                }
                    ]
            },
            title: "要货处理记录",
            id: "demandProcGrid",
            autoExpandColumn: "remark",
            ref: "../logGrid",
            columns: [{
                header: "操作人",
                sortable: false,
                resizable: true,
                width: 100,
                menuDisabled: true,
                dataIndex: "cuserName",
                id: "cuserName"
            },{
                xtype: "datecolumn",
                header: "操作时间",
                sortable: true,
                resizable: true,
                width: 150,
                format: "Y-m-d H:i:s",
                menuDisabled: true,
                dataIndex: "cdate",
                id: "cdate"
            },{
                header: "操作内容",
                sortable: false,
                resizable: true,
                width: 200,
                menuDisabled: true,
                dataIndex: "operate",
                id: "operate"
            },{
                header: "备注",
                sortable: false,
                resizable: true,
                width: 100,
                menuDisabled: true,
                dataIndex: "remark",
                id: "remark"
            }]
        }]
    }],
    listeners: {
        show: function(win) {
            win.preview = function(vid) {
                if (vid) {
                    Ext.Ajax.request({
                        url: 'entity/vDemand/' + vid,
                        method: 'GET',
                        success: function(resp) {
                            var data = Ext.decode(resp.responseText);
                            if (data) {
                                if (data.orderId) {
                                    win.lookOrder.show();
                                }
                                if (data.allocateStyleId) {
                                    win.infactDedail.show();
                                }
                                var form = win.form.getForm();
                                form.setValues(data);
                                win.detailData = data;
                                win.setTitle(win.title+ "(" + data.num + ")");
                                if (data.imageId) {
                                    var img = Ext.get('imgView2');
                                    img.dom.src = 'image/' + data.imageId;
                                }

                                var grid = Ext.getCmp('demandProcGrid');
                                grid.getStore().baseParams = {
                                    billId: vid,
                                    billCode: 'demand'
                                };
                                grid.getStore().load();
                            }
                        }
                    });
                }
            };
        }
    }
};
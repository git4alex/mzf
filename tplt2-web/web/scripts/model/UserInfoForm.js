// 用户表单和密码修改表单 
UserInfoForm = Ext.extend(Ext.form.FormPanel, {
	upLoadFileWin : null, // 照片上传窗体
	border : false,
	bodyStyle : 'padding:6px 20px 6px 10px;',
	initComponent : function() {
		// 照片上传窗体
		this.upLoadFileWin = new UpLoadFileWin();
		Ext.apply(this, {
			border : false, // 不要边框
			fileUpload : true, // 允许上传
			autoScroll : true,
			items : [{
				xtype : "panel",
				border : false,
				items : [{
					layout : "column",
					border : false,
					items : [{
								columnWidth : .33,
								labelWidth : 55,
								border : false,
								layout : "form",
								defaults : {
									xtype : "textfield",
									anchor : "90%"
								},
								items : [{
											name : "id",
											xtype : 'hidden'
										}, {
											fieldLabel : "用户名称",
											name : "userName"
										}, {
											fieldLabel : "登录账号",
											name : "loginName"
										}, {
											id : 'password',
											inputType : 'password',
											fieldLabel : "登录密码",
											name : "password",
											allowBlank : false
										}, {
											id : 'password2',
											inputType : 'password',
											fieldLabel : "重复密码",
											vtype : 'confirmPwd',
											confirmPwd : {
												first : 'password',
												second : 'password2'
											},
											allowBlank : false
										}, {
											xtype : "combo", // 下拉列表框
											fieldLabel : "状态",
											emptyText : "请选择状态",
											triggerAction : "all", // 显示所有数据
											hiddenName : "state",
											displayField : 'value',
											valueField : 'code',
											editable : false,
											mode : 'local',
											value : 1,
											store : {
												xtype : 'jsonstore',
												fields : ['code', 'value'],
												data : [{
															code : 1,
															value : '启用'
														}, {
															code : 0,
															value : '禁止'
														}]
											}
										}, {
											fieldLabel : "所属机构",
											name : "orgId"
										}]
							}, {
								columnWidth : .33,
								labelWidth : 55,
								border : false,
								layout : "form",
								defaults : {
									xtype : "textfield",
									anchor : "90%"
								},
								items : [{
											xtype : "numberfield", // 数字框
											fieldLabel : "年龄",
											name : "age"
										}, {
											xtype : "datefield", // 日期框
											fieldLabel : "出生年月", // label
											format : "Y-m-d",
											name : "birthday"
											// emptyText : "请选择出生年月" // 为空显示信息
									}	, {
											fieldLabel : "手机号码",
											name : "phone"
										}, {
											fieldLabel : "电子邮件",
											name : "email"
										}, {
											fieldLabel : "证件号",
											name : "idCard"
										}, {
											fieldLabel : "地址",
											name : "address"
										}]
							}, {

								layout : "form",
								columnWidth : .33,
								labelWidth : 55,
								border : false,
								defaults : {
									xtype : "textfield"
								},
								items : [{
											fieldLabel : "照片",
											inputType : "image",
											allowBlank : true,
											width : 120,
											height : 150,
											name : "photoName",

											autoCreate : {
												tag : "input",
												type : "image",
												src : "images/default_person.gif"
											}
										}, {
											xtype : "button",
											text : "上传照片",
											style : "margin-left:65px;",
											handler : this.onMyUpLoadClick,
											scope : this
										}]

							}]
				}]
			}]
		});

		UserInfoForm.superclass.initComponent.call(this);

		/**
		 * 监听upLoadFileWin的onUploadSuccess事件(图片上传成功事件)
		 */
		this.upLoadFileWin.on("onUploadSuccess", this.onUploadSuccess, this);
	},

	/**
	 * upLoadFileWin的onUploadSuccess事件(图片上传成功事件)
	 * 
	 * @param {}
	 *            _form
	 * @param {}
	 *            _action
	 */
	onUploadSuccess : function(response) {
		// 获取服务器随机生成的图片的名称

		// 获取服务器随机生成的图片的路径

		// 提示信息

		// 修改图片路径

		// 获取隐藏表单域的引用

		// 设置hidden的数值

		// 隐藏窗体
		this.upLoadFileWin.hide();
	},

	/**
	 * 上传图片按钮单击事件
	 */
	onMyUpLoadClick : function() {
		this.upLoadFileWin.show();
	}

});


PwdForm = Ext.extend(Ext.form.FormPanel, {
			autoHeight : false,
			buttonAlign : 'right',
			frame : false,
			border : false,
			autoWidth : true, 
			bodyStyle : 'padding:6px 20px 6px 10px;',
			labelWidth : 60,
			defaults : {
				anchor:'-30',
				xtype : 'textfield'
			},
			layoutConfig : {
				columns : 1
			},
			initComponent : function() {
				this.items = [{
							fieldLabel : '用户名',
							name : 'userName',
							allowBlank : false
						}, {
							name : 'id',
							xtype : 'hidden'
						}, {
							id : 'password',
							fieldLabel : '新密码',
							name : 'password',
							inputType : 'password',
							allowBlank : false
						}, {
							id : 'password2',
							fieldLabel : '重输密码',
							inputType : 'password',
							vtype : 'confirmPwd',
							confirmPwd : {
								first : 'password',
								second : 'password2'
							}
						}];
				PwdForm.superclass.initComponent.call(this);
			}
		});
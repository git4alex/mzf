UpLoadFileWin = Ext.extend(Ext.Window, {
			// 图片上传表单
			upLoadFormPanel : null,
			submitUrl : '',
			resizable : false,
			initComponent : function() {
				// 图片上传表单
				this.upLoadFormPanel = new Ext.form.FormPanel({
							fileUpload : true, // 允许上传文件
							labelWidth : 55,
							layout : "form",
							baseCls : "x-plain", // 统一背景色
							bodyStyle : "padding:10px;",
							items : [{
										xtype : 'fileuploadfield',
										name : "photo",
										allowBlank : false,
										fieldLabel : '上传照片',
										anchor : "95%",
										buttonCfg : {
											text : '选择'
										}
									}]
						});

				Ext.apply(this, {
							title : "照片上传",
							width : 400,
							height : 120,
							plain : true,
							modal : true, // 模态窗体
							closeAction : "hide",
							items : [this.upLoadFormPanel],
							listeners : {
								"beforeshow" : this.onBeforeshow,
								scope : this
							},
							buttons : [{
										text : "上传",
										handler : this.onUpLoad,
										scope : this
									}, {
										text : "取消",
										handler : this.onCloseWin,
										scope : this
									}]
						});
				UpLoadFileWin.superclass.initComponent.call(this);

				// 为当前组件添加自定义事件
				this.addEvents("onUploadSuccess");
			},

			/**
			 * 上传按钮单击事件
			 */
			onUpLoad : function() {
				// 如果表单验证通过,则提交表单
				if (this.upLoadFormPanel.getForm().isValid() == true) {
					this.upLoadFormPanel.getForm().submit({
								url : this.submitUrl,
								waitTitle : "数据传输",
								waitMsg : "数据传输中,请稍候......",
								success : this.onSuccess,
								failure : this.onFailure,
								scope : this
							});
				}
			},

			/**
			 * 上传成功回调函数
			 * 
			 * @param {}
			 *            _form
			 * @param {}
			 *            _action
			 */
			onSuccess : function(response) {
				this.fireEvent("onUploadSuccess", response);
			},

			/**
			 * 上传失败回调函数
			 * 
			 * @param {}
			 *            _form
			 * @param {}
			 *            _action
			 */
			onFailure : function(response) {
				result = Ext.decode(response.responseText)
				Ext.Msg.alert("系统消息", result.msg || "");
			},

			/**
			 * 取消按钮单击事件
			 */
			onCloseWin : function() {
				this.upLoadFormPanel.getForm().reset();
				this.hide();
			},

			/**
			 * 窗体在显示前的事件
			 */
			onBeforeshow : function() {
				this.upLoadFormPanel.getForm().reset();
			}
		});
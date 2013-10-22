Ext.QuickTips.init();
Ext.form.Field.prototype.msgTarget = 'side';// under

Ext.onReady(function() {
	var login = function() {
		var form = loginForm.getForm();

		if (form.isValid()) {
			form.submit();
		}
	};

	var loginForm = new Ext.form.FormPanel( {
		width : 600,
		labelWidth : 40,
		autoHeight:true,
		standardSubmit : true,
		frame : true,
		title : '茗钻坊企业运营管理系统',
		layout : 'column',
		renderTo : Ext.getBody(),
		url:'/login',
		items : [ {
			columnWidth : 1,
			height:200,
			bodyStyle:"border:1px solid #ddbba4;",
			style:"background-image: url('../images/login.jpg');margin-bottom:24px;"
		},{
			layout : 'form',
			columnWidth : .5,
			autoHeight:true,
			items : [ {
				id : 'userName',
				fieldLabel : '用户名',
				name : 'j_username',
				allowBlank : false,
				xtype:'textfield',
				anchor:'-20',
				blankText : '用户名不能为空'
			}]
		}, {
			layout : 'form',
			columnWidth : .5,
			autoHeight:true,
			items : [ {
				id : 'password',
				fieldLabel : '密&nbsp;&nbsp;&nbsp;码',
				name : 'j_password',
				inputType : 'password',
				allowBlank : false,
				xtype:'textfield',
				anchor:'-20',
				blankText : '密码不能为空'
			} ]
		} ],
		keys : [ {
			key : Ext.EventObject.ENTER,
			fn : login
		} ],
		buttons : [ {
			text : '登录',
			handler : login
		} ]
	});
	
	loginForm.getEl().center();

	if (window.location.href.indexOf('user_psw_error') != -1) {
		Ext.getCmp('msg').show();
	}

	var title = window.document.getElementsByTagName("title");
	if (title) {
		title[0].innerText = '茗钻坊企业运营管理系统';
	}
	Ext.getCmp('userName').focus();
});
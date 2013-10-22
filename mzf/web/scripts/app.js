//LogoutModule=Ext.extend(od.Module,{
//	moduleId:'logout',
//	moduleName:'Logout',
//	components:[],
//	createDefaultComponent:function(){
//		window.location="logout";
//	}
//});
//od.ModuleMgr.registerType('logout', LogoutModule);
Ext.EventManager.on(window, 'beforeunload', function(){
	
});

od.MzfWindow=Ext.extend(Ext.Window,{
	constrain:true,
	close:function(){
		od.curWinList.remove(this);
		od.MzfWindow.superclass.close.call(this);
	}
});

Ext.reg('window',od.MzfWindow);

Ext.onReady(function() {
	Ext.QuickTips.getQuickTip().el.setZIndex(70000);
	xds.DEBUG_MODE = false;
	Ext.BLANK_IMAGE_URL = '/tplt/scripts/ext-3.0+/resources/images/default/s.gif';
	SyntaxHighlighter.config.clipboardSwf = 'deploy/clipboard.swf';
	Ext.ux.logoDesktop = '<img src="images/banner.jpg" height="100%" width="100%" />';
	Ext.Msg.buttonText={ok:'确认',cancel:'取消',yes:'是',no:'否'};

	var app = new od.App( {
		defaultModule : 'index'
	});
	
	app.on('configupdated',function(cfg){
		var userInfoPanel=Ext.getCmp('userInfoPanel');
		if(userInfoPanel){
			var xtpl=new Ext.XTemplate('<p>用户：{userName} &nbsp;&nbsp;&nbsp;&nbsp;部门：{orgName}</p>');
			xtpl.overwrite(userInfoPanel.body,this.appConfig.user);			
			  
			Ext.TaskMgr.start({  
			    run : function() {  
					Ext.Ajax.request({
				   		url: "/heartbeat",
				   		params:{userId: od.AppInstance.appConfig.user.id},
						method : "GET",
						hideMask:true,
						success : function(resp) {						
				   			var result = Ext.decode(resp.responseText);
				   			if (result.success == false) {
					   			Ext.MessageBox.alert('提示', result.msg, function(btn) {
					   				window.location='/tplt/login.html';
					   			});
				   			}
						},
				   		failure: function(resp) {
				   			/*
					        Ext.MessageBox.show({
					           title: '错误',
					           msg: 'status: ' + resp.status + '<br/>statusText: ' + resp.statusText,
					           buttons: Ext.MessageBox.OK,
					           animEl: 'testerBtn',
					           fn: function(){},
					           icon: Ext.MessageBox.ERROR
					       });
					       */		       				
						}
				  	}); 
			    },  
			    interval : 200000	// 20 second  
			});
		}
	},app);
	
	app.run();
});
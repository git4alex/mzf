Ext.override(Ext.form.TriggerField,{
	initComponent:function(){
		Ext.form.TriggerField.superclass.initComponent.call(this);
		this.addEvents('triggerclick');
	},

	onTriggerClick:function(){
		this.fireEvent('triggerclick',this);
	}
});

Ext.override(Ext.Component,{
	initComponent:function(){
		if(this.listeners){
	        this.on(this.listeners);
	        delete this.listeners;
	    }
	    this.enableBubble(this.bubbleEvents);

	    if(this.permissionId){
	    	try{
		    	if(!od.hasPermission(this.permissionId)){
	    			if(this.isFormField || this.fieldLabel){
	    				this.inputType="hidden";
	    			}else{
	    				this.hidden=true;
	    			}
		    	}
	    	}catch(e){

	    	}
	    }
	}
});

Ext.Ajax.on('requestexception',function(con,resp,options){

});

Ext.ns('org.delta');

var od=org.delta;

//Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
Ext.QuickTips.init();
//Ext.History.init();
od.curWinList = new Ext.util.MixedCollection();
od.App=function(cfg){
	od.App.superclass.constructor.call(this,cfg);

	Ext.apply(this,cfg);

	var moduleRegistory={};
	this.getModuleRegistory=function(){
		return moduleRegistory;
	};

	this.Debug=true;

	od.AppInstance=this;

	Ext.History.on('change',function(token){
		if(token != this.active.moduleId){
			this.activeModule(token);
		}
	},this);
};

Ext.extend(od.App,Ext.util.Observable,{

	updateConfig:function(){
//		var mask=new Ext.LoadMask(Ext.getBody(),{msg:'Please wait...'});
//
//		mask.show();
		Ext.Ajax.request({
			url:'getAppConfig',
			success:function(response){
				this.appConfig=Ext.decode(response.responseText);
//				mask.hide();
				this.onConfigUpdated();
                Ext.util.Cookies.set("currentUserId",this.appConfig.user.id);
                this.startUserCheckTask();
				var menu=this.appConfig.menuList;
				if(menu && menu.length>0){
	//				var items=this.view.mainMenu.items.clone();
					this.view.mainMenu.removeAll(true);
					for(i=0;i<menu.length;i++){
						this.view.mainMenu.addItem(menu[i]);
					}

					this.view.mainMenu.doLayout();
				}
			},
			scope:this
		});
	},
    startUserCheckTask:function(){
        this.userCheckTask = {
            run:function(){
                var currentUserId = Ext.util.Cookies.get("currentUserId");
                if(Ext.isEmpty(currentUserId) || (currentUserId != od.AppInstance.appConfig.user.id)){
                    Ext.Msg.show({
                        title:'提示',
                        msg: '当前用户已退出，请重新登录。',
                        buttons: Ext.Msg.OK,
                        fn: function(btn){
                            window.location = '/login.html';
                        },
                        icon: Ext.MessageBox.WARNING
                    });

                    Ext.TaskMgr.stop(od.AppInstance.userCheckTask);
                }
            },
            interval:1000
        };

        Ext.TaskMgr.start(this.userCheckTask);
    },

	run:function(cfg){
		this.updateConfig();
	},

	onConfigUpdated:function(){
		this.showView();
		this.fireEvent('configupdated',this.appConfig);
	},

	showView:function(){
		this.view = new od.DefaultAppView();

		var mid=window.location.hash;
		if(!Ext.isEmpty(mid)){
			if(mid.length>0){
				mid=mid.substring(1, mid.length);
			}

			if(!Ext.isEmpty(mid)){
				this.activeModule(mid);
			}
		}else if(!Ext.isEmpty(this.defaultModule)){
			this.activeModule(this.defaultModule);
		}
	},

	activeModule:function(mtype){
		if(od.ModuleMgr.isRegistered(mtype)){
			var module=od.ModuleMgr.create(mtype);
			this.showModule(module);
		}else{
			od.ModuleMgr.loadModuleDef(mtype,this.showModule,this);
		}
	},

	showModule:function(m){
		delete this.active;
		this.active=m;
		this.view.showModule(m);
		window.location.hash=this.active.moduleId;
		Ext.History.add(this.active.moduleId);

		if(this.active.moduleName){
			Ext.getDoc().dom.title=this.active.moduleName;
		}

		m.fireEvent('active',m);
	}
});

od.DefaultMainMenu=Ext.extend(Ext.Toolbar,{
	add:function(item){
		this.addDefaultHandler(item);
		od.DefaultMainMenu.superclass.add.call(this,item);
	},

	addDefaultHandler:function(item){
		item.menu=item.children;

		if(item.menu){
            item.hideOnClick = false;
			Ext.each(item.menu,this.addDefaultHandler,this);
		} else {
			if(!Ext.isEmpty(item.moduleId)){
				Ext.apply(item,{
					handler:function(){
						od.AppInstance.activeModule(this.moduleId);
					}
				});
			}
		}
	}
});

od.DefaultAppView=Ext.extend(Ext.Viewport,{
	layout:'border',
	showModule:function(module){
		this.clientArea.removeAll(true);
		var defComp = module.createDefaultComponent();
		if(!Ext.isEmpty(defComp)){
			if(defComp.xtype == 'window'){
				this.clientArea.add({region:'center',xtype:'panel',style:'padding:4px;'});
				this.clientArea.doLayout();
				defComp.show();
			}else{
				Ext.apply(defComp,{style:'padding:4px;'});
				this.clientArea.add(defComp);
				this.clientArea.doLayout();
			}
		}
	},

	initComponent:function(){
		this.mainMenu=new od.DefaultMainMenu();

		this.header=new Ext.Panel({
			height:48,
			border:false,
			cls: 'tplt-bottom-border',
			region:'north',
			layout:'hbox',
			layoutConfig:{align:'stretch'},
			items:[{
				flex:1,
				border:false,
				html:Ext.ux.logoDesktop || ""
			},{
				width:320,
				border:false,
				padding:'6px',
				id:'userInfoPanel'
			}]
		});

		this.foot=new Ext.Panel({
			height:32,
			border:false,
			region:'south',
			cls: 'tplt-top-border'
		});

		this.clientArea=new Ext.Panel({
			region:'center',
			border:false,
			tbar:this.mainMenu,
			layout:'fit'
		});

		Ext.apply(this,{
			items:[this.header,this.foot,this.clientArea]
		});

		od.DefaultAppView.superclass.initComponent.call(this);
	}
});

od.ModuleMgr=function(){
	var types={};

	return{
		create:function(mtype){
			return new types[mtype]();
		},
        isRegistered: function (mtype) {
            return types[mtype] !== undefined;
        },
        registerType: function (mtype, cls) {
            types[mtype] = cls;
            cls.mtype = mtype;
        },
		loadModuleDef:function(mtype,cb,scope){
        	var me=this;
			Ext.Ajax.request({
				url:'entity/module?moduleId='+mtype,
				method:'GET',
				success:function(response){
					var cfg=Ext.decode(response.responseText);
					if(cfg){
						if(!Ext.isEmpty(cfg.root) && cfg.root.length>0){
							cfg=Ext.decode(cfg.root[0].config);

							me.registerType(mtype,Ext.extend(od.XdsModule,cfg));
							cb.call(scope,me.create(mtype));

						}else{

						}
					}
				}
			});
		},
		getTypes:function(){
			return types;
		}
	};
}();

od.Module=Ext.extend(Ext.util.Observable,{
	constructor:function(cfg){
		this.addEvents('active','init');
		od.Module.superclass.constructor.call(this,cfg);

		this.init();
	},
	init:function(){
		if(this.components && this.components.length>0){
			var tmp=this.components;
			this.comRegistry=new Ext.util.MixedCollection();
			for(var i=0;i<tmp.length;i++){
				if(tmp[i].id){
					this.comRegistry.add(tmp[i].id,tmp[i]);
				}
			}
		}

		this.fireEvent('init',this);
	},
	createDefaultComponent:Ext.emptyFn,
	createComponent:function(comId){
		var cfg=this.copy(this.comRegistry.get(comId));
		var cmp = Ext.create(cfg);
		if(cfg.xtype == "window"){
			od.curWinList.add(cmp.id, cmp);
		}
		return cmp;
	},
	copy : function (obj) {//e
		if(typeof(obj) != 'object'){
			return obj;
		}
	    var ret = {};
	    if (!obj) {
	        return ret;
	    }
	    var item, value;//c:item;b:value
	    for (var i in obj) {
	        item = typeof obj[i];
	        value = obj[i];
	        if (item === "object") {
	            if (Ext.isArray(value)) {
	            	ret[i]=[];
	            	for(var c=0;c<value.length;c++){
	            		ret[i].push(this.copy(value[c]));
	            	}
	            } else {
	                ret[i] = this.copy(value);
	            }
	        } else {
                ret[i] = value;
	        }
	    }
	    return ret;
	}
});

od.XdsModule=Ext.extend(od.Module,{
	createDefaultComponent:function(){
		if(this.components.length>0){
			if(this.defaultComponent){
				return this.createComponent(this.defaultComponent);
			}else{
				return this.createComponent(this.components[0].id);
			}
		}
	}
});

od.create=function(comId){
	return od.AppInstance.active.createComponent(comId);
};

od.showWindow=function(winId){

	var win=Ext.getCmp(winId);
	if(Ext.isEmpty(win)){
		win=od.AppInstance.active.createComponent(winId);
	}
	if(win){
		win.show();
	}
	return win;
};

od.closeWindow=function(winId){
	var win=Ext.getCmp(winId);
	if(win){
		od.curWinList.remove(win);
		win.close();
	}
};

od.hasPermission = function(permissionId){
	if(Ext.isArray(od.AppInstance.appConfig.user.perms)){
		if(od.AppInstance.appConfig.user.perms.indexOf(permissionId) < 0){
			return false;
		}
	}
	return true;
};

function JsonToXml(separator){
	this.result=[];
	this.isPretty = false;
	this.separator = separator || "\r\n";

	this.result.push("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
}

JsonToXml.prototype.spacialChars=["&","<",">","\"","''"];
JsonToXml.prototype.validChars=["&amp;","&lt;","&gt;","&quot;","&apos;"];

JsonToXml.prototype.toString = function(){
	return this.result.join("");
};

JsonToXml.prototype.replaceSpecialChar = function(s){
    for(var i=0;i<this.spacialChars.length;i++){
        s=s.replace(new RegExp(this.spacialChars[i],"g"),this.validChars[i]);
    }
    return s;
};

JsonToXml.prototype.appendText = function(s){
    s = this.replaceSpecialChar(s);
    this.result.push(s);
};


JsonToXml.prototype.appendFlagBegin = function(s){
	this.result.push("<"+s+">");
};

JsonToXml.prototype.appendFlagEnd = function(s){
	this.result.push("</"+s+">");
};

JsonToXml.prototype.toXml = function(json){
	this._toXml(json);
	return this.toString();
};

JsonToXml.prototype._isString = function(v){
	return v.constructor == String;
};

JsonToXml.prototype._isNumber = function(v){
	return v.constructor == Number;
};
JsonToXml.prototype._isBoolean = function(v){
	return v.constructor == Boolean;
};
JsonToXml.prototype._isObject = function(v){
	return v.constructor == Object;
};
JsonToXml.prototype._isArray = function(v){
	return v.constructor == Array;
};

JsonToXml.prototype._getValue = function(v){
	if(this._isBoolean(v)){
		if(v){
			return '1';
		}

		return '0';
	}

	return v;
};

JsonToXml.prototype._toXml = function(json){
    for(var tag in json){
    	if(this._isArray(json[tag])){
    		for(var i=0;i<json[tag].length;i++){
    			var item = json[tag][i];
    			if(this._isObject(item)){
    	        	this.result.push("<"+tag+" ");
    	        	for(var prop in item){
    	        		var v = item[prop];
    	        		if(this._isString(v) || this._isNumber(v) || this._isBoolean(v)){
    	        			this.result.push(prop+"='"+this._getValue(v)+"' ");
    	        			delete item[prop];
    	        		}
    	        	}
    	        	this.result.push(">");
    	            this._toXml(item);
    			} else if(this._isArray(item)){
    				this.appendFlagBegin(tag);
    				var obj={};
    				obj[tag]=item;
    				this._toXml(obj);
    			} else if(this._isString(item)){
    				this.appendFlagBegin(tag);
    	            this.appendText(item);
    	        }

    			this.appendFlagEnd(tag);
    		}
    	}else{
	        if(this._isObject(json[tag])){
	        	this.result.push("<"+tag+" ");
	        	for(var prop in json[tag]){
	        		var v = json[tag][prop];
	        		if(this._isString(v) || this._isNumber(v) || this._isBoolean(v)){
	        			this.result.push(prop+"='"+this._getValue(v)+"' ");
	        			delete json[tag][prop];
	        		}
	        	}
	        	this.result.push(">");
	            this._toXml(json[tag]);
	        }else if(this._isString(json[tag]) || this._isNumber(json[tag]) || this._isBoolean(json[tag])){
	        	this.appendFlagBegin(tag);
	            this.appendText(this._getValue(json[tag]));
	        }
	        this.appendFlagEnd(tag);
    	}
    }
};

Ext.onReady(function() {
	Ext.getDoc().addKeyListener(8,function(key,evt){
		var tgt = evt.target;
		if(tgt.tagName.toLowerCase() != 'input' && tgt.tagName.toLowerCase() != 'textarea'){
			evt.stopEvent();
		}
	});
});


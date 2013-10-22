xds.types.BizcodeBox = Ext.extend(xds.types.Trigger, {
	cid : 'bizcombo',
	iconCls : 'icon-combo-tree',
	category : "Form",
	defaultName : "&lt;BizcodeBox&gt;",
	text : "BizcodeBox",
	dtype : "xdbizcodebox",
	xtype : 'bizcodebox',
	xcls : "org.delta.BizcodeBox",
	naming : "BizcodeBox",
	isContainer : false,
	transformGroup: "fields",
	layoutable : false,
	enableFlyout : false,
	defaultConfig : {
		anchor : "100%",
		fieldLabel : "Label",
		editable:false
	},

	xdConfigs: [{
        name: "clearable",
        group: "Tplt",
        ctype: "boolean"
    },{
    	name: "checkable",
    	group: "Tplt",
    	ctype: "boolean"
    },{
        name: "treeHeight",
        group: "Tplt",
        ctype: "number"
    },{
    	name:'triggerclick',
    	group:'(Event)',
    	ctype:'fn'
    },{
    	name:'afterRender',
    	group:'(Event)',
    	ctype:'fn',
    	params:['field']
    },{
		name : "bizType",
		group : "Tplt",
		ctype : "string"
	},{
		name:'change',
		group:'(Event)',
		ctype:'fn',
		params:['field','newValue','oldValue']
	},{
		name:'newvalue',
		group:'(Event)',
		ctype:'fn',
		params:['field','newValue']
	},{
		name:'clear',
		group:'(Event)',
		ctype:'fn',
		params:[]
	}]
});

//xds.Registry.register(xds.types.BizcodeBox);


od.BizcodeBox =Ext.extend(Ext.form.TwinTriggerField, {
    trigger1Class: 'x-form-clear-trigger',
    hideTrigger1:true,
    initComponent : function(){
        od.BizcodeBox.superclass.initComponent.call(this);
        this.on('specialkey', function(f, e){
            if(e.getKey() == e.ENTER){
                this.onTrigger1Click();
            }
        }, this);
        this.getTree();
        this.addEvents('newvalue','clear');
    },

    getTriggerWidth: function(){
        var tw = 0;
        Ext.each(this.triggers, function(t, index){
            var triggerIndex = 'Trigger' + (index + 1),
                w = t.getWidth();
            tw+=w;
        }, this);
        return tw;
    },
    
    onTrigger2Click: function() {
        if(this.readOnly || this.disabled){
            return;
        }
        if(this.isExpanded()){
            this.collapse();
            this.el.focus();
        }else {
        	var tree=this.getTree();
        	tree.setWidth(this.getEl().getWidth()+this.getTriggerWidth());
            tree.show();
            tree.getEl().alignTo(this.wrap, 'tl-bl?');
        }
    },
    
    isExpanded : function(){
        return this.treePanel && this.treePanel.isVisible();
    },
    
    onTrigger1Click:function(){
    	this.clear();
    },
    
    clear:function(){
    	this.setValue(null);
    	this.hideClear();
    	this.collapse();
    	this.fireEvent('clear');
    },
    
    reset:function(){
    	this.clear();
    	od.BizcodeBox.superclass.reset.call(this);
    },
    
    hideClear:function(){
    	var c=this.getTrigger(0);
    	if(c){
    		c.hide();
    	}
    },
    
    showClear:function(){
    	if(!this.clearable)
    		return;
    	
    	if(this.triggers){
	    	var c=this.getTrigger(0);
	    	if(c){
	    		c.show();
	    	}
    	}
    },

    getTree: function() {
        if (!this.treePanel) {
            if (!this.treeWidth) {
                this.treeWidth = Math.max(200, this.width || 200);
            }
            if (!this.treeHeight) {
                this.treeHeight = 120;
            }
            this.treePanel = new Ext.tree.TreePanel({
                renderTo: Ext.getBody(),
                root: this.root || new Ext.tree.AsyncTreeNode({children: this.children}),
                rootVisible: (typeof this.rootVisible != 'undefined') ? this.rootVisible : (this.root ? true : false),
                floating: true,
                autoScroll: true,
                minWidth: 200,
                minHeight: 120,
                lines:false,
                width: this.treeWidth,
                height: this.treeHeight,
                listeners: {
                    hide: this.onTreeHide,
                    show: this.onTreeShow,
                    click: this.onTreeNodeClick,
                    scope: this
                }
            });
            this.treePanel.show();
            this.treePanel.hide();
            this.relayEvents(this.treePanel.loader, ['beforeload', 'load', 'loadexception']);
            
            this.treePanel.loader.baseAttrs = {
                    iconCls: 'no-icon'
            };
            
            if(this.resizable){
                this.resizer = new Ext.Resizable(this.treePanel.getEl(),  {
                   pinned:true, handles:'se'
                });
                this.mon(this.resizer, 'resize', function(r, w, h){
                    this.treePanel.setSize(w, h);
                }, this);
            }
            this.loadData();
        }
        return this.treePanel;
    },

    onTreeShow: function() {
        Ext.getDoc().on('mousewheel', this.collapseIf, this);
        Ext.getDoc().on('mousedown', this.collapseIf, this);
    },

    onTreeHide: function() {
        Ext.getDoc().un('mousewheel', this.collapseIf, this);
        Ext.getDoc().un('mousedown', this.collapseIf, this);
    },

    collapseIf : function(e){
        if(!e.within(this.wrap) && !e.within(this.getTree().getEl())){
            this.collapse();
        }
    },

    collapse: function() {
    	if(this.checkable){
    		var vs=this.getTree().getChecked(['id']);
    		this.setValue(vs);
    		this.fireEvent('newvalue', this, this.value);
    	}else{

    	}
    	
        this.getTree().hide();
        if(this.resizer){
        	this.resizer.resizeTo(this.treeWidth, this.treeHeight);
        }
    },

    // private
    validateBlur : function(){
        return !this.treePanel || !this.treePanel.isVisible();
    },

    setValue: function(v) {
        this.startValue = this.value = v;
        
        if(!this.treePanel){
        	this.getTree();
        }
        
        var tree=this.treePanel;
        
        if(this.checkable){
			if (typeof v === 'string') {
				v = v.split(',');
			}
			
			var text = [];
			
			var cns=tree.getChecked();
	        Ext.each(cns,function(n){
            	n.getUI().toggleCheck();
	        },this);
			
			
	        Ext.each(v,function(i){
	            var n=tree.getNodeById(i);
	            if(n){
	            	text.push(n.text);
	            	n.getUI().toggleCheck(true);
	            }
	        },this);
	        
	        if(text.length > 0 ){
	        	this.setRawValue(text.join(','));
	        }else{
	        	this.setRawValue(null);
	        }
        }else{
        	var n=tree.getNodeById(v);
        	if(n){
        		this.setRawValue(n.text);
        	}else{
        		this.setRawValue(null);
        	}
        }
        
        if(!Ext.isEmpty(this.value) && this.readOnly != true && this.clearable && this.disabled !=true){
    		this.showClear();
    	}else{
    		//this.hideClear();    		
    	}
    },

    getValue: function() {
    	if(Ext.isArray(this.value)){
    		return this.value.join(',');
    	}
        return this.value;
    },
    
	loadData:function(){
		if(!Ext.isEmpty(this.bizType)){
			var tree=this.getTree();
			if(!Ext.isEmpty(od.AppInstance.appConfig.bizCode[this.bizType])){
				var tmp=od.AppInstance.appConfig.bizCode[this.bizType];
				for(var i=0;i<tmp.length;i++){
					tmp[i].id=tmp[i].value;
					if(this.checkable){
						if(this.value && (this.value.indexOf) && (this.value.indexOf(tmp[i].id) != -1)){
							tmp[i].checked=true;
						}else{
							tmp[i].checked=false;
						}
					}
					
					tree.getRootNode().appendChild(tmp[i]);
				}
				
				var n=tree.getNodeById(this.value);
		        if(n){
		        	this.setRawValue(n.text);
		        	if(this.readOnly != true){
		        		this.showClear();
		        	}
		        }
			}else{
				Ext.Ajax.request({
					url:'entity/tree/dict',
					method:'GET',
					params:{typeCode:this.bizType},
					success:function(response,opts){
						var tmp=Ext.decode(response.responseText);
						if(!Ext.isEmpty(tmp)){
							od.AppInstance.appConfig[this.bizType]=[];
							for(var i=0;i<tmp.length;i++){
								od.AppInstance.appConfig[this.bizType].push(tmp[i]);
								if(this.checkable){
									if(this.value && (this.value.indexOf) && (this.value.indexOf(tmp[i].id) != -1)){
										tmp[i].checked=true;
									}else{
										tmp[i].checked=false;
									}
								}								
								tree.getRootNode().appendChild(tmp[i]);
							}
							
							var n=this.getTree().getNodeById(this.value);
					        if(n){
					        	this.setRawValue(n.text);
					        	if(this.readOnly != true){
					        		this.showClear();
					        	}
					        }
						}
					},
					scope:this
				});
			}
		}
	},   
	
    onTreeNodeClick: function(node, e) {
    	if(this.checkable){
    		node.getUI().toggleCheck();
    	}else{
	        this.setRawValue(node.text);
	        this.value = node.id;
	        this.fireEvent('newvalue', this, node.id);
	        this.collapse();
	        this.showClear();
    	}
    	//node.getUI().toggleCheck();
    },
    updateEditState: function(){
        if(this.rendered){
            if (this.readOnly) {
                this.el.dom.readOnly = true;
                this.el.addClass('x-trigger-noedit');
                this.mun(this.el, 'click', this.onTrigger2Click, this);
                this.trigger.setDisplayed(false);
            } else {
                if (!this.editable) {
                    this.el.dom.readOnly = true;
                    this.el.addClass('x-trigger-noedit');
                    this.mon(this.el, 'click', this.onTrigger2Click, this);
                } else {
                    this.el.dom.readOnly = false;
                    this.el.removeClass('x-trigger-noedit');
                    this.mun(this.el, 'click', this.onTrigger2Click, this);
                }
                this.trigger.setDisplayed(!this.hideTrigger);
            }
            this.onResize(this.width || this.wrap.getWidth());
        }
    } 
});


xds.BizcodeBox =Ext.extend(Ext.form.TwinTriggerField, {
    trigger1Class: 'x-form-clear-trigger',
    hideTrigger1:true,
    getTriggerWidth: function(){
	    var tw = 0;
	    Ext.each(this.triggers, function(t, index){
	        var triggerIndex = 'Trigger' + (index + 1);
	        var w = t.getWidth();
	        tw+=w;
	    }, this);
	    return tw;
	}
});

Ext.reg('xdbizcodebox',xds.BizcodeBox);
Ext.reg('bizcodebox', od.BizcodeBox);

xds.types.BizcodeColumn = Ext.extend(xds.types.GridColumn, {
    cid: "bizcodecolumn",
    defaultName: "&lt;bizcodeColumn&gt;",
    text: "Bizcode Column",
    xtype: "bizcodecolumn",
    dtype: "xdbizcodecolumn",
    xcls: "org.delta.BizcodeColumn",
    iconCls: "icon-grid-bizcode",
    defaultEditor: "bizcombo",
    defaultConfig: {
        header: "Column",
        sortable: true,
        resizable: true,
        width: 100,
        menuDisabled:true
    },
    defaultDataIndex: "bizcode",
    xdConfigs: [{
        name: "bizType",
        group: "Tplt",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.BizcodeColumn);
xds.BizcodeColumn=Ext.extend(Ext.grid.Column,{
	
});
Ext.reg('xdbizcodecolumn',xds.BizcodeColumn);
Ext.grid.Column.types.xdbizcodecolumn=xds.BizcodeColumn;

od.BizcodeColumn=Ext.extend(Ext.grid.Column, {
    constructor: function (cfg) {
		od.BizcodeColumn.superclass.constructor.call(this, cfg);
        this.codeMap=this.getCodeMap();
        this.renderer=this.rendererFn.createDelegate(this);
    },
    rendererFn : function (value, metaData, record, rowIndex, colIndex, store) {
		if(Ext.isEmpty(this.bizType)){
			return value;
		}else{
			if(Ext.isEmpty(this.codeMap)){
				return value;
			}else{
				return Ext.isEmpty(this.codeMap[value]) ? value : this.codeMap[value];
			}
		}
    },
    getCodeMap:function(){
    	var ret={};
    	var tmp=od.AppInstance.appConfig.bizCode[this.bizType];
    	Ext.each(tmp,function(item){
    		ret[item.value]=item.text;
    	},this);
    	
    	return ret;
    }
    
//    ,
//	loadData:function(){
//		if(!Ext.isEmpty(this.bizType)){
//			if(!Ext.isEmpty(od.AppInstance.appConfig.bizCode[this.bizType])){
//				this.addToCodeMap(od.AppInstance.appConfig.bizCode[this.bizType]);
//			}else{
//				Ext.Ajax.request({
//					url:'entity/tree/dict',
//					method:'GET',
//					params:{typeCode:this.bizType},
//					success:function(response,opts){
//						var tmp=Ext.decode(response.responseText);
//						if(!Ext.isEmpty(tmp)){
//							od.AppInstance.appConfig[this.bizType]=tmp;
//							this.addToCodeMap(tmp);
//						}
//					},
//					scope:this
//				});
//			}
//		}
//	},
//	addToCodeMap:function(arr){
//		this.codeMap=this.codeMap || {};
//		for(var i=0;i<arr.length;i++){
//			this.codeMap[arr[i].id]=arr[i];
//			
//			if(arr[i].children){
//				this.addToCodeMap(arr[i].children);
//			}
//		}
//	}
});
Ext.reg('bizcodecolumn',od.BizcodeColumn);
Ext.grid.Column.types.bizcodecolumn=od.BizcodeColumn;

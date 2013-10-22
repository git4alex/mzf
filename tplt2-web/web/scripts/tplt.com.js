od.GroupingView = Ext.extend(Ext.grid.GroupingView, {
    doGroupStart: function (buf, g, cs, ds, colCount) {
        if (this.getExtGroupText) {
            g.text = this.getExtGroupText(g, cs, ds, colCount);
        }
        od.GroupingView.superclass.doGroupStart.call(this, buf, g, cs, ds, colCount);
    }
});

od.GridView = Ext.extend(Ext.grid.GridView,{
    getTotalWidth:function(){
        return (this.cm.getColumnCount()*2 + this.cm.getTotalWidth()) + 'px';
    }
});

od.GridPanel = Ext.extend(Ext.grid.GridPanel, {
	rowNumberer: false,
	useRowEditor:false,
	showSummary:false,
    columnLines:true,
    initComponent: function () {
    	if(Ext.isArray(this.columns)){
    		var tmp = [];
    		Ext.each(this.columns,function(col){
    			if(col.permissionId){
    				if(od.hasPermission(col.permissionId)){
    					tmp.push(col);
    				}
    			}else{
    				tmp.push(col);
    			}
    		});
    		this.columns = tmp;
    	}

    	if(this.rowNumberer){
    		var rowNum = new Ext.grid.RowNumberer();
    		this.columns = [rowNum].concat(this.columns);
    	}

    	this.plugins = this.plugins || [];
    	if(this.useRowEditor){
    		this.plugins.push(new Ext.ux.grid.RowEditor());
    	}

    	if(this.showSummary){
    		this.summary = new Ext.ux.grid.GroupSummary();
    		this.plugins.push(this.summary);
    	}

        this.store = Ext.StoreMgr.lookup(this.store);
        od.GridPanel.superclass.initComponent.call(this);
        delete this.loadMask;
    },
    getSelectionModel: function () {
        if (!this.selModel) {
            Ext.each(this.colModel.config, function (col) {
                if (col.assm) {
                    this.selModel = this.colModel.getColumnById(col.id);
                    return;
                }
            }, this);
        }
        return od.GridPanel.superclass.getSelectionModel.call(this);
    },
    getViewConfig: function () {
        var viewConfigProps = ["autoFill", "forceFit", "markDirty", "enableRowBody", "selectedRowClass", "rowOverCls"];
        var ret = {};
        Ext.each(viewConfigProps, function (prop) {
            if (!Ext.isEmpty(this[prop])) {
                ret[prop] = this[prop];
                delete this[prop];
            }
        }, this);
        return ret;
    },
    getGroupViewConfig: function () {
        var ret = this.getViewConfig();
        var groupViewConfigProps = ["cancelEditOnToggle", "emptyGroupText", "enableGrouping", "enableGroupingMenu", "groupByText", "enableNoGroups", "showGroupName", "startCollapsed", "showGroupsText", "ignorAdd", "hideGroupedColumn", "groupTextTpl", "groupMode", "getExtGroupText"];
        Ext.each(groupViewConfigProps, function (prop) {
            if (!Ext.isEmpty(this[prop])) {
                ret[prop] = this[prop];
                delete this[prop];
            }
        }, this);
        return ret;
    },
    getView: function () {
        if (!this.view) {
            if (this.useGroupView || this.userGroupView) {
                this.view = new od.GroupingView(this.getGroupViewConfig());
            } else {
                this.view = new od.GridView(this.getViewConfig());
            }
        }
        return this.view;
    }
});
Ext.reg('grid', od.GridPanel);
od.CheckboxGroup = Ext.extend(Ext.form.CheckboxGroup, {
    getValue: function () {
        var out = [];
        this.eachItem(function (item) {
            if (item.checked) {
                out.push(item.inputValue);
            }
        });
        return out.join(',');
    },
    getName: function () {
        if (this.items.items) {
            for (var i = 0; i < this.items.items.length; i++) {
                if (!Ext.isEmpty(this.items.items[i])) {
                    if (this.items.items[i].getName) {
                        var ret = this.items.items[i].getName();
                        if (!Ext.isEmpty(ret)) {
                            return ret;
                        }
                    }
                }
            }
        }
    }
    //			setValue : function(v) {
    //				var arr = this.items.items;
    //
    //				for (var i = 0; i < arr.length; i++) {
    //					if (arr[i].inputValue) {
    //						for (var j = 0; j < v.length; j++) {
    //							if (v[j] == arr[i].inputValue) {
    //								arr[i].setValue(true);
    //							}
    //						}
    //					}
    //				}
    //
    //			}
});
Ext.reg('checkboxgroup', od.CheckboxGroup);
od.Checkbox = Ext.extend(Ext.form.Checkbox, {
    onClick: function () {
        if (this.readOnly == true || this.disabled) {
            return false;
        }
        od.Checkbox.superclass.onClick.call(this);
    }
});
Ext.reg('checkbox', od.Checkbox);
od.JsonGroupStore = Ext.extend(Ext.data.GroupingStore, {
    constructor: function (config) {
        od.JsonGroupStore.superclass.constructor.call(this, Ext.apply(config, {
            reader: new Ext.data.JsonReader(config)
        }));
    }
});
Ext.reg('jsongroupstore', od.JsonGroupStore);

od.PagingToolbar = Ext.extend(Ext.PagingToolbar,{
    displayInfo:true,
    showPageSizeCombo:false,
    initComponent : function(){
    	if(this.showPageSizeCombo){
	    	this.pageSizeCombo = new Ext.form.ComboBox({
				store:new Ext.data.ArrayStore({
					fields:['text','value'],
					data:[['每页20行',20],['每页50行',50],['每页100行',100],['每页300行',300],['每页500行',500],['每页1000行',1000]]
				}),
				mode: 'local',
				displayField:'text',
				valueField:'value',
				forceSelection: true,
				triggerAction: 'all',
				selectOnFocus:true,
				width:90,
				value:this.pageSize,
				editable:false,
				listeners:{
					'select':function(me,record,idx){
						this.pageSize = record.data.value;
						this.doLoad(0);
					},
					scope:this
				}
			});

	    	if(this.items){
	    		this.items.push(this.pageSizeCombo);
	    	}else{
	    		this.items = [this.pageSizeCombo];
	    	}
    	}
    	od.PagingToolbar.superclass.initComponent.call(this);

        if(this.inputItem){
        	 this.inputItem.width = 40;
        }
    }
});
Ext.reg('paging',od.PagingToolbar);

od.NumberField = Ext.extend(Ext.form.NumberField,{
	decimalPrecision:3
});
Ext.reg('numberfield',od.NumberField);

od.DateField = Ext.extend(Ext.form.DateField,{
	invalidText: '时间格式有误,格式为:'+new Date().format('Y-m-d'),
	emptyText: this.emptyText || new Date().format('Y-m-d')+" "
});
Ext.reg('datefield',od.DateField);

//--- A ComboBox with a secondary trigger button that clears the contents of the ComboBox
Ext.form.ClearableComboBox = Ext.extend(Ext.form.ComboBox, {
  initComponent : function(){
      Ext.form.ClearableComboBox.superclass.initComponent.call(this);

      this.triggerConfig = {
          tag:'span', cls:'x-form-twin-triggers', style:'padding-right:2px',  // padding needed to prevent IE from clipping 2nd trigger button
          cn:[

              {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger x-form-clear-trigger"},
              {tag: "img", src: Ext.BLANK_IMAGE_URL, cls: "x-form-trigger"}

             ]
         };
  },

  getTrigger : function(index){
      return this.triggers[index];
  },

  initTrigger : function(){
      var ts = this.trigger.select('.x-form-trigger', true);
      this.wrap.setStyle('overflow', 'hidden');
      var triggerField = this;
      ts.each(function(t, all, index){
           t.hide = function(){
              var w = triggerField.wrap.getWidth();
              this.dom.style.display = 'none';
              triggerField.el.setWidth(w-triggerField.trigger.getWidth());
          };
          t.show = function(){
              var w = triggerField.wrap.getWidth();
              this.dom.style.display = '';
              triggerField.el.setWidth(w-triggerField.trigger.getWidth());
          };
          var triggerIndex = 'Trigger'+(index+1);

          if(this['hide'+triggerIndex]){
              t.dom.style.display = 'none';
          }
          t.on("click", this['on'+triggerIndex+'Click'], this, {preventDefault:true});
          t.addClassOnOver('x-form-trigger-over');
          t.addClassOnClick('x-form-trigger-click');
      }, this);
      this.triggers = ts.elements;
  },

  onTrigger1Click : function() {this.reset();},     // clear contents of combobox
  onTrigger2Click : function() {this.onTriggerClick();}     // pass to original combobox trigger handler
});

Ext.reg('combo', Ext.form.ClearableComboBox);

od.DataView = Ext.extend(Ext.DataView,{
	dragSelector:false,
	labelEditor:false,
	initComponent:function(){
		this.plugins = this.plugins || [];
		if(this.dragSelector){
			this.plugins.push(new Ext.DataView.DragSelector());
		}

		if(this.labelEditor){
			if(this.store){
				this.plugins.push(new Ext.DataView.LabelEditor({dataIndex: this.store.idProperty}));
			}
		}

		od.DataView.superclass.initComponent.call(this);
	}
});

Ext.reg("dataview",od.DataView);
Ext.DataView.LabelEditor = Ext.extend(Ext.Editor, {
	alignment : "tl-tl",
	hideEl : false,
	cls : "x-small-editor",
	shim : false,
	completeOnEnter : true,
	cancelOnEsc : true,
	labelSelector : 'span.x-editable',

	constructor : function(cfg, field) {
		Ext.DataView.LabelEditor.superclass.constructor.call(this, field
				|| new Ext.form.TextField({
					allowBlank : false,
					growMin : 90,
					growMax : 240,
					grow : true,
					selectOnFocus : true
				}), cfg);
	},

	init : function(view) {
		this.view = view;
		view.on('render', this.initEditor, this);
		this.on('complete', this.onSave, this);
	},

	initEditor : function() {
		this.view.on({
			scope : this,
			containerclick : this.doBlur,
			click : this.doBlur
		});
		this.view.getEl().on('mousedown', this.onMouseDown, this, {
			delegate : this.labelSelector
		});
	},

	doBlur : function() {
		if (this.editing) {
			this.field.blur();
		}
	},

	onMouseDown : function(e, target) {
		if (!e.ctrlKey && !e.shiftKey) {
			var item = this.view.findItemFromChild(target);
			e.stopEvent();
			var record = this.view.store.getAt(this.view.indexOf(item));
			this.startEdit(target, record.data[this.dataIndex]);
			this.activeRecord = record;
		} else {
			e.preventDefault();
		}
	},

	onSave : function(ed, value) {
		this.activeRecord.set(this.dataIndex, value);
	}
});

Ext.DataView.DragSelector = function(cfg) {
	cfg = cfg || {};
	var view, proxy, tracker;
	var rs, bodyRegion, dragRegion = new Ext.lib.Region(0, 0, 0, 0);
	var dragSafe = (cfg.dragSafe === true);

	this.init = function(dataView) {
		view = dataView;
		view.on('render', onRender);
	};

	function fillRegions() {
		rs = [];
		view.all.each(function(el) {
			rs[rs.length] = el.getRegion();
		});
		bodyRegion = view.el.getRegion();
	}

	function cancelClick() {
		return false;
	}

	function onBeforeStart(e) {
		return !dragSafe || e.target == view.el.dom;
	}

	function onStart(e) {
		view.on('containerclick', cancelClick, view, {
			single : true
		});
		if (!proxy) {
			proxy = view.el.createChild({
				cls : 'x-view-selector'
			});
		} else {
			if (proxy.dom.parentNode !== view.el.dom) {
				view.el.dom.appendChild(proxy.dom);
			}
			proxy.setDisplayed('block');
		}
		fillRegions();
		view.clearSelections();
	}

	function onDrag(e) {
		var startXY = tracker.startXY;
		var xy = tracker.getXY();

		var x = Math.min(startXY[0], xy[0]);
		var y = Math.min(startXY[1], xy[1]);
		var w = Math.abs(startXY[0] - xy[0]);
		var h = Math.abs(startXY[1] - xy[1]);

		dragRegion.left = x;
		dragRegion.top = y;
		dragRegion.right = x + w;
		dragRegion.bottom = y + h;

		dragRegion.constrainTo(bodyRegion);
		proxy.setRegion(dragRegion);

		for ( var i = 0, len = rs.length; i < len; i++) {
			var r = rs[i], sel = dragRegion.intersect(r);
			if (sel && !r.selected) {
				r.selected = true;
				view.select(i, true);
			} else if (!sel && r.selected) {
				r.selected = false;
				view.deselect(i);
			}
		}
	}

	function onEnd(e) {
		if (!Ext.isIE) {
			view.un('containerclick', cancelClick, view);
		}
		if (proxy) {
			proxy.setDisplayed(false);
		}
	}

	function onRender(view) {
		tracker = new Ext.dd.DragTracker({
			onBeforeStart : onBeforeStart,
			onStart : onStart,
			onDrag : onDrag,
			onEnd : onEnd
		});
		tracker.initEl(view.el);
	}
};

Ext.ns('Ext.ux.grid');

Ext.ux.grid.GroupSummary = Ext.extend(Ext.util.Observable, {
    constructor : function(config){
        Ext.apply(this, config);
        Ext.ux.grid.GroupSummary.superclass.constructor.call(this);
    },
    init : function(grid){
        this.grid = grid;
        var v = this.view = grid.getView();

        v.templates = v.templates || {};
        v.templates.body = new Ext.Template('{rows}{total}');
        v.renderBody = this.renderBody.createDelegate(this);

        if(!this.rowTpl){
            this.rowTpl = new Ext.Template(
                '<div class="x-grid3-summary-row {rowCls}" style="{tstyle}" >',
                '<table class="x-grid3-summary-table" border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
                '<tbody><tr>{cells}</tr></tbody>',
                '</table></div>'
            );
            this.rowTpl.disableFormats = true;
        }
        this.rowTpl.compile();

        if(!this.cellTpl){
            this.cellTpl = new Ext.Template(
                '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}">',
                '<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on">{value}</div>',
                "</td>"
            );
            this.cellTpl.disableFormats = true;
        }
        this.cellTpl.compile();

        if(v.enableGrouping){
        	v.doGroupEnd = this.doGroupEnd.createDelegate(this);
            v.afterMethod('onColumnWidthUpdated', this.doWidth, this);
            v.afterMethod('onAllColumnWidthsUpdated', this.doAllWidths, this);
            v.afterMethod('onColumnHiddenUpdated', this.doHidden, this);
            v.afterMethod('onUpdate', this.doUpdate, this);
            v.afterMethod('onRemove', this.doRemove, this);
        }
    },

    renderBody:function(){
        var markup = this.view.renderRows() || '&#160;';
        return this.view.templates.body.apply({rows: markup,total:this.renderTotal()});
    },

    renderTotal:function(){
        var data = {};
        var cs = this.view.getColumnData(),cfgs=this.grid.getColumnModel().config;
        this.grid.getStore().each(function(rec){
            for(var i=0;i<cs.length;i++){
                if(cfgs[i].summaryType){
                    data[cs[i].name] = Ext.ux.grid.GroupSummary.Calculations[cfgs[i].summaryType](data[cs[i].name] || 0, rec, cs[i].name, data);
                }
            }
        });
        return this.renderSummary({data:data},cs,true);
    },

    renderSummary : function(o, cs,isTotal){
        cs = cs || this.view.getColumnData();
        var cfg = this.grid.getColumnModel().config,
            buf = [], c, p = {}, cf, last = cs.length-1;
        for(var i = 0, len = cs.length; i < len; i++){
            c = cs[i];
            cf = cfg[i];
            p.id = c.id;
            p.style = c.style;
            p.css = i == 0 ? 'x-grid3-cell-first ' : (i == last ? 'x-grid3-cell-last ' : '');
            if(cf.summaryType || cf.totalRenderer || cf.summaryRenderer){
                var sRenderer = isTotal ? cf.totalRenderer || cf.summaryRenderer : cf.summaryRenderer;
                p.value = (sRenderer || c.renderer)(o.data[c.name], p, o);
            }else{
                p.value = '';
            }
            if(p.value == undefined || p.value === "") p.value = "&#160;";
            buf[buf.length] = this.cellTpl.apply(p);
        }

        var rowData={
            tstyle: 'width:'+this.view.getTotalWidth()+';',
            cells: buf.join('')
        };

        if(isTotal){
            rowData.rowCls = 'total';
        }

        return this.rowTpl.apply(rowData);
    },
    toggleSummaries : function(visible){
        var el = this.grid.getGridEl();
        if(el){
            if(visible === undefined){
                visible = el.hasClass('x-grid-hide-summary');
            }
            el[visible ? 'removeClass' : 'addClass']('x-grid-hide-summary');
        }
    },
    calculate : function(rs, cs){
        var data = {}, r, c, cfg = this.grid.getColumnModel().config, cf;
        for(var j = 0, jlen = rs.length; j < jlen; j++){
            r = rs[j];
            for(var i = 0, len = cs.length; i < len; i++){
                c = cs[i];
                cf = cfg[i];
                if(cf.summaryType){
                    data[c.name] = Ext.ux.grid.GroupSummary.Calculations[cf.summaryType](data[c.name] || 0, r, c.name, data);
                }
            }
        }
        return data;
    },

    doGroupEnd : function(buf, g, cs, ds, colCount){
        var data = this.calculate(g.rs, cs);
        buf.push('</div>', this.renderSummary({data: data}, cs), '</div>');
    },

    doWidth : function(col, w, tw){
        if(!this.isGrouped()){
            return;
        }
        var gs = this.view.getGroups(),
            len = gs.length,
            i = 0,
            s;
        for(; i < len; ++i){
            s = gs[i].childNodes[2];
            s.style.width = tw;
            s.firstChild.style.width = tw;
            s.firstChild.rows[0].childNodes[col].style.width = w;
        }
    },

    doAllWidths : function(ws, tw){
        if(!this.isGrouped()){
            return;
        }
        var gs = this.view.getGroups(),
            len = gs.length,
            i = 0,
            j,
            s,
            cells,
            wlen = ws.length;

        for(; i < len; i++){
            s = gs[i].childNodes[2];
            s.style.width = tw;
            s.firstChild.style.width = tw;
            cells = s.firstChild.rows[0].childNodes;
            for(j = 0; j < wlen; j++){
                cells[j].style.width = ws[j];
            }
        }
    },

    doHidden : function(col, hidden, tw){
        if(!this.isGrouped()){
            return;
        }
        var gs = this.view.getGroups(),
            len = gs.length,
            i = 0,
            s,
            display = hidden ? 'none' : '';
        for(; i < len; i++){
            s = gs[i].childNodes[2];
            s.style.width = tw;
            s.firstChild.style.width = tw;
            s.firstChild.rows[0].childNodes[col].style.display = display;
        }
    },

    isGrouped : function(){
        return !Ext.isEmpty(this.grid.getStore().groupField);
    },

    refreshSummary : function(groupValue){
        return this.refreshSummaryById(this.view.getGroupId(groupValue));
    },

    getSummaryNode : function(gid){
        var g = Ext.fly(gid, '_gsummary');
        if(g){
            return g.down('.x-grid3-summary-row', true);
        }
        return null;
    },

    refreshSummaryById : function(gid){
        var g = Ext.getDom(gid);
        if(!g){
            return false;
        }
        var rs = [];
        this.grid.getStore().each(function(r){
            if(r._groupId == gid){
                rs[rs.length] = r;
            }
        });
        var cs = this.view.getColumnData(),
            data = this.calculate(rs, cs),
            markup = this.renderSummary({data: data}, cs),
            existing = this.getSummaryNode(gid);

        if(existing){
            g.removeChild(existing);
        }
        Ext.DomHelper.append(g, markup);
        return true;
    },

    doUpdate : function(ds, record){
        this.refreshSummaryById(record._groupId);
    },

    doRemove : function(ds, record, index, isUpdate){
        if(!isUpdate){
            this.refreshSummaryById(record._groupId);
        }
    },

    showSummaryMsg : function(groupValue, msg){
        var gid = this.view.getGroupId(groupValue),
             node = this.getSummaryNode(gid);
        if(node){
            node.innerHTML = '<div class="x-grid3-summary-msg">' + msg + '</div>';
        }
    }
});

//backwards compat
Ext.grid.GroupSummary = Ext.ux.grid.GroupSummary;


Ext.ux.grid.GroupSummary.Calculations = {
    'sum' : function(v, record, field){
        return v + (record.data[field]||0);
    },

    'count' : function(v, record, field, data){
        return data[field+'count'] ? ++data[field+'count'] : (data[field+'count'] = 1);
    },

    'max' : function(v, record, field, data){
        var v = record.data[field];
        var max = data[field+'max'] === undefined ? (data[field+'max'] = v) : data[field+'max'];
        return v > max ? (data[field+'max'] = v) : max;
    },

    'min' : function(v, record, field, data){
        var v = record.data[field];
        var min = data[field+'min'] === undefined ? (data[field+'min'] = v) : data[field+'min'];
        return v < min ? (data[field+'min'] = v) : min;
    },

    'average' : function(v, record, field, data){
        var c = data[field+'count'] ? ++data[field+'count'] : (data[field+'count'] = 1);
        var t = (data[field+'total'] = ((data[field+'total']||0) + (record.data[field]||0)));
        return t === 0 ? 0 : t / c;
    }
};
Ext.grid.GroupSummary.Calculations = Ext.ux.grid.GroupSummary.Calculations;

Ext.ux.grid.HybridSummary = Ext.extend(Ext.ux.grid.GroupSummary, {
    calculate : function(rs, cs){
        var gcol = this.view.getGroupField(),
            gvalue = rs[0].data[gcol],
            gdata = this.getSummaryData(gvalue);
        return gdata || Ext.ux.grid.HybridSummary.superclass.calculate.call(this, rs, cs);
    },

    updateSummaryData : function(groupValue, data, skipRefresh){
        var json = this.grid.getStore().reader.jsonData;
        if(!json.summaryData){
            json.summaryData = {};
        }
        json.summaryData[groupValue] = data;
        if(!skipRefresh){
            this.refreshSummary(groupValue);
        }
    },

    getSummaryData : function(groupValue){
        var reader = this.grid.getStore().reader,
            json = reader.jsonData,
            fields = reader.recordType.prototype.fields,
            v;

        if(json && json.summaryData){
            v = json.summaryData[groupValue];
            if(v){
                return reader.extractValues(v, fields.items, fields.length);
            }
        }
        return null;
    }
});

//backwards compat
Ext.grid.HybridSummary = Ext.ux.grid.HybridSummary;

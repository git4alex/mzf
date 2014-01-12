xds = new Ext.util.Observable();
xds.types = {};
xds.addEvents("init", "newcomponent");
xds.create = function (cfg) {
    var cls = xds.Registry.get(cfg.cid);
    var ret = new cls(cfg);
    return ret;
};
xds.copy = function (obj) {
    var ret = {};
    if (!obj) {
        return ret;
    }
    var item, value, i;
    for (i in obj) {
        item = typeof obj[i];
        value = obj[i];
        if (item === "object") {
            if (Ext.isArray(value)) {
                ret[i] = value.slice(0);
            } else {
                ret[i] = xds.copy(value);
            }
        } else {
            if (item !== "function") {
                ret[i] = value;
            }
        }
    }
    return ret;
};
xds.countKeys = function (collection) {
    var i = 0;
    for (var item in collection) {
        i++;
    }
    return i;
};
xds.TransformGroups = {};
xds.Registry = function () {
    var data = new Ext.util.MixedCollection(true, function (item) {
    	if(item){
    		return item.prototype.cid;
    	}
    });
    var ConfigStore = Ext.extend(Ext.data.JsonStore, {
        constructor: function () {
            ConfigStore.superclass.constructor.call(this, {
                id: "cid",
                fields: [{
                    name: "id",
                    mapping: "cid"
                },
                "xtype", "xcls", "typeDef", "text", "iconCls", "naming", "category", "isVisual"]
            });
        }
    });
    var pts = null;
    return {
        register: function (xdTypeCls) {
        	if(!xdTypeCls){
        		return;
        	}
            data.add(xdTypeCls);
            xdTypeCls.prototype.__xdclass = xdTypeCls;
            xdTypeCls.prototype.configs = xdTypeCls.configs = new Ext.util.MixedCollection(false, function (item) {
                return item.name;
            });
            var hierarchy = [];
            hierarchy.unshift(xdTypeCls.prototype);

        	var superCls = xdTypeCls.superclass;
            while (superCls) {
                hierarchy.unshift(superCls);
                superCls = superCls.constructor.superclass;
            }

            Ext.each(hierarchy, function (cls) {
                var xdCfgs = cls.xdConfigs;
                if (Ext.isArray(xdCfgs)) {
                    Ext.each(xdCfgs, function (cfg) {
                        if (cfg && xds.Config.types[cfg.ctype]) {
                            xdTypeCls.configs.add(new xds.Config.types[cfg.ctype](cfg));
                        }
                    });
                }
            });
            if (xdTypeCls.prototype.transformGroup) {
                xds.TransformGroups[xdTypeCls.prototype.transformGroup] = xds.TransformGroups[xdTypeCls.prototype.transformGroup] || [];
                if (!xds.TransformGroups[xdTypeCls.prototype.transformGroup].contains(xdTypeCls.prototype.cid)) {
                    xds.TransformGroups[xdTypeCls.prototype.transformGroup].push(xdTypeCls.prototype.cid);
                }
            }
        },
        register2: function (xdType) { //f
//            this.userTypes = this.userTypes || [];
//            this.userTypes.push(xdType);
            data.add(xdType);
            xdType.prototype.__xdclass = xdType;
            var xdConfig = xdType.prototype.xdConfigs || []; //g
            xdType.prototype.configs = xdType.configs = new Ext.util.MixedCollection(false, function (item) {
                return item.name;
            });

            for (var i = 0, len = xdConfig.length; i < len; i++) {
                xdType.configs.add(new xds.Config.types[xdConfig[i].ctype](xdConfig[i]));
            }
            if (xdType.prototype.transformGroup) {
                xds.TransformGroups[xdType.prototype.transformGroup] = xds.TransformGroups[xdType.prototype.transformGroup] || [];
                if (!xds.TransformGroups[xdType.prototype.transformGroup].contains(xdType.prototype.cid)) {
                    xds.TransformGroups[xdType.prototype.transformGroup].push(xdType.prototype.cid);
                }
            }
        },
        unregister: function (xdTypeCls) {
            data.remove(xdTypeCls);
        },
        get: function (cid) {
            return data.get(cid);
        },
        all: data,
        createStore: function (g) {
            if (!pts) {
                pts = [];
                for (var i = 0, len = data.items.length; i < len; i++) {
                    pts.push(data.items[i].prototype);
                }
            }
            var store = new ConfigStore();
            store.loadData(pts);
            if (g) {
                store.filter("isVisual", true);
            }
            return store;
        },
        addUserType: function (userType) {
            this.userTypes = this.userTypes || [];
            this.userTypes.push(userType);
        }
    };
}();
if (!Array.prototype.contains) {
    Array.prototype.contains = function (a) {
        return this.indexOf(a) !== - 1;
    };
}

(function () {
    var c = Ext.Component.prototype.afterRender;
    Ext.Component.override({
        filmCls: "",
        infoCls: "",
        afterRender: function () {
            c.apply(this, arguments);
            if (this.viewerNode) {
                this.createFilm();
            }
        },
        createFilm: function () {
            var a = Ext.getCmp("canvas");
            if (a && a.el && a.el.contains(this.el)) {
                var d = this.viewerNode;
                this.film = a.body.createChild({
                    cls: "el-film x-unselectable " + d.component.filmCls,
                    id: "film-for-" + d.id,
                    style: "z-index: " + (10000 + (d.getDepth() * 100))
                });
                this.film.enableDisplayMode();
                this.syncFilm();
                this.on("resize", this.syncFilm, this);
                this.on("destroy", this.destroyDesigner, this);
            } else {
                if (a && this.parentMenu) {
                    var d = this.viewerNode;
                    this.film = a.body.createChild({
                        cls: "el-film x-unselectable " + d.component.filmCls,
                        id: "film-for-" + d.id,
                        style: "z-index: " + (10000 + (d.getDepth() * 100))
                    });
                    this.film.enableDisplayMode();
                    this.syncFilm.defer(10, this);
                    this.on("resize", this.syncFilm, this, {
                        buffer: 100
                    });
                    this.on("destroy", this.destroyDesigner, this);
                }
            }
        },
        onFilmClick: function () {
            xds.fireEvent("componentclick", {
                component: this.viewerNode.attributes.config,
                node: this.viewerNode
            });
        },
        positionFilm: function () {
            this.film.setXY(this.el.getXY());
        },
        syncFilm: function () {
            var e;
            if (this.film && (e = this.getFilmEl())) {
                var d = e.getRegion();
                this.film.setRegion(d);
                this.film.lastRegion = d;
            }
        },
        getFilmEl: function () {
            var d = this.getPositionEl();
            if (this.fieldLabel) {
                return this.el.up(".x-form-item") || d;
            }
            return d;
        },
        destroyDesigner: function () {
            this.film.remove();
        }
    });
})();
Ext.tree.TreeNode.prototype.setNodeId = function (c) {
    var b = this;
    this.id = c;
    this.attributes.id = c;
    if (this.component) {
        this.component.id = c;
    }
    var a = this.getOwnerTree();
    if (a) {
        delete a.nodeHash[b];
        a.nodeHash[c] = this;
    }
};
(function () {
    var a = Ext.lib.Event;
    var b = a.addListener;
    a.suspend = function () {
        a.addListener = a.on = Ext.emptyFn;
    };
    a.resume = function () {
        a.addListener = a.on = b;
    };
})();
Ext.grid.GridView.override({
    focusCell: function (row, col, hscroll) {
        this.syncFocusEl(this.ensureVisible(row, col, hscroll));
        if (Ext.isGecko) {
            this.focusEl.focus();
        } else {
            this.focusEl.focus.defer(1, this.focusEl);
        }
    },
    resolveCell: function (row, col, hscroll) {
        if (typeof row != "number") {
        	row = row.rowIndex;
        }
        if (!this.ds) {
            return null;
        }
        if (row < 0 || row >= this.ds.getCount()) {
            return null;
        }
        col = (col !== undefined ? col : 0);
        var rowEl = this.getRow(row),cellEl;
        if (!(hscroll === false && col === 0)) {
            while (this.cm.isHidden(col)) {
            	col++;
            }
            cellEl = this.getCell(row, col);
        }
        return {
            row: rowEl,
            cell: cellEl
        };
    },
    getResolvedXY: function (a) {
        if (!a) {
            return null;
        }
        var b = this.scroller.dom,
            e = a.cell,
            d = a.row;
        return e ? Ext.fly(e).getXY() : [b.scrollLeft + this.el.getX(), Ext.fly(d).getY()];
    },
    syncFocusEl: function (d, a, c) {
        var b = d;
        if (!Ext.isArray(b)) {
            d = Math.min(d, Math.max(0, this.getRows().length - 1));
            b = this.getResolvedXY(this.resolveCell(d, a, c));
        }
        this.focusEl.setXY(b || this.scroller.getXY());
    },
    ensureVisible: function (t, g, e) {
        var r = this.resolveCell(t, g, e);
        if (!r || !r.row) {
            return
        }
        var k = r.row,
            h = r.cell;
        var n = this.scroller.dom;
        var s = 0;
        var d = k,
            o = this.el.dom;
        while (d && d != o) {
            s += d.offsetTop;
            d = d.offsetParent;
        }
        s -= this.mainHd.dom.offsetHeight;
        var q = s + k.offsetHeight;
        var a = n.clientHeight;
        var o = parseInt(n.scrollTop, 10);
        var m = o + a;
        if (s < o) {
            n.scrollTop = s;
        } else {
            if (q > m) {
                n.scrollTop = q - a;
            }
        }
        if (e !== false) {
            var l = parseInt(h.offsetLeft, 10);
            var j = l + h.offsetWidth;
            var i = parseInt(n.scrollLeft, 10);
            var b = i + n.clientWidth;
            if (l < i) {
                n.scrollLeft = l;
            } else {
                if (j > b) {
                    n.scrollLeft = j - n.clientWidth;
                }
            }
        }
        return this.getResolvedXY(r);
    }
});
Ext.form.ComboBox.override({
    expand: function () {
        if (this.isExpanded() || !this.hasFocus) {
            return
        }
        if (this.bufferSize) {
            this.doResize(this.bufferSize);
            delete this.bufferSize;
        }
        this.list.alignTo(this.wrap, this.listAlign);
        this.list.show();
        if (Ext.isGecko2) {
            this.innerList.setOverflow("auto");
        }
        this.mon(Ext.getDoc(), {
            scope: this,
            mousewheel: this.collapseIf,
            mousedown: this.collapseIf
        });
        this.fireEvent("expand", this);
    }
});
Ext.tree.TreeNode.override({
    destroy: function (slice) {
        Ext.tree.TreeNode.superclass.destroy.call(this, slice);
        Ext.destroy(this.ui, this.loader);
    }
});
Ext.tree.DefaultSelectionModel.override({
    select: function (c, a) {
        if (c.ui.wrap && !Ext.fly(c.ui.wrap).isVisible() && a) {
            return a.call(this, c);
        }
        var b = this.selNode;
        if (c == b) {
            c.ui.onSelectedChange(true);
        } else {
            if (this.fireEvent("beforeselect", this, c, b) !== false) {
                if (b) {
                    b.ui.onSelectedChange(false);
                }
                this.selNode = c;
                c.ui.onSelectedChange(true);
                this.fireEvent("selectionchange", this, c, b);
            }
        }
        return c;
    }
});
xds.PropertyRecord = Ext.data.Record.create([{
    name: "name",
    type: "string"
},
"value", "group", "text"]);
xds.PropGrid = Ext.extend(Ext.grid.EditorGridPanel, {
    enableColumnMove: false,
    stripeRows: false,
    trackMouseOver: false,
    clicksToEdit: 1,
    enableHdMenu: false,
    baseCls: "x-plain",
    hideHeaders: true,
    cls: "grouped-prop-grid",
    cacheSizes: false,
    initComponent: function () {
        this.lastEditRow = null;
        var b = new xds.PropGrid.Store(this);
        this.propStore = b;
        var a = new xds.PropGrid.ColumnModel(this, b);
        this.selModel = new Ext.grid.CellSelectionModel();
        b.store.sort("name", "ASC");
        this.addEvents("beforepropertychange", "propertychange");
        this.cm = a;
        this.store = b.store;
        this.view = new Ext.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            scrollOffset: 18,
            getRowClass: function (c) {
                return c.data.value === undefined ? "" : "has-value";
            }
        });
        this.tbar = new Ext.Toolbar({
            cls: this.tbCls || "xds-toolbar",
            items: [{
                tooltip: "Show configs grouped by component",
                pressed: this.propStore.showCommon,
                iconCls: "icon-grouped",
                enableToggle: true,
                toggleGroup: "prop-group",
                toggleHandler: function (c, d) {
                    if (d) {
                        this.setGrouped(true);
                    }
                },
                scope: this
            },
            " ", {
                tooltip: "Show configs alphabetical",
                pressed: false,
                iconCls: "icon-sorted",
                enableToggle: true,
                toggleGroup: "prop-group",
                toggleHandler: function (c, d) {
                    if (d) {
                        this.setGrouped(false);
                    }
                },
                scope: this
            },
            "-", {
                tooltip: 'Show "Common" config group',
                pressed: false,
                iconCls: "icon-common",
                enableToggle: true,
                toggleHandler: function (c, d) {
                    this.setShowCommon(d);
                },
                scope: this
            },
            "-", {
                tooltip: "Hide inherited configs",
                pressed: false,
                iconCls: "icon-hide-inherited",
                enableToggle: true,
                toggleHandler: function (c, d) {
                    this.setHideInherited(d);
                },
                scope: this
            },
            {
                xtype: "propertysearch",
                store: this.store
            }]
        });
        xds.PropGrid.superclass.initComponent.call(this);
        this.selModel.on("beforecellselect", function (e, d, c) {
            if (c === 0) {
                this.startEditing.defer(100, this, [d, 1]);
                return false;
            }
        }, this);
    },
    setGrouped: function (a) {
        if (!a) {
            this.view.enableGrouping = false;
            this.propStore.store.clearGrouping();
        } else {
            this.view.enableGrouping = true;
            this.propStore.store.groupBy("group");
        }
    },
    setHideInherited: function (b, a) {
        this.propStore.filterGroup = b ? this.component.xcls : undefined;
        if (a !== false) {
            this.propStore.refresh();
        }
    },
    setShowCommon: function (b, a) {
        this.propStore.showCommon = b;
        if (a !== false) {
            this.propStore.refresh();
        }
    },
    setComponent: function (a) {
        this.stopEditing();
        this.component = a;
        if (this.propStore.filterGroup) {
            this.propStore.filterGroup = a.xcls;
        }
        this.propStore.setComponent(a);
    },
    clear: function () {
        delete this.component;
        this.propStore.clear();
    },
    onRender: function () {
        xds.PropGrid.superclass.onRender.apply(this, arguments);
        this.getGridEl().addClass("x-props-grid");
        this.view.mainBody.on("mousedown", this.onChecked, this);
        this.view.mainBody.on("click", this.onBodyClick, this);
    },
    onChecked: function (d, b) {
        if (b = d.getTarget("span.bcheck", 2)) {
            d.stopPropagation();
            var a = b.firstChild.className;
            var c = this.propStore.store.getById(a);
            if (c) {
                c.set("value", !b.firstChild.checked);
            }
        }
    },
    onBodyClick: function (c, a) {
        if (a = c.getTarget(".props-grid-clear", 1)) {
            c.stopEvent();
            var d = this.getSelectionModel().getSelectedCell()[0];
            var b = this.propStore.store.getAt(d);
            if (b) {
                b.set("value", undefined);
            }
        }
    },
    startEditById: function (c) {
        var b = this.propStore.store.getById(c);
        if (b) {
            var a = this.propStore.store.indexOf(b);
            this.startEditing(a, 1);
        }
    }
});
xds.PropGrid.Search = Ext.extend(Ext.form.TriggerField, {
    enableKeyEvents: true,
    disableKeyFilter: false,
    width: 0,
    emptyText: "Filter...",
    triggerClass: "x-form-clear-trigger",
    initComponent: function () {
        xds.PropGrid.Search.superclass.initComponent.call(this);
        this.on("keyup", this.onPropertySearch, this, {
            buffer: 100
        });
        this.mon(this.store, "load", function () {
            this.reset();
            this.focus();
        }, this);
    },
    onRender: function () {
        xds.PropGrid.Search.superclass.onRender.apply(this, arguments);
        this.container.setStyle("width", "100%");
        this.ownerCt.on("resize", function () {
            this.setWidth(0);
            this.setWidth(this.container.getWidth());
        }, this);
    },
    onPropertySearch: function () {
        var a = this.getValue();
        if (a && a.trim() != "") {
            this.store.filter("name", this.getValue());
        } else {
            if (this.store.isFiltered()) {
                this.store.clearFilter();
            }
        }
    },
    onTriggerClick: function () {
        this.reset();
        this.onPropertySearch();
        this.focus();
    }
});
Ext.reg("propertysearch", xds.PropGrid.Search);
xds.PropGrid.Store = function (a, b) {
    this.grid = a;
    this.store = new Ext.data.GroupingStore({
        recordType: xds.PropertyRecord,
        groupField: "group"
    });
    this.store.on("update", this.onUpdate, this);
    xds.PropGrid.Store.superclass.constructor.call(this);
};
Ext.extend(xds.PropGrid.Store, Ext.util.Observable, {
    showCommon: true,
    getConfigByType: function (b, a) {
        if (b == "Common") {
            return this.grid.component.getConfigObject(a);
        }
        var c = this.grid.component["get" + b + "Configs"]();
        return c.map[a];
    },
    getConfig: function (a) {
        if (a.configType) {
            return this.getConfigByType(a.configType, a.data.name);
        }
        return this.grid.component.getConfigObject(a.data.name);
    },
    getConfigAt: function (a) {
        return this.getConfig(this.store.getAt(a));
    },
    loadModuleProperties: function () {
        this.store.removeAll();
        var mprops = [];
        this.store.loadRecords({
            records: mprops
        }, {}, true);
    },
    setComponent: function (comp) { //p:comp
        this.component = comp;
        this.store.removeAll();
        var f = [];
        var items = comp.configs.items; //m:items
        var cfg = comp.getConfig(); //d:cfg
        for (var h = 0, n = items.length, itemName, itemGroup; h < n; h++) { //e:itemName,j:itemGroup
        	itemText = items[h].text || items[h].name;
            itemName = items[h].name;
            itemGroup = items[h].group;
            if (!this.filterGroup || this.filterGroup == itemGroup) {
                if (itemGroup === '(Event)') {
                    f.push(new xds.PropertyRecord({
                    	text: itemText,
                        name: itemName,
                        value: comp.userConfig.evtHandlers ? comp.userConfig.evtHandlers[itemName] : undefined,
                        group: itemGroup
                    }, itemName));
                } else {
                    f.push(new xds.PropertyRecord({
                    	text: itemText,
                        name: itemName,
                        value: cfg[itemName],
                        group: itemGroup
                    }, itemName));
                }
            }
        }
        var layoutConfigs = comp.getLayoutConfigs(); //b:layoutConfigs
        if (layoutConfigs) {
            layoutConfigs = layoutConfigs.items;
            for (var h = 0, n = layoutConfigs.length, layoutName; h < n; h++) {
            	var layoutText = layoutConfigs[h].text || layoutConfigs[h].name;
                var layoutName = layoutConfigs[h].name;
                f.push(new xds.PropertyRecord({
                	text: layoutText,
                    name: layoutName,
                    value: cfg[layoutName],
                    group: layoutConfigs[h].group
                }, layoutName));
            }
        }
        var containerConifgs = comp.getContainerConfigs();
        if (containerConifgs) {
            containerConifgs = containerConifgs.items;
            for (var h = 0, n = containerConifgs.length, cfgName; h < n; h++) {
            	var cfgText = containerConifgs[h].text || containerConifgs[h].name;
                var cfgName = containerConifgs[h].name;
                var record = new xds.PropertyRecord({
                	text: cfgText,
                    name: cfgName,
                    value: containerConifgs[h].getValue(comp),
                    group: containerConifgs[h].group
                }, "Container-" + cfgName);
                record.configType = "Container";
                f.push(record);
            }
        }
        if (this.showCommon) {
            var commonConfigs = comp.getCommonConfigs(); //m:commonConfigs
            if (commonConfigs) {
                commonConfigs = commonConfigs.items;
                for (var h = 0, n = commonConfigs.length, configName; h < n; h++) {
                	var configText = commonConfigs[h].text || commonConfigs[h].name;
                	var configName = commonConfigs[h].name;
                    var a = new xds.PropertyRecord({
                    	text: configText,
                        name: configName,
                        value: commonConfigs[h].getValue(comp),
                        group: "(Common)"
                    }, "Common-" + configName);
                    a.configType = "Common";
                    f.push(a);
                }
            }
        }
        var editorConfigs = comp.getEditorConfigs();
        if (editorConfigs) {
            editorConfigs = editorConfigs.items;
            for (var h = 0, n = editorConfigs.length, cfgName; h < n; h++) {
            	var cfgText = editorConfigs[h].text || editorConfigs[h].name;
                var cfgName = editorConfigs[h].name;
                f.push(new xds.PropertyRecord({
                	text: cfgText,
                    name: cfgName,
                    value: editorConfigs[h].getValue(comp),
                    group: editorConfigs[h].group
                }, cfgName));
            }
        }
        this.store.loadRecords({
            records: f
        }, {}, true);
    },
    onUpdate: function (store, record, operation) { //f:store,a:record,e:operation
        if (operation == Ext.data.Record.EDIT) {
            var value = record.data.value; //b:value
            var oldValue = record.modified.value;
            if (this.grid.fireEvent("beforepropertychange", this.component, record.data.name, value, oldValue) !== false) {
                this.getConfig(record).setValue(this.component, value);
                if (record.configType == "Common") {
                    this.store.getById(record.data.name).set("value", value);
                } else {
                    var r = this.store.getById("Common-" + record.data.name);
                    if (r) {
                        r.set("value", value);
                    }
                }
                record.commit();
                this.grid.fireEvent("propertychange", this.component, record.data.name, value, oldValue);
            } else {
                record.reject();
            }
        }
    },
    clear: function () {
        this.component = null;
        this.store.removeAll();
    },
    refresh: function () {
        this.setComponent(this.component);
    }
});
xds.PropGrid.ColumnModel = Ext.extend(Ext.grid.ColumnModel, {
    nameText: "Name",
    valueText: "Value",
    dateFormat: "m/j/Y",
    constructor: function (b, a) {
        this.grid = b;
        xds.PropGrid.ColumnModel.superclass.constructor.call(this, [{
            header: this.nameText,
            width: 50,
            sortable: true,
            dataIndex: "text",
            id: "name",
            menuDisabled: true
        },{
            header: this.valueText,
            width: 50,
            resizable: false,
            dataIndex: "value",
            id: "value",
            menuDisabled: true
        },{
            header: "",
            hidden: true,
            width: 10,
            resizable: false,
            locked: true,
            dataIndex: "group",
            menuDisabled: true
        }]);
        this.store = a;
        this.renderCellDelegate = this.renderCell.createDelegate(this);
        this.renderPropDelegate = this.renderProp.createDelegate(this);
    },
    isCellEditable: function (colIdx, rowIdx) {
        var cfg = this.store.getConfigAt(rowIdx);
        return (colIdx == 1) && !(cfg.readonly == true);
    },
    getRenderer: function (a) {
        return a == 1 ? this.renderCellDelegate : this.renderPropDelegate;
    },
    renderProp: function (propName, meta, recrod, rowIdx, colIdx, store) {
        var cfg = this.store.getConfigAt(rowIdx);
        if (cfg.readonly) {
            return propName + '<i class="xds-suffix-error"> &nbsp; Readonly &nbsp; </i>';
        } else {
            return propName;
        }
    },
    renderCell: function (e, d, b, rowIdx) {
        var f = this.store.getConfigAt(rowIdx);
        var a = f.render(e, d, b, rowIdx);

        if(f.editor == 'options'){
        	var edtor=this.store.getConfigAt(rowIdx).getEditor();
        	var rec = edtor.field.store.getById(a);
        	if(rec){
        		a = rec.get('text');
        	}
        }

        a = f.htmlEncode ? Ext.util.Format.htmlEncode(a) : a;
        if (typeof e !== "undefined" && f.clearable != false) {
            a += '<div class="x-tool x-tool-close props-grid-clear"></div>';
        } else {
            if (typeof a === "undefined") {
                a = "(none)";
            }
        }
        return a;
    },
    getCellEditor: function (colIdx, rowIdx) {
        return this.store.getConfigAt(rowIdx).getEditor();
    }
});
Ext.ux.TileView = Ext.extend(Ext.DataView, {
    categoryName: "category",
    imagePath: "imagePath",
    imageName: "imageName",
    itemName: "text",
    itemDescription: "description",
    itemIconCls: "iconCls",
    itemSelector: "dd",
    initComponent: function () {
        this.tpl = new Ext.XTemplate(this.getMarkup(), {
            getCategory: this.getCategory,
            openCategory: this.openCategory,
            view: this
        });
        Ext.ux.TileView.superclass.initComponent.call(this);
    },
    getMarkup: function () {
        return ['<div class="x-tile-ct">',
                	'<tpl for=".">',
                		'<tpl if="this.openCategory(values, xindex, xcount)">',
                			'<tpl if="xindex != 1">',
                				'<div style="clear:left"></div></dl>',
                			"</tpl>",
                			'<h2><div unselectable="on" class="x-unselectable">{[this.getCategory(values)]}</div></h2>',
                			"<dl>",
                		"</tpl>",
                		'<dd>',
                			'<img title="{text:htmlEncode}" src="', Ext.BLANK_IMAGE_URL, '" class="{', this.itemIconCls, '}"/>',
                			"<div><h4>{", this.itemName, "}</h4><p>{", this.itemDescription, "}</p></div>",
                		"</dd>",
                		'<tpl if="xindex == xcount">',
                			'<div style="clear:left"></div></dl>',
                		"</tpl>",
                	"</tpl>",
                "</div>"].join("");
    },
    openCategory: function (b, c, d) {
        var a = this.getCategory(b);
        if (this.lastCat != a) {
            this.lastCat = a;
            return true;
        }
        return false;
    },
    getCategory: function (a) {
        return a[this.view.categoryName];
    },
    onClick: function (b) {
        var a = b.getTarget("h2", 3, true);
        if (a) {
            a.toggleClass("collapsed");
            a.next("dl").toggleClass("collapsed");
        } else {
            return Ext.ux.TileView.superclass.onClick.apply(this, arguments);
        }
    },
    onAdd: function (ds, records, index) {
        this.refresh();
    }
});
xds.Project = Ext.extend(Ext.util.Observable, {
    dirty: false,
    constructor: function (a) {
        this.moduleName = "NewModule";
        xds.project = this;
        this.setData(a);
        Ext.apply(this, a);
    },

    save: function (asData,cb,autoSave) {
        var module = xds.inspector.root.module;

        module.version = module.version || 0;
        module.version ++;

        var xdsJson = Ext.encode(xds.project.getData());
        var cfgStr = module.getModuleConfig();
        var data = {};
        if (module.dbId) {
            data.id = module.dbId;
        }
        data.moduleId = module.getModuleId();
        data.config = cfgStr;
        var mName = module.getConfigValue("name");
        if (Ext.isEmpty(mName)) {
            mName = module.getModuleId();
        }
        data.name = mName;
        data.xdsConfig = xdsJson;
        var cat = module.getConfigValue("category");
        if (!Ext.isEmpty(cat)) {
            cfgStr += ",category:'" + cat + "'";
        }
        if (!Ext.isEmpty(cat)) {
            data.category = cat;
        }

        if(asData){
        	delete data.id;
        	data.moduleId = asData.moduleId || data.moduleId;
        	module.setConfig("id",data.moduleId);
        	data.name = asData.moduleName || data.name;
        	module.setConfig("name",data.name);
        	data.category = asData.moduleCategory || data.category;
        	module.setConfig("category",data.category);
        }

        data.updateTime = 'default';

        Ext.Ajax.request({
            url: '/module' + (data.id ? '/' + data.id : ''),
            method: data.id ? 'PUT' : 'POST',
            jsonData: data,
            params:{autoSave:autoSave || false},
            success: function (response, conn) {
                try {
                    var resp = Ext.decode(response.responseText);
                    if (resp) {
                        if (resp.id) {
                            module.dbId = resp.id;
                        }
                    }
                } catch (e) {}
//                xds.actions.saveAction.enable.defer(3000, xds.actions.saveAction);
                if(cb){
                	cb();
                }
            },
            failure: function () {
                xds.actions.saveAction.enable();
            }
        });
    },

    doAutoSave:function(){
    	this.save(null,null,true);
    },

    saveAs: function () {
    	var win=new Ext.Window({
    		title:"Save as",
    		modal:true,
    		iconCls: "icon-project-save-as",
    		width:380,
    		autoHeight:true,
    		closable:false,
    		items:[{
    			ref:"form",
    			xtype:"form",
    			padding:"6px",
    			border:false,
    			defaults:{xtype:"textfield",anchor:"-20"},
    			labelWidth:65,
    			items:[{
    				name:"moduleId",
    				fieldLabel:"模块ID",
    				allowBlank:false
    			},{
    				name:"moduleName",
    				fieldLabel:"模块名称",
    				allowBlank:false
    			},{
    				name:"moduleCategory",
    				fieldLabel:"模块分组"
    			},{
    				xtype:"textarea",
    				name:"moduleDesc",
    				fieldLabel:"模块说明"
    			}]
    		}],
    		buttons:[{
    			text:"保存",
    			ref:"../btnSave",
    			handler:function(btn){
    				var form=btn.refOwner.form.getForm();
    				if(form && form.isValid()){
	    				var asData=form.getFieldValues(true);
	    				xds.project.save(asData,function(){
	    					btn.refOwner.close();
	    				});
    				}
    			}
    		},{
    			text:"取消",
    			ref:"../btnCancel",
    			handler:function(btn){
    				btn.refOwner.close();
    			}
    		}]
    	});

        Ext.select('.el-film').hide();
        win.on('close', function () {
            Ext.select('.el-film').show();
        });
        win.show();
    },

    open: function (data) {
        if (data) {
            this.setData(data);
        }
        xds.inspector.root.beginUpdate();
        var root = xds.inspector.root;
        root.module = this.createNewModule();
        if (data) {
            root.module.dbId = data.dbId;
            root.setText(data.name);
        }
        while (root.firstChild) {
            root.removeChild(root.firstChild);
        }
        var comps = this.components || [];
        var defaultNode;
        for (var i = 0, comp; comp = comps[i]; i++) {
            var n = xds.inspector.restore(comp, root);
            if(comp.userConfig && comp.userConfig.id == data.defaultComponent){
            	defaultNode = n;
            }
        }
        xds.inspector.root.endUpdate();

        defaultNode = defaultNode || root.firstChild;
        if (defaultNode) {
        	defaultNode.getOwnerTree().expandPath(defaultNode.getPath());
        	defaultNode.select();
            xds.fireEvent("componentchanged");
        }
    },

    createNewModule:function(){
    	return new xds.types.Module();
    },
    getData: function () {
        var a = xds.inspector.root;
        var m = a.module;
        return m.getXdsJson();
    },
    setData: function (a) {
        Ext.apply(this, a);
    },
    doClose: function () {
        var a = xds.inspector.root;
        while (a.firstChild) {
            var b = a.removeChild(a.firstChild);
            b.destroy();
        }
        xds.canvas.clear();
    },
    close: function (a, b) {
        this.doClose();
        if (a) {
            a.call(b || this);
        }
    }
});
Ext.ux.WinEditor = Ext.extend(Ext.grid.GridEditor, {
    ignoreNoChange: false,
    initComponent: function () {
        Ext.grid.GridEditor.superclass.initComponent.call(this);
        this.field.on('valueupdated', this.onValueUpdated, this);
        this.field.on('cancel', this.onEditCancel, this);
    },
    onValueUpdated: function () {
        this.completeEdit(false);
    },
    onEditCancel: function () {
        this.cancelEdit(false);
    },
    onBlur: function () {
        if (this.allowBlur === true && this.editing && this.selectSameEditor !== true && Ext.isEmpty(this.field.editWin)) {
            this.completeEdit();
        }
    }
});
Ext.ux.WinField = Ext.extend(Ext.form.TriggerField, {
    initComponent: function () {
        Ext.ux.WinField.superclass.initComponent.call(this);
        this.addEvents('valueupdated', 'cancel');
    },
    setValue: function (value) {
        var strValue = value;
        if (Ext.isArray(value)){
        	strValue = value.join(',');
        } else if (typeof value == 'object') {
            this.objValue = value;
            this.objValue.toString = function () {
                return this.value;
            };
            strValue = this.objValue.value;
        }
        Ext.ux.WinField.superclass.setValue.call(this, strValue);
    },
    getValue: function () {
        return this.objValue || this.value;
    },
    reset: function () {
        delete this.objValue;
        delete this.value;
    }
});

xds.ColorPalette = Ext.extend(Ext.ColorPalette,{
    initComponent : function(){
    	xds.ColorPalette.superclass.initComponent.call(this);
        this.addEvents(
            'over'
        );
    },
    afterRender : function(){
    	xds.ColorPalette.superclass.afterRender.call(this);

    	this.mon(this.el, {
            "mouseover": this.onMouseOver,
            scope:this
        });
    },
    onMouseOver:function(e,t){
        e.preventDefault();
        if(!this.disabled){
            var ms = t.className.match(/(?:^|\s)color-(.{6})(?:\s|$)/);
            if(ms){
            	this.fireEvent('over',this,ms[1]);
            }
        }
    }
});

xds.Config = function (a) {
    Ext.apply(this, a);
    if (!xds.Config.editors.string) {
        Ext.apply(xds.Config.editors, {
            options: new Ext.grid.GridEditor(new Ext.form.ComboBox({
                editable: false,
                forceSelection: true,
                rowHeight: false,
                lastSearchTerm: false,
                triggerAction: "all",
                mode: "local",
                listClass: "x-combo-list-small",
                store: new Ext.data.ArrayStore({
                	idIndex:0,
                    fields: ["code","text"]
                }),
                displayField: "text",
                valueField:"code"
            })),
            date: new Ext.grid.GridEditor(new Ext.form.DateField({
                selectOnFocus: true
            })),
            string: new Ext.grid.GridEditor(new Ext.form.TextField({
                selectOnFocus: true
            })),
            object: new Ext.grid.GridEditor(new Ext.form.TextArea({
                width: 250,
                height: 100
            }), {
                constrain: true
            }),
            number: new Ext.grid.GridEditor(new Ext.form.NumberField({
                selectOnFocus: true,
                style: "text-align:left;"
            })),
            color:new Ext.ux.WinEditor(new Ext.ux.WinField({
                triggerClass: "x-form-search-trigger",
                editable: false,
                onTriggerClick: function (e) {
                    var value = this.getValue();

                    var plt = new xds.ColorPalette({
                        ref:'plt',
                        itemCls:'xds-color-palette',
                        colors:['000000','000033','000066','000099','0000CC','0000FF','003300','003333','003366','003399','0033CC','0033FF','006600','006633','006666','006699','0066CC','0066FF','009900','009933','009966','009999','0099CC','0099FF','00CC00','00CC33','00CC66','00CC99','00CCCC','00CCFF','00FF00','00FF33','00FF66','00FF99','00FFCC','00FFFF','330000','330033','330066','330099','3300CC','3300FF','333300','333333','333366','333399','3333CC','3333FF','336600','336633','336666','336699','3366CC','3366FF','339900','339933','339966','339999','3399CC','3399FF','33CC00','33CC33','33CC66','33CC99','33CCCC','33CCFF','33FF00','33FF33','33FF66','33FF99','33FFCC','33FFFF','660000','660033','660066','660099','6600CC','6600FF','663300','663333','663366','663399','6633CC','6633FF','666600','666633','666666','666699','6666CC','6666FF','669900','669933','669966','669999','6699CC','6699FF','66CC00','66CC33','66CC66','66CC99','66CCCC','66CCFF','66FF00','66FF33','66FF66','66FF99','66FFCC','66FFFF','990000','990033','990066','990099','9900CC','9900FF','993300','993333','993366','993399','9933CC','9933FF','996600','996633','996666','996699','9966CC','9966FF','999900','999933','999966','999999','9999CC','9999FF','99CC00','99CC33','99CC66','99CC99','99CCCC','99CCFF','99FF00','99FF33','99FF66','99FF99','99FFCC','99FFFF','CC0000','CC0033','CC0066','CC0099','CC00CC','CC00FF','CC3300','CC3333','CC3366','CC3399','CC33CC','CC33FF','CC6600','CC6633','CC6666','CC6699','CC66CC','CC66FF','CC9900','CC9933','CC9966','CC9999','CC99CC','CC99FF','CCCC00','CCCC33','CCCC66','CCCC99','CCCCCC','CCCCFF','CCFF00','CCFF33','CCFF66','CCFF99','CCFFCC','CCFFFF','FF0000','FF0033','FF0066','FF0099','FF00CC','FF00FF','FF3300','FF3333','FF3366','FF3399','FF33CC','FF33FF','FF6600','FF6633','FF6666','FF6699','FF66CC','FF66FF','FF9900','FF9933','FF9966','FF9999','FF99CC','FF99FF','FFCC00','FFCC33','FFCC66','FFCC99','FFCCCC','FFCCFF','FFFF00','FFFF33','FFFF66','FFFF99','FFFFCC','FFFFFF']
                    });

                    plt.on('select',function(me,color){
                        var win = me.refOwner;
                        win.fireEvent('ok',color);
                        win.close();
                    });

                    plt.on('over',function(me,color){
                        var win = me.refOwner;
                        win.setTitle('选择颜色['+color+']');
                    });

                    var colorWin = new Ext.Window({
                        title:'选择颜色',
                        width:339,
                        height:280,
                        modal:true,
                        resizable:false,
                        closable:false,
                        items:[plt],
                        buttons:[{
                            ref:'../btnCancel',
                            text:"取消",
                            handler:function(btn){
                                var win = btn.refOwner;
                                win.fireEvent('cancel');
                                win.close();
                            }
                        }]
                    });
                    colorWin.addEvents('ok','cancel');
                    this.editWin = colorWin;
                    colorWin.on('ok', function (value) {
                        delete this.editWin;
                        this.setValue(value);
                        this.fireEvent('valueupdated');
                    }, this);
                    colorWin.on('cancel', function () {
                        delete this.editWin;
                        this.fireEvent('cancel');
                    }, this);
                    Ext.select('.el-film').hide();
                    colorWin.on('close', function () {
                        Ext.select('.el-film').show();
                    });
                    colorWin.show();
                }
            })),
            icon:new Ext.ux.WinEditor(new Ext.ux.WinField({
                triggerClass: "x-form-search-trigger",
                editable: false,
                onTriggerClick: function (e) {
                    var value = this.getValue();

                    var iconView = new Ext.DataView({
                        store: {
                            xtype: "jsonstore",
                            idProperty: "name",
                            root: "icons",
                            autoLoad: true,
                            url: "/tplt/icons.json",
                            fields:[{
                                name:"name",
                                type:"string"
                            },{
                                name:"url",
                                type:"string"
                            },{
                                name:"cls",
                                type:"string"
                            }]
                        },
                        tpl: '<div class="xds-designer"><tpl for="."><div class="thumb-wrap" id={name}><div class="thumb"><img src=/tplt/{url} title={name} style="width:16px;height:16px"/></div><span class="x-editable" style="width:16px;">{shortName}</span></div></tpl><div class="x-clear"></div></div>',
                        overClass: "x-view-over",
                        itemSelector: "div.thumb-wrap"
                    });

                    var iconWin = new Ext.Window({
                        title:'选择图标',
                        width:416,
                        height:276,
                        modal:true,
                        resizable:false,
                        closable:false,
                        autoScroll:true,
                        items:[iconView],
                        buttons:[{
                            ref:'../btnCancel',
                            text:"取消",
                            handler:function(btn){
                                var win = btn.refOwner;
                                win.fireEvent('cancel');
                                win.close();
                            }
                        }]
                    });
                    iconWin.addEvents('ok','cancel');
                    this.editWin = iconWin;
                    iconWin.on('ok', function (value) {
                        delete this.editWin;
                        this.setValue(value);
                        this.fireEvent('valueupdated');
                    }, this);
                    iconWin.on('cancel', function () {
                        delete this.editWin;
                        this.fireEvent('cancel');
                    }, this);
                    iconView.on('dblClick',function(dataview,idx,el,evt){
                    	var item = dataview.getRecord(el);
                    	if(!Ext.isEmpty(item)){
                    		this.editWin.close();
                    		delete this.editWin;

                    		this.setValue(item.data.cls);
                    		this.fireEvent('valueupdated');
                    	}
                    },this);
                    Ext.select('.el-film').hide();
                    iconWin.on('close', function () {
                        Ext.select('.el-film').show();
                    });
                    iconWin.show();
                }
            })),
            text:new Ext.ux.WinEditor(new Ext.ux.WinField({
                triggerClass: "x-form-search-trigger",
                editable: false,
                onTriggerClick: function (e) {
                    var value = this.getValue();
                    if (typeof value == 'object') {
                        if (value.toString){
                        	value = value.toString();
                        }else{
                        	value = '[Object]';
                        }
                    }
                    var scriptWin = new Ext.Window({
                    	title:'[Text]',
                    	width:800,
                    	height:600,
                    	modal:true,
                    	closable:false,
                    	layout:'fit',
                    	items:[new Ext.form.TextArea({
                    		ref:'field',
                    		style:'border:none;font-family:consolas;'
                    	})],
                    	buttons:[{
                    		ref:'../btnOk',
                    		text:"确定",
                    		handler:function(btn){
                    			var win = btn.refOwner;
                    			win.fireEvent('ok',win.field.getValue());
                    			win.close();
                    		}
                    	},{
                    		ref:'../btnCancel',
                    		text:"取消",
                    		handler:function(btn){
                    			var win = btn.refOwner;
                    			win.fireEvent('cancel');
                    			win.close();
                    		}
                    	}]
                    });
                    scriptWin.addEvents('ok','cancel');
                    this.editWin = scriptWin;
                    scriptWin.on('ok', function (value) {
                        delete this.editWin;
                        this.setValue(value);
                        this.fireEvent('valueupdated');
                    }, this);
                    scriptWin.on('cancel', function () {
                        delete this.editWin;
                        this.fireEvent('cancel');
                    }, this);
                    Ext.select('.el-film').hide();
                    scriptWin.on('close', function () {
                        Ext.select('.el-film').show();
                    });
                    scriptWin.show();
                    scriptWin.field.setValue(value);
                }
            })),
            file:new Ext.ux.WinEditor(new Ext.ux.WinField({
                triggerClass: "x-form-search-trigger",
                editable: false,
                onTriggerClick: function (e) {
                    var value = this.getValue();
                    if (typeof value == 'object') {
                        if (value.toString){
                        	value = value.toString();
                        }else{
                        	value = '[Object]';
                        }
                    }
                    var uploadWin = new Ext.Window({
                    	title:'[File upload]',
                    	width:400,
                    	modal:true,
                    	closable:false,
                    	autoHeight:true,
                    	resizable:false,
                    	layout:'fit',
                    	items:[new Ext.form.FormPanel({
                    		padding:6,
                    		autoHeight:true,
                    		fileUpload:true,
                    		ref:'form',
                    		border:false,
                    		items:[{
                    			xtype:'fileuploadfield',
                    			anchor:'100%',
                    			fieldLabel:'选择文件',
                    			buttonText:'选择...',
                    			ref:'file',
                    			name:'file'
                    		}]
                    	})],
                    	buttons:[{
                    		ref:'../btnOk',
                    		text:"上传",
                    		handler:function(btn){
                    			var win = btn.refOwner;
                    			var fileUploadMask = new Ext.LoadMask(Ext.getBody(),"文件上传中...");
                    			fileUploadMask.show();

                    			var form = win.form.getForm();
                    			Ext.Ajax.request({
                    				url: '/code/upload',
                    		        method: 'POST',
                    		        form: form.el.dom,
                    		        isUpload: true,
                    		        success: function(response) {
                    		        	 var respStr = response.responseXML.body.firstChild.innerHTML;
                    		             try {
                    		                 var resp = Ext.decode(respStr);
                    		                 if (resp.success) {
                    		                	 if(resp.fileId){
                    		                		 win.fireEvent('ok',resp.fileId);
                    		                		 fileUploadMask.hide();
                    		                		 win.close();
                    		                	 }
                    		                 }
                    		             } catch(e) {
                    		            	 fileUploadMask.hide();
                		                	 Ext.Msg.alert('错误','文件上传失败！');
                		                 }
                    		        }
                    			});
                    		}
                    	},{
                    		ref:'../btnCancel',
                    		text:"取消",
                    		handler:function(btn){
                    			var win = btn.refOwner;
                    			win.fireEvent('cancel');
                    			win.close();
                    		}
                    	}]
                    });
                    uploadWin.addEvents('ok','cancel');
                    this.editWin = uploadWin;
                    uploadWin.on('ok', function (value) {
                        delete this.editWin;
                        this.setValue(value);
                        this.fireEvent('valueupdated');
                    }, this);
                    uploadWin.on('cancel', function () {
                        delete this.editWin;
                        this.fireEvent('cancel');
                    }, this);
                    Ext.select('.el-film').hide();
                    uploadWin.on('close', function () {
                        Ext.select('.el-film').show();
                    });
                    uploadWin.show();
                }
            })),
            fn: new Ext.ux.WinEditor(new Ext.ux.WinField({
                triggerClass: "x-form-search-trigger",
                editable: false,
                onTriggerClick: function (e) {
                    var value = this.getValue();
                    if (typeof value == 'object') {
                        value = value.value;
                    }
                    var scriptWin = new Ext.ux.CodeWindow({
                        params: this.params,
                        help: this.help,
                        sourceCode: value
                    });
                    this.editWin = scriptWin;
                    scriptWin.on('ok', function (value) {
                        delete this.editWin;
                        this.setValue(value);
                        this.fireEvent('valueupdated');
                    }, this);
                    scriptWin.on('cancel', function () {
                        delete this.editWin;
                        this.fireEvent('cancel');
                    }, this);
                    Ext.select('.el-film').hide();
                    scriptWin.on('close', function () {
                        Ext.select('.el-film').show();
                    });
                    scriptWin.show();
                }
            }))
        });
    }
};
xds.Config.prototype = {
    name: "",
    defautValue: "",
    type: "String",
    htmlEncode: true,
    editor: "string",
    setFn: "setConfig",
    getFn: "getConfigValue",
    getValue: function (a) {
        return a[this.getFn](this.name);
    },
    setValue: function (f, d) {
        var a = f[this.getFn](this.name);
        f[this.setFn](this.name, d);
        if (String(a) !== String(d)) {
            var b = f.getTopComponent();
            if (typeof this.updateFn == "string") {
                b.takeSnapshot();
                var e = f.getExtComponent();
                e[this.updateFn](d);
                f.syncFilm();
            } else {
                if (typeof this.updateFn == "function") {
                    b.takeSnapshot();
                    this.updateFn(f.getExtComponent(), d, f);
                    f.syncFilm();
                } else {
                    xds.fireEvent("componentchanged");
                }
            }
        }
    },
    getEditor: function () {
        if (this.editor == "options") {
            var a = xds.Config.editors.options;
            var data = [];
            if (this.options) {
            	data = this.options;
            } else if (typeof(this.getOptions) == 'function') {
            	data = this.getOptions();
            }

            if(Ext.isArray(data)){
            	for (var i=0;i<data.length;i++){
            		if(!Ext.isArray(data[i])){
            			data[i]=[data[i],data[i]];
            		}
            	}
            }

            a.field.store.loadData(data);
            return a;
        } else if (this.editor == 'fn') {
            var editor = xds.Config.editors.fn;
            editor.field.params = this.params || [];
            editor.field.help = this.help || '';
            return editor;
        }
        return xds.Config.editors[this.editor];
    },
    render: function (a, c, b) {
        return a;
    }
};
xds.Config.String = Ext.extend(xds.Config, {
    type: "String",
    defaultValue: "",
    htmlEncode: true,
    editor: "string"
});
xds.Config.Number = Ext.extend(xds.Config, {
    type: "Number",
    defaultValue: 0,
    htmlEncode: false,
    editor: "number"
});
xds.Config.Boolean = Ext.extend(xds.Config, {
    type: "Boolean",
    defaultValue: false,
    editor: "boolean",
    htmlEncode: false,
    render: function (a, c, b) {
        a = a === undefined ? this.defaultValue : a;
        return '<span class="bcheck"><input type="checkbox" class="' + b.id + '"' + (a ? " checked" : "") + "></span>";
    }
});
xds.Config.Fn = Ext.extend(xds.Config, {
    type: "Fn",
    setFn: "setEventHandler",
    getFn: "getEventHandler",
    defaultValue: "...",
    editor: "fn"
});

xds.Config.Color = Ext.extend(xds.Config, {
    type: "Color",
    defaultValue: "...",
    editor: "color"
});

xds.Config.Icon = Ext.extend(xds.Config, {
    type: "Icon",
    defaultValue: "...",
    editor: "icon"
});

xds.Config.Text = Ext.extend(xds.Config, {
    type: "Text",
    defaultValue: "...",
    editor: "text"
});

xds.Config.File = Ext.extend(xds.Config, {
    type: "File",
    defaultValue: "...",
    editor: "file"
});

xds.Config.Object = Ext.extend(xds.Config.String, {
    type: "Object",
    defaultValue: null,
    editor: "object",
    render: function () {
        return "[object]...";
    },
    getValue: function (b) {
        var a = xds.Config.Object.superclass.getValue.call(this, b);
        if (typeof a === "object") {
            a = Ext.encode(a);
        }
        return a;
    },
    setValue: function (c, value) {
        if (typeof value != "object") {
            value = Ext.util.Format.trim(value);
            var o;
            eval("o = " + (value.length > 0 ? value : "null") + ";");
            c.setConfig(this.name, o);
        } else {
            c.setConfig(this.name, value);
        }
        xds.fireEvent("componentchanged");
    }
});
xds.Config.Array = Ext.extend(xds.Config.Object, {

});
xds.Config.types = {
    string: xds.Config.String,
    number: xds.Config.Number,
    "boolean": xds.Config.Boolean,
    object: xds.Config.Object,
    fn: xds.Config.Fn,
    color: xds.Config.Color,
    icon: xds.Config.icon,
    text:xds.Config.Text,
    file:xds.Config.File
};
xds.Config.editors = {};
xds.editorConfigs = new Ext.util.MixedCollection(false, function (a) {
    return a.name;
});
xds.editorConfigs.addAll([
	new xds.Config.String({
	    name: "name",
	    ctype: "string",
	    text: "名称",
	    group: "(Designer)",
	    getValue: function (a) {
	        return a.name || a.id;
	    },
	    setValue: function (e, b) {
	        var a = e.id;
	        var d = xds.inspector.getNodeById(e.id);
	        if (xds.canvas.selectedId == a) {
	            xds.canvas.selectedId = b;
	        }
	        //d.setNodeId(b);
	        d.setText(b);
	    }
	})
//	new xds.Config.String({
//	    name: "userXType",
//	    ctype: "string",
//	    group: "(Designer)",
//	    getValue: function (a) {
//	        return a.userXType;
//	    },
//	    setValue: function (b, a) {
//	        b.userXType = a;
//	    }
//	})
]);
xds.dockConfigs = new Ext.util.MixedCollection(false, function (dock) {
    return dock.name;
});
xds.dockConfigs.addAll([
	new xds.Config.String({
	    name: "dock",
	    ctype: "string",
	    group: "(Designer)",
	    editor: "options",
	    options: ["(none)", "bbar", "tbar", "fbar"],
	    getValue: function (a) {
	        return a.dock;
	    },
	    setValue: function (b, a) {
	        if (a == "(none)") {
	            a = undefined;
	        }
	        b.dock = a;
	        b.setSuffix(a);
	        xds.fireEvent("componentchanged");
	    }
	})
]);
xds.commonConfigs = ["id", "itemId", "title", "text", "layout", "width", "height", "autoScroll", "url", "name", "fieldLabel", "iconCls","permissionId"];
xds.actions = {
    saveAction: new Ext.Action({
        iconCls: "icon-project-save",
        itemText: "Save Project",
        tooltip: "Save Project",
        text: 'Save',
        disabled: true,
        handler: function () {
            xds.project.save();
            xds.actions.saveAction.disable();
            //xds.actions.saveAction.enable();
        }
    }),
    saveAsAction: new Ext.Action({
        iconCls: "icon-project-save-as",
        tooltip: "Save Project As...",
        text: 'SaveAs',
        itemText: "Save Project As...",
        disabled: true,
        handler: function () {
            xds.project.saveAs();
        }
    }),
    newAction: new Ext.Action({
        iconCls: "icon-project-new",
        itemText: "New Project",
        tooltip: "New Project",
        handler: function () {
            xds.project.close(function () {
                var a = new xds.Project();
                a.open();
            });
        }
    }),
    openAction: new Ext.Action({
        iconCls: "icon-project-open",
        text: "Open local",
        tooltip: "Open Project",
        handler: function () {
            var cfgWin = new Ext.Window({
                title: 'Component Config',
                icon: 'icon-component',
                layout: 'border',
                width: 600,
                modal: true,
                height: 450,
                items: [{
                    layout: 'form',
                    region: 'center',
                    border: false,
                    hideLabels: true,
                    items: [{
                        id: 'configArea',
                        xtype: 'textarea',
                        style: 'border:none;font-family:"consolas","courier new","segoe ui";',
                        anchor: '0 0'
                    }]
                }],
                buttons: [{
                    text: 'Open',
                    handler: function () {
                        xds.project.close(function () {
                            var cfgArea = Ext.getCmp('configArea');
                            if (!Ext.isEmpty(cfgArea)) {
                                var cfgStr = cfgArea.getValue();
                                var cfg = Ext.decode(cfgStr);
                                if (xds.project) {
                                    xds.project.close(function () {
                                        new xds.Project().open(cfg);
                                    });
                                }
                                cfgWin.close();
                            }
                        });
                    }
                },{
                    text: 'Cancel',
                    handler: function () {
                        cfgWin.close();
                    }
                }]
            });
            Ext.select('.el-film').hide();
            cfgWin.on('close', function () {
                Ext.select('.el-film').show();
            });
            cfgWin.show();
        }
    }),
    openAction2: new Ext.Action({
        iconCls: "icon-project-open",
        text: "Open remote",
        tooltip: "Open Project",
        handler: function () {
            var pwin = new xds.PWindow({
                modal: true
            });
            Ext.select('.el-film').hide();
            pwin.on('close', function () {
                Ext.select('.el-film').show();
            });
            pwin.show();
        }
    }),
    newCmpAction: new Ext.Action({
        iconCls: "icon-cmp-new",
        tooltip: "New Component",
        itemText: "New Component...",
        handler: function () {
            var a = new xds.CWindow({
                title: "New Component"
            });
            Ext.select('.el-film').hide();
            a.on('close', function () {
                Ext.select('.el-film').show();
            });
            a.show();
        }
    }),
    deleteCmpAction: new Ext.Action({
        iconCls: "icon-cmp-delete",
        tooltip: "Delete Component",
        disabled: true,
        itemText: "Delete Component",
        handler: function () {
            xds.inspector.removeComponent(xds.active.component);
        }
    }),
    undo: new Ext.Action({
        iconCls: "icon-undo",
        tooltip: "Undo last change (Ctrl+Z)",
        text: "Undo",
        disabled: true,
        itemText: "Undo",
        handler: function () {
            xds.inspector.back();
        }
    }),
    redo: new Ext.Action({
        iconCls: "icon-redo",
        tooltip: "Redo last change (Ctrl+Shift+Z)",
        disabled: true,
        text: "Redo",
        itemText: "Redo",
        handler: function () {
            xds.inspector.forward();
        }
    }),
    preview: new Ext.Action({
        iconCls: "icon-preview",
        tooltip: "Preview Component",
        text: "Preview",
        disabled: true,
        handler: function () {
            var preConfig = xds.active.topNode.component.getJsonConfig(true);
            preConfig = Ext.ux.CodeGenerator.parse_object(preConfig, true);
            //Ext.log(JSON.stringify(preConfig));
            var previewWin = null;
            if (preConfig.xtype && preConfig.xtype == 'window') {
                previewWin = new Ext.Window(Ext.apply(preConfig, {
                    modal: true
                }));
            } else {
                previewWin = new Ext.Window({
                    modal: true,
                    padding: 12,
                    iconCls: 'icon-preview',
                    maximized: true,
                    items: [preConfig]
                });
            }
            Ext.select(".el-film").hide();
            previewWin.on('close', function () {
                Ext.select(".el-film").show();
            });
            previewWin.show();
        }
    }),
    rtConfig:new Ext.Action({
    	text: "RTConfig",
    	handler: function(){
    		var s;
    		var win=new Ext.Window({
    			modal:true,
    			title:"RTConfig",
    			width:900,
    			height:600,
    			bodyStyle:'background-color:#ffffff;',
    			autoScroll:true,
    			fbar:[{text:'Cancel',ref:'../btnCancel',handler:function(btn){btn.refOwner.close();}}]
    		});
            Ext.select(".el-film").hide();
            win.on('close', function () {
                Ext.select(".el-film").show();
            });
    		win.show();
    		s=js_beautify(xds.inspector.root.module.getModuleConfig(), {
                opt_keep_array_indentation: false
            });
    		win.body.update("<pre class='brush:js' style='font-size:12px;font-family:\"consolas\",\"courier new\",\"segoe ui\";'>" + s + "</pre>");

    		SyntaxHighlighter.highlight({toolbar:false});
    	}
    })
};
xds.Canvas = Ext.extend(Ext.Panel, {
    constructor: function () {
        xds.canvas = this;
        xds.on("componentselect", this.onComponentSelect,this);
        xds.Canvas.superclass.constructor.call(this, {
            id: "canvas",
            region: "center",
            baseCls: "x-plain",
            layout: "auto",
            bodyStyle: "padding:5px;position:relative;left:0;top:0;",
            items: new Ext.Panel({
                baseCls: "x-plain"
            }),
            autoScroll: true,
            bregion: new Ext.lib.Region(0, 0, 0, 0),
            rregion: new Ext.lib.Region(0, 0, 0, 0),
            cregion: new Ext.lib.Region(0, 0, 0, 0)
        });
    },
    afterRender: function () {
        xds.Canvas.superclass.afterRender.call(this);
        this.body.on("mousedown", this.onBodyMouseDown, this);
        this.body.on("click", this.onBodyClick, this);
        this.body.on("contextmenu", this.onBodyContextMenu, this);
        this.body.on("mousemove", this.onBodyMove, this);
        this.body.on("dblclick", this.onBodyDblClick, this);
        this.dropZone = new xds.Canvas.DropZone(this);
        this.dragTracker = new xds.Canvas.DragTracker({
            el: this.body
        });
    },
    onBodyMouseDown: function (b, a, d) {
        var d = this.findTarget(b);
        if (d) {
            d.component.onFilmMouseDown(b);
        }
    },
    onBodyDblClick: function (b, a) {
        var d = this.findTarget(b);
        if (d) {
            d.component.onFilmDblClick(b);
        }
    },
    onBodyClick: function (d) {
        if (d.target == this.body.dom) {
            xds.inspector.getSelectionModel().clearSelections();
            return;
        }
        var f = this.findTarget(d);
        if (f) {
            var a = f.component.selected;
            if (f.component.onFilmClick(d, a) !== false) {
                xds.fireEvent("componentclick", {
                    component: f.component,
                    node: f,
                    event: d
                });
            }
        }
    },
    onBodyMove: function (evtObj, k) {
        if ((k = evtObj.getTarget(".el-film", 3))) {
            var tgtCmp = this.getTargetComponent(k);
            if (tgtCmp) {
                var film = Ext.get(k);
                var lr = film.lastRegion;
                var d = 7;
                var m = tgtCmp.component.isResizable("Corner", evtObj);

                if (m && (evtObj.browserEvent.offsetY > lr.bottom - lr.top -d) && (evtObj.browserEvent.offsetX > lr.right - lr.left -d)) {
                    this.dragTracker.setDragMode("Corner");
                    film.setStyle("cursor", "se-resize");
                    return;
                }
                if ((evtObj.browserEvent.offsetY > lr.bottom - lr.top -d)  && tgtCmp.component.isResizable("Bottom", evtObj)) {
                    this.dragTracker.setDragMode("Bottom");
                    film.setStyle("cursor", "s-resize");
                    return;
                }

                if ((evtObj.browserEvent.offsetX > lr.right - lr.left -d) && tgtCmp.component.isResizable("Right", evtObj)) {
                    this.dragTracker.setDragMode("Right");
                    film.setStyle("cursor","e-resize");
                    return;
                }

                film.setStyle("cursor", "default");
            }
            this.dragTracker.setDragMode("Absolute");
        }
    },
    onBodyContextMenu: function (evt) {
        var f = this.findTarget(evt);
        if (f) {
            var a = f.component;
            this.getContextMenu(a).showAt(evt.getXY());
        }
        evt.preventDefault();
    },
    beforeTransformShow: function () {
        this.transformMenu.removeAll(true);
        var a = this.contextMenu.component.getTransforms();
        if (a.length === 0) {
            a.push({
                text: "None Available",
                disabled: true
            });
        }
        this.transformMenu.add(a);
    },
    onTransformClick: function (b) {
        var c = this.contextMenu.component.node;
        var a = b.transtype;
        xds.inspector.transform(c, a);
    },
    getContextMenu:function(target){
        if (!this.contextMenu) {
            var b = this.transformMenu = new Ext.menu.Menu({
                items: []
            });
            b.on("beforeshow", this.beforeTransformShow, this);
            b.on("itemclick", this.onTransformClick, this);
            var a = this.contextMenu = new Ext.menu.Menu({
                zIndex: 80000,
                items: [{
                    text: "Duplicate",
                    handler: function () {
                        var f = this.contextMenu.component.node;
                        var e = this.contextMenu.component.getInternals(true);
                        var c = xds.inspector.getDropPosition(f, "below");
                        var d = xds.inspector.restore(e, c.parent, c.before);
                        d.select();
                        xds.fireEvent("componentchanged");
                    },
                    scope: this
                },{
                    text: "Transform to",
                    menu: this.transformMenu
                },
                "-", {
                    text: "Delete",
                    iconCls: "icon-delete",
                    handler: function () {
                        xds.inspector.removeComponent(this.contextMenu.component);
                        xds.fireEvent("componentchanged");
                    },
                    scope:this
                }]
            });
        }
        this.contextMenu.component = target;
        return this.contextMenu;
    },
    beginUpdate: function () {
        this.updating = true;
    },
    endUpdate: function (a) {
        this.updating = false;
        if (this.updateCmp && a !== true) {
            this.setComponent(this.updateCmp);
        }
    },
    setComponent: function (a) {
        if (!this.setComponentTask) {
            this.setComponentTask = new Ext.util.DelayedTask(this.doSetComponent, this);
        }
        if (!xds.syncMask) {
            xds.syncMask = new Ext.LoadMask(Ext.getBody(), {
                msg: "Please wait..."
            });
        }
        xds.syncMask.show();
        this.setComponentTask.delay(100, null, null, [a]);
    },
    doSetComponent: function (b) {
        if (this.updating) {
            this.updateCmp = b;
            return
        }

        var newCanvasConfig;
        var scrollTop = 0;
        if(this.body.dom){
        	scrollTop=this.body.dom.scrollTop;
        }
        if (b && b.getOwnerTree) {
        	newCanvasConfig = this.createConfig(b);
        }
        var a = this.items.items[0];
        if (a) {
            if (a.viewerNode) {
                a.viewerNode.component.beforeRemove();
            }
            this.remove(a, true);
        }

        if (newCanvasConfig) {
            var d = this.add(newCanvasConfig);
            Ext.lib.Event.suspend();
            try {
                this.doLayout();
                this.body.dom.scrollTop=scrollTop;
            } catch (e) {
            	if(console){
            		console.log(e);
            	}
            }
//            d.show();
            Ext.lib.Event.resume();
            this.syncAll.defer(10, this);
        }
        if (xds.syncMask) {
            xds.syncMask.hide();
        }
    },
    clear: function () {
        this.setComponent(null);
    },
    setComponentFromNode: function (a) {
        this.setComponent(this.createConfig(a));
    },
    createConfig: function (a) {
        return a.component.createCanvasConfig(a);
    },
    onComponentSelect: function (a) {
        this.setSelected(a.node ? a.node.id : null);
        if (a.component && this.editData && a.component != this.editData.component) {
            this.stopEdit();
        }
    },
    setSelected: function (d) {
        if (this.selectedId != d) {
            var a = Ext.get("film-for-" + this.selectedId);
            if (a && a.dom) {
                a.removeClass("el-film-selected");
                a.setStyle(a.getStyle("z-index") - 1);
            }
        }
        this.selectedId = d;
        if (d) {
            var b = Ext.get("film-for-" + this.selectedId);
            if (b) {
                b.addClass("el-film-selected");
                b.setStyle(b.getStyle("z-index") + 1);
            }
        }
    },
    syncAll: function () {
        if (xds.active && xds.active.topNode) {
            xds.active.topNode.cascade(function () {
                this.component.syncFilm();
            });
        }
        this.setSelected(this.selectedId);
    },
    getTargetComponent: function (b) {
        if (b.id.indexOf("film-for-") !== - 1) {
            var a = b.id.substr(9);
            return xds.inspector.getNodeById(a);
        }
        return false;
    },
    findTarget: function (evt, b) {
        var a = evt.getTarget(".el-film", 2) || evt.getTarget(".xds-child-target", 2);
        if (a && b !== false) {
            return this.getTargetComponent(a);
        }
        return a;
    },
    getInlineEditor: function () {
        if (!this.inlineEd) {
            this.inlineEd = new Ext.Editor({
                alignment: "l-l?",
                completeOnEnter: true,
                autoSize: "width",
                zIndex: 60000,
                shadow: "drop",
                shadowOffset: 3,
                cls: "x-small-editor",
                field: {
                    selectOnFocus: true
                },
                ignoreNoChange: false,
                doAutoSize: function () {
                    if (typeof this.requestedWidth == "number") {
                        this.setSize(this.requestedWidth);
                    } else {
                        this.setSize(this.boundEl.getWidth());
                    }
                }
            });
            this.inlineEd.on("complete", this.onEditComplete, this);
        }
        return this.inlineEd;
    },
    stopEdit: function () {
        if (this.inlineEd && this.inlineEd.editing) {
            this.inlineEd.completeEdit();
        }
    },
    startEdit: function (f, e, a, c) {
        var g = this.editData;
        if (g && g.component == f && g.el == e && g.config == a) {
            return
        }
        this.stopEdit();
        this.editData = {
            component: f,
            el: e,
            config: a
        };
        var b = this.getInlineEditor();
        b.requestedWidth = c;
        b.startEdit(e, a.getValue(f));
    },
    onEditComplete: function (a, b, c) {
        if (String(b) != String(c)) {
            if (xds.active && xds.active.component == this.editData.component) {
                xds.props.setValue(this.editData.config.name, b);
            } else {
                this.editData.config.setValue(this.editData.component, b);
            }
        }
        delete this.editData;
    }
});
xds.Canvas.DropZone = Ext.extend(Ext.dd.DropZone, {
    constructor: function (a) {
        this.allowContainerDrop = false;
        xds.Canvas.DropZone.superclass.constructor.call(this, a.bwrap, {});
        this.canvas = a;
        this.dragOverData = {};
        this.lastInsertClass = "xds-no-status";
    },
    ddGroup: "TreeDD",
    getTargetFromEvent: function (a) {
        return a.getTarget(".xds-child-target", 2) || a.getTarget(".el-film", 2) || this.canvas;
    },
    isValidDropPoint: function (g, b, d) {
        var a = g ? g.component : null;
        var f = b.node.component || b.node.instance;
        return xds.types.BaseType.isValidDrop(a, f);
    },
    onNodeEnter: function (d, a, c, b) {},
    onNodeOver: function (h, a, f, d) {
        var g = this.canvas.getTargetComponent(h);
        if (h == this.canvas) {
            return this.isValidDropPoint(g, d, f) ? "xds-dd-new" : this.dropNotAllowed;
        }
        var b = d.node;
        if (this.isValidDropPoint(g, d, f)) {
            return "xds-dd-add";
        } else {
            return this.dropNotAllowed;
        }
    },
    onNodeOut: function (d, a, c, b) {},
    onNodeDrop: function (dropTarget, dragSource, evt, data) { //h:dropTarget;a:dragSource;f:evt;d:data
        var tgtCom = dropTarget == this.canvas ? null : this.canvas.getTargetComponent(dropTarget); //g:tgtCom
        var node = data.node; //b:node
        if (this.isValidDropPoint(tgtCom, data, evt)) {
            this.canvas.lastDropPoint = evt.getPoint();
            xds.fireEvent("componentevent", {
                type: node.component ? "move" : "new",
                parentId: tgtCom ? tgtCom.id : null,
                component: node.component ? node.component : node.instance.getSpec(tgtCom)
            });
            delete this.canvas.lastDropPoint;
            return true;
        } else {
            return false;
        }
    }
});
xds.Canvas.DragTracker = Ext.extend(Ext.dd.DragTracker, {
    autoStart: true,
    preventDefault: false,
    dragMode: "Absolute",
    setDragMode: function (a) {
        if (!this.active && !this.waiting) {
            this.dragMode = a;
        }
    },
    onMouseUp: function (a) {
        this.waiting = false;
        xds.Canvas.DragTracker.superclass.onMouseUp.call(this, a);
    },
    isAbsolute: function (a) {
    	if(a.component.getConfigValue("locked")) return false;
        return (a.component.owner && a.component.owner.getConfigValue("layout") == "absolute");
    },
    onBeforeStart: function (b) {
        var a = b.getTarget(".el-film", 2);
        this.snapValue = false;
        if (a) {
            this.node = xds.canvas.getTargetComponent(a);
            this.cmp = this.node.component;
            if (this.dragMode == "Absolute") {
                if (this.isAbsolute(this.node)) {
                    this.pos = this.cmp.getExtComponent().getPosition(true);
                    this.snapValue = this.node.component.owner.snapToGrid;
                    this.startX = this.pos[0];
                    this.startY = this.pos[1];
                    this.waiting = true;
                    return true;
                }
            } else {
                this.startSize = this.cmp.getExtComponent().getSize();
                this.waiting = true;
                if (this.isAbsolute(this.node)) {
                    this.snapValue = this.node.component.owner.snapToGrid;
                }
                return true;
            }
        }
        return false;
    },
    onStart: function (a) {
        this.waiting = false;
        this.node.select();
        this.cmp.getExtComponent().film.addClass("el-film-drag");
    },
    onDrag: function (a) {
        this["onDrag" + this.dragMode](a, this.getOffset(), this.cmp.getExtComponent());
    },
    onDragAbsolute: function (b, c, a) {
        a.setPosition(this.snap(this.startX - c[0]), this.snap(this.startY - c[1]));
        a.syncFilm();
    },
    onDragRight: function (b, c, a) {
        a.setWidth(Math.max(this.cmp.minWidth, this.snap(this.startSize.width - c[0])));
        a.syncFilm();
    },
    onDragBottom: function (b, c, a) {
    	var height = Math.max(this.cmp.minHeight, this.snap(this.startSize.height - c[1]));
        a.setHeight(height);
        a.syncFilm();
    },
    onDragCorner: function (b, c, a) {
        a.setSize(Math.max(this.cmp.minWidth, this.snap(this.startSize.width - c[0])), Math.max(this.cmp.minHeight, this.snap(this.startSize.height - c[1])));
        a.syncFilm();
    },
    onEnd: function (b) {
        var a = this.cmp.getExtComponent();
        a.film.removeClass("el-film-drag");
        this["onEnd" + this.dragMode](b, this.getOffset(), a);
        if (a.ownerCt && a.ownerCt.layout) {
            delete a.anchorSpec;
            a.ownerCt.doLayout();
        }
    },
    onEndAbsolute: function (b, c, a) {
        var d = a.getPosition(true);
        d[0] = this.snap(d[0]);
        d[1] = this.snap(d[1]);
        xds.canvas.beginUpdate();
        this.cmp.setConfig("x", d[0]);
        this.cmp.setConfig("y", d[1]);
        xds.props.setValue("x", d[0]);
        xds.props.setValue("y", d[1]);
        xds.canvas.endUpdate(true);
        xds.fireEvent("componentchanged");
    },
    onEndRight: function (c, d, b) {
        xds.canvas.beginUpdate();
        var a = b.getWidth();
        this.cmp.setConfig("width", a);
        xds.props.setValue("width", a);
        xds.canvas.endUpdate(true);
        xds.fireEvent("componentchanged");
    },
    onEndBottom: function (c, d, b) {
        xds.canvas.beginUpdate();
        var a = b.getHeight();
        this.cmp.setConfig("height", a);
        xds.props.setValue("height", a);
        xds.canvas.endUpdate(true);
        xds.fireEvent("componentchanged");
    },
    onEndCorner: function (d, f, c) {
        xds.canvas.beginUpdate();
        var b = c.getWidth();
        this.cmp.setConfig("width", b);
        xds.props.setValue("width", b);
        var a = c.getHeight();
        this.cmp.setConfig("height", a);
        xds.props.setValue("height", a);
        xds.canvas.endUpdate(true);
        xds.fireEvent("componentchanged");
    },
    snap: function (c, b) {
        b = b || this.snapValue;
        if (b < 1 || !c) {
            return c;
        }
        var e = c,
            d = b;
        var a = c % d;
        if (a > 0) {
            if (a > (d / 2)) {
                e = c + (d - a);
            } else {
                e = c - a;
            }
        }
        return e;
    }
});
xds.ConfigEditor = Ext.extend(Ext.Panel, {
    constructor: function (a) {
        this.grid = new xds.PropGrid();
        this.grid.on("rowcontextmenu", this.onRowContext, this);
        xds.ConfigEditor.superclass.constructor.call(this, Ext.apply({
            id: "props",
            title: "Component Config",
            layout: "fit",
            items: this.grid,
            tools: [{
                id: "expand-all",
                handler: function () {
                    this.grid.view.expandAllGroups();
                },
                qtip: "Expand All",
                scope: this
            },{
                id: "collapse-all",
                handler: function () {
                    this.grid.view.collapseAllGroups();
                },
                qtip: "Collapse All",
                scope: this
            }]
        }, a));
    },
    findRecord: function (b) {
        var a = null;
        this.grid.store.each(function (c) {
            if (c.data.name == b) {
                a = c;
                return false;
            }
        });
        return a;
    },
    addAndEdit: function (c, d, e) {
        var b = xds.active.component.config;
        if (e !== undefined || b[c] === undefined) {
            b[c] = e !== undefined ? this.convertForType(d, e) : this.getDefaultForType(d);
            this.grid.setComponent(b);
        }
        if (e === undefined) {
            var a = this.findRecord(c);
            if (a) {
                this.grid.startEditing.defer(10, this.grid, [this.grid.store.indexOf(a), 1]);
            }
        } else {
            xds.fireEvent("componentchanged");
        }
    },
    getDefaultForType: function (a) {
        a = a.toLowerCase();
        switch (a) {
        case "string":
            return "";
        case "boolean":
            return false;
        case "number":
            return 0;
        default:
            return "";
        }
    },
    convertForType: function (a, b) {
        a = a.toLowerCase();
        switch (a) {
        case "string":
            return "" + b;
        case "boolean":
            return !(b === false || b === "0" || b === "false");
        case "number":
            return b === "" ? 0 : parseInt(b, 10);
        default:
            return b;
        }
    },
    onRowContext: function (a, c, b) {
        if (!this.contextMenu) {
            this.contextMenu = new Ext.menu.Menu({
                items: [{
                    text: "Delete",
                    iconCls: "icon-delete",
                    handler: function () {
                        xds.active.component.getConfigObject(this.contextProperty).setValue(xds.active.component, undefined);
                        this.refresh();
                        xds.fireEvent("componentchanged");
                    },
                    scope: this
                },
                "-", {
                    text: "Refresh values",
                    iconCls: "icon-refresh",
                    handler: this.refresh,
                    scope: this
                }]
            });
        }
        this.contextProperty = this.grid.store.getAt(c).data.name;
        this.contextMenu.items.items[0].setText("Delete " + this.contextProperty);
        this.contextMenu.showAt(b.getXY());
        b.stopEvent();
    },
    refresh: function () {
        if (xds.active) {
            var a = xds.active.component;
            this.grid.setComponent(a);
        } else {
            this.grid.clear();
        }
    },
    setValue: function (c, b) {
        var a = this.grid.propStore.store.getById(c);
        if (a) {
            a.set("value", b);
        }
    }
});
xds.Inspector = Ext.extend(Ext.tree.TreePanel, {
    constructor: function (cfg) {
        Ext.apply(this, cfg);
        xds.Inspector.superclass.constructor.call(this, {
            tools: [{
                id: "expand-all",
                qtip: "Expand All",
                handler: function () {
                    this.root.expand(true);
                },
                scope: this
            },{
                id: "collapse-all",
                qtip: "Collapse All",
                handler: function () {
                    this.root.collapse(true);
                },
                scope: this
            },{
                id: "refresh",
                qtip: "Repaint Canvas",
                handler: function () {
                    xds.fireEvent("componentchanged");
                }
            }],
            keys: [{
                key: Ext.EventObject.DELETE,
                fn: function () {
                    if (xds.active) {
                    	Ext.select('.el-film').hide();
                        Ext.Msg.confirm("删除组件", "确认要删除选择的组件吗?", function (a) {
                            if (a == "yes") {
                                xds.inspector.removeComponent(xds.active.component);
                                xds.fireEvent("componentchanged");
                            }else{
                            	Ext.select('.el-film').show();
                            }
                        });
                    }
                }
            }]
        });
    },
    gotoSpec: function (a) {
        if (xds.active) {
            var c = xds.active.top;
            a = a.constrain(0, c.priorSpecs.length);
            var b = c.priorSpecs[a];
            if (b) {
                c.currentSpec = a;
                xds.actions.undo[c.currentSpec > 0 ? "enable" : "disable"]();
                xds.actions.redo[(c.currentSpec < c.priorSpecs.length - 1) ? "enable" : "disable"]();
                var d = this.restore(b);
                d.component.priorSpecs = c.priorSpecs;
                d.component.currentSpec = c.currentSpec;
                d.component.finalSpec = c.finalSpec;
                d.attributes.inRollbackMode = true;
                this.removeComponent(xds.active.top);
                d.select();
            }
        }
    },
    back: function () {
        if (xds.active) {
            var a = xds.active.top;
            this.gotoSpec(a.currentSpec - 1);
        }
    },
    forward: function () {
        if (xds.active) {
            var a = xds.active.top;
            this.gotoSpec(a.currentSpec + 1);
        }
    },
    initComponent: function () {
        this.loader = new xds.Inspector.DemoLoader();
        this.root = {
            id: "croot",
            async: true,
            expanded: true,
            allowDrag: false,
            text: "NewModule",
            iconCls: 'icon-project',
            allowDrop: false
        };
        this.on("nodedragover", this.onDragOver, this);
        this.on("beforeappend", this.onBeforeAppend, this);
        this.on("beforenodedrop", this.onBeforeDrop, this);
        this.on("nodedrop", this.onAfterDrop, this);
        this.on("contextmenu", this.onNodeContext, this);
        xds.on("componentevent", this.onComponentEvent, this);
        xds.on("componentclick", this.onComponentClick, this);
        this.getSelectionModel().on("selectionchange", this.onInspectorSelectionChange, this);
        xds.Inspector.superclass.initComponent.call(this);
    },
    beforeTransformShow: function () {
        this.transformMenu.removeAll(true);
        var b = this.contextMenu.node;
        var a = b.component.getTransforms();
        if (a.length === 0) {
            a.push({
                text: "None Available",
                disabled: true
            });
        }
        this.transformMenu.add(a);
    },
    onTransformClick: function (b, d) {
        var c = this.contextMenu.node;
        var a = b.transtype;
        this.transform(c, a);
    },
    transform: function (g, i, f, d, c) {
        f = f || g;
        d = d || "below";
        var a = this.getDropPosition(f, d);
        var e;
        var h = g.component;
        var j = xds.Registry.get(i);
        if (j.transform) {
            if (!(e = j.transform(h, j))) {
                e = h.getInternals(true);
            }
            if (e === false) {
                return false;
            }
        } else {
            e = g.component.getInternals(true);
        }
        e.cid = i;
        if (e.name === h.defaultName) {
            e.name = j.prototype.defaultName;
        }
        var b = this.restore(e, a.parent, a.before);
        g.remove();
        if (c !== false) {
            b.select();
            xds.fireEvent("componentchanged");
        }
        return b;
    },
    onInspectorSelectionChange: function (sm, node) { //c:sm,b:node
        if (this.prevSelection) {
            this.prevSelection.onSelectChange(false);
            delete this.prevSelection;
        }
        if (node && node.isRoot) {
            xds.fireEvent("moduleselect");
            return;
        }
        var top;
        if (node) {
            top = node.component.getTopComponent();
        }
        if (node && node.component) {
            node.component.onSelectChange(true);
            this.prevSelection = node.component;
        }
        xds.fireEvent("componentselect", {
            component: node ? node.component : null,
            node: node,
            top: node ? top : null,
            topNode: node ? top.getNode() : null
        });
    },
    onBeforeEdit: function (c, b, a) {
        return !this.getNodeById(b);
    },
    onEdit: function (c, b, a) {
        var d = this.editor.editNode;
    },
    onComponentClick: function (comp) {
        if (comp.node) {
            var node = comp.node;
            node.getOwnerTree().collapseAll();
            node.getOwnerTree().expandPath(node.getPath());
            comp.node.select();
        }
    },
    onBeforeAppend: function (a, b, d) {
        var c;
        if (b.component && (c = b.component.getConfigValue("layout"))) {
            if (xds.Layouts[c] && xds.Layouts[c].onBeforeAdd) {
                xds.Layouts[c].onBeforeAdd(b, d);
            }
        }
    },
    removeComponent: function (b) {
        var a = b.attributes ? b : this.getNodeById(b.id);
        if (a) {
            if (a.isSelected()) {
                if (a.nextSibling) {
                    a.nextSibling.select();
                } else {
                    if (a.previousSibling) {
                        a.previousSibling.select();
                    } else {
                        if (a.parentNode.component) {
                            a.parentNode.select();
                        }

//                        else {
//                            xds.canvas.clear();
//                        }
                    }
                }
            }
            a.parentNode.removeChild(a);
            if (!this.root.hasChildNodes()) {
                xds.canvas.clear();
            }
        }
    },
    getContextMenu: function () {
        if (!this.contextMenu) {
            var b = this.transformMenu = new Ext.menu.Menu({
                items: []
            });
            b.on("beforeshow", this.beforeTransformShow, this);
            b.on("itemclick", this.onTransformClick, this);
            var a = this.contextMenu = new Ext.menu.Menu({
                zIndex: 80000,
                items: [{
                    text: "Select Component",
                    iconCls: "icon-cmp-view",
                    handler: function () {
                        a.node.select();
                    }
                },{
                    text: "Duplicate",
                    handler: function () {
                        var f = a.node;
                        var e = f.component.getInternals(true);
                        var c = this.getDropPosition(f, "below");
                        var d = this.restore(e, c.parent, c.before);
                        d.select();
                        xds.fireEvent("componentchanged");
                    },
                    scope: this
                },{
                    text: "Transform to",
                    menu: this.transformMenu
                },{
                    itemId: "save-to-toolbox",
                    text: "Save to Toolbox",
                    iconCls: "icon-save",
                    handler: function () {
                        var cmp = a.node.component;
                        var c = cmp.getInternals(true);
                        c.name = a.name || a.defaultName;
                        c.naming = a.naming;
                        xds.Registry.addUserType(c);
                        xds.toolbox.loadUserTypes();
                        var d = xds.toolbox.getNodeById("User_Components");
                        if (d && d.lastChild) {
                            d.lastChild.ensureVisible();
                            d.lastChild.ui.highlight();
                        }
                    }
                },
                "-", {
                    itemId: "move-up",
                    text: "Move Up",
                    handler: function () {
                        a.node.parentNode.insertBefore(a.node, a.node.previousSibling);
                        a.node.select();
                        xds.fireEvent("componentchanged");
                    }
                },
                {
                    itemId: "move-down",
                    text: "Move Down",
                    handler: function () {
                        a.node.parentNode.insertBefore(a.node, a.node.nextSibling.nextSibling);
                        a.node.select();
                        xds.fireEvent("componentchanged");
                    }
                },
                "-", {
                    text: "Delete",
                    iconCls: "icon-delete",
                    handler: function () {
                        xds.inspector.removeComponent(a.node.component);
                        xds.fireEvent("componentchanged");
                    }
                }],
                onContextShow: function () {
                    if (Ext.isEmpty(a.node.component)) {
                        return false;
                    }
                    this.items.get("save-to-toolbox").setDisabled( !! a.node.component.owner);
                    this.items.get("move-up").setDisabled(!a.node.previousSibling);
                    this.items.get("move-down").setDisabled(!a.node.nextSibling);
                    a.node.ui.addClass("xds-context-node");
                    var e = a.node.component.getActions();
                    if (e) {
                        a.add(new Ext.menu.Separator({
                            id: "actions-sep"
                        }));
                        for (var d = 0, c = e.length; d < c; d++) {
                            a.add(e[d]);
                        }
                    }
                },
                onContextClose: function () {
                    var e = a.node.component.getActions();
                    if (e) {
                        a.remove(a.items.get("actions-sep"));
                        for (var d = 0, c = e.length; d < c; d++) {
                            a.remove(a.items.get(e[d].initialConfig.itemId));
                        }
                    }
                    a.node.ui.removeClass("xds-context-node");
                }
            });
            a.on("beforeshow", a.onContextShow, a);
            a.on("hide", a.onContextClose, a);
        }
        return this.contextMenu;
    },
    onNodeContext: function (c, b) {
        var a = this.getContextMenu();
        a.node = c;
        a.showAt(b.getXY());
        b.stopEvent();
    },
    nextId: function (naming) {
        if (!this.getNodeById(naming)) {
            return naming;
        }
        var i = 0;
        while (this.getNodeById(naming + (++i))) {}
        return naming + i;
    },
    onDragOver: function (a) {
        return xds.types.BaseType.isValidDrop(this.getDropPosition(a.target, a.point).parent.component, a.dropNode.component || a.dropNode.instance);
    },
    onBeforeDrop: function (d) {
        if (!xds.types.BaseType.isValidDrop(this.getDropPosition(d.target, d.point).parent.component, d.dropNode.component || d.dropNode.instance)) {
            return false;
        }
        if (d.tree == d.source.tree) {
            if (!d.dropNode.component.owner) {
                this.initCopy(d.dropNode, d.target, d.point);
                d.dropStatus = true;
                return false;
            }
            return true;
        } else {
            if (d.dropNode) {
                d.dropStatus = true;
                var c = this.getDropPosition(d.target, d.point);
                var b = d.dropNode.instance.getSpec(d.target);
                var a = this.restore(b, c.parent, c.before);
                a.select();
                xds.fireEvent("componentchanged");
            }
        }
        return false;
    },
    getDropPosition: function (b, a) {
        var c = {};
        switch (a) {
        case "above":
            c.parent = b.parentNode;
            c.before = b;
            break;
        case "below":
            c.parent = b.parentNode;
            c.before = b.nextSibling;
            break;
        default:
            c.parent = b;
        }
        return c;
    },
    onAfterDrop: function (a) {
        a.dropNode.select();
        a.dropNode.component.setOwner(a.dropNode.parentNode.component);
        xds.fireEvent("componentchanged");
    },
    onComponentEvent: function (evt) { //c
        var parent = evt.parentId ? this.getNodeById(evt.parentId) : this.root;
    	//var parent = this.getNodeById(evt.parentId) || this.root;
        if (evt.type == "new") {
            this.restore(evt.spec || evt.component, parent).select();
            xds.fireEvent("componentchanged");
        } else {
            if (evt.type == "move") {
                evt.component.setOwner(parent.component);
                var node = evt.component.getNode();
                parent.appendChild(node);
                node.select();
                xds.fireEvent("componentchanged");
            }
        }
    },
    initCopy: function (c, b, a) {
        Ext.select('.el-film').hide();
        Ext.Msg.show({
            title: "Copy",
            msg: "Dropping a root level component here can not be undone. Would you like to copy it instead?",
            buttons: Ext.Msg.YESNOCANCEL,
            fn: function (d) {
                if (d == "yes") {
                    var e = c.component.getInternals(true);
                    var f = this.getDropPosition(b, a);
                    this.restore(e, f.parent, f.before).select();
                } else {
                    if (d == "no") {
                        var f = this.getDropPosition(b, a);
                        c.component.setOwner(f.parent.component);
                        f.parent.insertBefore(c, f.before);
                        c.select();
                        xds.fireEvent("componentchanged");
                    }
                }
                Ext.select('.el-film').show();
            },
            scope: this
        });
    },
    restore: function (cfg, parent, f) {
        parent = parent || this.root;
        var comp = xds.create(cfg);
        delete comp.cn;
        if (parent) {
            comp.setOwner(parent.component);
        }
        var node = comp.getNode();

        parent.insertBefore(node, f);
        if (cfg.cn) {
            for (var i = 0, cn; cn = cfg.cn[i]; i++) {
                this.restore(cn, node);
            }
        }

        return node;
    }
});
xds.Inspector.DemoLoader = Ext.extend(Ext.tree.TreeLoader, {
    load: function (a, b) {
        b();
    }
});
xds.Toolbox = Ext.extend(Ext.tree.TreePanel, {
    constructor: function (cfg) {
    	Ext.apply(this,cfg);

        xds.Toolbox.superclass.constructor.call(this, {
            width: 200,
            region: "west",
            split: true,
            id: "toolbox",
            border: false,
            margins: "0 0 0 0",
            cmargins: "0 1 0 0",
            layout: "fit",
            collapsible: true,
            rootVisible: false,
            animate: false,
            autoScroll: true,
            useArrows: true,
            minWidth: 150,
            enableDrag: true,
            collapseFirst: false,
            animCollapse: false,
            animFloat: false,
            tools: [{
                id: "expand-all",
                handler: function () {
                    this.root.expand(true);
                },
                qtip: "Expand All",
                scope: this
            },{
                id: "collapse-all",
                handler: function () {
                    this.root.collapse(true);
                },
                qtip: "Collapse All",
                scope: this
            }]
        });
        xds.toolbox = this;
    },
    initComponent: function () {
        this.loader = new xds.Toolbox.DemoLoader();
        this.root = {
            id: "troot",
            async: true,
            //expanded: true,
            text: "troot"
        };
        xds.Toolbox.superclass.initComponent.call(this);
        this.getSelectionModel().on("beforeselect", function (a, b) {
            if (b && !b.isLeaf()) {
                b.toggle();
                return false;
            }
        });
        this.on("dblclick", this.onDblClick, this);
    },
    onDblClick: function (a) {
        if (a.isLeaf() && xds.active && xds.types.BaseType.isValidDrop(xds.active.component, a.instance)) {
            xds.inspector.restore(a.instance.getSpec(), xds.active.node);
            xds.fireEvent("componentchanged");
        }
    },
    loadUserTypes: function () {
        var e = xds.Registry.userTypes;
        if (e) {
            var d = this.getNodeById("User_Components");
            if (d) {
                while (d.firstChild) {
                    d.removeChild(d.firstChild);
                }
            } else {
                d = this.root.appendChild({
                    cls: "toolbox-ct",
                    allowDrag: false,
                    text: "User Components",
                    id: "User_Components",
                    leaf: false
                });
            }
            d.beginUpdate();
            for (var b = 0, a; a = e[b]; b++) {
                var g = xds.Registry.get(a.cid);
                if (g) {
                    var f = new Ext.tree.TreeNode({
                        text: a.name || g.prototype.defaultName,
                        iconCls: g.prototype.iconCls,
                        leaf: true
                    });
                    d.appendChild(f);
                    f.type = g;
                    f.spec = a;
                    f.instance = new g();
                    f.instance.spec = a;
                }
            }
            d.endUpdate();
            d.expand();
        }
    },
    onRender: function (b, a) {
        xds.Toolbox.superclass.onRender.call(this, b, a);
        this.innerCt.setStyle("padding-bottom", "20px");
    }
});
xds.Toolbox.Loader = Ext.extend(Ext.tree.TreeLoader, {
    load: Ext.emptyFn
});
xds.Toolbox.WebLoader = Ext.extend(xds.Toolbox.Loader, {});
xds.Toolbox.DemoLoader = Ext.extend(xds.Toolbox.Loader, {
    load: function (node, callback) { //a:node;k:callback
        if (node.id != "troot") {
            callback();
            return
        }
        var tree = node.getOwnerTree(); //l
        node.beginUpdate();
        var xdsTypes = xds.Registry.all.items; //j
        for (var i = 0, len = xdsTypes.length, catNode, xdsType, category; i < len; i++) {
            xdsType = xdsTypes[i];
            if (xdsType.prototype.cid == 'module') {
                continue;
            }
            if (Ext.isEmpty(xdsType.prototype.category)) {
                continue;
            }
            category = "xdc" + xdsType.prototype.category.replace(/\s/g, "_");
            catNode = tree.getNodeById(category);
            if (!catNode) {
                catNode = node.appendChild({
                    cls: "toolbox-ct",
                    allowDrag: false,
                    text: xdsType.prototype.category,
                    id: category,
                    leaf: false
                });
            }
            var tpNode = new Ext.tree.TreeNode({
                text: xdsType.prototype.text,
                iconCls: xdsType.prototype.iconCls,
                leaf: true
            });
            catNode.appendChild(tpNode);
            tpNode.type = xdsType;
            tpNode.instance = new xdsType();
        }
        tree.loadUserTypes();
        node.endUpdate();
        node.firstChild.expand.defer(10, node.firstChild, [true]);
        callback();
        xds.fireEvent("componentsloaded");
    }
});
xds.Layouts = {
    form: {
        id: "form",
        xcls: "Ext.layout.FormLayout",
        text: "Form Layout",
        configs: [{
            name: "anchor",
            group: "Ext.layout.FormLayout",
            ctype: "string"
        },{
            name: "clearCls",
            group: "Ext.layout.FormLayout",
            ctype: "string"
        },{
            name: "fieldLabel",
            group: "Ext.layout.FormLayout",
            ctype: "string"
        },{
            name: "hideLabel",
            group: "Ext.layout.FormLayout",
            ctype: "boolean"
        },{
            name: "itemCls",
            group: "Ext.layout.FormLayout",
            ctype: "string"
        },{
            name: "labelSeparator",
            group: "Ext.layout.FormLayout",
            ctype: "string"
        },{
            name: "labelStyle",
            group: "Ext.layout.FormLayout",
            ctype: "string"
        }],
        layoutConfigs: [{
            name: "labelAlign",
            group: "(Layout)",
            ctype: "string",
            editor: "options",
            options: ["left", "right", "top"]
        },{
            name: "labelSeparator",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "string"
        },{
            name: "labelPad",
            group: "Ext.layout.FormLayout",
            ctype: "number"
        },{
            name: "labelWidth",
            group: "Ext.layout.FormLayout",
            ctype: "number"
        }]
    },
    table: {
        id: "table",
        xcls: "Ext.layout.TableLayout",
        text: "Table Layout",
        configs: [{
            name: "cellId",
            group: "Ext.layout.TableLayout",
            ctype: "string"
        },{
            name: "cellCls",
            group: "Ext.layout.TableLayout",
            ctype: "string"
        },{
            name: "colspan",
            group: "Ext.layout.TableLayout",
            ctype: "number"
        },{
            name: "rowspan",
            group: "Ext.layout.TableLayout",
            ctype: "number"
        }],
        layoutConfigs: [{
            name: "columns",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "number"
        }]
    },
    card: {
        id: "card",
        xcls: "Ext.layout.CardLayout",
        text: "Card Layout",
        configs: [],
        layoutConfigs: [{
            name: "deferredRender",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: false
        },{
            name: "layoutOnCardChange",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: false
        },{
            name: "activeItem",
            group: "(Layout)",
            ctype: "number",
            updateFn: function (extCmp, value, cmp) {
                extCmp.getLayout().setActiveItem(value);
            },
            defaultValue: 0
        }],
        onInit: function (node) {
            node.component.setConfig("activeItem", 0);
        }
    },
    accordion: {
        id: "accordion",
        xcls: "Ext.layout.AccordionLayout",
        text: "Accordion Layout",
        configs: [],
        layoutConfigs: [{
            name: "fill",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: true
        },{
            name: "autoWidth",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: true
        },{
            name: "titleCollapse",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: true
        },{
            name: "hideCollapseTool",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: false
        },{
            name: "collapseFirst",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: false
        },{
            name: "animate",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: false
        },{
            name: "sequence",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: false
        },{
            name: "activeOnTop",
            group: "(Layout)",
            ctype: "boolean",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            defaultValue: false
        }]
    },
    border: {
        id: "border",
        xcls: "Ext.layout.BorderLayout",
        text: "Border Layout",
        configs: [{
            name: "animFloat",
            group: "Ext.layout.BorderLayout",
            ctype: "boolean"
        },{
            name: "autoHide",
            group: "Ext.layout.BorderLayout",
            ctype: "boolean"
        },{
            name: "cmargins",
            group: "Ext.layout.BorderLayout",
            ctype: "string"
        },{
            name: "collapseMode",
            group: "Ext.layout.BorderLayout",
            ctype: "string",
            editor: "options",
            options: ["standard", "mini"]
        },{
            name: "floatable",
            group: "Ext.layout.BorderLayout",
            ctype: "boolean"
        },{
            name: "margins",
            group: "Ext.layout.BorderLayout",
            ctype: "string"
        },{
            name: "minHeight",
            group: "Ext.layout.BorderLayout",
            ctype: "number"
        },{
            name: "minWidth",
            group: "Ext.layout.BorderLayout",
            ctype: "number"
        },{
            name: "region",
            group: "Ext.layout.BorderLayout",
            ctype: "string",
            editor: "options",
            options: [["center","中"], ["east","右"], ["north","上"], ["south","下"], ["west","左"]],
            setFn: "setRegion"
        },{
            name: "split",
            group: "Ext.layout.BorderLayout",
            ctype: "boolean"
        }],
        onBeforeAdd: function (d, c) {
            if (!c.component.getConfigValue("region") && !c.component.dock) {
                var g;
                var f = ["center", "west", "east", "north", "south"];
                var e = d.firstChild;
                while (e) {
                    var b = e.component.getConfigValue("region");
                    if (b) {
                        var a = f.indexOf(b);
                        if (a != - 1) {
                            f.splice(a, 1);
                        }
                    }
                    e = e.nextSibling;
                }
                c.component.setRegion("region",f[0]);
            }
        },
        onInit: function (a) {
            var b = a.firstChild;
            while (b) {
                this.onBeforeAdd(a, b);
                b = b.nextSibling;
            }
        }
    },
    anchor: {
        id: "anchor",
        xcls: "Ext.layout.AnchorLayout",
        text: "Anchor Layout",
        configs: [{
            name: "anchor",
            group: "Ext.layout.AnchorLayout",
            ctype: "string"
        }]
    },
    absolute: {
        id: "absolute",
        xcls: "Ext.layout.AbsoluteLayout",
        text: "Absolute Layout",
        configs: [{
            name: "anchor",
            text: "参照",
            group: "Ext.layout.AbsoluteLayout",
            ctype: "string"
        },{
            name: "x",
            text: "位置-左",
            group: "Ext.layout.AbsoluteLayout",
            ctype: "number"
        },{
            name: "y",
            text: "位置-上",
            group: "Ext.layout.AbsoluteLayout",
            ctype: "number"
        }],
        layoutConfigs: [{
            name: "snapToGrid",
            text: "网格大小",
            group: "Ext.layout.AbsoluteLayout",
            setFn: "setSnapToGrid",
            getFn: "getSnapToGrid",
            ctype: "string",
            editor: "options",
            options: ["(none)", "5", "10", "15", "20"],
            defaultValue: "(none)"
        }],
        onBeforeAdd: function (b, a) {
            if (xds.canvas.lastDropPoint) {
                var d = b.component.getExtComponent();
                if (d) {
                    var c = d.getLayoutTarget().translatePoints(xds.canvas.lastDropPoint);
                    c.left = xds.canvas.dragTracker.snap(c.left, b.component.snapToGrid);
                    c.top = xds.canvas.dragTracker.snap(c.top, b.component.snapToGrid);
                    a.component.config.x = c.left - 12;
                    a.component.config.y = c.top - 12;
                }
            }
        }
    },
    column: {
        id: "column",
        xcls: "Ext.layout.ColumnLayout",
        text: "Column Layout",
        configs: [{
            name: "columnWidth",
            group: "Ext.layout.ColumnLayout",
            ctype: "number"
        }],
        layoutConfigs: [{
            name: "scrollOffset",
            group: "(Layout)",
            ctype: "number"
        }]
    },
    fit: {
        id: "fit",
        xcls: "Ext.layout.FitLayout",
        text: "Fit Layout",
        configs: []
    },
    hbox: {
        id: "hbox",
        xcls: "Ext.layout.HBoxLayout",
        text: "HBox Layout",
        configs: [{
            name: "flex",
            group: "Ext.layout.HBoxLayout",
            ctype: "number"
        },{
            name: "margins",
            group: "Ext.layout.HBoxLayout",
            ctype: "string"
        }],
        layoutConfigs: [{
            name: "align",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "string",
            editor: "options",
            options: ["top", "middle", "stretch", "stretchmax"],
            defaultValue: "top"
        },{
            name: "pack",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "string",
            editor: "options",
            options: ["start", "center", "end"],
            defaultValue: "start"
        },{
            name: "padding",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "string",
            defaultValue: "0"
        },{
            name: "scrollOffset",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "number",
            defaultValue: 0
        }]
    },
    vbox: {
        id: "vbox",
        xcls: "Ext.layout.VBoxLayout",
        text: "VBox Layout",
        configs: [{
            name: "flex",
            group: "Ext.layout.VBoxLayout",
            ctype: "number"
        },{
            name: "margins",
            group: "Ext.layout.VBoxLayout",
            ctype: "string"
        }],
        layoutConfigs: [{
            name: "align",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "string",
            editor: "options",
            options: ["left", "center", "stretch", "stretchmax"],
            defaultValue: "top"
        },{
            name: "pack",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "string",
            editor: "options",
            options: ["start", "center", "end"],
            defaultValue: "start"
        },{
            name: "padding",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "string",
            defaultValue: "0"
        },{
            name: "scrollOffset",
            group: "(Layout)",
            setFn: "setContainerConfig",
            getFn: "getContainerConfigValue",
            ctype: "number",
            defaultValue: 0
        }]
    }
};
xds.layouts = ["auto", "absolute", "accordion", "anchor", "border", "card", "column", "fit", "form", "hbox", "table", "vbox"];

(function () {
	var initConfigs = function (property, layout) {
	    var d = layout[property];
	    layout[property] = new Ext.util.MixedCollection(false, function (item) {
	        return item.name;
	    });
	    if (d) {
	        for (var b = 0, a = d.length; b < a; b++) {
	            layout[property].add(new xds.Config.types[d[b].ctype](d[b]));
	        }
	    }
	};

    for (var layout in xds.Layouts) {
        if (!Ext.isEmpty(xds.Layouts[layout])) {
            initConfigs("configs", xds.Layouts[layout]);
            initConfigs("layoutConfigs", xds.Layouts[layout]);
        }
    }
})();
xds.StoreCache = new Ext.util.MixedCollection(false, function (a) {
    return a.component.id;
});
Ext.intercept(Ext.StoreMgr, "register", function (a) {
    if (a.cache === false) {
        return false;
    }
    if (a.component) {
        xds.StoreCache.replace(a);
    }
});
xds.CWindow = Ext.extend(Ext.Window, {
    iconCls: "icon-cmp",
    width: 500,
    height: 350,
    layout: "border",
    plain: true,
    modal: true,
    initComponent: function () {
        this.items = [this.view = new Ext.ux.TileView({
            style: "background:#fff;",
            autoScroll: true,
            region: "center",
            categoryName: "category",
            store: new xds.Registry.createStore(true),
            singleSelect: true,
            trackOver: true,
            overClass: "x-tile-over"
        }), {
            layout: "form",
            region: "south",
            height: 29,
            bodyStyle: "padding:3px;border-top:1px solid #B7CCE4;",
            baseCls: "x-plain",
            labelWidth: 70,
            items: this.idField = new Ext.form.TextField({
                value: "MyComponent",
                selectOnFocus: true,
                fieldLabel: "Class Name",
                anchor: "100%"
            })
        }];
        this.buttons = [{
            text: "OK",
            disabled: true,
            handler: this.onAccept,
            scope: this
        },{
            text: "Cancel",
            handler: this.close,
            scope: this
        }];
        this.view.on("selectionchange", this.onViewSelect, this);
        xds.CWindow.superclass.initComponent.call(this);
    },
    onViewSelect: function () {
        var a = this.view.getSelectedRecords()[0];
        if (a) {
            this.buttons[0].enable();
            this.idField.setValue(xds.inspector.nextId(a.data.naming));
        } else {
            this.buttons[0].disable();
        }
    },
    onAccept: function () {
        var a = this.view.getSelectedRecords()[0];
        var b = xds.Registry.get(a.id);
        xds.fireEvent("componentevent", {
            type: "new",
            component: (new b()).getSpec()
        });
        this.close();
    }
});
xds.PWindow = Ext.extend(Ext.Window, {
    iconCls: "icon-project",
    title: 'Open Module',
    width: 600,
    height: 400,
    layout: "border",
    plain: true,
    buttonAlign: 'left',
    initComponent: function () {
        var store = new Ext.data.JsonStore({
            method: 'GET',
            url: 'entity/moduleView',
            autoLoad: true,
            autoDestroy: true,
            idProperty: 'id',
            root: 'root',
            sortInfo: {
                field: "category",
                direction: "ASC"
            },
            fields: [{
                name: 'id'
            },{
                name: 'text',
                mapping: 'name'
            },{
                name: 'xdsConfig'
            },{
                name: 'moduleId'
            },{
                name: 'iconCls',
                defaultValue: 'icon-cmp'
            },{
                name: 'category',
                convert: function (v, rec) {
                    if (Ext.isEmpty(v)) {
                        return 'Modules';
                    }
                    return v;
                }
            }]
        });
        this.items = [
        this.openView = new Ext.ux.TileView({
            style: "background:#fff;",
            autoScroll: true,
            region: "center",
            categoryName: "category",
            store: store,
            singleSelect: true,
            trackOver: true,
            overClass: "x-tile-over"
        }), {
            layout: "form",
            region: "south",
            height: 29,
            bodyStyle: "padding:3px;border-top:1px solid #B7CCE4;",
            baseCls: "x-plain",
            labelWidth: 100,
            items: this.idField = new Ext.form.TextField({
                value: "NewModule",
                selectOnFocus: true,
                fieldLabel: "Module Name",
                anchor: "100%"
            })
        }];
        store.on('load', function () {
            store.insert(0, [new Ext.data.Record({
                "moduleId": "TestMoudle",
                "text": "NewModule",
                "category": "Create New Module ...",
                "iconCls": "icon-project"
            })]);
        }, this);
        this.fbar = [{
            text: 'Delete',
            disabled: true,
            handler: this.onDelete,
            scope: this
        },
        '->', {
        	xtype:'splitbutton',
            text: "Open",
            disabled: true,
            handler: this.onAccept,
            scope: this,
            menu:new Ext.menu.Menu({
                items:[{
                	text:'Select version ...',
                	handler:function(btn){
                		var id = this.selectedId;
                		var ownerWin = this;
                		var win = new Ext.Window({
                	        width: 636,
                	        height: 408,
                	        modal:true,
                	        title: "Select Version",
                	        constrain: true,
                	        layout: "fit",
                	        fbar: {
                	            xtype: "toolbar",
                	            items: [{
                	                xtype: "button",
                	                text: "确认",
                	                ref: "../btnAccept"
                	            },{
                	                xtype: "button",
                	                text: "取消",
                	                ref: "../btnCancel",
                	                listeners: {
                	                    click: function(btn, evt) {
                	                        if (btn.refOwner) {
                	                            btn.refOwner.close();
                	                        }
                	                    }
                	                }
                	            }]
                	        },
                	        items: [{
                	            xtype: "grid",
                	            store: {
                	                xtype: "jsonstore",
                	                storeId: "MyStore",
                	                url: "entity/moduleHistory",
                	                root: "root",
                	                autoLoad: true,
                	                idProperty: "id",
                	                sortField: "updateTime",
                	                sortDir: "DESC",
                	                restful: true,
                	                baseParams:{dbId:id},
                	                fields: [
                	                    {
                	                    name: "id",
                	                    type: "integer",
                	                    text: "ID"
                	                },
                	                    {
                	                    name: "name",
                	                    type: "string",
                	                    text: "模块名称"
                	                },
                	                    {
                	                    name: "moduleId",
                	                    type: "string",
                	                    text: "moduleId"
                	                },
                	                    {
                	                    name: "category",
                	                    type: "string",
                	                    text: "category"
                	                },
                	                    {
                	                    name: "autoSave",
                	                    type: "boolean",
                	                    text: "自动保存"
                	                },
                	                    {
                	                    name: "updateTime",
                	                    type: "date",
                	                    text: "更新时间",
                	                    dateFormat: "time"
                	                }
                	                    ]
                	            },
                	            border: false,
                	            forceFit: true,
                	            columns: [{
                	                header: "模块名称",
                	                sortable: false,
                	                resizable: true,
                	                width: 200,
                	                menuDisabled: true,
                	                dataIndex: "name",
                	                id: "name"
                	            },{
                	                header: "模块编码",
                	                sortable: false,
                	                resizable: true,
                	                width: 160,
                	                menuDisabled: true,
                	                dataIndex: "moduleId",
                	                id: "moduleId"
                	            },{
                	            	xtype: "booleancolumn",
                	                header: "自动保存",
                	                sortable: false,
                	                resizable: true,
                	                width: 60,
                	                menuDisabled: true,
                	                dataIndex: "autoSave",
                	                id: "autoSave"
                	            },{
                	                xtype: "datecolumn",
                	                header: "更新时间",
                	                sortable: true,
                	                resizable: true,
                	                width: 120,
                	                format: "Y-m-d H:i:s",
                	                menuDisabled: true,
                	                dataIndex: "updateTime",
                	                id: "updateTime"
                	            }],
                	            listeners:{
                	            	scope:this,
                	            	rowdblclick:function(grid,rowIdx,evt){
                	            		var hisId = grid.getStore().getAt(rowIdx).id;
                	            		if(hisId){
                	            	        Ext.Ajax.request({
                	            	            url: 'entity/moduleHistory/' + hisId,
                	            	            method: 'GET',
                	            	            success: function (resp) {
            	            	                    var ret = Ext.decode(resp.responseText);
            	            	                    if (!Ext.isEmpty(ret)) {
            	            	                        var data = Ext.decode(ret.xdsConfig);
            	            	                        data.dbId = ret.dbId;
            	            	                        if (xds.project) {
            	            	                            xds.project.close(function () {
            	            	                                new xds.Project().open(data);
            	            	                            });
            	            	                        }
            	            	                        win.close();
            	            	                        ownerWin.close();
            	            	                        xds.actions.saveAsAction.enable();
            	            	                    }
                	            	            }
                	            	        });
                	            		}
                	            	}
                	            }
                	        }]
                	    });

                		win.show();
                	},
                	scope:this
                }]
            }),
            scope:this
        },{
            text: "Cancel",
            handler: this.close,
            scope: this
        }];
        this.openView.on("selectionchange", this.onOpenViewSelect, this);
        this.openView.on('dblclick', this.onDblClick, this);
        xds.PWindow.superclass.initComponent.call(this);
    },
    onDblClick: function (view, idx, node, evt) {
        var record = view.getRecord(node);
        if (record) {
            this.selectedId = record.data.id;
        }
        this.onAccept();
    },
    onOpenViewSelect: function () {
        var a = this.openView.getSelectedRecords()[0];
        if (a) {
            this.buttons[0].enable();
            this.buttons[2].enable();
            this.idField.setValue(a.data.moduleId);
            try {
                //this.xdsJson=Ext.decode(a.data.xdsConfig);
                this.selectedId = a.data.id;
            } catch (e) {}
        } else {
            this.buttons[0].disable();
            this.buttons[2].disable();
            delete this.selectedId;
        }
    },
    onAccept: function () {
        var win = this;
        Ext.Ajax.request({
            url: 'entity/module/' + this.selectedId,
            method: 'GET',
            success: function (resp) {
//                try {
                    var ret = Ext.decode(resp.responseText);
                    if (!Ext.isEmpty(ret)) {
                        var data = Ext.decode(ret.xdsConfig);
                        data.dbId = ret.id;
                        if (xds.project) {
                            xds.project.close(function () {
                                new xds.Project().open(data);
                            });
                        }
                        win.close();

                        xds.actions.saveAsAction.enable();
                    }
//                } catch (e) {
//                	console.log(e);
//                    Ext.Msg.alert('Error', 'Load module config error!');
//                }
            }
        });
    },
    onDelete: function () {
        var a = this.openView.getSelectedRecords()[0];
        var mid = a.data.id;
        var view = this.openView;

        Ext.Msg.confirm("Delete Module", "Are you sure you wish to permanently delete this Module?", function (a) {
            if (a == "yes") {
                Ext.Ajax.request({
                    url: '/module/' + mid,
                    method: 'DELETE',
                    success: function () {
                        view.getStore().reload();
                    }
                });
            }
        });
    }
});

if (!this.JSON) {
    JSON = {};
}

(function () {
    function f(n) {
        return n < 10 ? "0" + n : n;
    }
    if (typeof Date.prototype.toJSON !== "function") {
        Date.prototype.toJSON = function (key) {
            return this.getUTCFullYear() + "-" + f(this.getUTCMonth() + 1) + "-" + f(this.getUTCDate()) + "T" + f(this.getUTCHours()) + ":" + f(this.getUTCMinutes()) + ":" + f(this.getUTCSeconds()) + "Z";
        };
        String.prototype.toJSON = Number.prototype.toJSON = Boolean.prototype.toJSON = function (key) {
            return this.valueOf();
        };
    }
    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        gap, indent, meta = {
        "\b": "\\b",
        "\t": "\\t",
        "\n": "\\n",
        "\f": "\\f",
        "\r": "\\r",
        '"': '\\"',
        "\\": "\\\\"
    },
        rep;
    function quote(string) {
        escapable.lastIndex = 0;
        return escapable.test(string) ? '"' + string.replace(escapable, function (a) {
            var c = meta[a];
            if (typeof c === "string") {
                return c;
            }
            return "\\u" + ("0000" + a.charCodeAt(0).toString(16)).slice(-4);
        }) + '"' : '"' + string + '"';
    }
    function str(key, holder) {
        var i, k, v, length, mind = gap,
            partial, value = holder[key];
        if (value && typeof value === "object" && typeof value.toJSON === "function") {
            value = value.toJSON(key);
        }
        if (typeof rep === "function") {
            value = rep.call(holder, key, value);
        }
        switch (typeof value) {
        case "string":
            return quote(value);
        case "number":
            return isFinite(value) ? String(value) : "null";
        case "boolean":
        case "null":
            return String(value);
        case "object":
            if (!value) {
                return "null";
            }
            gap += indent;
            partial = [];
            if (typeof value.length === "number" && !value.propertyIsEnumerable("length")) {
                length = value.length;
                for (i = 0; i < length; i += 1) {
                    partial[i] = str(i, value) || "null";
                }
                v = partial.length === 0 ? "[]" : gap ? "[\n" + gap + partial.join(",\n" + gap) + "\n" + mind + "]" : "[" + partial.join(",") + "]";
                gap = mind;
                return v;
            }
            if (rep && typeof rep === "object") {
                length = rep.length;
                for (i = 0; i < length; i += 1) {
                    k = rep[i];
                    if (typeof k === "string") {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ": " : ":") + v);
                        }
                    }
                }
            } else {
                for (k in value) {
                    if (Object.hasOwnProperty.call(value, k)) {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ": " : ":") + v);
                        }
                    }
                }
            }
            v = partial.length === 0 ? "{}" : gap ? "{\n" + gap + partial.join(",\n" + gap) + "\n" + mind + "}" : "{" + partial.join(",") + "}";
            gap = mind;
            return v;
        }
    }
    if (typeof JSON.stringify !== "function") {
        JSON.stringify = function (value, replacer, space) {
            var i;
            gap = "";
            indent = "";
            if (typeof space === "number") {
                for (i = 0; i < space; i += 1) {
                    indent += " ";
                }
            } else {
                if (typeof space === "string") {
                    indent = space;
                }
            }
            rep = replacer;
            if (replacer && typeof replacer !== "function" && (typeof replacer !== "object" || typeof replacer.length !== "number")) {
                throw new Error("JSON.stringify");
            }
            return str("", {
                "": value
            });
        };
    }
    if (typeof JSON.parse !== "function") {
        JSON.parse = function (text, reviver) {
            var j;
            function walk(holder, key) {
                var k, v, value = holder[key];
                if (value && typeof value === "object") {
                    for (k in value) {
                        if (Object.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            } else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }
            cx.lastIndex = 0;
            if (cx.test(text)) {
                text = text.replace(cx, function (a) {
                    return "\\u" + ("0000" + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }
            if (/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, "@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, "]").replace(/(?:^|:|,)(?:\s*\[)+/g, ""))) {
                j = eval("(" + text + ")");
                return typeof reviver === "function" ? walk({
                    "": j
                }, "") : j;
            }
            throw new SyntaxError("JSON.parse");
        };
    }
})();

xds.Designer = Ext.extend(Ext.Panel, {
    layout: 'border',
    title: 'XDS-Designer',
    initComponent: function () {
        this.tbar = new Ext.Toolbar({
            items: ["-", xds.actions.newAction, xds.actions.openAction, xds.actions.openAction2, "-", xds.actions.saveAction,xds.actions.saveAsAction,"-", xds.actions.undo, xds.actions.redo, "-", xds.actions.preview, "-",xds.actions.rtConfig]
        });
        var tbox = new xds.Toolbox({
        	title:'ToolBox',
        	singleExpand:true
        });
        xds.inspector = new xds.Inspector({
            id: "structure",
            region: "north",
            split: true,
            height: 300,
            minHeight: 120,
            autoScroll: true,
            title: "Component",
            trackMouseOver: false,
            animate: false,
            autoScroll: true,
            enableDD: true,
            border: false,
            rootVisible: true,
            useArrows: true
        });
        xds.props = new xds.ConfigEditor({
            region: "center",
            margins: "0 0 0 0",
            border: false,
            split: true,
            disabled: true
        });
        //        xds.propDesc = new Ext.Panel({
        //        	id:"propdesc",
        //        	region:"south",
        //        	height:64,
        //        	margins: "0 0 0 0",
        //        	border:false,
        //        	autoScroll:true,
        //        	split: true
        //        });
        xds.east = new Ext.Panel({
            id: 'east',
            width: 240,
            region: "east",
            minWidth: 150,
            split: true,
            margins: "0",
            cmargins: "2 1 1 5",
            baseCls: "x-plain",
            layout: "border",
            items: [xds.inspector, xds.props]
        });
        var canvas = new xds.Canvas(); //b
        var codeTab = new Ext.Panel({
            title: 'Code',
            autoScroll: true,
            tbar: ['-', {
                text: 'Class',
                enableToggle: true,
                pressed: true,
                toggleHandler: function (btn, pressed) {
                    codeTab.refresh(pressed);
                }
            }],
            refresh: function (cls) {
                var insRoot = xds.inspector.root;
                var firstComp = insRoot.firstChild;
                var s = "";
                while (firstComp) {
                    var jsonConfig = firstComp.component.getJsonConfig(true);
                    jsonConfig = Ext.ux.CodeGenerator.parse_object(jsonConfig, false);
                    s += Ext.ux.CodeGenerator.objToString(jsonConfig, null, '', false, cls) + ';';
                    firstComp = firstComp.nextSibling;
                }
                s = js_beautify(s, {
                    opt_keep_array_indentation: false
                });
                var c = "<pre class='brush:js;' style='font-family:\"consolas\",\"courier new\",\"segoe ui\";'>" + s + "</pre>";
                this.body.update(c);
                SyntaxHighlighter.highlight();
            },
            listeners: {
                activate: function (tab) {
                    this.refresh(true);
                }
            }
        });
        var workspace = new Ext.TabPanel({ //w
            region: 'center',
            activeTab: 0,
            tabPosition: 'bottom',
            items: [{
                title: 'Designer',
                layout: 'border',
                items: [canvas]
            },
            codeTab, {
                title: 'XDS-JSON',
                autoScroll: true,
                listeners: {
                    activate: function (tab) {
                        var s = Ext.encode(xds.project.getData());
                        s = js_beautify(s);
                        var c = "<pre style='padding:8px;font-family:\"consolas\",\"courier new\",\"segoe ui\";'>" + s + "</pre>";
                        tab.body.update(c);
                    }
                }
            },{
                title: 'Module-Config',
                autoScroll: true,
                listeners: {
                    activate: function (tab) {
//                        var firstComp = xds.inspector.root.firstChild;
//                        var tmp = [];
//                        while (firstComp) {
//                            var jsonConfig = firstComp.component.getJsonConfig(true);
//                            jsonConfig = Ext.ux.CodeGenerator.parse_object(jsonConfig, false);
//                            tmp.push(Ext.ux.CodeGenerator.objToString(jsonConfig, null, '', false, false));
//                            firstComp = firstComp.nextSibling;
//                        }
                        var module = xds.inspector.root.module;
                        this.body.update("<pre style='font-family:\"consolas\",\"courier new\",\"segoe ui\";'>" + js_beautify(module.getModuleConfig(), {
                            opt_keep_array_indentation: false
                        }) + "</pre>");
                    }
                }
            }]
        });
        xds.onComponentSelect = function (tgt) {
            if (tgt.component) {
                var current = xds.active;
                xds.props.enable();
                xds.active = tgt;
                xds.props.refresh();
                if (Ext.isEmpty(current)) {
                    canvas.setComponent(tgt.topNode);
                } else if (current.topNode != tgt.topNode) {
                    canvas.setComponent(tgt.topNode);
                }
            } else {
                xds.props.disable();
                xds.active = null;
                xds.props.refresh();
            }
        };
        xds.onModuleSelect = function () {
            xds.active = null;
            xds.props.enable();
            xds.props.grid.setComponent(xds.inspector.root.module);
            canvas.setSelected(null);
        };
        xds.onComponentChanged = function () {
            if (xds.active) {
                var l = xds.active.top;
                var k = xds.active.topNode;
                if (k.attributes.inRollbackMode) {
                    l.priorSpecs = l.priorSpecs.splice(0, l.currentSpec + 1);
                    l.finalSpec = l.currentSpec;
                    delete k.attributes.inRollbackMode;
                }
                l.takeSnapshot();
                canvas.setComponent(k);
            }
        };
        xds.maintainToolbarState = function (node) { //p
            var isEnable = node.component ? "enable" : "disable";
            if (node.component) {
                var o = node.component.getTopComponent();
                xds.actions.undo[o.currentSpec > 0 ? "enable" : "disable"]();

                if(o.currentSpec>0){
                	xds.actions.saveAction.enable();
                }

                xds.actions.redo[(o.currentSpec < o.priorSpecs.length - 1) ? "enable" : "disable"]();
                if (this.cmpActions) {
                    for (var l = 0, k = this.cmpActions.length; l < k; l++) {
                    	this.getTopToolbar().remove(this.cmpActions[l].itemId);
                    }
                    this.cmpActions = null;
                }
                var actions = node.component.getActions();
                if (actions) {
                    for (var n = 0, k = actions.length; n < k; n++) {
                        this.getTopToolbar().add(actions[n]);
                    }
                    this.getTopToolbar().doLayout();
                } else {}
                this.cmpActions = actions;
            } else {
                xds.actions.undo.disable();
                xds.actions.redo.disable();
                if (this.cmpActions) {
                    for (var l = 0, k = this.cmpActions.length; l < k; l++) {
                    	this.getTopToolbar().remove(this.cmpActions[l].itemId);
                    }
                    this.cmpActions = null;
                }
            }
            xds.actions.deleteCmpAction[isEnable]();
            xds.actions.preview[isEnable]();
        };
        xds.on("componentselect", xds.maintainToolbarState,this);
        xds.on("componentselect", xds.onComponentSelect);
        xds.on("componentchanged", xds.onComponentChanged);
        xds.on("moduleselect", xds.onModuleSelect);
        this.items = [xds.east, tbox, canvas]; //, statusbar
        xds.fireEvent("init");
        this.tools = [{
            id: 'maximize',
            handler: function (evt, toolEl, panel, tc) {
                panel.originalOwnerCt = panel.ownerCt;
                panel.originalPosition = panel.ownerCt.items.indexOf(panel);
                panel.originalSize = panel.getSize();
                if (!toolEl.window) {
                    var defaultConfig = {
                        id: panel.getId() + '-MAX',
                        closable: true,
                        closeAction: 'hide',
                        layout: 'fit',
                        bodyStyle: 'background-color: #ffffff;'
                    };
                    toolEl.window = new Ext.Window(defaultConfig);
                    toolEl.window.on('hide', function () {
                        this.originalOwnerCt.insert(this.originalPosition, this);
                        this.originalOwnerCt.doLayout();
                        this.setSize(this.originalSize);
                        this.tools['maximize'].setVisible(true);
                    }, panel);
                }
                if (!panel.dummyComponent) {
                    var dummyCompConfig = {
                        title: panel.title,
                        width: panel.getSize().width,
                        height: panel.getSize().height,
                        html: '&nbsp;'
                    };
                    panel.dummyComponent = new Ext.Panel(dummyCompConfig);
                }
                toolEl.window.add(panel);
                panel.tools['maximize'].setVisible(false);
                panel.originalOwnerCt.insert(panel.originalPosition, panel.dummyComponent);
                panel.originalOwnerCt.doLayout();
                panel.dummyComponent.setSize(panel.originalSize);
                toolEl.window.show();
                toolEl.window.maximize();
            }
        }];
        xds.Designer.superclass.initComponent.call(this);
    },
    afterRender: function () {
        xds.Designer.superclass.afterRender.call(this);
//        this.autoSaveTask = {
//        	run:function(){
//        		if(xds.project){
//        			if(xds.inspector.root.module){
//        				if(xds.inspector.root.module.dbId){
//        					xds.project.doAutoSave();
//        				}
//        			}
//        		}
//        	},
//        	interval:300000
//        };
//
//        Ext.TaskMgr.start(this.autoSaveTask);
        new xds.Project().open();
    },
    destroy: function () {
//    	Ext.TaskMgr.stop(this.autoSaveTask);

        xds.un("componentselect", xds.maintainToolbarState,xds);
        xds.un("componentselect", xds.onComponentSelect,xds);
        xds.un("componentchanged", xds.onComponentChanged,xds);
        xds.un("moduleselect", xds.onModuleSelect,xds);

        xds.un("componentevent", xds.inspector.onComponentEvent,xds.inspector);
        xds.un("componentclick", xds.inspector.onComponentClick,xds.inspector);
        xds.project.close();
        xds.project = null;
        xds.inspector = null;
        xds.canvas = null;
        xds.Designer.superclass.destroy.call(this);
    }
});
XDSDesignerModule = Ext.extend(od.XdsModule, {
    moduleId: 'xdsdesigner',
    iconCls: 'icon-xds',
    moduleName: 'XDS Designer',
    components: [xds.Designer],
    createDefaultComponent: function () {
        return new xds.Designer();
    },
	init:function(){
		xds.Registry.all.clear();
		xds.types.registerAllTypes();
		XDSDesignerModule.superclass.init.call(this);
	}
});
od.ModuleMgr.registerType('xdsdesigner', XDSDesignerModule);
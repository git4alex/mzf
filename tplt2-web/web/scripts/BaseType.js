Ext.ns("xds.types");
xds.types.BaseType = Ext.extend(Ext.util.Observable, {
    isContainer: false,
    bindable: false,
    isVisual: true,
    nameSuffix: "",
    filmCls: "",
    flyoutCls: "",
    minWidth: 10,
    minHeight: 10,
    snapToGrid: 10,
    showGrid: true,
    defaultLayout: "auto",
    currentSpec: - 1,
    finalSpec: - 1,
    constructor: function (a) {
        xds.types.BaseType.superclass.constructor.call(this);
        this.configs = this.getConfigs();
        this.userConfig = {};
        Ext.apply(this, a);
        this.name = this.name || this.defaultName;
        this.id = this.id || this.nextId();
        if (this.enableFlyout) {
            this.flyoutCls = "xds-flyout";
        }
        this.priorSpecs = [];
    },
    getTopComponent: function () {
        var node = this.getNode();
        var root = node.ownerTree.getRootNode();
        var top = node;
        while (top && top.parentNode != root) {
            top = top.parentNode;
        }
        return top.component;
    },
    takeSnapshot: function () {
        var a = this.getInternals(true);
        var b = this.getTopComponent().getNode();
        this.priorSpecs[++this.currentSpec] = a;
        this.finalSpec++;
        if (this.currentSpec > 0) {
            xds.actions.undo.enable();
        }
        xds.actions.redo.disable();
    },
    setOwner: function (owner) {
        if (this.owner && !owner) {
            this.setName(this.id);
        }
        this.owner = owner;
        delete this.config;
    },
    setConfig: function (name, value) {
        this.userConfig[name] = value;
        if (this.config) {
            this.config[name] = value;
        }
        
        this.updateNodeText();
        
        if (name == "layout") {
            if (value != "card") {
                delete this.config.activeItem;
                delete this.userConfig.activeItem;
            }
            delete this.layoutConfig;
            xds.props.refresh.defer(100, xds.props);
            if (xds.Layouts[value] && xds.Layouts[value].onInit) {
                xds.Layouts[value].onInit(this.getNode());
            }
        }
    },
    setEventHandler: function (evt, handler) {
        if (Ext.isEmpty(evt)) {
            return;
        }
        if (!this.userConfig.evtHandlers) {
            this.userConfig.evtHandlers = {};
        }
        if (Ext.isEmpty(handler)) {
            delete this.userConfig.evtHandlers[evt];
            return;
        }
        try {
            var test = new Function(handler.params, handler.value);
        } catch (e) {}
        this.userConfig.evtHandlers[evt] = handler;
    },
    getEventHandler: function (evt) {
        if (Ext.isEmpty(evt)) {
            return null;
        }
        if (!this.userConfig.evtHandlers) {
            return null;
        }
        return this.userConfig.evtHandlers[evt];
    },
    setSuffix: function (b, a) {
        a = a || "loaded";
        if (!b) {
            delete this.nameSuffix;
        } else {
            this.nameSuffix = ' <i class="xds-suffix-' + a + '">&nbsp;' + b + "&nbsp;</i>";
        }
        this.setName(this.name);
    },    
    setConfigWithSuffix: function (configName, configValue) {
        this.setSuffix(configValue);
        this.setConfig(configName, configValue);
    },
    getSnapToGrid: function (a) {
        return !this.snapToGrid ? "(none)" : this.snapToGrid;
    },
    setSnapToGrid: function (b, a) {
        this.snapToGrid = a == "(none)" ? 0 : parseInt(a, 10);
    },
    updateNodeText: function () {
        var cfg=this.getConfig();
        this.getNode().setText((cfg.title || cfg.header || cfg.fieldLabel || cfg.boxLabel || cfg.text || cfg.name || this.defaultName) + this.nameSuffix);
    },
    getConfig: function () {
        if (!this.config) {
            this.config = Ext.apply({
                xtype: this.xtype
            }, this.defaultConfig);
            this.initConfig(this.config, this.owner);
            Ext.apply(this.config, this.userConfig);
        }
        return this.config;
    },
    getJsonConfig: function (includeChild) {
        var jsonCfg = Ext.apply({
            xtype: this.xtype,
            dock: this.dock,
            xcls: this.xcls
        }, this.defaultConfig);
        this.initConfig(jsonCfg, this.owner);
        Ext.apply(jsonCfg, this.userConfig);
        if (this.layoutConfig) {
            jsonCfg.layoutConfig = this.layoutConfig;
        }
        if (this.userXType) {
            jsonCfg.userXType = this.userXType;
        }
        if (includeChild) {
            var f = this.getNode();
            if (f.hasChildNodes()) {
                jsonCfg.cn = [];
                for (var b = 0, e; e = f.childNodes[b]; b++) {
                    jsonCfg.cn.push(e.component.getJsonConfig(true));
                }
            }
        }
        for (var i in jsonCfg) {
            if (Ext.isEmpty(jsonCfg[i])) {
                delete jsonCfg[i];
            }
        }
        return jsonCfg;
    },
    getConfigValue: function (b, a) {
        return this.getConfig()[b] || a;
    },
    isSet: function (a) {
        return this.userConfig[a] !== undefined;
    },
    initConfig: function (b, a) {},
    nextId: function () {
        return xds.inspector.nextId(this.naming);
    },
    getNode: function () {
        if (!this.node) {
            var cfg = this.getConfig();
            var attrs = this.attrs = { //b:attrs
                id: this.id,
                //text: !this.owner ? this.id : (this.name.replace(/</, '&lt;').replace(/>/, '&gt;') || this.defaultName),
                text: cfg.title || cfg.header || cfg.fieldLabel || cfg.boxlabel || cfg.text || cfg.name || this.defaultName,
                iconCls: this.iconCls,
                leaf: true
            };
            if (this.isContainer || this.bindable) {
                attrs.leaf = false;
                attrs.children = [];
                //attrs.expanded = true
            }
            this.node = new Ext.tree.TreeNode(attrs);
            this.node.component = this;
        }
        return this.node;
    },
    getFilm: function () {
        return Ext.get("film-for-" + this.id);
    },
    isValidChild: function (b) {
        var a = xds.Registry.get(b);
        if (a.prototype.isPlugin) {
            return true;
        }
        if (this.bindable) {
            if ((/store|jsonstore|xmlstore|directstore/).exec(b)) {
                return true;
            }
        }
        if (this.isContainer) {
            if (this.validChildTypes) {
                return this.validChildTypes.contains(b);
            }
            return xds.Registry.get(b).prototype.isVisual !== false;
        }
        return false;
    },
    isValidParent: function (a) {
        return this.isVisual ? true : !! a;
    },
    getConfigs: function () {
        return this.configs;
    },
    getConfigObject: function (c) {
        if (this.configs.map[c]) {
            return this.configs.map[c];
        } else {
            var e = this.getLayoutConfigs();
            if (e && e.map[c]) {
                return e.map[c];
            } else {
                var b = this.getEditorConfigs();
                if (b && b.map[c]) {
                    return b.map[c];
                } else {
                    var d = this.getContainerConfigs();
                    if (d && d.map[c]) {
                        return d.map[c];
                    }
                    var a = this.getCmpPluginConfigs();
                    if (a) {
                        return a.map[c];
                    }
                }
            }
        }
    },
    getContainerConfigs: function () {
        var a = this.getConfigValue("layout");
        if (a && a != "auto") {
            return xds.Layouts[a].layoutConfigs;
        }
        return null;
    },
    setContainerConfig: function (a, b) {
        this.layoutConfig = this.layoutConfig || {};
        this.layoutConfig[a] = b;
    },
    getContainerConfigValue: function (a) {
        return this.layoutConfig ? this.layoutConfig[a] : undefined;
    },
    getLayoutConfigs: function () {
        var a = this.owner;
        var b;
        if (a) {
            b = a.getConfigValue("layout") || a.defaultLayout;
            if (b && b != "auto") {
                return xds.Layouts[b].configs;
            }
        }
        return null;
    },
    getCmpPluginConfigs: function () {
        var f = new Ext.util.MixedCollection(false, function (h) {
            return h.name;
        });
        var b = this.getPlugins();
        var g;
        if (b.length) {
            for (var d = 0, e = b.length; d < e; d++) {
                g = b[d].component.componentConfigs;
                if (g) {
                    for (var c = 0, a = g.length; c < a; c++) {
                        f.add(new xds.Config.types[g[c].ctype](g[c]));
                    }
                }
            }
        }
        return f;
    },
    getCommonConfigs: function () {
        if (!this.configs.common) {
            this.configs.common = this.configs.filterBy(function (a) {
                return xds.commonConfigs.indexOf(a.name) !== - 1;
            });
        }
        return this.configs.common;
    },
    getEditorConfigs: function () {
        if (this.owner) {
            return false;
        }
        return xds.editorConfigs;
    },
    createCanvasConfig: function (g) {
        var f = Ext.apply({}, this.getConfig());
        if (g.component.isPlugin) {
            f.ptype = this.dtype;
        } else {
            f.xtype = this.dtype;
        }
        f.stateful = false;
        f.viewerNode = g;
        if (this.layoutConfig) {
            f.layoutConfig = Ext.apply({}, this.layoutConfig);
        }
        if (this.snapToGrid && this.showGrid && f.layout == "absolute") {
            var b = "xds-grid-" + this.snapToGrid;
            f.bodyCssClass = f.bodyCssClass ? f.bodyCssClass + b : b;
        }
        //patch by alex
        //this.activeCmpId = f.id = Ext.id();
        if (this.cid == 'gridcolumn') {
            this.activeCmpId = f.id; //autoExtendColumn ref this id,so pls do not regrenerate
        } else {
            this.activeCmpId = f.id = Ext.id();
        }
        var e;
        if (g.hasChildNodes()) {
            f.items = [];
            for (var d = 0, a = g.childNodes.length; d < a; d++) {
                e = g.childNodes[d].component.createCanvasConfig(g.childNodes[d]);
                var refO = this.getReferenceForConfig(g.childNodes[d].component, e);
                if (refO.type === "string") {
                    f[refO.ref] = e;
                } else {
                    if (refO.type === "array") {
                        f[refO.ref] = f[refO.ref] || [];
                        f[refO.ref].push(e);
                    }
                }
            }
            if (f.items.length < 1) {
                delete f.items;
            }
        }
        return f;
    },
    getActions: function () {
        return null;
    },
    syncFilm: function () {
        if (this.isVisual !== false) {
            var a = Ext.getCmp(this.activeCmpId);
            if (a) {
                a.syncFilm();
            }
        }
    },
    getExtComponent: function () {
        return Ext.getCmp(this.activeCmpId);
    },
    isResizable: function () {
        return false;
    },
    onFilmClick: function (b, a) {},
    getLabel: function (f) {
        var a;
        var d = this.getExtComponent();
        if (d) {
            var c = d.el.up(".x-form-item", 3);
            if (c) {
                a = c.down(".x-form-item-label");
            }
            var b = d.el.next(".x-form-cb-label");
            if (a && a.getRegion().contains(f.getPoint())) {
                return {
                    el: a,
                    name: "fieldLabel"
                };
            } else {
                if (b && b.getRegion().contains(f.getPoint())) {
                    return {
                        el: b,
                        name: "boxLabel"
                    };
                }
            }
        }
        return null;
    },
    onFilmDblClick: function (b) {
        var a = this.getLabel(b);
        if (a) {
            xds.canvas.startEdit(this, a.el, this.getConfigObject(a.name));
        }
    },
    onSelectChange: function (a) {
        this.selected = a;
    },
    onFilmMouseDown: function (a) {
        if (this.enableFlyout && a.getTarget("b", 1)) {
            this.delegateFlyout(a);
        }
    },
    delegateFlyout: function (a) {
        if (this.enableFlyout) {
            if (!this.flyout) {
                this.getNode().select();
                this.flyout = this.onFlyout(a);
                if (this.flyout && !this.flyout.isVisible()) {
                    this.flyout.showBy(this.getFlyoutButton(), "tl-tr?");
                }
            } else {
                this.flyout.destroy();
            }
        }
    },
    getFlyoutButton: function () {
        var a = this.getFilm();
        return a ? a.child("b") : null;
    },
    hasConfig: function (a, b) {
        return this.getConfigValue(a) === b;
    },
    getInternals: function (a) {
        var d = {
            cid: this.cid,
            name: !this.owner ? this.id : (this.name || this.defaultName),
            dock: this.dock,
            layoutConfig: xds.copy(this.layoutConfig),
            userConfig: xds.copy(this.userConfig)
        };
        if (Ext.countKeys(d.layoutConfig) === 0) {
            delete d.layoutConfig;
        }
        if (Ext.countKeys(d.userConfig) === 0) {
            delete d.userConfig;
        }
        if (this.userXType) {
            d.userXType = this.userXType;
        }
        if (a) {
            var f = this.getNode();
            if (f.hasChildNodes()) {
                d.cn = [];
                for (var b = 0, e; e = f.childNodes[b]; b++) {
                    d.cn.push(e.component.getInternals(true));
                }
            }
        }
        return d;
    },
    getPlugins: function () {
        var a = [];
        var e = this.getNode();
        if (e.hasChildNodes()) {
            for (var b = 0, d; d = e.childNodes[b]; b++) {
                if (d.component.isPlugin) {
                    a.push(d);
                }
            }
        }
        return a;
    },
    getReferenceForConfig: function (b, a) {
        var d;
        var c = "string";
        if (b.dock) {
            d = b.dock;
        } else {
            if (b.isStore) {
                d = "store";
            } else {
                if (b.isPlugin) {
                    d = "plugins";
                    c = "array";
                } else {
                    d = "items";
                    c = "array";
                }
            }
        }
        return {
            ref: d,
            type: c
        };
    },
    getDefaultInternals: function (a) {
        a = a || {};
        Ext.apply(a, {
            cid: this.cid
        });
        var e = a.cn;
        if (e) {
            for (var b = 0, d = e.length; b < d; b++) {
                var c = xds.Registry.get(e[b].cid);
                e[b] = c.prototype.getDefaultInternals(e[b], this);
            }
        }
        return a;
    },
    getSpec: function (a) {
        return this.spec || this.getDefaultInternals({}, a && a.component);
    },
    beforeRemove: function () {
        if (this.flyout) {
            this.flyout.destroy();
        }
    },
    isAnchored: function () {
        var a = this.owner ? this.owner.getConfigValue("layout") : "";
        return a && this.getConfigValue("anchor") && (a == "form" || a == "anchor" || a == "absolute");
    },
    isFit: function () {
        var a = this.owner ? this.owner.getConfigValue("layout") : "";
        return a == "fit" || a == "card";
    },
    usesBoxLayout: function () {
        return this.hasConfig("layout", "hbox") || this.hasConfig("layout", "vbox");
    },
    setComponentX: function (b, a) {
        b.setPosition(a);
    },
    setComponentY: function (a, b) {
        a.setPosition(undefined, b);
    },
    getStoreNode: function () {
        var a = this.getNode().firstChild;
        while (a) {
            if (a.component.isStore) {
                return a;
            }
            a = a.nextSibling;
        }
        return null;
    },
    getTransforms: function () {
        var c = xds.TransformGroups[this.transformGroup] || [];
        var a = [];
        for (var b = 0, d = c.length; b < d; b++) {
            if (!((this.owner && (c[b] === "viewport" || c[b] === "window")) || c[b] === this.cid)) {
                a.push({
                    transtype: c[b],
                    text: xds.Registry.get(c[b]).prototype.text
                });
            }
        }
        return a;
    }
});
xds.types.BaseType.getFilmEl = function () {
    var a = this.getPositionEl();
    if (this.fieldLabel) {
        return this.el.up(".x-form-item") || a;
    }
    return a;
};
xds.types.BaseType.isValidDrop = function (a, b) {
    return a != b && (!a || a.isValidChild(b.cid)) && b.isValidParent(a);
};
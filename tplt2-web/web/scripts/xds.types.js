Ext.ns("xds.types");
xds.types.BaseType = Ext.extend(Ext.util.Observable, {
    isContainer: false,
    bindable: false,
    isVisual: true,
    nameSuffix: "",
    filmCls: "",
    minWidth: 10,
    minHeight: 10,
    snapToGrid: 10,
    showGrid: true,
    defaultLayout: "auto",
    currentSpec: - 1,
    finalSpec: - 1,
    constructor: function (cfg) {
        xds.types.BaseType.superclass.constructor.call(this);
        Ext.apply(this, cfg);
        this.userConfig = this.userConfig || {};
        this.name = this.name || this.defaultName;
        this.id = this.id || this.nextId();
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
    setOwner: function (o) {
        this.owner = o;
        delete this.config;
    },
    setConfig: function (n, v) {
        this.userConfig[n] = v;
        if (this.config) {
            this.config[n] = v;
        }
        this._updateNodeText();
        if (n == "layout") {
            if (v != "card") {
                delete this.config.activeItem;
                delete this.userConfig.activeItem;
            }
            delete this.layoutConfig;
            if (xds.Layouts[v] && xds.Layouts[v].onInit) {
                xds.Layouts[v].onInit(this.getNode());
            }
        }
//        xds.props.refresh();
    },
    _updateNodeText: function () {
        var cfg = this.getConfig();
        this.getNode().setText((cfg.title || cfg.header || cfg.fieldLabel || cfg.boxLabel || cfg.text || cfg.name || this.defaultName) + this.nameSuffix);
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
            new Function(handler.params, handler.value);
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
    setSuffix: function (text, type) {
        type = type || "loaded";
        if (!text) {
            delete this.nameSuffix;
        } else {
            this.nameSuffix = ' <i class="xds-suffix-' + type + '">&nbsp;' + text + "&nbsp;</i>";
        }
        this._updateNodeText();
    },
    setConfigWithSuffix: function (configName, configValue) {
        this.setSuffix(configValue);
        this.setConfig(configName, configValue);
    },
    setRegion:function(name,value) {
    	this.setConfigWithSuffix(name,value);

        switch (value) {
	        case "center":
	            return;
	        case "east":
	        case "west":
	            g = "width";
	            break;
	        case "north":
	        case "south":
	            g = "height";
	            break;
        }
        this.setConfig(g, this.getConfigValue(g) || 200);
    },
    getSnapToGrid: function (a) {
        return !this.snapToGrid ? "(none)" : this.snapToGrid;
    },
    setSnapToGrid: function (b, a) {
        this.snapToGrid = a == "(none)" ? 0 : parseInt(a, 10);
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
    getConfigs: function () {
        return this.configs;
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
                }
            }
        }
    },
    getConfigValue: function (b, a) {
        return this.getConfig()[b] || a;
    },
    initConfig: function (b, a) {},
    nextId: function () {
        return xds.inspector.nextId(this.naming);
    },
    getNode: function () {
        if (!this.node) {
            var cfg = this.getConfig();
            var attrs = {
                id: this.id,
                text: cfg.title || cfg.header || cfg.fieldLabel || cfg.boxlabel || cfg.text || cfg.name || this.defaultName,
                iconCls: this.iconCls
            };
            if (this.isContainer || this.bindable) {
                attrs.leaf = false;
                attrs.children = [];
            } else {
                attrs.leaf = true;
            }
            this.node = new Ext.tree.TreeNode(attrs);
            this.node.component = this;
        }
        return this.node;
    },
    getFilm: function () {
        return Ext.get("film-for-" + this.id);
    },
    isValidChild: function (cid) {
        if (this.bindable) {
            if ((/store|jsonstore|xmlstore|directstore/).exec(cid)) {
                return true;
            }
        }
        if (this.isContainer) {
            if (this.validChildTypes) {
                return this.validChildTypes.contains(cid);
            }
            return xds.Registry.get(cid).prototype.isVisual !== false;
        }
        return false;
    },
    isValidParent: function (a) {
        return this.isVisual ? true : !! a;
    },
    getContainerConfigs: function () {
        var a = this.getConfigValue("layout");
        if (xds.Layouts[a]) {
            return xds.Layouts[a].layoutConfigs;
        }
    },
    setContainerConfig: function (a, b) {
        this.layoutConfig = this.layoutConfig || {};
        this.layoutConfig[a] = b;
    },
    getContainerConfigValue: function (a) {
        return this.layoutConfig ? this.layoutConfig[a] : undefined;
    },
    getLayoutConfigs: function () {
        var owner = this.owner;
        if (owner) {
            layoutType = owner.getConfigValue("layout") || owner.defaultLayout;
            if (xds.Layouts[layoutType]) {
                return xds.Layouts[layoutType].configs;
            }
        }
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
        f.xtype = this.dtype;
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
        //this.cmpId = f.id = Ext.id();
        if (this.cid == 'gridcolumn') {
            this.cmpId = f.id; //autoExtendColumn ref this id,so pls do not regrenerate
        } else {
            this.cmpId = f.id = Ext.id();
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
            var a = Ext.getCmp(this.cmpId);
            if (a) {
                a.syncFilm();
            }
        }
    },
    getExtComponent: function () {
        return Ext.getCmp(this.cmpId);
    },
    isResizable: function () {
        return false;
    },
    getLabel: function (f) {
        var a, d = this.getExtComponent();
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
    onFilmClick: function (b, a) {},
    onFilmMouseDown: function (a) {
    },
    hasConfig: function (a, b) {
        return this.getConfigValue(a) === b;
    },
    takeSnapshot: function () {
        var a = this.getInternals(true);
        this.priorSpecs[++this.currentSpec] = a;
        this.finalSpec++;
        if (this.currentSpec > 0) {
            xds.actions.undo.enable();
            xds.actions.saveAction.enable();
        }
        xds.actions.redo.disable();
    },
    getInternals: function (includeChild) {
        var ret = {
            cid: this.cid
        };
        if (this.dock) {
            ret.dock = this.dock;
        }
        if (xds.countKeys(this.layoutConfig) != 0) {
            ret.layoutConfig = xds.copy(this.layoutConfig);
        }
        if (xds.countKeys(this.userConfig) != 0) {
            ret.userConfig = xds.copy(this.userConfig);
        }
        if (this.userXType) {
            ret.userXType = this.userXType;
        }
        if (includeChild) {
            var f = this.getNode();
            if (f.hasChildNodes()) {
                ret.cn = [];
                for (var b = 0, e; e = f.childNodes[b]; b++) {
                    ret.cn.push(e.component.getInternals(true));
                }
            }
        }
        return ret;
    },
    getReferenceForConfig: function (b, a) {
        var d, c = "string";
        if (b.dock) {
            d = b.dock;
        } else {
            if (b.isStore) {
                d = "store";
            } else {
                d = "items";
                c = "array";
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

xds.types.Module = Ext.extend(xds.types.BaseType, {
    cid: "module",
    defaultName: "NewModule",
    text: "NewModule",
    iconCls: "icon-project",
    isContainer: false,
    isVisual: false,
    layoutable: false,
    constructor: function (cfg) {
        xds.types.Module.superclass.constructor.call(this);
    },
    getXdsJson: function () {
        var ret = this.getConfig();
        if (this.dbId) {
            ret.dbId = this.dbId;
        }
        if (xds.countKeys(this.userConfig) > 0) {
            ret.userConfig = this.userConfig;
        }
        ret.components = [];
        var node = xds.inspector.root.firstChild;
        while (node) {
            ret.components.push(node.component.getInternals(true));
            node = node.nextSibling;
        }

        ret.version = this.version || 0;

        return ret;
    },
    getModuleConfig: function () {
        var node = xds.inspector.root.firstChild;
        var tmp = [];
        while (node) {
            var jsonConfig = node.component.getJsonConfig(true);
            jsonConfig = Ext.ux.CodeGenerator.parse_object(jsonConfig, false);
            tmp.push(Ext.ux.CodeGenerator.objToString(jsonConfig, null, '', false, false));
            node = node.nextSibling;
        }
        var jsonCfg = this.getJsonConfig(false);
        var cfgStr = "{moduleId:'" + this.getModuleId() + "'";
        if (!Ext.isEmpty(jsonCfg.iconCls)) {
            cfgStr += ",iconCls:'" + jsonCfg.iconCls + "'";
        }
        var mName = jsonCfg.name;
        if (Ext.isEmpty(mName)) {
            mName = this.getModuleId();
        }
        cfgStr += ",moduleName:'" + mName + "'";
        var cat = jsonCfg.category;
        if (!Ext.isEmpty(cat)) {
            cfgStr += ",category:'" + cat + "'";
        }
        var dftComp = this.getDefaultComponent();
        if (!Ext.isEmpty(dftComp)) {
            cfgStr += ",defaultComponent:'" + dftComp + "'";
        }
        if (xds.countKeys(jsonCfg.evtHandlers) > 0) {
            cfgStr += ",listeners:{";
            for (var evt in jsonCfg.evtHandlers) {
                if (typeof(jsonCfg.evtHandlers[evt]) == 'object') {
                    cfgStr += evt + ' : function (' + jsonCfg.evtHandlers[evt].params.join(',') + ') {' + jsonCfg.evtHandlers[evt].value + '},';
                }
            }
            cfgStr = cfgStr.substring(0, cfgStr.length - 1);
            cfgStr += '}';
        }

        var version = this.version || 0;
        cfgStr+=",version:"+version;

        cfgStr += ",components:[" + tmp.join(',') + "]}";
        return cfgStr;
    },
    initConfig: function (config, a) {
        config.id = xds.project.id;
        config.defaultComponent = xds.project.defaultComponent;
        config.iconCls = xds.project.iconCls;
        config.name = xds.project.name;
        config.category = xds.project.category;
        this.userConfig = Ext.apply(this.userConfig, (xds.project.userConfig || {}));
    },
    getTopComponent: function () {
        return this;
    },
    getCommonConfigs: function () {
        return null;
    },
    getEditorConfigs: function () {
        return null;
    },
    getNode: function () {
        return xds.inspector.root;
    },
    getModuleId: function () {
        var id = this.getConfigValue('id');
        if (Ext.isEmpty(id)) {
            id = Ext.id();
        }
        return id;
    },
    getDefaultComponent: function () {
        var ret = this.getConfigValue("defaultComponent");
        if (Ext.isEmpty(ret)) {
            var node = xds.inspector.root.firstChild;
            if (!Ext.isEmpty(node)) {
                ret = node.component.getConfigValue("id");
            }
        }
        return ret;
    },
    xdConfigs: [{
        name: "id",
        group: "Module",
        ctype: "string"
    },{
        name: "iconCls",
        group: "Module",
        ctype: "string",
        updateFn: function (eCmp, value, cmp) {
            var node = xds.inspector.root;
            var ui = node.ui;
            if (ui && ui.iconNode) {
                Ext.fly(ui.iconNode).replaceClass(node.attributes.iconCls, value);
            }
            node.attributes.iconCls = value;
        }
    },{
        name: "name",
        group: "Module",
        ctype: "string"
    },{
        name: "category",
        group: "Module",
        ctype: "string"
    },{
        name: "defaultComponent",
        group: "Module",
        ctype: "string",
        editor: "options",
        getOptions: function () {
            var comps = [];
            var root = xds.inspector.root;
            var node = root.firstChild;
            while (node) {
                if (node.component.getConfig().id) {
                    comps.push(node.component.getConfig().id);
                }
                node = node.nextSibling;
            }
            return comps;
        }
    },{
        name: "active",
        group: "(Event)",
        ctype: "fn",
        params: ["module"]
    },{
        name: "init",
        group: "(Event)",
        ctype: "fn",
        params: ["module"]
    }]
});

//xds.Registry.register(xds.types.Module);

xds.types.Component = Ext.extend(xds.types.BaseType,{
    xdConfigs: [{
        name: "cls",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "ctCls",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "disabled",
        group: "Ext.Component",
        ctype: "boolean"
    },{
        name: "disabledClass",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "fieldLabel",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "hidden",
        group: "Ext.Component",
        ctype: "boolean"
    },{
        name: "hideLabel",
        group: "Ext.Component",
        ctype: "boolean"
    },{
        name: "hideMode",
        group: "Ext.Component",
        ctype: "string",
        editor: "options",
        options: ["display", "offsets", "visibility"]
    },{
        name: "hideParent",
        group: "Ext.Component",
        ctype: "boolean"
    },{
        name: "html",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "id",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "labelSeparator",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "labelStyle",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "overCls",
        group: "Ext.Component",
        ctype: "string"
    },{
    	name: "permissionId",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "ref",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "stateEvents",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "stateId",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "stateful",
        group: "Ext.Component",
        ctype: "boolean"
    },{
        name: "tpl",
        group: "Ext.Component",
        ctype: "text"
    },{
        name: "style",
        group: "Ext.Component",
        ctype: "string"
    }]
});

//xds.Registry.register(xds.types.Component);

xds.types.BoxComponent = Ext.extend(xds.types.Component, {
    cid: "boxcomponent",
    category: "Standard",
    defaultName: "&lt;boxcomponent&gt;",
    text: "BoxComponent",
    dtype: "xdboxcomponent",
    xtype: "boxcomponent",
    xcls: "Ext.BoxComponent",
    iconCls: "icon-boxcomponent",
    naming: "MyBoxComponent",
    isContainer: false,
    defaultConfig: {},
    initConfig: function (b, a) {
        //b.width = this.defaultWidth;
        //b.height = this.defaultHeight;
    },
    isResizable:function(){
    	return true;
    },
    xdConfigs: [{
        name: "anchor",
        group: "Ext.BoxComponent",
        ctype: "string"
    },{
        name: "autoWidth",
        group: "Ext.BoxComponent",
        ctype: "boolean"
    },{
        name: "autoScroll",
        group: "Ext.BoxComponent",
        ctype: "boolean"
    },{
        name: "autoWidth",
        group: "Ext.BoxComponent",
        ctype: "boolean"
    },{
        name: "boxMaxHeight",
        group: "Ext.BoxComponent",
        ctype: "number"
    },{
        name: "boxMaxWidth",
        group: "Ext.BoxComponent",
        ctype: "number"
    },{
        name: "boxMinHeight",
        group: "Ext.BoxComponent",
        ctype: "number"
    },{
        name: "boxMinWidth",
        group: "Ext.BoxComponent",
        ctype: "number"
    },{
        name: "flex",
        group: "Ext.BoxComponent",
        ctype: "number"
    },{
        name: "height",
        group: "Ext.BoxComponent",
        ctype: "number",
        updateFn: "setHeight"
    },{
        name: "margins",
        group: "Ext.BoxComponent",
        ctype: "string"
    },{
        name: "pageX",
        group: "Ext.BoxComponent",
        ctype: "number"
    },{
        name: "pageY",
        group: "Ext.BoxComponent",
        ctype: "number"
//    },{
//        name: "region",
//        group: "Ext.BoxComponent",
//        ctype: "string",
//        editor: "options",
//        options: ["west","east","north","south","center"]
    },{
        name: "width",
        group: "Ext.BoxComponent",
        ctype: "number"
    },{
        name: "x",
        group: "Ext.BoxComponent",
        ctype: "number"
    },{
        name: "y",
        group: "Ext.BoxComponent",
        ctype: "number"
    }]
});
//xds.Registry.register(xds.types.BoxComponent);
xds.BoxComponent = Ext.extend(Ext.BoxComponent, {});
Ext.reg("xdboxcomponent", xds.BoxComponent);


xds.types.Container = Ext.extend(xds.types.BoxComponent, {
    cid: "container",
    category: "Containers",
    defaultName: "&lt;container&gt;",
    text: "Container",
    dtype: "xdcontainer",
    xtype: "container",
    xcls: "Ext.Container",
    iconCls: "icon-container",
    naming: "MyContainer",
    isContainer: true,
    transformGroup: "container",
//    defaultConfig: {
//        layout: 'fit'
//    },
    initConfig: function (d, b) {
        var c = this.owner ? this.owner.getConfigValue("layout") : "";
        if (c == "form" || c == "anchor") {
            d.anchor = "100%";
        }
    },
    isResizable: function (a, b) {
        return !this.isFit() && !this.isAnchored();
    },
    xdConfigs: [{
        name: "activeItem",
        group: "Ext.Container",
        ctype: "string"
    },{
        name: "autoDestroy",
        group: "Ext.Container",
        ctype: "boolean"
    },{
        name: "bufferResize",
        group: "Ext.Container",
        ctype: "string"
    },{
        name: "defaultType",
        group: "Ext.Container",
        ctype: "string"
    },{
        name: "forceLayout",
        group: "Ext.Container",
        ctype: "boolean"
    },{
        name: "hideBorders",
        group: "Ext.Container",
        ctype: "boolean"
    },{
        name: "layout",
        group: "Ext.Container",
        ctype: "string",
        editor: "options",
        options: xds.layouts
    },{
        name: "monitorResize",
        group: "Ext.Container",
        ctype: "boolean"
    },{
        name: "resizeEvent",
        group: "Ext.Container",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.Container);
xds.Container = Ext.extend(Ext.Container, {});
Ext.reg("xdcontainer", xds.Container);


xds.PanelBase = Ext.extend(xds.types.Container, {
    category: "Containers",
    isContainer: true,
    isResizable: function (b, f) {
        var a = this.owner ? this.owner.getConfigValue("layout", "") : "";
        var c = false;
        switch (a) {
        case "":
        case "auto":
        case "absolute":
            c = true;
            break;
        case "border":
            var d = this.getConfigValue("region");
            c = (d !== "center");
            break;
        case "hbox":
        case "vbox":
            c = !this.getConfigValue("flex");
            break;
        }
        return c;
    },
    initConfig: function (b, a) {
        if (!a) {
            b.width = 400;
            b.height = 250;
        }
    },
    autoScrollable: true,
    layoutable: true,
    getPanelHeader: function () {
        var a = this.getExtComponent();
        if (a.header && a.headerAsText) {
            return a.header.child("span");
        }
        return null;
    },
    onFilmDblClick: function (a) {
        var b = this.getPanelHeader();
        if (b && b.getRegion().contains(a.getPoint())) {
            this.startTitleEdit(b);
        } else {
            xds.PanelBase.superclass.onFilmDblClick.call(this, a);
        }
    },
    startTitleEdit: function (a) {
        xds.canvas.startEdit(this, a || this.getPanelHeader(), this.getConfigObject("title"), 150);
    }
});

xds.types.Panel = Ext.extend(xds.PanelBase, {
    cid: "panel",
    defaultName: "&lt;panel&gt;",
    text: "Panel",
    dtype: "xdpanel",
    xtype: "panel",
    defaultWidth: 400,
    defaultHeight: 250,
    xcls: "Ext.Panel",
    iconCls: "icon-panel",
    naming: "MyPanel",
    initConfig: function (b, a) {
        if (!a) {
            b.width = this.defaultWidth;
            b.height = this.defaultHeight;
            b.title = 'My Panel';
        }
    },
    transformGroup: "container",
    xdConfigs: [{
        name: "animCollapse",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "autoHeight",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "baseCls",
        group: "Ext.Panel",
        ctype: "string"
    },{
        name: "bodyBorder",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "bodyStyle",
        group: "Ext.Panel",
        ctype: "string"
    },{
        name: "bodyCssClass",
        group: "Ext.Panel",
        ctype: "string"
    },{
        name: "border",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "buttonAlign",
        group: "Ext.Panel",
        ctype: "string",
        editor: "options",
        options: ["center", "left", "right"]
    },{
        name: "closable",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "collapsedCls",
        group: "Ext.Panel",
        ctype: "string"
    },{
        name: "collapseFirst",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "collapsible",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "collapsed",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "disabled",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "draggable",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "elements",
        group: "Ext.Panel",
        ctype: "string"
    },{
        name: "floating",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "footer",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "frame",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "header",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "headerAsText",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "hideCollapseTool",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "iconCls",
        group: "Ext.Panel",
        ctype: "string",
        editor: "icon"
    },{
        name: "maskDisabled",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "minButtonWidth",
        group: "Ext.Panel",
        ctype: "number"
    },{
        name: "padding",
        group: "Ext.Panel",
        ctype: "string"
    },{
        name: "ref",
        group: "Ext.Component",
        ctype: "string"
    },{
        name: "shadow",
        group: "Ext.Panel",
        ctype: "string",
        editor: "options",
        options: ["sides", "drop", "frame"]
    },{
        name: "shadowOffset",
        group: "Ext.Panel",
        ctype: "number"
    },{
        name: "shim",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "title",
        group: "Ext.Panel",
        ctype: "string"
    },{
        name: "titleCollapse",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "unstyled",
        group: "Ext.Panel",
        ctype: "boolean"
    },{
        name: "beforerender",
        group: "(Event)",
        ctype: "fn",
        params: ['panel']
    },{
        name: "afterrender",
        group: "(Event)",
        ctype: "fn",
        params: ['panel']
    }]
});
//xds.Registry.register(xds.types.Panel);
xds.types.Panel.transform = function (b, a) {
    var c = b.getInternals(true);
    if (c.userConfig && typeof c.userConfig.activeItem !== "undefined") {
        delete c.userConfig.activeItem;
    }
    return c;
};
xds.Panel = Ext.extend(Ext.Panel, {
    createFbar: function (b) {
        var a = this.minButtonWidth;
        this.elements += ",footer";
        this.fbar = this.createToolbar(b, {
            buttonAlign: this.buttonAlign,
            toolbarCls: "x-panel-fbar",
            enableOverflow: false,
            defaults: function (d) {
                return {
                    minWidth: d.minWidth || a
                };
            }
        });
        if (this.fbar.items) {
            this.fbar.items.each(function (d) {
                d.minWidth = d.minWidth || this.minButtonWidth;
            }, this);
            this.buttons = this.fbar.items.items;
        }
    }
});
Ext.reg("xdpanel", xds.Panel);

xds.types.TabPanel = Ext.extend(xds.types.Panel, {
    cid: "tabpanel",
    defaultName: "&lt;tabs&gt;",
    text: "TabPanel",
    dtype: "xdtabpanel",
    xtype: "tabpanel",
    xcls: "Ext.TabPanel",
    iconCls: "icon-tabs",
    naming: "MyTabs",
    layoutable: false,
    autoScrollable: false,
    defaultLayout: "card",
    defaultConfig: {
        activeTab: 0
    },
    transformGroup: "container",
    xdConfigs: [{
        name: "activeTab",
        group: "Ext.TabPanel",
        ctype: "string",
        updateFn: "setActiveTab"
    },{
        name: "animScroll",
        group: "Ext.TabPanel",
        ctype: "boolean"
    },{
        name: "autoTabSelector",
        group: "Ext.TabPanel",
        ctype: "string"
    },{
        name: "autoTabs",
        group: "Ext.TabPanel",
        ctype: "boolean"
    },{
        name: "deferredRender",
        group: "Ext.TabPanel",
        ctype: "boolean"
    },{
        name: "enableTabScroll",
        group: "Ext.TabPanel",
        ctype: "boolean"
    },{
        name: "itemTpl",
        group: "Ext.TabPanel",
        ctype: "string"
    },{
        name: "layout",
        group: "Ext.TabPanel",
        ctype: "string",
        readonly: true
    },{
        name: "layoutOnTabChange",
        group: "Ext.TabPanel",
        ctype: "boolean"
    },{
        name: "minTabWidht",
        group: "Ext.TabPanel",
        ctype: "boolean"
    },{
        name: "plain",
        group: "Ext.TabPanel",
        ctype: "boolean"
    },{
        name: "resizeTabs",
        group: "Ext.TabPanel",
        ctype: "boolean"
    },{
        name: "scrollDuration",
        group: "Ext.TabPanel",
        ctype: "string"
    },{
        name: "scrollIncrement",
        group: "Ext.TabPanel",
        ctype: "number"
    },{
        name: "scrollRepeatInterval",
        group: "Ext.TabPanel",
        ctype: "number"
    },{
        name: "tabMargin",
        group: "Ext.TabPanel",
        ctype: "number"
    },{
        name: "tabCls",
        group: "Ext.TabPanel",
        ctype: "string"
    },{
        name: "tabPosition",
        group: "Ext.TabPanel",
        ctype: "string",
        editor: "options",
        options: ["top", "bottom"]
    },{
        name: "tabWidth",
        group: "Ext.TabPanel",
        ctype: "number"
    },{
        name: "wheelIncrement",
        group: "Ext.TabPanel",
        ctype: "number"
    },{
        name: "activate",
        group: "(Event)",
        ctype: "fn",
        params: ['panel']
    },{
        name: "add",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "added",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "afterlayout",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "afterrender",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "beforeadd",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "beforeclose",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "beforecollapse",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "beforedestory",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "beforeexpand",
        group: "(Event)",
        ctype: "fn"
    },{
        name: "tabchange",
        group: "(Event)",
        ctype: "fn",
        params: ['tabpanel', 'tab']
    }],
    getDefaultInternals: function () {
        return {
            cid: this.cid,
            cn: [{
                cid: "panel",
                userConfig: {
                    title: "Tab 1"
                }
            },
            {
                cid: "panel",
                userConfig: {
                    title: "Tab 2"
                }
            },
            {
                cid: "panel",
                userConfig: {
                    title: "Tab 3"
                }
            }]
        };
    },
    getTabTarget: function (d) {
        if (d.getTarget("b", 1)) {
            return false;
        }
        var g = this.getExtComponent();
        if (g) {
            var k = d.getPoint();
            var f = g.stripWrap.getRegion();
            if (!f.contains(k)) {
                return;
            }
            var j = g.stripWrap.dom.getElementsByTagName("li"),
                b = false;
            for (var a = 0, c = j.length - 1; a < c; a++) {
                var h = j[a];
                if (Ext.fly(h).getRegion().contains(k)) {
                    b = a;
                    break;
                }
            }
            return b;
        }
        return false;
    },
    getTabComponent: function (b) {
        var e = 0;
        var d = this.getNode();
        for (var a = 0, c; c = d.childNodes[a]; a++) {
            if (!c.dock) {
                if (e === b) {
                    return c.component;
                } else {
                    e++;
                }
            }
        }
        return null;
    },
    onFilmClick: function (d) {
        var b = this.getTabTarget(d);
        if (b !== false) {
            var a = this.getConfigObject("activeTab");
            a.setValue(this, b);
            if (xds.active && xds.active.component == this) {
                xds.props.setValue("activeTab", b);
            }
            var c = this.getTabComponent(b);
            if (c) {
                c.getNode().select();
                return false;
            }
        }
    },
    onFilmDblClick: function (d) {
        var a = this.getTabTarget(d);
        if (a !== false) {
            var c = this.getTabComponent(a);
            var b = this.getExtComponent().getTabEl(a);
            xds.canvas.startEdit(c, b, c.getConfigObject("title"), 100);
        }
    }
});
xds.types.TabPanel.transform = function (b, a) {
    var c = b.getInternals(true);
    if (c.userConfig && c.userConfig.layout && c.userConfig.layout == "accordion") {
        delete c.userConfig.layout;
    }
    return c;
};
//xds.Registry.register(xds.types.TabPanel);
xds.TabPanel = Ext.extend(Ext.TabPanel, {
    createFbar: function (fbar) {
        var min = this.minButtonWidth;
        this.elements += ',footer';
        this.fbar = this.createToolbar(fbar, {
            buttonAlign: this.buttonAlign,
            toolbarCls: 'x-panel-fbar',
            enableOverflow: false,
            defaults: function (c) {
                return {
                    minWidth: c.minWidth || min
                };
            }
        });
        if (this.fbar.items) {
            this.fbar.items.each(function (c) {
                c.minWidth = c.minWidth || this.minButtonWidth;
            }, this);
            this.buttons = this.fbar.items.items;
        }
    }
});
Ext.reg("xdtabpanel", xds.TabPanel);

xds.types.Viewport = Ext.extend(xds.types.Container, {
    cid: "viewport",
    category: "Containers",
    defaultName: "&lt;viewport&gt;",
    text: "Viewport",
    dtype: "xdviewport",
    xtype: "viewport",
    xcls: "Ext.Viewport",
    iconCls: "icon-viewport",
    naming: "MyViewport",
    //    enableFlyout: true,
    isContainer: true,
    filmCls: "el-film-btn-overlap",
    isValidParent: function (a) {
        return !a;
    },
    transformGroup: "container"
});
//xds.Registry.register(xds.types.Viewport);
xds.Viewport = Ext.extend(Ext.Panel, {
    baseCls: "page",
    frame: true,
    initComponent: function () {
        xds.Viewport.superclass.initComponent.call(this);
    },
    onShow: function () {
        xds.Viewport.superclass.onShow.call(this);
        this.onCanvasResize();
        xds.canvas.on("resize", this.onCanvasResize, this);
    },
    onHide: function () {
        xds.Viewport.superclass.onHide.call(this);
        xds.canvas.un("resize", this.onCanvasResize, this);
    },
    onCanvasResize: function () {
        this.setSize(xds.canvas.body.getStyleSize());
    }
});
Ext.reg("xdviewport", xds.Viewport);

xds.types.Window = Ext.extend(xds.types.Panel, {
    cid: "window",
    defaultName: "&lt;window&gt;",
    text: "Window",
    dtype: "xdwindow",
    xtype: "window",
    xcls: "Ext.Window",
    iconCls: "icon-window",
    naming: "MyWindow",
    isValidParent: function (a) {
        return !a || a.cid == "viewport";
    },
    isResizable: function (a, b) {
        return true;
    },
    defaultConfig: {
        width: 400,
        height: 250,
        title: "My Window",
        constrain:true
    },
    getDefaultInternals: function () {
        return xds.types.Window.superclass.getDefaultInternals.call(this, {
            cn: [{
                cid: 'toolbar',
                dock: 'fbar',
                cn: [{
                    cid: 'button',
                    userConfig: {
                        text: '确认',
                        ref: '../btnAccept'
                    }
                },
                {
                    cid: 'button',
                    userConfig: {
                        text: '取消',
                        ref: '../btnCancel',
                        "evtHandlers": {
                            "click": {
                                "value": "if (btn.refOwner) {\n    btn.refOwner.close();\n}",
                                "params": ['btn', 'evt']
                            }
                        }
                    }
                }]
            }]
        });
    },
    transformGroup: "container",
    xdConfigs: [{
    	name:"animateTarget",
    	group:"Ext.Window",
    	ctype:"string"
    },{
        name: "baseCls",
        group: "Ext.Window",
        ctype: "string"
    },{
        name: "closable",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "closeAction",
        group: "Ext.Window",
        ctype: "string",
        editor: "options",
        options: ["close", "hide"]
    },{
        name: "collapsed",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "constrain",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "constrainHeader",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "draggable",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "defaultButton",
        group: "Ext.Window",
        ctype: "string"
    },{
        name: "expandOnShow",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "hidden",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "hideAnimDuration",
        group: "Ext.Window",
        ctype: "number"
    },{
        name: "initHidden",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "maximizable",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "minHeight",
        group: "Ext.Window",
        ctype: "number"
    },{
        name: "minimizable",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "minWidth",
        group: "Ext.Window",
        ctype: "number"
    },{
        name: "modal",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "plain",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "resizable",
        group: "Ext.Window",
        ctype: "boolean"
    },{
        name: "resizeHandles",
        group: "Ext.Window",
        ctype: "string",
        editor: "options",
        options: ["all", "n" ,"s" ,"e","w","nw","sw","se","ne"]
    },{
        name: "show",
        group: "(Event)",
        ctype: "fn",
        params: ['win']
    },{
    	name: "beforeshow",
    	group: "(Event)",
    	ctype: "fn",
    	params: ['win']
    },{
        name: "resize",
        group: "(Event)",
        ctype: "fn",
        params: ['win', 'width', 'height']
    },{
        name: "afterlayout",
        group: "(Event)",
        ctype: "fn",
        params: ['win', 'layout']
    }]
});
//xds.Registry.register(xds.types.Window);
xds.Window = Ext.extend(Ext.Panel, {
    baseCls: "x-window",
    closable: true,
    elements: "header,body",
    frame: true,
    initEvents: function () {
        xds.Window.superclass.initEvents.call(this);
        if (this.minimizable) {
            this.addTool({
                id: "minimize"
            });
        }
        if (this.maximizable) {
            this.addTool({
                id: "maximize"
            });
        }
        if (this.closable) {
            this.addTool({
                id: "close"
            });
        }
    },
    createFbar: function (fbar) {
        var min = this.minButtonWidth;
        this.elements += ',footer';
        this.fbar = this.createToolbar(fbar, {
            buttonAlign: this.buttonAlign,
            toolbarCls: 'x-panel-fbar',
            enableOverflow: false,
            defaults: function (c) {
                return {
                    minWidth: c.minWidth || min
                };
            }
        });
        if (this.fbar.items) {
            this.fbar.items.each(function (c) {
                c.minWidth = c.minWidth || this.minButtonWidth;
            }, this);
            this.buttons = this.fbar.items.items;
        }
    },
    onRender: function () {
        xds.Window.superclass.onRender.apply(this, arguments);
        if (this.plain) {
            this.el.addClass("x-window-plain");
        }
    }
});
Ext.reg("xdwindow", xds.Window);
xds.types.Button = Ext.extend(xds.types.BoxComponent, {
    cid: "button",
    category: "Standard",
    defaultName: "&lt;cbutton&gt;",
    text: "Button",
    dtype: "xdbutton",
    xtype: "button",
    xcls: "Ext.Button",
    iconCls: "icon-button",
    naming: "MyButton",
    isContainer: true,
    filmCls: "el-film-nolabel",
    validChildTypes: ["menu"],
    transformGroup: "tbitem",
    initConfig: function (b, a) {
        if (Ext.isEmpty(b.text)) {
            b.text = "MyButton";
        }
    },
    xdConfigs: [{
        name: "allowDepress",
        group: "Ext.Button",
        ctype: "boolean"
    },{
        name: "arrowAlign",
        group: "Ext.Button",
        ctype: "string",
        editor: "options",
        options: ["bottom", "left", "right", "top"]
    },{
        name: "autoWidth",
        group: "Ext.Button",
        ctype: "boolean"
    },{
        name: "buttonSelector",
        group: "Ext.Button",
        ctype: "string"
    },{
        name: "clickEvent",
        group: "Ext.Button",
        ctype: "string",
        editor: "options",
        options: ["click", "mousedown"]
    },{
        name: "cls",
        group: "Ext.Button",
        ctype: "string"
    },{
        name: "disabled",
        group: "Ext.Button",
        ctype: "boolean"
    },{
        name: "enableToggle",
        group: "Ext.Button",
        ctype: "boolean"
    },{
        name: "handleMouseEvents",
        group: "Ext.Button",
        ctype: "boolean"
    },{
        name: "hidden",
        group: "Ext.Button",
        ctype: "boolean"
    },{
        name: "icon",
        group: "Ext.Button",
        ctype: "string"
    },{
        name: "iconAlign",
        group: "Ext.Button",
        ctype: "string",
        editor: "options",
        options: ["bottom", "left", "right", "top"]
    },{
        name: "iconCls",
        group: "Ext.Button",
        ctype: "string",
        editor: "icon"
    },{
        name: "menuAlign",
        group: "Ext.Button",
        ctype: "string"
    },{
        name: "minWidth",
        group: "Ext.Button",
        ctype: "number"
    },{
        name: "overflowText",
        group: "Ext.Button",
        ctype: "string"
    },{
        name: "pressed",
        group: "Ext.Button",
        ctype: "boolean"
    },{
        name: "repeat",
        group: "Ext.Button",
        ctype: "boolean"
    },{
        name: "scale",
        group: "Ext.Button",
        ctype: "string",
        editor: "options",
        options: ["small", "medium", "large"]
    },{
        name: "tabIndex",
        group: "Ext.Button",
        ctype: "number"
    },{
        name: "text",
        group: "Ext.Button",
        ctype: "string"
    },{
        name: "toggleGroup",
        group: "Ext.Button",
        ctype: "string"
    },{
        name: "tooltip",
        group: "Ext.Button",
        ctype: "string"
    },{
        name: "tooltipType",
        group: "Ext.Button",
        ctype: "string",
        editor: "options",
        options: ["title", "qtip"]
    },{
        name: "type",
        group: "Ext.Button",
        ctype: "string",
        editor: "options",
        options: ["button", "reset", "submit"]
    },{
        name: "click",
        group: "(Event)",
        ctype: "fn",
        params: ['btn', 'evt']
    }],
    onFilmDblClick: function (b) {
        var a = this.getExtComponent();
        xds.canvas.startEdit(this, a.el.child(a.buttonSelector), this.getConfigObject("text"), 80);
    },
    onFilmClick: function (b) {
        var a = this.getExtComponent();
        if (a.menu) {
            a.showMenu();
        }
    },
    getReferenceForConfig: function (c, b) {
        var a = xds.types.Menu.superclass.getReferenceForConfig.call(this, c, b);
        if (c.isMenu) {
            a.ref = "menu";
            a.type = "string";
        }
        return a;
    }
});
//xds.Registry.register(xds.types.Button);

xds.Button = Ext.extend(Ext.Button, {
    showMenu: function () {
        if (this.rendered && this.menu) {
            if (this.tooltip) {
                Ext.QuickTips.getQuickTip().cancelShow(this.btnEl);
            }
            this.menu.show(this.el, this.menuAlign);
        }
        return this;
    },
    onClick: function (a) {
        if (a) {
            a.preventDefault();
        }
        if (a.button !== 0) {
            return
        }
        if (!this.disabled) {
            if (this.enableToggle && (this.allowDepress !== false || !this.pressed)) {
                this.toggle();
            }
            if (this.menu && !this.menu.isVisible() && !this.ignoreNextClick) {
                this.showMenu();
            }
            this.fireEvent("click", this, a);
            if (this.handler) {
                this.handler.call(this.scope || this, this, a);
            }
        }
    }
});
Ext.reg("xdbutton", xds.Button);


xds.types.Label = Ext.extend(xds.types.BoxComponent, {
    cid: "label",
    category: "Standard",
    defaultName: "&lt;label&gt;",
    text: "Label",
    dtype: "xdlabel",
    xtype: "label",
    xcls: "Ext.form.Label",
    iconCls: "icon-label",
    naming: "MyLabel",
    filmCls: "el-film-nolabel",
    defaultConfig: {
        text: "Label:"
    },
    isResizable: function (a, b) {
        return a == "Right" && !this.getConfigValue("anchor") && (!this.owner || this.owner.getConfigValue("layout") != "form");
    },
    onFilmDblClick: function (b) {
        var a = this.getExtComponent();
        xds.canvas.startEdit(this, a.el, this.getConfigObject("text"));
    },
    xdConfigs: [{
        name: "forId",
        group: "Ext.form.Label",
        ctype: "string"
    },{
        name: "html",
        group: "Ext.form.Label",
        ctype: "string"
    },{
        name: "text",
        group: "Ext.form.Label",
        ctype: "string",
        updateFn: "setText"
    }]
});
//xds.Registry.register(xds.types.Label);
xds.Label = Ext.extend(Ext.form.Label, {});
Ext.reg("xdlabel", xds.Label);

xds.types.Slider = Ext.extend(xds.types.BoxComponent, {
    cid: "slider",
    category: "Standard",
    defaultName: "&lt;slider&gt;",
    text: "Slider",
    dtype: "xdslider",
    xtype: "slider",
    xcls: "Ext.Slider",
    iconCls: "icon-slider",
    naming: "MySlider",
    isContainer: false,
    filmCls: "el-film-nolabel",
    defaultWidth: 200,
    defaultHeight: 200,
    isResizable: function (a, b) {
        var c = this.getConfigValue("vertical", false);
        if ((c && a === "Bottom") || (!c && a == "Right")) {
            return true;
        } else {
            return false;
        }
    },
    setOrientation: function (c, b) {
        var d = this.getConfigValue("width", false);
        var a = this.getConfigValue("height", false);
        if (b && d) {
            this.setConfig("width", undefined);
            if (!a) {
                this.setConfig("height", d);
            }
        } else {
            if (!b && a) {
                this.setConfig("height", undefined);
                if (!d) {
                    this.setConfig("width", a);
                }
            }
        }
        this.setConfig(c, b);
    },
    initConfig: function (b, a) {
        if (!a) {
            b.width = this.defaultWidth;
        }
    },
    xdConfigs: [{
        name: "animate",
        group: "Ext.Slider",
        ctype: "boolean"
    },{
        name: "clickToChange",
        group: "Ext.Slider",
        ctype: "boolean"
    },{
        name: "constrainThumbs",
        group: "Ext.Slider",
        ctype: "boolean"
    },{
        name: "decimalPrecision",
        group: "Ext.Slider",
        ctype: "boolean"
    },{
        name: "increment",
        group: "Ext.Slider",
        ctype: "number"
    },{
        name: "keyIncrement",
        group: "Ext.Slider",
        ctype: "number"
    },{
        name: "maxValue",
        group: "Ext.Slider",
        ctype: "number"
    },{
        name: "minValue",
        group: "Ext.Slider",
        ctype: "number"
    },{
        name: "value",
        group: "Ext.Slider",
        ctype: "number"
    },{
        name: "vertical",
        group: "Ext.Slider",
        ctype: "boolean",
        setFn: "setOrientation"
    }],
    onFilmClick: function (b) {
        var f = this.getExtComponent();
        var d = f.innerEl.translatePoints(b.getXY());
        if (f.vertical) {
            var a = f.innerEl.getHeight() - d.top;
            xds.props.setValue("value", f.minValue + Ext.util.Format.round(a / f.getRatio(), f.decimalPrecision));
        } else {
            xds.props.setValue("value", Ext.util.Format.round(f.reverseValue(d.left), f.decimalPrecision));
        }
    }
});
//xds.Registry.register(xds.types.Slider);
xds.Slider = Ext.extend(Ext.Slider, {

});
Ext.reg("xdslider", xds.Slider);


xds.types.ProgressBar = Ext.extend(xds.types.BoxComponent, {
    cid: "progressbar",
    category: "Standard",
    defaultName: "&lt;progressbar&gt;",
    text: "ProgressBar",
    dtype: "xdprogressbar",
    xtype: "progressbar",
    xcls: "Ext.ProgressBar",
    iconCls: "icon-progressbar",
    naming: "MyProgressBar",
    isContainer: false,
    isResizable: function (a, b) {
        if (a == "Right") {
            return true;
        } else {
            return false;
        }
    },
    defaultWidth: 200,
    initConfig: function (b, a) {
        if (!a) {
            b.width = this.defaultWidth;
        }
    },
    xdConfigs: [{
        name: "animate",
        group: "Ext.ProgressBar",
        ctype: "boolean"
    },{
        name: "baseCls",
        group: "Ext.ProgressBar",
        ctype: "string"
    },{
        name: "text",
        group: "Ext.ProgressBar",
        ctype: "string"
    },{
        name: "textEl",
        group: "Ext.ProgressBar",
        ctype: "string"
    },{
        name: "value",
        group: "Ext.ProgressBar",
        ctype: "number"
    },{
        name: "id",
        group: "Ext.ProgressBar",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.ProgressBar);
xds.ProgressBar = Ext.extend(Ext.ProgressBar, {});
Ext.reg("xdprogressbar", xds.ProgressBar);


xds.types.Toolbar = Ext.extend(xds.types.Container, {
    cid: "toolbar",
    category: "Toolbar",
    defaultName: "&lt;toolbar&gt;",
    text: "Toolbar",
    dtype: "xdtoolbar",
    xtype: "toolbar",
    xcls: "Ext.Toolbar",
    iconCls: "icon-toolbar",
    naming: "MyToolbar",
    //    enableFlyout: true,
    isContainer: true,
    dock: "tbar",
    defaultConfig: {},
    initConfig: function (b, a) {
        if (!a) {
            b.width = 400;
        }
    },
    transformGroup: "toolbar",
    xdConfigs: [{
        name: "layout",
        group: "Ext.Toolbar",
        ctype: "string",
        defaultValue: "toolbar",
        readonly: true
    },{
        name: "enableOverflow",
        group: "Ext.Toolbar",
        ctype: "boolean"
    },{
        name: "buttonAlign",
        group: "Ext.Toolbar",
        ctype: "string",
        editor: "options",
        options: ["left", "center", "right"]
    }],
    getEditorConfigs: function () {
        if (!this.owner) {
            return xds.types.Toolbar.superclass.getEditorConfigs.call(this);
        }
        return xds.dockConfigs;
    },
    isValidParent: function (a) {
        if (a) {
            var c = a.getExtComponent();
            if (c) {
                if (!c.isXType('panel')) {
                    return false;
                }
            }
        }
        return true;
    },
    isResizable:function(p,g){
    	if(p == "Right"){
    		return true;
    	}
    }
});
//xds.Registry.register(xds.types.Toolbar);
xds.Toolbar = Ext.extend(Ext.Toolbar, {
    afterRender: function () {
        if (!this.items || this.items.length < 1) {
            this.height = 27;
        }
        xds.Toolbar.superclass.afterRender.call(this);
    }
});
Ext.reg("xdtoolbar", xds.Toolbar);

xds.types.ToolbarSeparator = Ext.extend(xds.types.BoxComponent, {
    cid: "tbseparator",
    category: "Toolbar",
    defaultName: "&lt;separator&gt;",
    text: "Separator",
    dtype: "xdtbseparator",
    xtype: "tbseparator",
    xcls: "Ext.Toolbar.Separator",
    iconCls: "icon-separator",
    naming: "MySeparator",
    filmCls: "el-film-nolabel",
    isContainer: false,
    transformGroup: "tbitemspace"
});
//xds.Registry.register(xds.types.ToolbarSeparator);
xds.ToolbarSeparator = Ext.extend(Ext.Toolbar.Separator, {});
Ext.reg("xdtbseparator", xds.ToolbarSeparator);

xds.types.ToolbarSpacer = Ext.extend(xds.types.BoxComponent, {
    cid: "tbspacer",
    category: "Toolbar",
    defaultName: "&lt;spacer&gt;",
    text: "Spacer",
    dtype: "xdtbspacer",
    xtype: "tbspacer",
    xcls: "Ext.Toolbar.Spacer",
    iconCls: "icon-spacer",
    naming: "MySpacer",
    filmCls: "el-film-nolabel",
    isContainer: false,
    transformGroup: "tbitemspace",
    xdConfigs: [{
        name: "width",
        group: "Ext.Toolbar.Spacer",
        ctype: "number"
    }]
});
//xds.Registry.register(xds.types.ToolbarSpacer);
xds.ToolbarSpacer = Ext.extend(Ext.Toolbar.Spacer, {});
Ext.reg("xdtbspacer", xds.ToolbarSpacer);


xds.types.ToolbarText = Ext.extend(xds.types.BoxComponent, {
    cid: "tbtext",
    category: "Toolbar",
    defaultName: "&lt;text&gt;",
    text: "Text Item",
    dtype: "xdtbtext",
    xtype: "tbtext",
    xcls: "Ext.Toolbar.TextItem",
    iconCls: "icon-label",
    naming: "MyText",
    filmCls: "el-film-nolabel",
    isContainer: false,
    transformGroup: "tbitem",
    xdConfigs: [{
        name: "text",
        group: "Ext.Toolbar.TextItem",
        ctype: "string"
    }],
    onFilmDblClick: function (a) {
        xds.canvas.startEdit(this, this.getExtComponent().el, this.getConfigObject("text"), 80);
    }
});
//xds.Registry.register(xds.types.ToolbarText);
xds.ToolbarText = Ext.extend(Ext.Toolbar.TextItem, {});
Ext.reg("xdtbtext", xds.ToolbarText);


xds.types.ToolbarFill = Ext.extend(xds.types.ToolbarSpacer, {
    cid: "tbfill",
    category: "Toolbar",
    defaultName: "&lt;fill&gt;",
    text: "Fill",
    dtype: "xdtbfill",
    xtype: "tbfill",
    xcls: "Ext.Toolbar.Fill",
    iconCls: "icon-fill",
    naming: "MyFill",
    filmCls: "el-film-nolabel",
    isContainer: false,
    transformGroup: "tbitemspace"
});
//xds.Registry.register(xds.types.ToolbarFill);
xds.ToolbarFill = Ext.extend(Ext.Toolbar.Fill, {});
Ext.reg("xdtbfill", xds.ToolbarFill);

xds.types.PagingToolbar = Ext.extend(xds.types.Toolbar, {
    cid: "pagingtoolbar",
    defaultName: "&lt;pagingtoolbar&gt;",
    text: "Paging Toolbar",
    dtype: "xdpagingtoolbar",
    xtype: "paging",
    xcls: "Ext.PagingToolbar",
    naming: "MyPagingToolbar",
    isContainer: true,
    dock: "bbar",
    initConfig: function (c, a) {
        if (!a) {
            c.width = 400;
        } else {
            if (a.getStoreNode) {
                var b = a.getStoreNode();
                if (b) {
                    c.store = b.component.id;
                }
            }
        }
    },
    xdConfigs: [{
        name: "store",
        group: "Ext.PagingToolbar",
        ctype: "string",
        updateFn: function (extCmp, value, b) {}
    },{
        name: "afterPageText",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "beforePageText",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "displayInfo",
        group: "Ext.PagingToolbar",
        ctype: "boolean"
    },{
        name: "displayMsg",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "emptyMsg",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "firstText ",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "lastText",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "nextText",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "pageSize",
        group: "Ext.PagingToolbar",
        ctype: "number"
    },{
        name: "prependButtons",
        group: "Ext.PagingToolbar",
        ctype: "boolean"
    },{
        name: "prevText",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "refreshText ",
        group: "Ext.PagingToolbar",
        ctype: "string"
    },{
        name: "showPageSizeCombo",
        group: "Ext.PagingToolbar",
        ctype: "boolean"
    }]
});
//xds.Registry.register(xds.types.PagingToolbar);
xds.PagingToolbar = Ext.extend(Ext.PagingToolbar, {
    constructor: function (a) {
        a.store = new Ext.data.ArrayStore();
        xds.PagingToolbar.superclass.constructor.call(this, a);
    }
});
Ext.reg("xdpagingtoolbar", xds.PagingToolbar);


xds.types.ButtonGroup = Ext.extend(xds.types.Panel, {
    cid: "buttongroup",
    category: "Toolbar",
    defaultName: "&lt;buttongroup&gt;",
    text: "ButtonGroup",
    dtype: "xdbuttongroup",
    xtype: "buttongroup",
    xcls: "Ext.ButtonGroup",
    iconCls: "icon-buttongroup",
    naming: "MyButtonGroup",
    isContainer: true,
    isLayoutable: false,
    defaultLayout: "table",
    defaultConfig: {
        title: "Buttons"
    },
    xdConfigs: [{
        name: "columns",
        group: "Ext.ButtonGroup",
        ctype: "number"
    },{
        name: "frame",
        group: "Ext.ButtonGroup",
        ctype: "boolean"
    }],
    getDefaultInternals: function () {
        return xds.types.ButtonGroup.superclass.getDefaultInternals.call(this, {
            cn: [{
                cid: "button"
            }]
        });
    },
    isValidParent: function (a) {
        return a && (a.cid == "toolbar" || a.cid == "pagingtoolbar");
    },
    getPanelHeader: function () {
        var a = this.getExtComponent();
        if (a.header && a.headerAsText) {
            return a.header.child("span");
        }
        return null;
    },
    onFilmDblClick: function (a) {
        var b = this.getPanelHeader();
        if (b && b.getRegion().contains(a.getPoint())) {
            this.startTitleEdit(b);
        } else {
            xds.PanelBase.superclass.onFilmDblClick.call(this, a);
        }
    },
    startTitleEdit: function (a) {
        xds.canvas.startEdit(this, a || this.getPanelHeader(), this.getConfigObject("title"), 150);
    }
});
//xds.Registry.register(xds.types.ButtonGroup);
xds.ButtonGroup = Ext.extend(Ext.ButtonGroup, {});
Ext.reg("xdbuttongroup", xds.ButtonGroup);


xds.types.Menu = Ext.extend(xds.types.Container, {
    cid: "menu",
    category: "Toolbar",
    defaultName: '&lt;menu&gt;',
    text: 'Menu',
    naming: 'Menu',
    dtype: "xdmenu",
    xtype: 'menu',
    xcls: 'Ext.menu.Menu',
    iconCls: 'icon-menu',
    isContainer: true,
    isMenu: true,
    filmCls: 'el-film-nolabel',
    isValidParent: function (target) {
        return target && (target.cid == "button");
    },
    getDefaultInternals: function () {
        return xds.types.Menu.superclass.getDefaultInternals.call(this, {
            cn: [{
                cid: "menuitem"
            }]
        });
    },
    xdConfigs: [{
        name: 'allowOtherMenus',
        group: 'Ext.Menu',
        ctype: 'boolean'
    },{
        name: 'defaultAlign',
        group: 'Ext.Menu',
        ctype: 'string'
    },{
        name: 'defaultOffsets',
        group: 'Ext.Menu',
        ctype: 'string'
    },{
        name: "enableScrolling",
        group: "Ext.Menu",
        ctype: "boolean"
    },{
        name: 'floating',
        group: 'Ext.Menu',
        ctype: 'boolean'
    },{
        name: 'ignoreParentClicks ',
        group: 'Ext.Menu',
        ctype: 'string'
    },{
        name: 'maxHeight',
        group: 'Ext.Menu',
        ctype: 'number'
    },{
        name: 'minWidth',
        group: 'Ext.Menu',
        ctype: 'number'
    },{
        name: 'plain',
        group: 'Ext.Menu',
        ctype: 'boolean'
    },{
        name: 'scrollIncrement',
        group: 'Ext.Menu',
        ctype: 'number'
    },{
        name: 'shadow',
        group: 'Ext.Menu',
        ctype: 'boolean'
    },{
        name: 'showSeparator',
        group: 'Ext.Menu',
        ctype: 'boolean'
    },{
        name: 'subMenuAlign',
        group: 'Ext.Menu',
        ctype: 'string'
    },{
        name: 'zIndex',
        group: 'Ext.Menu',
        ctype: 'number'
    },]
});
//xds.Registry.register(xds.types.Menu);
xds.Menu = Ext.extend(Ext.menu.Menu, {});
Ext.reg("xdmenu", xds.Menu);

xds.types.MenuItem = Ext.extend(xds.types.Component, {
    cid: "menuitem",
    category: "Toolbar",
    defaultName: '&lt;menuitem&gt;',
    text: 'Menu Item',
    dtype: "xdmenuitem",
    xtype: 'menuitem',
    xcls: 'Ext.menu.Item',
    iconCls: 'icon-menuitem',
    naming: 'MenuItem',
    isContainer: true,
    isMenuItem: true,
    filmCls: 'el-film-nolabel',
    defaultConfig: {
        text: 'Menu Item'
    },
    isValidParent: function (target) {
        return target && (target.cid == "menu" || target.cid == "menuitem");
    },
    getReferenceForConfig: function (c, b) {
        var a = xds.types.MenuItem.superclass.getReferenceForConfig.call(this, c, b);
        if (c.isMenuItem) {
            a.ref = "menu";
            a.type = "string";
        }
        return a;
    },
    xdConfigs: [{
        name: 'activeClass',
        group: 'Ext.menu.Item',
        ctype: 'string'
    },{
        name: 'altText',
        group: 'Ext.menu.Item',
        ctype: 'string'
    },{
        name: 'canActivate',
        group: 'Ext.menu.Item',
        ctype: 'boolean'
    },{
        name: 'clickHideDelay',
        group: 'Ext.menu.Item',
        ctype: 'number'
    },{
        name: 'hideOnClick',
        group: 'Ext.menu.Item',
        ctype: 'boolean'
    },{
        name: 'hideOnClick',
        group: 'Ext.menu.BaseItem',
        ctype: 'boolean'
    },{
        name: 'href',
        group: 'Ext.menu.MenuItem',
        ctype: 'string'
    },{
        name: 'hrefTarget',
        group: 'Ext.menu.MenuItem',
        ctype: 'string'
    },{
        name: 'icon',
        group: 'Ext.menu.MenuItem',
        ctype: 'string'
    },{
        name: 'iconCls',
        group: 'Ext.menu.MenuItem',
        ctype: 'string',
        editor: 'icon'
    },{
        name: 'itemCls',
        group: 'Ext.menu.MenuItem',
        ctype: 'string'
    },{
        name: 'showDelay',
        group: 'Ext.menu.MenuItem',
        ctype: 'number'
    },{
        name: "text",
        group: "Ext.menu.MenuItem",
        ctype: "string"
    },{
        name: 'click',
        group: '(Event)',
        ctype: 'fn',
        params: ['menuItem', 'event']
    }]
});
//xds.Registry.register(xds.types.MenuItem);
xds.MenuItem = Ext.extend(Ext.menu.Item, {});
Ext.reg("xdmenuitem", xds.MenuItem);


xds.types.GridPanel = Ext.extend(xds.types.Panel, {
    cid: "grid",
    category: "Grid",
    defaultName: "&lt;grid&gt;",
    text: "Grid Panel",
    dtype: "xdgrid",
    xtype: "grid",
    xcls: "od.GridPanel",
    iconCls: "icon-grid",
    naming: "MyGrid",
    isContainer: true,
    bindable: true,
    autoScrollable: false,
    layoutable: false,
    validChildTypes: ["gridcolumn", "rownumbercolumn","booleancolumn", "checkcolumn", "numbercolumn", "datecolumn", "templatecolumn", "toolbar", "pagingtoolbar", "bizcodecolumn"],
    defaultConfig: {
        store: "(none)"
    },
    transformGroup: "grid",
    initConfig: function (b, a) {
        if (!a) {
            b.width = 400;
            b.height = 250;
            b.title = 'My Grid';
        }
    },
    isResizable: function (a, b) {
        return !this.isFit() && !this.isAnchored();
    },
    xdConfigs: [{
        name: "autoExpandColumn",
        group: "Ext.grid.GridPanel",
        ctype: "string"
    },{
        name: "autoExpandMax",
        group: "Ext.grid.GridPanel",
        ctype: "number"
    },{
        name: "autoExpandMin",
        group: "Ext.grid.GridPanel",
        ctype: "number"
    },{
        name: "columnLines",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "ddGroup",
        group: "Ext.grid.GridPanel",
        ctype: "string"
    },{
        name: "ddText",
        group: "Ext.grid.GridPanel",
        ctype: "string"
    },{
        name: "deferRowRender",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "disableSelection",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "enableColumnHide",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "enableColumnMove",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "enableColumnResize",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "enableDragDrop",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "enableHdMenu",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "hideHeaders",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "loadMask",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "maxHeight",
        group: "Ext.grid.GridPanel",
        ctype: "number"
    },{
        name: "minColumnWidth",
        group: "Ext.grid.GridPanel",
        ctype: "number"
    },{
        name: "stripeRows",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "trackMouseOver",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "autoFill",
        group: "Ext.grid.GridPanel.GridView",
        ctype: "boolean"
    },{
        name: "forceFit",
        group: "Ext.grid.GridPanel.GridView",
        ctype: "boolean"
    },{
        name: "markDirty",
        group: "Ext.grid.GridPanel.GridView",
        ctype: "boolean"
    },{
        name: "enableRowBody",
        group: "Ext.grid.GridPanel.GridView",
        ctype: "boolean"
    },{
    	name: "useRowEditor",
    	group: "Ext.grid.GridPanel",
    	ctype: "boolean"
    },{
        name: "selectedRowClass",
        group: "Ext.grid.GridPanel.GridView",
        ctype: "string"
    },{
        name: "rowOverCls",
        group: "Ext.grid.GridPanel.GridView",
        ctype: "string"
    },{
        name: "useGroupView",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
    	name: "showSummary",
    	group: "Ext.grid.GridPanel.GroupView",
    	ctype: "boolean"
    },{
        name: "cancelEditOnToggle",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
        name: "emptyGroupText",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "string"
    },{
        name: "enableGrouping",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
        name: "enableGroupingMenu",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
        name: "groupByText",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "string"
    },{
        name: "enableNoGroups",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
        name: "showGroupName",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
        name: "startCollapsed",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
        name: "showGroupsText",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "string"
    },{
        name: "ignorAdd",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
        name: "hideGroupedColumn",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "boolean"
    },{
        name: "groupTextTpl",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "string"
    },{
        name: "groupMode",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "string",
        editor: "options",
        options: ["value", "display"]
    },{
        name: "selModel",
        group: "Ext.grid.GridPanel",
        ctype: "string"
    },{
        name: "getExtGroupText",
        group: "Ext.grid.GridPanel.GroupView",
        ctype: "fn",
        setFn: 'setConfig',
        getFn: 'getConfigValue',
        params: ['gvalue', 'cols', 'store', 'colCount']
    },{
        name: "cellclick",
        group: "(Event)",
        ctype: "fn",
        params: ['grid', 'rowIndex', 'columnIndex', 'event']
    },{
        name: "rowcontextmenu",
        group: "(Event)",
        ctype: "fn",
        params: ['grid', 'rowIndex', 'event']
    },{
        name: "rowdblclick",
        group: "(Event)",
        ctype: "fn",
        params: ['grid', 'rowIndex', 'event']
    },{
        name: "rowclick",
        group: "(Event)",
        ctype: "fn",
        params: ['grid', 'rowIndex', 'event']
    },{
    	name:"beforerender",
    	group: "(Event)",
        ctype: "fn",
        params: ['grid']
    },{
        name: "rowNumberer",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    }],
    getTargetColumnIndex: function (evt) { //h:evt
        var grid = this.getExtComponent(); //a:grid
        if (grid) {
            var p = evt.getPoint();
            var l = grid.getView().mainHd.getRegion();
            if (!l.contains(p)) {
                return false;
            }
            var m = p.left;
            var b = l.left;
            var j = grid.colModel.config;
            var d = 0,
                f = false;
            for (var d = 0, g = j.length, k; k = j[d]; d++) {
                if (!k.hidden) {
                    b += k.width;
                    if (m <= b) {
                        f = d;
                        break;
                    }
                }
            }
            return f;
        }
        return false;
    },
    getTargetColumn: function (a) {
        if (typeof a == "object") {
            a = this.getTargetColumnIndex(a);
        }
        if (a === false) {
            return null;
        }
        var b = this.getColumnNodes();
        return b[a].component;
    },
    onFilmClick: function (b) {
        var a = this.getTargetColumn(b);
        if (a) {
        	var n=a.getNode();
        	//if(n.parentNode && !n.parentNode.isExpanded()){
        	//	n.parentNode.expand();
        	//}
        	if(n){
        		var path = n.getPath();
        		n.getOwnerTree().expandPath(path);
            	n.select();
            }
            return false;
        }
    },
    onFilmDblClick: function (d) {
        var a = this.getTargetColumnIndex(d);
        if (a !== false) {
            var c = this.getExtComponent().view.getHeaderCell(a);
            var b = this.getTargetColumn(a);
            xds.canvas.startEdit(b, c, b.getConfigObject("header"));
        } else {
            xds.types.GridPanel.superclass.onFilmDblClick.call(this, d);
        }
    },
    getDefaultInternals: function () {
        return xds.types.GridPanel.superclass.getDefaultInternals.call(this, {
            cid: this.cid,
            cn: [{
                cid: "gridcolumn"
            },{
                cid: "gridcolumn"
            },{
                cid: "gridcolumn"
            }]
        });
    },
    getActions: function () {
        if (!this.actions) {
            this.autoColumnAction = new Ext.Action({
                itemId: "auto-columns",
                text: "Auto columns",
                iconCls: "icon-auto-columns",
                handler: this.doAutoColumns,
                scope: this
            });
            this.actions = [this.autoColumnAction];
        }
        this.autoColumnAction.disable();
        if (!Ext.isEmpty(this.getStoreNode())) {
            if (this.getStoreNode().firstChild) {
                this.autoColumnAction.enable();
            }
        }
        return this.actions;
    },
    getColumnNodes: function () {
        var f = this.getNode(),
            c = f.childNodes,
            d = [];
        for (var b = 0, a = c.length, e; e = c[b]; b++) {
            if (!e.component.dock && !e.component.isStore) {
                d.push(e);
            }
        }
        return d;
    },
    doAutoColumns: function () {
        var k = this.getStoreNode(),
            b = this.getNode();
        if (!k) {
            Ext.Msg.alert("Warning", "Unable to read columns - no store has been defined.");
            return;
        }
        xds.canvas.beginUpdate();
        var h = this.getColumnNodes();
        for (var d = 0, f; f = h[d]; d++) {
            f.parentNode.removeChild(f);
        }
        var e = k.childNodes;
        for (var d = 0, f; f = e[d]; d++) {
            var j = f.component,
                g = j.getConfigValue("type"),
                a = j.getConfigValue("name"),
                n = j.getConfigValue("text");
            switch (g) {
            case "int":
            case "float":
                xds.inspector.restore({
                    cid: "numbercolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a
                    }
                }, b);
                break;
            case "date":
                xds.inspector.restore({
                    cid: "datecolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a
                    }
                }, b);
                break;
            case "boolean":
                xds.inspector.restore({
                    cid: "booleancolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a
                    }
                }, b);
                break;
            default:
                xds.inspector.restore({
                    cid: "gridcolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a
                    }
                }, b);
                break;
            }
        }
        xds.canvas.endUpdate();
        xds.fireEvent("componentchanged");
    },
    getReferenceForConfig: function (b, a) {
        var c = xds.types.GridPanel.superclass.getReferenceForConfig.call(this, b, a);
        if (b.isColumn) {
            c.type = "array";
            c.ref = "columns";
        }
        return c;
    }
});
//xds.Registry.register(xds.types.GridPanel);
xds.types.GridPanel.transform = function (c, e) {
    var g;
    var d = c.getNode();
    var f;
    if (c.canContainEditors) {
        for (var a = 0, b = d.childNodes.length; a < b; a++) {
            g = d.childNodes[a].component;
            if (g.getEditorNode && (f = g.getEditorNode())) {
                f.remove();
            }
        }
    }
};
xds.GridPanel = Ext.extend(od.GridPanel, {
    afterRender: function () {
        xds.GridPanel.superclass.afterRender.call(this);
        if (false && this.store && this.store.viewerNode) {
            this.createFloater(this.store.viewerNode.id, this.store.storeId, this.store.iconCls);
        }
    },
    createFbar: function (b) {
        var a = this.minButtonWidth;
        this.elements += ",footer";
        this.fbar = this.createToolbar(b, {
            buttonAlign: this.buttonAlign,
            toolbarCls: "x-panel-fbar",
            enableOverflow: false,
            defaults: function (d) {
                return {
                    minWidth: d.minWidth || a
                };
            }
        });
        if (this.fbar.items) {
            this.fbar.items.each(function (d) {
                d.minWidth = d.minWidth || this.minButtonWidth;
            }, this);
            this.buttons = this.fbar.items.items;
        }
    }
});
Ext.reg("xdgrid", xds.GridPanel);

xds.GridPanel.DefaultStore = new Ext.data.JsonStore({
    storeId: "(none)",
    fields: [{
        name: "string",
        type: "string"
    },{
        name: "number",
        type: "float"
    },{
        name: "date",
        type: "date"
    },{
        name: "bool",
        type: "boolean"
    }],
    data: [{
        string: "cell",
        number: 1000000,
        bool: true,
        date: new Date().clearTime()
    },{
        string: "cell",
        number: 1000000,
        bool: true,
        date: new Date().clearTime()
    },{
        string: "cell",
        number: 1000000,
        bool: true,
        date: new Date().clearTime()
    }]
});
//Ext.onReady(function () {
//    xds.Config.editors.columns = new Ext.grid.GridEditor(new xds.MoreField({
//        value: "(Collection)",
//        setRawValue: function (a) {
//            this.value = a;
//        },
//        onMoreClick: function (b) {
//            var a = new xds.ColumnWindow();
//            a.component = xds.active.component;
//            a.show(b.target);
//        }
//    }));
//});
xds.types.EditorGridPanel = Ext.extend(xds.types.GridPanel, {
    cid: "editorgrid",
    category: "Grid",
    defaultName: "&lt;editorgrid&gt;",
    text: "Editor Grid Panel",
    dtype: "xdeditorgrid",
    xtype: "editorgrid",
    xcls: "Ext.grid.EditorGridPanel",
    naming: "MyEditorGrid",
    canContainEditors: true,
    defaultConfig: {
        store: "(none)"
    },
    xdConfigs: [{
        name: "autoEncode",
        group: "Ext.grid.EditorGridPanel",
        ctype: "boolean"
    },{
        name: "clicksToEdit ",
        group: "Ext.grid.EditorGridPanel",
        ctype: "number"
    },{
        name: "forceValidation",
        group: "Ext.grid.GridPanel",
        ctype: "boolean"
    },{
        name: "afteredit",
        group: "(Event)",
        ctype: "fn",
        params: ['evt']
    }],
    isValidChild: function () {
        return xds.types.BaseType.prototype.isValidChild.apply(this, arguments);
    },
    doAutoColumns: function () {
        var k = this.getStoreNode(),
            b = this.getNode();
        if (!k) {
            Ext.Msg.alert("Warning", "Unable to read columns - no store has been defined.");
            return
        }
        xds.canvas.beginUpdate();
        var h = this.getColumnNodes();
        for (var d = 0, f; f = h[d]; d++) {
            f.parentNode.removeChild(f);
        }
        var e = k.childNodes;
        for (var d = 0, f; f = e[d]; d++) {
            var j = f.component,
                bizType = j.getConfigValue("bizType"),
                g = j.getConfigValue("type"),
                a = j.getConfigValue("name"),
                n = j.getConfigValue("text");
            switch (g) {
            case "int":
            case "float":
                xds.inspector.restore({
                    cid: "numbercolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a
                    },
                    cn: [{
                        cid: 'numberfield'
                    }]
                }, b);
                break;
            case "date":
                xds.inspector.restore({
                	cid: "datecolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a
                    },
                    cn: [{
                        cid: 'datefield'
                    }]
                }, b);
                break;
            case "boolean":
                xds.inspector.restore({
                    cid: "booleancolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a
                    },
                    cn: [{
                        cid: 'checkbox'
                    }]
                }, b);
                break;
            case "bizcode":
                xds.inspector.restore({
                    cid: "bizcodecolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a,
                        bizType: bizType || ''
                    },
                    cn: [{
                        cid: 'bizcombo',
                        userConfig: {
                            bizType: bizType || ''
                        }
                    }]
                }, b);
                break;
            default:
                xds.inspector.restore({
                    cid: "gridcolumn",
                    userConfig: {
                        header: n || a,
                        dataIndex: a,
                        id: a
                    },
                    cn: [{
                        cid: 'textfield'
                    }]
                }, b);
                break;
            }
        }
        xds.canvas.endUpdate();
        xds.fireEvent("componentchanged");
    }
});
//xds.Registry.register(xds.types.EditorGridPanel);
xds.EditorGridPanel = Ext.extend(Ext.grid.EditorGridPanel, {
	afterRender: function () {
        xds.EditorGridPanel.superclass.afterRender.call(this);
        var b = this.getColumnModel();
        var g = b.getColumnCount();
        var j, a, d, k, h, f;
        this.on("viewready", function () {
            while (g--) {
                h = this.store.getAt(0);
                k = b.getColumnAt(g);
                j = k.getEditor();
                if (j && h) {
                    d = h.get(k.dataIndex) || "";
                    a = Ext.get(this.view.getCell(0, g));
                    a.update("");
                    j.render(a);
                    j.setWidth(a.getWidth() - 1);
                    j.setValue(d);
                }
            }
        }, this);
    },
    destroy: function () {
        xds.EditorGridPanel.superclass.destroy.call(this);
    }
});
Ext.reg("xdeditorgrid", xds.EditorGridPanel);

xds.ColumnBase = Ext.extend(xds.types.BaseType, {
    category: "Grid",
    defaultName: "&lt;column&gt;",
    naming: "MyColumn",
    isVisual: false,
    isColumn: true,
    isContainer: true,
    transformGroup: "columns",
    defaultEditor: "textfield",
    validEditors: ["checkbox", "datefield", "combo", "numberfield", "radio", "textfield", "textarea", "timefield", "bizcombo"],
    setConfig: function (a, b) {
        xds.ColumnBase.superclass.setConfig.call(this, a, b);
        if (a == "dataIndex") {
            var c = this.getConfigValue("id");
        }
    },
    defaultConfig: {
        header: "Column",
        sortable: false,
        resizable: true,
        width: 100,
        menuDisabled: true
    },
    defaultDataIndex: "string",
    initConfig: function (b, a) {
        b.dataIndex = this.defaultDataIndex;
        if (a && a.canContainEditors) {
            b.editable = true;
            //patch editorGrid doAutoColumn
            if (Ext.isEmpty(b.cn)) {
                b.cn = [{
                    cid: this.defaultEditor
                }];
            }
        }
    },
    getDefaultInternals: function (b, a) {
        b = b || {};
        if (a && a.canContainEditors) {
            b.cn = [{
                cid: this.defaultEditor
            }];
        }
        return xds.ColumnBase.superclass.getDefaultInternals.call(this, b);
    },
    isValidChild: function (a) {
        return this.validEditors.contains(a) && !this.getEditorNode();
    },
    getEditorNode: function () {
        var a = this.getNode().firstChild;
        while (a) {
            if (a.component.isField) {
                return a;
            }
            a = a.nextSibling;
        }
        return null;
    },
    getReferenceForConfig: function (c, b) {
        var a = xds.ColumnBase.superclass.getReferenceForConfig.call(this, c, b);
        if (c.isField && this.owner.canContainEditors) {
            a.ref = "editor";
            a.type = "string";
        }
        return a;
    }
});

xds.RowNumberColumn = Ext.extend(Ext.grid.RowNumberer,{
	constructor:function(config){
		xds.RowNumberColumn.superclass.constructor.apply(this, arguments);
		this.initialConfig = config || {};
	},
	isColumn:true
});

Ext.grid.Column.types["rownumbercolumn"] = xds.RowNumberColumn;

Ext.reg("rownumbercolumn",xds.RowNumberColumn);

xds.types.RowNumberColumn = Ext.extend(xds.ColumnBase, {
    cid: "rownumbercolumn",
    text: "RowNumber Column",
    xtype: "rownumbercolumn",
    dtype: "rownumbercolumn",
    xcls: "Ext.grid.Column",
    iconCls: "icon-grid-column",
    defaultDataIndex:'',
    defaultConfig: {
        header: "",
        sortable: false,
        resizable: false,
        width: 35,
        fixed:true,
        hideable: false,
        menuDisabled:true,
        id: 'numberer',
        rowspan: undefined
    }
});

xds.types.GridColumn = Ext.extend(xds.ColumnBase, {
    cid: "gridcolumn",
    text: "Grid Column",
    xtype: "gridcolumn",
    dtype: "gridcolumn",
    xcls: "Ext.grid.Column",
    iconCls: "icon-grid-column",
    validChildTypes: ["checkbox", "datefield", "combo", "numberfield", "radio", "textfield", "textarea", "timefield", "bizcombo"],
    xdConfigs: [{
        name: "align",
        group: "Ext.grid.Column",
        ctype: "string",
        editor: "options",
        options: ["center", "left", "right"]
    },{
        name: "css",
        group: "Ext.grid.Column",
        ctype: "string"
    },{
        name: "dataIndex",
        group: "Ext.grid.Column",
        ctype: "string"
    },{
        name: "editable",
        group: "Ext.grid.Column",
        ctype: "boolean"
    },{
        name: "emptyGroupText",
        group: "Ext.grid.Column",
        ctype: "string"
    },{
        name: "fixed",
        group: "Ext.grid.Column",
        ctype: "boolean"
    },{
        name: "groupName",
        group: "Ext.grid.Column",
        ctype: "string"
    },{
        name: "groupable",
        group: "Ext.grid.Column",
        ctype: "boolean"
    },{
        name: "header",
        group: "Ext.grid.Column",
        ctype: "string"
    },{
        name: "hidden",
        group: "Ext.grid.Column",
        ctype: "boolean"
    },{
        name: "hideable",
        group: "Ext.grid.Column",
        ctype: "boolean"
    },{
        name: "id",
        group: "Ext.grid.Column",
        ctype: "string"
    },{
        name: "menuDisabled",
        group: "Ext.grid.Column",
        ctype: "boolean"
    },{
    	name: "permissionId",
        group: "Ext.grid.Column",
        ctype: "string"
    },{
        name: "renderer",
        group: "Ext.grid.Column",
        ctype: "fn",
        setFn: 'setConfig',
        getFn: 'getConfigValue',
        params: ['value', 'metaData', 'record', 'rowIndex', 'colIndex', 'store']
    },{
        name: "resizable",
        group: "Ext.grid.Column",
        ctype: "boolean"
    },{
        name: "sortable",
        group: "Ext.grid.Column",
        ctype: "boolean"
    },{
        name: "summaryType",
        group: "Ext.grid.Column",
        ctype: "string",
        editor:"options",
        options:['sum','count','max','min','average']
    },{
        name: "summaryRenderer",
        group: "Ext.grid.Column",
        ctype: "fn",
        setFn: 'setConfig',
        getFn: 'getConfigValue',
        params: ['value', 'params', 'data']
    },{
        name: "totalRenderer",
        group: "Ext.grid.Column",
        ctype: "fn",
        setFn: 'setConfig',
        getFn: 'getConfigValue',
        params: ['value', 'params', 'data']
    },{
        name: "tooltip",
        group: "Ext.grid.Column",
        ctype: "string"
    },{
        name: "width",
        group: "Ext.grid.Column",
        ctype: "number"
    }]
});
//xds.Registry.register(xds.types.GridColumn);

xds.types.BooleanColumn = Ext.extend(xds.types.GridColumn, {
    cid: "booleancolumn",
    defaultName: "&lt;booleanColumn&gt;",
    text: "Boolean Column",
    xtype: "booleancolumn",
    dtype: "booleancolumn",
    xcls: "Ext.grid.BooleanColumn",
    iconCls: "icon-grid-bool",
    defaultEditor: "checkbox",
    defaultConfig: {
        header: "True/False",
        sortable: true,
        resizable: true,
        width: 100,
        menuDisabled: true
    },
    defaultDataIndex: "bool",
    xdConfigs: [{
    	name: "falseText",
    	group: "Ext.grid.BooleanColumn",
    	ctype: "string"
    },{
    	name: "trueText",
    	group: "Ext.grid.BooleanColumn",
    	ctype: "string"
    },{
    	name: "undefinedText",
    	group: "Ext.grid.BooleanColumn",
    	ctype: "string"
    }]
});
//xds.Registry.register(xds.types.BooleanColumn);


xds.types.CheckboxColumn = Ext.extend(xds.types.GridColumn, {
    cid: "checkcolumn",
    defaultName: "&lt;checkboxColumn&gt;",
    text: "Checkbox Column",
    xtype: "checkcolumn",
    dtype: "checkcolumn",
    xcls: "od.CheckboxColumn",
    iconCls: "icon-grid-bool",
    defaultEditor: "checkbox",
    defaultConfig: {
        sortable: false,
        resizable: false,
        menuDisabled: true,
        hideable: false,
        dataIndex: '',
        width: 25,
        id: 'checker',
        assm: true
    },
    defaultDataIndex: "",
    xdConfigs: [{
        name: "checkOnly",
        group: "Ext.grid.CheckboxSelectionModel",
        ctype: "boolean"
    },{
        name: "moveEditorOnEnter",
        group: "Ext.grid.RowSelectionModel",
        ctype: "boolean"
    },{
        name: "singleSelect",
        group: "Ext.grid.RowSelectionModel",
        ctype: "boolean"
    },{
        name: "assm",
        group: "Ext.grid.CheckboxSelectionModel",
        ctype: "boolean"
    },{
    	 name: "rowselect",
         group: "(Event)",
         ctype: "fn",
         params:['selectionModel','rowIndex','record']
    },{
	   	 name: "rowdeselect",
	     group: "(Event)",
	     ctype: "fn",
	     params:['selectionModel','rowIndex','record']
    }],
    getNode: function () {
        var node = xds.types.CheckboxColumn.superclass.getNode.call(this);
        node.setText('CheckColumn');
        return node;
    }
});
//xds.Registry.register(xds.types.CheckboxColumn);

xds.CheckColumn = Ext.extend(Ext.grid.CheckboxSelectionModel, {
    fixed: true
});
Ext.reg('checkcolumn', xds.CheckColumn);

Ext.grid.Column.types.checkcolumn = xds.CheckColumn;

xds.types.NumberColumn = Ext.extend(xds.types.GridColumn, {
    cid: "numbercolumn",
    defaultName: "&lt;numberColumn&gt;",
    text: "Number Column",
    xtype: "numbercolumn",
    dtype: "numbercolumn",
    xcls: "Ext.grid.NumberColumn",
    iconCls: "icon-grid-num",
    defaultConfig: {
        header: "Value",
        sortable: true,
        resizable: true,
        width: 100,
        format: "0,000.00",
        menuDisabled: true
    },
    defaultDataIndex: "number",
    defaultEditor: "numberfield",
    xdConfigs: [{
        name: "format",
        group: "Ext.grid.NumberColumn",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.NumberColumn);

xds.types.DateColumn = Ext.extend(xds.types.GridColumn, {
    cid: "datecolumn",
    defaultName: "&lt;dateColumn&gt;",
    text: "Date Column",
    xtype: "datecolumn",
    dtype: "datecolumn",
    xcls: "Ext.grid.DateColumn",
    iconCls: "icon-grid-date",
    defaultConfig: {
        header: "Date",
        sortable: true,
        resizable: true,
        width: 100,
        format: "Y-m-d",
        menuDisabled: true
    },
    defaultDataIndex: "date",
    defaultEditor: "datefield",
    xdConfigs: [{
        name: "format",
        group: "Ext.grid.DateColumn",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.DateColumn);

xds.types.TemplateColumn = Ext.extend(xds.types.GridColumn, {
    cid: "templatecolumn",
    defaultName: "&lt;templateColumn&gt;",
    text: "Template Column",
    xtype: "templatecolumn",
    dtype: "templatecolumn",
    xcls: "Ext.grid.TemplateColumn",
    iconCls: "icon-grid-tpl",
    defaultConfig: {
        header: "Template",
        sortable: false,
        resizable: true,
        width: 100,
        tpl: "{string}",
        menuDisabled: true
    },
    xdConfigs: [{
        name: "tpl",
        group: "Ext.grid.TemplateColumn",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.TemplateColumn);
xds.ColumnBase.transform =
	xds.types.GridColumn.transform =
		xds.types.BooleanColumn.transform =
			xds.types.CheckboxColumn.transform =
				xds.types.DateColumn.transform =
					xds.types.NumberColumn.transform =
						xds.types.TemplateColumn.transform = function (c, d) {
						    var e = c.getEditorNode();
						    var a = c.owner;
						    var b;
						    if (a.canContainEditors && e && e.component.cid === c.defaultEditor && !xds.countKeys(e.component.userConfig)) {
						        e.remove();
						        b = xds.Registry.get(d.prototype.defaultEditor).prototype.getDefaultInternals(null, c);
						        xds.inspector.restore(b, c.getNode());
						    }
						};

xds.StoreBase = Ext.extend(xds.types.BaseType, {
    category: "Data",
    defaultName: "&lt;store&gt;",
    naming: "MyStore",
    isVisual: false,
    isContainer: true,
    isStore: true,
    validChildTypes: ["datafield"],
    initConfig: function (b, a) {
        b.storeId = this.id;
    },
    setConfig: function (a, b) {
        xds.StoreBase.superclass.setConfig.call(this, a, b);
        this.reconfigure();
        if (a == "url" && this.actions) {
            this.actions[0][b ? "enable" : "disable"]();
            this.actions[0].initialConfig.disabled = !b;
        }
    },
    reconfigure: function (d) {
        var b = xds.StoreCache.get(this.owner.id);
        var e = this.processConfig(b.viewerNode);
        e.cache = false;
        var a = this.createStore(e, false);
        b.reader = a.reader;
        b.proxy = a.proxy;
        if (b.proxy) {
            b.proxy.on("loadexception", this.onLoadException, this);
        }
        b.remoteSort = a.remoteSort;
        b.sortDir = a.sortDir;
        b.sortField = a.sortField;
        b.url = a.url;
        if (d !== false && b.dataCache) {
            b.loadData(b.dataCache);
        }
    },
    createCanvasConfig: function (d) {
        var a = xds.StoreCache.get(this.owner.id);
        if (!a) {
            var b = this.processConfig(d);
            b.viewerNode = d;
            b.component = this.owner;
            a = this.createStore(b, true);
        }
        return a;
    },
    onLoadException: function () {
        //xds.status.el.update("");
        Ext.Msg.alert("Error", "Unable to load data using the supplied configuration.");
        this.setSuffix("load error", "error");
    },
    processConfig: function (e) {
        var d = Ext.apply({}, this.getConfig());
        d.xtype = this.xtype;
        d.fields = [];
        d.autoLoad = false;
        d.iconCls = this.iconCls;
        if (e.hasChildNodes()) {
            for (var b = 0, a = e.childNodes.length; b < a; b++) {
                d.fields.push(e.childNodes[b].component.getConfig());
            }
        }
        return d;
    },
    getActions: function () {
        if (!this.actions) {
            var quickAdd = function (count) { //a
                //var b = [];
                for (var i = 0; i < count; i++) {
                    xds.inspector.restore({
                        cid: "datafield"
                    }, this.getNode());
                }
            };
            this.actions = [new Ext.Action({
                itemId: "store-load",
                text: "Load data",
                iconCls: "icon-load",
                handler: function () {
                    var store = xds.StoreCache.get(this.owner.id);
                    delete store.dataCache;
                    store.reload();
                },
                scope: this,
                disabled: !this.getConfigValue("url")
            }), new Ext.Action({
                itemId: "quick-add",
                text: "Quick add",
                hideOnClick: false,
                menu: {
                    zIndex: 80001,
                    items: [{
                        text: "1 field",
                        handler: quickAdd.createDelegate(this, [1])
                    },{
                        text: "2 fields",
                        handler: quickAdd.createDelegate(this, [2])
                    },{
                        text: "3 fields",
                        handler: quickAdd.createDelegate(this, [3])
                    },{
                        text: "4 fields",
                        handler: quickAdd.createDelegate(this, [4])
                    },{
                        text: "5 fields",
                        handler: quickAdd.createDelegate(this, [5])
                    }]
                }
            }), new Ext.Action({
                itemId: 'bind-entity',
                text: 'Bind-Entity',
                handler: function () {
                	var storeNode = this.getNode();
                    var sewin = new od.SEWindow();
                    sewin.on('selected',function(entityId,items){
    					while(storeNode.childNodes.length>0){
    						xds.inspector.removeComponent(storeNode.childNodes[0]);
    					}

    					Ext.each(items,function(item){
    						var fCfg={
    							cid:'datafield',
    							userConfig:{
    								name:item.data.code,
    								type:item.data.dataType,
    								text:item.data.name
    							}
    						};
    						xds.inspector.restore(fCfg,storeNode);
    					},this);
    					storeNode.component.setConfig('url','entity/'+entityId);
    					storeNode.component.setConfig('restful',true);
    					storeNode.component.setConfig('root','root');
    					storeNode.component.setConfig('idProperty','id');
    					storeNode.component.setConfig('autoLoad',true);

                    });
                    Ext.select('.el-film').hide();
                    sewin.on('close', function () {
                        Ext.select('.el-film').show();
                    });
                    sewin.show();
                },
                scope: this
            })];
        }
        return this.actions;
    },
    isValidParent: function (a) {
        if (!a) {
            return false;
        }
        if (a && a.getStoreNode) {
            return !a.getStoreNode();
        }
        return true;
    },
    xdConfigs:[{
	        name: "autoDestory",
	        group: "Ext.data.Stroe",
	        ctype: "boolean"
    	},{
           name: "autoLoad",
           group: "Ext.data.Store",
           ctype: "boolean"
        },{
            name: "autoSave",
            group: "Ext.data.Store",
            ctype: "boolean"
        },{
            name: "batch",
            group: "Ext.data.Store",
            ctype: "boolean"
        },{
            name: "pruneModifiedRecords",
            group: "Ext.data.Store",
            ctype: "boolean"
        },{
            name: "remoteSort",
            group: "Ext.data.Store",
            ctype: "boolean"
        },{
            name: "restful",
            group: "Ext.data.Store",
            ctype: "boolean"
        },{
            name: "storeId",
            group: "Ext.data.Store",
            ctype: "string"
        },{
            name: "url",
            group: "Ext.data.Store",
            ctype: "string"
        }]
});

xds.types.JsonStore = Ext.extend(xds.StoreBase, {
    cid: "jsonstore",
    text: "Json Store",
    xtype: "jsonstore",
    dtype: "jsonstore",
    xcls: "Ext.data.JsonStore",
    iconCls: "icon-json",
    transformGroup: 'store',
    createStore: function (a, c) {
        a = a || {};
        a.proxy = a.proxy || new Ext.data.HttpProxy(a);
        var b = new Ext.data.JsonStore(a);
        if (c) {
            b.on("beforeload", function () {
                if (!b.proxy.conn.url) {
                    Ext.Msg.alert("Warning", 'Could not load JsonStore, "url" has not been set.');
                    return false;
                }
                if (b.dataCache) {
                    b.loadData(b.dataCache);
                    return false;
                } else {
                    //xds.status.el.update("Loading store...")
                }
            });
            b.on("load", function (d) {
                d.dataCache = d.reader.jsonData;
                //xds.status.el.update("");
                this.setSuffix((d.data.length) + " records loaded", "loaded");
            }, this);
            b.proxy.on("loadexception", this.onLoadException, this);
        }
        return b;
    },
    xdConfigs: [{
        name: "idProperty",
        group: "Ext.data.JsonStore",
        ctype: "string"
    },{
        name: "root",
        group: "Ext.data.JsonStore",
        ctype: "string"
    },{
        name: "totalProperty",
        group: "Ext.data.JsonStore",
        ctype: "string"
    },{
        name: "sortDir",
        group: "Ext.data.Store",
        ctype: "string",
        editor: "options",
        options: ["ASC", "DESC"]
    },{
        name: "sortField",
        group: "Ext.data.Store",
        ctype: "string"
    },{
        name: "requestMethod",
        group: "Ext.data.Store",
        ctype: "string",
        editor: 'options',
        options: ['POST', 'GET', 'DELETE', 'HEAD', 'PUT']
    },{
        name: 'beforeload',
        group: "(Event)",
        ctype: 'fn',
        params: ['store', 'options']
    },{
        name: 'load',
        group: "(Event)",
        ctype: 'fn',
        params: ['store', 'records', 'options']
    }]
});
//xds.Registry.register(xds.types.JsonStore);
xds.types.JsonGroupStore = Ext.extend(xds.StoreBase, {
    cid: "jsongroupstore",
    text: "Json Group Store",
    xtype: "jsongroupstore",
    dtype: "jsongroupstore",
    xcls: "od.JsonGroupStore",
    iconCls: "icon-json",
    transformGroup: 'store',
    createStore: function (a, c) {
        a = a || {};
        a.proxy = a.proxy || new Ext.data.HttpProxy(a);
        var b = new od.JsonGroupStore(a);
        if (c) {
            b.on("beforeload", function () {
                if (!b.proxy.conn.url) {
                    Ext.Msg.alert("Warning", 'Could not load JsonStore, "url" has not been set.');
                    return false;
                }
                if (b.dataCache) {
                    b.loadData(b.dataCache);
                    return false;
                } else {
                    //xds.status.el.update("Loading store...")
                }
            });
            b.on("load", function (d) {
                d.dataCache = d.reader.jsonData;
                //xds.status.el.update("");
                this.setSuffix((d.data.length) + " records loaded", "loaded");
            }, this);
            b.proxy.on("loadexception", this.onLoadException, this);
        }
        return b;
    },
    xdConfigs: [{
        name: "idProperty",
        group: "Ext.data.JsonStore",
        ctype: "string"
    },{
        name: "root",
        group: "Ext.data.JsonStore",
        ctype: "string"
    },{
        name: "totalProperty",
        group: "Ext.data.JsonStore",
        ctype: "string"
    },{
        name: "remoteGroup",
        group: "Ext.data.GroupingStore",
        ctype: "boolean"
    },{
        name: "groupOnSort",
        group: "Ext.data.GroupingStore",
        ctype: "boolean"
    },{
        name: "groupField",
        group: "Ext.data.GroupingStore",
        ctype: "string"
    },{
        name: "groupDir",
        group: "Ext.data.GroupingStore",
        ctype: "string",
        editor: "options",
        options: ["ASC", "DESC"]
    },{
        name: "sortDir",
        group: "Ext.data.Store",
        ctype: "string",
        editor: "options",
        options: ["ASC", "DESC"]
    },{
        name: "sortField",
        group: "Ext.data.Store",
        ctype: "string"
    },{
        name: "requestMethod",
        group: "Ext.data.Store",
        ctype: "string",
        editor: 'options',
        options: ['POST', 'GET', 'DELETE', 'HEAD', 'PUT']
    },{
        name: 'beforeload',
        group: "(Event)",
        ctype: 'fn',
        params: ['store', 'options']
    },{
        name: 'load',
        group: "(Event)",
        ctype: 'fn',
        params: ['store', 'records', 'options']
    },{
        name: 'groupchange',
        group: "(Event)",
        ctype: 'fn',
        params: ['store', 'groupField']
    }]
});
//xds.Registry.register(xds.types.JsonGroupStore);
xds.types.ArrayStore = Ext.extend(xds.StoreBase, {
    cid: "arraystore",
    text: "Array Store",
    xtype: "arraystore",
    dtype: "arraystore",
    xcls: "Ext.data.Store",
    iconCls: "icon-array",
    transformGroup: 'store',
    createStore: function (a, c) {
        a.reader = new Ext.data.ArrayReader({
            idIndex: a.idIndex,
            root: a.root,
            totalProperty: a.totalProperty
        }, a.fields);
        var b = new Ext.data.Store(a);
        if (c) {
            b.on("beforeload", function () {
                if (!b.proxy.conn.url) {
                    Ext.Msg.alert("Warning", 'Could not load Array Store, "url" has not been set.');
                    return false;
                }
                if (b.dataCache) {
                    b.loadData(b.dataCache);
                    return false;
                } else {
                    //xds.status.el.update("Loading store...")
                }
            });
            b.on("load", function () {
                b.dataCache = b.reader.arrayData;
                //xds.status.el.update("");
                this.setSuffix((b.dataCache ? b.dataCache.length : 0) + " records loaded", "loaded");
            }, this);
            if (b.proxy) {
                b.proxy.on("loadexception", this.onLoadException, this);
            }
        }
        return b;
    },
    xdConfigs: [{
        name: "idIndex",
        group: "Ext.data.ArrayStore",
        ctype: "number"
    },{
        name: "root",
        group: "Ext.data.ArrayStore",
        ctype: "string"
    },{
        name: "totalProperty",
        group: "Ext.data.ArrayStore",
        ctype: "string"
    },{
        name: "sortDir",
        group: "Ext.data.Store",
        ctype: "string",
        editor: "options",
        options: ["ASC", "DESC"]
    },{
        name: "sortField",
        group: "Ext.data.Store",
        ctype: "string"
    },{
        name: 'beforeload',
        group: "(Event)",
        ctype: 'fn',
        params: ['store', 'options']
    },{
        name: 'load',
        group: "(Event)",
        ctype: 'fn',
        params: ['store', 'records', 'options']
    }]
});
//xds.Registry.register(xds.types.ArrayStore);
xds.types.XmlStore = Ext.extend(xds.StoreBase, {
    cid: "xmlstore",
    text: "Xml Store",
    xtype: "xmlstore",
    dtype: "xmlstore",
    xcls: "Ext.data.XmlStore",
    iconCls: "icon-xml",
    transformGroup: 'store',
    createStore: function (a, c) {
        a = a || {};
        a.proxy = a.proxy || new Ext.data.HttpProxy(a);
        var b = new Ext.data.XmlStore(a);
        if (c) {
            b.on("beforeload", function () {
                if (!b.proxy.conn.url) {
                    Ext.Msg.alert("Warning", 'Could not load XmlStore, "url" has not been set.');
                    return false;
                }
                if (!b.reader.meta.record) {
                    Ext.Msg.alert("Warning", 'Could not load XmlStore, "record" has not been set.');
                    return false;
                }
                if (b.dataCache) {
                    b.loadData(b.dataCache);
                    return false;
                } else {
                    //xds.status.el.update("Loading store...")
                }
            });
            b.on("load", function () {
                b.dataCache = b.reader.xmlData;
                //xds.status.el.update("");
                this.setSuffix((b.data.length) + " records loaded", "loaded");
            }, this);
            b.proxy.on("loadexception", this.onLoadException, this);
        }
        return b;
    },
    xdConfigs: [{
        name: "idPath",
        group: "Ext.data.XmlStore",
        ctype: "string"
    },{
        name: "record",
        group: "Ext.data.XmlStore",
        ctype: "string"
    },{
        name: "totalRecords",
        group: "Ext.data.XmlStore",
        ctype: "string"
    },{
        name: "sortDir",
        group: "Ext.data.Store",
        ctype: "string",
        editor: "options",
        options: ["ASC", "DESC"]
    },{
        name: "sortField",
        group: "Ext.data.Store",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.XmlStore);

xds.types.Form = Ext.extend(xds.types.Panel, {
    cid: "form",
    category: "Form",
    defaultName: "&lt;cform&gt;",
    text: "Form Panel",
    dtype: "xdform",
    xtype: "form",
    xcls: "Ext.form.FormPanel",
    iconCls: "icon-form",
    naming: "MyForm",
    defaultConfig: {
        labelWidth: 70,
        labelAlign: "left",
        layout: "form"
    },
    transformGroup: "container",
    initConfig: function (b, a) {
        if (!a) {
            b.width = 400;
            b.height = 250;
            b.title = "My Form";
        } else {
            b.border = false;
        }
    },
    xdConfigs: [{
        name: "formId",
        group: "Ext.form.FormPanel",
        ctype: "string"
    },{
        name: "hideLabels",
        group: "Ext.form.FormPanel",
        ctype: "boolean"
    },{
        name: "itemCls",
        group: "Ext.form.FormPanel",
        ctype: "string"
    },{
        name: "labelAlign",
        group: "Ext.form.FormPanel",
        ctype: "string",
        editor: "options",
        options: ["left", "right", "top"]
    },{
        name: "labelSeparator",
        group: "Ext.form.FormPanel",
        ctype: "string"
    },{
        name: "labelWidth",
        group: "Ext.form.FormPanel",
        ctype: "number"
    },{
        name: "minButtonWidth",
        group: "Ext.form.FormPanel",
        ctype: "number"
    },{
        name: "monitorPoll",
        group: "Ext.form.FormPanel",
        ctype: "number"
    },{
        name: "monitorValid",
        group: "Ext.form.FormPanel",
        ctype: "boolean"
    },{
    	 name: "expand",
         group: "(Event)",
         ctype: "fn",
         params: ['formPanel']
    }],
    isValidChild: function (a) {
        var b = xds.types.Form.superclass.isValidChild.call(this, a);
        if (a === "form") {
            b = false;
        }
        return b;
    },
    getActions:function(){
        if (!this.actions) {
            var bindEntityAction = new Ext.Action({
                itemId: "bindEntity",
                text: "Bind-Entity",
                handler: function(){
                	var storeNode = this.getNode();
                    var sewin = new od.SEWindow();
                    sewin.on('selected',function(entityId,items){
    					while(storeNode.childNodes.length>0){
    						xds.inspector.removeComponent(storeNode.childNodes[0]);
    					}

    					Ext.each(items,function(item){
    						var fCfg={
    							cid:'textfield',
    							userConfig:{
    								name:item.data.code,
    								type:item.data.dataType,
    								fieldLabel:item.data.name,
    								anchor:'-20'
    							}
    						};
    						xds.inspector.restore(fCfg,storeNode);
    					},this);
    					storeNode.component.setConfig('padding',6);
    					storeNode.component.setConfig('autoHeight',true);
    					xds.fireEvent("componentchanged");
                    });
                    Ext.select('.el-film').hide();
                    sewin.on('close', function () {
                        Ext.select('.el-film').show();
                    });
                    sewin.show();
                },
                scope: this
            });
            this.actions = [bindEntityAction];
        }
        return this.actions;
    }
});
//xds.Registry.register(xds.types.Form);
xds.FormPanel = Ext.extend(Ext.form.FormPanel, {
    createFbar: function (b) {
        var a = this.minButtonWidth;
        this.elements += ",footer";
        this.fbar = this.createToolbar(b, {
            buttonAlign: this.buttonAlign,
            toolbarCls: "x-panel-fbar",
            enableOverflow: false,
            defaults: function (d) {
                return {
                    minWidth: d.minWidth || a
                };
            }
        });
        if (this.fbar.items) {
            this.fbar.items.each(function (d) {
                d.minWidth = d.minWidth || this.minButtonWidth;
            }, this);
            this.buttons = this.fbar.items.items;
        }
    }
});
Ext.reg("xdform", xds.FormPanel);


xds.types.FieldSet = Ext.extend(xds.types.Panel, {
    cid: "fieldset",
    category: "Form",
    defaultName: "&lt;fieldset&gt;",
    text: "FieldSet",
    dtype: "xdfieldset",
    xtype: "fieldset",
    xcls: "Ext.form.FieldSet",
    iconCls: "icon-fieldset",
    naming: "MyFieldSet",
    isContainer: true,
    transformGroup: "container",
    defaultConfig: {
        layout: "form",
        title: "My Fields"
    },
    initConfig: function (b, a) {
        if (!a) {
            b.width = 400;
        }
    },
    onFilmDblClick: function (b) {
        var a = this.getExtComponent();
        if (a.header && a.header.getRegion().contains(b.getPoint())) {
            xds.canvas.startEdit(this, a.header, this.getConfigObject("title"));
        } else {
            xds.types.FieldSet.superclass.onFilmDblClick.call(this, b);
        }
    },
    xdConfigs: [    {
        name: "animCollapse",
        group: "Ext.form.FieldSet",
        ctype: "boolean"
    },{
        name: "baseCls",
        group: "Ext.form.FieldSet",
        ctype: "string"
    },{
        name: "checkboxName",
        group: "Ext.form.FieldSet",
        ctype: "string"
    },{
        name: "checkboxToggle",
        group: "Ext.form.FieldSet",
        ctype: "boolean"
    },{
        name: "collapsible",
        group: "Ext.form.FieldSet",
        ctype: "boolean"
    },{
        name: "itemCls",
        group: "Ext.form.FieldSet",
        ctype: "string"
    },{
        name: "labelWidth",
        group: "Ext.form.FieldSet",
        ctype: "number"
    },{
        name: "collapse",
        group: "(Event)",
        ctype: "fn",
        params: ['panel']
    },{
        name: "expand",
        group: "(Event)",
        ctype: "fn",
        params: ['panel']
    }]
});
//xds.Registry.register(xds.types.FieldSet);
xds.FieldSet = Ext.extend(Ext.form.FieldSet, {});
Ext.reg("xdfieldset", xds.FieldSet);

xds.types.FieldBase = Ext.extend(xds.types.BoxComponent, {
    isContainer: false,
    isField: true,
    category: "Form",
    naming: "MyField",
    defaultConfig: {
        fieldLabel: "Label"
    },
    defaultColumn: "gridcolumn",
    isResizable: function (a, b) {
        return a == "Right" && !this.isAnchored() && !this.isFit() && (!this.owner || this.owner.getConfigValue("layout") != "form");
    },
    initConfig: function (d, b) {
        var c = this.owner ? this.owner.getConfigValue("layout") : "";
        if (!b) {
            d.width = 200;
        } else {
            if (c == "form" || c == "anchor") {
                d.anchor = "100%";
            }
        }
//        var a = this.owner && this.owner.isColumn;
//        if (a) {
//            var f = this.owner.owner;
//            var g = f.node.childNodes.indexOf(this.owner.node);
//            if (!f.canContainEditors) {
//                var e = xds.inspector.transform(this.owner.owner.getNode(), "editorgrid", null, null, false);
//                if(e.childNodes[g].firstChild){
//                	e.childNodes[g].firstChild.select();
//                }
//            }
//        }
    },
    getTransforms: function () {
        var a = xds.types.FieldBase.superclass.getTransforms.call(this);
        if (this.owner && this.owner.isColumn && this.owner.owner.canContainEditors) {
            var c;
            var d = [];
            var b = a.length;
            while (b--) {
                c = a[b];
                if (c.transtype !== "htmleditor" && c.transtype !== "textarea") {
                    d.push(c);
                }
            }
            a = d;
        }
        return a;
    },
    xdConfigs: [{
        name: "autoCreate",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "cls",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "disabled",
        group: "Ext.form.Field",
        ctype: "boolean"
    },{
        name: "fieldClass",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "focusClass",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "inputType",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "invalidText",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "msgFx",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "msgTarget",
        group: "Ext.form.Field",
        ctype: "string",
        editor: "options",
        options: ["qtip","title","under","side"]
    },{
        name: "name",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "preventMark",
        group: "Ext.form.Field",
        ctype: "boolean"
    },{
        name: "readOnly",
        group: "Ext.form.Field",
        ctype: "boolean"
    },{
        name: "submitValue",
        group: "Ext.form.Field",
        ctype: "boolean"
    },{
        name: "tabIndex",
        group: "Ext.form.Field",
        ctype: "number"
    },{
        name: "validateOnBlur",
        group: "Ext.form.Field",
        ctype: "boolean"
    },{
        name: "validationDelay",
        group: "Ext.form.Field",
        ctype: "number"
    },{
        name: "validationEvent",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "value",
        group: "Ext.form.Field",
        ctype: "string"
    },{
        name: "valid",
        group: "(Event)",
        ctype: "fn",
        params: ['me']
    }]
});

xds.types.CheckboxGroup = Ext.extend(xds.types.FieldBase, {
    isContainer: true,
    category: "Form",
    naming: "CheckboxGroup",
    defaultConfig: {
        fieldLabel: "Label"
    },
    cid: "checkboxgroup",
    defaultName: "&lt;checkboxGroup&gt;",
    text: "CheckboxGroup",
    xtype: "checkboxgroup",
    dtype: "xdcheckboxgroup",
    iconCls: "icon-checkbox-group",
    filmCls: "el-film-nolabel",
    xcls: "Ext.form.CheckboxGroup",
    getDefaultInternals: function () {
        return xds.types.CheckboxGroup.superclass.getDefaultInternals.call(this, {
            cn: [{
                cid: 'checkbox'
            }]
        });
    },
    xdConfigs: [{
        name: "allowBlank",
        group: "Ext.form.CheckboxGroup",
        ctype: "boolean"
    },{
        name: "blankText",
        group: "Ext.form.CheckboxGroup",
        ctype: "string"
    },{
        name: "columns",
        group: "Ext.form.CheckboxGroup",
        ctype: "number"
    },{
        name: "vertical",
        group: "Ext.form.CheckboxGroup",
        ctype: "boolean"
    }]
});
//xds.Registry.register(xds.types.CheckboxGroup);
xds.CheckboxGroup = Ext.extend(Ext.form.CheckboxGroup, {
    getFilmEl: xds.types.BaseType.getFilmEl
});
Ext.reg("xdcheckboxgroup", xds.CheckboxGroup);

xds.types.TextField = Ext.extend(xds.types.FieldBase, {
    cid: "textfield",
    defaultName: "&lt;textField&gt;",
    text: "Text Field",
    dtype: "xdtextfield",
    xtype: "textfield",
    xcls: "Ext.form.TextField",
    iconCls: "icon-textfield",
    transformGroup: "fields",
    xdConfigs: [{
        name: "allowBlank",
        group: "Ext.form.TextField",
        ctype: "boolean"
    },{
        name: "blankText",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "disableKeyFilter",
        group: "Ext.form.TextField",
        ctype: "boolean"
    },{
        name: "emptyClass",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "enableKeyEvents",
        group: "Ext.form.TextField",
        ctype: "boolean"
    },{
        name: "emptyText",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "grow",
        group: "Ext.form.TextField",
        ctype: "boolean"
    },{
        name: "growMax",
        group: "Ext.form.TextField",
        ctype: "number"
    },{
        name: "growMin",
        group: "Ext.form.TextField",
        ctype: "number"
    },{
        name: "maskRe",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "maxLength",
        group: "Ext.form.TextField",
        ctype: "number"
    },{
        name: "maxLengthText",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "minLength",
        group: "Ext.form.TextField",
        ctype: "number"
    },{
        name: "minLengthText",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "regex",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "regexText",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "selectOnFocus",
        group: "Ext.form.TextField",
        ctype: "boolean"
    },{
        name: "stripCharsRe",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "vtype",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "vtypeText",
        group: "Ext.form.TextField",
        ctype: "string"
    },{
        name: "validator",
        group: "Ext.form.TextField",
        setFn: 'setConfig',
        getFn: 'getConfigValue',
        ctype: "fn",
        params: ['value']
    },{
        name: "change",
        group: "(Event)",
        ctype: "fn",
        params: ['textfield', 'newValue', 'oldValue']
    },{
        name: "blur",
        group: "(Event)",
        ctype: "fn",
        params: ['field']
    },{
    	name: "keyup",
        group: "(Event)",
        ctype: "fn",
        params: ['textfield','e']
    }]
});
//xds.Registry.register(xds.types.TextField);
xds.TextField = Ext.extend(Ext.form.TextField, {
    getFilmEl: xds.types.BaseType.getFilmEl
});
Ext.reg("xdtextfield", xds.TextField);

xds.types.Trigger = Ext.extend(xds.types.TextField, {
    cid: 'trigger',
    iconCls: 'icon-trigger',
    category: "Form",
    defaultName: "&lt;TriggerField&gt;",
    text: "TriggerField",
    dtype: "xdtrigger",
    xtype: 'trigger',
    xcls: "Ext.form.TriggerField",
    naming: "TriggerField",
    isContainer: false,
    layoutable: false,
    //    enableFlyout: false,
    bindable: false,
    defaultConfig: {
        anchor: "100%",
        fieldLabel: "Label"
    },
    xdConfigs: [
    {
        name: "autoCreate",
        group: "Ext.form.TriggerField",
        ctype: "string"
    },{
        name: "editable",
        group: "Ext.form.TriggerField",
        ctype: "boolean"
    },{
        name: "hideTrigger",
        group: "Ext.form.TriggerField",
        ctype: "boolean"
    },{
        name: "readOnly",
        group: "Ext.form.TriggerField",
        ctype: "boolean"
    },{
        name: "triggerClass",
        group: "Ext.form.TriggerField",
        ctype: "string"
    },{
        name: "wrapFocusClass",
        group: "Ext.form.TriggerField",
        ctype: "string"
    },{
        name: 'triggerclick',
        group: '(Event)',
        ctype: 'fn',
        params: ['field']
    },{
        name: 'afterRender',
        group: '(Event)',
        ctype: 'fn',
        params: ['field']
    }]
});
//xds.Registry.register(xds.types.Trigger);
xds.Trigger = Ext.extend(Ext.form.TriggerField, {});
Ext.reg('xdtrigger', xds.Trigger);

xds.types.Checkbox = Ext.extend(xds.types.FieldBase, {
    cid: "checkbox",
    defaultName: "&lt;checkbox&gt;",
    text: "Checkbox",
    dtype: "xdcheckbox",
    xtype: "checkbox",
    xcls: "Ext.form.Checkbox",
    iconCls: "icon-checkbox",
    naming: "MyCheckbox",
    filmCls: "el-film-nolabel",
    transformGroup: "boolFields",
    defaultConfig: {
        boxLabel: "boxLabel"
    },
    initConfig: function (comp, ct) {
        if (ct.xtype != "checkboxgroup") {
            fieldLabel = "Label";
        }
    },
    defaultColumn: "booleancolumn",
    xdConfigs: [{
        name: "autoCreate",
        group: "Ext.form.Checkbox",
        ctype: "string"
    },{
        name: "boxLabel",
        group: "Ext.form.Checkbox",
        ctype: "string"
    },{
        name: "checked",
        group: "Ext.form.Checkbox",
        ctype: "boolean"
    },{
        name: "fieldClass",
        group: "Ext.form.Checkbox",
        ctype: "string"
    },{
        name: "focusClass",
        group: "Ext.form.Checkbox",
        ctype: "string"
    },{
        name: "inputValue",
        group: "Ext.form.Checkbox",
        ctype: "string"
    },{
    	name: "check",
        group: "(Event)",
        ctype: "fn",
        params: ["checkbox","checked"]
    }]
});
//xds.Registry.register(xds.types.Checkbox);
xds.Checkbox = Ext.extend(Ext.form.Checkbox, {});
Ext.reg("xdcheckbox", xds.Checkbox);


xds.types.DateField = Ext.extend(xds.types.Trigger, {
    cid: "datefield",
    defaultName: "&lt;dateField&gt;",
    text: "Date Field",
    dtype: "xddatefield",
    xtype: "datefield",
    xcls: "Ext.form.DateField",
    iconCls: "icon-datefield",
    naming: "MyField",
    transformGroup: "fields",
    defaultColumn: "datecolumn",
    xdConfigs: [{
        name: "altFormats",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "autoCreate",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "disabledDatesText",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "disabledDaysText",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "format",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "invalidText",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "maxText",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "maxValue",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "minText",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "minValue",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
        name: "showToday",
        group: "Ext.form.DateField",
        ctype: "boolean"
    },{
        name: "startDay",
        group: "Ext.form.DateField",
        ctype: "number"
    },{
        name: "triggerClass",
        group: "Ext.form.DateField",
        ctype: "string"
    },{
       	name: "select",
        group: "(Event)",
        ctype: "fn",
        params: ["datefield","date"]
    }]
});
//xds.Registry.register(xds.types.DateField);
xds.DateField = Ext.extend(Ext.form.DateField, {
    getFilmEl: xds.types.BaseType.getFilmEl
});
Ext.reg("xddatefield", xds.DateField);


xds.types.HtmlEditor = Ext.extend(xds.types.FieldBase, {
    cid: "htmleditor",
    defaultName: "&lt;htmlEditor&gt;",
    text: "Html Editor",
    dtype: "xdhtmleditor",
    xtype: "htmleditor",
    xcls: "Ext.form.HtmlEditor",
    iconCls: "icon-html",
    transformGroup: "fields",
    defaultConfig: {
        anchor: "100%",
        fieldLabel: "Label",
        height: 150,
        width: 300
    },
    isResizable: function (a, b) {
        return !this.getConfigValue("anchor") && (!this.owner || this.owner.getConfigValue("layout") != "form");
    },
    xdConfigs: [{
        name: "createLinkText",
        group: "Ext.form.HtmlEditor",
        ctype: "string"
    },{
        name: "defaultLinkValue",
        group: "Ext.form.HtmlEditor",
        ctype: "string"
    },{
        name: "defaultValue",
        group: "Ext.form.HtmlEditor",
        ctype: "string"
    },{
        name: "enableAlignments",
        group: "Ext.form.HtmlEditor",
        ctype: "boolean"
    },{
        name: "enableColors",
        group: "Ext.form.HtmlEditor",
        ctype: "boolean"
    },{
        name: "enableFont",
        group: "Ext.form.HtmlEditor",
        ctype: "boolean"
    },{
        name: "enableFontSize",
        group: "Ext.form.HtmlEditor",
        ctype: "boolean"
    },{
        name: "enableFormat",
        group: "Ext.form.HtmlEditor",
        ctype: "boolean"
    },{
        name: "enableLinks",
        group: "Ext.form.HtmlEditor",
        ctype: "boolean"
    },{
        name: "enableLists",
        group: "Ext.form.HtmlEditor",
        ctype: "boolean"
    },{
        name: "enableSourceEdit",
        group: "Ext.form.HtmlEditor",
        ctype: "boolean"
    }]
});
//xds.Registry.register(xds.types.HtmlEditor);
xds.HtmlEditor = Ext.extend(Ext.form.HtmlEditor, {
    getFilmEl: xds.types.BaseType.getFilmEl,
    createIFrame: function () {
        xds.HtmlEditor.superclass.createIFrame.call(this);
        //		this.bogusFrame = this.wrap.createChild({
        //			cls: "xds-bogus-frame x-form-text"
        //		});
    }
});
Ext.reg("xdhtmleditor", xds.HtmlEditor);


xds.types.NumberField = Ext.extend(xds.types.TextField, {
    cid: "numberfield",
    defaultName: "&lt;numberField&gt;",
    text: "Number Field",
    dtype: "xdnumberfield",
    xtype: "numberfield",
    xcls: "Ext.form.NumberField",
    iconCls: "icon-numfield",
    transformGroup: "fields",
    defaultColumn: "numbercolumn",
    xdConfigs: [{
        name: "allowDecimals",
        group: "Ext.form.NumberField",
        ctype: "boolean"
    },{
        name: "allowNegative",
        group: "Ext.form.NumberField",
        ctype: "boolean"
    },{
        name: "autoStripChars",
        group: "Ext.form.NumberField",
        ctype: "boolean"
    },{
        name: "baseChars",
        group: "Ext.form.NumberField",
        ctype: "boolean"
    },{
        name: "decimalPrecision",
        group: "Ext.form.NumberField",
        ctype: "number"
    },{
        name: "decimalSeparator",
        group: "Ext.form.NumberField",
        ctype: "string"
    },{
        name: "fieldClass",
        group: "Ext.form.NumberField",
        ctype: "string"
    },
    {
        name: "maxText",
        group: "Ext.form.NumberField",
        ctype: "string"
    },{
        name: "maxValue",
        group: "Ext.form.NumberField",
        ctype: "number"
    },{
        name: "minText",
        group: "Ext.form.NumberField",
        ctype: "string"
    },{
        name: "minValue",
        group: "Ext.form.NumberField",
        ctype: "number"
    },{
        name: "nanText",
        group: "Ext.form.NumberField",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.NumberField);
xds.NumberField = Ext.extend(Ext.form.NumberField, {
    getFilmEl: xds.types.BaseType.getFilmEl
});
Ext.reg("xdnumberfield", xds.NumberField);


xds.types.RadioGroup = Ext.extend(xds.types.CheckboxGroup, {
    isContainer: true,
    category: "Form",
    naming: "RadioGroup",
    filmCls: "el-film-nolabel",
    defaultConfig: {
        fieldLabel: "Label"
    },
    cid: "radiogroup",
    defaultName: "&lt;radioGroup&gt;",
    text: "RadioGroup",
    xtype: "radiogroup",
    dtype: "xdradiogroup",
    iconCls: "icon-radio-group",
    xcls: "Ext.form.RadioGroup",
    getDefaultInternals: function () {
        return xds.types.RadioGroup.superclass.getDefaultInternals.call(this, {
            cn: [{
                cid: 'radio'
            }]
        });
    },
    xdConfigs: [{
        name: "allowBlank",
        group: "Ext.form.RadioGroup",
        ctype: "boolean"
    },{
        name: "blankText",
        group: "Ext.form.RadioGroup",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.RadioGroup);
xds.RadioGroup = Ext.extend(Ext.form.RadioGroup, {
    getFilmEl: xds.types.BaseType.getFilmEl
});
Ext.reg("xdradiogroup", xds.RadioGroup);


xds.types.Radio = Ext.extend(xds.types.Checkbox, {
    cid: "radio",
    defaultName: "&lt;radio&gt;",
    text: "Radio",
    dtype: "xdradio",
    xtype: "radio",
    xcls: "Ext.form.Radio",
    iconCls: "icon-radio",
    naming: "MyRadio",
    filmCls: "el-film-nolabel",
    transformGroup: "boolFields",
    defaultConfig: {
        boxLabel: "boxLabel"
    },
    initConfig: function (comp, ct) {
        if (ct.xtype != 'radiogroup') {
            fieldLabel = "Label";
        }
    }
});
//xds.Registry.register(xds.types.Radio);
xds.Radio = Ext.extend(Ext.form.Radio, {
    getFilmEl: xds.types.BaseType.getFilmEl
});
Ext.reg("xdradio", xds.Radio);


xds.types.Hidden = Ext.extend(xds.types.FieldBase, {
    cid: "hidden",
    defaultName: "&lt;hidden&gt;",
    text: "Hidden",
    dtype: "hidden",
    xtype: "hidden",
    xcls: "Ext.form.Hidden",
    iconCls: "icon-textfield",
    transformGroup: "fields"
});
//xds.Registry.register(xds.types.Hidden);

xds.types.TextArea = Ext.extend(xds.types.TextField, {
    cid: "textarea",
    defaultName: "&lt;ctextArea&gt;",
    text: "Text Area",
    dtype: "xdtextarea",
    xtype: "textarea",
    xcls: "Ext.form.TextArea",
    iconCls: "icon-textarea",
    transformGroup: "fields",
    isResizable: function (a, b) {
        return !this.getConfigValue("anchor") && (!this.owner || this.owner.getConfigValue("layout") != "form");
    },
    xdConfigs: [{
        name: "autoCreate",
        group: "Ext.form.TextArea",
        ctype: "string"
    },{
        name: "growMax",
        group: "Ext.form.TextField",
        ctype: "number"
    },{
        name: "growMin",
        group: "Ext.form.TextField",
        ctype: "number"
    },{
        name: "preventScrollbars",
        group: "Ext.form.TextField",
        ctype: "boolean"
    }]
});
//xds.Registry.register(xds.types.TextArea);
xds.TextArea = Ext.extend(Ext.form.TextArea, {
    getFilmEl: xds.types.BaseType.getFilmEl
});
Ext.reg("xdtextarea", xds.TextArea);

xds.types.ComboBox = Ext.extend(xds.types.Trigger, {
    cid: 'combo',
    iconCls: 'icon-combo',
    category: "Form",
    defaultName: "&lt;ComboBox&gt;",
    text: "ComboBox",
    dtype: "xdcombo",
    xtype: 'combo',
    xcls: "Ext.form.ComboBox",
    naming: "ComboBox",
    isContainer: false,
    layoutable: false,
    //    enableFlyout: false,
    bindable: true,
    transformGroup: 'fields',
    defaultConfig: {
        anchor: "100%",
        fieldLabel: "Label",
        queryAction: 'all'
    },
    getReferenceForConfig: function (b, a) {
        var c = xds.types.ComboBox.superclass.getReferenceForConfig.call(this, b, a);
        if (b.isColumn) {
            c.type = "object";
            c.ref = "store";
        }
        return c;
    },
    xdConfigs: [{
        name: "allQuery",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "allowBlank",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "autoCreate",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "autoSelect",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "clearFilterOnReset",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "displayField",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "forceSelection",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "handleHeight",
        group: "Ext.form.ComboBox",
        ctype: "number"
    },{
        name: "hiddenId",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "hiddenName",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "hiddenValue",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "itemSelector",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "lazyInit",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "lazyRender",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "listAlign",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "listClass",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "listEmptyText",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "listWidth",
        group: "Ext.form.ComboBox",
        ctype: "number"
    },{
        name: "loadingText",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "maxHeight",
        group: "Ext.form.ComboBox",
        ctype: "number"
    },{
        name: "minChars",
        group: "Ext.form.ComboBox",
        ctype: "number"
    },{
        name: "minHeight",
        group: "Ext.form.TextField",
        ctype: "number"
    },{
        name: "minListWidth",
        group: "Ext.form.ComboBox",
        ctype: "number"
    },{
        name: "mode",
        group: "Ext.form.ComboBox",
        ctype: "string",
        editor: 'options',
        options: ['local', 'remote']
    },{
        name: "pageSize",
        group: "Ext.form.ComboBox",
        ctype: "number"
    },{
        name: "queryDelay",
        group: "Ext.form.ComboBox",
        ctype: "number"
    },{
        name: "queryParam",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "resizable",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "selectOnFocus",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "selectedClass",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "submitValue",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "title",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "tpl",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "triggerAction",
        group: "Ext.form.ComboBox",
        ctype: "string",
        editor: 'options',
        options: ['all', 'query']
    },{
        name: "triggerClass",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "typeAhead",
        group: "Ext.form.ComboBox",
        ctype: "boolean"
    },{
        name: "typeAheadDelay",
        group: "Ext.form.ComboBox",
        ctype: "number"
    },{
        name: "valueField",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "valueNotFoundText",
        group: "Ext.form.ComboBox",
        ctype: "string"
    },{
        name: "beforerender",
        group: "(Event)",
        ctype: "fn",
        params: ["field"]
    },{
		name:'select',
		group:'(Event)',
		ctype:'fn',
		params:['combo','record','index']
	}]
});
//xds.Registry.register(xds.types.ComboBox);
xds.ComboBox = Ext.extend(Ext.form.ComboBox, {});
Ext.reg('xdcombo', xds.ComboBox);


xds.types.TimeField = Ext.extend(xds.types.ComboBox, {
    cid: "timefield",
    defaultName: "&lt;timeField&gt;",
    text: "Time Field",
    dtype: "xdtimefield",
    xtype: "timefield",
    xcls: "Ext.form.TimeField",
    iconCls: "icon-timefield",
    transformGroup: "fields",
    xdConfigs: [{
        name: "altFormats",
        group: "Ext.form.TimeField",
        ctype: "string"
    },{
        name: "format",
        group: "Ext.form.TimeField",
        ctype: "string"
    },{
        name: "increment",
        group: "Ext.form.TimeField",
        ctype: "number"
    },{
        name: "invalidText",
        group: "Ext.form.TimeField",
        ctype: "string"
    },{
        name: "maxText",
        group: "Ext.form.TimeField",
        ctype: "string"
    },{
        name: "maxValue",
        group: "Ext.form.TimeField",
        ctype: "string"
    }]
});
//xds.Registry.register(xds.types.TimeField);
xds.TimeField = Ext.extend(Ext.form.TimeField, {
    getFilmEl: xds.types.BaseType.getFilmEl
});
Ext.reg("xdtimefield", xds.TimeField);


xds.types.DataField = Ext.extend(xds.types.BaseType, {
    cid: "datafield",
    category: "Data",
    name: "DataField",
    text: "Data Field",
    xtype: "datafield",
    dtype: "xddatafield",
    xcls: "Ext.data.DataField",
    iconCls: "icon-data-field",
    naming: "MyField",
    isVisual: false,
    defaultConfig: {
        name: "field",
        type: "string"
    },
    initConfig: function (b, a) {
        b.storeId = this.id;
    },
    setConfig: function (a, b) {
        this.supr().setConfig.call(this, a, b);
        this.owner.reconfigure();
    },
    xdConfigs: [{
        name: "allowBlank",
        group: "Ext.data.Field",
        ctype: "boolean"
    },{
        name: "dateFormat",
        group: "Ext.data.Field",
        ctype: "string"
    },{
        name: "defaultValue",
        group: "Ext.data.Field",
        ctype: "string"
    },{
        name: "mapping",
        group: "Ext.data.Field",
        ctype: "string"
    },{
        name: "name",
        group: "Ext.data.Field",
        ctype: "string"
    },{
        name: "text",
        group: "Ext.data.Field",
        ctype: "string"
    },{
        name: "permissionId",
        group: "Ext.data.Field",
        ctype: "string"
    },{
        name: "sortDir",
        group: "Ext.data.Field",
        ctype: "string",
        editor: "options",
        options: ["ASC","DESC"]
    },{
        name: "useNull",
        group: "Ext.data.Field",
        ctype: "boolean"
    },{
        name: "type",
        group: "Ext.data.Field",
        ctype: "string",
        editor: "options",
        options: ["auto", "boolean", "date", "float", "int", "string", "bizcode"]
    },{
        name: "convert",
        group: "Ext.data.Field",
        ctype: "fn",
        setFn: 'setConfig',
        getFn: 'getConfigValue',
        params: ["value", "record"]
    }]
});
//xds.Registry.register(xds.types.DataField);


xds.types.TreePanel = Ext.extend(xds.types.Panel, {
    cid: "tree",
    category: "Tree",
    defaultName: "&lt;tree&gt;",
    text: "TreePanel",
    dtype: "xdtree",
    xtype: "treepanel",
    xcls: "Ext.tree.TreePanel",
    iconCls: "icon-treepanel",
    naming: "MyTree",
    isContainer: true,
    autoScrollable: false,
    layoutable: false,
    //    enableFlyout: false,
    validChildTypes: ["treeloader", "toolbar"],
    initConfig: function (b, a) {
        if (!a) {
            b.width = 400;
            b.height = 250;
            b.title = "Tree Panel";
        }
    },
    xdConfigs: [{
        name: "animate",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "containerScroll",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "ddAppendOnly",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "ddGroup",
        group: "Ext.tree.TreePanel",
        ctype: "string"
    },{
        name: "ddScroll",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "enableDD",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "enableDrag",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "enableDrop",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "hlColor",
        group: "Ext.tree.TreePanel",
        ctype: "string"
    },{
        name: "hlDrop",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "linse",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "pathSeparator",
        group: "Ext.tree.TreePanel",
        ctype: "string"
    },{
        name: "rootVisible",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "singleExpand",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "trackMouseOver",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "useArrows",
        group: "Ext.tree.TreePanel",
        ctype: "boolean"
    },{
        name: "movenode",
        group: "(Event)",
        ctype: "fn",
        params: ['tree', 'node', 'oldParent', 'newParent', 'orderBy']
    },{
        name: "contextmenu",
        group: "(Event)",
        ctype: "fn",
        params: ['node', 'evt']
    },{
        name: "dblclick",
        group: "(Event)",
        ctype: "fn",
        params: ['node', 'evt']
    },{
        name: "checkchange",
        group: "(Event)",
        ctype: "fn",
        params: ['node', 'checked']
    },{
        name: "dataUrl",
        group: "Ext.tree.TreePanel",
        ctype: "string"
    },{
        name: "requestMethod",
        group: "Ext.data.Store",
        ctype: "string",
        editor: 'options',
        options: ['POST', 'GET', 'DELETE', 'HEAD', 'PUT']
    },{
        name: "afterrender",
        group: "(Event)",
        ctype: "fn",
        params: ['tree']
    },{
        name: "beforerender",
        group: "(Event)",
        ctype: "fn",
        params: ['tree']
    },{
        name: "click",
        group: "(Event)",
        ctype: "fn",
        params: ['node', 'evt']
    }],
    getDefaultInternals: function () {
        return xds.types.TreePanel.superclass.getDefaultInternals.call(this, {
            cn: [{
                cid: 'treenode',
                text: 'DefaultRoot',
                isRoot: true,
                leaf: false,
                expanded: true
            }]
        });
    },
    getReferenceForConfig: function (c, b) {
        var a = xds.types.TreePanel.superclass.getReferenceForConfig.call(this, c, b);
        if (c.isRoot) {
            a.ref = "root";
            a.type = "string";
        }
        return a;
    },
    createCanvasConfig: function (g) {
        var f = xds.types.TreePanel.superclass.createCanvasConfig.call(this, g);
        if (f.dataUrl) {
            delete f.dataUrl;
        }
        return f;
    },
    getActions: function () {
        if (!this.actions) {
            this.actions = [new Ext.Action({
                itemId: "load-treenode",
                text: "Load treenode",
                iconCls: "icon-load",
                handler: function () {
                    var com = this.getExtComponent();
                    if (com) {
                        com.getLoader().load(com.root);
                    }
                },
                scope: this,
                disabled: !this.getConfigValue("dataUrl")
            })];
        }
        return this.actions;
    }
});
//xds.Registry.register(xds.types.TreePanel);
xds.TreePanel = Ext.extend(Ext.tree.TreePanel, {
    createFbar: function (b) {
        var a = this.minButtonWidth;
        this.elements += ",footer";
        this.fbar = this.createToolbar(b, {
            buttonAlign: this.buttonAlign,
            toolbarCls: "x-panel-fbar",
            enableOverflow: false,
            defaults: function (d) {
                return {
                    minWidth: d.minWidth || a
                };
            }
        });
        if (this.fbar.items) {
            this.fbar.items.each(function (d) {
                d.minWidth = d.minWidth || this.minButtonWidth;
            }, this);
            this.buttons = this.fbar.items.items;
        }
    }
});
Ext.reg("xdtree", xds.TreePanel);


xds.types.TreeNode = Ext.extend(xds.types.BaseType, {
    cid: "treenode",
    category: "Tree",
    defaultName: "&lt;TreeNode&gt;",
    text: "TreeNode",
    dtype: "xdtreenode",
    xtype: 'treenode',
    xcls: "Ext.tree.TreeNode",
    iconCls: "icon-treenode",
    naming: "TreeNode",
    filmCls: "el-film-nolabel",
    isContainer: true,
    layoutable: false,
    //    enableFlyout: false,
    validChildTypes: ["treenode"],
    defaultConfig: {
        text: 'TreeNode',
        nodeType: 'xdtreenode'
    },
    getInternals: function (includeChild) {
        var ret = xds.types.TreeNode.superclass.getInternals.call(this, includeChild);
        if (this.isRoot) {
            return Ext.apply({
                isRoot: true
            }, ret);
        }
        return ret;
    },
    getReferenceForConfig: function (c, b) { //b:cfg
        var a = xds.types.TreeNode.superclass.getReferenceForConfig.call(this, c, b);
        if (c.cid == 'treenode') {
            a.ref = "children";
            a.type = "array";
        }
        return a;
    },
    createCanvasConfig: function (g) {
        var f = xds.types.TreeNode.superclass.createCanvasConfig.call(this, g);
        if (Ext.isArray(f.children) && f.children.length > 0) {
            f.leaf = false;
            f.expanded = true;
        } else {
            f.leaf = true;
        }
        return f;
    },
    onFilmClick: function (b) {
        var a = this.getExtComponent();
    },
    xdConfigs: [{
        name: "allowChildren",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "allowDrag",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "allowDrop",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "checked",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "cls",
        group: "Ext.tree.TreeNode",
        ctype: "string"
    },{
        name: "disabled",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "draggable",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "editable",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "expandable",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "expanded",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "hidden",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "href",
        group: "Ext.tree.TreeNode",
        ctype: "string"
    },{
        name: "hrefTarget",
        group: "Ext.tree.TreeNode",
        ctype: "string"
    },{
        name: "icon",
        group: "Ext.tree.TreeNode",
        ctype: "string"
    },{
        name: "iconCls",
        group: "Ext.tree.TreeNode",
        ctype: "string",
        editor: "icon"
    },{
        name: "id",
        group: "Ext.tree.TreeNode",
        ctype: "string"
    },{
        name: "isTarget",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "leaf",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "qtip",
        group: "Ext.tree.TreeNode",
        ctype: "string"
    },{
        name: "qtipCfg",
        group: "Ext.tree.TreeNode",
        ctype: "string"
    },{
        name: "singleClickExpand",
        group: "Ext.tree.TreeNode",
        ctype: "boolean"
    },{
        name: "text",
        group: "Ext.tree.TreeNode",
        ctype: "string"
    },{
        name: "click",
        group: "(Event)",
        ctype: "fn",
        params: ['node', 'evt'],
        help: '<b>click</b> : ( Node this, Ext.EventObject e ) </br></br> Fires when this node is clicked.'
    },{
        name: "dblclick",
        group: "(Event)",
        ctype: "fn",
        params: ['node', 'evt']
    }]
});
//xds.Registry.register(xds.types.TreeNode);
xds.TreeNode = function (attributes) {
    xds.TreeNode.superclass.constructor.call(this, attributes);
    this.viewerNode = attributes.viewerNode;
    this.initialConfig = {};
};
Ext.extend(xds.TreeNode, Ext.tree.AsyncTreeNode, {
    render: function () {
        xds.TreeNode.superclass.render.call(this);
        if (this.isRoot) {
            this.createFilm();
        }
    },
    getItemId: function () {
        return this.id;
    },
    onAdded: function (parent, index) {},
    initRef: function () {},
    createFilm: function () {
        if (this.film) {
            Ext.destroy(this.film);
        }
        var a = Ext.getCmp('canvas');
        var d = this.viewerNode;
        this.film = a.body.createChild({
            cls: "el-film x-unselectable " + d.component.filmCls,
            id: "film-for-" + d.id,
            style: "z-index: " + (50000 + (d.getDepth() * 100))
            //            cn: [{
            //                tag: "b",
            //                cls: d.component.flyoutCls
            //            },
            //            {
            //                tag: "i",
            //                html: d.component.dock || d.text
            //            }]
        });
        this.film.enableDisplayMode();
        this.syncFilm();
        this.on("resize", this.syncFilm, this);
        this.on("destroy", this.destroyDesigner, this);
        var cs = this.childNodes;
        if (cs && cs.length > 0) {
            for (var i = 0, len = cs.length; i < len; i++) {
                cs[i].createFilm();
            }
        }
    },
    syncFilm: function () {
        var e;
        if (this.film && (e = this.getFilmEl())) {
            var d = Ext.lib.Dom.getRegion(e);
            this.film.setRegion(d);
            this.film.lastRegion = d;
        }
    },
    getFilmEl: function () {
        var d = this.ui.elNode;
        if (this.fieldLabel) {
            return this.el.up(".x-form-item") || d;
        }
        return d;
    },
    positionFilm: function () {
        this.film.setXY(this.ui.elNode.getXY());
    },
    destroy: function (silent) {
        xds.TreeNode.superclass.destroy.call(this, silent);
        if (this.film) {
            this.film.remove();
            Ext.destroy(this.film);
        }
    }
});
Ext.reg('xdtreenode', xds.TreeNode);
Ext.tree.TreePanel.nodeTypes.xdtreenode = xds.TreeNode;


xds.types.DataView = Ext.extend(xds.types.BoxComponent, {
    cid: 'dataview',
    iconCls: 'icon-dataview',
    category: "Data",
    defaultName: "&lt;DataView&gt;",
    text: "Data View",
    dtype: "xddataview",
    xtype: 'dataview',
    xcls: "Ext.DataView",
    naming: "DataView",
    isContainer: false,
    layoutable: false,
    bindable: true,
    defaultConfig: {
        store: "(dataview sample)",
        tpl: '<div class="xds-designer"><tpl for="."><div class="thumb-wrap" id="{name}"><div class="thumb"><img src="{url}" title="{name}"></div><span class="x-editable">{shortName}</span></div></tpl><div class="x-clear"></div></div>',
        overClass: 'x-view-over',
        itemSelector: 'div.thumb-wrap'
    },
    xdConfigs: [{
        name: "blockRefresh",
        group: "Ext.DataView",
        ctype: "boolean"
    },{
        name: "deferEmptyText",
        group: "Ext.DataView",
        ctype: "boolean"
    },{
    	name: "dragSelector",
    	group: "Ext.DataView",
    	ctype: "boolean"
    },{
        name: "emptyText",
        group: "Ext.DataView",
        ctype: "string"
    },{
        name: "itemSelector",
        group: "Ext.DataView",
        ctype: "string"
    },{
    	name: "labelEditor",
    	group: "Ext.DataView",
    	ctype: "boolean"
    },{
        name: "loadingText",
        group: "Ext.DataView",
        ctype: "string"
    },{
        name: "multiSelect",
        group: "Ext.DataView",
        ctype: "boolean"
    },{
        name: "overClass",
        group: "Ext.DataView",
        ctype: "string"
    },{
        name: "selectedClass",
        group: "Ext.DataView",
        ctype: "string"
    },{
        name: "simpleSelect",
        group: "Ext.DataView",
        ctype: "boolean"
    },{
        name: "singleSelect",
        group: "Ext.DataView",
        ctype: "boolean"
    },{
        name: "tpl",
        group: "Ext.DataView",
        ctype: "text"
    },{
        name: "trackOver",
        group: "Ext.DataView",
        ctype: "boolean"
    },{
        name: "click",
        group: "(Event)",
        ctype: "fn",
        params: ['dataview', 'index', 'node', 'evt']
    },{
        name: "contextmenu",
        group: "(Event)",
        ctype: "fn",
        params: ['dataview', 'index', 'node', 'evt']
    },{
        name: "dblclick",
        group: "(Event)",
        ctype: "fn",
        params: ['dataview', 'index', 'node', 'evt']
    },{
        name: "selectionchange",
        group: "(Event)",
        ctype: "fn",
        params: ['dataview', 'selections']
    },{
        name: "afterrender",
        group: "(Event)",
        ctype: "fn",
        params: ['dataview']
    },{
    	name: "beforerender",
        group: "(Event)",
        ctype: "fn",
        params: ['dataview']
    }]
});
//xds.Registry.register(xds.types.DataView);
xds.DataView = Ext.extend(Ext.DataView, {});
Ext.reg('xddataview', xds.DataView);


xds.DataView.DefaultStore = new Ext.data.JsonStore({
    storeId: "(dataview sample)",
    fields: [{
        name: "id",
        type: "string"
    },{
        name: "name",
        type: "string"
    },{
        name: "url",
        type: "string"
    },{
        name: "shortName",
        type: "string"
    }],
    data: [{
        id: '001',
        name: "Item001",
        url: '/tplt/images/page.png',
        shortName: 'Item001'
    },{
        id: '002',
        name: "Item002",
        url: '/tplt/images/page.png',
        shortName: 'Item002'
    },{
        id: '003',
        name: "Item003",
        url: '/tplt/images/page.png',
        shortName: 'Item003'
    }]
});

xds.types.FileField = Ext.extend(xds.types.TextField, {
    cid: 'filefield',
    iconCls: 'icon-file-field',
    category: "Form",
    defaultName: "&lt;FileField&gt;",
    text: "FileField",
    dtype: "xdfilefield",
    xtype: 'fileuploadfield',
    xcls: "Ext.form.FileUploadField",
    naming: "FileField",
    isContainer: false,
    layoutable: false,
    //    enableFlyout: false,
    bindable: false,
    defaultConfig: {
        fieldLabel: "Label"
    },
    xdConfigs: [{
        name: "buttonText",
        group: "Ext.form.FileUploadField",
        ctype: "string"
    },{
        name: "buttonIcon",
        group: "Ext.form.FileUploadField",
        ctype: "string"
    },{
        name: "buttonOnly",
        group: "Ext.form.FileUploadField",
        ctype: "boolean"
    },{
        name: "buttonOffset",
        group: "Ext.form.FileUploadField",
        ctype: "number"
    }]
});
//xds.Registry.register(xds.types.FileField);
xds.FileField = Ext.extend(Ext.form.FileUploadField, {});
Ext.reg('xdfilefield', xds.FileField);

xds.types.registerAllTypes = function(){
	xds.Registry.register(xds.types.Module);
	xds.Registry.register(xds.types.Component);
	xds.Registry.register(xds.types.BoxComponent);
	xds.Registry.register(xds.types.Container);
	xds.Registry.register(xds.types.Panel);
	xds.Registry.register(xds.types.TabPanel);
	xds.Registry.register(xds.types.Viewport);
	xds.Registry.register(xds.types.Window);
	xds.Registry.register(xds.types.Button);
	xds.Registry.register(xds.types.Label);
	xds.Registry.register(xds.types.Slider);
	xds.Registry.register(xds.types.ProgressBar);
	xds.Registry.register(xds.types.Toolbar);
	xds.Registry.register(xds.types.ToolbarSeparator);
	xds.Registry.register(xds.types.ToolbarSpacer);
	xds.Registry.register(xds.types.ToolbarText);
	xds.Registry.register(xds.types.ToolbarFill);
	xds.Registry.register(xds.types.PagingToolbar);
	xds.Registry.register(xds.types.ButtonGroup);
	xds.Registry.register(xds.types.Menu);
	xds.Registry.register(xds.types.MenuItem);
	xds.Registry.register(xds.types.GridPanel);
	xds.Registry.register(xds.types.EditorGridPanel);
	xds.Registry.register(xds.types.GridColumn);
	xds.Registry.register(xds.types.RowNumberColumn);
	xds.Registry.register(xds.types.BooleanColumn);
	xds.Registry.register(xds.types.CheckboxColumn);
	xds.Registry.register(xds.types.NumberColumn);
	xds.Registry.register(xds.types.DateColumn);
	xds.Registry.register(xds.types.TemplateColumn);
	xds.Registry.register(xds.types.JsonStore);
	xds.Registry.register(xds.types.JsonGroupStore);
	xds.Registry.register(xds.types.ArrayStore);
	xds.Registry.register(xds.types.XmlStore);
	xds.Registry.register(xds.types.Form);
	xds.Registry.register(xds.types.FieldSet);
	xds.Registry.register(xds.types.CheckboxGroup);
	xds.Registry.register(xds.types.Checkbox);
	xds.Registry.register(xds.types.TextField);
	xds.Registry.register(xds.types.Trigger);
	xds.Registry.register(xds.types.DateField);
	xds.Registry.register(xds.types.HtmlEditor);
	xds.Registry.register(xds.types.NumberField);
	xds.Registry.register(xds.types.RadioGroup);
	xds.Registry.register(xds.types.Radio);
	xds.Registry.register(xds.types.Hidden);
	xds.Registry.register(xds.types.TextArea);
	xds.Registry.register(xds.types.ComboBox);
	xds.Registry.register(xds.types.TimeField);
	xds.Registry.register(xds.types.DataField);
	xds.Registry.register(xds.types.TreePanel);
	xds.Registry.register(xds.types.TreeNode);
	xds.Registry.register(xds.types.DataView);
	xds.Registry.register(xds.types.FileField);
	xds.Registry.register(xds.types.BizcodeBox);
	xds.Registry.register(xds.types.BizcodeColumn);

	xds.Registry.register(xds.types.ChartColumn);
	xds.Registry.register(xds.types.ChartBar);
	xds.Registry.register(xds.types.ChartLine);
	xds.Registry.register(xds.types.ChartPie);
	xds.Registry.register(xds.types.ChartArea);
	xds.Registry.register(xds.types.ChartMapChina);
};
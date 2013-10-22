Ext.onReady(function () {
    var CSS = Ext.util.CSS;
    if (CSS) {
    	var rules = [];
        CSS.getRule('.x-hide-nosize') || (rules.push('.x-hide-nosize{height:0px!important;width:0px!important;border:none!important;zoom:1;}.x-hide-nosize * {height:0px!important;width:0px!important;border:none!important;zoom:1;}'));
	    CSS.getRule('.x-media', true) || (rules.push('.x-media{width:100%;height:100%;outline:none;overflow:hidden;}'));
	    CSS.getRule('.x-media-mask') || (rules.push('.x-media-mask{width:100%;height:100%;overflow:hidden;position:relative;zoom:1;}'));
	    CSS.getRule('.x-media-img') || (rules.push('.x-media-img{background-color:transparent;width:auto;height:auto;position:relative;}'));
	    CSS.getRule('.x-masked-relative') || (rules.push('.x-masked-relative{position:relative!important;}'));
	    
	    if ( !! rules.length) {
	        CSS.createStyleSheet(rules.join(''));
	        CSS.refreshCache();
	    }
    }
});

(function () {
    var El = Ext.Element;
    var A = Ext.lib.Anim;
    var supr = El.prototype;
    var VISIBILITY = "visibility",DISPLAY = "display",HIDDEN = "hidden",NONE = "none";
    var fx = {};
    fx.El = {
        setDisplayed: function (value) {
            var me = this;
            me.visibilityCls ? (me[value !== false ? 'removeClass' : 'addClass'](me.visibilityCls)) : supr.setDisplayed.call(me, value);
            return me;
        },
        isDisplayed: function () {
            return !(this.hasClass(this.visibilityCls) || this.isStyle(DISPLAY, NONE));
        },
        fixDisplay: function () {
            var me = this;
            supr.fixDisplay.call(me);
            me.visibilityCls && me.removeClass(me.visibilityCls);
        },
        isVisible: function (deep) {
            var vis = this.visible || (!this.isStyle(VISIBILITY, HIDDEN) && (this.visibilityCls ? !this.hasClass(this.visibilityCls) : !this.isStyle(DISPLAY, NONE)));
            if (deep !== true || !vis) {
                return vis;
            }
            var p = this.dom.parentNode,
                bodyRE = /^body/i;
            while (p && !bodyRE.test(p.tagName)) {
                if (!Ext.fly(p, '_isVisible').isVisible()) {
                    return false;
                }
                p = p.parentNode;
            }
            return true;
        },
        isStyle: function (style, val) {
            return this.getStyle(style) == val;
        }
    };
    
    Ext.override(El.Flyweight, fx.El);
    
    Ext.namespace('Ext.ux.plugin');
    
    Ext.ux.plugin.VisibilityMode = function (opt) {
        Ext.apply(this, opt || {});
        var CSS = Ext.util.CSS;
        if (CSS && !Ext.isIE && this.fixMaximizedWindow !== false && !Ext.ux.plugin.VisibilityMode.MaxWinFixed) {
            CSS.updateRule('.x-window-maximized-ct', 'overflow', '');
            Ext.ux.plugin.VisibilityMode.MaxWinFixed = true;
        }
    };
    
    Ext.extend(Ext.ux.plugin.VisibilityMode, Object, {
        bubble: true,
        fixMaximizedWindow: true,
        elements: null,
        visibilityCls: 'x-hide-nosize',
        hideMode: 'nosize',
        ptype: 'uxvismode',
        init: function (c) {
            var hideMode = this.hideMode || c.hideMode,
                plugin = this,
                bubble = Ext.Container.prototype.bubble,
                changeVis = function () {
                var els = [this.collapseEl, this.actionMode].concat(plugin.elements || []);
                Ext.each(els, function (el) {
                    plugin.extend(this[el] || el);
                }, this);
                var cfg = {
                    visFixed: true,
                    animCollapse: false,
                    animFloat: false,
                    hideMode: hideMode,
                    defaults: this.defaults || {}
                };
                cfg.defaults.hideMode = hideMode;
                Ext.apply(this, cfg);
                Ext.apply(this.initialConfig || {}, cfg);
            };
            
            c.on('render', function () {
                if (plugin.bubble !== false && this.ownerCt) {
                    bubble.call(this.ownerCt, function () {
                        this.visFixed || this.on('afterlayout', changeVis, this, {
                            single: true
                        });
                    });
                }
                changeVis.call(this);
            }, c, {single: true});
        },
        extend: function (el, visibilityCls) {
            el && Ext.each([].concat(el), function (e) {
                if (e && e.dom) {
                    if ('visibilityCls' in e) {
                    	return;
                    }
                    
                    Ext.apply(e, fx.El);
                    e.visibilityCls = visibilityCls || this.visibilityCls;
                }
            }, this);
            return this;
        }
    });
    Ext.preg && Ext.preg('uxvismode', Ext.ux.plugin.VisibilityMode);
})();



(function () {
    //remove null and undefined members from an object and optionally URL encode the results
    var compactObj = function (obj, encodeIt) {
        var out = obj && Ext.isObject(obj) ? {} : obj;
        if (out && Ext.isObject(out)) {
            for (var member in obj) {
                (obj[member] === null || obj[member] === undefined) || (out[member] = obj[member]);
            }
        }
        return encodeIt ? ((out && Ext.isObject(out)) ? Ext.urlEncode(out) : encodeURI(out)) : out;
    };
    var toString = Object.prototype.toString;
    
    Ext.ux.Media = function (config) {
        this.toString = this.asMarkup; 
        Ext.apply(this, config || {});
        this.initMedia();
    };
    var stateRE = /4$/i;
    if (parseFloat(Ext.version) < 2.2) {
        throw "Ext.ux.Media and sub-classes are not License-Compatible with your Ext release.";
    }
    
    Ext.ux.Media.prototype = {
        hasVisModeFix: !! Ext.ux.plugin.VisibilityMode,
        mediaObject: null,
        mediaCfg: null,
        mediaVersion: null,
        requiredVersion: null,
        hideMode: 'display',
        unsupportedText: null,
        animCollapse: Ext.enableFx && Ext.isIE,
        animFloat: Ext.enableFx && Ext.isIE,
        autoScroll: true,
        bodyStyle: {
            position: 'relative'
        },
        initMedia: function () {
            this.hasVisModeFix = !! Ext.ux.plugin.VisibilityMode;
        },
        disableCaching: false,
        _maxPoll: 200,
        getMediaType: function (type) {
            return Ext.ux.Media.mediaTypes[type];
        },
        assert: function (v, def) {
            v = typeof v === 'function' ? v.call(v.scope || null) : v;
            return Ext.value(v, def);
        },
        assertId: function (id, def) {
            id || (id = def || Ext.id());
            return id;
        },
        prepareURL: function (url, disableCaching) {
            var parts = url ? url.split('#') : [''];
            if ( !! url && (disableCaching = disableCaching === undefined ? this.disableCaching : disableCaching)) {
                var u = parts[0];
                if (!(/_dc=/i).test(u)) {
                    var append = "_dc=" + (new Date().getTime());
                    if (u.indexOf("?") !== - 1) {
                        u += "&" + append;
                    } else {
                        u += "?" + append;
                    }
                    parts[0] = u;
                }
            }
            return parts.length > 1 ? parts.join('#') : parts[0];
        },
        prepareMedia: function (mediaCfg, width, height, ct) {
            mediaCfg = mediaCfg || this.mediaCfg;
            if (!mediaCfg) {
                return '';
            }
            var m = Ext.apply({
                url: false,
                autoSize: false
            }, mediaCfg); //make a copy
            m.url = this.prepareURL(this.assert(m.url, false), m.disableCaching);
            if (m.mediaType) {
                var value, tag, p, El = Ext.Element.prototype;
                var media = Ext.apply({}, this.getMediaType(this.assert(m.mediaType, false)) || false);
                var params = compactObj(Ext.apply(media.params || {}, m.params || {}));
                for (var key in params) {
                    if (params.hasOwnProperty(key)) {
                        m.children || (m.children = []);
                        p = this.assert(params[key], null);
                        p && (p = compactObj(p, m.encodeParams !== false));
                        tag = {
                            tag: 'param',
                            name: key,
                            value: p
                        };
                        (tag.value == key) && delete tag.value;
                        p && m.children.push(tag);
                    }
                }
                delete media.params;
                //childNode Text if plugin/object is not installed.
                var unsup = this.assert(m.unsupportedText || this.unsupportedText || media.unsupportedText, null);
                if (unsup) {
                    m.children || (m.children = []);
                    m.children.push(unsup);
                }
                if (m.style && typeof m.style != "object") {
                    throw 'Style must be JSON formatted';
                }
                m.style = this.assert(Ext.apply(media.style || {}, m.style || {}), {});
                delete media.style;
                m.height = this.assert(height || m.height || media.height || m.style.height, null);
                m.width = this.assert(width || m.width || media.width || m.style.width, null);
                m = Ext.apply({
                    tag: 'object'
                }, m, media);
                //Convert element height and width to inline style to avoid issues with display:none;
                if (m.height || m.autoSize) {
                    Ext.apply(m.style, {
                        //Ext 2 & 3 compatibility -- Use the defaultUnit from the Component's el for default
                        height: (Ext.Element.addUnits || El.addUnits).call(this.mediaEl, m.autoSize ? '100%' : m.height, El.defaultUnit || 'px')
                    });
                }
                if (m.width || m.autoSize) {
                    Ext.apply(m.style, {
                        //Ext 2 & 3 compatibility -- Use the defaultUnit from the Component's el for default
                        width: (Ext.Element.addUnits || El.addUnits).call(this.mediaEl, m.autoSize ? '100%' : m.width, El.defaultUnit || 'px')
                    });
                }
                m.id = this.assertId(m.id);
                m.name = this.assertId(m.name, m.id);
                m._macros = {
                    url: m.url || '',
                    height: (/%$/.test(m.height)) ? m.height : parseInt(m.height, 10) || null,
                    width: (/%$/.test(m.width)) ? m.width : parseInt(m.width, 10) || null,
                    scripting: this.assert(m.scripting, false),
                    controls: this.assert(m.controls, false),
                    scale: this.assert(m.scale, 1),
                    status: this.assert(m.status, false),
                    start: this.assert(m.start, false),
                    loop: this.assert(m.loop, false),
                    volume: this.assert(m.volume, 20),
                    id: m.id
                };
                delete m.url;
                delete m.mediaType;
                delete m.controls;
                delete m.status;
                delete m.start;
                delete m.loop;
                delete m.scale;
                delete m.scripting;
                delete m.volume;
                delete m.autoSize;
                delete m.autoScale;
                delete m.params;
                delete m.unsupportedText;
                delete m.renderOnResize;
                delete m.disableCaching;
                delete m.listeners;
                delete m.height;
                delete m.width;
                delete m.encodeParams;
                return m;
            } else {
                var unsup = this.assert(m.unsupportedText || this.unsupportedText || media.unsupportedText, null);
                unsup = unsup ? Ext.DomHelper.markup(unsup) : null;
                return String.format(unsup || 'Media Configuration/Plugin Error', ' ', ' ');
            }
        },
        asMarkup: function (mediaCfg) {
            return this.mediaMarkup(this.prepareMedia(mediaCfg));
        },
        mediaMarkup: function (mediaCfg) {
            mediaCfg = mediaCfg || this.mediaCfg;
            if (mediaCfg) {
                var _macros = mediaCfg._macros;
                delete mediaCfg._macros;
                var m = Ext.DomHelper.markup(mediaCfg);
                if (_macros) {
                    var _m, n;
                    for (n in _macros) {
                        _m = _macros[n];
                        if (_m !== null) {
                            m = m.replace(new RegExp('((%40|@)' + n + ')', 'g'), _m + '');
                        }
                    }
                }
                return m;
            }
        },
        setMask: function (el) {
            var mm;
            if ((mm = this.mediaMask)) {
                mm.el || (mm = this.mediaMask = new Ext.ux.IntelliMask(el, Ext.isObject(mm) ? mm : {
                    msg: mm
                }));
                mm.el.addClass('x-media-mask');
            }
        },
        refreshMedia: function (target) {
            if (this.mediaCfg) {
                this.renderMedia(null, target);
            }
            
            this.on('chartload',function(){
            	if(this.dataXML){
            		this.setChartData(this.dataXML);
            	}
            },this,{single:true});
            
            return this;
        },
        renderMedia: function (mediaCfg, ct, domPosition, w, h) {
            if (!Ext.isReady) {
                Ext.onReady(this.renderMedia.createDelegate(this, Array.prototype.slice.call(arguments, 0)));
                return;
            }
            var mc = (this.mediaCfg = mediaCfg || this.mediaCfg);
            ct = Ext.get(this.lastCt || ct || (this.mediaObject ? this.mediaObject.dom.parentNode : null));
            this.onBeforeMedia.call(this, mc, ct, domPosition, w, h);
            if (ct) {
                this.lastCt = ct;
                if (mc && (mc = this.prepareMedia(mc, w, h, ct))) {
                    this.setMask(ct);
                    this.mediaMask && this.autoMask && this.mediaMask.show();
                    this.clearMedia().writeMedia(mc, ct, domPosition || 'afterbegin');
                }
            }
            this.onAfterMedia(ct);
        },
        writeMedia: function (mediaCfg, container, domPosition) {
            var ct = Ext.get(container);
            if (ct) {
                var markup = this.mediaMarkup(mediaCfg);
                domPosition ? Ext.DomHelper.insertHtml(domPosition, ct.dom, markup) : ct.update(markup);
            }
        },
        clearMedia: function () {
            var mo;
            if (Ext.isReady && (mo = this.mediaObject)) {
                mo.remove(true, true);
            }
            this.mediaObject = null;
            return this;
        },
        resizeMedia: function (comp, aw, ah, w, h) {
            var mc = this.mediaCfg;
            if (mc && this.rendered && mc.renderOnResize && ( !! aw || !! ah)) {
                // Ext.Window.resizer fires this event a second time
                if (arguments.length > 3 && (!this.mediaObject || mc.renderOnResize)) {
                    this.refreshMedia(this[this.mediaEl]);
                }
            }
        },
        onBeforeMedia: function (mediaCfg, ct, domPosition, width, height) {
            var m = mediaCfg || this.mediaCfg,
                mt;
            if (m && (mt = this.getMediaType(m.mediaType))) {
                m.autoSize = m.autoSize || mt.autoSize === true;
                var autoSizeEl;
                //Calculate parent container size for macros (if available)
                if (m.autoSize && (autoSizeEl = Ext.isReady ?
                //Are we in a layout ? autoSize to the container el.
                Ext.get(this[this.mediaEl] || this.lastCt || ct) : null)) {
                    m.height = this.autoHeight ? null : autoSizeEl.getHeight(true);
                    m.width = this.autoWidth ? null : autoSizeEl.getWidth(true);
                }
            }
            this.assert(m.height, height);
            this.assert(m.width, width);
            mediaCfg = m;
        },
        onMediaLoad: function (e) {
            if (e && e.type == 'load') {
                this.fireEvent('mediaload', this, this.mediaObject);
                this.mediaMask && this.autoMask && this.mediaMask.hide();
            }
        },
        onAfterMedia: function (ct) {
            var mo;
            if (this.mediaCfg && ct && (mo = new(this.elementClass || Ext.ux.Media.Element)(ct.child('.x-media', true), true)) && mo.dom) {
                //Update ElCache with the new Instance
                this.mediaObject = mo;
                mo.ownerCt = this;
                var L; //Reattach any DOM Listeners after rendering.
                if (L = this.mediaCfg.listeners || null) {
                    mo.on(L); //set any DOM listeners
                }
                this.fireEvent('mediarender', this, this.mediaObject);
                //Load detection for non-<object> media (iframe, img)
                if (mo.dom.tagName !== 'OBJECT') {
                    mo.on({
                        load: this.onMediaLoad,
                        scope: this,
                        single: true
                    });
                } else {
                    //IE, Opera possibly others, support a readyState on <object>s
                    this._countPoll = 0;
                    this.pollReadyState(this.onMediaLoad.createDelegate(this, [{
                        type: 'load'
                    }], 0));
                }
            }(this.autoWidth || this.autoHeight) && this.syncSize();
        },
        pollReadyState: function (cb, readyRE) {
            var media = this.getInterface();
            if (media && 'readyState' in media) {
                (readyRE || stateRE).test(media.readyState) ? cb() : arguments.callee.defer(10, this, arguments);
            }
        },
        getInterface: function () {
            return this.mediaObject ? this.mediaObject.dom || null : null;
        },
        detectVersion: Ext.emptyFn,
        autoMask: false
    };
    
    
    Ext.ux.Media.Component = Ext.extend(Ext.BoxComponent, {
        ctype: "Ext.ux.Media.Component",
        mediaEl: 'el',
        autoScroll: true,
        autoEl: {
            tag: 'div',
            style: {
                overflow: 'hidden',
                display: 'block',
                position: 'relative'
            }
        },
        cls: "x-media-comp",
        mediaClass: Ext.ux.Media,
        constructor: function (config) {
            Ext.apply(this, config, this.mediaClass.prototype);
            Ext.ux.Media.Component.superclass.constructor.apply(this, arguments);
        },
        initComponent: function () {
        	Ext.ux.Media.Component.superclass.initComponent.apply(this, arguments);
            this.getId = function () {
                return this.id || (this.id = "media-comp" + (++Ext.Component.AUTO_ID));
            };
            this.html = this.contentEl = this.items = null;
            this.initMedia();
            //Attach the Visibility Fix (if available) to the current instance
            if (this.hideMode == 'nosize' && this.hasVisModeFix) {
                new Ext.ux.plugin.VisibilityMode({
                    elements: ['bwrap', 'mediaEl'],
                    hideMode: 'nosize'
                }).init(this);
            }
            //Inline rendering support for this and all subclasses
            this.toString = this.asMarkup;
            this.addEvents('mediarender', 'mediaload');
        },
        afterRender: function (ct) {
        	Ext.ux.Media.Component.superclass.afterRender.apply(this, arguments);
        	
            this.setMask(this[this.mediaEl] || ct);
            this.setAutoScroll();
            this.renderMedia(this.mediaCfg, this[this.mediaEl]);
        },
        beforeDestroy: function () {
            this.clearMedia();
            Ext.destroy(this.mediaMask, this.loadMask);
            this.lastCt = this.mediaObject = this.renderTo = this.applyTo = this.mediaMask = this.loadMask = null;
            
            this.rendered && Ext.ux.Media.Component.superclass.beforeDestroy.apply(this, arguments);
        },
        doAutoLoad: Ext.emptyFn,
        getContentTarget: function () {
            return this[this.mediaEl];
        },
        //Ext 2.x does not have Box setAutoscroll
        setAutoScroll:function () {
            if (this.rendered) {
                this.getContentTarget().setOverflow( !! this.autoScroll ? 'auto' : 'hidden');
            }
        },
        onResize: function () {
        	Ext.ux.Media.Component.superclass.onResize.apply(this, arguments);
            if (this.mediaObject && this.mediaCfg.renderOnResize) {
                this.refreshMedia();
            }
        }
    });
    Ext.reg('uxmedia', Ext.ux.Media.Component);
    Ext.reg('media', Ext.ux.Media.Component);

    
    Ext.ux.Media.Element = Ext.extend(Ext.Element, {
        constructor: function (element) {
            Ext.ux.Media.Element.superclass.constructor.apply(this, arguments);
            if (Ext.elCache) { //Ext 3.1 compat
                Ext.elCache[this.id] || (Ext.elCache[this.id] = {
                    events: {},
                    data: {}
                });
                Ext.elCache[this.id].el = this;
            } else {
                Ext.Element.cache[this.id] = this;
            }
        },
        mask: function (msg, msgCls) {
            this.maskEl || (this.maskEl = this.parent('.x-media-mask') || this.parent());
            return this.maskEl.mask.apply(this.maskEl, arguments);
        },
        unmask: function (remove) {
            if (this.maskEl) {
                this.maskEl.unmask(remove);
                this.maskEl = null;
            }
        },
        remove: function (cleanse, deep) {
            if (this.dom) {
                this.unmask(true);
                this.removeAllListeners(); //remove any Ext-defined DOM listeners
                Ext.ux.Media.Element.superclass.remove.apply(this, arguments);
                this.dom = null; //clear ANY DOM references
            }
        }
    });
    Ext.ux.Media.prototype.elementClass = Ext.ux.Media.Element;
    
    Ext.ux.IntelliMask = function (el, config) {
        Ext.apply(this, config || {
            msg: this.msg
        });
        this.el = Ext.get(el);
    };
    Ext.ux.IntelliMask.prototype = {
        removeMask: false,
        msg: 'Loading Media...',
        msgCls: 'x-mask-loading',
        zIndex: null,
        disabled: false,
        active: false,
        autoHide: false,
        disable: function () {
            this.disabled = true;
        },
        enable: function () {
            this.disabled = false;
        },
        show: function (msg, msgCls, fn, fnDelay) {
            var opt = {},
                autoHide = this.autoHide;
            fnDelay = parseInt(fnDelay, 10) || 20; //ms delay to allow mask to quiesce if fn specified
            if (Ext.isObject(msg)) {
                opt = msg;
                msg = opt.msg;
                msgCls = opt.msgCls;
                fn = opt.fn;
                autoHide = typeof opt.autoHide != 'undefined' ? opt.autoHide : autoHide;
                fnDelay = opt.fnDelay || fnDelay;
            }
            if (!this.active && !this.disabled && this.el) {
                var mask = this.el.mask(msg || this.msg, msgCls || this.msgCls);
                this.active = !! this.el._mask;
                if (this.active) {
                    if (this.zIndex) {
                        this.el._mask.setStyle("z-index", this.zIndex);
                        if (this.el._maskMsg) {
                            this.el._maskMsg.setStyle("z-index", this.zIndex + 1);
                        }
                    }
                }
            } else {
                fnDelay = 0;
            }
            //passed function is called regardless of the mask state.
            if (typeof fn === 'function') {
                fn.defer(fnDelay, opt.scope || null);
            } else {
                fnDelay = 0;
            }
            if (autoHide && (autoHide = parseInt(autoHide, 10) || 2000)) {
                this.hide.defer(autoHide + (fnDelay || 0), this);
            }
            return this.active ? {
                mask: this.el._mask,
                maskMsg: this.el._maskMsg
            } : null;
        },
        hide: function (remove) {
            if (this.el) {
                this.el.unmask(remove || this.removeMask);
            }
            this.active = false;
            return this;
        },
        // private
        destroy: function () {
            this.hide(true);
            this.el = null;
        }
    };
    Ext.ux.Media.mediaTypes = {
        SWF: Ext.apply({
            tag: 'object',
            cls: 'x-media x-media-swf',
            type: 'application/x-shockwave-flash',
            scripting: 'sameDomain',
            standby: 'Loading..',
            loop: true,
            start: false,
            unsupportedText: {
                cn: ['The Adobe Flash Player is required.', {
                    tag: 'br'
                },{
                    tag: 'a',
                    href: 'http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash',
                    target: '_flash'
                }]
            },
            params: {
                movie: "@url",
                menu: "@controls",
                play: "@start",
                quality: "high",
                allowscriptaccess: "@scripting",
                allownetworking: 'all',
                allowfullScreen: false,
                bgcolor: "#FFFFFF",
                wmode: "opaque",
                loop: "@loop"
            }
        }, Ext.isIE ? {
            classid: "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",
            codebase: "http" + ((Ext.isSecure) ? 's' : '') + "://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,0,0"
        } : {
            data: "@url"
        })
    };
    Ext.applyIf(Array.prototype, {
        map: function (fun, scope) {
            var len = this.length;
            if (typeof fun != "function") {
                throw new TypeError();
            }
            var res = new Array(len);
            for (var i = 0; i < len; i++) {
                if (i in this) {
                    res[i] = fun.call(scope || this, this[i], i, this);
                }
            }
            return res;
        }
    });
    Ext.ux.MediaComponent = Ext.ux.Media.Component;
})();


(function () {
    Ext.ux.Media.Flash = Ext.extend(Ext.ux.Media, {
        varsName: 'flashVars',
        externalsNamespace: null,
        mediaType: Ext.apply({
	            tag: 'object',
	            cls: 'x-media x-media-swf',
	            type: 'application/x-shockwave-flash',
	            loop: null,
	            style: {
	                'z-index': 0
	            },
	            scripting: "sameDomain",
	            start: true,
	            unsupportedText: {
	                cn: ['The Adobe Flash Player{0}is required.', {
	                    tag: 'br'
	                },{
	                    tag: 'a',
	                    href: 'http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash',
	                    target: '_flash'
	                }]
	            },
	            params: {
	                movie: "@url",
	                play: "@start",
	                loop: "@loop",
	                menu: "@controls",
	                quality: "high",
	                bgcolor: "#FFFFFF",
	                wmode: "opaque",
	                allowscriptaccess: "@scripting",
	                allowfullscreen: false,
	                allownetworking: 'all'
	            }
        	}, Ext.isIE ? {
            	classid: "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",
            	codebase: "http" + ((Ext.isSecure) ? 's' : '') + "://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0"
        	} : {
        		data: "@url"
        	}
        ),
        getMediaType: function () {
            return this.mediaType;
        },
        assertId: function (id, def) {
            id || (id = def || Ext.id());
            return id.replace(/\+|-|\\|\/|\*/g, '');
        },
        initMedia: function () {
        	Ext.ux.Media.Flash.superclass.initMedia.call(this);
        	
            var mc = Ext.apply({}, this.mediaCfg || {});
            var requiredVersion = (this.requiredVersion = mc.requiredVersion || this.requiredVersion || false);
            var hasFlash = !! (this.playerVersion = this.detectFlashVersion());
            var hasRequired = hasFlash && (requiredVersion ? this.assertVersion(requiredVersion) : true);
            var unsupportedText = this.assert(mc.unsupportedText || this.unsupportedText || (this.getMediaType() || {}).unsupportedText, null);
            if (unsupportedText) {
                unsupportedText = Ext.DomHelper.markup(unsupportedText);
                unsupportedText = mc.unsupportedText = String.format(unsupportedText, (requiredVersion ? ' ' + requiredVersion + ' ' : ' '), (this.playerVersion ? ' ' + this.playerVersion + ' ' : ' Not installed.'));
            }
            mc.mediaType = "SWF";
            if (!hasRequired) {
                this.autoMask = false;
                //Version check for the Flash Player that has the ability to start Player Product Install (6.0r65)
                var canInstall = hasFlash && this.assertVersion('6.0.65');
                if (canInstall && mc.installUrl) {
                    mc = mc.installDescriptor || {
                        mediaType: 'SWF',
                        tag: 'object',
                        cls: 'x-media x-media-swf x-media-swfinstaller',
                        id: 'SWFInstaller',
                        type: 'application/x-shockwave-flash',
                        data: "@url",
                        url: this.prepareURL(mc.installUrl)
                        //The dimensions of playerProductInstall.swf must be at least 310 x 138 pixels,
                        ,
                        width: (/%$/.test(mc.width)) ? mc.width : ((parseInt(mc.width, 10) || 0) < 310 ? 310 : mc.width),
                        height: (/%$/.test(mc.height)) ? mc.height : ((parseInt(mc.height, 10) || 0) < 138 ? 138 : mc.height),
                        loop: false,
                        start: true,
                        unsupportedText: unsupportedText,
                        params: {
                            quality: "high",
                            movie: '@url',
                            allowscriptacess: "always",
                            wmode: "opaque",
                            align: "middle",
                            bgcolor: "#3A6EA5",
                            pluginspage: mc.pluginsPage || this.pluginsPage || "http://www.adobe.com/go/getflashplayer"
                        }
                    };
                    mc.params[this.varsName] = "MMredirectURL=" + (mc.installRedirect || window.location) + "&MMplayerType=" + (Ext.isIE ? "ActiveX" : "Plugin") + "&MMdoctitle=" + (document.title = document.title.slice(0, 47) + " - Flash Player Installation");
                } else {
                    //Let superclass handle with unsupportedText property
                    mc.mediaType = null;
                }
            }
            if (mc.eventSynch) {
                mc.params || (mc.params = {});
                var vars = mc.params[this.varsName] || (mc.params[this.varsName] = {});
                if (typeof vars === 'string') {
                    vars = Ext.urlDecode(vars, true);
                }
                var eventVars = (mc.eventSynch === true ? {
                    allowedDomain: vars.allowedDomain || document.location.hostname,
                    elementID: mc.id || (mc.id = Ext.id()),
                    eventHandler: 'Ext.ux.Media.Flash.eventSynch'
                } : mc.eventSynch);
                Ext.apply(mc.params, {
                    allowscriptaccess: 'always'
                })[this.varsName] = Ext.applyIf(vars, eventVars);
            }
            this.bindExternals(mc.boundExternals);
            delete mc.requiredVersion;
            delete mc.installUrl;
            delete mc.installRedirect;
            delete mc.installDescriptor;
            delete mc.eventSynch;
            delete mc.boundExternals;
            this.mediaCfg = mc;
        },
        assertVersion: function (versionMap) {
            var compare;
            versionMap || (versionMap = []);
            if (Ext.isArray(versionMap)) {
                compare = versionMap;
            } else {
                compare = String(versionMap).split('.');
            }
            compare = (compare.concat([0, 0, 0, 0])).slice(0, 3); //normalize
            var tpv;
            if (!(tpv = this.playerVersion || (this.playerVersion = this.detectFlashVersion()))) {
                return false;
            }
            if (tpv.major > parseFloat(compare[0])) {
                return true;
            } else if (tpv.major == parseFloat(compare[0])) {
                if (tpv.minor > parseFloat(compare[1])) {
                    return true;
                }
                else if (tpv.minor == parseFloat(compare[1])) {
                    if (tpv.rev >= parseFloat(compare[2])) {
                        return true;
                    }
                }
            }
            return false;
        },
        detectFlashVersion: function () {
            if (Ext.ux.Media.Flash.prototype.flashVersion) {
                return this.playerVersion = Ext.ux.Media.Flash.prototype.flashVersion;
            }
            var version = false;
            var formatVersion = function (version) {
                return version && !! version.length ? {
                    major: version[0] !== null ? parseInt(version[0], 10) : 0,
                    minor: version[1] !== null ? parseInt(version[1], 10) : 0,
                    rev: version[2] !== null ? parseInt(version[2], 10) : 0,
                    toString: function () {
                        return this.major + '.' + this.minor + '.' + this.rev;
                    }
                } : false;
            };
            var sfo = null;
            if (Ext.isIE) {
                try {
                    sfo = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.7");
                } catch (e) {
                    try {
                        sfo = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.6");
                        version = [6, 0, 21];
                        // error if player version < 6.0.47 (thanks to Michael Williams @ Adobe for this solution)
                        sfo.allowscriptaccess = "always";
                    } catch (ex) {
                        if (version && version[0] === 6) {
                            return formatVersion(version);
                        }
                    }
                    try {
                        sfo = new ActiveXObject("ShockwaveFlash.ShockwaveFlash");
                    } catch (ex1) {}
                }
                if (sfo) {
                    version = sfo.GetVariable("$version").split(" ")[1].split(",");
                }
            } else if (navigator.plugins && navigator.mimeTypes.length) {
                sfo = navigator.plugins["Shockwave Flash"];
                if (sfo && sfo.description) {
                    version = sfo.description.replace(/([a-zA-Z]|\s)+/, "").replace(/(\s+r|\s+b[0-9]+)/, ".").split(".");
                }
            }
            return (this.playerVersion = Ext.ux.Media.Flash.prototype.flashVersion = formatVersion(version));
        },
        onAfterMedia: function (ct) {
        	Ext.ux.Media.Flash.superclass.onAfterMedia.apply(this, arguments);
            var mo;
            if (mo = this.mediaObject) {
                var id = mo.id;
                if (Ext.isIE) {
                    //fscommand bindings
                    //implement a fsCommand event interface since its not supported on IE when writing innerHTML
                    if (!(Ext.query('script[for=' + id + ']').length)) {
                        writeScript('var c;if(c=Ext.getCmp("' + this.id + '")){c.onfsCommand.apply(c,arguments);}', {
                            event: "FSCommand",
                            htmlFor: id
                        });
                    }
                } else {
                    window[id + '_DoFSCommand'] || (window[id + '_DoFSCommand'] = this.onfsCommand.createDelegate(this));
                }
            }
        },
        clearMedia: function () {
            //de-register fscommand hooks
            if (this.mediaObject) {
                var id = this.mediaObject.id;
                if (Ext.isIE) {
                    Ext.select('script[for=' + id + ']', true).remove();
                } else {
                    window[id + '_DoFSCommand'] = null;
                    delete window[id + '_DoFSCommand'];
                }
            }
            return Ext.ux.Media.Flash.superclass.clearMedia.call(this) || this;
        },
        getSWFObject: function () {
            return this.getInterface();
        },
        onfsCommand: function (command, args) {
            if (this.events) {
                this.fireEvent('fscommand', this, command, args);
            }
        },
        setVariable: function (varName, value) {
            var fo = this.getInterface();
            if (fo && 'SetVariable' in fo) {
                fo.SetVariable(varName, value);
                return true;
            }
            fo = null;
            return false;
        },
        getVariable: function (varName) {
            var fo = this.getInterface();
            if (fo && 'GetVariable' in fo) {
                return fo.GetVariable(varName);
            }
            fo = null;
            return undefined;
        },
        bindExternals: function (methods) {
            if (methods && this.playerVersion.major >= 8) {
                methods = new Array().concat(methods);
            } else {
                return;
            }
            var nameSpace = (typeof this.externalsNamespace == 'string' ? this[this.externalsNamespace] || (this[this.externalsNamespace] = {}) : this);
            Ext.each(methods, function (method) {
                var m = method.name || method;
                var returnType = method.returnType || 'javascript';
                //Do not overwrite existing function with the same name.
                nameSpace[m] || (nameSpace[m] = function () {
                    return this.invoke.apply(this, [m, returnType].concat(Array.prototype.slice.call(arguments, 0)));
                }.createDelegate(this));
            }, this);
        },
        invoke: function (method, returnType) {
            var obj, r;
            if (method && (obj = this.getInterface()) && 'CallFunction' in obj) {
                var c = [
                String.format('<invoke name="{0}" returntype="{1}">', method, returnType), '<arguments>', (Array.prototype.slice.call(arguments, 2)).map(this._toXML, this).join(''), '</arguments>', '</invoke>'].join('');
                r = obj.CallFunction(c);
                typeof r === 'string' && returnType === 'javascript' && (r = Ext.decode(r));
            }
            return r;
        },
        onFlashInit: function () {
            if (this.mediaMask && this.autoMask) {
                this.mediaMask.hide();
            }
            this.fireEvent.defer(300, this, ['flashinit', this, this.getInterface()]);
        },
        pollReadyState: function (cb, readyRE) {
            var media;
            if (media = this.getInterface()) {
                if (typeof media.PercentLoaded != 'undefined') {
                    var perc = media.PercentLoaded();
                    this.fireEvent('progress', this, this.getInterface(), perc);
                    if (perc = 100) {
                        cb();
                        return;
                    }
                }
                this._countPoll++ > this._maxPoll || arguments.callee.defer(10, this, arguments);
            }
        },
        _handleSWFEvent: function (event) {
            var type = event.type || event || false;
            if (type) {
                if (this.events && !this.events[String(type)]) {
                    this.addEvents(String(type));
                }
                return this.fireEvent.apply(this, [String(type), this].concat(Array.prototype.slice.call(arguments, 0)));
            }
        },
        _toXML: function (value) {
            var format = Ext.util.Format;
            var type = typeof value;
            if (type == "string") {
                return "<string>" + format.xmlEncode(value) + "</string>";
            }
            else if (type == "undefined") {
                return "<undefined/>";
            }
            else if (type == "number") {
                return "<number>" + value + "</number>";
            }
            else if (value == null) {
                return "<null/>";
            }
            else if (type == "boolean") {
                return value ? "<true/>" : "<false/>";
            }
            else if (value instanceof Date) {
                return "<date>" + value.getTime() + "</date>";
            }
            else if (Ext.isArray(value)) {
                return this._arrayToXML(value);
            }
            else if (type == "object") {
                return this._objectToXML(value);
            }
            else {
                return "<null/>";
            }
        },
        _arrayToXML: function (arrObj) {
            var s = "<array>";
            for (var i = 0, l = arrObj.length; i < l; i++) {
                s += "<property id=\"" + i + "\">" + this._toXML(arrObj[i]) + "</property>";
            }
            return s + "</array>";
        },
        _objectToXML: function (obj) {
            var s = "<object>";
            for (var prop in obj) {
                if (obj.hasOwnProperty(prop)) {
                    s += "<property id=\"" + prop + "\">" + this._toXML(obj[prop]) + "</property>";
                }
            }
            return s + "</object>";
        }
    });
    Ext.ux.Media.Flash.eventSynch = function (elementID, event) {
        var SWF = Ext.get(elementID),
            inst;
        if (SWF && (inst = SWF.ownerCt)) {
            return inst._handleSWFEvent.apply(inst, Array.prototype.slice.call(arguments, 1));
        }
    };
    Ext.ux.Media.Flash.Component = Ext.extend(Ext.ux.Media.Component, {
        ctype: "Ext.ux.Media.Flash.Component",
        cls: "x-media-flash-comp",
        autoEl: {
            tag: 'div',
            style: {
                overflow: 'hidden',
                display: 'block'
            }
        },
        mediaClass: Ext.ux.Media.Flash,
        initComponent: function () {
            this.getId = function () {
                return this.id || (this.id = "flash-comp" + (++Ext.Component.AUTO_ID));
            };
            this.addEvents('flashinit', 'fscommand', 'progress');
            Ext.ux.Media.Flash.Component.superclass.initComponent.apply(this, arguments);
        }
    });
    Ext.reg('uxflash', Ext.ux.Media.Flash.Component);
    Ext.ux.Media.Flash.prototype.detectFlashVersion();

    Ext.ux.Media.Flash.Element = Ext.extend(Ext.ux.Media.Element, {
        remove: function () {
            if (Ext.isIE && Ext.isWindows && (this.dom)) {
                this.removeAllListeners();
                this.dom.style.display = 'none'; //hide it regardless of state
                if (this.dom.readyState == 4) {
                    for (var x in this.dom) {
                        if (x.toLowerCase() != 'flashvars' && typeof this.dom[x] == 'function') {
                            this.dom[x] = null;
                        }
                    }
                }
            }
            Ext.ux.Media.Flash.Element.superclass.remove.apply(this, arguments);
        }
    });
    Ext.ux.Media.Flash.prototype.elementClass = Ext.ux.Media.Flash.Element;
    
    var writeScript = function (block, attributes) {
        attributes = Ext.apply({}, attributes || {}, {
            type: "text/javascript",
            text: block
        });
        try {
            var head, script, doc = document;
            if (doc && doc.getElementsByTagName) {
                if (!(head = doc.getElementsByTagName("head")[0])) {
                    head = doc.createElement("head");
                    doc.getElementsByTagName("html")[0].appendChild(head);
                }
                if (head && (script = doc.createElement("script"))) {
                    for (var attrib in attributes) {
                        if (attributes.hasOwnProperty(attrib) && attrib in script) {
                            script[attrib] = attributes[attrib];
                        }
                    }
                    return !!head.appendChild(script);
                }
            }
        } catch (ex) {}
        return false;
    };
    if (Ext.isIE && Ext.isWindows && Ext.ux.Media.Flash.prototype.flashVersion.major == 9) {
        window.attachEvent('onbeforeunload', function () {
            __flash_unloadHandler = __flash_savedUnloadHandler = function () {};
        });
        //Note: we cannot use IE's onbeforeunload event because an internal Flash Form-POST
        // raises the browsers onbeforeunload event when the server returns a response.  that is crazy!
        window.attachEvent('onunload', function () {
            Ext.each(Ext.query('.x-media-swf'), function (item, index) {
                item.style.display = 'none';
                for (var x in item) {
                    if (x.toLowerCase() != 'flashvars' && typeof item[x] == 'function') {
                        item[x] = null;
                    }
                }
            });
        });
    }
    Ext.apply(Ext.util.Format, {
        xmlEncode: function (value) {
            return !value ? value : String(value).replace(/&/g, "&amp;").replace(/>/g, "&gt;").replace(/</g, "&lt;").replace(/"/g, "&quot;").replace(/'/g, "&apos;");
        },
        xmlDecode: function (value) {
            return !value ? value : String(value).replace(/&gt;/g, ">").replace(/&lt;/g, "<").replace(/&quot;/g, '"').replace(/&amp;/g, "&").replace(/&apos;/g, "'");
        }
    });
    Ext.ux.FlashComponent = Ext.ux.Media.Flash.Component;
})();
(function () {
    Ext.namespace("Ext.ux.Chart");
    var chart = Ext.ux.Chart;
    var flash = Ext.ux.Media.Flash;
    Ext.ux.Chart.FlashAdapter = Ext.extend(Ext.ux.Media.Flash, {
        requiredVersion: 8,
        unsupportedText: {
            cn: ['The Adobe Flash Player{0}is required.', {
                tag: 'br'
            },{
                tag: 'a',
                href: 'http://www.adobe.com/shockwave/download/download.cgi?P1_Prod_Version=ShockwaveFlash',
                target: '_flash'
            }]
        },
        chartURL: null,
        chartData: null,
        dataURL: null,
        autoLoad: null,
        loadMask: null,
        mediaMask: null,
        autoMask: null,
        disableCaching: true,
        blankChartData: '',
        externalsNamespace: 'chart',
        chartCfg: null,
        chart: null,
        mediaCfg: {
            url: null,
            id: null,
            start: true,
            controls: false,
            height: null,
            width: null,
            autoSize: true,
            renderOnResize: false,
            scripting: 'always',
            cls: 'x-media x-media-swf x-chart',
            params: {
                allowscriptaccess: '@scripting',
                wmode: 'opaque',
                scale: 'exactfit',
                scale: null,
                salign: null
            }
        },
        initMedia: function () {
            this.addEvents('beforeload', 'loadexception', 'chartload', 'chartrender');
            this.mediaCfg.renderOnResize = this.mediaCfg.renderOnResize || (this.chartCfg || {}).renderOnResize;
            chart.FlashAdapter.superclass.initMedia.call(this);
            
            if (this.autoLoad) {
                this.on('chartload', this.doAutoLoad, this, {
                    single: true
                });
            }
        },
        onBeforeMedia: function () {
            var mc = this.mediaCfg;
            var mp = mc.params || {};
            delete mc.params;
            var mv = mp[this.varsName] || {};
            delete mp[this.varsName];
            //chartCfg
            var cCfg = Ext.apply({}, this.chartCfg || {});
            //chart params
            var cp = Ext.apply({}, this.assert(cCfg.params, {}));
            delete cCfg.params;
            //chart.params.flashVars
            var cv = Ext.apply({}, this.assert(cp[this.varsName], {}));
            delete cp[this.varsName];
            Ext.apply(mc, cCfg, {
                url: this.assert(this.chartURL, null)
            });
            mc.params = Ext.apply(mp, cp);
            mc.params[this.varsName] = Ext.apply(mv, cv);
            chart.FlashAdapter.superclass.onBeforeMedia.call(this);
        },
        setChartDataURL: function (url, immediate) {
            return this;
        },
        load: function (url, params, callback, scope) {
            if (!url) {
                return null;
            }
            
            this.connection || (this.connection = new Ext.data.Connection());
            
            if (this.loadMask && this.autoMask && !this.loadMask.active) {
                this.loadMask.show({
                    msg: url.text || null,
                    fn: arguments.callee.createDelegate(this, arguments),
                    fnDelay: 100
                });
                return this.connection;
            }

            var dataUrl = url;
            if (!(dataUrl = this.assert(dataUrl, null))) {
                return null;
            }
            var method = method || (params ? "POST" : "GET");
            if (method === "GET") {
                dataUrl = this.prepareURL(dataUrl, false);
            }

            var o = {
                url: dataUrl,
                params: params,
                method: method,
                success: function (response, options) {
                    o.loadData = this.fireEvent('beforeload', this, this.getInterface(), response, options) !== false;
                },
                failure: function (response, options) {
                    this.fireEvent('loadexception', this, this.getInterface(), response, options);
                },
                scope: this,
                callback: function (options, success, response) {
                    o.loadData = success;
                    if (callback) {
                        o.loadData = callback.call(scope, this, success, response, options) !== false;
                    }
                    if (success && o.loadData) {
                        this.setChartData(options.chartResponse || response.responseText);
                    }
                    if (this.autoMask) {
                        this.onChartLoaded();
                    }
                },
                timeout: (30 * 1000),
                argument: {
                    "url": dataUrl,
                    "form": null,
                    "callback": callback,
                    "scope": scope,
                    "params": params
                }
            };
            this.connection.request(o);
            return this.connection;
        },
        setChartData: function (data) {
            return this;
        },
        setMask: function (ct) {
            chart.FlashAdapter.superclass.setMask.apply(this, arguments);
            var lm = this.loadMask;
            if (lm && !lm.disabled) {
                lm.el || (this.loadMask = lm = new Ext.ux.IntelliMask(this[this.mediaEl] || ct, Ext.isObject(lm) ? lm : {
                    msg: lm
                }));
            }
        },
        doAutoLoad: function () {
        	if(this.dataURL){
	            this.load(this.dataURL);
        	}
        },
        onChartRendered: function () {
            this.fireEvent('chartrender', this, this.getInterface());
            if (this.loadMask && this.autoMask) {
                this.loadMask.hide();
            }
        },
        onChartLoaded: function () {
            this.fireEvent('chartload', this, this.getInterface());
            if (this.loadMask && this.autoMask) {
                this.loadMask.hide();
            }
        },
        onFlashInit: function (id) {
            chart.FlashAdapter.superclass.onFlashInit.apply(this, arguments);
            this.fireEvent.defer(1, this, ['chartload', this, this.getInterface()]);
        },
        loadMask: false,
        getChartVersion: function () {}
    });
    chart.FlashAdapter.chartOnLoad = function (DOMId) {
        var c, d = Ext.get(DOMId);
        if (d && (c = d.ownerCt)) {
            c.onChartLoaded.defer(1, c);
            c = d = null;
            return false;
        }
        d = null;
    };
    chart.FlashAdapter.chartOnRender = function (DOMId) {
        var c, d = Ext.get(DOMId);
        if (d && (c = d.ownerCt)) {
            c.onChartRendered.defer(1, c);
            c = d = null;
            return false;
        }
        d = null;
    };
})();


(function () {
    Ext.namespace("Ext.ux.Chart.Fusion");
    var chart = Ext.ux.Chart;
    Ext.ux.Chart.Fusion.Adapter = Ext.extend(Ext.ux.Chart.FlashAdapter, {
        requiredVersion: 8,
        blankChartData: '<chart></chart>',
        chartData: null,
        disableCaching: false,
        dataURL: null,
        autoLoad: null,
        chartCfg: null,
        autoScroll: true,
        mediaCfg: {
            url: null,
            id: null,
            start: true,
            controls: false,
            height: null,
            width: null,
            autoSize: true,
            autoScale: false,
            renderOnResize: true,
            //Fusion required after reflow for < Fusion 3.1 (use when autoScale is false)
            scripting: 'always',
            cls: 'x-media x-media-swf x-chart-fusion',
            params: {
                wmode: 'opaque',
                salign: null
            },
            boundExternals: ['print', 'saveAsImage', 'setDataXML', 'setDataURL', 'getDataAsCSV', 'getXML', 'getChartAttribute', 'hasRendered', 'signature', 'exportChart']
        },
        initMedia: function () {
            this.addEvents(
            //Defined in FlashAdaper superclass
            'dataloaded', 'dataloaderror', 'nodatatodisplay', 'dataxmlinvalid', 'exported', 'exportready');
            //For compat with previous versions < 2.1
            this.chartCfg || (this.chartCfg = this.fusionCfg || {});
            chart.Fusion.Adapter.superclass.initMedia.call(this);
        },
        onBeforeMedia: function () {
            var mc = this.mediaCfg;
            var cCfg = this.chartCfg || (this.chartCfg = {});
            cCfg.params = this.assert(cCfg.params, {});
            cCfg.params[this.varsName] = this.assert(cCfg.params[this.varsName], {});
            cCfg.params[this.varsName] = Ext.apply({
                chartWidth: '@width',
                chartHeight: '@height',
                scaleMode: mc.autoScale ? 'exactFit' : 'noScale',
                debugMode: 0,
                DOMId: '@id',
                registerWithJS: 1,
                allowScriptAccess: "@scripting",
                lang: 'EN',
                dataXML: this.assert(this.dataXML || this.chartData || this.blankChartData, null)
//                dataXML: this.dataURL ? null : this.assert(this.dataXML || this.chartData || this.blankChartData, null)
//                dataURL: this.dataURL ? encodeURI(this.prepareURL(this.dataURL)) : null
            }, cCfg.params[this.varsName]);
            chart.Fusion.Adapter.superclass.onBeforeMedia.call(this);
        },

        setChartData: function (xml, immediate) {
            var o;
            this.chartData = xml;
//            this.dataURL = null;
            this.dataXML = xml;
            if (immediate !== false && (o = this.getInterface())) {
                if ('setDataXML' in o) {
                    o.setDataXML(xml);
                } else { //FC Free Interface
                    this.setVariable("_root.dataURL", "");
                    //Set the flag
                    this.setVariable("_root.isNewData", "1");
                    //Set the actual data
                    this.setVariable("_root.newData", xml);
                    //Go to the required frame
                    if ('TGotoLabel' in o) {
                        o.TGotoLabel("/", "JavaScriptHandler");
                    }
                }
            }
            o = null;
            return this;
        },
        setChartDataURL: function (url, immediate) {
            var o;
            this.dataURL = url;
            if (immediate !== false && (o = this.getInterface())) {
                'setDataURL' in o ? o.setDataURL(url) : this.load(url);
                o = null;
            }
        },
        getChartVersion: function () {
            return '';
        }
    });
    window.FC_Rendered = window.FC_Rendered ? window.FC_Rendered.createInterceptor(chart.FlashAdapter.chartOnRender) : chart.FlashAdapter.chartOnRender;
    window.FC_Loaded = window.FC_Loaded ? window.FC_Loaded.createInterceptor(chart.FlashAdapter.chartOnLoad) : chart.FlashAdapter.chartOnLoad;
    var dispatchEvent = function (name, id) {
        var c, d = Ext.get(id);
        if (d && (c = d.ownerCt)) {
            c.fireEvent.apply(c, [name, c, c.getInterface()].concat(Array.prototype.slice.call(arguments, 2)));
        }
        c = d = null;
    };
    //Bind Fusion callbacks to an Ext.Event for the corresponding chart.
    Ext.each(['FC_DataLoaded', 'FC_DataLoadError', 'FC_NoDataToDisplay', 'FC_DataXMLInvalid', 'FC_Exported', 'FC_ExportReady'], function (fnName) {
        var cb = dispatchEvent.createDelegate(null, [fnName.toLowerCase().replace(/^FC_/i, '')], 0);
        window[fnName] = typeof window[fnName] == 'function' ? window[fnName].createInterceptor(cb) : cb;
    });
    Ext.ux.Chart.Fusion.Component = Ext.extend(Ext.ux.Media.Flash.Component, {
        ctype: 'Ext.ux.Chart.Fusion.Component',
        mediaClass: Ext.ux.Chart.Fusion.Adapter
    });
    Ext.reg('fusion', chart.Fusion.Component);
})();
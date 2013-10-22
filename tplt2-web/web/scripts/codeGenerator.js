Ext.ux.CodeGenerator = function () {
    return {
        cfg: {
            NOID: false,
            CLASS: false
        },

        isArray: function (o) {
            return Object.prototype.toString.call(o) === '[object Array]';
        },

        fieldToStr: function (obj, lpad) {
            var bString = true;
            for (var p in obj) {
                switch (p) {
                case 'name':
                case 'xcls':
                case 'xtype':
                case 'storeId':
                case 'id':
                case 'dock':
                    break;
                case 'type':
                    if (obj[p] != 'auto') bString = false;
                    break;
                default:
                    bString = false;
                }
            }
            if (bString) {
                return "'" + obj.name + "'";
            }
            var arr = [];
            for (var p in obj) {
                switch (p) {
                case 'xtype':
                case 'xcls':
                case 'storeId':
                case 'id':
                    break;
                case 'type':
                    if (obj[p] != 'auto') arr.push(p + ':"' + obj[p] + '"');
                    break;
                default:
                    switch (typeof(obj[p])) {
                    case 'string':
                        arr.push(p + ':"' + obj[p] + '"');
                        break;
                    }
                    break;
                }
            }
            return '{\n' + lpad + '\t' + arr.join(',\n' + lpad + '\t') + '\n' + lpad + '}';
        },
        fieldsToString: function (obj, lpad) {
            if (this.isArray(obj)) {
                var l = obj.length;
                var arr = [];
                for (var i = 0; i < l; i++) {
                    arr.push(this.fieldToStr(obj[i], lpad));
                }
                return '[' + '\n' + lpad + arr.join(",\n" + lpad) + "\n" + lpad + ']';
            }
            return '[]';
        },

        objToCls: function (obj, dt, lpad) {
            // obj.name;
            var out = '';
            var s = obj.name.split('.');
            if (s.length > 2) // Ext.ux.component ...
            {
                out = 'Ext.ns("';
                var ns = [];
                for (var i = 0; i < s.length - 1; i++) //namespa
                ns.push(s[i]);
                out += ns.join('.') + '");';
            }
            if (s.length == 1) obj.name = 'Ext.' + obj.name; //Ext.component
            out += obj.name + '=Ext.extend(' + obj.xcls + ' ,{';
            var arr = [];
            var evtHandler = "";
            for (var p in obj) {
                switch (p) {
                case 'name':
                    break;
                case 'xcls':
                    break;
                case 'userXType':
                    break;
                case 'id':
                    if (this.cfg.NOID) break;
                case 'items':
                case 'tbar':
                case 'fbar':
                case 'bbar':
                    break;
                    break;
                case 'listeners':
                    evtHandler += 'listeners : {';
                    for (var evt in obj[p]) {
                        if (typeof(evt) == 'string') {
                            evtHandler += evt + ':' + obj[p][evt] + ',';
                        }
                    }
                    evtHandler = evtHandler.substring(0, evtHandler.length - 1);
                    evtHandler += '},';
                    break;
//				case 'selModel':
//					arr.push(p+': new Ext.grid.'+String(obj[p])+'()');
//					break;
                default:
                    switch (typeof(obj[p])) {
                    case 'string':
                        arr.push(p + ':"' + obj[p].replace(/\"/g, '\\\"') + '"');
                        break;
                    case 'number':
                        arr.push(p + ':' + obj[p]);
                        break;
                    case 'boolean':
                        arr.push(p + ':' + (obj[p] ? 'true' : 'false'));
                        break;
                    case 'object':
                        arr.push(p + ':' + this.objToString(obj[p], null, lpad, false));
                        break;
                    }
                }
            }

            if (!Ext.isEmpty(evtHandler)) {
                arr.push(evtHandler);
            }

            out += arr.join(',' + lpad);

            if (out.charAt(out.length - 1) != ',') {
                out += ',';
            }

            //if (arr.length > 0) out += ',' + lpad;
            out += 'initComponent: function(){';
            // initComponent 代码处理
            if (typeof(obj.tbar) != 'undefined') {
                out += lpad + 'this.tbar=' + this.objToString(obj.tbar, 'button', lpad, false) + ';';
            }
            if (typeof(obj.fbar) != 'undefined') {
                out += lpad + 'this.fbar=' + this.objToString(obj.fbar, 'button', lpad, false) + ';';
            }
            if (typeof(obj.bbar) != 'undefined') {
                out += lpad + 'this.bbar=' + this.objToString(obj.bbar, 'button', lpad, false) + ';';
            }
            if (typeof(obj.items) != 'undefined') {
                if (typeof(obj['defaultType']) == 'string') {
                    out += lpad + 'this.items=' + this.objToString(obj.items, obj['defaultType'], lpad, false) + ';';
                } else {
                    out += lpad + 'this.items=' + this.objToString(obj.items, null, lpad, false) + ';';
                }
            }
            out += lpad + obj.name + '.superclass.initComponent.call(this);' + lpad + '}})';
            if (typeof(obj.userXType) == 'string') {
                out += 'Ext.reg("' + obj.userXType + '",' + obj.name + ');';
            }

            return out;
        },

        objToString: function (obj, dt, lpad, isW, isClass) { // dt = defaulttype
            if (!lpad) {
                lpad = '';
            }
            var out = '';
            var isArr = this.isArray(obj);
            if (!isArr && isClass && typeof(obj['name']) == 'string') { // generate class code;
                return this.objToCls(obj, dt, lpad);
            }
            if (isW) {
                if (isArr) {
                    out = '[' + lpad;
                } else {
                    out = 'var ' + obj.name + '=new ' + obj.xcls + '({' + lpad;
                }
            } else {
                if (isArr) {
                    out = '[' + lpad;
                } else {
                    out = '{' + lpad;
                }
            }
            var arr = [];
            var evtHandler = "";
            if (isArr) {
                var len = obj.length;
                for (var i = 0; i < len; i++) {
                    switch (typeof(obj[i])) {
                    case 'string':
                		arr.push('"' + obj[i].replace(/\"/g, '\\\"') + '"');
                        break;
                    case 'number':
                        arr.push(obj[i]);
                        break;
                    case 'boolean':
                        arr.push(obj[i] ? 'true' : 'false');
                        break;
                    case 'object':
                        arr.push(this.objToString(obj[i], dt, lpad, false));
                        break;
                    }
                }
            } else {
                for (var p in obj) {
                    switch (p) {
                    case 'xcls':
                        break;
//                    case 'id':
//                        if (this.cfg.NOID) break;
//                    case 'name':
//                        break;
                    case 'xtype':
                        //						if (!isW) {
                        switch (obj[p]) {
							case 'tbfill':
								//return '"->"';
							case 'tbseparator':
								//return '"-"';
							case 'tbspacer':
								//return '" "';
							default:
								if (typeof(dt) == 'string' && dt == obj[p]) {
									break;
								} else {
									arr.push(p + ':"' + obj[p].replace(/\"/g, '\\\"') + '"');
								}
                        }
                        //						if (typeof(dt) == 'string' && dt == obj[p]) {
                        //							break;
                        //						}
                        //						if (isW){ 
                        //							break;
                        //						}
                        break;
                    case 'listeners':
                        evtHandler += 'listeners : {';
                        for (var evt in obj[p]) {
                            if (typeof(evt) == 'string') {
                                evtHandler += evt + ':' + obj[p][evt] + ',';
                            }
                        }

                        evtHandler = evtHandler.substring(0, evtHandler.length - 1);
                        evtHandler += '}';
                        break;
                    case 'renderer':
                       	arr.push(p+':function (' + obj[p].params.join(',') + ') {' + obj[p].value + '}');
                    	break;
                    case 'getExtGroupText':
                       	arr.push(p+':function (' + obj[p].params.join(',') + ') {' + obj[p].value + '}');
                    	break;
                    case 'convert':
                       	arr.push(p+':function (' + obj[p].params.join(',') + ') {' + obj[p].value + '}');
                    	break;
//    				case 'selModel':
//    					arr.push(p+': new Ext.grid.'+String(obj[p])+'()');
//    					break;
                    default:
                        switch (typeof(obj[p])) {
                        case 'string':
                            arr.push(p + ':"' + obj[p].replace(/\"/g, '\\\"') + '"');
                            break;
                        case 'number':
                            arr.push(p + ':' + obj[p]);
                            break;
                        case 'boolean':
                            arr.push(p + ':' + (obj[p] ? 'true' : 'false'));
                            break;
                        case 'object':
                            switch (p) {
                            case 'items':
                                //子控件
                                if (typeof(obj['defaultType']) == 'string') {
                                    arr.push(p + ':' + this.objToString(obj[p], obj['defaultType'], lpad, false));
                                    break;
                                }
                                switch (obj['xtype']) {
                                case 'buttongroup':
                                    arr.push(p + ':' + this.objToString(obj[p], 'button', lpad, false));
                                    break;
                                default:
                                    arr.push(p + ':' + this.objToString(obj[p], null, lpad, false));
                                    break;
                                }
                                break;
                            case 'bbar':
                            case 'tbar':
                            case 'fbar':
                                //工具条
                                arr.push(p + ':' + this.objToString(obj[p], 'button', lpad, false));
                                break;
                            case 'fields':
                                arr.push(p + ':' + this.fieldsToString(obj[p], lpad));
                                break;
                            default:
                            	if(obj[p].params != undefined){
                            		arr.push(p+':function (' + obj[p].params.join(',') + ') {' + obj[p].value + '}');
                            		break;
                            	}
                                arr.push(p + ':' + this.objToString(obj[p], null, lpad, false));
                            }
                            break;
                        }
                        break;
                    }
                }
            }
            if (!Ext.isEmpty(evtHandler)) {
                arr.push(evtHandler);
            }

            if (isW) {
                if (isArr) {
                    out += arr.join(lpad) + lpad.substring(0, lpad.length - 1) + ']';
                } else {
                    out += arr.join(',' + lpad) + lpad.substring(0, lpad.length - 1) + '})';
                }
            } else {
                if (isArr) {
                    out += arr.join(',' + lpad) + lpad.substring(0, lpad.length - 1) + ']';
                } else {
                    out += arr.join(',' + lpad) + lpad.substring(0, lpad.length - 1) + '}';
                }
            }
            return out;
        },

        parse_store: function (o,runable) {
            if (typeof(o.cn) != 'undefined') {
                o.fields = o.cn;
                delete o.cn;
            }
            
            if(Ext.isEmpty(o.fields)){
            	return;
            }
            
            for(var i=0;i<o.fields.length;i++){
            	var field=o.fields[i];
            	for(p in field){
            		if(p == "convert"){
            			if(runable){
            				field.convert=new Function(field[p].params,field[p].value);
            			}else{
            				field.convert='function ('+field[p].params.join(',')+") {"+field[p].value+'}';
            			}
            		}
            	}
            }
            
            if ((typeof(o.evtHandlers) !=  'undefined')) {
				var i=0;
                o.listeners = {};
                for (var evt in o.evtHandlers) {
                    if (typeof(o.evtHandlers[evt]) == 'object') {
                        if (runable) {
                            o.listeners[evt] = new Function(o.evtHandlers[evt].params, o.evtHandlers[evt].value);
                        } else {
                            o.listeners[evt] = 'function (' + o.evtHandlers[evt].params.join(',') + ') {' + o.evtHandlers[evt].value + '}';
                        }
                    }
					i++;
                }

				if(i==0){
					delete o.listeners;
				}

                delete o.evtHandlers;
            }            
        },

        parse_object: function (o, runable) {
            if (o.xtype == 'grid' || o.xtype == 'editorgrid') {
                o.columns = [];

				if(o.getExtGroupText){
					if(runable){
						o.getExtGroupText=new Function(o.getExtGroupText.params,o.getExtGroupText.value);
					}
				}
            }

            if (typeof(o.cn) != 'undefined') {
                var i = 0;
                var tmp = [];

                if (o.xtype == 'treepanel') {
                    o.root = o.cn.shift();
                }

                while (i < o.cn.length) {
                    if (typeof(o.cn[i].dock) != 'undefined') {
                    	var dock=o.cn[i].dock;
						if(o.cn[i].xtype){
							o[dock]=o.cn[i];
							
						} else if(o.cn[i].cn){
							o[dock]=o.cn[i].cn;
						} 
						
						delete o.cn[i];
						delete o[dock].dock;
                        //o[o.cn[i].dock] = o.cn[i].cn || [];
                        i++;
                    } else {
                        switch (o.cn[i].xtype) {
                        case 'gridcolumn':
                        case 'stringcolumn':
                            delete o.cn[i].xtype;
                            //o.columns.push(this.parse_object(o.cn[i], runable));
                            //i++;
                            //break;
                        case 'booleancolumn':
                        case 'rownumbercolumn':
                        case 'checkcolumn':
                        case 'datecolumn':
                        case 'bizcodecolumn':
                        case 'numbercolumn':
							var col=o.cn[i];
							if(col.cn){
								col.editor=this.parse_object(col.cn[0], runable);
								delete col.cn;
							}
							
							if(col.renderer){
								if(runable){
									col.renderer = new Function(col.renderer.params, col.renderer.value);
								}
							}
							
				            if ((typeof(col.evtHandlers) !=  'undefined')) {
								var size = 0;
				                col.listeners = {};
				                for (var evt in col.evtHandlers) {
				                    if (typeof(col.evtHandlers[evt]) == 'object') {
				                        if (runable) { 
				                            col.listeners[evt] = new Function(col.evtHandlers[evt].params, col.evtHandlers[evt].value);
				                        } else { 
				                            col.listeners[evt] = 'function (' + col.evtHandlers[evt].params.join(',') + ') {' + col.evtHandlers[evt].value + '}';
				                        }
				                    } 
				                    size ++;
				                } 
								if(size == 0){
									delete col.listeners;
								}

				                delete col.evtHandlers;
				            } 
                            o.columns.push(col);
                            i++;
                            break;
                        case 'jsonstore':
                        case 'jsongroupstore':
                        case 'arraystore':
                        case 'xmlstore':
                            this.parse_store(o.cn[i],runable);
                            o.store = Ext.apply({},o.cn[i]);
                            i++;
                            break;
                        case 'menu':
                            o.menu = o.cn[i];
                            i++;
                            break;
                        default:
                            tmp.push(o.cn[i]);
                            i++;
                        }
                    }
                }

                if (tmp.length > 0) {
                    o.items = tmp;
                }

                delete o.cn;

                if (o.xtype == 'menuitem' && typeof(o.items) != 'undefined') {
                    o.menu = {
                        xtype: 'menu'
                    };
                    o.menu.cn = o.items;
                    delete o.items;
                }

                if (o.xtype == 'treenode') {
                    o.children = o.items;
                    delete o.items;
                }

                if (typeof(o.items) != 'undefined') {
                    var len = o.items.length;
                    for (var i = 0; i < len; i++) {
                        if (typeof(o.items[i]) != 'undefined') {
                            this.parse_object(o.items[i], runable);
                        }
                    }
                }

                if (typeof(o.tbar) != 'undefined') {
                	this.parse_object(o.tbar, runable);

//                    var len = o.tbar.length;
//                    for (var i = 0; i < len; i++) {
//                        if (typeof(o.tbar[i]) != 'undefined') {
//                            this.parse_object(o.tbar[i], runable);
//                        }
//                    }
                }

                if (typeof(o.fbar) != 'undefined') {
                	this.parse_object(o.fbar, runable);
                	
//                    var len = o.fbar.length;
//                    for (var i = 0; i < len; i++) {
//                        if (typeof(o.fbar[i]) != 'undefined') {
//                            this.parse_object(o.fbar[i], runable);
//                        }
//                    }
                }

                if (typeof(o.bbar) != 'undefined') {
                	this.parse_object(o.bbar, runable);
                	
//                    var len = o.bbar.length;
//                    for (var i = 0; i < len; i++) {
//                        if (typeof(o.bbar[i]) != 'undefined') {
//                            this.parse_object(o.bbar[i], runable);
//                        }
//                    }
                }

                if (typeof(o.store) != 'undefined') {
                    if (typeof(o.store) != 'object') delete o.store;
                }

                if (typeof(o.menu) != 'undefined') {
                    this.parse_object(o.menu, runable);
                }

                if (typeof(o.root) != 'undefined') {
                    this.parse_object(o.root, runable);
                }

                if (typeof(o.children) != 'undefined') {
                    var len = o.children.length;
                    for (var i = 0; i < len; i++) {
                        if (typeof(o.children[i]) != 'undefined') {
                            this.parse_object(o.children[i], runable);
                        }
                    }
                }
            }

            if (o.xtype == 'treenode') {
                delete o.xtype;
                delete o.nodeType;
//                if (!o.children) {
//                    o.leaf = true;
//                }
            }

            if ((typeof(o.evtHandlers) !=  'undefined')) {
				var i=0;
                o.listeners = {};
                for (var evt in o.evtHandlers) {
                    if (typeof(o.evtHandlers[evt]) == 'object') {
                        if (runable) {
                            o.listeners[evt] = new Function(o.evtHandlers[evt].params, o.evtHandlers[evt].value);
                        } else {
                            o.listeners[evt] = 'function (' + o.evtHandlers[evt].params.join(',') + ') {' + o.evtHandlers[evt].value + '}';
                        }
                    }
					i++;
                }

				if(i==0){
					delete o.listeners;
				}

                delete o.evtHandlers;
            }
            

            return o;
        }
    };
} ();
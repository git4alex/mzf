 // Define a set of code type configurations
Ext.ns('Ext.ux.panel.CodeMirrorConfig');
Ext.apply(Ext.ux.panel.CodeMirrorConfig, {
    cssPath: "/tplt/styles/",
    jsPath: "/tplt/scripts/codemirror/"
});
Ext.apply(Ext.ux.panel.CodeMirrorConfig, {
    parser: {
        defo: { // js code
            parserfile: ["tokenizejavascript.js", "parsejavascript.js"],
            stylesheet: Ext.ux.panel.CodeMirrorConfig.cssPath + "jscolors.css"
        }
    }
});

Ext.ns('Ext.ux.panel.CodeMirror');
Ext.ux.panel.CodeMirror = Ext.extend(Ext.Panel, {
    sourceCode: this.sourceCode || '',
    initComponent: function() {
        this.contentChanged = false;
        var oThis = this;
        Ext.apply(this, {
            items: [{
                xtype: 'textarea',
                readOnly: false,
                hidden: true,
                value: this.sourceCode
            }]
        });
        
        Ext.ux.panel.CodeMirror.superclass.initComponent.apply(this, arguments);
        
        this.addEvents('save','codechanged');
    },
    
    triggerOnSave: function(){
        this.setTitleClass(true);
        var sNewCode = this.codeMirrorEditor.getCode();
        
//        Ext.state.Manager.set("edcmr_"+this.itemId+'_lnmbr', this.codeMirrorEditor.currentLine());
        
        this.oldSourceCode = sNewCode;
        this.onSave(arguments[0] || false);
    },
    
    onRender: function() {
        this.oldSourceCode = this.sourceCode;
        Ext.ux.panel.CodeMirror.superclass.onRender.apply(this, arguments);
        // trigger editor on afterlayout
        this.on('afterlayout', this.triggerCodeEditor, this, {
            single: true
        });
        
    },
    
    /** @private */
    triggerCodeEditor: function() {
        //this.codeMirrorEditor;
        var oThis = this;
        var oCmp = this.findByType('textarea')[0];
        var editorConfig = Ext.applyIf(this.codeMirror || {}, {
           height: "100%",
           width: "100%",
           lineNumbers: false,
           textWrapping: false,
           content: oCmp.getValue(),
           indentUnit: 4,
           tabMode: 'shift',
           readOnly: oCmp.readOnly,
           path: Ext.ux.panel.CodeMirrorConfig.jsPath,
           autoMatchParens: true,
//           initCallback: function(editor) {
//               editor.win.document.body.lastChild.scrollIntoView();
//               try {
//                   var iLineNmbr = ((Ext.state.Manager.get("edcmr_" + oThis.itemId + '_lnmbr') !== undefined) ? Ext.state.Manager.get("edcmr_" + oThis.itemId + '_lnmbr') : 1);
//                   //console.log(iLineNmbr);
//                   editor.jumpToLine(iLineNmbr);
//               }catch(e){
//                   //console.error(e);
//               }
//           },
           onChange: function() {
               var sCode = oThis.codeMirrorEditor.getCode();
               oCmp.setValue(sCode);
               
               if(oThis.oldSourceCode == sCode){
                   oThis.fireEvent('codechanged',false);
               }else{
            	   oThis.fireEvent('codechanged',true);
               }
               
           },
           saveFunction:function(){
        	   oThis.fireEvent('save');
           }
       });
        
        var sParserType = oThis.parser || 'defo';
        editorConfig = Ext.applyIf(editorConfig, Ext.ux.panel.CodeMirrorConfig.parser[sParserType]);
        
        this.codeMirrorEditor = new CodeMirror.fromTextArea( Ext.getDom(oCmp.id).id, editorConfig);
        
    }
//    
//    ,
//    
//    setTitleClass: function(){
//        //var tabEl = Ext.get(this.ownerCt.getTabEl( this ));
//        if(arguments[0] === true){// remove class
//            //tabEl.removeClass( "tab-changes" );
//            this.contentChanged = false;
//            this.setTitle("[Script]");
//        }else{//add class
//            //tabEl.addClass( "tab-changes" );
//            this.contentChanged = true;
//            this.setTitle("[Script] * ");
//        }
//    }
});


Ext.reg('uxCodeMirrorPanel', Ext.ux.panel.CodeMirror);
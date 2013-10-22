Ext.ux.CodeWindow=Ext.extend(Ext.Window,{
	title: "[Script]",
	modal: true,
	width: 800,
	height: 650,
	layout: 'border',
	maximizable:true,
	bodyStyle:'border:0 none;',
	initComponent:function(){
		var ps='';
		if(!Ext.isEmpty(this.params)){
			ps=this.params.join(',');
		}
		this.codeBegin=new Ext.Panel({
			border:false,
			region:'north',
			html:'function ('+ps+') {',
			padding:2,
			height:20,
			style: 'font-family:"consolas","courier new","segoe ui";'
		});

		this.codeEnd=new Ext.Panel({
			border:false,
			region:'south',
			html:'}',
			padding:2,
			height:20,
			style: 'font-family:"consolas","courier new","segoe ui";'+ Ext.isEmpty(this.help) ? 'border-bottom:1px solid #99bbe8;':''		
		});

		this.codeArea=new Ext.ux.panel.CodeMirror({
			region:'center',
			border:false,
			sourceCode: this.sourceCode,
			style:'padding-left:24px;background-color:white;',
			layout: 'fit',
			codeMirror: {
				height: '100%',
				width: '100%'
			}	
		});
		
		this.codeArea.on('save',this.handleOk,this);
		this.codeArea.on('codechanged',function(isChanged){
			this.setTitle(isChanged ? "[Script] * " : "[Script]");
			this.btnSave[isChanged ? "enable" : "disable"]();
		},this);

		this.helpPanel=new Ext.Panel({
			id:'helpPanel',
			region: 'south',
			split: true,
			padding: 4,
			border: false,
			style:'border-top:1px solid #99bbe8;',
			html: this.help,
			hidden:Ext.isEmpty(this.help)
		});
		
		Ext.apply(this,{
			tbar:["-", {
				text: 'Format',
				iconCls: 'icon-format',
				handler: this.handleFormat,
				scope:this
			}, '->', {
				text: "Help",
				iconCls: 'icon-help',
				pressed: true,
				enableToggle:true,
				toggleHandler: this.handleHelp,
				scope:this,
				disabled:Ext.isEmpty(this.help)
			}],
			items:[{
				border:false,
				layout:'border',
				region:'center',
				items:[
					this.codeBegin,
					this.codeArea,
					this.codeEnd
				]
			},this.helpPanel],
			buttons: [{
				ref:"../btnSave",
				text: "Save",
				handler: this.handleOk,
				scope:this,
				disabled:true
			},{
				text: "Cancel",
				handler:this.handleCancel,
				scope:this
			}],
			keys:[{key:'s',ctrl:true,fn:this.handleOk,scope:this,stopEvent:true}]
		});

		Ext.ux.CodeWindow.superclass.initComponent.call(this);
		this.addEvents('ok','cancel');
	},
	
	handleOk:function () {
		try{
			new Function(this.codeArea.codeMirrorEditor.getCode());
		}catch(e){
			Ext.Msg.alert('提示',e);
			return;
		}
		var value={};
		value.value=this.codeArea.codeMirrorEditor.getCode();
		value.params=this.params;
		this.fireEvent('ok',value);
		this.close();
	},

	handleCancel:function () {
		this.fireEvent('cancel');
		this.close();
	},
	
	handleFormat:function(){
		this.codeArea.codeMirrorEditor.setCode(js_beautify(this.codeArea.codeMirrorEditor.getCode()));
	},
	
	handleHelp:function (btn,pressed) {
		this.helpPanel.setVisible(pressed);
		this.codeEnd.style=('font-family:"consolas","courier new","segoe ui";'+ pressed ? 'border-bottom:1px solid #99bbe8;':'');
		this.helpPanel.ownerCt.doLayout();
	}
});

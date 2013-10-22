Ext.ns("xds.chart");
Ext.ns("od.chart");

xds.types.ChartColumn = Ext.extend(xds.types.BoxComponent, {
	cid : 'chartcolumn',
	iconCls : 'icon-chart-col',
	category : "Chart",
	defaultName : "&lt;ColumnChart&gt;",
	text : "ColumnChart",
	dtype : "xdchartcolumn",
	xtype : 'chartcolumn',
	xcls : "org.delta.chart.Column",
	naming : "ChartColumn",
	isContainer : false,
	bindable: true,
	transformGroup: "chart",
	layoutable : false,
	enableFlyout : false,
	defaultConfig:{
		showValues:true,
		showShadow:true,
		showLabels:true,
		showToolTip:true,
		showPlotBorder:false
	},
	initConfig:function(d,b){
		d.width=500;
		d.height=350;
	},
	getConfig:function(){
		if(!this.config){
			var ret = xds.types.ChartColumn.superclass.getConfig.apply(this,arguments);
			this.updateChartUrl();
			return ret;
		}
		
		return this.config;
	},
	setMultiSeries:function(name,value){
		this.setConfig(name,value);
		this.updateChartUrl();
	},
	updateChartUrl:function(){
		var ms = this.getConfigValue('multiSeries');
		var is3d = this.getConfigValue('is3d');
		if(ms){
			if(is3d){
				this.setConfig('chartURL','/tplt/fusioncharts/MSColumn3D.swf');
			}else{
				this.setConfig('chartURL','/tplt/fusioncharts/MSColumn2D.swf');
			}
		}else{
			if(is3d){
				this.setConfig('chartURL','/tplt/fusioncharts/Column3D.swf');
			}else{
				this.setConfig('chartURL','/tplt/fusioncharts/Column2D.swf');
			}
		}
	},
	set3d:function(name,value){
		this.setConfig(name,value);
		this.updateChartUrl();
	},
	xdConfigs: [{
        name: "animation",
        text: "显示动画",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "caption",
        text: "标题",
        group: "ChartColumn",
        ctype: "string"
    },{
        name: "xAxisName",
        text: "x轴名称",
        group: "ChartColumn",
        ctype: "string"
    },{
        name: "yAxisName",
        text: "y轴名称",
        group: "ChartColumn",
        ctype: "string"
    },{
        name: "dataURL",
        text: "数据源连接",
        group: "ChartColumn",
        ctype: "string"
    },{
        name: "loadMask",
        text: "数据加载提示",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "multiSeries",
        text: "多数据分组",
        group: "ChartColumn",
        ctype: "boolean",
        setFn: "setMultiSeries"
    },{
        name: "is3d",
        text: "3D效果",
        group: "ChartColumn",
        ctype: "boolean",
        setFn: "set3d"
    },{
        name: "showValues",
        text: "显示指标数值",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "palette",
        text: "颜色方案",
        group: "ChartColumn",
        ctype: "string",
        editor: 'options',
        options:['1','2','3','4','5']
    },{
        name: "showLabels",
        text: "显示x轴文字",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "rotateLabels",
        text: "标签文本旋转",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "labelDisplay",
        text: "标签显示方式",
        group: "ChartColumn",
        ctype: "string",
        editor: 'options',
        options:['WRAP', 'STAGGER', 'ROTATE', 'NONE']
    },{
        name: "labelStep",
        text: "x轴间隔",
        group: "ChartColumn",
        ctype: "number"
    },{
        name: "rotateValues",
        text: "指标值旋转",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "yAxisValuesStep",
        text: "y轴间隔",
        group: "ChartColumn",
        ctype: "number"
    },{
        name: "showShadow",
        text: "显示阴影",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "clickURL",
        text: "点击链接",
        group: "ChartColumn",
        ctype: "string"
    },{
        name: "subCaption",
        text: "副标题",
        group: "ChartColumn",
        ctype: "string"
    },{
        name: "showPlotBorder",
        text: "显示线框",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "showBorder",
        text: "显示图表边框",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "borderThickness",
        text: "边框线宽",
        group: "ChartColumn",
        ctype: "number"
    },{
        name: "showToolTip",
        text: "显示鼠标提示",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "useRoundEdges",
        text: "圆角",
        group: "ChartColumn",
        ctype: "boolean"
    },{
        name: "baseFontSize",
        text: "字体大小",
        group: "ChartColumn",
        ctype: "number"
    },{
        name: "itemClick",
        group: "(Event)",
        ctype: "fn",
        params: ['value','chartId']
    }]
});

xds.chart.fireEvent=function(cmpId,evtName,itemValue){
	Ext.getCmp(cmpId).fireEvent(evtName,itemValue,cmpId);
};

xds.chart.FusionAdapter = Ext.extend(Ext.ux.Chart.Fusion.Adapter,{
	setChartData:function(data,immediate){
		var xml = this.json2xml(data);
		xds.chart.FusionAdapter.superclass.setChartData.call(this,xml,immediate);
	},
	json2xml:function(data){
		var xml = data;
		try{
			var jsonData = Ext.decode(data);
			var props = {chart:{}};
			Ext.apply(props.chart,this.getChartCfg());
			Ext.apply(props.chart,jsonData.chart||{});
			if(props.chart.set){
				Ext.each(props.chart.set,function(item){
					item.link = 'JavaScript:xds.chart.fireEvent("'+this.getId()+'","itemClick","'+item.value+'");';
				},this);
			}
			var util = new JsonToXml();
			xml = util.toXml(props);
			
			return xml;
		}catch(e){
		}
		
		return data;
	}
});

xds.chart.Chart = Ext.extend(Ext.ux.Chart.Fusion.Component,{
	getChartCfg:function(){
		var ret = {};
		var initCfg = this.initialConfig;
		for(prop in initCfg){
			var v = initCfg[prop];
			if(v.constructor == String || v.constructor == Number || v.constructor == Boolean){
				ret[prop] = v;
			}
		}
		
		return ret;
	},
	demoData:"[{label:'一月',value:'462'},{label:'二月',value:'857'},{label:'三月',value:'671'},{label:'四月',value:'494'},{label:'五月',value:'761'},{label:'六月',value:'960'},{label:'七月',value:'629'},{label:'八月',value:'622'},{label:'九月',value:'376'},{label:'十月',value:'494'},{label:'十一月',value:'761'},{label:'十二月',value:'960'}]",
	afterRender:function(ct){
		this.autoLoad = false;
		this.dataXML = this.getDemoDataXML();
		xds.chart.Chart.superclass.afterRender.apply(this,arguments);
	},
	getDemoDataXML:function(){
		var ret = {chart:{}};
		ret.chart.set = Ext.decode(this.demoData);
		Ext.apply(ret.chart,this.getChartCfg());

		var util = new JsonToXml();
		xml = util.toXml(ret);
		
		return xml;
	}
});

od.chart.Chart = Ext.extend(Ext.ux.Chart.Fusion.Component, {
	mediaClass:xds.chart.FusionAdapter,
	initComponent:function(){
		this.autoLoad = true;
		od.chart.Chart.superclass.initComponent.apply(this,arguments);
		this.addEvents('itemClick');
	},
	getChartCfg:function(){
		var ret = {};
		var initCfg = this.initialConfig;
		for(prop in initCfg){
			var v = initCfg[prop];
			if(v.constructor == String || v.constructor == Number || v.constructor == Boolean){
				ret[prop] = v;
			}
		}
		
		return ret;
	}
});

xds.chart.Column = Ext.extend(xds.chart.Chart, {

});

od.chart.Column = Ext.extend(od.chart.Chart, {

});

Ext.reg('xdchartcolumn',xds.chart.Column);
Ext.reg('chartcolumn', od.chart.Column);

xds.types.ChartBar = Ext.extend(xds.types.BoxComponent, {
	cid : 'chartbar',
	iconCls : 'icon-chart-bar',
	category : "Chart",
	defaultName : "&lt;BarChart&gt;",
	text : "BarChart",
	dtype : "xdchartbar",
	xtype : 'chartbar',
	xcls : "org.delta.chart.Bar",
	naming : "ChartBar",
	isContainer : false,
	transformGroup: "chart",
	layoutable : false,
	enableFlyout : false,
	defaultConfig : {
		chartURL: '/tplt/fusioncharts/Bar2D.swf',
		showValues:true,
		showShadow:true,
		showLabels:true,
		showToolTip:true,
		showBorder:false
	},
	initConfig:function(d,b){
		d.width=500;
		d.height=350;
	},
	getConfig:function(){
		if(!this.config){
			var ret = xds.types.ChartBar.superclass.getConfig.apply(this,arguments);
			this.updateChartUrl();
			return ret;
		}
		
		return this.config;
	},
	setMultiSeries:function(name,value){
		this.setConfig(name,value);
		this.updateChartUrl();
	},
	updateChartUrl:function(){
		var ms = this.getConfigValue('multiSeries');
		if(ms){
			this.setConfig('chartURL','/tplt/fusioncharts/MSBar2D.swf');
		}else{
			this.setConfig('chartURL','/tplt/fusioncharts/Bar2D.swf');
		}
	},
	xdConfigs: [{
        name: "animation",
        text: "显示动画",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "caption",
        text: "标题",
        group: "ChartBar",
        ctype: "string"
    },{
        name: "xAxisName",
        text: "x轴名称",
        group: "ChartBar",
        ctype: "string"
    },{
        name: "yAxisName",
        text: "y轴名称",
        group: "ChartBar",
        ctype: "string"
    },{
        name: "dataURL",
        text: "数据源连接",
        group: "ChartBar",
        ctype: "string"
    },{
        name: "loadMask",
        text: "数据加载提示",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "multiSeries",
        text: "多数据分组",
        group: "ChartBar",
        ctype: "boolean",
        setFn: "setMultiSeries"
    },{
        name: "showValues",
        text: "显示指标数值",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "palette",
        text: "颜色方案",
        group: "ChartBar",
        ctype: "string",
        editor: 'options',
        options:['1','2','3','4','5']
    },{
        name: "showLabels",
        text: "显示x轴文字",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "rotateLabels",
        text: "标签文本旋转",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "labelDisplay",
        text: "标签显示方式",
        group: "ChartBar",
        ctype: "string",
        editor: 'options',
        options:['WRAP', 'STAGGER', 'ROTATE', 'NONE']
    },{
        name: "labelStep",
        text: "x轴间隔",
        group: "ChartBar",
        ctype: "number"
    },{
        name: "rotateValues",
        text: "指标值旋转",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "yAxisValuesStep",
        text: "y轴间隔",
        group: "ChartBar",
        ctype: "number"
    },{
        name: "showShadow",
        text: "显示阴影",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "clickURL",
        text: "点击链接",
        group: "ChartBar",
        ctype: "string"
    },{
        name: "subCaption",
        text: "副标题",
        group: "ChartBar",
        ctype: "string"
    },{
        name: "showPlotBorder",
        text: "显示线框",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "showBorder",
        text: "显示图表边框",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "borderThickness",
        text: "边框线宽",
        group: "ChartBar",
        ctype: "number"
    },{
        name: "showToolTip",
        text: "显示鼠标提示",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "useRoundEdges",
        text: "圆角",
        group: "ChartBar",
        ctype: "boolean"
    },{
        name: "baseFontSize",
        text: "字体大小",
        group: "ChartBar",
        ctype: "number"
    },{
        name: "itemClick",
        group: "(Event)",
        ctype: "fn",
        params: ['value','chartId']
    }]
});


xds.chart.Bar = Ext.extend(xds.chart.Chart, {

});

od.chart.Bar = Ext.extend(od.chart.Chart, {

});

Ext.reg('xdchartbar',xds.chart.Bar);
Ext.reg('chartbar', od.chart.Bar);

xds.types.ChartLine = Ext.extend(xds.types.BoxComponent, {
	cid : 'chartline',
	iconCls : 'icon-chart-line',
	category : "Chart",
	defaultName : "&lt;LineChart&gt;",
	text : "LineChart",
	dtype : "xdchartline",
	xtype : 'chartline',
	xcls : "org.delta.chart.Line",
	naming : "ChartLine",
	isContainer : false,
	transformGroup: "chart",
	layoutable : false,
	enableFlyout : false,
	defaultConfig : {
		chartURL: '/tplt/fusioncharts/Line.swf',
		drawAnchors:true,
		showValues:true,
		showShadow:true,
		showLabels:true,
		showToolTip:true,
		showBorder:false
	},
	initConfig:function(d,b){
		d.width=500;
		d.height=350;
	},
	getConfig:function(){
		if(!this.config){
			var ret = xds.types.ChartLine.superclass.getConfig.apply(this,arguments);
			this.updateChartUrl();
			return ret;
		}
		
		return this.config;
	},
	setMultiSeries:function(name,value){
		this.setConfig(name,value);
		this.updateChartUrl();
	},
	updateChartUrl:function(){
		var ms = this.getConfigValue('multiSeries');
		if(ms){
			this.setConfig('chartURL','/tplt/fusioncharts/MSLine.swf');
		}else{
			this.setConfig('chartURL','/tplt/fusioncharts/Line.swf');
		}
	},
	xdConfigs: [{
        name: "animation",
        text: "显示动画",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "caption",
        text: "标题",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "xAxisName",
        text: "x轴名称",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "yAxisName",
        text: "y轴名称",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "dataURL",
        text: "数据源连接",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "loadMask",
        text: "数据加载提示",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "multiSeries",
        text: "多数据分组",
        group: "ChartLine",
        ctype: "boolean",
        setFn: "setMultiSeries"
    },{
        name: "connectNullData",
        text: "连接空值",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "showValues",
        text: "显示指标数值",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "palette",
        text: "颜色方案",
        group: "ChartLine",
        ctype: "string",
        editor: 'options',
        options:['1','2','3','4','5']
    },{
        name: "showLabels",
        text: "显示x轴文字",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "rotateLabels",
        text: "标签文本旋转",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "labelDisplay",
        text: "标签显示方式",
        group: "ChartLine",
        ctype: "string",
        editor: 'options',
        options:['WRAP', 'STAGGER', 'ROTATE', 'NONE']
    },{
        name: "labelStep",
        text: "x轴间隔",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "rotateValues",
        text: "指标值旋转",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "yAxisValuesStep",
        text: "y轴间隔",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "showShadow",
        text: "显示阴影",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "clickURL",
        text: "点击链接",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "subCaption",
        text: "副标题",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "showBorder",
        text: "显示图表边框",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "borderThickness",
        text: "边框线宽",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "showToolTip",
        text: "显示鼠标提示",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "lineDashed",
        text: "虚线显示",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "lineThickness",
        text: "线宽",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "drawAnchors",
        text: "显示数据点",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "anchorRadius",
        text: "数据点大小",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "baseFontSize",
        text: "字体大小",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "itemClick",
        group: "(Event)",
        ctype: "fn",
        params: ['value','chartId']
    }]
});


xds.chart.Line = Ext.extend(xds.chart.Chart, {

});

od.chart.Line = Ext.extend(od.chart.Chart, {

});

Ext.reg('xdchartline',xds.chart.Line);
Ext.reg('chartline', od.chart.Line);


xds.types.ChartPie = Ext.extend(xds.types.BoxComponent, {
	cid : 'chartpie',
	iconCls : 'icon-chart-pie',
	category : "Chart",
	defaultName : "&lt;PieChart&gt;",
	text : "PieChart",
	dtype : "xdchartpie",
	xtype : 'chartpie',
	xcls : "org.delta.chart.Pie",
	naming : "ChartPie",
	isContainer : false,
	transformGroup: "chart",
	layoutable : false,
	enableFlyout : false,
	defaultConfig:{
		showValues:true,
		showShadow:true,
		showLabels:true,
		showToolTip:true,
		showBorder:false
	},
	initConfig:function(d,b){
		d.width=500;
		d.height=350;
	},
	getConfig:function(){
		if(!this.config){
			var ret = xds.types.ChartPie.superclass.getConfig.apply(this,arguments);
			this.updateChartUrl();
			return ret;
		}
		
		return this.config;
	},
	setMultiSeries:function(name,value){
		this.setConfig(name,value);
		this.updateChartUrl();
	},
	updateChartUrl:function(){
		var ms = this.getConfigValue('multiSeries');
		var is3d = this.getConfigValue('is3d');
		if(is3d){
			this.setConfig('chartURL','/tplt/fusioncharts/Pie3D.swf');
		}else{
			this.setConfig('chartURL','/tplt/fusioncharts/Pie2D.swf');
		}
	},
	set3d:function(name,value){
		this.setConfig(name,value);
		this.updateChartUrl();
	},
	xdConfigs: [{
        name: "animation",
        text: "显示动画",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "caption",
        text: "标题",
        group: "ChartPie",
        ctype: "string"
    },{
        name: "showPercentValues",
        text: "显示百分比",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "showPercentInToolTip",
        text: "鼠标提示中显示百分比",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "dataURL",
        text: "数据源连接",
        group: "ChartPie",
        ctype: "string"
    },{
        name: "loadMask",
        text: "数据加载提示",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "is3d",
        text: "3D效果",
        group: "ChartPie",
        ctype: "boolean",
        setFn: "set3d"
    },{
        name: "showValues",
        text: "显示指标数值",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "palette",
        text: "颜色方案",
        group: "ChartPie",
        ctype: "string",
        editor: 'options',
        options:['1','2','3','4','5']
    },{
        name: "showLabels",
        text: "显示文字",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "showValues",
        text: "显示指标值",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "use3DLighting",
        text: "使用3D光线效果",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "enableRotation",
        text: "允许旋转",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "radius3D",
        text: "3D效果直径",
        group: "ChartPie",
        ctype: "number"
    },{
        name: "labelDisplay",
        text: "标签显示方式",
        group: "ChartPie",
        ctype: "string",
        editor: 'options',
        options:['WRAP', 'STAGGER', 'ROTATE', 'NONE']
    },{
        name: "labelStep",
        text: "x轴间隔",
        group: "ChartPie",
        ctype: "number"
    },{
        name: "rotateValues",
        text: "指标值旋转",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "yAxisValuesStep",
        text: "y轴间隔",
        group: "ChartPie",
        ctype: "number"
    },{
        name: "showShadow",
        text: "显示阴影",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "clickURL",
        text: "点击链接",
        group: "ChartPie",
        ctype: "string"
    },{
        name: "subCaption",
        text: "副标题",
        group: "ChartPie",
        ctype: "string"
    },{
        name: "showPlotBorder",
        text: "显示线框",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "showBorder",
        text: "显示图表边框",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "borderThickness",
        text: "边框线宽",
        group: "ChartPie",
        ctype: "number"
    },{
        name: "showToolTip",
        text: "显示鼠标提示",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "useRoundEdges",
        text: "圆角",
        group: "ChartPie",
        ctype: "boolean"
    },{
        name: "baseFontSize",
        text: "字体大小",
        group: "ChartPie",
        ctype: "number"
    },{
        name: "itemClick",
        group: "(Event)",
        ctype: "fn",
        params: ['value','chartId']
    }]
});


xds.chart.Pie = Ext.extend(xds.chart.Chart, {

});

od.chart.Pie = Ext.extend(od.chart.Chart, {

});

Ext.reg('xdchartpie',xds.chart.Pie);
Ext.reg('chartpie', od.chart.Pie);

xds.types.ChartArea = Ext.extend(xds.types.BoxComponent, {
	cid : 'chartarea',
	iconCls : 'icon-chart-area',
	category : "Chart",
	defaultName : "&lt;AreaChart&gt;",
	text : "AreaChart",
	dtype : "xdchartarea",
	xtype : 'chartarea',
	xcls : "org.delta.chart.Area",
	naming : "ChartArea",
	isContainer : false,
	transformGroup: "chart",
	layoutable : false,
	enableFlyout : false,
	defaultConfig : {
		chartURL: '/tplt/fusioncharts/Area2D.swf',
		drawAnchors:true,
		showValues:true,
		showShadow:true,
		showLabels:true,
		showToolTip:true,
		showBorder:false
	},
	initConfig:function(d,b){
		d.width=500;
		d.height=350;
	},
	getConfig:function(){
		if(!this.config){
			var ret = xds.types.ChartLine.superclass.getConfig.apply(this,arguments);
			this.updateChartUrl();
			return ret;
		}
		
		return this.config;
	},
	setMultiSeries:function(name,value){
		this.setConfig(name,value);
		this.updateChartUrl();
	},
	updateChartUrl:function(){
		var ms = this.getConfigValue('multiSeries');
		if(ms){
			this.setConfig('chartURL','/tplt/fusioncharts/MSArea.swf');
		}else{
			this.setConfig('chartURL','/tplt/fusioncharts/Area2D.swf');
		}
	},
	xdConfigs: [{
        name: "animation",
        text: "显示动画",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "caption",
        text: "标题",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "xAxisName",
        text: "x轴名称",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "yAxisName",
        text: "y轴名称",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "dataURL",
        text: "数据源连接",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "loadMask",
        text: "数据加载提示",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "multiSeries",
        text: "多数据分组",
        group: "ChartLine",
        ctype: "boolean",
        setFn: "setMultiSeries"
    },{
        name: "connectNullData",
        text: "连接空值",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "showValues",
        text: "显示指标数值",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "palette",
        text: "颜色方案",
        group: "ChartLine",
        ctype: "string",
        editor: 'options',
        options:['1','2','3','4','5']
    },{
        name: "showLabels",
        text: "显示x轴文字",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "rotateLabels",
        text: "标签文本旋转",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "labelDisplay",
        text: "标签显示方式",
        group: "ChartLine",
        ctype: "string",
        editor: 'options',
        options:['WRAP', 'STAGGER', 'ROTATE', 'NONE']
    },{
        name: "labelStep",
        text: "x轴间隔",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "rotateValues",
        text: "指标值旋转",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "yAxisValuesStep",
        text: "y轴间隔",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "showShadow",
        text: "显示阴影",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "clickURL",
        text: "点击链接",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "subCaption",
        text: "副标题",
        group: "ChartLine",
        ctype: "string"
    },{
        name: "showBorder",
        text: "显示图表边框",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "borderThickness",
        text: "边框线宽",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "showToolTip",
        text: "显示鼠标提示",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "lineDashed",
        text: "虚线显示",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "lineThickness",
        text: "线宽",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "drawAnchors",
        text: "显示数据点",
        group: "ChartLine",
        ctype: "boolean"
    },{
        name: "anchorRadius",
        text: "数据点大小",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "baseFontSize",
        text: "字体大小",
        group: "ChartLine",
        ctype: "number"
    },{
        name: "itemClick",
        group: "(Event)",
        ctype: "fn",
        params: ['value','chartId']
    }]
});


xds.chart.Area = Ext.extend(xds.chart.Chart, {
});

od.chart.Area = Ext.extend(od.chart.Chart, {
});

Ext.reg('xdchartarea',xds.chart.Area);
Ext.reg('chartarea', od.chart.Area);


xds.types.ChartMapChina = Ext.extend(xds.types.BoxComponent, {
	cid : 'chartmapchina',
	iconCls : 'icon-chart-china',
	category : "Chart",
	defaultName : "&lt;MapChina&gt;",
	text : "MapChina",
	dtype : "xdchartmapchina",
	xtype : 'chartmapchina',
	xcls : "org.delta.chart.Area",
	naming : "MapChina",
	isContainer : false,
	transformGroup: "chart",
	layoutable : false,
	enableFlyout : false,
	defaultConfig : {
		chartURL: '/tplt/fusioncharts/map_china.swf',
		showBevel:false,
		animation:false,
		showShadow:false,
		showCanvasBorder:false,
		baseFontSize:12
	},
	initConfig:function(d,b){
		d.width=500;
		d.height=350;
	},
	xdConfigs: [{
        name: "animation",
        text: "显示动画",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "dataURL",
        text: "数据源连接",
        group: "ChartMapChina",
        ctype: "string"
    },{
        name: "showLegend",
        text: "显示图例",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "includeNameInLabels",
        text: "显示区域名称",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "includeValueInLabels",
        text: "显示区域指标",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "useSNameInLabels",
        text: "显示区域简称",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "useSNameInToolTip",
        text: "鼠标提示区域简称",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "hoverOnEmpty",
        text: "隐藏空值区域鼠标提示",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "exposeHoverEvent",
        text: "触发鼠标事件",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "showCanvasBorder",
        text: "显示边框",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "canvasBorderThickness",
        text: "边框线宽",
        group: "ChartMapChina",
        ctype: "number"
    },{
        name: "showShadow",
        text: "显示阴影",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "clickURL",
        text: "点击链接",
        group: "ChartMapChina",
        ctype: "string"
    },{
        name: "showToolTip",
        text: "显示鼠标提示",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "autoColor",
        text: "自动颜色填充",
        group: "ChartMapChina",
        ctype: "boolean"
    },{
        name: "baseFontSize",
        text: "字体大小",
        group: "ChartMapChina",
        ctype: "number"
    },{
        name: "itemClick",
        group: "(Event)",
        ctype: "fn",
        params: ['value','chartId']
    }]
});

xds.chart.Map={AH:'安徽',BJ:'北京',CQ:'重庆',FJ:'福建',GS:'甘肃',GD:'广东',GX:'广西',GZ:'贵州',HA:'海南',HB:'河北',HE:'河南',HU:'湖北',HL:'黑龙江',HN:'湖南',JS:'江苏',JX:'江西',JL:'吉林',LN:'辽宁',NM:'内蒙古',NX:'宁夏',QH:'青海',SA:'陕西',SD:'山东',SH:'上海',SX:'山西',SC:'四川',TJ:'天津',XJ:'新疆',XZ:'西藏',YN:'云南',ZJ:'浙江',MA:'澳门',HK:'香港',TA:'台湾'};
xds.chart.MapChina = Ext.extend(xds.chart.Chart, {
	demoData:"[{id:'CN.AH',value:'100',color:'FF3300'},{id:'CN.BJ',value:'100'},{id:'CN.CQ',value:'100'},{id:'CN.FJ',value:'100'},{id:'CN.GS',value:'100'},{id:'CN.GD',value:'100'},{id:'CN.GX',value:'100'},{id:'CN.GZ',value:'100'},{id:'CN.HA',value:'100'},{id:'CN.HB',value:'100'},{id:'CN.HE',value:'100'},{id:'CN.HU',value:'100'},{id:'CN.HL',value:'100'},{id:'CN.HN',value:'100'},{id:'CN.JS',value:'100'},{id:'CN.JX',value:'100'},{id:'CN.JL',value:'100'},{id:'CN.LN',value:'100'},{id:'CN.NM',value:'100'},{id:'CN.NX',value:'100'},{id:'CN.QH',value:'100'},{id:'CN.SA',value:'100'},{id:'CN.SD',value:'100'},{id:'CN.SH',value:'100'},{id:'CN.SX',value:'100'},{id:'CN.SC',value:'100'},{id:'CN.TJ',value:'100'},{id:'CN.XJ',value:'100'},{id:'CN.XZ',value:'100'},{id:'CN.YN',value:'100'},{id:'CN.ZJ',value:'100'},{id:'CN.MA',value:'100'},{id:'CN.HK',value:'100'},{id:'CN.TA',value:'100'}]",
	getDemoDataXML:function(){
		var ret = {map:{data:{entity:{}}}};
		ret.map.data.entity = Ext.decode(this.demoData);
		Ext.apply(ret.map,this.getChartCfg());

		var util = new JsonToXml();
		xml = util.toXml(ret);
		
		return xml;
	}
});


xds.chart.FusionMapAdapter = Ext.extend(xds.chart.FusionAdapter,{
	json2xml:function(data){
		var xml = data;
		try{
			var jsonData = Ext.decode(data);
			var props = {map:{data:{entity:{}}}};
			Ext.apply(props.map,this.getChartCfg());
			Ext.apply(props.map,jsonData.map||{});
			if(props.map.data.entity){
				Ext.each(props.map.data.entity,function(item){
					item.link = 'javascript:Ext.getCmp("'+this.getId()+'").fireEvent("itemClick","'+item.value+'","'+this.getId()+'");';
					var displayId = item.id.substr(3);
					if(this.includeNameInLabels){
						item.displayValue = (xds.chart.Map[displayId] || item.id);
					}
					item.toolText = (xds.chart.Map[displayId] || item.id) + ',' +item.value;
				},this);
				
				if(this.autoColor){
					this.fillColor(props.map.data.entity);
				}
				
				if(this.showLegend){
					var colorRange = [];
					Ext.each(props.map.data.entity,function(item){
						if(Ext.isEmpty(item.value)){
							return;
						}
						var range = {};
						range.minValue = item.value;
						range.maxValue = item.value;
						var displayId = item.id.substr(3);
						range.displayValue = (xds.chart.Map[displayId] || item.id) + '('+item.value+')';
						if(item.color){
							range.color = item.color;
						}
						colorRange.push(range);
					},this);
					
					props.map.colorRange = {};
					props.map.colorRange.color = colorRange; 
				}
			}
			
			var util = new JsonToXml();
			xml = util.toXml(props);
			return xml;
		}catch(e){
		}
		
		return data;
	},
	fillColor:function(entities){
		if(!Ext.isArray(entities)){
			return;
		}
		
		var step = parseInt(256/entities.length);
		this.sortEntity(entities);
		for(var i=0;i<entities.length;i++){
			if(entities[i].value){
				var g=(i*step).toString(16);
				g=('00'+g).substr(g.length);
				var b=g;
				entities[i].color = 'FF'+ g +b;				
			}else{
				entities[i].color = 'FFFFFF';		
			}
		}
	},
	sortEntity:function(entities){
		if(!Ext.isArray(entities)){
			return;
		}
		
		for(var i=0;i<entities.length;i++){
			for(var j=0;j<entities.length-i-1;j++){
				var item = entities[j];
				if(Ext.isEmpty(item.value) || parseInt(item.value) < parseInt(entities[j+1].value)){
					entities[j] = entities[j+1];
					entities[j+1] = item;
				}
			}
		}
	}
});

od.chart.MapChina = Ext.extend(od.chart.Chart, {
	mediaClass:xds.chart.FusionMapAdapter
});

Ext.reg('xdchartmapchina',xds.chart.MapChina);
Ext.reg('chartmapchina', od.chart.MapChina);

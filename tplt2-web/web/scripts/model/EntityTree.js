EntityTree = Ext.extend(Ext.tree.TreePanel, {
	id : 'EntityTree',
	frame : false,
	border : false,
	width : 300,
	height : 600,
	split : true,
	useArrows : true,
	autoScroll : true,
	animate : true,
	enableDD : false,
	// rootVisible:false,
	containerScroll : true,
	initComponent : function() {
		this.loader = new Ext.tree.TreeLoader({
			requestMethod : 'GET',
			url : Constants.queryEntityForList
		})
		EntityTree.superclass.initComponent.call(this);
	}
});
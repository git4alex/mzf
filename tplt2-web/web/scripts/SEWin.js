od.SEWindow = Ext.extend(Ext.Window, {
    xtype: "window",
    title: "Select Entity",
    width: 712,
    height: 430,
    layout: "border",
	modal:true,
    initComponent: function() {
        this.fbar = [{
            text: "Ok",
			handler:function(){
				var pGrid=Ext.getCmp('pGrid');
				var ps=pGrid.getSelectionModel().getSelections();
				if(ps.length>0){
					this.fireEvent('selected',this.entityId,ps);
				}
				this.close();
			},
			scope:this
        },{
            text: "Cancel",
			handler:function(){
				this.close();
			},
			scope:this
        }];
		
		var psm=new Ext.grid.CheckboxSelectionModel();

        this.items = [{
			id:'pGrid',
            xtype: "grid",
            store: {
                xtype: "jsonstore",
                storeId: "MyStore",
				root:'root',
				idProperty:'id',
				url:"fieldMetadata",
				restful:true,
				requestMethod:'GET',
				fields:['code','name','dataType','id']
            },
            region: "center",
            border: false,
            style: "border-left:1px solid #8daccb",
            autoExpandColumn: "colName",
			sm:psm,
            columns: [psm,{
                header: "Code",
                sortable: true,
                resizable: true,
                width: 100,
                dataIndex: "code",
                menuDisabled: true
            },{
				id:'colName',
                header: "Name",
                sortable: true,
                resizable: true,
                width: 100,
                dataIndex: "name",
                menuDisabled: true
            },{
                header: "Type",
                sortable: true,
                resizable: true,
                width: 100,
                dataIndex: "dataType",
                menuDisabled: true
            }]
        },{
            xtype: "grid",
            store: {
                xtype: "jsonstore",
				url:'entityMetadata',
                storeId: "entityStore",
				root:'root',
				idProperty:'id',                
				fields:['name','id','code'],
				autoLoad:true
            },
            region: "west",
            width: 240,
            border: false,
            split: true,
            style: "border-right:1px solid #8daccb",
            autoExpandColumn: "col1",
            columns: [{
				id:'col1',
                header: "Entity",
                sortable: true,
                resizable: false,
                width: 100,
                dataIndex: "name",
                menuDisabled: true
            }],
			listeners:{
				'rowclick':{
					fn:function(grid,idx,evt){
						var pGrid=Ext.getCmp('pGrid');
						var item=grid.getSelectionModel().getSelected();
						if(item){
							this.entityId=item.data.code;
							pGrid.getStore().load({params:{entityId:item.data.id}});
						}
					},
					scope:this
				}
			}
        }];
        od.SEWindow.superclass.initComponent.call(this);
        this.addEvents('selected');
    }
});
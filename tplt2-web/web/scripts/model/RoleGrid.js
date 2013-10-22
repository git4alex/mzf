RoleGrid = Ext.extend(Ext.grid.GridPanel, {
			/**
			 * @cfg {String} 得到数据的URL
			 */
			dataUrl : "",

			loadMask : true,

			autoLoad : this.dataUrl,

			singleSelect : true,

			pageSize : 20,

			height : 500,

			storeFields : [],

			defaultSort : [],

			viewConfig : {
				autoFill : true,
				forceFit : true
			},
			requestMethod : 'GET',
			initComponent : function() {
				this.columns = [{
							header : '编号',
							hidden : true,
							dataIndex : 'id'
						}, {
							header : '分配角色编号',
							hidden : true,
							dataIndex : 'allocatedId'
						}, {
							header : '角色名称',
							width : 100,
							sortable : false,
							dataIndex : 'name'
						}, {
							header : '角色代码',
							width : 40,
							sortable : true,
							dataIndex : 'roleCode'
						}, {
							header : '修改时间',
							width : 100,
							sortable : true,
							dataIndex : 'mdate'
						}, {
							header : '说明',
							width : 80,
							sortable : false,
							dataIndex : 'remark'
						}];
				if (this.dataUrl) {
					var storeFields = this.storeFields;
					Ext.each(this.columns, function(v) {
								storeFields.push({
											name : v.dataIndex
										});
							});

					this.sm = new Ext.grid.CheckboxSelectionModel({
								singleSelect : this.singleSelect
							});

					this.cm = new Ext.grid.ColumnModel(new Array(this.sm)
							.concat(this.columns));

					this.store = new Ext.data.Store({
								proxy : new Ext.data.HttpProxy({
											url : this.dataUrl,
											method : this.requestMethod
										}),
								reader : new Ext.data.JsonReader({
											root : 'root',
											totalProperty : 'totalCount',
											id : 'id',
											fields : this.storeFields
										}),
								baseParams : this.baseParams || {}
							});
				};

				// 默认排序
				if (this.defaultSort.length != 0) {
					this.store.setDefaultSort(this.defaultSort[0], Ext.value(
									this.defaultSort[1], 'desc'));
				}

				// 自动加载
				if (this.store)
					this.store.load({
								params : {
									start : 0,
									limit : this.pageSize
								}
							});

				this.bbar = this.bbar || new Ext.PagingToolbar({
							pageSize : 20,
							store : this.store,
							displayInfo : true,
							displayMsg : '第{0} 到 {1} 条数据 共{2}条',
							emptyMsg : "没有数据",
							items : []
						});
				RoleGrid.superclass.initComponent.call(this);
			}
		});
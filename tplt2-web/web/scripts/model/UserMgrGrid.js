UserMgrGrid = Ext.extend(Ext.grid.GridPanel, {
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
							header : '用户名',
							sortable : false,
							dataIndex : 'userName'
						}, {
							header : '登录名',
							sortable : false,
							dataIndex : 'loginName'
						}, {
							header : '密码',
							hidden : true,
							dataIndex : 'password'
						}, {
							header : '生日',
							dataIndex : 'birthday'
						}, {
							header : '年龄',
							dataIndex : 'age'
						}, {
							header : '电话',
							dataIndex : 'phone'
						}, {
							header : '邮箱',
							dataIndex : 'email'
						}, {
							header : '证件',
							dataIndex : 'idCard'
						}, {
							header : '地址',
							dataIndex : 'address'
						}, {
							header : '最后登录时间',
							format : 'Y-m-d',
							dataIndex : 'lastLoginDate'
						}, {
							header : '状态',
							width : 100,
							sortable : true,
							dataIndex : 'state',
							renderer : function(val) {
								return val == 1 ? '可用' : '禁止';
							}
						}, {
							header : '创建时间',
							width : 100,
							sortable : false,
							dataIndex : 'cdata',
							format : "Y-m-d"
						}];

				// 如果存在这个URl，那么就生成默认的Store
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
										})
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

				UserMgrGrid.superclass.initComponent.call(this);
			}
		});
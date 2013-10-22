LoggorGrid = Ext.extend(Ext.ux.grid.GroupGridPanel, {
			initComponent : function() {
				this.columns = [{
							header : '编号',
							hidden : true,
							dataIndex : 'id'
						}, {
							header : '模块编号',
							width : 100,
							dataIndex : 'moduleId'
						}, {
							header : '名称',
							width : 40,
							sortable : true,
							dataIndex : 'name'
						}, {

							header : '状态',
							width : 40,
							sortable : true,
							dataIndex : 'enable',
							renderer : function(val) {
								if (val == true)
									val = '有效';
								else
									val = '无效';
								return val;
							}

						}, {
							header : '修改时间',
							width : 130,
							sortable : false,
							dataIndex : 'mdate'
						}, {
							header : '说明',
							width : 100,
							sortable : false,
							dataIndex : 'remark'
						}];

				LoggorGrid.superclass.initComponent.call(this);
			}
		});
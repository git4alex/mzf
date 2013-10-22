FieldGrid = Ext.extend(DynamicGrid, { 
			singleSelect : true,
			rowEditor : null,
			initComponent : function() {
				if (!this.rowEditor)
					this.rowEditor = new Ext.ux.grid.RowEditor({
								saveText : '保存',
								cancelText : '取消'
							});

				if (this.rowEditable) {
					Ext.apply(this, {
								plugins : this.rowEditor
							});
				}
				FieldGrid.superclass.initComponent.call(this);
			},
			getRecord : function() {
				var Record = Ext.data.Record.create([{
							name : 'name',
							type : 'string'
						}, {
							name : 'code',
							type : 'string'
						}, {
							name : 'dataType',
							type : 'int'
						}, {
							name : 'length',
							type : 'int'
						}, {
							name : 'primaryKey',
							type : 'int'
						}, {
							name : 'mandatory',
							type : 'int'
						}, {
							name : 'displayType',
							type : 'string'
						}, {
							name : 'fieldSize',
							type : 'int'
						}, {
							name : 'showInGrid',
							type : 'int'
						}, {
							name : 'hidden',
							type : 'int'
						}, {
							name : 'readOnly',
							type : 'int'
						}, {
							name : 'allowBlank',
							type : 'int'
						}, {
							name : 'datasourceUrl',
							type : 'string'
						}, {
							name : 'comment',
							type : 'string'
						}]);

				return new Record();
			}
		});
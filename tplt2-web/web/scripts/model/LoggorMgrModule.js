LoggorMgrModule = Ext.extend(Ext.Panel, {
	layout:'fit',
	initComponent : function() {
		var loggor = new LoggorGrid({
			title : '日志管理',
			groupField : 'moduleId',
			border : false,
			dataUrl : Constants.sysloggor,
			tbar : [{
						text : '更新状态',
						iconCls : 'icon-user-add',
						scope : this,
						handler : function() {
							var record = loggor.getSelectionModel()
									.getSelected();
							if (record) {
								var id = record.get('id');
								var enable = record.get('enable');
								Ext.Ajax.request({
											url : Constants.sysloggor + "/" + id,
											jsonData : {
												id : id,
												enable : !enable
											},
											method : 'PUT',
											success : function(resp) {
												result = Ext
														.decode(resp.responseText)
												if (result.success) {
													loggor.getStore().reload();
												} else
													Ext.Msg.alert('提示', '操作失败');
											},
											failure : function(resp) {
												Ext.Msg.alert('提示', '操作失败');
											}
										});
							} else {
								Ext.Msg.alert('提示', '请选择一行');
							}
						}
					}]
		});

		this.items = [loggor];
		LoggorMgrModule.superclass.initComponent.call(this);
	}
});

LoggerModule = Ext.extend(od.Module, {
			moduleId : 'logmgr',
			iconCls : 'icon-xds',
			components : [LoggorMgrModule],
			createDefaultComponent : function() {
				return new LoggorMgrModule();
			}
		});

od.ModuleMgr.registerType('logmgr', LoggerModule);
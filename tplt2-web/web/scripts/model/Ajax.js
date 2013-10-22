/**
 * 主要负责对Ext.ajax 进一步封装
 */
Ajax = {
	// public: main request method
	request : function(config) {
		Ajax.showWait();
		if (config.confirm) {
			Ext.Msg.confirm('操作提示', config.confirm, function(btn) {
						if (btn == 'yes') {
							Ajax.doRequest(config);
						}
					});
		} else {
			Ajax.doRequest(config);
		}
	},

	// private
	processRequest : function(options, success, response, successCallback,
			failureCallback) {
		try {
			var result = Ext.decode(response.responseText);
			if (result.success && success) {
				successCallback(result);
			} else {
				failureCallback(result);
			}
		} catch (e) {
			if (success == false) {
				failureCallback();
			}
		}
	},
	// private
	doRequest : function(config) {
		var successCallback = function(result) {
			if (result.msg) {
				Ajax.alert(result.msg);
			} else {
				Ajax.alert("操作成功！");
			}
			if (config.success) {
				config.success(result);
			}
		};

		var failureCallback = function(result) {
			if (result.msg) {
				Ajax.alert(result.msg);
			} else {
				Ajax.alert("操作成功！");
			}
			if (config.failure) {
				config.failure(result);
			}
		};
		Ext.Ajax.request(Ext.applyIf(config, {
					callback : function(options, success, response) {
						Ajax.processRequest(options, success, response,
								successCallback, failureCallback);
					}
				}));

		Ajax.hideWait();
	},
	// private
	alert : function(msg, fn) {
		Ext.Msg.alert("操作提示", msg, fn || function() {
				});
	},
	// private
	showWait : function() {
		Ext.MessageBox.show({
					progressText : '进行中...',
					msg : '正在处理中, 请稍侯....',
					width : 300,
					wait : true,
					waitConfig : {
						interval : 200
					}
				});
	},
	// private
	hideWait : function() {
		Ext.MessageBox.hide();
	}
};
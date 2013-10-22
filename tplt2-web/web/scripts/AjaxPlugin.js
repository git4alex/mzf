// Ajax请求前事件
// Ext.Ajax.on("beforerequest", function(conn, opts) {
// if (opts.maskBody) {
// this.loadMask = new Ext.LoadMask(opts.maskBody, {
// removeMask : true,
// msg : "信息请求中......"
// });
// this.loadMask.show();
// }
// }, this);
// Ajax请求完成事件
// Ext.Ajax.on("requestcomplete", function(conn, resp, opts) {
// if (opts.maskBody) {
// if (this.loadMask) {
// this.loadMask.hide();
// delete this.loadMask;
// }
// }
// }, this);
// Ajax请求出现异常事件
// Ext.Ajax.on("requestexception", function(conn, resp, opts) {
// if (opts.maskBody) {
// if (this.loadMask) {
// this.loadMask.hide();
// delete this.loadMask;
// }
// }
// }, this);

Ext.LoadMask.prototype.maskShow = Ext.LoadMask.prototype.show;
Ext.LoadMask.prototype.show = function(isShow) {
	if (isShow) {
		this.maskShow();
	}
};
var doLoadMask = function() {
	var mainBody = Ext.getBody();	 
	if (od.curWinList.getCount() >= 1){
		mainBody = od.curWinList.first().bwrap;
	}
	od.globMask = new Ext.LoadMask(mainBody, {
				msg : '请稍候......',
				removeMask : true
			});
}

Ext.Ajax.on('beforerequest', function(con, cfg) {
			if (!Ext.getBody().isMasked()) {
				if (!cfg.hideMask) {
					doLoadMask();
					od.globMask.show(true);
				}
			}
		});

Ext.Ajax.on('requestcomplete', function(con, resp, options) {
			od.globMask.hide();
		});

Ext.Ajax.on('requestexception', function(con, resp, options) {
			od.globMask.hide();
		});
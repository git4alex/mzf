/**
 * framework主要功能： 1.配置文件加载 2.继承module的子模块加载 3.提供子模块实例化方式
 */

Framework = function() { 
	return { 
		// 框架默认窗口
		showWin : function(config) {
			Ext.apply(config, {
						autoDestroy : true
					})
			this.win = new DefaultWin(config);
			this.win.show();
		},
		closeWin : function() {
			if (this.win)
				this.win.close();
		}
	};
}();

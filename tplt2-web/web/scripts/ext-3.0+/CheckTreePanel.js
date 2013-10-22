Ext.ns('Ext.ux.tree');

Ext.ux.tree.CheckTreePanel = Ext.extend(Ext.tree.TreePanel, {
	dataUrl : "",
	autoScroll : true,
	treeFilter : false,
	requestMethod : 'GET',
	createNode : Ext.emptyFn,
	baseParams:{},
	initComponent : function() {

		// 'multiple':多选; 'single':单选; 'cascade':级联多选
		if (this.checkModel) {
			this.baseAttrs = {
				uiProvider : Ext.tree.TreeCheckNodeUI
			};
		}

		this.root = this.root || new Ext.tree.AsyncTreeNode({
					text : this.rootText || '',
					expanded : true
				});

		this.loader = new Ext.tree.TreeLoader({
					requestMethod : this.requestMethod,
					dataUrl : this.dataUrl,
					baseAttrs : this.baseAttrs || {},
					createNode : this.createNode,
					baseParams:this.baseParams
				});
		this.hiddenPkgs = [];
		if (this.treeFilter) {
			Ext.applyIf(this, {
						tbar : [' ', new Ext.form.TextField({
									width : 140,
									emptyText : '查找',
									enableKeyEvents : true,
									listeners : {
										render : function(f) {
											this.filter = new Ext.tree.TreeFilter(
													this, {
														clearBlank : true,
														autoClear : true
													});
										},
										keydown : {
											fn : this.filterTree,
											buffer : 350,
											scope : this
										},
										scope : this
									}
								}), ' ', ' ', {
							iconCls : 'icon-expand-all',
							tooltip : '展开全部',
							handler : function() {
								this.root.expand(true);
							},
							scope : this
						}, '-', {
							iconCls : 'icon-collapse-all',
							tooltip : '收合全部',
							handler : function() {
								this.root.collapse(true);
							},
							scope : this
						}]
					});
		}
		Ext.ux.tree.CheckTreePanel.superclass.initComponent.call(this);
	},
	filterTree : function(t, e) {

		var text = t.getValue();
		this.filter.clear();
		this.matched = [];
		// 如果输入的数据不存在，就执行clear()
		if (!text) {
			return;
		}
		this.expandAll();

		// 不是回车键就返回
		if (e.getKey() != 13) {
			return;
		}

		// 根据输入制作一个正则表达式，'i'代表不区分大小写
		var re = new RegExp(Ext.escapeRe(text), 'i');
		// 找出所有匹配的结点
		this.root.cascade(function(n) {
					if (re.test(n.text)) {
						this.matched.push(n);
					}
				}, this);

		// 从每个叶子结点向根方向处理,处理所有结点的枝叶,
		// 如果该枝叶包含匹配的结点,则保留,否则裁剪掉(隐藏)
		this.root.cascade(function(n) {
			if (!n.isLeaf()) {
				// 处理每一条子结点路径
				n.bubble(function(nbb) {
							// 从叶子到根,逐个剪掉
							var contain = false;
							for (var mted = 0; mted < this.matched.length; mted++) {
								if (nbb.contains(this.matched[mted])
										|| nbb == this.matched[mted]) {
									// 包含匹配的结点
									contain = true;
									break;
								}
							}
							// 把不包含匹配结点的结点隐藏
							if (!contain) {
								nbb.ui.hide();
								this.filter.filtered[nbb.id] = nbb;
							}
						}, this);
			}
		}, this);
	}
});

Ext.reg("CheckTreePanel", Ext.ux.tree.CheckTreePanel);
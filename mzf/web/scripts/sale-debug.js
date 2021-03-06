
Ext.ns("com.mzf.sale");
com.mzf.sale = {
  matchSalerule:function (btn,evt){
   var win = btn.refOwner;
	if (!win) {
		Ext.Msg.alert("\u63d0\u793a", "\u672a\u627e\u5230\u7a97\u4f53");
		return;
	}
	var form = win.get(0);
	if (form && form.getForm().isValid()) {
		var data = form.getForm().getFieldValues();
		data.detailList = [];
		var grid = Ext.getCmp("saleDedailGrid");
		var hasMaterial = false;
		var pArr = [];
		if (grid) {
			grid.getStore().each(function (record) {
				if (record.data.type == "material") {
					hasMaterial = true;
				}
				if (record.data.type == "product") {
					pArr.push(record);
				}
				data.detailList.push(record.data);
			});
		}
		if (!hasMaterial) {
			Ext.Msg.alert("\u63d0\u793a", "\u6b64\u9500\u552e\u5355\u660e\u7ec6\u5217\u8868\u4e2d\u6ca1\u6709\u7269\u6599\u9500\u552e");
			return false;
		}
		if (!Ext.isEmpty(pArr)) {
			if (pArr.length > 1) {
				var rec = pArr[0];
				for (var i = 0; i < pArr.length; i++) {
					if (rec.data.ptype != pArr[i].data.ptype) {
						Ext.Msg.alert("\u63d0\u793a", "\u9500\u552e\u5355\u53ea\u80fd\u9500\u552e\u540c\u4e00\u79cd\u5546\u54c1\u7c7b\u578b\u7684\u5546\u54c1");
						return;
					}
				}
			}
		}
		var empId = Ext.getCmp("employeeId");
		if (empId) {
			data.employeeId = empId.getValue();
			data.employeeName = empId.getRawValue();
		}
		var assistantEmployeeId = Ext.getCmp("assistantEmployeeId");
		if(assistantEmployeeId){
		    data.assistantEmployeeId = assistantEmployeeId.getValue();
		    data.assistantEmployeeName = assistantEmployeeId.getRawValue();
		}
		
		var checker = Ext.getCmp("checkerId");
		if (checker) {
			data.checkerId = checker.getValue();
			data.checkerName = checker.getRawValue();
		}
		Ext.Ajax.request({
		    url:"salerule/app/matchAllSalerule", 
		    method:"POST", jsonData:data, 
		    maskBody:win.getEl(), 
		    success:function (resp) {
			var result = Ext.decode(resp.responseText);
			if (result.success) {
               var win_ = od.showWindow('showRuleWin');
               com.mzf.sale.showAllSalerule(result);
			} else {
				Ext.Msg.alert("\u63d0\u793a", result.msg);
			}
		}, failure:ajaxFailure});
		}
},
step1Process:function (btn, evt) {
	var win = btn.refOwner;
	if (!win) {
		Ext.Msg.alert("\u63d0\u793a", "\u672a\u627e\u5230\u7a97\u4f53");
		return;
	}
	var form = win.get(0);
	if (form && form.getForm().isValid()) {
		var data = form.getForm().getFieldValues();
		data.detailList = [];
		var grid = Ext.getCmp("saleDedailGrid");
		var hasMaterial = false;
		var pArr = [];
		if (grid) {
			grid.getStore().each(function (record) {
				if (record.data.type == "material") {
					hasMaterial = true;
				}
				if (record.data.type == "product") {
					pArr.push(record);
				}
				data.detailList.push(record.data);
			});
		}
		if (!hasMaterial) {
			Ext.Msg.alert("\u63d0\u793a", "\u6b64\u9500\u552e\u5355\u660e\u7ec6\u5217\u8868\u4e2d\u6ca1\u6709\u7269\u6599\u9500\u552e");
			return false;
		}
		if (!Ext.isEmpty(pArr)) {
			if (pArr.length > 1) {
				var rec = pArr[0];
				for (var i = 0; i < pArr.length; i++) {
					if (rec.data.ptype != pArr[i].data.ptype) {
						Ext.Msg.alert("\u63d0\u793a", "\u9500\u552e\u5355\u53ea\u80fd\u9500\u552e\u540c\u4e00\u79cd\u5546\u54c1\u7c7b\u578b\u7684\u5546\u54c1");
						return;
					}
				}
			}
		}
		var empId = Ext.getCmp("employeeId");
		if (empId) {
			data.employeeId = empId.getValue();
			data.employeeName = empId.getRawValue();
		}
		var assistantEmployeeId = Ext.getCmp("assistantEmployeeId");
		if(assistantEmployeeId){
		    data.assistantEmployeeId = assistantEmployeeId.getValue();
		    data.assistantEmployeeName = assistantEmployeeId.getRawValue();
		}
		var checker = Ext.getCmp("checkerId");
		if (checker) {
			data.checkerId = checker.getValue();
			data.checkerName = checker.getRawValue();
		}
		Ext.Ajax.request({url:"salerule/app/matchSaleruleForSingle", method:"POST", jsonData:data, maskBody:win.getEl(), success:function (resp) {
			var result = Ext.decode(resp.responseText);
			if (result.success) {
				if (result.step == 2) {
					var _win = od.create("singleRuleWin");
					_win.data = {preview:result.productIdRules, cache:result.bill};
					_win.show();
					com.mzf.sale.step2ShowInit(_win);
				} else {
					if (result.step == 3) {
						var _win = od.create("billRuleWin");
						_win.data = {preview:result.billSalerules, cache:result.bill};
						_win.show();
						com.mzf.sale.step3ShowInit(_win);
					} else {
						if (result.step == 5) {
							var _win = od.create("payWin");
							_win.data = {preview:result.bill};
							_win.show();
							com.mzf.sale.step5ShowInit(_win);
						} else {
							Ext.Msg.alert("\u63d0\u793a", "\u6ca1\u6709\u5339\u914d\u5230\u4fc3\u9500\u89c4\u5219");
						}
					}
				}
			} else {
				Ext.Msg.alert("\u63d0\u793a", result.msg);
			}
		}, failure:ajaxFailure});
	}
}, step2Process:function (btn, evt) {
	var win = btn.refOwner;
	var tree = Ext.getCmp("singleRuleTree");
	if (!win.data || !win.data.cache) {
		Ext.Msg.alert("\u63d0\u793a", "\u8868\u5355\u6570\u636e\u4e22\u5931");
		return;
	}
	var data = win.data.cache;
	var productIdRuleResults = [];
	var roots = tree.getRootNode().childNodes;
	for (var i = 1; i < roots.length; i++) {
		    var root = roots[i]; 
				var proResult = {};
				proResult.proNum = root.id;
				var resultIds = [];
				for (var j = 0; j < root.childNodes.length; j++) {
					var child = root.childNodes[j];
					if (child.attributes.checked) {
						for (var m = 0; m < child.childNodes.length; m++) {
							var resultChild = child.childNodes[m];
							if (resultChild.attributes.checked) {
								resultIds.push(resultChild.resultId);
							}
						}
					}
			 } 
			 proResult.resultIds = resultIds;
		     productIdRuleResults.push(proResult);  
	} 
	data.productIdRuleResults = productIdRuleResults;
	 
	Ext.Ajax.request({url:"salerule/app/appSingleRulesOfDiscountAndPoints", method:"POST", jsonData:data, maskBody:win.getEl(), success:function (resp) {
		var result = Ext.decode(resp.responseText);
		if (result.success) {
			win.close();
			if (result.step == 3) {
				var _win = od.create("billRuleWin");
				_win.data = {preview:result.billSalerules, cache:result.bill};
				_win.show();
				com.mzf.sale.step3ShowInit(_win);
			} else {
				if (result.step == 4) {
					var _win = od.create("presentWin");
					_win.data = {preview:result.present, cache:result.bill};
					_win.show();
					com.mzf.sale.step4ShowInit(_win);
				} else {
					if (result.step == 5) {
						var _win = od.create("payWin");
						_win.data = {preview:result.bill};
						_win.show();
						com.mzf.sale.step5ShowInit(_win);
					} else {
						Ext.Msg.alert("\u63d0\u793a", "\u6ca1\u6709\u5339\u914d\u5230\u4fc3\u9500\u89c4\u5219");
					}
				}
			}
		} else {
			Ext.Msg.alert("\u63d0\u793a", result.msg);
		}
	}, failure:ajaxFailure});
}, step3Process:function (btn, evt) {
	var win = btn.refOwner;
	var tree = Ext.getCmp("billRuleTree");
	if (!win.data || !win.data.cache) {
		Ext.Msg.alert("\u63d0\u793a", "\u8868\u5355\u6570\u636e\u4e22\u5931");
		return;
	}
	var data = win.data.cache;
	var saleruleResultIds = [];
	var roots = tree.getRootNode().childNodes;
	var resultIds = [];
	for (var j = 0; j < roots.length; j++) {
		var child = roots[j];
		if (child.attributes.checked) {
			for (var m = 0; m < child.childNodes.length; m++) {
				var resultChild = child.childNodes[m];
				if (resultChild.attributes.checked) {
					saleruleResultIds.push(resultChild.resultId);
				}
			}
		}
	}
	data.saleruleResultIds = saleruleResultIds;
	Ext.Ajax.request({url:"salerule/app/appBillRulesOfDiscountAndPoints", method:"POST", jsonData:data, maskBody:win.getEl(), success:function (resp) {
		var result = Ext.decode(resp.responseText);
		if (result.success) {
			if (result.step == 4) {
				var _win = od.create("presentWin");
				_win.data = {preview:result.present, cache:result.bill};
				_win.show();
				com.mzf.sale.step4ShowInit(_win);
				win.close();
			} else {
				if (result.step == 5) {
					var _win = od.create("payWin");
					_win.data = {preview:result.bill};
					_win.show();
					com.mzf.sale.step5ShowInit(_win);
					win.close();
				} else {
					Ext.Msg.alert("\u63d0\u793a", "\u6ca1\u6709\u5339\u914d\u5230\u4fc3\u9500\u89c4\u5219");
				}
			}
		} else {
			Ext.Msg.alert("\u63d0\u793a", result.msg);
		}
	}, failure:ajaxFailure});
}, step4Process:function (btn, evt) {
	var win = btn.refOwner;
	var tree = Ext.getCmp("presentTree");
	var panel = Ext.getCmp("mainPanel");
	var curGrid = panel.grid;
	if (!win.data || !win.data.cache) {
		Ext.Msg.alert("\u63d0\u793a", "\u8868\u5355\u6570\u636e\u4e22\u5931");
		return;
	}
	var roots = tree.getRootNode().childNodes;
	var present = {};
	var products = [];
	var materials = [];
	var chits = [];
	for (var i = 0; i < roots.length; i++) {
		var root = roots[i];
		if (root.id == "productNode") {
			for (var j = 0; j < root.childNodes.length; j++) {
				var child = root.childNodes[j];
				if (curGrid.curNode == child.id) {
					child.data = curGrid.getData();
				}
				if (!Ext.isEmpty(child.data)) {
					for (var m = 0; m < child.data.length; m++) {
						products.push(child.data[m]);
					}
				}
			}
		}
		if (root.id == "materialNode") {
			for (var j = 0; j < root.childNodes.length; j++) {
				var child = root.childNodes[j];
				if (curGrid.curNode == child.id) {
					child.data = curGrid.getData();
				}
				if (!Ext.isEmpty(child.data)) {
					for (var m = 0; m < child.data.length; m++) {
						materials.push(child.data[m]);
					}
				}
			}
		}
		if (root.id == "chitNode") {
			for (var j = 0; j < root.childNodes.length; j++) {
				var child = root.childNodes[j];
				if (curGrid.curNode == child.id) {
					child.data = curGrid.getData();
				}
				if (!Ext.isEmpty(child.data)) {
					for (var m = 0; m < child.data.length; m++) {
						chits.push(child.data[m]);
					}
				}
			}
		}
		present.products = products;
		present.materials = materials;
		present.chits = chits;
	}
	var data = win.data.cache;
	data.present = present;
	Ext.Ajax.request({url:"salerule/app/addPresent", method:"POST", jsonData:data, maskBody:win.getEl(), success:function (resp) {
		var result = Ext.decode(resp.responseText);
		if (result.success) {
			if (result.step == 5) {
				var _win = od.create("payWin");
				_win.data = {preview:result.bill};
				_win.show();
				win.close();
				com.mzf.sale.step5ShowInit(_win);
			} else {
				Ext.Msg.alert("\u63d0\u793a", "\u6ca1\u6709\u5339\u914d\u5230\u4fc3\u9500\u89c4\u5219");
			}
		} else {
			Ext.Msg.alert("\u63d0\u793a", result.msg);
		}
	}, failure:ajaxFailure});
}, step5Process:function (btn, evt) {
	var win = btn.refOwner;
	
		//marketProxyWin, bankCardWin ,coBankWin,valueCardWin,foreCardWin
	if (win) {
		var form = win.get(0);
		debugger;
		if (form && form.getForm().isValid()) {
			var cacheData = win.data.preview;
			var payChitList = win.chitDetailData;
			if(!Ext.isEmpty(payChitList)){ 
				var payChitList = [];  
				for(var i = 0;i < win.chitDetailData.length;i++){
	                var chit = win.chitDetailData[i];
	                chit.targetId = chit.id;
	                chit.targetNum = chit.num;
	                chit.targetName = chit.name;
	                chit.price = chit.actualValue;
	                //cacheData.detailList.push(chit);
	                payChitList.push(chit);
				}
				cacheData.payChitList = payChitList;
			}
			var data = form.getForm().getFieldValues();
			cacheData.amount = data.amount;
			cacheData.cash = data.cash;
			cacheData.chit = data.chit;
			cacheData.marketProxy = data.marketProxy;
			cacheData.transfer = data.transfer;
			cacheData.other = data.other;
			cacheData.bankCard = data.bankCard;
			cacheData.valueCard = data.valueCard; 
			cacheData.clearDiscount = data.clearDiscount;
			
			var total = 0;
			if (cacheData.discount) {
				total += Ext.num(cacheData.discount);
			}
			if (cacheData.amount) {
				total += Ext.num(cacheData.amount);
			}
			if(data.clearDiscount){
			   //total += Ext.num(data.clearDiscount);
			}
			if (Ext.num(cacheData.amount) < 0) {
				Ext.Msg.alert('提示', '实收金额不能小于0');
				return;
			}
			 
			if (Ext.num(cacheData.totalAmount) != total) {
				Ext.Msg.alert('提示', '实收金额加折扣金额不等于应收金额，请核实');
				return;
			}
			var marketProxyWin = Ext.getCmp("marketProxyWin");
			if (marketProxyWin) {
				var value = marketProxyWin.get(0).getForm().getFieldValues(true);
				Ext.apply(cacheData, value);
			}
			var bankCardWin = Ext.getCmp("bankCardWin");
			if (bankCardWin) {
				var value = bankCardWin.get(0).getForm().getFieldValues(true);
				Ext.apply(cacheData, value);
			}
			var valueCardWin = Ext.getCmp("valueCardWin");
			if (valueCardWin) {
				var value = valueCardWin.get(0).getForm().getFieldValues(true);
				Ext.apply(cacheData, value);
			}
			 
			Ext.Ajax.request({url:"salerule/app/sale", method:"POST", jsonData:cacheData, maskBody:win.getEl(), success:function (resp) {
				var result = Ext.decode(resp.responseText);
				if (result.success) {
					win.close();
					var printWin = od.showWindow("billPrintWin");
					if (printWin) {
						printWin.preview(result.saleId);
					}
					var grid = Ext.getCmp("saleGrid");
					if (grid) {
						grid.getStore().reload({params:{start:0, limit:30}});
					}
					Ext.getCmp('newSaleWin').close();
				} else {
					Ext.Msg.alert("\u63d0\u793a", result.msg);
				}
			}, failure:ajaxFailure});
		}
	}
},showAllSalerule:function(data){
  var tree = Ext.getCmp('showRuleTree');
  if(data.singleSalerules){
    var root_ = new Ext.tree.TreeNode({text:'单品规则',leaf:false});
    var rules = data.singleSalerules;
    	for (var e in rules) {
			var root = new Ext.tree.TreeNode({text:e, leaf:false});
			for (var i = 0; i < rules[e].length; i++) {
				var rule = rules[e][i]; 
				var node = new Ext.tree.TreeNode({text:rule.name, leaf:true});
				node.ruleId = rule.id;
				for (var j = 0; j < rule.results.length; j++) {
					var result = rule.results[j];
					var checked = false;
					if (j == 0) {
						checked = true;
					}
					var child = new Ext.tree.TreeNode({text:result.resultName, leaf:true, checked:checked});
					child.resultId = result.id;
					//node.appendChild(child);
				}
				root.appendChild(node);
			}
			root_.appendChild(root);
			tree.getRootNode().appendChild(root_);
		}
		
  }
  if(data.billSalerules){
   var root_ = new Ext.tree.TreeNode({text:'整单规则',leaf:false});
   var rules = data.billSalerules;
   for (var i = 0; i < rules.length; i++) {
			var rule = rules[i]; 
			var node = new Ext.tree.TreeNode({text:rule.name, leaf:true});
			node.ruleId = rule.id;
			for (var j = 0; j < rule.results.length; j++) {
				var result = rule.results[j];
				var checked = false;
				if (j == 0) {
					checked = true;
				}
				var child = new Ext.tree.TreeNode({text:result.resultName, id:result.id, leaf:true, checked:checked});
				child.resultId = result.id;
				//node.appendChild(child);
			}
			root_.appendChild(node);
			tree.getRootNode().appendChild(root_);
		}
  }
  tree.expandAll();
}
, step2ShowInit:function (win) {
	win.createTree = function (rules) {
		var tree = Ext.getCmp("singleRuleTree");
		for (var e in rules) {
			var root = new Ext.tree.TreeNode({text:e, id:e, leaf:false});
			for (var i = 0; i < rules[e].length; i++) {
				var rule = rules[e][i];
				var node = new Ext.tree.TreeNode({text:rule.name, leaf:false, checked:true});
				for (var j = 0; j < rule.results.length; j++) {
					var result = rule.results[j];
					var checked = false;
					if (j == 0) {
						checked = true;
					}
					var child = new Ext.tree.TreeNode({text:result.resultName, leaf:true, checked:checked});
					child.resultId = result.id;
					node.appendChild(child);
				}
				root.appendChild(node);
			}
			tree.getRootNode().appendChild(root);
		}
		tree.expandAll();
	};
	if (win.data && win.data.preview) {
		win.createTree(win.data.preview);
	}
}, step3ShowInit:function (win) {
	win.createTree = function (rules) {
		var tree = Ext.getCmp("billRuleTree");
		for (var i = 0; i < rules.length; i++) {
			var rule = rules[i];
			var node = new Ext.tree.TreeNode({text:rule.name, leaf:false, checked:true});
			for (var j = 0; j < rule.results.length; j++) {
				var result = rule.results[j];
				var checked = false;
				if (j == 0) {
					checked = true;
				}
				var child = new Ext.tree.TreeNode({text:result.resultName, leaf:true, checked:checked});
				child.resultId = result.id;
				node.appendChild(child);
			}
			tree.getRootNode().appendChild(node);
		}
		tree.expandAll();
	};
	if (win.data && win.data.preview) {
		win.createTree(win.data.preview);
	}
}, step4ShowInit:function (win) {
	win.createTree = function (present) {
		var tree = Ext.getCmp("presentTree");
		if (!Ext.isEmpty(present.products)) {
			var root = new Ext.tree.TreeNode({id:"productNode", text:"\u5546\u54c1", leaf:false});
			var products = present.products; 
			for (var j = 0; j < products.length; j++) {
				var product = products[j];
				if (!Ext.isEmpty(product.products)) {
					var child = new Ext.tree.TreeNode({text:product.text, leaf:true});
					child.data = product.products;
					root.appendChild(child);
				}
			}
			tree.getRootNode().appendChild(root);
		}
		if (!Ext.isEmpty(present.materials)) {
			var root = new Ext.tree.TreeNode({id:"materialNode", text:"\u7269\u6599", leaf:false});  
			 
			var materials = present.materials;
			for (var j = 0; j < materials.length; j++) {
				var material = materials[j];
				if (!Ext.isEmpty(material.materials)) {
					var child = new Ext.tree.TreeNode({text:material.text, leaf:true});
					child.data = material.materials;
					root.appendChild(child);
				}
			}
			tree.getRootNode().appendChild(root);
		}
		 
		if (!Ext.isEmpty(present.chits)) {
			var root = new Ext.tree.TreeNode({id:"chitNode", text:"代金券", leaf:false});
			var chits = present.chits;
			for (var j = 0; j < chits.length; j++) {
				var chit = chits[j];
				if (!Ext.isEmpty(chit.chits)) {
					var child = new Ext.tree.TreeNode({text:chit.text, leaf:true});
					child.data = chit.chits;
					root.appendChild(child);
				}
			}
			tree.getRootNode().appendChild(root);
		}
	};
	if (win.data && win.data.preview) {
		win.createTree(win.data.preview);
	}
}, step5ShowInit:function (win) {
	if (!win.data || !win.data.preview) {
		Ext.Msg.alert("\u63d0\u793a", "\u672a\u627e\u5230\u6570\u636e");
		return;
	}
	var data = win.data.preview;
	var form = Ext.getCmp("salePayForm");
	if (form) {
		form.getForm().setValues(data);
	}
	var grid = Ext.getCmp("salePayDedailGrid");
	if (grid) {
		var detailList = data.detailList;
		grid.getStore().removeAll();
		var arr = [];
		if (!Ext.isEmpty(detailList)) {
			for (var j = 0; j < detailList.length; j++) {
				var record = detailList[j]; 
			    grid.addData(record);
			}
		}
	   }
	  
  
}, calc:function () {
	var items = ["frontMoney", "goldPay", "productPay", "marketProxy", "bankCard", "cash", "transfer", "valueCard", "chit", "other"];
	var amount = 0;
	for (var i = 0; i < items.length; i++) {
		var item = items[i];
		var field = Ext.getCmp(item);
		if (field && field.getValue()) {
			amount += Ext.num(field.getValue(), 0);
		}
	}
	var win = Ext.getCmp("payWin");
	var form = win.get(0);
	if (form) {
		form.getForm().setValues({amount:amount});
	}
}, addClearDiscount:function (oldValue,newValue){
   var win = Ext.getCmp('payWin');
   if (oldValue != newValue) {
    Ext.Msg.confirm('提示', '确认要抹' + newValue + '元?', function(btn) {
        if (btn == 'yes') {
            var data = win.data.preview;
            Ext.Ajax.request({
                url: 'salerule/app/addClearDiscount/' + newValue,
                method: 'POST',
                jsonData: data,
                success: function(resp) {
                    var result = Ext.decode(resp.responseText);
                    var win1 = Ext.getCmp('saleInfoWin');
                    if (result.success) {
                         win.data.preview = result.bill;
                         com.mzf.sale.step5ShowInit(win);
                    } else {
                        Ext.Msg.alert('提示', result.msg);
                    }
                },
                failure: function(resp) {
                    Ext.Msg.show({
                        title: '错误',
                        msg: 'status:' + resp.status + '<br/> statusText:' + resp.statusText,
                        buttons: Ext.Msg.OK,
                        fn: Ext.emptyFn,
                        icon: Ext.Msg.ERROR
                    });
                }
            });
        } else {
            Ext.getCmp('clearDiscount').setValue(oldValue);
        }

    });

}
}
};


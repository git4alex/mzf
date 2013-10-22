/**
 *  商品销售算法
 */

var calc = {
    // 计算（商品销售、物料销售、旧饰回收、旧金回收）的总价
    getSaleAmount: function(records, saleType) {
        var val = 0;
        for (var i = 0; i < records.length; i++) {
            var rec = records[i];
            if (rec.data.type == saleType) {
                val += Ext.num(rec.data.price, 0);
                // 旧金回收
                if (saleType == 'secondGold') {
                    val -= Ext.num(rec.data.totalDiscount, 0);
                }

            }
            // 商品销售要加上工费
            /*if (saleType == 'product') {
                val += Ext.num(rec.data.cost, 0);
            }*/

        }
        return Math.round(val);
    },

    // 销售总积分
    getSalePoint: function(records) {
        var val = 0;
        for (var i = 0; i < records.length; i++) {
            var rec = records[i];
            if (rec.data.points) {
                val += Ext.num(rec.data.points, 0);
            }
        }
        return Math.round(val);
    },
    // 折扣总金额
    getSaleDiscount: function(records) {
        var val = 0;
        for (var i = 0; i < records.length; i++) {
            var rec = records[i];
            if (rec.data.type == 'secondGold') {
                continue;
            }
            if (rec.data.totalDiscount) {
                val += Ext.num(rec.data.totalDiscount, 0);
            }
        }
        return Math.round(val);
    },
    // 预付定金总额
    getSaleFrontMoney: function(records) {
        var val = 0;
        for (var i = 0; i < records.length; i++) {
            var rec = records[i];
            if (rec.data.frontMoney) {
                val += Ext.num(rec.data.frontMoney, 0);
            }
        }
        return Math.round(val);
    },

    // 计算表单上付款总额 items：要计算的表单项['amount','valueCart']
    getSaleBillAmount: function() {
        var items = ['frontMoney', 'goldPay', 'productPay', 'marketProxy', 'bankCard', 'coBrandedCard', 'cash', 'transfer', 'valueCard', 'foreignCard', 'chit', 'other'];
        var val = 0;
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var field = Ext.getCmp(item);
            if (field && field.getValue()) {
                val += Ext.num(field.getValue(), 0);
            }
        }
        return Math.round(val);
    },

    resetAll: function() {
        var detailGrid = Ext.getCmp('saleDedailGrid');
        if (detailGrid) {
            var records = detailGrid.getStore().getRange();
            var prodAm = this.getSaleAmount(records, 'product');
            var mateAm = this.getSaleAmount(records, 'material');
            var chitAm = this.getSaleAmount(records, 'genChit');
            var secGoldAm = this.getSaleAmount(records, 'secondGold');
            var secJewelAm = this.getSaleAmount(records, 'secondJewel');
            var retChitAm = this.getSaleAmount(records, 'returnsChit');

            var points = this.getSalePoint(records);
            var discount = this.getSaleDiscount(records)
            var fmAm = this.getSaleFrontMoney(records);

            Ext.getCmp('totalAmountId').setValue(prodAm + mateAm + chitAm);
            Ext.getCmp('frontMoney').setValue(fmAm);
            Ext.getCmp('goldPay').setValue(secGoldAm);
            Ext.getCmp('productPay').setValue(secJewelAm);
            Ext.getCmp('discountId').setValue(discount);
            Ext.getCmp('pointsId').setValue(points);
            Ext.getCmp('chit').setValue(retChitAm);


            var am = this.getSaleBillAmount();
            Ext.getCmp('amountId').setValue(am);

        }
    }
};

com.mzf.common.saleSum = function() {
    return calc.resetAll();
};

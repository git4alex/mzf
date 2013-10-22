/**
 * 表单呈现方式辅助类
 */
FormUtil = function() {
	return {
		getDisType : function(disType, data) {
			switch (disType) {
				case 'hidden' :
					return {
						xtype : 'hidden'
					};
				case 'date' :
					return {
						xtype : 'datefield',
						format : 'Y-m-d'
					};
				case 'boolean' :
					return {
						xtype : 'checkbox'
					};
				case 'email' :
					return {
						xtype : 'textfield',
						vtype : 'email'
					};
				case 'number' :
					return {
						xtype : 'numberfield'
					};
				case 'string' :
					return {
						xtype : 'textfield'
					};
				case 'integer' :
					return {
						xtype : 'numberfield',
						allowNegative : false,
						decimalPrecision : 0
					};
				case 'textarea' :
					return {
						xtype : 'textarea'
					};
				case 'combo' :
					return {
						xtype : 'combo',
						typeAhead : true,
						store : {
							xtype : 'jsonstore',
							fields : ['text', 'value'],
							data : data
						},
						displayField : 'text',
						valueField : 'value',
						editable : false,
						triggerAction : 'all',
						mode : 'local'
					};
				default :
					return {
						xtype : 'textfield'
					};
			}
		},

		getGridCol : function(type, data) {
			switch (type) {
				case 'date' :
					return {
						xtype : 'datecolumn',
						format : 'Y-m-d'
					};
				case 'boolean' :
					return {
						xtype : 'booleancolumn',
						trueText : '是',
						falseText : '否',
						align : 'center'
					};
				case 'number' :
					return {
						xtype : 'numbercolumn',
						align : 'right'
					};
				case 'integer' :
					return {
						xtype : 'numbercolumn',
						format : '0,000',
						align : 'right'
					};
				case 'combo' :
					return {
						renderer : function(val) {
							var items = data;
							if (items == undefined)
								return val;
							for (i = 0; i < items.length; i++) {
								var item = items[i];
								if (item.value == val) {
									return item.text;
								}
							};
							return val;
						}
					};

			}
		}
	};
}();
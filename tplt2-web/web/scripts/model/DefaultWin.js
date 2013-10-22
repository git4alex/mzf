DefaultWin = Ext.extend(Ext.Window, {
			width : 450,
			height : 300,
			layout : 'fit',
			// draggable : false,
			// resizable : false,
			modal : true,
			submitHandler : null,
			resetHandler : null,
			cancelHandler : null,
			form : null,
			initComponent : function() {
				var btns = [];
				if (this.submitHandler)
					btns.push({
								text : '提交',
								xtype : 'button',
								scope : this.scope,
								handler : this.submitHandler
							});

				if (this.resetHandler)
					btns.push({
								text : '重置',
								xtype : 'button',
								scope : this.scope,
								handler : this.resetHandler
							});

				btns.push({
							text : '取消',
							xtype : 'button',
							scope : this,
							handler : this.cancelHandler || function() {
								this.close();
							}
						});
				this.buttons = this.buttons || btns;
				if (this.form != null)
					this.items = [this.form];
				DefaultWin.superclass.initComponent.call(this);
			}
		});

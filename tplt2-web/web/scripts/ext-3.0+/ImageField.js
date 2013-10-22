Ext.namespace('Ext.ux.form');

/**
 * Example class: var imageField = new Ext.ux.form.ImageField({ fieldLabel :
 * 'Avatar', name: 'avatar', dataUrl:'image.json', fields:['name', 'url'],
 * msgTarget: 'title', browserWidth: 500, windowConfig: { cls: 'images-view' },
 * id: 'avatarimage'}); Json: {xtype:'imagefield',fieldLabel : 'Avatar', name:
 * 'avatar', dataUrl:'image.json', fields:['name', 'url'], msgTarget: 'title',
 * browserWidth: 500, windowConfig: { cls: 'images-view' }, id: 'avatarimage' }
 * 
 */

/**
 * @class Ext.form.ImageField
 * @extends Ext.BoxComponent Class for form image fields that provides event
 *          handling value handling and other functionality.
 * @constructor Creates a new ImageField
 * @param {Object}
 *            config Configuration options
 */
Ext.ux.form.ImageField = Ext.extend(Ext.form.TriggerField, {

	inputType : 'text',
	/**
	 * @cfg {Mixed} value A value to initialize this field with (defaults to
	 *      '').
	 */
	value : '',
	/**
	 * @cfg {String} name The field's HTML name attribute (defaults to "").
	 */
	name : '',
	editable:false,
	msgTarget : 'qtip',
	/**
	 * @cfg {String} msgFx <b>Experimental</b> The effect used when displaying
	 *      a validation message under the field (defaults to 'normal').
	 */
	msgFx : 'normal',
	/**
	 * @cfg {Boolean} disabled True to disable the field (defaults to false).
	 */
	disabled : false,
	/**
	 * @cfg {Boolean} optional True allow the image field to not have a value
	 *      (value == '') Set this to true when the image field is not required
	 *      to be specified (defaults to false)
	 */
	optional : false,

	/**
	 * @cfg {String} triggerClass A CSS class to apply to the trigger
	 */
	triggerClass : 'x-form-image-trigger',
	/**
	 * @cfg {String} defaultImage The default image to display in the field
	 *      (default to Ext.BLANK_IMAGE_URL)
	 */
	defaultImage : Ext.BLANK_IMAGE_URL,
	/**
	 * @cfg {Number} browserWidth The width of the image browser window
	 */
	browserWidth : 350,
	/**
	 * @cfg {Number} browserHeight The height of the image browser window
	 */
	browserHeight : 250,
	/**
	 * @cfg {String} browserTitle The title of the image browser window
	 */
	browserTitle : '请选择图标',

	/**
	 * @cfg {Object} windowConfig Additional configuration for the image browser
	 *      window
	 */
	windowConfig : {},
	/**
	 * @cfg {Object} view The {Ext.DataView} of the image browser
	 */
	view : {},
	/**
	 * @cfg {String} valueField The data store field to return as the field's
	 *      value
	 */
	valueField : 'name',
	isFormField : true,
	// Private
	isStoreLoaded : false,
	// private
	isFormField : true,
	// Private
	selections : [],
	// Private
	selectedRecords : [],
	// dataUrl of store
	dataUrl : '',
	// field of store
	fields : [],

	initComponent : function() {
		Ext.ux.form.ImageField.superclass.initComponent.call(this);
		this.addEvents(
				/**
				 * @event expand Fires when the image browser is expanded
				 * @param {Ext.ux.form.ImageField}
				 *            this
				 * @param {Ext.DataView}
				 *            view The Ext.DataView of the image browser
				 */
				'expand',
				/**
				 * @event collapse Fires when the image browser is collapsed
				 * @param {Ext.ux.form.ImageField}
				 *            this
				 * @param {Ext.DataView}
				 *            view The Ext.DataView of the image browser
				 */
				'collapse');
		this.initTemplates();
		if (this.dataUrl != '' && this.dataUrl != null)
			this.getStore();

		// create the DataView
		this.view = new Ext.DataView({
					store : this.store,
					tpl : this.thumbTemplate,
					autoHeight : true,
					autoWidth : true,
					overClass : 'x-view-over',
					itemSelector : 'div.thumb-wrap',
					emptyText : 'No images to display',
					loadingText : 'Loading...',
					singleSelect : true
				});

		// if store was auto loaded, mark it as loaded
		if (this.view.store.autoLoad) {
			this.isStoreLoaded = true;
		}

	},

	getStore : function() {
		this.store = new Ext.data.JsonStore({
					url : this.dataUrl,
					root : 'images',
					fields : this.fields
				});
	},

	getSelectedRecords : function() {
		this.selections = this.view.getSelectedIndexes();
		this.selectedRecords = this.view.getSelectedRecords(); 
		return this.selectedRecords;
	},
	/**
	 * private initialize XTemplate for dataView.
	 */
	initTemplates : function() {
		this.thumbTemplate = new Ext.XTemplate(
				'<tpl for=".">',
				'<div class="thumb-wrap" id="{name}">',
				'<div class="thumb"><img src="{url}" class="{iconCls}" alt="{name}" title="{name}" /></div>',
				'</div>', '</tpl>');
		this.thumbTemplate.compile();
	},

	// private
	onSelect : function() {
		var selectedRecords = '';
		var returnValue = (this.getSelectedRecords().length > 0)
				? this.selectedRecords[0].get(this.valueField)
				: ''; 
		if (returnValue !== this.value) {
			this.setValue(returnValue);
		} 
		this.window.hide();
	},

	/**
	 * The function that should handle the trigger's click event. This method
	 * does nothing by default until overridden by an implementing function.
	 * 
	 * @method
	 * @param {EventObject}
	 *            e
	 */
	onTriggerClick : function(e) {
		if (this.disabled) {
			return;
		}
		// load the data store
		if (!this.isStoreLoaded) {
			this.view.store.load();
			this.isStoreLoaded = true;
		} else if (this.alwaysLoadStore === true) {
			this.view.store.reload();
		}
		// setup window with forced config
		this.windowConfig = Ext.apply(this.windowConfig, {
					title : this.browserTitle,
					width : this.browserWidth,
					height : this.browserHeight,
					draggable : false,
					resizable : false,
					closable : false,
					autoScroll : true,
					cls : 'images-view',
					layout : 'fit',
					bbar : ['->',{
								text : '取消',
								handler : function() {
									this.view.clearSelections();
									this.window.hide();
									this.fireEvent('collapse', this, this.view);
								},
								scope : this
							},  {
								text : '选定',
								handler : this.onSelect,
								scope : this
							}],
					items : this.view
				}, {
					shadow : false,
					frame : true
				});
		// create the image browser window
		if (!this.window) {
			this.window = new Ext.Window(this.windowConfig);
			this.window.setPagePosition(this.trigger.getRight(), this.trigger
							.getTop());
			this.window.on('deactivate', this.onActivate, this);
			this.view.on('dblclick', this.onSelect, this);
		}
		// show the image browser window
		this.window.show();
		this.fireEvent('expand', this, this.view);
	},
	onActivate : function(e) {
		if (this.window)
			this.window.hide();
	},
	// private
	onDestroy : function() {
		if (this.trigger) {
			this.trigger.removeAllListeners();
			this.trigger.remove();
		}
		this.wrap.remove();
		Ext.ux.form.ImageField.superclass.onDestroy.call(this);
	}

});

Ext.reg('imagefield', Ext.ux.form.ImageField);
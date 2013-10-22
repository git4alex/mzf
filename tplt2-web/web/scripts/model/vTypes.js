
/** 身份证验证 */

Ext.form.VTypes['chinaIdVal'] = /^\d{15}(\d{2}[a-zA-Z0-9])?$/;

Ext.form.VTypes['chinaId'] = function(sId) {
	return Ext.form.VTypes['chinaIdVal'].test(sId);
}
Ext.form.VTypes['chinaIdMask'] = /^[\dxX]*$/;

Ext.form.VTypes['chinaIdText'] = '非法的身份证号码。';

/** 数字验证 */
Ext.form.VTypes['numberVal'] = /^[\d]*$/;

Ext.form.VTypes['number'] = function(v) {
	return Ext.form.VTypes['numberVal'].test(v);
}
Ext.form.VTypes['numberMask'] = /^[0-9;]*$/;

Ext.form.VTypes['numberText'] = '必须输入数字。';

/** 只能输入“a-z 0-9 _” */
Ext.form.VTypes['char_1Val'] = /^[0-9a-z][\w-]*[0-9a-z]$/i;

Ext.form.VTypes['char_1'] = function(v) {
	return Ext.form.VTypes['char_1Val'].test(v);
}
Ext.form.VTypes['char_1Mask'] = /^[0-9a-z\w-]$/i;

Ext.form.VTypes['char_1Text'] = '必须由不区分大小写的“a-z _ 0-9 ”中的字符组成，且“_” 不能为第一位或最后一位。';

/** 只能输入“a-z 0-9 中文 ” */
Ext.form.VTypes['char_2Val'] = /^[0-9a-z\u4E00-\u9FA5]*$/i;

Ext.form.VTypes['char_2'] = function(v) {
	return Ext.form.VTypes['char_2Val'].test(v);
}
Ext.form.VTypes['char_2Mask'] = /^[0-9a-z\u4E00-\u9FA5]$/i;

Ext.form.VTypes['char_2Text'] = '必须由不区分大小写的“a-z 0-9  中文 ”中的字符组成。';

/** 电话号码验证 */
Ext.form.VTypes['phoneVal'] = /^[\d-]*$/;

Ext.form.VTypes['phone'] = function(v) {
	return Ext.form.VTypes['phoneVal'].test(v);
}
Ext.form.VTypes['phoneMask'] = /^[\d-]*$/;

Ext.form.VTypes['phoneText'] = '电话号码必须是数字或者‘-’。';

/** 手机号码验证 */
Ext.form.VTypes['mobileVal'] = /^1\d{10}$|^0\d{10,11}$/;

Ext.form.VTypes['mobile'] = function(sId) {
	return Ext.form.VTypes['mobileVal'].test(sId);
}
Ext.form.VTypes['mobileMask'] = /^[0-9;]*$/;

Ext.form.VTypes['mobileText'] = '非法的手机号码。';

/** 手机列表验证 */
Ext.form.VTypes['mobileListVal'] = /^[0-9;]*$/;

Ext.form.VTypes['mobileList'] = function(v) {
	return Ext.form.VTypes['mobileListVal'].test(v);
}
Ext.form.VTypes['mobileListMask'] = /^[0-9;]*$/;

Ext.form.VTypes['mobileListText'] = '有手机号码输入错误,手机号码是数字，并且用分号分割。';

Ext.apply(Ext.form.VTypes, {
			confirmPwd : function(val, field) {
				if (field.confirmPwd) {
					var firstPwdId = field.confirmPwd.first;
					var secondPwdId = field.confirmPwd.second;
					this.firstField = Ext.getCmp(firstPwdId);
					this.secondField = Ext.getCmp(secondPwdId);
					var firstPwd = this.firstField.getValue();
					var secondPwd = this.secondField.getValue();
					if (firstPwd == secondPwd) {
						return true;
					} else {
						return false;
					}
				}
			},
			confirmPwdText : '两次输入的密码不一致!'
		});

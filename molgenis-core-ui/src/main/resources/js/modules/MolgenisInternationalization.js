define(function(require, exports, module) {
	/**
	 * @module MolgenisInternationalization
	 */

	var i18n = {};

	i18n.get = function(str, lang) {
		lang = typeof lang !== 'undefined' ? lang : 'en';
		var i18nObj;

		if (str && (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}')) {
			i18nObj = {
				'en' : str
			};
		} else {
			i18nObj = JSON.parse(str ? str : '{}');
		}

		return i18nObj[lang];
	};

	i18n.getAll = function(str, lang) {
		lang = typeof lang !== 'undefined' ? lang : 'en';
		var i18nObj;
		if (str && (str.charAt(0) !== '{' || str.charAt(str.length - 1) !== '}')) {
			i18nObj = {
				'en' : str
			};
		} else {
			i18nObj = JSON.parse(str ? str : '{}');
		}
		return i18nObj;
	};

	i18n.I18nStrings = function(callback) {
		if (!i18n.i18nStrings) {
			$.ajax({
				type : 'GET',
				url : '/api/v2/i18n',
				contentType : 'application/json',
				async : true,
				success : function(data) {
					i18n.i18nStrings = data;
					callback(data);
				}
			});
		} else {
			callback(i18n.i18nStrings);
		}
	}

	module.exports = i18n;
});
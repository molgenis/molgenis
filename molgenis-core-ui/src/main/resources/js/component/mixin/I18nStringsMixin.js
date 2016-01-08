define(function(require, exports, module) {
	"use strict";

	var I18nStringsMixin = {
		componentDidMount : function() {
			var self = this;
			I18nStrings(function(i18nStrings) {
				self.setState({
					i18nStrings : i18nStrings
				});
			});
		}
	};

	module.exports = I18nStringsMixin;
});
 (function(_, molgenis) {
	"use strict";
	
	var I18nStringsMixin = {
		componentDidMount: function() {
			var self = this;
			molgenis.I18nStrings(function(i18nStrings) {
				self.setState({i18nStrings: i18nStrings});
			});
		}
	};
	
	// export component
	molgenis.ui = molgenis.ui || {};
	molgenis.ui.mixin = molgenis.ui.mixin || {};
	_.extend(molgenis.ui.mixin, {
		I18nStringsMixin: I18nStringsMixin
	});
}(_, molgenis));
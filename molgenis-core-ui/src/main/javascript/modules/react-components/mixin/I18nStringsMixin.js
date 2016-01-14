import {I18nStrings} from '../../MolgenisInternationalization';

var I18nStringsMixin = {
	componentDidMount: function() {
		var self = this;
		I18nStrings(function(i18nStrings) {
			self.setState({
				i18nStrings : i18nStrings
			});
		});
	}
};

export default I18nStringsMixin;
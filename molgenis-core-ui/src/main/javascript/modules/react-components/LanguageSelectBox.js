(function(_, React, molgenis) {
	"use strict";
	
	var api = new molgenis.RestClient();
	
	/**
	 * Shows a Select2 box for switching the user language
	 * 
	 * @memberOf component
	 */
	var LanguageSelectBox = React.createClass({	
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'LanguageSelectBox',
		propTypes: {
			onValueChange: React.PropTypes.func
		},
		getInitialState: function() {
			return {
				select2Data: null,
				selectedLanguage: null
			};
		},
		componentDidMount: function() {
			this._loadLanguages();
		},
		render: function() {
			if (this.state.select2Data === null) {
				return molgenis.ui.Spinner();
			}
			
			if (this.state.select2Data.length > 1) {
				return molgenis.ui.wrapper.Select2({
					options: {
						data: this.state.select2Data,
					},
					value: this.state.selectedLanguage,
					name: 'languages',
					onChange: this._handleChange
				});
			}
			
			return  React.DOM.div();
		},
		_loadLanguages: function() {
			var self = this;
			api.getAsync('/api/v2/languages').done(function(languages) {
				var selectedLanguage = null;
				var select2Data = languages.items.map(function(item){
					if (item.code === languages.meta.languageCode) {
						selectedLanguage = {id: item.code, text: item.name};
					}
					return {id: item.code, text: item.name}
				});
				
				self.setState({
					select2Data: select2Data,
					selectedLanguage: selectedLanguage
				});
			});
		},
		_handleChange: function(language) {
			 $.ajax({
				 type: 'POST',
		         url:  '/plugin/useraccount/language/update',
		         data: 'languageCode=' + language.id,
		         success: function() {
		        	 location.reload(true);
		         }
			 });
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		LanguageSelectBox: React.createFactory(LanguageSelectBox)
	});
}(_, React, molgenis));
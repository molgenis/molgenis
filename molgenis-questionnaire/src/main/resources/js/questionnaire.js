(function($, molgenis) {
	"use strict";

	var QuestionnaireSelect = React.createClass({
		componentDidMount: function() {
			restApi.getAsync('/api/v1/entities', {q: {q: [field: '', operator: 'EQUALS', value: 'questionnaire']}}, function(data) {
				this.state.questionnaires = data.items;
			});
		},
		render: function() {
			if(this.state.questionnaires === undefined) {
				return div({});
			}
			
			return molgenis.control.Select2({
				options: {
					placeholder : 'Select a questionnaire',
					initSelection: function(element, callback) {
						callback(this.state.questionnaires);
					}
				},
				onChange: function(e) {
					console.log(e);
				}
			});
		}
	});
	var QuestionnaireSelectFactory = React.createFactory(QuestionnaireSelect);
	
	React.render(, $('#questionairre-select')[0]);
}($, window.top.molgenis = window.top.molgenis || {}));

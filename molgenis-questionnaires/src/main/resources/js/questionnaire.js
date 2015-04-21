(function($, molgenis) {
	"use strict";
	
	$(function() {
		var data = $('#form-holder').data();
		getForm(data.name, data.id);
		
		$('#submit-button').click(function(e) {
			$('#entity-form input[name=status]').val('SUBMITTED');
			$('#entity-form').submit();
			document.location = data.name + "/thanks";
		});
	});
	
	function getForm(name, id) {
		React.render(molgenis.ui.Questionnaire({
			entity: name,
			entityInstance: id,
			onContinueLaterClick: function() {
				history.back();
			}
		}), $('#form-holder')[0]);
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));
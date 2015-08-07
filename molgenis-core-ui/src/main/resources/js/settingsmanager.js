(function($, molgenis) {
	"use strict";

	$(function() {
		var EntitySelectBox = React.render(molgenis.ui.EntitySelectBox({
			entity: 'entities',
			query : {
				operator : 'NESTED',
				nestedRules : [
	               	{field : 'package', operator : 'EQUALS', value : 'settings'},
	               	{operator : 'AND'},
	               	{operator : 'NOT'},
	               	{field : 'abstract', operator : 'EQUALS', value : 'true'}
               	]
			},
			mode: 'view',
			multiple: false,
			placeholder: 'Select application or plugin settings',
			focus: true,
			onValueChange: function(event) {
				React.render(molgenis.ui.Form({
					entity: event.value.fullName,
					entityInstance: event.value.simpleName,
					mode: 'edit',
					modal: false,
					enableOptionalFilter: false,
					enableFormIndex: false
				}), $('#settings-container')[0]);
			}
		}), $('#settings-select-container')[0]);
	});
}($, window.top.molgenis = window.top.molgenis || {}));
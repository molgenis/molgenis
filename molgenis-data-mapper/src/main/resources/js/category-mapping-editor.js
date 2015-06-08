(function($, molgenis) {
	"use strict";

	function generateAlgorithm(mappedCategoryIds, attribute, defaultValue, nullValue) {
		var algorithm;
		if(nullValue !== undefined) {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ", " + JSON.stringify(defaultValue) + ", " + JSON.stringify(nullValue) +").value();";
		} else if(defaultValue !== undefined) {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ", " + JSON.stringify(defaultValue) + ").value();";
		} else {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ").value();";
		}
		return algorithm;
	}

	$(function() {
		// N.B. Always do this first cause it fiddles with the DOM and disrupts
		// listeners you may have placed on the table elements!
		$('#category-mapping-table').scrollTableBody({rowsToDisplay : 6});
		
		$('#save-category-mapping-btn').on('click',function() {
			var mappedCategoryIds = {}, defaultValue = undefined, nullValue = undefined, key, val;
	
			// for each source xref value, check which target xref value
			// was chosen
			$('#category-mapping-table > tbody > tr').each(function() {
				key = $(this).attr('id');
				val = $(this).find('option:selected').val();
				if ( key === 'nullValue') {
					if(val !== 'use-default-option') {
						if(val === 'use-null-value') {
							nullValue = null;
						} else {
							nullValue = val;
						}
					}
				} else {
					if(val !== 'use-default-option') {
						if(val === 'use-null-value') {
							mappedCategoryIds[$(this).attr('id')] = null;
						} else {
							mappedCategoryIds[$(this).attr('id')] = val;
						}
					}
				}
			});
			
			if(nullValue !== undefined) {
				defaultValue = null;
			}
			if($('#default-value').is(":visible")){
				defaultValue = $('#default-value').find('option:selected').val();
				if(defaultValue === 'use-null-value') {
					defaultValue = null;
				}
			}

			$.post(molgenis.getContextUrl() + '/savecategorymapping', {
				mappingProjectId : $('input[name="mappingProjectId"]').val(),
				target : $('input[name="target"]').val(),
				source : $('input[name="source"]').val(),
				targetAttribute : $('input[name="targetAttribute"]').val(),
				algorithm : generateAlgorithm(mappedCategoryIds, $('input[name="sourceAttribute"]').val(), defaultValue, nullValue),
				success : function() {
					window.history.back();
				}
			});
		});

		$('#cancel-category-mapping-btn').on('click', function() {
			bootbox.confirm("Are you sure you want to go back? All unsaved changes will be lost!", function(result) {
				if (result) {
					window.history.back();
				}
			});
		});

	});
}($, window.top.molgenis = window.top.molgenis || {}));

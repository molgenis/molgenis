(function($, molgenis) {
	"use strict";

	function generateAlgorithm(mappedCategoryIds, attribute, defaultValue, nullValue) {
		var algorithm;
		if(nullValue !== undefined) {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ", " + defaultValue + ", " + nullValue +").value();"; 			
		} else if(defaultValue !== undefined) {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ", " + defaultValue + ").value();";
		} else {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ").value();";
		}
		return algorithm;
	}

	$(function() {
		
		$('#save-category-mapping-btn').on('click',function() {
			var mappedCategoryIds = {}, mappingProjectId, target, source, targetAttribute, sourceAttribute, algorithm, defaultValue, nullValue;
	
			// for each source xref value, check which target xref value
			// was chosen
			$('#category-mapping-table > tbody > tr').each(function() {
				if ($(this).attr('id') === 'nullValue') {
					nullValue = $(this).find('option:selected').val();
					
				} else {
					if($(this).find('option:selected').val() !== 'use default') {
						if($(this).find('option:selected').val() === '') {
							mappedCategoryIds[$(this).attr('id')] = null;
						} else{
							mappedCategoryIds[$(this).attr('id')] = $(this).find('option:selected').val();
						}
					}
				}
			});
	
			mappingProjectId = $('input[name="mappingProjectId"]').val(), 
			target = $('input[name="target"]').val(), 
			source = $('input[name="source"]').val(), 
			targetAttribute = $('input[name="targetAttribute"]').val(), 
			sourceAttribute = $('input[name="sourceAttribute"]').val(),
			defaultValue = $('#default-value').find('option:selected').val();
			algorithm = generateAlgorithm(mappedCategoryIds, sourceAttribute, defaultValue, nullValue);
			
			$.post(molgenis.getContextUrl() + '/savecategorymapping', {
				mappingProjectId : mappingProjectId,
				target : target,
				source : source,
				targetAttribute : targetAttribute,
				algorithm : algorithm,
				success : function() {
					//window.history.back();
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

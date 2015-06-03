(function($, molgenis) {
	"use strict";

	function generateAlgorithm(mappedCategoryIds, attribute) {
		var mapping = '';
		var first = true;
		for (var key in mappedCategoryIds) {
			if(first) {
				mapping = mapping.concat(key + " : " + mappedCategoryIds[key]);
				first = false;
			} else {
				mapping = mapping.concat(", " + key + " : " + mappedCategoryIds[key]);
			}
		}
		return "$('" + attribute + "').map({" + mapping + "}).value();";
	}

	$(function() {
		$('#save-category-mapping-btn').on('click', function() {
			var mappedCategoryIds = {};

			// for each source xref value, check which target xref value was
			// chosen
			$('#category-mapping-table > tbody > tr').each(function() {

				// switched key value to work with algorithm map function out of
				// the box
				mappedCategoryIds[$(this).find('option:selected').val()] = $(this).attr('id');
			});

			var mappingProjectId = $('input[name="mappingProjectId"]').val();
			var target = $('input[name="target"]').val();
			var source = $('input[name="source"]').val();
			var targetAttribute = $('input[name="targetAttribute"]').val();
			var sourceAttribute = $('input[name="sourceAttribute"]').val();
			var algorithm = generateAlgorithm(mappedCategoryIds, sourceAttribute);

			$.post(molgenis.getContextUrl() + '/savecategorymapping', {
				mappingProjectId : mappingProjectId,
				target : target,
				source : source,
				targetAttribute : targetAttribute,
				algorithm : algorithm
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

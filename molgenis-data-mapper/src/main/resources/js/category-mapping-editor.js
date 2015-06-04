(function($, molgenis) {
	"use strict";

	function generateAlgorithm(mappedCategoryIds, attribute) {
		//TODO: type JSON should depend on the type of the source and target attributes and of the xref's id attribute's type
		return "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ").value();";
	}

	$(function() {
		$('#save-category-mapping-btn').on('click', function() {
			var mappedCategoryIds = {};

			// for each source xref value, check which target xref value was
			// chosen
			$('#category-mapping-table > tbody > tr').each(function() {
				mappedCategoryIds[$(this).attr('id')] = $(this).find('option:selected').val();
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
				algorithm : algorithm,
				success: function() {
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

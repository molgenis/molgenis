/**
 * Tag wizard
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";

	var relationAndTagTemplate;

	/**
	 * Function that loads the expression and tag information.
	 */
	function loadRelationAndTagContainer(relation, tags) {
		var relationAndTagInfoContainer = $('#relation-tag-info-container');

		relationAndTagInfoContainer.empty();
		relationAndTagTemplate = Handlebars.compile($("#relation-and-tag-template").html());

		relationAndTagInfoContainer.append(relationAndTagTemplate({
			'relation' : relation,
			'taglist' : tags
		}));

		$('#tag-dropdown').select2();
	}

	$(function() {
		// Tag on click listener
		$('.tag-remove-btn').on('click', function() {
			var relation = $(this).parent().prev().children().val();
			var tags = [];

			$.each($(this).parent().children(), function() {
				tags.push($(this).text());
			});

			tags.splice(tags.indexOf($(this).text()), 1);
			$(this).remove();

			loadRelationAndTagContainer(relation, tags);
		});

		// Expression on click listener
		$('.relation').on('click', function() {
			var relation = $(this).val();
			var tags = [];

			$.each($(this).parent().next().children(), function() {
				tags.push($(this).text());
			});

			loadRelationAndTagContainer(relation, tags);
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
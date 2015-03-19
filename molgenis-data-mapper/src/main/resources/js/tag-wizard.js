/**
 * Tag wizard
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";

	var expressionAndTagTemplate;

	/**
	 * Function that loads the expression and tag information.
	 */
	function loadExpressionAndTagContainer(expression, tags) {
		var expressionAndTagInfoContainer = $('#expression-tag-info-container');

		expressionAndTagInfoContainer.empty();
		expressionAndTagTemplate = Handlebars.compile($("#expression-and-tag-template").html());

		expressionAndTagInfoContainer.append(expressionAndTagTemplate({
			'expression' : expression,
			'taglist' : tags
		}));

		$('#tag-dropdown').select2();
	}

	$(function() {
		// Tag on click listener
		$('.tag-remove-btn').on('click', function() {
			var expression = $(this).parent().prev().children().val();
			var tags = [];

			$.each($(this).parent().children(), function() {
				tags.push($(this).text());
			});

			tags.splice(tags.indexOf($(this).text()), 1);
			$(this).remove();

			loadExpressionAndTagContainer(expression, tags);
		});

		// Expression on click listener
		$('.expression').on('click', function() {
			var expression = $(this).val();
			var tags = [];

			$.each($(this).parent().next().children(), function() {
				tags.push($(this).text());
			});

			loadExpressionAndTagContainer(expression, tags);
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
/**
 * Tag wizard
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";

	var relationAndTagTemplate;

	function createSelect2(inputIdentifier) {
		$('#'+inputIdentifier).select2({
			width: 500,
			minimumInputLength: 1,
			multiple: true,
			closeOnSelect: true,
			query: function(options) {
				$.ajax({
					url: 'getontologyterms?search=' + options.term + '&ontologyIds=AAAACTBSIXVJ7OHIKZFAZXQAAE',
					success: function(data){
						options.callback({results : data, more : false});
					}
				});
			},
			formatSelection: function(term) {
				return term.label;
			},
			formatResult: function(term) {
				return term.label;
			},
			id: function(term) {
				return term.IRI;
			}
		});
	}
	
	function addNewTags(relation) {
		var relationAndTagInfoContainer = $('#relation-and-tag-info-container');

		relationAndTagInfoContainer.empty();
		relationAndTagTemplate = Handlebars.compile($("#relation-and-tag-template").html());

		relationAndTagInfoContainer.append(relationAndTagTemplate({
			'relation' : relation,
		}));

		createSelect2('tag-dropdown');
	}

	$(function() {
		$('#ontology-select').select2();
		
		$('.show-tags-screen-btn').on('click', function() {
			var relation = $(this).closest('tr').find("td:first").data('relation');
			addNewTags(relation);
		});
		
		$('.remove-tag-btn').on('click', function() {
			var tagIRI =  $(this).data('tag');
			$.ajax({
				url: '/deletesingletag'
				data: tagIRI
			});
			$(this).remove();
		});

	});
}($, window.top.molgenis = window.top.molgenis || {}));
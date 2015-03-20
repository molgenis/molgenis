/**
 * Tag wizard
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";

	var selectedAttributeName;
	var relationIRI;

	function createSelect2(inputIdentifier) {
		$(inputIdentifier).select2({
			width : '100%',
			minimumInputLength : 1,
			multiple : true,
			closeOnSelect : true,
			query : function(options) {
				$.ajax({
					url : 'getontologyterms?search=' + options.term + '&ontologyIds=AAAACTBSIXVJ7OHIKZFAZXQAAE',
					success : function(data) {
						options.callback({
							results : data,
							more : false
						});
					}
				});
			},
			formatSelection : function(term) {
				return term.label;
			},
			formatResult : function(term) {
				return term.label;
			},
			id : function(term) {
				return term.IRI;
			}
		});
	}

	$(function() {
		$('#ontology-select').select2();
		createSelect2('#tag-dropdown');

		$('.edit-ontology-term-btn').on('click', function() {
			selectedAttributeName = $(this).data('attribute');
			relationIRI = $(this).data('relation');
		});
		
		$('#save-tag-selection-btn').on('click', function() {
			var entityName = $(this).data('entity');
			var attributeName = selectedAttributeName;
			var ontologyTermIRIs = $('#tag-dropdown').select2('val');
			
			$.ajax({
				url : 'tagattribute',
				type : 'POST',
				contentType : 'application/json',
				data : JSON.stringify({
					'entityName' : entityName,
					'attributeName' : attributeName,
					'relationIRI' : relationIRI,
					'ontologyTermIRIs' : ontologyTermIRIs
				}),
				
			});
		});

		$('.remove-tag-btn').on('click', function() {
			var tagIRI = $(this).data('tag');
			$.ajax({
				url : '/deletesingletag',
				data : {
					tagIRI : tagIRI
				}
			});
			$(this).remove();
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
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
	var selectedOntologyIds = [];

	function createDynamicSelectDropdown() {
		$('#tag-dropdown').select2({
			width : '100%',
			minimumInputLength : 1,
			multiple : true,
			closeOnSelect : true,
			query : function(options) {
				$.ajax({
					url : 'getontologyterms',
					type : 'POST',
					contentType : 'application/json',
					data : JSON.stringify({
						'searchTerm' : options.term,
						'ontologyIds' : selectedOntologyIds
					}),
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

		selectedOntologyIds = $('#ontology-select').select2('data');

		$('#ontology-select').on('change', function() {
			var x = $('#ontology-select').select2('data')
			for ( var item in x) {
				console.log($(item));
			}

			selectedOntologyIds = $(this).val();
			var selectedOntologyIRI = $(this).find(':selected').data('iri');
			$.ajax({
				url : 'setontologies',
				type : 'POST',
				data : {
					'selectedOntologyIRI' : selectedOntologyIRI
				},
				success : function() {
					molgenis.createAlert([ {
						'message' : 'Ontology succesfully set'
					} ], 'success');
				}
			});
		});

		createDynamicSelectDropdown();

		$('.edit-attribute-tags-btn').on('click', function() {
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
				})
			});
		});

		$('.remove-tag-btn').on('click', function() {
			var entityName = $(this).data('entity');
			var attributeName = $(this).data('attribute');
			relationIRI = $(this).data('relation');
			var ontologyTermIRI = $(this).data('tag');
			$.ajax({
				url : 'deletesingletag',
				type : 'POST',
				contentType : 'application/json',
				data : JSON.stringify({
					'entityName' : entityName,
					'attributeName' : attributeName,
					'relationIRI' : relationIRI,
					'ontologyTermIRI' : ontologyTermIRI
				})
			});
			$(this).remove();
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
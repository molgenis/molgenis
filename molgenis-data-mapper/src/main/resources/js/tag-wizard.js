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
	var entityName;

	function noOntologySelectedHandler() {
		molgenis.createAlert([ {
			'message' : 'You need atleast one ontology selected before being able to tag.'
		} ], 'warning');
	}

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
		entityName = $('#global-information').data('entity');
		noOntologySelectedHandler();

		$('#ontology-select').select2();
		$('#automatic-tag-btn').on('click', function() {
			if (selectedOntologyIds.length > 0) {
				$.ajax({
					url : 'autotagattributes',
					type : 'POST',
					contentType : 'application/json',
					data : JSON.stringify({
						'entityName' : entityName,
						'ontologyIds' : selectedOntologyIds
					}),
					success : function(data) {
						// TODO data is a Map<AttributeMetaData,
						// List<OntologyTerm>>
						// Do something nice with it
					}
				});
			} else {
				noOntologySelectedHandler();
			}
		});

		$('#clear-all-tags-btn').on('click', function() {
			bootbox.confirm("Are you sure you want to remove all tags?", function(result) {
				if (result === true) {
					$.ajax({
						url : 'clearalltags',
						type : 'POST',
						data : {
							'entityName' : entityName
						}
					});
				}
			});
		});

		$('.edit-attribute-tags-btn').on('click', function() {
			selectedAttributeName = $(this).data('attribute');
			relationIRI = $(this).data('relation');
		});

		$('#ontology-select').on('change', function() {
			selectedOntologyIds = $(this).val();
		});

		// Dynamic dropdown for selecting ontologyterms as tags
		createDynamicSelectDropdown();

		$('#save-tag-selection-btn').on('click', function() {

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
				success : function() {
					// TODO close modal
					// TODO reload page to actually show the added tag OR add it
					// via javascript
				}
			});
		});

		$('.remove-tag-btn').on('click', function() {
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
				}),
				success : function() {
					$(this).remove();
				}
			});
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
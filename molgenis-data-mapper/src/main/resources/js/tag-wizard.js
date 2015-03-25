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

	function noOntologySelectedHandler(messageType) {
		molgenis.createAlert([ {
			'message' : 'You need atleast one ontology selected before being able to tag.'
		} ], messageType);
	}

	function createNewButtonHtml(entityName, attributeName, relationIRI, ontologyTermIRIs, labelIriMap) {
		var btnHtml = '';
		var tagIRI = '';
		var tagLabel = '';
		
		$.each(labelIriMap, function(key, value) {
			tagIRI = key;
			tagLabel = value;
		});

		btnHtml += '<button '
		btnHtml += 'type="btn" ';
		btnHtml += 'class="btn btn-primary btn-xs remove-tag-btn" ';
		btnHtml += 'data-relation="' + relationIRI + '" ';
		btnHtml += 'data-attribute="' + attributeName + '" ';
		btnHtml += 'data-tag="' + tagIRI + '">';
		btnHtml += tagLabel + ' ';
		btnHtml += '<span class="glyphicon glyphicon-remove"></span>';
		btnHtml += '</button>';

		return btnHtml;
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

		$('#ontology-select').select2();

		$('#ontology-select').on('change', function() {
			if ($(this).val() === null) {
				selectedOntologyIds = []
			} else {
				selectedOntologyIds = $(this).val();
			}
		});

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
						console.log(data);
						// TODO data is a Map<AttributeMetaData,
						// List<OntologyTerm>>
						// Do something nice with it
					}
				});
			} else {
				noOntologySelectedHandler('warning');
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

		$('.edit-attribute-tags-btn').on('click', function(event) {
			if (selectedOntologyIds.length > 0) {
				selectedAttributeName = $(this).data('attribute');
				relationIRI = $(this).data('relation');
			} else {
				noOntologySelectedHandler('warning');
				event.stopPropagation();
				event.preventDefault()
			}
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
				success : function(tagLabel) {
					$('#tag-dropdown').select2('val', '');
					var btn = createNewButtonHtml(entityName, attributeName, relationIRI, ontologyTermIRIs, tagLabel);
					$('#' + attributeName + '-tag-column').append(btn);
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
				})
			});
			$(this).remove();
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
/**
 * Tag wizard
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";

	var selectedAttributeName, 
		relationIRI, 
		selectedOntologyIds = [], 
		entityName;

	function noOntologySelectedHandler(messageType) {
		molgenis.createAlert([ {
			'message' : 'You need at least one ontology selected before being able to tag.'
		} ], messageType);
	}

	function createNewButtonHtml(attributeName, tag) {
		var btnHtml = '';

		btnHtml += '<button '
		btnHtml += 'type="btn" ';
		btnHtml += 'class="btn btn-primary btn-xs remove-tag-btn" ';
		btnHtml += 'data-relation="' + tag.relationIRI + '" ';
		btnHtml += 'data-attribute="' + attributeName + '" ';
		btnHtml += 'data-tag="' + tag.ontologyTerm.IRI + '">';
		btnHtml += tag.ontologyTerm.label + ' ';
		btnHtml += '<span class="glyphicon glyphicon-remove"></span>';
		btnHtml += '</button> ';

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
					url : 'tagwizard/getontologyterms',
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

		$('#tag-mapping-table').scrollTableBody();
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
					url : 'tagwizard/autotagattributes',
					type : 'POST',
					contentType : 'application/json',
					data : JSON.stringify({
						'entityName' : entityName,
						'ontologyIds' : selectedOntologyIds
					}),
					success : function(data) {
						molgenis.createAlert([ {
							'message' : 'Automatic tagging is a success!'
						} ], 'success');
						$.each(data, function(attributeName, tags) {
							$.each(tags, function(index) {
								if(tags[index] !== null){
									$('#' + attributeName + '-tag-column').append(createNewButtonHtml(attributeName, tags[index]));
								}
							});
						});
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
						url : 'tagwizard/clearalltags',
						type : 'POST',
						data : {
							'entityName' : entityName
						},
						success : function() {
							// empty columns with class tag-column
							$('td.tag-column').empty();

							molgenis.createAlert([ {
								'message' : 'All tags have been succesfully removed!'
							} ], 'success');
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
			var attributeName = selectedAttributeName,
				ontologyTermIRIs = $('#tag-dropdown').select2('val');

			$.ajax({
				url : 'tagwizard/tagattribute',
				type : 'POST',
				contentType : 'application/json',
				data : JSON.stringify({
					'entityName' : entityName,
					'attributeName' : attributeName,
					'relationIRI' : relationIRI,
					'ontologyTermIRIs' : ontologyTermIRIs
				}),
				success : function(ontologyTag) {
					$('#tag-dropdown').select2('val', '');
					if(ontologyTag !== undefined){
						$('#' + attributeName + '-tag-column').append(createNewButtonHtml(attributeName, ontologyTag));
					}
				}
			});
		});

		$('.tag-column').on('click', '.remove-tag-btn', function() {
			var attributeName = $(this).data('attribute'),
				ontologyTermIRI = $(this).data('tag');
			relationIRI = $(this).data('relation');
			$.ajax({
				url : 'tagwizard/deletesingletag',
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
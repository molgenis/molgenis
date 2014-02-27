(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();

	var selectedEntityMetaData = null;
	var dataTable = null;
	var attributeFilters = {};
	var selectedAttributes = [];
	var searchQuery = null;
	
	molgenis.createEntityMetaTree = function(entityMetaData, selectedAttributes) {
		var container = $('#feature-selection');
		container.tree({
			'entityMetaData' : entityMetaData,
			'selectedAttributes' : selectedAttributes,
			'onAttributeSelect' : function(attribute, selected) {
				var attributes = container.tree('getSelectedAttributes');
				$(document).trigger('changeAttributeSelection', {'attributes': attributes});
			},
			'onAttributeClick' : function(attribute) {
				$(document).trigger('clickAttribute', {'attribute': attribute});
			}
		});
	};
	
	molgenis.createDataTable = function() {
		if (selectedAttributes.length > 0) {
			$('#data-table-container').table({
				'entityMetaData' : selectedEntityMetaData,
				'attributes' : selectedAttributes,
				'query' : molgenis.createEntityQuery()
			});
			dataTable = $('#data-table-container');
		} else {
			$('#data-table-container').html('No items selected');
		}
	};
	
	molgenis.createAttributeFilterDialog = function(attribute, attributeFilter) {
		var items = [];
		if (attribute.description) {
			items.push('<h3>Description</h3><p>' + attribute.description + '</p>');
		}
		items.push('<h3>Filter:</h3>');
		var applyButton = $('<input type="button" class="btn pull-left" value="Apply filter">');
		var divContainer = molgenis.createGenericAttributeFilterInput(attribute, attributeFilter, applyButton, false);
		molgenis.createSpecificAttributeFilterInput(items, divContainer, attribute, attributeFilter, applyButton, attribute.href);
	};

	molgenis.createSpecificAttributeFilterInput = function(items, divContainer, attribute, config, applyButton, attributeUri) {
		switch (attribute.fieldType) {
		case 'HTML':
		case 'MREF':
		case 'XREF':
		case 'EMAIL':
		case 'HYPERLINK':
		case 'TEXT':
		case 'STRING':
			if (divContainer.find($("[id='text_" + attribute.name + "']")).val() === "") {
				$(applyButton).prop('disabled', true);
			}
			divContainer.find($("[id='text_" + attribute.name + "']")).keyup(function(e) {
				if (divContainer.find($("[id='text_" + attribute.name + "']").val() == "")) {
					$(applyButton).prop('disabled', true);
				} else {
					$(applyButton).prop('disabled', false);
				}
			});

			applyButton.click(function() {
				var attributeFilter = {
						attribute : attribute,
						values : [ $("[id='text_" + attribute.name + "']").val() ]
					};
				$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				$('.feature-filter-dialog').dialog('close');
			});

			break;

		case 'DATE':
		case 'DATE_TIME':
			var datePickerFrom = $('<div id="from" class="input-append date" />');
			var filterFrom;

			if (config == null) {
				filterFrom = datePickerFrom
						.append($('<input id="date-feature-from"  type="text"><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
				applyButton.attr('disabled', 'disabled');
			} else {
				filterFrom = datePickerFrom
						.append($('<input id="date-feature-from"  type="text" value="'
								+ config.values[0].replace("T", "'T'")
								+ '"><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
			}

			datePickerFrom.on('changeDate', function(e) {
				$('#date-feature-to').val($('#date-feature-from').val());
				applyButton.removeAttr('disabled');
			});

			var datePickerTo = $('<div id="to" class="input-append date" />');
			var filterTo;

			if (config == null)
				filterTo = datePickerTo
						.append($('<input id="date-feature-to" type="text"><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
			else
				filterTo = datePickerTo
						.append($('<input id="date-feature-to" type="text" value="'
								+ config.values[1].replace("T", "'T'")
								+ '"><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));

			var filter = $('<span>From:<span>').after(filterFrom).after($('<span>To:</span>')).after(filterTo);
			$(".feature-filter-dialog").dialog("option", "width", 710);

			datePickerTo.on('changeDate', function(e) {
				applyButton.removeAttr('disabled');
			});

			divContainer.empty().append(filter);

			applyButton.click(function() {
				var attributeFilter = {
						attribute : attribute,
						range : true,
						values : [ $('#date-feature-from').val().replace("'T'", "T"),
							$('#date-feature-to').val().replace("'T'", "T") ]
					};
				$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				$('.feature-filter-dialog').dialog('close');
			});

			break;
		case 'LONG':
		case 'INT':
		case 'DECIMAL':
			applyButton.click(function() {
				var attributeFilter = {
					attribute : attribute,
					values : [ $("[id='from_" + attribute.name + "']").val(),
							$("[id='to_" + attribute.name + "']").val() ],
					range : true
				};
				$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				$('.feature-filter-dialog').dialog('close');
			});
			break;
		case 'BOOL':
			$(applyButton).prop('disabled', true);
			$('input[type=radio]').live('change', function() {
				$(applyButton).prop('disabled', false);
			});
			applyButton.click(function() {
				var attributeFilter = {
					attribute : attribute,
					values : [ $('input[name=bool-feature_' + attribute.name + ']:checked').val() ]
				};
				$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				$('.feature-filter-dialog').dialog('close');
			});
			break;
		case 'CATEGORICAL':
			break;
		default:
			return;

		}

		var applyButtonHolder = $('<div id="applybutton-holder" />').append(applyButton);
		if ($.isArray(divContainer)) {
			divContainer.push(applyButtonHolder);
		} else {
			divContainer.after(applyButtonHolder);
		}
		$('.feature-filter-dialog').html(items.join('')).append(divContainer);

		$('.feature-filter-dialog').dialog({
			title : attribute.label,
			dialogClass : 'ui-dialog-shadow'
		});
		$('.feature-filter-dialog').dialog('open');

		$('.feature-filter-dialog').keyup(function(e) {
			if (e.keyCode == 13) // enter
			{
				if (applyButton.attr("disabled") != "disabled") {// enter
																	// only
																	// works if
																	// button is
																	// enabled
																	// (filter
																	// input is
																	// given)
					applyButton.click();
				}
			}
			if (e.keyCode == 27)// esc
			{
				$('.feature-filter-dialog').dialog('close');
			}
		});

		if (attribute.fieldType === 'DATE') {
			$('.date').datetimepicker({
				format : 'yyyy-MM-dd',
				language : 'en',
				pickTime : false
			});
		} else if (attribute.fieldType === 'DATE_TIME') {
			$('.date').datetimepicker({
				format : "yyyy-MM-dd'T'hh:mm:ss" + getCurrentTimezoneOffset(),
				language : 'en',
				pickTime : true
			});
		}
	};

	// Generic part for filter fields
	molgenis.createGenericAttributeFilterInput = function(attribute, attributeFilter, applyButton, wizard) {
		var attributeUri = attribute.href;
		var divContainer = $('<div />');
		var filter = null;
		switch (attribute.fieldType) {
		case 'HTML':
		case 'MREF':
		case 'XREF':
		case 'EMAIL':
		case 'HYPERLINK':
		case 'TEXT':
		case 'STRING':
			var attrs = {
				'id': 'text_' + attribute.name,
				'placeholder': 'filter text',
				'autofocus': 'autofocus'
			};
			var val = attributeFilter ? attributeFilter.values[0] : undefined;
			filter = createInput(attribute.fieldType, attrs, val);
			
			divContainer.append(filter);
			if (wizard) {
				$("[id='text_" + attribute.name + "']").change(function() {
					var attributeFilter = {
						attribute : attribute,
						values : [ $(this).val() ]
					};
					$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				});
			}
			break;
		case 'DATE':
		case 'DATE_TIME':
			var valFrom = attributeFilter ? attributeFilter.values[0].replace("T", "'T'") : undefined;
			var filterFrom = createInput(attribute.fieldType, {'id': 'from_' + attribute.name}, valFrom);
			
			var valTo = attributeFilter ? attributeFilter.values[1].replace("T", "'T'") : undefined;
			var filterTo = createInput(attribute.fieldType, {'id': 'to_' + attribute.name}, valTo);

			filter = $('<span>From:<span>').after(filterFrom).after($('<span>To:</span>')).after(filterTo);
			divContainer.append(filter);
			break;
		case 'LONG':
		case 'INT':
		case 'DECIMAL':
			var valFrom = attributeFilter ? attributeFilter.values[0] : undefined;
			var filterFrom = createInput(attribute.fieldType, {'id': 'from_' + attribute.name}, valFrom);
			
			var valTo = attributeFilter ? attributeFilter.values[1] : undefined;
			var filterTo = createInput(attribute.fieldType, {'id': 'to_' + attribute.name}, valTo);

			filterFrom.on('keyup input', function() {
				// If 'from' changed set 'to' at the same value
				$("[id='to_" + attribute.name + "']").val($("[id='from_" + attribute.name + '').val());
			});

			filter = $('<span>From:<span>').after(filterFrom).after($('<span>To:</span>')).after(filterTo);

			divContainer.append(filter);
			if (wizard) {
				divContainer.find($("[id='from_" + attribute.name + "']")).change(function() {
					var attributeFilter = {
						attribute : attribute,
						values : [ $($("[id='from_" + attribute.name + "']")).val(),
								$($("[id='to_" + attribute.name + "']")).val() ],
						range : true
					};
					$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});

				});
				divContainer.find($("[id='to_" + attribute.name + "']")).change(function() {
					var attributeFilter = {
						attribute : attribute,
						values : [ $($("[id='from_" + attribute.name + "']")).val(),
								$($("[id='to_" + attribute.name + "']")).val() ],
						range : true
					};
					$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				});
			}
			break;
		case 'BOOL':
			var groupName = 'bool-feature_' + attribute.name;
			
			var valTrue = attributeFilter ? attributeFilter.values[0] === 'true' : undefined;
			var idTrue = 'bool-feature-true_' + attribute.name;
			var filterTrue = createInput(attribute.fieldType, {'id': idTrue, 'name': groupName, 'checked': valTrue}, 'true');
			
			var valFalse = attributeFilter ? attributeFilter.values[0] === 'false' : undefined;
			var idFalse = 'bool-feature-fl_' + attribute.name;
			var filterFalse = createInput(attribute.fieldType, {'id': idFalse, 'name': groupName, 'checked': valFalse}, 'false');
			
			filter = filterTrue.after('<label for="' + idTrue + '">True</label>').after(filterFalse).after('<label for="' + idFalse + '">False</label>');
			divContainer.append(filter);
			
			if (wizard) {
				filter.find('input[name="' + groupName + '"]').change(function() {
					var attributeFilter = {
						attribute : attribute,
						values : [ $(this).val() ]
					};
					$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				});
			}
			break;
		case 'CATEGORICAL':
			var attributeMetaDataExpanded = restApi.get(attributeUri + "?expand=refEntity");
			var categoryMetaData = attributeMetaDataExpanded.refEntity;
			var labelAttribute = categoryMetaData.labelAttribute.toLowerCase();
			$('input[name="' + attribute.name + '"]:checked').remove();

			$.ajax({
				type : 'GET',
				url : categoryMetaData.href.replace(new RegExp('/meta[^/]*$'), ""),
				contentType : 'application/json',
				async : false,
				success : function(categories) {
					filter = [];
					$.each(categories.items, function() {
						var checked = attributeFilter && ($.inArray(this[labelAttribute], attributeFilter.values) > -1); 
						
						var attrs = {
								'class': 'cat-value',
								'name': attribute.name,
								'checked': checked
						};
						var input = createInput(attribute.fieldType, attrs, this[labelAttribute]);
						filter.push($('<label class="checkbox">').html(' ' + this[labelAttribute]).prepend(input));
					});
				}
			});

			divContainer.append(filter);

			if (wizard) {
				divContainer.find('input[name="' + attribute.name + '"]').click(function() {
					molgenis.updateCategoryAttributeFilter(attribute);
				});
			} else {
				if (attributeFilter && attributeFilter.values.length > 0) {
					$(applyButton).prop('disabled', false);
				} else {
					$(applyButton).prop('disabled', true);
				}
				$('.cat-value').live('change', function() {
					if ($('.cat-value:checked').length > 0) {
						applyButton.removeAttr('disabled');
					} else {
						$(applyButton).prop('disabled', true);
					}
				});

				applyButton.click(function() {
					molgenis.updateCategoryAttributeFilter(attribute);
					$('.feature-filter-dialog').dialog('close');
				});
			}
			break;
		default:
			return;
		}

		if ((attribute.fieldType === 'XREF') || (attribute.fieldType == 'MREF')) {

			restApi.getAsync(attributeUri, {
				attributes : [ 'refEntity' ],
				expand : [ 'refEntity' ]
			}, function(entityMetaData) {
				var refEntity = entityMetaData.refEntity;
				if (refEntity) {
					var refEntityName = refEntity.name;
					var refEntityAttribute = refEntity.labelAttribute;

					if (refEntityName && refEntityAttribute) {

						divContainer.find($("[id='text_" + attribute.name + "']")).autocomplete({
							source : function(request, response) {
								$.ajax({
									type : 'POST',
									url : '/api/v1/' + refEntityName + '?_method=GET',
									data : JSON.stringify({
										num : 15,
										q : [ {
											"field" : refEntityAttribute,
											"operator" : "LIKE",
											"value" : request.term
										} ]
									}),
									contentType : 'application/json',
									async : true,
									success : function(resultList) {
										response($.map(resultList.items, function(item) {
											return item[uncapitalize(refEntityAttribute)];
										}));
									}
								});
							},
							minLength : 2
						});

					}
				}

			});

		}

		if (attribute.fieldType === 'DATE') {
			var container = divContainer.find('.date').datetimepicker({
				format : 'yyyy-MM-dd',
				language : 'en',
				pickTime : false
			});

			if (wizard) {
				container.on('changeDate', function(e) {
					var attributeFilter = {
						attribute : attribute,
						range : true,
						values : [ $("[id='date-feature-from_" + attribute.name + "']").val(),
								$("[id='date-feature-to_" + attribute.name + "']").val() ]
					};
					$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				});
			}
		} else if (attribute.fieldType === 'DATE_TIME') {
			// FIXME get working for generic data explorer
			var container = divContainer.find('.date').datetimepicker({
				format : "yyyy-MM-dd'T'hh:mm:ss" + getCurrentTimezoneOffset(),
				language : 'en',
				pickTime : true
			});
			if (wizard) {
				container.on('changeDate', function(e) {
					var attributeFilter = {
						attribute : attribute,
						range : true,
						values : [ $("[id='date-feature-from_" + attribute.name + "']").val().replace("'T'", "T"),
								$("[id='date-feature-to_" + attribute.name + "']").val().replace("'T'", "T") ]
					};
					$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
				});
			}
		}

		return divContainer;
	};

	molgenis.createAttributeFilterInput = function(elements) {
		var attributes = {};
		$.each(elements, function(index, element) {
			var attributeUri = $(element).attr('data-attribute');
			attributes[attributeUri] = restApi.get(attributeUri);
		});
		
		$.each(elements, function(index, element) {
			var attributeUri = $(element).attr('data-attribute');
			
			var config = attributeFilters[attributeUri];
			var applyButton = $('<input type="button" class="btn pull-left" value="Apply filter">');
			var divContainer = molgenis
					.createGenericAttributeFilterInput(attributes[attributeUri],
							config, applyButton,true);
			var trElement = $(element).closest('tr');
			trElement.append(divContainer);
		});
	};
	
	/**
	 * Update the category attribute filter
	 */
	molgenis.updateCategoryAttributeFilter = function(attribute) {
		var attributeFilter = {
			attribute: attribute,
			name : attribute.name,
			identifier : attribute.label, // FIXME label? identifier?
			type : attribute.fieldType,
			values : $.makeArray($('input[name="' + attribute.name + '"]:checked').map(function() {
				return $(this).val();
			}))
		};
		$(document).trigger('updateAttributeFilter', {'attributeUri' : attribute.href, 'attributeFilter' : attributeFilter});
	};

	molgenis.createAttributeFiltersList = function(attributeFilters) {
		var items = [];
		$.each(attributeFilters, function(attributeUri, attributeFilter) {
			var attribute = attributeFilter.attribute;
			items.push('<p><a class="feature-filter-edit" data-href="' + attributeUri + '" href="#">'
					+ attribute.name + ' (' + attributeFilter.values.join(',')
					+ ')</a><a class="feature-filter-remove" data-href="' + attributeUri + '" href="#" title="Remove '
					+ attribute.name + ' filter" ><i class="ui-icon ui-icon-closethick"></i></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));
	};

	molgenis.createEntityQuery = function() {
		var entityCollectionRequest = {
			q : []
		};
		
		var count = 0;

		if (searchQuery) {
			if (/\S/.test(searchQuery)) {
				entityCollectionRequest.q.push({
					operator : 'SEARCH',
					value : searchQuery
				});
				count++;
			}
		}

		$.each(attributeFilters, function(attributeUri, attributeFilter) {
			if (count > 0) {
				entityCollectionRequest.q.push({
					operator : 'AND'
				});
			}
			var attribute = attributeFilter.attribute;
			$.each(attributeFilter.values, function(index, value) {
				if (attributeFilter.range) {

					// Range filter
					var rangeAnd = false;
					if ((index == 0) && (value != '')) {
						entityCollectionRequest.q.push({
							field : attribute.name,
							operator : 'GREATER_EQUAL',
							value : value
						});
						rangeAnd = true;
					}
					if (rangeAnd) {
						entityCollectionRequest.q.push({
							operator : 'AND'
						});
					}
					if ((index == 1) && (value != '')) {
						entityCollectionRequest.q.push({
							field : attribute.name,
							operator : 'LESS_EQUAL',
							value : value
						});
					}
				} else {
					if (index > 0) {
						entityCollectionRequest.q.push({
							operator : 'OR'
						});
					}
					entityCollectionRequest.q.push({
						field : attribute.name,
						operator : 'EQUALS',
						value : value
					});
				}
			});
			count++;
		});

		return entityCollectionRequest;
		
	};
	
	molgenis.createDownloadDataRequest = function() {
		var entityQuery = molgenis.createEntityQuery();
		
		var dataRequest = {
			entityName : selectedEntityMetaData.name,
			attributeNames: [],
			query : {
				rules : [entityQuery.q]
			}
		};

		var query = dataTable.table('getQuery');
		if (query && query.sort) {
			searchRequest.query.sort = query.sort;
		}
		
		$.each(selectedAttributes, function() {
			var feature = this;
			dataRequest.attributeNames.push(feature.name);
			if (feature.fieldType === 'XREF' || feature.fieldType === 'MREF')
				dataRequest.attributeNames.push("key-" + feature.name);
		});

		return dataRequest;
	};

	molgenis.createAggregatesTable = function() {
		var attributeSelect = $('<select id="selectFeature"/>');
		if(Object.keys(selectedEntityMetaData.attributes).length === 0) {
			attributeSelect.attr('disabled', 'disabled');
		} else {
			$.each(selectedEntityMetaData.attributes, function(key, attribute) {
				if(attribute.fieldType === 'BOOL' || attribute.fieldType === 'CATEGORICAL') {
					attributeSelect.append('<option value="' + attribute.href + '">' + attribute.label + '</option>');
				}
			});
			$('#feature-select').empty().append(attributeSelect);
			molgenis.updateAggregatesTable(attributeSelect.val());
			attributeSelect.chosen();
			attributeSelect.change(function() {
				molgenis.updateAggregatesTable($(this).val());
			});
		}
	};

	molgenis.updateAggregatesTable = function(attributeUri) {
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/aggregate',
			data : JSON.stringify({'attributeUri': attributeUri}),
			contentType : 'application/json',
			success : function(aggregateResult) {
				var table = $('<table />').addClass('table table-striped');
				table.append('<tr><th>Category name</th><th>Count</th></tr>');
				$.each(aggregateResult.hashCategories, function(categoryName,
						count) {
					table.append('<tr><td>' + categoryName + '</td><td>'
							+ count + '</td></tr>');
				});
				$('#aggregate-table-container').empty().append(table);
			},
			error : function(xhr) {
				molgenis.createAlert(JSON.parse(xhr.responseText).errors);
			}
		});
	};

	molgenis.filterDialog = function() {
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/filterdialog',
			data : JSON.stringify({'entityUri' : $('#dataset-select').val()}),
			contentType : 'application/json',
			success : function(result) {
				$(function() {
					var modal = $('#filter-dialog-modal').html(result);
					modal.show();
				});
			},
			error : function(xhr) {
				molgenis.createAlert(JSON.parse(xhr.responseText).errors);
			}
		});
	};

	molgenis.download = function() {
		parent.showSpinner();
		$.download(molgenis.getContextUrl() + '/download', {
			// Workaround, see http://stackoverflow.com/a/9970672
			'dataRequest' : JSON.stringify(molgenis.createDownloadDataRequest())
		});
		parent.hideSpinner();
	};

	//--BEGIN genome browser--
	molgenis.updateGenomeBrowser = function() {
		if (selectedEntityMetaData.name in genomeBrowserDataSets) {
			document.getElementById('genomebrowser').style.display = 'block';
			document.getElementById('genomebrowser').style.visibility = 'visible';
			dalliance.reset();
			var dallianceTrack = [{
				name : selectedEntityMetaData.label,
				uri : '/das/molgenis/dataset_' + selectedEntityMetaData.name + '/',
				desc : "Selected dataset",
				stylesheet_uri : '/css/selected_dataset-track.xml'
			}];
			dalliance.addTier(dallianceTrack[0]);
			$.each(genomeBrowserDataSets, function(entityName, entityLabel) {
				if (entityName != selectedEntityMetaData.name) {
					var dallianceTrack = [{
						name : entityLabel,
						uri : '/das/molgenis/dataset_' + entityName + '/',
						desc : "unselected dataset",
						stylesheet_uri : '/css/not_selected_dataset-track.xml'
					}];
					dalliance.addTier(dallianceTrack[0]);
				}
			});
		} else {
			document.getElementById('genomebrowser').style.display = 'none';
		}
	};

	molgenis.setDallianceFilter = function() {
		$.each(selectedEntityMetaData.attributes, function(key, attribute) {
			if(key === 'start_nucleotide') {
				var attributeFilter = {
					attribute : attribute,
					range : true,
					values : [ Math.floor(dalliance.viewStart).toString(), Math.floor(dalliance.viewEnd).toString() ]
				};
				$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
			} else if(key === 'chromosome') {
				var attributeFilter = {
					attribute : attribute,
					values : [ dalliance.chr ]
				};
				$(document).trigger('updateAttributeFilter', {'attributeUri' : attributeUri, 'attributeFilter' : attributeFilter});
			}
		});
	};
	//--END genome browser--

	// on document ready
	$(function() {
		$(document).on('changeEntity', function(e, entityUri) {
			// reset			
			selectedEntityMetaData = null;
			dataTable = null;
			attributeFilters = {};
			selectedAttributes = [];
			searchQuery = null;
			
			$('#feature-filters p').remove();
			$("#observationset-search").val("");
			$('#data-table-pager').empty();
			
			restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
				selectedEntityMetaData = entityMetaData;
				selectedAttributes = $.map(entityMetaData.attributes, function(attribute) {
					return attribute.fieldType !== 'COMPOUND' ? attribute : null;
				});
				
				$(document).trigger('changeAttributeSelection', {'attributes': selectedAttributes});
				molgenis.createEntityMetaTree(entityMetaData, selectedAttributes);
			});
		});
		
		$(document).on('changeAttributeSelection', function(e, data) {
			selectedAttributes = data.attributes;
			
			switch($("#tabs li.active").attr('id')) {
				case 'tab-data':
					if(dataTable)
						dataTable.table('setSelectedAttributes', data.attributes);
					else
						molgenis.createDataTable();
					molgenis.updateGenomeBrowser();
					break;
				case 'tab-aggregates':
					molgenis.updateAggregatesTable();
					break;
				case 'tab-charts':
					break;
			}
		});

		$(document).on('updateAttributeFilter', function(e, data) {
			attributeFilters[data.attributeUri] = data.attributeFilter;
			molgenis.createAttributeFiltersList(attributeFilters);
			
			switch($("#tabs li.active").attr('id')) {
				case 'tab-data':
					molgenis.createDataTable();
					
					// TODO implement elegant solution for genome browser specific code
					var filterValues = data.attributeFilter.values;
					var attribute = data.attributeFilter.attribute;
					if(attribute.name === 'start_nucleotide') dalliance.setLocation(dalliance.chr, filterValues[0], filterValues[1]);
					if(attribute.name === 'chromosome') dalliance.setLocation(filterValues[0], dalliance.viewStart, dalliance.viewEnd);
					break;
				case 'tab-aggregates':
					molgenis.updateAggregatesTable();
					break;
				case 'tab-charts':
					break;
			}
		});
		
		$(document).on('removeAttributeFilter', function(e, data) {
			delete attributeFilters[data.attributeUri];
			molgenis.createAttributeFiltersList(attributeFilters);
			
			switch($("#tabs li.active").attr('id')) {
				case 'tab-data':
					molgenis.createDataTable();
					// TODO what to do for genomebrowser?
					break;
				case 'tab-aggregates':
					molgenis.updateAggregatesTable();
					break;
				case 'tab-charts':
					break;
			}
		});
		
		$(document).on('changeEntitySearchQuery', function(e, entitySearchQuery) {
			searchQuery = entitySearchQuery;
			
			switch($("#tabs li.active").attr('id')) {
				case 'tab-data':
					if(dataTable)
						dataTable.table('setQuery', molgenis.createEntityQuery());
					else
						molgenis.createDataTable();
					break;
				case 'tab-aggregates':
					molgenis.updateAggregatesTable();
					break;
				case 'tab-charts':
					break;
			}
		});
		
		$(document).on('clickAttribute', function(e, data) {
			molgenis.createAttributeFilterDialog(data.attribute, attributeFilters[data.attribute.href]);
			
			switch($("#tabs li.active").attr('id')) {
				case 'tab-charts':
					//Clean the select boxes of the charts designer
					if (molgenis.charts) 
					{
						molgenis.charts.dataexplorer.resetChartDesigners();
					}
					break;
			}
		});
	});
	
	$(function() {
		var container = $("#plugin-container");
		
		// use chosen plugin for data set select
		$('#dataset-select').chosen();
		$('#dataset-select').change(function() {
			$(document).trigger('changeEntity', $(this).val());
		});

		$('a[data-toggle="tab"][href="#dataset-data-container"]').on('show', function(e) {
			molgenis.createDataTable();
		});
		$('a[data-toggle="tab"][href="#dataset-aggregate-container"]').on('show', function(e) {
			molgenis.createAggregatesTable();
		});

		$("#observationset-search").focus();
		
		$("#observationset-search").change(function(e) {
			$(document).trigger('changeEntitySearchQuery', $(this).val());
		});

		$('#wizard-button').click(function() {
			molgenis.filterDialog();
		});

		$('.feature-filter-dialog').dialog({
			modal : true,
			width : 500,
			autoOpen : false
		});

		$(container).on('click', '.feature-filter-edit', function(e) {
			e.preventDefault();
			var attributeFilter = attributeFilters[$(this).data('href')];
			molgenis.createAttributeFilterDialog(attributeFilter.attribute, attributeFilter);
		});
		
		$(container).on('click', '.feature-filter-remove', function(e) {
			e.preventDefault();
			$(document).trigger('removeAttributeFilter', {'attributeUri': $(this).data('href')});
		});
		
		$('#download-button').click(function() {
			molgenis.download();
		});

		$('#genomebrowser-filter-button').click(function() {
			molgenis.setDallianceFilter();
		});

		// fire event handler
		$('#dataset-select').change();
	});
}($, window.top.molgenis = window.top.molgenis || {}));
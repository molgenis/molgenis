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
	
	molgenis.openFeatureFilterDialog = function(attributeUri) {
		restApi.getAsync(attributeUri, null, function(feature) {
				var items = [];
				if (feature.description) {
					items.push('<h3>Description</h3><p>' + feature.description + '</p>');
				}
				items.push('<h3>Filter:</h3>');
				var config = attributeFilters[attributeUri];
				var applyButton = $('<input type="button" class="btn pull-left" value="Apply filter">');
				var divContainer = molgenis.createGenericFeatureField(items, feature, config, applyButton, attributeUri, false);
				
				molgenis.createSpecificFeatureField(items, divContainer, feature, config, applyButton, attributeUri);
			}
		);
	};

	//Filter dialog for one feature
	molgenis.createSpecificFeatureField = function(items, divContainer,
			attribute, config, applyButton, featureUri) {
		switch (attribute.fieldType) {
			case "HTML" :
			case "MREF" :
			case "XREF" :
			case "EMAIL" :
			case "HYPERLINK" :
			case "TEXT" :
			case "STRING" :
				if (divContainer.find(
                    $("[id='text_" + attribute.name +"']")).val() === "") {
					$(applyButton).prop('disabled', true);
				}
				divContainer.find(
                    $("[id='text_" + attribute.name + "']")).keyup(
                        function (e) {
                            if (divContainer.find(
                                $("[id='text_" + attribute.name + "']")
                                    .val() == "")) {
                                $(applyButton).prop('disabled', true);
                            } else {
                                $(applyButton).prop('disabled', false);
                            }
                        });

				applyButton.click(function() {
					molgenis.updateFeatureFilter(featureUri, {
						name : attribute.label,
						identifier : attribute.name,
						type : attribute.fieldType,
						values : [$("[id='text_" + attribute.name + "']")
								.val()]
					});
					$('.feature-filter-dialog').dialog('close');
				});

				break;

			case "DATE" :
			case "DATE_TIME" :
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

				var filter = $('<span>From:<span>').after(filterFrom).after(
						$('<span>To:</span>')).after(filterTo);
				$(".feature-filter-dialog").dialog("option", "width", 710);

				datePickerTo.on('changeDate', function(e) {
					applyButton.removeAttr('disabled');
				});

				divContainer.empty().append(filter);

				applyButton.click(function() {
					molgenis.updateFeatureFilter(featureUri,
							{
								name : attribute.label,
								identifier : attribute.name,
								type : attribute.fieldType,
								range : true,
								values : [
										$('#date-feature-from').val().replace(
												"'T'", "T"),
										$('#date-feature-to').val().replace(
												"'T'", "T")]
							});
					$('.feature-filter-dialog').dialog('close');
				});

				break;
			case "LONG" :
			case "INT" :
			case "DECIMAL" :
				applyButton.click(function() {
					molgenis.updateFeatureFilter(featureUri, {
						name : attribute.label,
						identifier : attribute.name,
						type : attribute.fieldType,
						values : [
								$("[id='from_" + attribute.name+"']")
										.val(),
								$("[id='to_" + attribute.name+"']")
										.val()],
						range : true
					});
					$('.feature-filter-dialog').dialog('close');
				});
				break;
			case "BOOL" :
				$(applyButton).prop('disabled', true);
				$('input[type=radio]').live('change', function() {
					$(applyButton).prop('disabled', false);
				});
				applyButton.click(function() {
					molgenis.updateFeatureFilter(featureUri, {
						name : attribute.label,
						identifier : attribute.name,
						type : attribute.fieldType,
						values : [$(
								'input[name=bool-feature_' + attribute.name
										+ ']:checked').val()]
					});
					$('.feature-filter-dialog').dialog('close');
				});
				break;
			case "CATEGORICAL" :
				if (config && config.values.length > 0) {
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
					molgenis.updateFeatureFilter(featureUri, {
						name : attribute.label,
						identifier : attribute.name,
						type : attribute.fieldType,
						values : $.makeArray($('.cat-value:checked').map(
								function() {
									return $(this).val();
								}))
					});
					$('.feature-filter-dialog').dialog('close');
				});
				break;
			default :
				return;
	
		}
		
		var applyButtonHolder = $('<div id="applybutton-holder" />').append(
				applyButton);
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
				if (applyButton.attr("disabled") != "disabled") {//enter only works if button is enabled (filter input is given)
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

	//Generic part for filter fields
	molgenis.createGenericFeatureField = function(items, attribute, config, applyButton, featureUri, wizard) {
		var divContainer = $('<div />');
		var filter = null;
		
		switch (attribute.fieldType) {
			case "HTML" :
			case "MREF" :
			case "XREF" :
			case "EMAIL" :
			case "HYPERLINK" :
			case "TEXT" :
			case "STRING" :
				if (config == null) {
					filter = $('<input type="text" id="text_'
							+ attribute.name + '">');
				} else {
	
					filter = $('<input type="text" id="text_'
							+ attribute.name
							+ '" placeholder="filter text" autofocus="autofocus" value="'
							+ config.values[0] + '">');
				}
				divContainer.append(filter);
				if (wizard) {
	              $("[id='text_" + attribute.name + "']").change(
							function() {
								molgenis.updateFeatureFilter(featureUri, {
									name : attribute.label,
									identifier : attribute.name,
									type : attribute.fieldType,
									values : [$(this).val()]
								});
	
							});
				}
				break;
			case "DATE" :
			case "DATE_TIME" :
				var datePickerFrom = $('<div id="from_' + attribute.name
						+ '" class="input-append date" />');
				var filterFrom;
	
				if (config == null) {
					filterFrom = datePickerFrom.append($('<input id="date-feature-from_'
									+ attribute.name
									+ '"  type="text" /><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
				} else {
					filterFrom = datePickerFrom.append($('<input id="date-feature-from_'
									+ attribute.name
									+ '"  type="text" value="'
									+ config.values[0].replace("T", "'T'")
									+ '" /><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
				}
	
				datePickerFrom.on('changeDate', function(e) {
	              $("[id='date-feature-to_" + attribute.name+"']").val(
	              		$("[id='date-feature-from_" + attribute.name+"']").val()
	              );
				});
	
				var datePickerTo = $('<div id="to_' + attribute.name
						+ '" class="input-append date" />');
				var filterTo;
	
				if (config == null)
					filterTo = datePickerTo
							.append($('<input id="date-feature-to_'
									+ attribute.name
									+ '" type="text"><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
				else
					filterTo = datePickerTo
							.append($('<input id="date-feature-to_'
									+ attribute.name
									+ '" type="text" value="'
									+ config.values[1].replace("T", "'T'")
									+ '"><span class="add-on"><i data-time-icon="icon-time" data-date-icon="icon-calendar"></i></span>'));
	
				filter = $('<span>From:<span>').after(filterFrom).after(
						$('<span>To:</span>')).after(filterTo);
				divContainer.append(filter);
				break;
			case "LONG" :
			case "INT" :
			case "DECIMAL" :
				var fromFilter;
				var toFilter;
	
				if (config == null) {
					fromFilter = $('<input id="from_' + attribute.name
							+ '" type="number" step="any" style="width:100px">');
					toFilter = $('<input id="to_' + attribute.name
							+ '" type="number" step="any" style="width:100px">');
				} else {
					fromFilter = $('<input id="from_'
							+ attribute.name
							+ '" type="number" step="any" style="width:100px" value="'
							+ config.values[0] + '">');
					toFilter = $('<input id="to_'
							+ attribute.name
							+ '" type="number" step="any" style="width:100px" value="'
							+ config.values[1] + '">');
				}
	
				fromFilter.on('keyup input', function() {
					// If 'from' changed set 'to' at the same value
					$("[id='to_" + attribute.name+"']").val(
							$("[id='from_"
													+ attribute.name + '')
									.val());
				});
	
				filter = $('<span>From:<span>').after(fromFilter).after(
						$('<span>To:</span>')).after(toFilter);
	
				divContainer.append(filter);
				if (wizard) {
					divContainer
							.find(
	                      $("[id='from_" + attribute.name +"']"))
							.change(
									function() {
	
										molgenis
												.updateFeatureFilter(
														featureUri,
														{
															name : attribute.label,
															identifier : attribute.name,
															type : attribute.fieldType,
															values : [
																	$(
	                                                                  $("[id='from_"
																							+ attribute.name + "']"))
																			.val(),
																	$(
	                                                                  $("[id='to_"
	
																							+ attribute.name + "']"))
																			.val()],
															range : true
														});
	
									});
					divContainer
							.find($("[id='to_" + attribute.name + "']"))
							.change(
									function() {
										molgenis
												.updateFeatureFilter(
														featureUri,
														{
															name : attribute.label,
															identifier : attribute.name,
															type : attribute.fieldType,
															values : [
																	$(
	                                                                  $("[id='from_"
																							+ attribute.name +"']"))
																			.val(),
																	$(
	                                                                  $("[id='to_"
	
																							+ attribute.name + "']"))
																			.val()],
															range : true
														});
									});
				}
				break;
			case "BOOL" :
				if (config == null) {
					filter = $('<label class="radio"><input type="radio" id="bool-feature-true_'
							+ attribute.name
							+ '" name="bool-feature_'
							+ attribute.name
							+ '" value="true">True</label><label class="radio"><input type="radio" id="bool-feature-fl_'
							+ attribute.name
							+ '" name="bool-feature_'
							+ attribute.name
							+ '" value="false">False</label>');
	
				} else {
					if (config.values[0] == 'true') {
						filter = $('<label class="radio"><input type="radio" id="bool-feature-true_'
								+ attribute.name
								+ '" name="bool-feature_'
								+ attribute.name
								+ '" checked value="true">True</label><label class="radio"><input type="radio" id="bool-feature-fl_'
								+ attribute.name
								+ '" name="bool-feature_'
								+ attribute.name
								+ '" value="false">False</label>');
					} else {
						filter = $('<label class="radio"><input type="radio" id="bool-feature-true_'
								+ attribute.name
								+ '" name="bool-feature_'
								+ attribute.name
								+ '" value="true">True</label><label class="radio"><input type="radio" id="bool-feature-fl_'
								+ attribute.name
								+ '" name="bool-feature_'
								+ attribute.name
								+ '" checked value="false">False</label>');
					}
				}
				divContainer.append(filter);
				if (wizard) {
					filter.find(
							'input[name="bool-feature_' + attribute.name
									+ '"]').change(function() {
						molgenis.updateFeatureFilter(featureUri, {
							name : attribute.label,
							identifier : attribute.name,
							type : attribute.fieldType,
							values : [$(this).val()]
						});
	
					});
				}
	
				break;
			case "CATEGORICAL" :
				// http://localhost:8080/api/v1/celiacsprue/meta/<attribute.name>?expand=refEntity
				// refEntity.labelAttribute is attribute voor category label dat in UI getoond wordt
				// refEntity.href min "/meta" is de href voor entity collection repsonse met alle categories 
				// for each item in items
				//     create input met value item.href en label is item.<refEntity.labelAttribute>
				
				// for OMX model the labels will display identifiers and not the valueCodes, we have to change observ.xml to display the info we want
				//     <entity name="Category" extends="Characteristic"> --> add attribute xref_label="valueCode"
				
				// reminder: we also have to fix the case at line 768!!
				$.ajax({
					type : 'POST',
					url : '/api/v1/category?_method=GET',
					data : JSON.stringify({
						q : [{
							"field" : "observableFeature_Identifier",
							"operator" : "EQUALS",
							"value" : attribute.name
						}]
					}),
					contentType : 'application/json',
					async : false,
					success : function(categories) {
						filter = [];
						$.each(categories.items,
								function() {
									var input;
									if (config
											&& ($.inArray(this.name,
													config.values) > -1)) {
										input = $('<input type="checkbox" id="'
												+ this.name + '_'
												+ attribute.name
												+ '" "class="cat-value" name="'
												+ attribute.name
												+ '" value="' + this.name
												+ '"checked>');
									} else {
										input = $('<input type="checkbox" id="'
												+ this.name + '_'
												+ attribute.name
												+ '" class="cat-value" name="'
												+ attribute.name
												+ '" value="' + this.name
												+ '">');
									}
									filter.push($('<label class="checkbox">')
											.html(' ' + this.name).prepend(
													input));
								});
					}
				});
				divContainer.append(filter);
				if (wizard) {
					divContainer.find(
							'input[name="' + attribute.name + '"]').click(
							function() {
								molgenis.updateFeatureFilter(featureUri, {
									name : attribute.label,
									identifier : attribute.name,
									type : attribute.fieldType,
									values : $.makeArray($(
											'input[name="' + attribute.name
													+ '"]:checked').map(
											function() {
												return $(this).val();
											}))
								});
							});
				}
				break;
			default :
				return;
		}
	
		if ((attribute.fieldType === 'XREF') || (attribute.fieldType == 'MREF')) {
			// FIXME get working for generic data explorer
			divContainer
					.find($("[id='text_" + attribute.name +"']"))
					.autocomplete(
							{
								source : function(request, response) {
									$
											.ajax({
												type : 'POST',
												url : '/api/v1/characteristic?_method=GET',
												data : JSON.stringify({
													num : 15,
													q : [{
														"field" : "name",
														"operator" : "LIKE",
														"value" : request.term
													}]
												}),
												contentType : 'application/json',
												async : true,
												success : function(
														characteristicList) {
													response($
															.map(
																	characteristicList.items,
																	function(
																			item) {
																		return item.name;
																	}));
												}
											});
								},
								minLength : 2
							});
		}
	
		if (attribute.fieldType === 'DATE') {
			var container = divContainer.find('.date').datetimepicker({
				format : 'yyyy-MM-dd',
				language : 'en',
				pickTime : false
			});
	
			if (wizard) {
				container
						.on(
								'changeDate',
								function(e) {
									molgenis
											.updateFeatureFilter(
													featureUri,
													{
														name : attribute.label,
														identifier : attribute.name,
														type : attribute.fieldType,
														range : true,
														values : [
																    $("[id='date-feature-from_"+ attribute.name
	                                                                    +"']")
																		.val(),
	                                                            $("[id='date-feature-to_"
																				+ attribute.name
	                                                                        +"']")
																		.val()]
													});
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
				container
						.on(
								'changeDate',
								function(e) {
									molgenis
											.updateFeatureFilter(
													featureUri,
													{
														name : attribute.label,
														identifier : attribute.name,
														type : attribute.fieldType,
														range : true,
														values : [
																$("[id='date-feature-from_" + attribute.name + "']")
	
																		.val()
																		.replace(
																				"'T'",
																				"T"),
																$("[id='date-feature-to_"+ attribute.name + "']")
																		.val()
																		.replace(
																				"'T'",
																				"T")]
													});
								});
			}
		}
	
		return divContainer;
	};

	molgenis.createFeatureFilterField = function(elements) {
		var attributes = {};
		$.each(elements, function(index, element) {
			var attributeUri = $(element).attr('data-molgenis-url');
			attributes[attributeUri] = restApi.get(attributeUri);
		});
		
		$.each(elements, function(index, element) {
			var attributeUri = $(element).attr('data-molgenis-url');
			
			var config = attributeFilters[attributeUri];
			var applyButton = $('<input type="button" class="btn pull-left" value="Apply filter">');
			var divContainer = molgenis
					.createGenericFeatureField(null, attributes[attributeUri],
							config, applyButton, attributeUri,
							true);
			var trElement = $(element).closest('tr');
			trElement.append(divContainer);
		});
	};

	molgenis.updateFeatureFilter = function(attributeUri, featureFilter) {
		restApi.getAsync(attributeUri, null, function(attribute) {
			// TODO implement elegant solution for genome browser specific code
			if(attribute.name === 'start_nucleotide') dalliance.setLocation(dalliance.chr, featureFilter.values[0], featureFilter.values[1]);
			if(attribute.name === 'chromosome') dalliance.setLocation(featureFilter.values[0], dalliance.viewStart, dalliance.viewEnd);
			attributeFilters[attributeUri] = featureFilter;
			molgenis.onFeatureFilterChange(attributeFilters);
		});
	};

	molgenis.removeFeatureFilter = function(featureUri) {
		delete attributeFilters[featureUri];
		molgenis.onFeatureFilterChange(attributeFilters);
	};

	molgenis.onFeatureFilterChange = function(featureFilters) {
		molgenis.createFeatureFilterList(featureFilters);
		molgenis.createDataTable();
		if ($('#selectFeature').val() != null) {
			molgenis.updateAggregatesTable($('#selectFeature').val());
		}
	};

	molgenis.createFeatureFilterList = function(featureFilters) {
		var items = [];
		$
				.each(
						featureFilters,
						function(featureUri, feature) {
							items
									.push('<p><a class="feature-filter-edit" data-href="'
											+ featureUri
											+ '" href="#">'
											+ feature.name
											+ ' ('
											+ feature.values.join(',')
											+ ')</a><a class="feature-filter-remove" data-href="'
											+ featureUri
											+ '" href="#" title="Remove '
											+ feature.name
											+ ' filter" ><i class="ui-icon ui-icon-closethick"></i></a></p>');
						});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));

		$('.feature-filter-edit').click(function() {
			molgenis.openFeatureFilterDialog($(this).data('href'));
			return false;
		});
		$('.feature-filter-remove').click(function() {
			molgenis.removeFeatureFilter($(this).data('href'));
			return false;
		});
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

		$.each(attributeFilters, function(featureUri, filter) {
			if (count > 0) {
				entityCollectionRequest.q.push({
					operator : 'AND'
				});
			}
			$.each(filter.values, function(index, value) {
				if (filter.range) {

					// Range filter
					var rangeAnd = false;
					if ((index == 0) && (value != '')) {
						entityCollectionRequest.q.push({
							field : filter.identifier,
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
							field : filter.identifier,
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
						field : filter.identifier,
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
				molgenis.loadAggregate($(this).val());
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
			error : function(xhr, tst, err) {
				$.log('XHR ERROR ' + XMLHttpRequest.status);
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
				molgenis.updateFeatureFilter(attribute.href, {
					name : attribute.label,
					identifier : attribute.name,
					type : attribute.fieldType,
					range : true,
					values : [Math.floor(dalliance.viewStart).toString(), Math.floor(dalliance.viewEnd).toString()]
				});
			} else if(key === 'chromosome') {
				molgenis.updateFeatureFilter(attribute.href, {
					name : attribute.label,
					identifier : attribute.name,
					type : attribute.fieldType,
					values : [dalliance.chr]
				});
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
				
		$(document).on('changeAttributeFilter', function(e, data) {
			attributeFilters = data.attributeFilters;
			
			switch($("#tabs li.active").attr('id')) {
				case 'tab-data':
					molgenis.updateDataTable();
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
			molgenis.openFeatureFilterDialog(data.attribute.href);
			
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
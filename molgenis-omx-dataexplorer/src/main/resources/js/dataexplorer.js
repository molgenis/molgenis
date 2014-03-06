(function($, molgenis) {
	"use strict";
	var self = molgenis.dataexplorer = molgenis.dataexplorer || {};
	
	// module api
	self.getSelectedEntityMeta = getSelectedEntityMeta;
	self.getSelectedAttributes = getSelectedAttributes;
	self.getEntityQuery = getEntityQuery;
	self.createFilterControls = createFilterControls;
	self.createFilters = createFilters;
	
	var restApi = new molgenis.RestClient();
	var selectedEntityMetaData = null;
	var dataTable = null;
	var attributeFilters = {};
	var selectedAttributes = [];
	var searchQuery = null;
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getSelectedEntityMeta() {
		return selectedEntityMetaData;
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getSelectedAttributes() {
		return selectedAttributes;
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function getEntityQuery() {
		return self.createEntityQuery();
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createEntityMetaTree(entityMetaData, selectedAttributes) {
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
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createDataTable() {
		if (selectedAttributes.length > 0) {
			$('#data-table-container').table({
				'entityMetaData' : selectedEntityMetaData,
				'attributes' : selectedAttributes,
				'query' : createEntityQuery()
			});
			dataTable = $('#data-table-container');
		} else {
			$('#data-table-container').html('No items selected');
		}
	};
	
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createAttributeFiltersList(attributeFilters) {
		var items = [];
		$.each(attributeFilters, function(attributeUri, attributeFilter) {
			var attribute = attributeFilter.attribute;
			var joinChars = attributeFilter.operator ? ' ' + attributeFilter.operator + ' ' : ',';
			
			items.push('<p><a class="feature-filter-edit" data-href="' + attributeUri + '" href="#">'
					+ attribute.name + ' (' + attributeFilter.values.join(joinChars)
					+ ')</a><a class="feature-filter-remove" data-href="' + attributeUri + '" href="#" title="Remove '
					+ attribute.name + ' filter" ><i class="ui-icon ui-icon-closethick"></i></a></p>');
		});
		items.push('</div>');
		$('#feature-filters').html(items.join(''));
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createEntityQuery() {
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
						var operator = attributeFilter.operator ? attributeFilter.operator : 'OR';
						entityCollectionRequest.q.push({
							operator : operator
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
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createDownloadDataRequest() {
		var entityQuery = createEntityQuery();
		
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
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createAggregatesTable() {
		function updateAggregatesTable(attributeUri) {
			console.log(attributeUri);
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/aggregate',
				data : JSON.stringify({'attributeUri': attributeUri, 'q': createEntityQuery().q}),
				contentType : 'application/json',
				success : function(aggregateResult) {
					var table = $('<table />').addClass('table table-striped');
					table.append('<tr><th>Category name</th><th>Count</th></tr>');
					$.each(aggregateResult.hashCategories, function(categoryName,
							count) {
						table.append('<tr><td>' + categoryName + '</td><td>'
								+ count + '</td></tr>');
					});
					$('#aggregate-table-container').html(table);
				},
				error : function(xhr) {
					molgenis.createAlert(JSON.parse(xhr.responseText).errors);
				}
			});
		}
		
		var attributes = molgenis.getAtomicAttributes(getSelectedAttributes(), restApi);
		var attributeSelect = $('<select id="selectFeature"/>');
		if(Object.keys(attributes).length === 0) {
			attributeSelect.attr('disabled', 'disabled');
		} else {
			$.each(attributes, function(key, attribute) {
				if(attribute.fieldType === 'BOOL' || attribute.fieldType === 'CATEGORICAL') {
					attributeSelect.append('<option value="' + attribute.href + '">' + attribute.label + '</option>');
				}
			});
			$('#feature-select').html(attributeSelect);
			if(attributeSelect.val()) {
				updateAggregatesTable(attributeSelect.val());
				attributeSelect.chosen();
				attributeSelect.change(function() {
					updateAggregatesTable($(this).val());
				});
			}
		}
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilterControls(attribute, attributeFilter) {
		var label;
		var controls = $('<div class="controls">');
		controls.data('attribute', attribute);
		
		var name = 'input-' + attribute.name + '-' + new Date().getTime();
		var values = attributeFilter ? attributeFilter.values : null;
		switch(attribute.fieldType) {
			case 'BOOL':
				label = $('<span class="control-label">' + attribute.label + '</label>');
				var attrs = {'name': name};
				var attrsTrue = values && values[0] === 'true' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var attrsFalse = values && values[0] === 'false' ? $.extend({}, attrs, {'checked': 'checked'}) : attrs;
				var inputTrue = createInput(attribute.fieldType, attrsTrue, true);
				var inputFalse = createInput(attribute.fieldType, attrsFalse, false);
				controls.append(inputTrue.addClass('inline')).append(inputFalse.addClass('inline'));
				break;
			case 'CATEGORICAL':
				label = $('<label class="control-label" for="' + name + '">' + attribute.label + '</label>');
				var entityMeta = restApi.get(attribute.refEntity.href);
				var entitiesUri = entityMeta.href.replace(new RegExp('/meta[^/]*$'), ""); // TODO do not manipulate uri

				var entities = restApi.get(entitiesUri);
				$.each(entities.items, function() {
					var attrs = {'name': name, 'id': name};
					if(values && $.inArray(this[entityMeta.labelAttribute], values) > -1)
						attrs.checked = 'checked';
					controls.append(createInput(attribute.fieldType, attrs, this[entityMeta.labelAttribute], this[entityMeta.labelAttribute]));
				});
				break;
			case 'DATE':
			case 'DATE_TIME':
				label = $('<span class="control-label">' + attribute.label + '</label>');
				var nameFrom = name + '-from', nameTo = name + '-to';
				var valFrom = values ? values[0] : undefined;
				var valTo = values ? values[1] : undefined;
				var inputFrom = $('<div class="control-group">').append(createInput(attribute.fieldType, {'name': nameFrom, 'placeholder': 'Start date'}, valFrom ? valFrom.replace("T", "'T'") : valFrom));
				var inputTo = createInput(attribute.fieldType, {'name': nameTo, 'placeholder': 'End date'}, valTo ? valTo.replace("T", "'T'") : valTo);
				controls.append(inputFrom).append(inputTo);
				break;
			case 'DECIMAL':
			case 'INT':
			case 'LONG':
				label = $('<span class="control-label">' + attribute.label + '</label>');
				var nameFrom = name + '-from', nameTo = name + '-to';
				var labelFrom = $('<label class="horizontal-inline" for="' + nameFrom + '">From</label>');
				var labelTo = $('<label class="horizontal-inline inbetween" for="' + nameTo + '">To</label>');
				var inputFrom = createInput(attribute.fieldType, {'name': nameFrom, 'id': nameFrom}, values ? values[0] : undefined).addClass('input-small');
				var inputTo = createInput(attribute.fieldType, {'name': nameTo, 'id': nameTo}, values ? values[1] : undefined).addClass('input-small');
				controls.addClass('form-inline').append(labelFrom).append(inputFrom).append(labelTo).append(inputTo);
				break;
			case 'EMAIL':
			case 'HTML':
			case 'HYPERLINK':
			case 'STRING':
			case 'TEXT':
				label = $('<label class="control-label" for="' + name + '">' + attribute.label + '</label>');
				controls.append(createInput(attribute.fieldType, {'name': name, 'id': name}, values ? values[0] : undefined)); 
				break;
			case 'MREF':
			case 'XREF':
				label = $('<label class="control-label" for="' + name + '">' + attribute.label + '</label>');
				var element = $('<div />');
				var operator = attributeFilter ? attributeFilter.operator : 'OR';
				element.xrefsearch({attribute: attribute, values: values, operator: operator});
				controls.append(element);
				break;
			case 'COMPOUND' :
			case 'ENUM':
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + dataType;
			default:
				throw 'Unknown data type: ' + dataType;			
		}
		
		return $('<div class="control-group">').append(label).append(controls);	
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function createFilters(form) {
		var filters = {};
		$(":input", form).not('[type=radio]:not(:checked)').not('[type=checkbox]:not(:checked)').each(function(){
			var value = $(this).val();
			if(value) {
				var attribute = $(this).closest('.controls').data('attribute');
				var filter = filters[attribute.href];
				if(!filter) {
					filter = {};
					filters[attribute.href] = filter;
					filter.attribute = attribute;
				}
				var values = filter.values;
				if(!values) {
					values = [];
					filter.values = values;
				}
				
				if ($(this).hasClass('operator')) {
					filter.operator = value;
				} else {
					values.push(value);
				}
			}
		});
		return Object.keys(filters).map(function (key) { return filters[key]; });	
	}
	
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function download() {
		parent.showSpinner();
		$.download(molgenis.getContextUrl() + '/download', {
			// Workaround, see http://stackoverflow.com/a/9970672
			'dataRequest' : JSON.stringify(createDownloadDataRequest())
		});
		parent.hideSpinner();
	}

	//--BEGIN genome browser--
	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function updateGenomeBrowser() {
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
	}

	/**
	 * @memberOf molgenis.dataexplorer
	 */
	function setDallianceFilter() {
		$.each(selectedEntityMetaData.attributes, function(key, attribute) {
			if(key === 'start_nucleotide') {
				var attributeFilter = {
					attribute : attribute,
					range : true,
					values : [ Math.floor(dalliance.viewStart).toString(), Math.floor(dalliance.viewEnd).toString() ]
				};
				$(document).trigger('updateAttributeFilters', {'filters': [attributeFilter]});
			} else if(key === 'chromosome') {
				var attributeFilter = {
					attribute : attribute,
					values : [ dalliance.chr ]
				};
				$(document).trigger('updateAttributeFilters', {'filters': [attributeFilter]});
			}
		});
	}
	//--END genome browser--

	/**
	 * @memberOf molgenis.dataexplorer
	 */
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
				createEntityMetaTree(entityMetaData, selectedAttributes);
				
				//Show wizard on show of dataexplorer if url param 'wizard=true' is added
				if (showWizard) {
					molgenis.dataexplorer.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
					showWizard = false;
				}
			});
		});
		
		$(document).on('changeAttributeSelection', function(e, data) {
			selectedAttributes = data.attributes;
			
			switch($("#tabs li.active").attr('id')) {
				case 'tab-data':
					if(dataTable)
						dataTable.table('setAttributes', data.attributes);
					else
						createDataTable();
					updateGenomeBrowser();
					break;
				case 'tab-aggregates':
					createAggregatesTable();
					break;
				case 'tab-charts':
					break;
			}
		});

		$(document).on('updateAttributeFilters', function(e, data) {
			$.each(data.filters, function() {
				attributeFilters[this.attribute.href] = this;
			});
			createAttributeFiltersList(attributeFilters);
			switch($("#tabs li.active").attr('id')) {
				case 'tab-data':
					createDataTable();
					
					// TODO implement elegant solution for genome browser specific code
					$.each(data.filters, function() {
						if(this.attribute.name === 'start_nucleotide') dalliance.setLocation(dalliance.chr, this.values[0], this.values[1]);
						if(this.attribute.name === 'chromosome') dalliance.setLocation(this.values[0], dalliance.viewStart, dalliance.viewEnd);
					});
					break;
				case 'tab-aggregates':
					createAggregatesTable();
					break;
				case 'tab-charts':
					break;
			}
		});
		
		$(document).on('removeAttributeFilter', function(e, data) {
			delete attributeFilters[data.attributeUri];
			createAttributeFiltersList(attributeFilters);
			
			switch($("#tabs li.active").attr('id')) {
				case 'tab-data':
					createDataTable();
					// TODO what to do for genomebrowser?
					break;
				case 'tab-aggregates':
					createAggregatesTable();
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
						dataTable.table('setQuery', createEntityQuery());
					else
						createDataTable();
					break;
				case 'tab-aggregates':
					createAggregatesTable();
					break;
				case 'tab-charts':
					break;
			}
		});
		
		$(document).on('clickAttribute', function(e, data) {
			molgenis.dataexplorer.filter.openFilterModal(data.attribute, attributeFilters[data.attribute.href]);
		});
		
		var container = $("#plugin-container");
		
		// use chosen plugin for data set select
		$('#dataset-select').chosen();
		$('#dataset-select').change(function() {
			$(document).trigger('changeEntity', $(this).val());
		});

		$('a[data-toggle="tab"][href="#dataset-data-container"]').on('show', function(e) {
			createDataTable();
		});
		$('a[data-toggle="tab"][href="#dataset-aggregate-container"]').on('show', function(e) {
			createAggregatesTable();
		});

		$("#observationset-search").focus();
		
		$("#observationset-search").change(function(e) {
			$(document).trigger('changeEntitySearchQuery', $(this).val());
		});
	
		$('#filter-wizard-btn').click(function() {
			molgenis.dataexplorer.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
		});

		$(container).on('click', '.feature-filter-edit', function(e) {
			e.preventDefault();
			var attributeFilter = attributeFilters[$(this).data('href')];
			molgenis.dataexplorer.filter.openFilterModal(attributeFilter.attribute, attributeFilter);
		});
		
		$(container).on('click', '.feature-filter-remove', function(e) {
			e.preventDefault();
			$(document).trigger('removeAttributeFilter', {'attributeUri': $(this).data('href')});
		});
		
		$('#download-button').click(function() {
			download();
		});

		$('#genomebrowser-filter-button').click(function() {
			setDallianceFilter();
		});
		
		
		// fire event handler
		$('#dataset-select').change();
	});
}($, window.top.molgenis = window.top.molgenis || {}));
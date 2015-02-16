(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();

	/**
	 * @memberOf molgenis.table
	 */
	function createTable(settings) {
		// create elements
		var items = [];
		items.push('<div class="row">');
		items.push('<div class="col-md-12">');
		if(settings.searchable) {
			items.push('<div class="row">');
			items.push('<div class="col-md-offset-3 col-md-6">');
			items.push('<div class="form-horizontal">');
			items.push('<div class="form-group">');
			items.push('<div class="col-md-12">');
			items.push('<div class="input-group">');
			items.push('<input type="text" class="form-control table-search" placeholder="Search workflows" autofocus="autofocus"/>');
			items.push('<span class="input-group-btn">');
			items.push('<button class="btn btn-default search-clear-btn" type="button"><span class="glyphicon glyphicon-remove"></span></button>');
			items.push('<button class="btn btn-default search-btn" type="button"><span class="glyphicon glyphicon-search"></span></button>');
			items.push('</span>');
			items.push('</div>');
			items.push('</div>');   
			items.push('</div>');
			items.push('</div>');
			items.push('</div>');
			items.push('</div>');	
		}
		items.push('<div class="molgenis-table-container">');
		items.push('<table class="table table-striped table-condensed molgenis-table"><thead><th></th></thead><tbody></tbody></table>');
		items.push('</div>');
		items.push('</div>');
		items.push('</div>');
		items.push('<div class="row">');
		items.push('<div class="col-md-3"><div class="molgenis-table-controls pull-left">');
		items.push('</div></div>');
		items.push('<div class="col-md-6"><div class="molgenis-table-pager"></div></div>');
		items.push('<div class="col-md-3"><div class="molgenis-table-info pull-right"></div></div>');
		items.push('</div>');
		settings.container.html(items.join(''));
		
		// add data to elements
		getTableMetaData(settings, function(attributes, refEntitiesMeta) {
			settings.colAttributes = attributes;
			settings.refEntitiesMeta = refEntitiesMeta;

			getTableData(settings, function(data) {
				createTableHeader(settings);
				createTableBody(data, settings);
				createTablePager(data, settings);
				createTableFooter(data, settings);
			});
		});
	}
	
	/**
	 * @memberOf molgenis.table
	 */
	function getTableMetaData(settings, callback) {
		if(settings.attributes && settings.attributes.length > 0) {
			var colAttributes = molgenis.getAtomicAttributes(settings.attributes, restApi);
			// get meta data for referenced entities
			var refEntitiesMeta = {};
			$.each(colAttributes, function(i, attribute) {
				if(attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL') {
					refEntitiesMeta[attribute.refEntity.href] = null;
				}
			});
	
			var dfds = [];
			$.each(refEntitiesMeta, function(entityHref) {
				dfds.push($.Deferred(function(dfd) {
					restApi.getAsync(entityHref, {'expand' : [ 'attributes' ]}, function(entityMeta) {
						refEntitiesMeta[entityHref] = entityMeta;
						dfd.resolve();
					});
				}).promise());
			});
	
			// build table after all meta data for referenced entities was loaded
			$.when.apply($, dfds).done(function() {
				// inject referenced entities meta data in attributes
				$.each(colAttributes, function(i, attribute) {
					if(attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL') {
						attribute.refEntity = refEntitiesMeta[attribute.refEntity.href];
					}
				});
				callback(colAttributes, refEntitiesMeta);
			});
		} else callback([], {});
	}

	/**
	 * @memberOf molgenis.table
	 */
	function getTableData(settings, callback) {
		// TODO do not construct uri from other uri
		var entityCollectionUri = settings.entityMetaData.href.replace("/meta", "");
		var q = $.extend({}, settings.query, {'start': settings.start, 'num': settings.maxRows, 'sort': settings.sort});
		restApi.getAsync(entityCollectionUri, {'expand' : ['workflow[name]'], 'q' : q}, function(data) {
			settings.data = data;
			callback(data);
		});
	}

	/**
	 * @memberOf molgenis.table
	 */
	function createTableHeader(settings) {
		var container = $('.molgenis-table thead', settings.container);

		var items = [];
		$.each(settings.colAttributes, function(i, attribute) {
			var header;
			if (settings.sort && settings.sort.orders[0].property === attribute.name) {
				if (settings.sort.orders[0].direction == 'ASC') {
					header = $('<th>' + attribute.label + '<span data-attribute="' + attribute.name
							+ '" class="ui-icon ui-icon-triangle-1-s down"></span></th>');
				} else {
					header = $('<th>' + attribute.label + '<span data-attribute="' + attribute.name
							+ '" class="ui-icon ui-icon-triangle-1-n up"></span></th>');
				}
			} else {
				header = $('<th>' + attribute.label + '<span data-attribute="' + attribute.name
						+ '" class="ui-icon ui-icon-triangle-2-n-s updown"></span></th>');
			}
			header.data('attr', attribute);
			items.push(header);
		});
		items.push($('<th>No. Jobs</th>'));
		items.push($('<th>Actions</th>'));
		container.html(items);
	}

	/**
	 * @memberOf molgenis.table
	 */
	function createTableBody(data, settings) {
		var container = $('.molgenis-table tbody', settings.container);

		var items = [];
		for ( var i = 0; i < data.items.length; ++i) {
			var entity = data.items[i];
			var row = $('<tr>').data('entity', entity).data('id', entity.href);
			
			$.each(settings.colAttributes, function(i, attribute) {
				var cell = $('<td>').data('id', entity.href + '/' + attribute.name);
				renderCell(cell, entity, attribute, settings);
				row.append(cell);
			});

			// FIXME fails for data sets with > 10.000 entities
			var jobs = restApi.get('/api/v1/computeui_AnalysisJob', {'attributes' : [ 'status' ], 'q' : { 'q' : [{field:'analysis',operator:'EQUALS',value: entity.identifier}]}, 'num': 10000});
			var cellValue = '';
			if(jobs.items.length > 0) {
				var jobCount = {};
				for(var j = 0; j < jobs.items.length; ++j) {
					var status = jobs.items[j].status;
					if(!jobCount.hasOwnProperty(status)) {
						jobCount[status] = 1;
					} else {
						jobCount[status] = jobCount[status] + 1;
					}
				}
				
				
				for (var key in jobCount) {
					if(cellValue.length > 0)
						cellValue += ' | ';
					cellValue += 'jobs ' + key.toLowerCase() + ' ' + jobCount[key];
				}
			}
			row.append($('<td>' + cellValue + '</td>'));
			

				
				var actions = '';
				if(entity.status === 'RUNNING')
					actions = '<a href="#" class="view-analysis-btn" data-id="' + entity.identifier + '">view</a>&nbsp<a href="#" class="stop-analysis-btn" data-id="' + entity.identifier + '">stop</a>';
				else
					actions = '<a href="#" class="view-analysis-btn" data-id="' + entity.identifier + '">view</a>';
				row.append($('<td>' + actions + '</td>'));
//			} else {
//				row.append($('<td>'));
//				row.append($('<td>'));
//				row.append($('<td><a href="#" class="view-analysis-btn" data-id="' + entity.identifier + '">view</a></td>'));
//			}
			items.push(row);
		}
		container.html(items);

		$('.show-popover').popover({trigger:'hover', placement: 'bottom', container: 'body'});
	}
	
	/**
	 * @memberOf molgenis.table.cell
	 */
	function renderCell(cell, entity, attribute, settings) {
		cell.empty();
		
		var rawValue = entity[attribute.name];				
		switch(attribute.fieldType) {
			case 'XREF':
			case 'MREF':
            case 'CATEGORICAL':
                if (rawValue) {
                	var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
                	var refAttribute = refEntity.labelAttribute;
                	var refValue = refEntity.attributes[refAttribute];
					
                	if (refValue) {
                		var refAttributeType = refValue.fieldType;
                		if (refAttributeType === 'XREF' || refAttributeType === 'MREF' || refAttributeType === 'COMPOUND') {
                			throw 'unsupported field type ' + refAttributeType;
                		}
						
                		switch(attribute.fieldType) {
							case 'CATEGORICAL':
							case 'XREF':
								var cellValue = $('<a href="#">' + formatTableCellValue(rawValue[refAttribute], refAttributeType) + '</a>'); 
								cellValue.click(function(event) {
									if(settings.cellClickHandlers && settings.cellClickHandlers[attribute.name]) {
										settings.cellClickHandlers[attribute.name](attribute, refEntity, refAttribute, rawValue);
									} else {
										// execute default cell click handler
										openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
									}
									event.stopPropagation();
								});
								cell.append(cellValue);
								break;
							case 'MREF':
								$.each(rawValue.items, function(i, rawValue) {
									var cellValuePart = $('<a href="#">' + formatTableCellValue(rawValue[refAttribute], refAttributeType) + '</a>');
									cellValuePart.click(function(event) {
										if(settings.cellClickHandlers && settings.cellClickHandlers[attribute.name]) {
											settings.cellClickHandlers[attribute.name](attribute, refEntity, refAttribute, rawValue);
										} else {
											// execute default cell click handler
											openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
										}
										event.stopPropagation();
									});
									if (i > 0)
										cell.append(',');
									cell.append(cellValuePart);
								});
								break;
							default:
								throw 'unexpected field type ' + attribute.fieldType;
                		}
                	}
                }
				break;
            case 'BOOL':
            	// FIXME refactor formatTableCellValue to accept attribute instead of attribute field type and move nillable boolean code to this function
            	var cellValuePart = $(formatTableCellValue(rawValue, attribute.fieldType));
            	if(attribute.nillable && rawValue === undefined) {
            		cellValuePart.prop('indeterminate', true);
            	}
				cell.append(cellValuePart);
            	break;
			default :
				var value = formatTableCellValue(rawValue, attribute.fieldType);
				cell.append(value);
				break;
		}
	}
	
	/**
	 * @memberOf molgenis.table
	 */
	function openRefAttributeModal(attribute, refEntity, refAttribute, refValue) {
		// create modal structure
		var modal = $('#table-ref-modal');
		if(!modal.length) {
			var items = [];
			items.push('<div class="modal" id="table-ref-modal" tabindex="-1" aria-labelledby="table-ref-modal-label" aria-hidden="true">');
			items.push('<div class="modal-dialog">');
			items.push('<div class="modal-content">');
			items.push('<div class="modal-header">');
			items.push('<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>');
			items.push('<h4 class="modal-title ref-title" id="table-ref-modal-label">Sign up</h4>');
			items.push('</div>');
			items.push('<div class="modal-body">');
			items.push('<legend class="ref-description-header"></legend>');
			items.push('<p class="ref-description"></p>');
			items.push('<legend>Data</legend>');
			items.push('<div class="ref-table"></div>');
			items.push('</div>');
			items.push('<div class="modal-footer">');
			items.push('<a href="#" class="btn btn-primary filter-apply-btn" data-dismiss="modal">Ok</a>');
			items.push('</div>');
			items.push('</div>');
			modal = $(items.join(''));
		}
		
		// inject modal data
		var refAttributes = molgenis.getAtomicAttributes(refEntity.attributes, restApi);
		
		// TODO use idAttribute once github #1400 is fixed
		// TODO remove trim() once github #1401 is fixed
		var val = refValue[refEntity.labelAttribute];
		if (typeof val.trim == 'function') {
			val = val.trim();
		}
		
		var refQuery = {
			'q' : [ {
				// TODO use idAttribute once github #1400 is fixed
				'field' : refEntity.labelAttribute, 
				'operator' : 'EQUALS',
				'value' : val
			} ]
		}; 
		$('.ref-title', modal).html(attribute.label || attribute.name);
		$('.ref-description-header', modal).html((refEntity.label || refEntity.name) + ' description');
		$('.ref-description', modal).html(refEntity.description || 'No description available');
		$('.ref-table', modal).analysisTable({'entityMetaData' : refEntity, 'attributes': refAttributes, 'query' : refQuery, 'searchable': false});
		
		// show modal
		modal.modal({'show': true});
	}
	
	/**
	 * @memberOf molgenis.table
	 */
	function createTablePager(data, settings) {
		var container = $('.molgenis-table-pager', settings.container);

		if(data.total > settings.maxRows) {
			container.pager({
				'nrItems' : data.total,
				'nrItemsPerPage' : settings.maxRows,
				'onPageChange' : function(page) {
					settings.start = page.start;
					getTableData(settings, function(data) {
						createTableBody(data, settings);
					});
				}
			});
			container.show();
		} else container.hide();
	}

	/**
	 * @memberOf molgenis.table
	 */
	function createTableFooter(data, settings) {
		var container = $('.molgenis-table-info', settings.container);
		container.html(data.total + ' item' + (data.total !== 1 ? 's' : '') + ' found');
	}
	
	$.fn.analysisTable = function(options) {
		var container = this;

		// call plugin method
		if (typeof options == 'string') {
			var args = Array.prototype.slice.call(arguments, 1);
			if (args.length === 0)
				return container.data('table')[options]();
			else if (args.length === 1)
				return container.data('table')[options](args[0]);
		}

		// create tree container
		var settings = $.extend({}, $.fn.analysisTable.defaults, options, {'container': container});

		// store tree settings
		container.off();
		container.empty();
		container.data('settings', settings);

		// plugin methods
		container.data('table', {
			'setAttributes' : function(attributes) {
				settings.attributes = attributes;
				
				// add data to elements
				getTableMetaData(settings, function(attributes, refEntitiesMeta) {
					settings.colAttributes = attributes;
					settings.refEntitiesMeta = refEntitiesMeta;
		
					getTableData(settings, function(data) {
						createTableHeader(settings);
						createTableBody(data, settings);
					});
				});
			},
			'setQuery' : function(query) {
				settings.query = query;
				settings.start = 0;
				
				getTableData(settings, function(data) {
					createTableBody(data, settings);
					createTablePager(data, settings);
					createTableFooter(data, settings);
				});
			},
			'getQuery' : function() {
				return settings.query;
			},
			'getSort' : function() {
				return settings.sort;
			}
		});

		createTable(settings, function() {
			if(settings.onInit)
				setting.onInit();
		});

		// sort column ascending/descending
		$(container).on('click', 'thead th .ui-icon', function(e) {
			e.preventDefault();
			
			var attributeName = $(this).data('attribute');
			if (settings.sort) {
				var order = settings.sort.orders[0];
				order.property = attributeName;
				order.direction = order.direction === 'ASC' ? 'DESC' : 'ASC';
			} else {
				settings.sort = {
					orders: [{
						property: attributeName,
						direction: 'ASC'
					}]
				};
			}

			var classUp = 'ui-icon-triangle-1-n up', classDown = 'ui-icon-triangle-1-s down', classUpDown = 'ui-icon-triangle-2-n-s updown';
			$('thead th .ui-icon', container).not(this).removeClass(classUp + ' ' + classDown).addClass(classUpDown);
			if (settings.sort.orders[0].direction === 'ASC') {
				$(this).removeClass(classUpDown + ' ' + classUp).addClass(classDown);
			} else {
				$(this).removeClass(classUpDown + ' ' + classDown).addClass(classUp);
			}

			getTableData(settings, function(data) {
				createTableBody(data, settings);
			});
		});
		
		function clearSearch() {
			$('.table-search', settings.container).val('');
			var query = { q : []}
			container.analysisTable('setQuery', null);
		}
		
		function performSearch() {
			var query = { q : [{
				operator : 'SEARCH',
				value : $('.table-search', settings.container).val().trim()
			}]}
			
			container.analysisTable('setQuery', query);
		}
		
		$(container).on('keyup', '.table-search', function(e) {
			switch(e.which) {
				case 13: // enter
					performSearch();
					break;
				case 27: // escape
					clearSearch();
					break;
				default:
					break;
			}
		});
		
		$(container).on('click', '.search-btn', function() {
			performSearch();
		});
		
		$(container).on('click', '.search-clear-btn', function() {
			clearSearch();
		});
		
		$(document).on('click', '.view-analysis-btn', function(e) {
			e.preventDefault();
			settings.onViewAnalysis($(this).data('id'));
		});
		
		$(document).on('click', '.stop-analysis-btn', function(e) {
			e.preventDefault();
			settings.onStopAnalysis($(this).data('id'));
		});
		
		return this;
	};

	// default tree settings
	$.fn.analysisTable.defaults = {
		'entityMetaData' : null,
		'maxRows' : 20,
		'attributes' : null,
		'query' : null,
		'searchable' : true,
		'onViewAnalysis': function(){},
		'onStopAnalysis': function(){},
		'cellClickHandlers' : null
	};
}($, window.top.molgenis = window.top.molgenis || {}));

(function($, molgenis) {
	"use strict";

	var restApi = new molgenis.RestClient();

	var self = molgenis.analysis = molgenis.analysis || {};
	self.changeAnalysis = changeAnalysis;
	
	// state
	var settings = {
		showOverview: true,
		showDetails: false,
		analysis: null
	}
	
	var timer;
	 
	/**
	 * @memberOf molgenis.analysis
	 */
	function changeAnalysis(analysisId) {
		settings.showOverview = false;
		settings.showDetails = true;
		
		restApi.getAsync('/api/v1/computeui_Analysis/' + analysisId, {'expand' : [ 'workflow' ]}, function(analysis) {
			settings.analysis = analysis;
			renderPlugin();
		});
	}
	
	/**
	 * @memberOf molgenis.analysis
	 */
	function renderPlugin() {
		if(settings.showOverview) {
			$('#analysis-overview-container').removeClass('hidden');
			$('#analysis-details-container').addClass('hidden');
		} else if(settings.showDetails) {
			$('#analysis-overview-container').addClass('hidden');
			$('#analysis-details-container').removeClass('hidden');
			renderAnalysis();
		}
	}
	
	/**
	 * @memberOf molgenis.analysis
	 */
	function renderAnalysis() {
		history.pushState(settings, '', molgenis.getContextUrl() + '/view/' + settings.analysis.identifier);
			
		// update analysis fields
		$('#analysis-name').val(settings.analysis.name);
		$('#analysis-description').val(settings.analysis.description || '');
		$('#analysis-workflow').val(settings.analysis.workflow.identifier);
		
		//Start progress polling
		startProgressPolling();
		
		// update analysis target select and table
		renderAnalysisTargets();
	}
	
	function startProgressPolling() {
		
		//Load progress script
		$.getScript(molgenis.getContextUrl() + '/' + settings.analysis.identifier + '/progress.js');
		
		if (timer) {
			clearTimeout(timer);
		}
		
		timer = setTimeout(startProgressPolling, 5000);
	}
	
	function stopProgressPolling() {
		if (timer) {
			clearTimeout(timer);
		}
	}
	
	/**
	 * @memberOf molgenis.analysis
	 */
	function addAnalysisTarget(targetHref) {
		restApi.getAsync(targetHref, {'attributes' : ['analysis'], 'expand': ['analysis[identifier]']}, function(target) {
			// FIXME fails for data sets with more than the max mref items
			var identifiers = $.map(target.analysis.items, function(analysis) {
				return analysis.identifier;
			});
			identifiers.push(settings.analysis.identifier);
			// update target analysis list
			restApi.update(targetHref + '/analysis', identifiers, {
				success: function() {
					renderAnalysisTargets();
				}
			});
		});
	}
	
	/**
	 * @memberOf molgenis.analysis
	 */
	function deleteAnalysisTarget(targetHref) {
		restApi.getAsync(targetHref, {'attributes' : ['analysis'], 'expand': ['analysis[identifier]']}, function(target) {
			// FIXME fails for data sets with more than the max mref items
			var identifiers = $.map(target.analysis.items, function(analysis) {
				// remove analysis from target
				return analysis.identifier !== settings.analysis.identifier ? analysis.identifier : null;
			});
			// update target analysis list
			restApi.update(targetHref + '/analysis', identifiers, {
				success: function() {
					renderAnalysisTargets();
				}
			});
		});
	}
	
	/**
	 * @memberOf molgenis.analysis
	 */
	function renderAnalysisTargets() {
		var targetType = settings.analysis.workflow.targetType;
		
		// FIXME fails for data sets with > 10.000 entities
		restApi.getAsync('/api/v1/' + targetType + '/meta', {'expand' : [ 'attributes' ]}, function(targetMeta) {
			var idAttrName = targetMeta.idAttribute;
			var labelAttrName = targetMeta.labelAttribute;
			
			if(settings.analysis.status === 'CREATED') {
				// update analysis targets select
				// TODO use NOT operator in query
				restApi.getAsync('/api/v1/' + targetType, {'attributes' : [ idAttrName, labelAttrName, 'analysis'], 'expand' : [ 'analysis[identifier]' ], 'num': 10000}, function(data) {
					var items = [];
					for(var i = 0; i < data.items.length; ++i) {
						var item = data.items[i];
						
						var equalsAnalysis = false;
						for(var j = 0; j < item.analysis.items.length; ++j) {
							if(settings.analysis.identifier === item.analysis.items[j].identifier) {
								equalsAnalysis = true;
								break;
							}
						}
						if(!equalsAnalysis)
							items.push('<option value="' + item[idAttrName] + '">' + item[labelAttrName] + '</option>');
					}
					if(items.length === 0)
						$('#analysis-target-select-container').addClass('hidden');
					else
						$('#analysis-target-select-container').removeClass('hidden');
					$('#analysis-target-select').html(items.join(''));
					$('#analysis-target-select').select2();
				});
			} else {
				$('#analysis-target-select-container').addClass('hidden');
			}
			
			// update analysis targets table
			$('#analysis-target-table-container').table({
				'entityMetaData' : targetMeta,
				'attributes' : $.map(targetMeta.attributes, function(attr) {
					switch(attr.name) {
						case 'analysis':
							return null;
						default:
							return attr;
					}
				}),
				'query' : {'q' : [{field:'analysis', operator:'EQUALS', value:settings.analysis.identifier}]},
				'deletable' : settings.analysis.status === 'CREATED',
				'searchable': true,
				'maxRows' : 10,
				'onDeleteRow' : function(href) {
					deleteAnalysisTarget(href);
				},
				'onInit' : function() {
					var nrItems = $('#analysis-target-table-container').table('getNrItems');

                    var entities = restApi.get('/api/v1/computeui_Analysis',
                        {q : { field : analysis.identifier, operator : 'EQUALS',
                            value : settings.analysis.identifier } });
                    var obj = JSON && JSON.parse(entities) || $.parseJSON(entities);
                    var wasRun = obj.wasRun;

					// enable/disable workflow select
					$('#analysis-workflow').prop('disabled', nrItems > 0);
					$('#run-analysis-btn').prop('disabled', nrItems === 0);
					
					if(settings.analysis.status === 'CREATED') {
						$('#delete-analysis-btn').removeClass('hidden');
					}
					else if(settings.analysis.status === 'RUNNING') {
						$('#pause-analysis-btn').removeClass('hidden');
						$('#run-analysis-btn').addClass('hidden');
						$('#delete-analysis-btn').addClass('hidden');
					} 
					else if(settings.analysis.status === 'PAUSED') {
						$('#pause-analysis-btn').addClass('hidden');
						$('#continue-analysis-btn').removeClass('hidden');
					}
					else {
						$('#run-analysis-btn').removeClass('hidden');
						$('#pause-analysis-btn').addClass('hidden');
					}

                    if(settings.analysis.wasRun)
                    {
                        $('#rerun-analysis-btn').removeClass('hidden');
                        $('#run-analysis-btn').addClass('hidden');
                    }

					if(nrItems === 0)
						$('#analysis-target-footer').removeClass('hidden');
					else
						$('#analysis-target-footer').addClass('hidden');
				}
			});
		});
	}
	
	function showAnalysisOverview() {
		stopProgressPolling();
		history.back();
	}
	
	function runAnalysis(analysisId) {
		$.post(molgenis.getContextUrl() + '/run/' + analysisId).done(function() {
			changeAnalysis(analysisId);
//            settings.analysis.wasRun = true;
//            settings.analysis.status = 'RUNNING';
		});
	}

    function rerunAnalysis(analysisId) {
        $.post(molgenis.getContextUrl() + '/rerun/' + analysisId).done(function() {

        });

    }


    /**
	 * @memberOf molgenis.analysis
	 */
	function stopAnalysis(analysisId) {
		$.post(molgenis.getContextUrl() + '/stop/' + analysisId);
	}
	
	/**
	 * @memberOf molgenis.analysis
	 */
	function cloneAnalysis(analysisId) {
		$.post(molgenis.getContextUrl() + '/clone/' + analysisId).done(function(clonedAnalysis) {
			changeAnalysis(clonedAnalysis.identifier);
		});
	}
	
	$(function() {
		history.replaceState(settings, null, molgenis.getContextUrl());
		
		// analysis overview screen
		var tableContainer = $('#analysis-table-container');
		if(tableContainer.length > 0) {
			restApi.getAsync('/api/v1/computeui_analysis/meta', {
				'expand' : [ 'attributes' ]
			}, function(entityMetaData) {
				tableContainer.analysisTable({
					'entityMetaData' : entityMetaData,
					'attributes' : $.map(entityMetaData.attributes, function(attr) {
						switch(attr.name) {
							case 'name':
							case 'workflow':
							case 'status':
								return attr;
							default:
								return null;
						}
					}),
					'maxRows' : 10,
					'onViewAnalysis' : changeAnalysis,
					'onStopAnalysis' : stopAnalysis,
					// FIXME remove hardcoded reference
					'cellClickHandlers' : {'workflow': function(){window.location='/menu/main/workflow';}}
				});
			});
		}
		
		$(document).on('click', '#create-analysis-btn', function(e) {
			e.preventDefault();
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/create',
				data: '{}',
				contentType: 'application/json'
			}).done(function(analysis) {
				console.log('analysis.identifier', analysis);
				changeAnalysis(analysis.identifier);
			});
		});
		
		
		// analysis create screen event handlers
		$(document).on('click', '#analysis-back-btn', function(e) {
			e.preventDefault();
			showAnalysisOverview();
		});
		
		$(document).on('change', '#analysis-name', function() {
			var href = '/api/v1/computeui_Analysis/' + settings.analysis.identifier + '/name';
			restApi.update(href, $(this).val());
		});
		
		$(document).on('change', '#analysis-description', function() {
			var href = '/api/v1/computeui_Analysis/' + settings.analysis.identifier + '/description';
			restApi.update(href, $(this).val());
		});
		
		$(document).on('change', '#analysis-workflow', function() {
			var href = '/api/v1/computeui_Analysis/' + settings.analysis.identifier + '/workflow';
			restApi.update(href, $(this).val(), {
				success: function() {
					renderAnalysisTargets();
				}
			});
		});
		
		$(document).on('click', '#view-workflow-btn', function() {
			// FIXME use id instead of name
			var workflowName = $('#analysis-workflow-select option:selected').text();
			window.location = '/menu/compute/workflow/' + workflowName;
		});
		
		$(document).on('click', '#delete-analysis-btn', function(e) {
			e.preventDefault();
			var ok = confirm('Are you sure you want to delete this analysis?');
			if (ok === true) {
				var analysisId = $('#delete-analysis-btn').closest('form').data('id');
				restApi.remove(analysisId);
				window.location = '/menu/main/analysis';
			}
		});
		
		$(document).on('click', '#clone-analysis-btn', function(e) {
			e.preventDefault();
			cloneAnalysis(settings.analysis.identifier);
		});
		
		$(document).on('click', '#run-analysis-btn', function(e) {
			e.preventDefault();
			runAnalysis(settings.analysis.identifier);
		});

        $(document).on('click', '#rerun-analysis-btn', function(e) {
            e.preventDefault();
            rerunAnalysis(settings.analysis.identifier);
        });


        $(document).on('click', '#add-target-btn', function(e) {
			e.preventDefault();
			var targetId = $('#analysis-target-select option:selected').val();
			addAnalysisTarget('/api/v1/' + settings.analysis.workflow.targetType + '/' + targetId);
		});

		window.onpopstate = function(event) {
			if (event.state !== null) {
				settings = event.state;
				renderPlugin();
			}
		};
		
		$('#jobModal').on('show.bs.modal', function () {
			stopProgressPolling();
		});
		
		$('#jobModal').on('hidden.bs.modal', function () {
			startProgressPolling();
		});
		  
	});
}($, window.top.molgenis = window.top.molgenis || {}));
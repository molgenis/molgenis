(function($, molgenis) {
	"use strict";

	var restApi = new molgenis.RestClient();

	/**
	 * @memberOf molgenis.table
	 */
	function createTable(settings) {
		// create elements
		var items = [];
		items.push('<div class="row-fluid molgenis-table-container">');
		items.push('<table class="table-striped table-condensed molgenis-table"><thead></thead><tbody></tbody></table>');
		items.push('</div>');
		items.push('<div class="row-fluid">');
		items.push('<div class="span3 molgenis-table-info pull-left"></div>');
		items.push('<div class="span6 molgenis-table-pager"></div>');
		items.push('<div class="span3"></div>');
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
				callback(colAttributes, refEntitiesMeta);
			});
		} else callback([], {});
	}

	/**
	 * @memberOf molgenis.table
	 */
	function getTableData(settings, callback) {
		var attributeNames = $.map(settings.colAttributes, function(attribute) {
			return attribute.name;
		});
		var expandAttributeNames = $.map(settings.colAttributes, function(attribute) {
			if(attribute.fieldType === 'XREF' || attribute.fieldType === 'CATEGORICAL' ||attribute.fieldType === 'MREF') {
				// partially expand reference entities (only request label attribute)
				var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
				return attribute.name + '[' + refEntity.labelAttribute + ']';
			}
			return null;
		});

		// TODO do not construct uri from other uri
		var entityCollectionUri = settings.entityMetaData.href.replace("/meta", "");
		var q = $.extend({}, settings.query, {'start': settings.start, 'num': settings.maxRows, 'sort': settings.sort});
		restApi.getAsync(entityCollectionUri, {'attributes' : attributeNames, 'expand' : expandAttributeNames, 'q' : q}, function(data) {
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
			if (settings.sort && settings.sort.orders[0].property === attribute.name) {
				if (settings.sort.orders[0].direction == 'ASC') {
					items.push('<th>' + attribute.label + '<span data-attribute="' + attribute.name
							+ '" class="ui-icon ui-icon-triangle-1-s down"></span></th>');
				} else {
					items.push('<th>' + attribute.label + '<span data-attribute="' + attribute.name
							+ '" class="ui-icon ui-icon-triangle-1-n up"></span></th>');
				}
			} else {
				items.push('<th>' + attribute.label + '<span data-attribute="' + attribute.name
						+ '" class="ui-icon ui-icon-triangle-2-n-s updown"></span></th>');
			}
		});
		
		container.html(items.join(''));
	}

	/**
	 * @memberOf molgenis.table
	 */
	function createTableBody(data, settings) {
		function openRefAttributeModal(attribute, refEntity, refAttribute, refValue) {
			// create modal structure
			var modal = $('#table-ref-modal');
			if(!modal.length) {
				var items = [];
				items.push('<div class="modal hide medium" id="table-ref-modal" tabindex="-1">');
				items.push('<div class="modal-header">');
				items.push('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
				items.push('<h3 class="ref-title"></h3>');
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
			var refQuery = {
				'q' : [ {
					// TODO use idAttribute once github #1400 is fixed
					'field' : refEntity.labelAttribute, 
					'operator' : 'EQUALS',
					// TODO use idAttribute once github #1400 is fixed
					// TODO remove trim() once github #1401 is fixed
					'value' : refValue[refEntity.labelAttribute].trim()
				} ]
			}; 
			$('.ref-title', modal).html(attribute.label || attribute.name);
			$('.ref-description-header', modal).html((refEntity.label || refEntity.name) + ' description');
			$('.ref-description', modal).html(refEntity.description || 'No description available');
			$('.ref-table', modal).table({'entityMetaData' : refEntity, 'attributes': refAttributes, 'query' : refQuery});
			
			// show modal
			modal.modal({'show': true});
		}
		
		var container = $('.molgenis-table tbody', settings.container);

		var items = [];
		for ( var i = 0; i < data.items.length; ++i) {
			var row = $('<tr>');
			var entity = data.items[i];

			$.each(settings.colAttributes, function(i, attribute) {
				var cell = $('<td>');
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
										cellValue.click(function() {
											openRefAttributeModal(attribute, refEntity, refAttribute, rawValue); 
										});
										cell.append(cellValue);
										break;
									case 'MREF':
										$.each(rawValue.items, function(i, rawValue) {
											var cellValuePart = $('<a href="#">' + formatTableCellValue(rawValue[refAttribute], refAttributeType) + '</a>');
											cellValuePart.click(function() {
												openRefAttributeModal(attribute, refEntity, refAttribute, rawValue); 
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
                    	var cellValuePart = $(formatTableCellValue(rawValue, attribute.fieldType, settings.editable));
                    	
						cellValuePart.change((function(entity, attribute) {
							return function() {
								var value = cellValuePart.is(':checked');
								restApi.update(entity.href + "/" + attribute.name, value, {
									success: function() {
									},
									error: function() {
									}
								});
							}
						})(entity, attribute));
						
						cell.append(cellValuePart);
                    	break;
					default :
						cell.append(formatTableCellValue(rawValue, attribute.fieldType));
						break;
				}
				
				row.append(cell);
			});
			items.push(row);
		}
		container.html(items);

		$('.show-popover').popover({trigger:'hover', placement: 'bottom'});
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

	$.fn.table = function(options) {
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
		var settings = $.extend({}, $.fn.table.defaults, options, {'container': container});

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

		return this;
	};

	// default tree settings
	$.fn.table.defaults = {
		'entityMetaData' : null,
		'maxRows' : 20,
		'attributes' : null,
		'query' : null,
		'editable' : false
	};
}($, window.top.molgenis = window.top.molgenis || {}));
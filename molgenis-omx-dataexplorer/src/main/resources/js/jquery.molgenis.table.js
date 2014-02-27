(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	
	function createTable(settings) {
		// create elements
		var items = [];
		items.push('<div class="row-fluid">');
		items.push('<table class="table-striped table-condensed molgenis-table"><thead></thead><tbody></tbody></table>');
		items.push('</div>');
		items.push('<div class="row-fluid">');
		items.push('<div class="span3 molgenis-table-footer pull-left"></div>');
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
	
	function getTableMetaData(settings, callback) {
		// for compound attributes, expand recursively and select all atomic attributes
		var colAttributes = [];
		function createColAttributesRec(attributes) {
			$.each(attributes, function(i, attribute) {
				if(attribute.fieldType === 'COMPOUND'){
					// FIXME improve performance by retrieving async 
					attribute = restApi.get(attribute.href, {'expand': ['attributes']});
					createColAttributesRec(attribute.attributes);
				}
 				else
					colAttributes.push(attribute);
			});	
		}
		createColAttributesRec(settings.attributes);
		
		// get meta data for referenced entities
		var refEntitiesMeta = {}; 
		$.each(colAttributes, function(i, attribute) {
			if(attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF') {
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
	}
	
	function getTableData(settings, callback) {
		var attributeNames = $.map(settings.colAttributes, function(attribute) {
			return attribute.name;
		});
		var expandAttributeNames = $.map(settings.colAttributes, function(attribute) {
			return attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' ? attribute.name : null; 
		});
		
		// TODO do not construct uri from other uri
		var entityCollectionUri = settings.entityMetaData.href.replace("/meta", "");
		var q = $.extend({}, settings.query, {'start': settings.start, 'num': settings.maxRows});
		restApi.getAsync(entityCollectionUri, {'attributes' : attributeNames, 'expand' : expandAttributeNames, 'q' : q, 'sort' : settings.sort}, function(data) {
			callback(data);
		});
	}
	
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
	
	function createTableBody(data, settings) {
		var container = $('.molgenis-table tbody', settings.container);

		var items = [];
		for ( var i = 0; i < data.items.length; ++i) {
			items.push('<tr>');
			var entity = data.items[i];

			$.each(settings.colAttributes, function(i, attribute) {
				var rawValue = entity[attribute.name];
				if (rawValue) {
					var cellValue;
					switch(attribute.fieldType) {
						case 'XREF':
						case 'MREF':
							var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
							var refAttribute = refEntity.labelAttribute;
							var refAttributeType = refEntity.attributes[refAttribute].fieldType;
							if (refAttributeType === 'XREF' || refAttributeType === 'MREF' || refAttributeType === 'COMPOUND') {
								throw 'unsupported field type ' + refAttributeType;
							}
							
							switch(attribute.fieldType) {
								case 'XREF':
									cellValue = formatTableCellValue(rawValue[refAttribute], refAttributeType);
									break;
								case 'MREF':
									var cellValueParts = [];
									$.each(rawValue.items, function(i, rawValue) {
										var cellValuePart = formatTableCellValue(rawValue[refAttribute], refAttributeType);
										cellValueParts.push(cellValuePart);
									});
									cellValue = cellValueParts.join(',');
									break;
								default:
									throw 'unexpected field type ' + attribute.fieldType;
							}
							break;
						default :
							cellValue = formatTableCellValue(rawValue, attribute.fieldType);
							break;
					}
					items.push('<td class="multi-os-datacell">' + cellValue + '</td>');
				} else {
					items.push('<td></td>');
				}
			});

			items.push('</tr>');
		}
		container.html(items.join(''));
	}
	
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
		} else container.hide();
	}
	
	function createTableFooter(data, settings) {
		var container = $('.molgenis-table-footer', settings.container);
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
		container.empty();
		container.data('settings', settings);
		
		// plugin methods
		container.data('table', {
			'setSelectedAttributes' : function(attributes) {
				settings.attributes = attributes;
				createTable(settings);
			},
			'setQuery' : function(query) {
				settings.query = query;
				createTable(settings);
			},
			'getQuery' : function() {
				return settings.query;
			}
		});
		
		createTable(settings, function() {
			if(settings.onInit)
				setting.onInit();
		});
		
		// sort column ascending/descending
		$('thead th .ui-icon', container).click(function() {
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

			getTableData(settings, function(data) {
				createTableBody(data, settings);
			});
			return false;
		});
		
		$('.show-popover', container).popover({trigger:'hover', placement: 'bottom'});
		
		return this;
	};

	// default tree settings
	$.fn.table.defaults = {
		'entityMetaData' : null,
		'maxRows' : 20,
		'attributes' : null,
		'query' : null
	};
}($, window.top.molgenis = window.top.molgenis || {}));
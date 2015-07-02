/**
 * @deprecated Use /molgenis-core-ui/src/main/resources/js/component/Table.js instead
 * 
 * @param $
 * @param molgenis
 */
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
		items.push('<div class="molgenis-table-container" style="min-height: 0%">');  /* workaround for IE9 bug https://github.com/molgenis/molgenis/issues/2755 */
		if(settings.rowClickable){
			items.push('<table class="table table-striped table-condensed molgenis-table table-hover"><thead></thead><tbody></tbody></table>');
		}else{
			items.push('<table class="table table-striped table-condensed molgenis-table"><thead><th></th></thead><tbody></tbody></table>');
		}
		items.push('</div>');
		items.push('</div>');
		items.push('</div>');
		items.push('<div class="row">');
		items.push('<div class="col-md-3"><div class="molgenis-table-controls">');
		if(settings.editable) {
			items.push('<a class="btn btn-default btn-primary edit-table-btn" href="#" data-toggle="button" title="Edit"><span class="glyphicon glyphicon-edit"></span></a>');
			items.push('<a class="btn btn-default btn-success add-row-btn" style="display: none" href="#" data-toggle="button" title="Add row"><span class="glyphicon glyphicon-plus"></span></a>');
		}
		
		items.push('</div></div>');
		items.push('<div class="col-md-6"><div class="molgenis-table-pager"></div></div>');
		items.push('<div class="col-md-3"><div class="molgenis-table-info pull-right"></div></div>');
		items.push('</div>');
		settings.container.html(items.join(''));
		
		// add data to elements
		getTableMetaData(settings, function(attributes, refEntitiesMeta) {
			var visibleAttributes = [];
			for (var i = 0; i < attributes.length; ++i) {
				if(attributes[i].visible) {
					visibleAttributes.push(attributes[i]);
				}
			}
			
			settings.colAttributes = visibleAttributes;
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
				if(attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'CATEGORICAL_MREF') {
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
					if(attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'CATEGORICAL_MREF') {
						attribute.refEntity = refEntitiesMeta[attribute.refEntity.href];
					}
				});
				callback(colAttributes, refEntitiesMeta);
			});
		} else {
			callback([], {});
		}
	}

	/**
	 * @memberOf molgenis.table
	 */
	function getTableData(settings, callback) {
		var attributeNames = $.map(settings.colAttributes, function(attribute) {
			if(attribute.visible){
				return attribute.name;
			}
		});
		var expandAttributeNames = $.map(settings.colAttributes, function(attribute) {
			if(attribute.expression){
				if(attribute.visible){
					return attribute.name;
				}
			}
			if(attribute.fieldType === 'XREF' || attribute.fieldType === 'CATEGORICAL' ||attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL_MREF') {
				// partially expand reference entities (only request label attribute)
				var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
				if(attribute.visible){
					return attribute.name;// + '[' + refEntity.labelAttribute + ']';
				}
			}
			return null;
		});
		
		// TODO do not construct uri from other uri
		var entityCollectionUri = settings.entityMetaData.href.replace("/meta", "");
		if(settings.query) {
			var q = $.extend({}, settings.query, {'start': settings.start, 'num': settings.maxRows, 'sort': settings.sort});
			restApi.getAsync(entityCollectionUri, {'attributes' : attributeNames, 'expand' : expandAttributeNames, 'q' : q}, function(data) {
				settings.data = data;
				callback(data);
			});
		} else {
			// don't query but use the predefined value
			settings.data = settings.value;
			callback(settings.value);
		}
	}

	/**
	 * @memberOf molgenis.table
	 */
	function calculateNrHeaderRows(attributes) {
	    var level = 1;
	    if(attributes) {
		    var key;
		    for(key in attributes) {
		        if (!attributes.hasOwnProperty(key)) continue;
	
		        if(typeof attributes[key] === 'object'){
		            var depth = calculateNrHeaderRows(attributes[key]) + 1;
		            level = Math.max(depth, level);
		        }
		    }
	    }
	    return level;
	}
	
	/**
	 * @memberOf molgenis.table
	 */
	function createTableHeader(settings) {
		var container = $('.molgenis-table thead', settings.container);

		var items = [];
		if (settings.editenabled) {
			items.push($('<th>')); // edit row
			items.push($('<th>')); // delete row
		}
		
		// calculate number of header rows 
		var nrHeaderRows = calculateNrHeaderRows(settings.expandAttributes);
		console.log(settings.colAttributes, settings.expandAttributes);
		$.each(settings.colAttributes, function(i, attribute) {
			if(attribute.visible) {
				var expandCollapseControl;
				if(attribute.refEntity) {
					if(settings.expandAttributes && settings.expandAttributes[attribute.name] !== undefined) {
						expandCollapseControl = '<span data-attribute="' + attribute.name + '"class="collapse-btn glyphicon glyphicon-minus"></span>';	
					} else {
						expandCollapseControl = '<span data-attribute="' + attribute.name + '"class="expand-btn glyphicon glyphicon-plus"></span>';
					}
				} else {
					expandCollapseControl = '';
				}

				function createAttributeHeader(attribute, headerClass) {
					var header;
					if (settings.sort && settings.sort.orders[0].property === attribute.name) {
						if (settings.sort.orders[0].direction === 'ASC') {
							header = $('<th' + (headerClass ? ' class="' + headerClass + '"' : '') + '>' + attribute.label + '<span data-attribute="' + attribute.name
									+ '" class="ui-icon ui-icon-triangle-1-s down"></span>' + expandCollapseControl + '</th>');
						} else {
							header = $('<th' + (headerClass ? ' class="' + headerClass + '"' : '') + '>' + attribute.label + '<span data-attribute="' + attribute.name
									+ '" class="ui-icon ui-icon-triangle-1-n up"></span>' + expandCollapseControl + '</th>');
						}
					} else {
						header = $('<th' + (headerClass ? ' class="' + headerClass + '"' : '') + '>' + attribute.label + '<span data-attribute="' + attribute.name
								+ '" class="ui-icon ui-icon-triangle-2-n-s updown"></span>' + expandCollapseControl + '</th>');
					}
					header.data('attr', attribute);
					items.push(header);
				}
				
				if(settings.expandAttributes && settings.expandAttributes[attribute.name] !== undefined) {
					$.each(attribute.refEntity.attributes, function(i, refAttribute) {
						createAttributeHeader(refAttribute, 'ref-attr');	
					});
				} else {
					createAttributeHeader(attribute);
				}
			}
		});
		container.html(items);
	}

	/**
	 * @memberOf molgenis.table
	 */
	function createTableBody(data, settings) {
		var container = $('.molgenis-table tbody', settings.container);
		var items = [];
		var tabindex = 1;
		for (var i = 0; i < data.items.length; ++i) {
			var entity = data.items[i];
			var row = $('<tr>').data('entity', entity).data('id', entity.href);
			if (settings.editenabled) {
				// edit row button
				var cell = $('<td class="edit" tabindex="' + tabindex++ + '">');
				$('<a class="btn btn-xs btn-primary edit-row-btn" href="#" data-toggle="button" title="Edit"><span class="glyphicon glyphicon-edit"></span></button>').appendTo(cell);
				row.append(cell);
				
				// delete row button
				var cell = $('<td class="trash" tabindex="' + tabindex++ + '">');
				$('<a class="btn btn-xs btn-danger delete-row-btn" href="#" data-toggle="button" title="Delete"><span class="glyphicon glyphicon-minus"></span></button>').appendTo(cell);
				row.append(cell);
			}

			$.each(settings.colAttributes, function(i, attribute) {
				function renderAttribute(entity, attribute) {
					var cell = $('<td>').data('id', entity.href + '/' + encodeURIComponent(attribute.name));
					renderCell(cell, entity, attribute, settings);
					if(settings.editenabled) {
						cell.attr('tabindex', tabindex++);
					}
					row.append(cell);
				}
				
				if(settings.expandAttributes && settings.expandAttributes[attribute.name] !== undefined) {
					$.each(attribute.refEntity.attributes, function(i, refAttribute) {
						var refEntity = entity[attribute.name];
						if(refEntity) {
							renderAttribute(entity[attribute.name], refAttribute);
						} else {
							row.append($('<td>'));
						}
					});
				} else {
					renderAttribute(entity, attribute);
				}
			});
			items.push(row);
		}
		container.html(items);

		$('.show-popover').popover({trigger:'hover', placement: 'bottom', container: 'body'});
	}

	/**
	 * @memberOf molgenis.table.cell
	 */
	function renderCell(cell, entity, attribute, settings) {
		if(settings.editenabled && !attribute.readOnly) {
			renderEditCell(cell, entity, attribute, settings);
		}
		else {
			renderViewCell(cell, entity, attribute, settings);
		}
	}
	
	/**
	 * @memberOf molgenis.table.cell
	 */
	function renderEditCell(cell, entity, attribute, settings) {
		cell.empty();
		
		var value = entity[attribute.name];
		switch(attribute.fieldType) {
			case 'BOOL':
				var items = [];
				items.push('<div class="bool-btn-group btn-group-xs">');
				items.push('<button type="button" class="btn btn-default');
				if(value === true) {items.push(' active');}
				items.push('" data-state="true">Yes</button>');
				items.push('<button type="button" class="btn btn-default');
				if(value === false) {items.push(' active');}
				items.push('" data-state="false">No</button>');
				if(attribute.nillable) {
					items.push('<button type="button" class="btn btn-default');
					if(value === undefined) {items.push(' active');}
					items.push('" data-state="undefined">N/A</button>');
				}
				items.push('</div>');
				cell.html(items.join(''));
				break;
			case 'CATEGORICAL':
				var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
				// TODO do not construct uri from other uri
				var refEntityCollectionUri = attribute.refEntity.href.replace("/meta", "");
				
				var format = function(item) {
					if (item) {
						return item[refEntityMeta.labelAttribute];
					}
				};
				
				var opts = {
					id: 'href',
					allowClear : attribute.nillable ? true : false,
					placeholder : ' ', // cannot be an empty string
					initSelection: function(element, callback) {
						callback(value);
					},
				    query: function (query) {
				    	var num = 25;
					    var q = {
							q : {
								start : (query.page - 1) * num, 
								num : num
							}
						};
				    	
				    	restApi.getAsync(refEntityCollectionUri, q, function(data) {
				    		query.callback({results: data.items, more: data.nextHref ? true : false});
				    	});
				    },
				    formatResult: format,
				    formatSelection: format,
				    minimumResultsForSearch: -1, // permanently hide the search field
				    width: '100%'
				};
				
				var container = $('<input type="hidden" class="ref-select">');
				cell.html(container); // first append container, then create select2
				container.select2(opts).select2('val', []); // create select2 and trigger initSelection
				break;
			case 'DATE':
				var datepicker = createInput(attribute, {'style': 'min-width: 100px'}, entity[attribute.name]);
				cell.html(datepicker);
				break;
			case 'DATE_TIME':
				var datepicker = createInput(attribute, {'style': 'min-width: 210px'}, entity[attribute.name]);
				cell.html(datepicker);
				break;
			case 'DECIMAL':
			case 'INT':
			case 'LONG':
				var input = createInput(attribute, null, entity[attribute.name]);
				input.addClass('number-input');
				cell.html(input);
				break;
			case 'CATEGORICAL_MREF': // TODO render like CATEGORICAL is rendered for XREF
			case 'MREF':
				var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
				// TODO do not construct uri from other uri
				var refEntityCollectionUri = attribute.refEntity.href.replace("/meta", "");
				
				var format = function(item) {
					return item[refEntityMeta.labelAttribute];
				};
				
				// note: allowClear not possible in combination with multiple select
				var opts = {
					id: 'href', 
					multiple: true,
					initSelection: function(element, callback) {
						callback(value.items);
					},
				    query: function (query) {
				    	var num = 100;
					    var q = {
							q : {
								start : (query.page - 1) * num, 
								num : num,
								q : [ {
									field : refEntityMeta.labelAttribute,
									operator : 'SEARCH',
									value : query.term
								} ]
							}
						};
				    	
				    	restApi.getAsync(refEntityCollectionUri, q, function(data) {
				    		query.callback({results: data.items, more: data.nextHref ? true : false});
				    	});
				    },
				    formatResult: format,
				    formatSelection: format,
				    width: '400px' // preserve row height changes by limiting y overflow
				};
				
				var container = $('<input type="hidden" class="ref-select">');
				cell.html(container); // first append container, then create select2
				container.select2(opts).select2('val', []); // create select2 and trigger initSelection
				break;
			case 'XREF':
				var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
				// TODO do not construct uri from other uri
				var refEntityCollectionUri = attribute.refEntity.href.replace("/meta", "");
				
				var format = function(item) {
					if(item) {
						return item[refEntityMeta.labelAttribute];
					}
				};
				
				var opts = {
					id: 'href',
					allowClear : attribute.nillable ? true : false,
					placeholder : ' ', // cannot be an empty string
					initSelection: function(element, callback) {
						callback(value);
					},
				    query: function (query) {
				    	var num = 100;
					    var q = {
							q : {
								start : (query.page - 1) * num, 
								num : num,
								q : [ {
									field : refEntityMeta.labelAttribute,
									operator : 'SEARCH',
									value : query.term
								} ]
							}
						};
				    	
				    	restApi.getAsync(refEntityCollectionUri, q, function(data) {
				    		query.callback({results: data.items, more: data.nextHref ? true : false});
				    	});
				    },
				    formatResult: format,
				    formatSelection: format,
				    width: '100%'
				};
				
				var container = $('<input type="hidden" class="ref-select">');
				cell.html(container); // first append container, then create select2
				container.select2(opts).select2('val', []); // create select2 and trigger initSelection
				break;
			default:
				var value = entity[attribute.name];
				cell.text(value).attr('contenteditable', 'true');
				break;
		}
	}
	
	/**
	 * @memberOf molgenis.table.cell
	 */
	function renderViewCell(cell, entity, attribute, settings) {
		cell.empty();
		var rawValue = entity[attribute.name];
		
		switch(attribute.fieldType) {
			case 'XREF':
			case 'MREF':
			case 'CATEGORICAL':
			case 'CATEGORICAL_MREF':
				if (undefined === rawValue) {
					cell.append(formatTableCellValue(undefined, undefined));
				} else {
					var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
					var refAttribute = refEntity.labelAttribute;
					var refValue = refEntity.attributes[refAttribute];
					
					if (refValue) {
						var refAttributeType = refValue.fieldType;
						if (refAttributeType === 'XREF' || refAttributeType === 'MREF' || refAttributeType === 'CATEGORICAL' || refAttributeType === 'CATEGORICAL_MREF' || refAttributeType === 'COMPOUND') {
							throw 'unsupported field type ' + refAttributeType;
						}
						
						switch(attribute.fieldType) {
							case 'CATEGORICAL':
							case 'XREF':
								var $cellValue = $('<a href="#">').append(formatTableCellValue(rawValue[refAttribute], refAttributeType));
								$cellValue.click(function(event) {
									openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
									event.stopPropagation();
								});
								cell.append($cellValue);
								break;
							case 'CATEGORICAL_MREF':
							case 'MREF':
								if(!rawValue.items.length){
									cell.append(formatTableCellValue(undefined, refAttributeType));
								}else{
									$.each(rawValue.items, function(i, rawValue) {
										var $cellValuePart = $('<a href="#">').append(formatTableCellValue(rawValue[refAttribute], refAttributeType));
										$cellValuePart.click(function(event) {
											openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
											event.stopPropagation();
										});
										if (i > 0) {cell.append(',');}
										cell.append($cellValuePart);
									});
								}
								break;
							default:
								throw 'unexpected field type ' + attribute.fieldType;
						}
					}
				}
				break;
			case 'BOOL':
				cell.append(formatTableCellValue(rawValue, attribute.fieldType, undefined, attribute.nillable));
				break;
			default :
				cell.append(formatTableCellValue(rawValue, attribute.fieldType));
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
		var val = restApi.get(refValue.href)[refEntity.idAttribute];
        
        var refQuery = {
			'q' : [ {
				'field' : refEntity.idAttribute,
				'operator' : 'EQUALS',
				'value' : val
			} ]
		}; 
	
		$('.ref-title', modal).html(attribute.label || attribute.name);
		$('.ref-description-header', modal).html((refEntity.label || refEntity.name) + ' description');
		$('.ref-description', modal).html(refEntity.description || 'No description available');
		if(attribute.expression){
			// computed attribute, don't query but show the computed value
			$('.ref-table', modal).table({'entityMetaData' : refEntity, 'attributes': refAttributes, 'value': {items:[refValue], total: 1} });
		} else {
			$('.ref-table', modal).table({'entityMetaData' : refEntity, 'attributes': refAttributes, 'query' : refQuery });
		}
		
		// show modal
		modal.modal({'show': true});
	}
	
	/**
	 * @memberOf molgenis.table.cell
	 */
	function persistCell(cell, settings) {
		var row = cell.closest('tr').index();
		var col = cell.index();
		var attribute = cell.closest('table').find('th').eq(col).data('attr');
		var value = settings.data.items[row][attribute.name];
		
		switch(attribute.fieldType) {
			case 'BOOL':
				var editValue;
				
				var state = cell.find('button.active').data('state');
				if(state === true) {editValue = true;}
				else if(state === false) {editValue = false;}
				else if(state === 'undefined' && attribute.nillable) {editValue = undefined;}
				else {throw 'invalid state: ' + state;}
				
				if(value !== editValue) {
					restApi.update(cell.data('id'), editValue, {
						success: function() {
							settings.onDataChange();
							value = editValue;
							settings.data.items[row][attribute.name] = value;
							cell.addClass('edited');
						}
					});
				}
				break;
			case 'DATE':
			case 'DATE_TIME':
				var editValue = cell.find('input').val();
				var entity = settings.data.items[row];
				var value = entity[attribute.name];
				if(value !== editValue) {
					restApi.update(cell.data('id'), editValue, {
						success: function() {
							settings.onDataChange();
							if (editValue === '') {
								delete entity[attribute.name];
							}
							else {
								entity[attribute.name] = editValue;	
							}
							cell.addClass('edited');
						}
					});
				}
				break;
			case 'DECIMAL':
			case 'INT':
			case 'LONG':
				var input = cell.find('input');
				if(input[0].validity.valid) {
					var editValue = input.val();
					if(value !== editValue) {
						restApi.update(cell.data('id'), editValue, {
							success: function() {
								settings.onDataChange();
								settings.data.items[row][attribute.name] = editValue;
								cell.removeClass('invalid-input').addClass('edited');
							}
						});
					}
				} else {
					cell.removeClass('edited').addClass('invalid-input');
				}
				break;
			case 'CATEGORICAL_MREF' :
			case 'MREF':
				var select = cell.find('input[type=hidden]');
				var data = select.select2('data');
				if(attribute.nillable || data.length > 0) {
					var entity = settings.data.items[row];
					var value = entity[attribute.name];
					if(JSON.stringify(data) !== JSON.stringify(value.items) ) {
						var editValue = $.map(data, function(val){ return restApi.getPrimaryKeyFromHref(val.href);});
						restApi.update(cell.data('id'), editValue, {
							success: function() {
								settings.onDataChange();
								entity[attribute.name].total = data.length;
								entity[attribute.name].items = data;
								cell.addClass('edited');
							}
						});
					}
				}
				break;
			case 'CATEGORICAL' :
			case 'XREF':
				var select = cell.find('input[type=hidden]');
				var data = select.select2('data');
				if(attribute.nillable || data) {
	            	var entity = settings.data.items[row];
					var value = entity[attribute.name] ? entity[attribute.name].href : '';
					var editValue = data ? data.href : '';
					
	            	if(value !== editValue) {
	            		var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
	            		var editLabel = data ? data[refEntityMeta.labelAttribute] : '';
	            		
	            		editValue = editValue !== '' ? restApi.getPrimaryKeyFromHref(editValue) : ''; 
						restApi.update(cell.data('id'), editValue, {
							success: function() {
								settings.onDataChange();
								if (editValue === '') {
									delete entity[attribute.name];
								}
								else {
									if(!entity[attribute.name]) {
										entity[attribute.name] = {};
									}
									entity[attribute.name].href = editValue;
									entity[attribute.name][refEntityMeta.labelAttribute] = editLabel;	
								}
								
								cell.addClass('edited');
							}
						});
					}
				}
				break;
			default:
				var editValue = cell.text();
				if(value !== editValue) {
					restApi.update(cell.data('id'), editValue, {
						success: function() {
							settings.onDataChange();
							value = editValue;
							settings.data.items[row][attribute.name] = value;
							cell.addClass('edited');
						}
					});
				}
				break;
		}
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
		} else {
			container.hide();
		}
	}

	function refresh(settings) {
		getTableData(settings, function(data) {
			createTableBody(data, settings);
			createTablePager(data, settings);
			createTableFooter(data, settings);
		});
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
				refresh(settings);
			},
			'getQuery' : function() {
				return settings.query;
			},
			'getSort' : function() {
				return settings.sort;
			}
		});

		createTable(settings, function() {
			if(settings.onInit) {
				settings.onInit();
			}
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
		
		$(container).on('click', 'thead th .expand-btn', function(e) {
			var attributeName = $(this).data('attribute');
			settings.expandAttributes = settings.expandAttributes || {};
			settings.expandAttributes[attributeName] = null;
			
			createTable(settings);
		});
		
		$(container).on('click', 'thead th .collapse-btn', function(e) {
			var attributeName = $(this).data('attribute');
			delete settings.expandAttributes[attributeName];
			
			createTable(settings);
		});

		// toggle edit table mode
		$(container).on('click', '.edit-table-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();
			if( molgenis.ie9 ){
				bootbox.alert("Sorry. In-place editing is not supported in Internet Explorer 9.<br/>Please use a modern browser instead.");
				return;
			}
			settings.editenabled = !settings.editenabled;
			createTableHeader(settings);
			createTableBody(settings.data, settings);
			if (settings.editenabled) {
				createTableBody(settings.data, settings);
				$('.molgenis-table tbody').addClass('editable');
				$('.molgenis-table tbody td:not(.trash)', settings.container).first().focus();
				$('.add-row-btn').show();
				 
				$('.edit-table-btn').html('Done');
			} else {
				$('.add-row-btn').hide();
				getTableData(settings, function(data) {
					createTableBody(data, settings);
				});
				$('.molgenis-table tbody').removeClass('editable');
				$('.edit-table-btn').html('<span class="glyphicon glyphicon-edit"></span>');
			}
		});
		
		//Add row
		$(container).on('click', '.add-row-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();
			getCreateForm(settings.entityMetaData);
		});
		
		function getCreateForm(entityMetaData) {
			React.render(molgenis.ui.Form({
				mode: 'create',
				showHidden: true,
				entity : entityMetaData.name,
				modal: true,
				onSubmitSuccess: function() {
					settings.start = 0;
					refresh(settings);
				}
			}), $('<div>')[0]);
		}
		
		// edit row
		$(container).on('click', '.edit-row-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();
			
			React.render(molgenis.ui.Form({
				entity : settings.entityMetaData.name,
				entityInstance: $(this).closest('tr').data('id'),
				mode: 'edit',
				showHidden: true,
				modal: true,
				onSubmitSuccess : function() {
					settings.start = 0;
					refresh(settings);
				}
			}), $('<div>')[0]);
		});
		
		// delete row
		$(container).on('click', '.delete-row-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();
			
			if(confirm('Are you sure you want to delete this row?')) {
				var href = $(this).closest('tr').data('id');
				restApi.remove(href, {
					success: function() {
						settings.start = 0;
						refresh(settings);
					}
				});
			}
		});
		
		// update values on losing focus on cell
		$(container).on('keydown', '.molgenis-table tbody.editable td', function(e) {
			var cell = $(this);
			switch(e.keyCode) {
				case 37: // left arrow
					cell.prev('td').focus();
					break;
				case 38: // up arrow
					cell.closest('tr').prev().children().eq(cell.index()).focus();
					break;
				case 39: // right arrow
					cell.next('td').focus();
					break;
				case 40: // down arrow
					cell.closest('tr').next().children().eq(cell.index()).focus();
					break;
				default:
					break;
			}
		});
		
		// handle table cell focus out event (do not use focusout, since it triggers on children taking focus)
		$(container).on('blur', '.molgenis-table tbody.editable td[contenteditable="true"]', function(e) {
			// determine if focus was lost to child:
			// http://marc.codewisp.com/2013/01/18/detecting-blur-child-elements-jquery/
			setTimeout($.proxy(function()
		    {
		        var target = document.activeElement;
		        if (target !== null) {
		        	if($(target).is('td')) {
		    			e.preventDefault();
		    			e.stopPropagation();
		    			var cell = $(this);
		    			persistCell(cell, settings);	
		        	}
		        }
		    }, this), 1);
		});
		
		// edit event handlers
				
		// BOOL
		$(container).on('click', '.molgenis-table tbody.editable .bool-btn-group button', function(e) {
			// do not use bootstrap data-toggle to prevent race condition:
			// http://stackoverflow.com/questions/9262827/twitter-bootstrap-onclick-event-on-buttons-radio
			$(this).addClass('active').siblings().removeClass('active');
			
			var cell = $(this).closest('td');
			persistCell(cell, settings);
		});

		// CATEGORICAL
		$(container).on('change', '.molgenis-table tbody.editable .categorical-select', function(e) {
			var cell = $(this).closest('td');
			persistCell(cell, settings);
		});

		// DATE, DATE_TIME
		$(container).on('dp.change', function(e) {
			var cell = $(e.target).closest('td');
			persistCell(cell, settings);
		});

		// DECIMAL, INT, LONG
		$(container).on('change', '.molgenis-table tbody.editable .number-input', function(e) {			
			var cell = $(this).closest('td');
			persistCell(cell, settings);
		});
		
		// XREF, MREF
		$(container).on('change', '.molgenis-table tbody.editable .ref-select', function(e) {
			var cell = $(this).closest('td');
			persistCell(cell, settings);
		});
		
		$(container).on('click', '.molgenis-table.table-hover tbody:not(.editable) tr', function(e){
			// Issue #1400 ask for IdAttribute directly
			var entityData = $(this).data('entity').href.split('/');
			var entityId = decodeURIComponent(entityData.pop());
			var entityName = decodeURIComponent(entityData.pop());
			
			$('#entityReport').load("dataexplorer/details",{entityName: entityName, entityId: entityId}, function() {
				  $('#entityReportModal').modal("show");
				  
				  // Button event handler when a button is placed inside an entity report ftl
				  $(".modal-body button", "#entityReport").on('click', function() {
						$.download($(this).data('href'), {entityName: entityName, entityId: entityId}, "GET");
				  });
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
		'editable' : false,
		'rowClickable': false,
		'onDataChange': function(){}
	};
}($, window.top.molgenis = window.top.molgenis || {}));
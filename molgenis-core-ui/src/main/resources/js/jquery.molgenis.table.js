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
		if(settings.rowClickable){
			items.push('<table class="table-striped table-condensed molgenis-table table-hover"><thead></thead><tbody></tbody></table>');
		}else{
			items.push('<table class="table-striped table-condensed molgenis-table"><thead></thead><tbody></tbody></table>');
		}
		items.push('</div>');
		items.push('<div class="row-fluid">');
		items.push('<div class="span3"><div class="molgenis-table-controls pull-left">');
		if(settings.editable)
			items.push('<a class="btn edit-table-btn" href="#" data-toggle="button"><i class="icon-edit"></i></a>');
		items.push('</div></div>');
		items.push('<div class="span6"><div class="molgenis-table-pager"></div></div>');
		items.push('<div class="span3"><div class="molgenis-table-info pull-right"></div></div>');
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
		if (settings.editenabled)
			items.push($('<th>'));
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
		container.html(items);
	}

	/**
	 * @memberOf molgenis.table
	 */
	function createTableBody(data, settings) {
		
		
		
		
		var container = $('.molgenis-table tbody', settings.container);

		var items = [];
		var tabindex = 1;
		for ( var i = 0; i < data.items.length; ++i) {
			var entity = data.items[i];
			var row = $('<tr>').data('entity', entity).data('id', entity.href); // FIXME remove id
			if (settings.editenabled) {
				var cell = $('<td class="trash" tabindex="' + tabindex++ + '">');
				var href = entity.href;
				$('<i class="icon-trash delete-row-btn"></i>').click(function(e) {
					if(confirm('Are you sure you want to delete this row?')) {
						restApi.remove(href, {
							success: function() {
								getTableData(settings, function(data) {
									createTableBody(data, settings);
									createTablePager(data, settings);
									createTableFooter(data, settings);
								});
							}
						});
					}
				}).appendTo(cell);
				row.append(cell);
			}

			$.each(settings.colAttributes, function(i, attribute) {
				var cell = $('<td>').data('id', entity.href + '/' + attribute.name);
				renderCell(cell, entity, attribute, settings);
				if(settings.editenabled) {
					cell.attr('tabindex', tabindex++);
				}
				row.append(cell);
			
			});
			items.push(row);
		}
		container.html(items);

		$('.show-popover').popover({trigger:'hover', placement: 'bottom'});
	}

	/**
	 * @memberOf molgenis.table.cell
	 */
	function renderCell(cell, entity, attribute, settings) {
		if(settings.editenabled)
			renderEditCell(cell, entity, attribute, settings);
		else
			renderViewCell(cell, entity, attribute, settings);
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
				items.push('<div class="bool-btn-group">');
				items.push('<button type="button" class="btn btn-mini');
				if(value === true) items.push(' active');
				items.push('" data-state="true">Yes</button>');
				items.push('<button type="button" class="btn btn-mini');
				if(value === false) items.push(' active');
				items.push('" data-state="false">No</button>');
				if(attribute.nillable) {
					items.push('<button type="button" class="btn btn-mini');
					if(value === undefined) items.push(' active');
					items.push('" data-state="undefined">N/A</button>');
				}
				items.push('</div>');
				cell.html(items.join(''));
				break;
			case 'CATEGORICAL':
				var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
				// TODO do not construct uri from other uri
				var refEntityCollectionUri = attribute.refEntity.href.replace("/meta", "");
				
				var opts = {
					// lazy load drop-down content 
				    query: function (query) {
				    	restApi.getAsync(refEntityCollectionUri, null, function(data) {
				    		var results = [];
				    		$.each(data.items, function(i, item) {
				    			results.push({id: item.href, text: item[refEntityMeta.labelAttribute]});
				    		});
				    		query.callback({results : results});
				    	});
				    },
				    // disable search box
				    minimumResultsForSearch: -1,
				    width: '100%'
				};
				if(value) {
					opts.initSelection = function(element, callback) {
						callback({id: value.href, text: value[refEntityMeta.labelAttribute]});
					};
				}
				if(attribute.nillable) {
					opts.allowClear = true,
					opts.placeholder = ' '; // cannot be an empty string
				}
				
				var container = $('<input type="hidden" class="categorical-select">');
				// first append container, then create select2
				cell.html(container);
				container.select2(opts);
				
				if(value && attribute.nillable) {
					// initSelection not called in case of placeholder: https://github.com/ivaynberg/select2/issues/2086
					// workaround:
					container.select2('val', []);
				}
				break;
			case 'DATE':
			case 'DATE_TIME':
				var datepicker = createInput(attribute, null, entity[attribute.name]);
				cell.html(datepicker);
				break;
			case 'DECIMAL':
			case 'INT':
			case 'LONG':
				var input = createInput(attribute, null, entity[attribute.name]);
				input.addClass('number-input');
				cell.html(input);
				break;
			case 'MREF':
				var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
				var lblValue = entity[attribute.name] ? $.map(entity[attribute.name].items, function(val) {return val[refEntityMeta.labelAttribute];}) : undefined; 
				var container = $('<div class="xrefmrefsearch">');
				container.xrefmrefsearch({attribute: attribute, values: lblValue});
				container.addClass('ref-select');
				cell.html(container);
				break;
			case 'XREF':
				var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
				var lblValue = entity[attribute.name] ? entity[attribute.name][refEntityMeta.labelAttribute] : undefined;
				var container = $('<div class="xrefmrefsearch">');
				container.xrefmrefsearch({attribute: attribute, values: lblValue});
				container.addClass('ref-select');
				cell.html(container);
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
									openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
									event.stopPropagation();
								});
								cell.append(cellValue);
								break;
							case 'MREF':
								$.each(rawValue.items, function(i, rawValue) {
									var cellValuePart = $('<a href="#">' + formatTableCellValue(rawValue[refAttribute], refAttributeType) + '</a>');
									cellValuePart.click(function(event) {
										openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
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
		$('.ref-table', modal).table({'entityMetaData' : refEntity, 'attributes': refAttributes, 'query' : refQuery});
		
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
				if(state === true) editValue = true;
				else if(state === false) editValue = false;
				else if(state === 'undefined' && attribute.nillable) editValue = undefined;
				else throw 'invalid state: ' + state;
				
				if(value !== editValue) {
					restApi.update(cell.data('id'), editValue, {
						success: function() {
							value = editValue;
							settings.data.items[row][attribute.name] = value;
							cell.addClass('edited');
						}
					});
				}
				break;
			case 'CATEGORICAL':
				var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];

				var data = cell.find('.categorical-select').select2('data');
				var editValue = data ? data.id : '';
            	var entity = settings.data.items[row];
				value = entity[attribute.name] ? entity[attribute.name].href : '';
            	
            	if(value !== editValue) {
            		editValue = editValue !== '' ? restApi.getPrimaryKeyFromHref(editValue) : ''; 
					restApi.update(cell.data('id'), editValue, {
						success: function() {
							if (editValue === '')
								delete entity[attribute.name];
							else {
								if(!entity[attribute.name])
									entity[attribute.name] = {};
								entity[attribute.name].href = editValue;
								entity[attribute.name][refEntityMeta.labelAttribute] = data.text;	
							}
							
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
							if (editValue === '')
								delete entity[attribute.name];
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
								settings.data.items[row][attribute.name] = editValue;
								cell.addClass('edited');
							}
						});
					}
				}
				break;
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
								entity[attribute.name].total = data.length;
								entity[attribute.name].items = data;
								cell.addClass('edited');
							}
						});
					}
				}
				break;
			case 'XREF':
				var select = cell.find('input[type=hidden]');
				var data = select.select2('data');
				var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
            	var entity = settings.data.items[row];
				value = entity[attribute.name] ? entity[attribute.name].href : '';
				var editValue = data.href;
				var editLabel = data[refEntityMeta.labelAttribute];
				
            	if(value !== editValue) {
            		editValue = editValue !== '' ? restApi.getPrimaryKeyFromHref(editValue) : ''; 
					restApi.update(cell.data('id'), editValue, {
						success: function() {
							if (editValue === '')
								delete entity[attribute.name];
							else {
								if(!entity[attribute.name])
									entity[attribute.name] = {};
								entity[attribute.name].href = editValue;
								entity[attribute.name][refEntityMeta.labelAttribute] = editLabel;	
							}
							
							cell.addClass('edited');
						}
					});
				}
				break;
			default:
				var editValue = cell.text();
				if(value !== editValue) {
					restApi.update(cell.data('id'), editValue, {
						success: function() {
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
		
		// toggle edit table mode
		$(container).on('click', '.edit-table-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();
			
			settings.editenabled = !settings.editenabled;
			createTableHeader(settings);
			createTableBody(settings.data, settings);
			if (settings.editenabled) {
				createTableBody(settings.data, settings);
				$('.molgenis-table tbody').addClass('editable');
				$('.molgenis-table tbody td:not(.trash)', settings.container).first().focus();
			} else {
				getTableData(settings, function(data) {
					createTableBody(data, settings);
				});
				$('.molgenis-table tbody').removeClass('editable');
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
		$(container).on('changeDate', function(e) {
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
			var entityId = entityData.pop();
			var entityName = entityData.pop();
			
			$('#entityReport').load("dataexplorer/details",{entityName: entityName, entityId: entityId}, function() {
				  $('#entityReportModal').modal("show");
				  	  
				  $(".specific-content button", "#entityReport").on('click', function() {
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
		'rowClickable': false
	};
}($, window.top.molgenis = window.top.molgenis || {}));
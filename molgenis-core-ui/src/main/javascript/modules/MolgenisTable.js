define(function(require, exports, module) {
	/**
	 * @module MolgenisTable
	 * @deprecated Use
	 *             /molgenis-core-ui/src/main/resources/js/component/Table.js
	 *             instead
	 */

	"use strict";

	var $ = require('jquery');
	var restApi = require('modules/RestClientV1');
	var molgenis = require('modules/MolgenisQuery');

	var Form = require('component/Form');

	// Save a reference to module's global scope outside the exports.
	// This is used to both export a function and also call it within the module
	var _this_ = this;

	/**
	 * @memberOf MolgenisTable
	 */
	exports.createTable = function(settings) {
		// Create elements
		var items = [];
		items.push('<div class="row">');
		items.push('<div class="col-md-12">');
		// Workaround for IE9 bug
		// https://github.com/molgenis/molgenis/issues/2755
		items.push('<div class="molgenis-table-container" style="min-height: 0%">');
		if (settings.rowClickable) {
			items.push('<table class="table table-striped table-condensed molgenis-table table-hover"><thead></thead><tbody></tbody></table>');
		} else {
			items.push('<table class="table table-striped table-condensed molgenis-table"><thead><th></th></thead><tbody></tbody></table>');
		}
		items.push('</div>');
		items.push('</div>');
		items.push('</div>');
		items.push('<div class="row">');
		items.push('<div class="col-md-3"><div class="molgenis-table-controls">');
		if (settings.editable) {
			items
					.push('<a class="btn btn-default btn-primary edit-table-btn" href="#" data-toggle="button" title="Edit"><span class="glyphicon glyphicon-edit"></span></a>');
			items
					.push('<a class="btn btn-default btn-success add-row-btn" style="display: none" href="#" data-toggle="button" title="Add row"><span class="glyphicon glyphicon-plus"></span></a>');
		}

		items.push('</div></div>');
		items.push('<div class="col-md-6"><div class="molgenis-table-pager"></div></div>');
		items.push('<div class="col-md-3"><div class="molgenis-table-info pull-right"></div></div>');
		items.push('</div>');
		settings.container.html(items.join(''));

		// add data to elements
		_this_.getTableMetaData(settings, function(attributes, refEntitiesMeta) {
			var visibleAttributes = [];
			for (var i = 0; i < attributes.length; ++i) {
				if (attributes[i].visible) {
					visibleAttributes.push(attributes[i]);
				}
			}

			settings.colAttributes = visibleAttributes;
			settings.refEntitiesMeta = refEntitiesMeta;

			_this_.getTableData(settings, function(data) {
				_this_.createTableHeader(settings);
				_this_.createTableBody(data, settings);
				_this_.createTablePager(data, settings);
				_this_.createTableFooter(data, settings);
			});
		});
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.getTableMetaData = function(settings, callback) {
		if (settings.attributes && settings.attributes.length > 0) {
			var colAttributes = molgenis.getAtomicAttributes(settings.attributes, restApi);
			// get meta data for referenced entities
			var refEntitiesMeta = {};
			$.each(colAttributes, function(i, attribute) {
				if (attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL'
						|| attribute.fieldType === 'CATEGORICAL_MREF') {
					refEntitiesMeta[attribute.refEntity.href] = null;
				}
			});

			var dfds = [];
			$.each(refEntitiesMeta, function(entityHref) {
				dfds.push($.Deferred(function(dfd) {
					restApi.getAsync(entityHref, {
						'expand' : [ 'attributes' ]
					}, function(entityMeta) {
						refEntitiesMeta[entityHref] = entityMeta;
						dfd.resolve();
					});
				}).promise());
			});

			// build table after all meta data for referenced entities was
			// loaded
			$.when.apply($, dfds).done(
					function() {
						// inject referenced entities meta data in attributes
						$.each(colAttributes, function(i, attribute) {
							if (attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL'
									|| attribute.fieldType === 'CATEGORICAL_MREF') {
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
	 * @memberOf MolgenisTable
	 */
	exports.getTableData = function(settings, callback) {
		var attributeNames = $.map(settings.colAttributes, function(attribute) {
			if (attribute.visible) {
				return attribute.name;
			}
		});
		var expandAttributeNames = $.map(settings.colAttributes, function(attribute) {
			if (attribute.expression) {
				if (attribute.visible) {
					return attribute.name;
				}
			}
			if (attribute.fieldType === 'XREF' || attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL_MREF') {
				// partially expand reference entities (only request label
				// attribute)
				var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
				if (attribute.visible) {
					return attribute.name;// + '[' + refEntity.labelAttribute
					// + ']';
				}
			}
			return null;
		});

		// TODO do not construct uri from other uri
		var entityCollectionUri = settings.entityMetaData.href.replace("/meta", "");
		if (settings.query) {
			var q = $.extend({}, settings.query, {
				'start' : settings.start,
				'num' : settings.maxRows,
				'sort' : settings.sort
			});
			restApi.getAsync(entityCollectionUri, {
				'attributes' : attributeNames,
				'expand' : expandAttributeNames,
				'q' : q
			}, function(data) {
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
	exports.calculateNrHeaderRows = function(attributes) {
		var level = 1;
		if (attributes) {
			var key;
			for (key in attributes) {
				if (!attributes.hasOwnProperty(key))
					continue;

				if (typeof attributes[key] === 'object') {
					var depth = _this_.calculateNrHeaderRows(attributes[key]) + 1;
					level = Math.max(depth, level);
				}
			}
		}
		return level;
	}

	/**
	 * 
	 */
	exports.createTableHeader = function(settings) {
		var container = $('.molgenis-table thead', settings.container);

		var items = [];
		if (settings.editenabled) {
			items.push($('<th>')); // edit row
			items.push($('<th>')); // delete row
		}

		// calculate number of header rows
		var nrHeaderRows = _this_.calculateNrHeaderRows(settings.expandAttributes);
		$.each(settings.colAttributes, function(i, attribute) {
			if (attribute.visible) {
				var expandCollapseControl;
				if (attribute.refEntity) {
					if (settings.expandAttributes && settings.expandAttributes[attribute.name] !== undefined) {
						expandCollapseControl = '<span data-attribute="' + attribute.name + '"class="collapse-btn glyphicon glyphicon-minus"></span>';
					} else {
						expandCollapseControl = '<span data-attribute="' + attribute.name + '"class="expand-btn glyphicon glyphicon-plus"></span>';
					}
				} else {
					expandCollapseControl = '';
				}

				createAttributeHeader(attribute, headerClass);

				if (settings.expandAttributes && settings.expandAttributes[attribute.name] !== undefined) {
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

	/**
	 * @memberOf MolgenisTable
	 */
	exports.createTableBody = function(data, settings) {
		var container = $('.molgenis-table tbody', settings.container);
		var items = [];
		var tabindex = 1;
		for (var i = 0; i < data.items.length; ++i) {
			var entity = data.items[i];
			var row = $('<tr>').data('entity', entity).data('id', entity.href);
			if (settings.editenabled) {
				// edit row button
				var cell = $('<td class="edit" tabindex="' + tabindex++ + '">');
				$('<a class="btn btn-xs btn-primary edit-row-btn" href="#" data-toggle="button" title="Edit"><span class="glyphicon glyphicon-edit"></span></button>')
						.appendTo(cell);
				row.append(cell);

				// delete row button
				var cell = $('<td class="trash" tabindex="' + tabindex++ + '">');
				$('<a class="btn btn-xs btn-danger delete-row-btn" href="#" data-toggle="button" title="Delete"><span class="glyphicon glyphicon-minus"></span></button>')
						.appendTo(cell);
				row.append(cell);
			}

			$.each(settings.colAttributes, function(i, attribute) {
				_this_.renderAttribute(entity, attribute);

				if (settings.expandAttributes && settings.expandAttributes[attribute.name] !== undefined) {
					$.each(attribute.refEntity.attributes, function(i, refAttribute) {
						var refEntity = entity[attribute.name];
						if (refEntity) {
							_this_.renderAttribute(entity[attribute.name], refAttribute);
						} else {
							row.append($('<td>'));
						}
					});
				} else {
					_this_.renderAttribute(entity, attribute);
				}
			});
			items.push(row);
		}
		container.html(items);

		$('.show-popover').popover({
			trigger : 'hover',
			placement : 'bottom',
			container : 'body'
		});
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.renderAttribute = function(entity, attribute) {
		var cell = $('<td>').data('id', entity.href + '/' + encodeURIComponent(attribute.name));
		_this_.renderCell(cell, entity, attribute, settings);
		if (settings.editenabled) {
			cell.attr('tabindex', tabindex++);
		}
		row.append(cell);
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.renderCell = function(cell, entity, attribute, settings) {
		if (settings.editenabled && !attribute.readOnly) {
			_this_.renderEditCell(cell, entity, attribute, settings);
		} else {
			_this_.renderViewCell(cell, entity, attribute, settings);
		}
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.renderEditCell = function(cell, entity, attribute, settings) {
		cell.empty();

		var value = entity[attribute.name];
		switch (attribute.fieldType) {
		case 'BOOL':
			var items = [];
			items.push('<div class="bool-btn-group btn-group-xs">');
			items.push('<button type="button" class="btn btn-default');
			if (value === true) {
				items.push(' active');
			}
			items.push('" data-state="true">Yes</button>');
			items.push('<button type="button" class="btn btn-default');
			if (value === false) {
				items.push(' active');
			}
			items.push('" data-state="false">No</button>');
			if (attribute.nillable) {
				items.push('<button type="button" class="btn btn-default');
				if (value === undefined) {
					items.push(' active');
				}
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
				id : 'href',
				allowClear : attribute.nillable ? true : false,
				placeholder : ' ', // cannot be an empty string
				initSelection : function(element, callback) {
					callback(value);
				},
				query : function(query) {
					var num = 25;
					var q = {
						q : {
							start : (query.page - 1) * num,
							num : num
						}
					};

					restApi.getAsync(refEntityCollectionUri, q, function(data) {
						query.callback({
							results : data.items,
							more : data.nextHref ? true : false
						});
					});
				},
				formatResult : format,
				formatSelection : format,
				minimumResultsForSearch : -1, // permanently hide the search
				// field
				width : '100%'
			};

			var container = $('<input type="hidden" class="ref-select">');
			cell.html(container); // first append container, then create
			// select2
			container.select2(opts).select2('val', []); // create select2 and
			// trigger initSelection
			break;
		case 'DATE':
			var datepicker = _this_.createInput(attribute, {
				'style' : 'min-width: 100px'
			}, entity[attribute.name]);
			cell.html(datepicker);
			break;
		case 'DATE_TIME':
			var datepicker = _this_.createInput(attribute, {
				'style' : 'min-width: 210px'
			}, entity[attribute.name]);
			cell.html(datepicker);
			break;
		case 'DECIMAL':
		case 'INT':
		case 'LONG':
			var input = _this_.createInput(attribute, null, entity[attribute.name]);
			input.addClass('number-input');
			cell.html(input);
			break;
		case 'CATEGORICAL_MREF': // TODO render like CATEGORICAL is rendered
			// for XREF
		case 'MREF':
			var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
			// TODO do not construct uri from other uri
			var refEntityCollectionUri = attribute.refEntity.href.replace("/meta", "");

			var format = function(item) {
				return item[refEntityMeta.labelAttribute];
			};

			// note: allowClear not possible in combination with multiple select
			var opts = {
				id : 'href',
				multiple : true,
				initSelection : function(element, callback) {
					callback(value.items);
				},
				query : function(query) {
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
						query.callback({
							results : data.items,
							more : data.nextHref ? true : false
						});
					});
				},
				formatResult : format,
				formatSelection : format,
				width : '400px' // preserve row height changes by limiting y
			// overflow
			};

			var container = $('<input type="hidden" class="ref-select">');
			cell.html(container); // first append container, then create
			// select2
			container.select2(opts).select2('val', []); // create select2 and
			// trigger initSelection
			break;
		case 'XREF':
			var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
			// TODO do not construct uri from other uri
			var refEntityCollectionUri = attribute.refEntity.href.replace("/meta", "");

			var format = function(item) {
				if (item) {
					return item[refEntityMeta.labelAttribute];
				}
			};

			var opts = {
				id : 'href',
				allowClear : attribute.nillable ? true : false,
				placeholder : ' ', // cannot be an empty string
				initSelection : function(element, callback) {
					callback(value);
				},
				query : function(query) {
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
						query.callback({
							results : data.items,
							more : data.nextHref ? true : false
						});
					});
				},
				formatResult : format,
				formatSelection : format,
				width : '100%'
			};

			var container = $('<input type="hidden" class="ref-select">');
			cell.html(container); // first append container, then create
			// select2
			container.select2(opts).select2('val', []); // create select2 and
			// trigger initSelection
			break;
		default:
			var value = entity[attribute.name];
			cell.text(value).attr('contenteditable', 'true');
			break;
		}
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.renderViewCell = function(cell, entity, attribute, settings) {
		cell.empty();
		var rawValue = entity[attribute.name];

		switch (attribute.fieldType) {
		case 'XREF':
		case 'MREF':
		case 'CATEGORICAL':
		case 'CATEGORICAL_MREF':
			if (undefined === rawValue) {
				cell.append(_this_.formatTableCellValue(undefined, undefined));
			} else {
				var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
				var refAttribute = refEntity.labelAttribute;
				var refValue = refEntity.attributes[refAttribute];

				if (refValue) {
					var refAttributeType = refValue.fieldType;
					if (refAttributeType === 'XREF' || refAttributeType === 'MREF' || refAttributeType === 'CATEGORICAL' || refAttributeType === 'CATEGORICAL_MREF'
							|| refAttributeType === 'COMPOUND') {
						throw 'unsupported field type ' + refAttributeType;
					}

					switch (attribute.fieldType) {
					case 'CATEGORICAL':
					case 'XREF':
						var $cellValue = $('<a href="#">').append(_this_.formatTableCellValue(rawValue[refAttribute], refAttributeType));
						$cellValue.click(function(event) {
							_this_.openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
							event.stopPropagation();
						});
						cell.append($cellValue);
						break;
					case 'CATEGORICAL_MREF':
					case 'MREF':
						if (!rawValue.items.length) {
							cell.append(_this_.formatTableCellValue(undefined, refAttributeType));
						} else {
							$.each(rawValue.items, function(i, rawValue) {
								var $cellValuePart = $('<a href="#">').append(_this_.formatTableCellValue(rawValue[refAttribute], refAttributeType));
								$cellValuePart.click(function(event) {
									_this_.openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
									event.stopPropagation();
								});
								if (i > 0) {
									cell.append(',');
								}
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
			cell.append(_this_.formatTableCellValue(rawValue, attribute.fieldType, undefined, attribute.nillable));
			break;
		default:
			cell.append(_this_.formatTableCellValue(rawValue, attribute.fieldType));
			break;
		}
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.openRefAttributeModal = function(attribute, refEntity, refAttribute, refValue) {
		// create modal structure
		var modal = $('#table-ref-modal');
		if (!modal.length) {
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
		if (attribute.expression) {
			// computed attribute, don't query but show the computed value
			$('.ref-table', modal).table({
				'entityMetaData' : refEntity,
				'attributes' : refAttributes,
				'value' : {
					items : [ refValue ],
					total : 1
				}
			});
		} else {
			$('.ref-table', modal).table({
				'entityMetaData' : refEntity,
				'attributes' : refAttributes,
				'query' : refQuery
			});
		}

		// show modal
		modal.modal({
			'show' : true
		});
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.persistCell = function(cell, settings) {
		var row = cell.closest('tr').index();
		var col = cell.index();
		var attribute = cell.closest('table').find('th').eq(col).data('attr');
		var value = settings.data.items[row][attribute.name];

		switch (attribute.fieldType) {
		case 'BOOL':
			var editValue;

			var state = cell.find('button.active').data('state');
			if (state === true) {
				editValue = true;
			} else if (state === false) {
				editValue = false;
			} else if (state === 'undefined' && attribute.nillable) {
				editValue = undefined;
			} else {
				throw 'invalid state: ' + state;
			}

			if (value !== editValue) {
				restApi.update(cell.data('id'), editValue, {
					success : function() {
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
			if (value !== editValue) {
				restApi.update(cell.data('id'), editValue, {
					success : function() {
						settings.onDataChange();
						if (editValue === '') {
							delete entity[attribute.name];
						} else {
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
			if (input[0].validity.valid) {
				var editValue = input.val();
				if (value !== editValue) {
					restApi.update(cell.data('id'), editValue, {
						success : function() {
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
		case 'CATEGORICAL_MREF':
		case 'MREF':
			var select = cell.find('input[type=hidden]');
			var data = select.select2('data');
			if (attribute.nillable || data.length > 0) {
				var entity = settings.data.items[row];
				var value = entity[attribute.name];
				if (JSON.stringify(data) !== JSON.stringify(value.items)) {
					var editValue = $.map(data, function(val) {
						return restApi.getPrimaryKeyFromHref(val.href);
					});
					restApi.update(cell.data('id'), editValue, {
						success : function() {
							settings.onDataChange();
							entity[attribute.name].total = data.length;
							entity[attribute.name].items = data;
							cell.addClass('edited');
						}
					});
				}
			}
			break;
		case 'CATEGORICAL':
		case 'XREF':
			var select = cell.find('input[type=hidden]');
			var data = select.select2('data');
			if (attribute.nillable || data) {
				var entity = settings.data.items[row];
				var value = entity[attribute.name] ? entity[attribute.name].href : '';
				var editValue = data ? data.href : '';

				if (value !== editValue) {
					var refEntityMeta = settings.refEntitiesMeta[attribute.refEntity.href];
					var editLabel = data ? data[refEntityMeta.labelAttribute] : '';

					editValue = editValue !== '' ? restApi.getPrimaryKeyFromHref(editValue) : '';
					restApi.update(cell.data('id'), editValue, {
						success : function() {
							settings.onDataChange();
							if (editValue === '') {
								delete entity[attribute.name];
							} else {
								if (!entity[attribute.name]) {
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
			if (value !== editValue) {
				restApi.update(cell.data('id'), editValue, {
					success : function() {
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
	 * @memberOf MolgenisTable
	 */
	exports.createTablePager = function(data, settings) {
		var container = $('.molgenis-table-pager', settings.container);

		if (data.total > settings.maxRows) {
			container.pager({
				'nrItems' : data.total,
				'nrItemsPerPage' : settings.maxRows,
				'onPageChange' : function(page) {
					settings.start = page.start;
					_this_.getTableData(settings, function(data) {
						_this_.createTableBody(data, settings);
					});
				}
			});
			container.show();
		} else {
			container.hide();
		}
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.refresh = function(settings) {
		_this_.getTableData(settings, function(data) {
			_this_.createTableBody(data, settings);
			_this_.createTablePager(data, settings);
			_this_.createTableFooter(data, settings);
		});
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.createTableFooter = function(data, settings) {
		var container = $('.molgenis-table-info', settings.container);
		container.html(data.total + ' item' + (data.total !== 1 ? 's' : '') + ' found');
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.table = function(options) {
		var container = this;

		// call plugin method
		if (typeof options == 'string') {
			var args = Array.slice.call(arguments, 1);
			if (args.length === 0)
				return container.data('table')[options]();
			else if (args.length === 1)
				return container.data('table')[options](args[0]);
		}

		// create tree container
		var settings = $.extend({}, $.fn.table.defaults, options, {
			'container' : container
		});

		// store tree settings
		container.off();
		container.empty();
		container.data('settings', settings);

		// plugin methods
		container.data('table', {
			'setAttributes' : function(attributes) {
				settings.attributes = attributes;

				// add data to elements
				_this_.getTableMetaData(settings, function(attributes, refEntitiesMeta) {
					settings.colAttributes = attributes;
					settings.refEntitiesMeta = refEntitiesMeta;

					_this_.getTableData(settings, function(data) {
						_this_.createTableHeader(settings);
						_this_.createTableBody(data, settings);
					});
				});
			},
			'setQuery' : function(query) {
				settings.query = query;
				settings.start = 0;
				_this_.refresh(settings);
			},
			'getQuery' : function() {
				return settings.query;
			},
			'getSort' : function() {
				return settings.sort;
			}
		});

		_this_.createTable(settings, function() {
			if (settings.onInit) {
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
					orders : [ {
						property : attributeName,
						direction : 'ASC'
					} ]
				};
			}

			var classUp = 'ui-icon-triangle-1-n up', classDown = 'ui-icon-triangle-1-s down', classUpDown = 'ui-icon-triangle-2-n-s updown';
			$('thead th .ui-icon', container).not(this).removeClass(classUp + ' ' + classDown).addClass(classUpDown);
			if (settings.sort.orders[0].direction === 'ASC') {
				$(this).removeClass(classUpDown + ' ' + classUp).addClass(classDown);
			} else {
				$(this).removeClass(classUpDown + ' ' + classDown).addClass(classUp);
			}

			_this_.getTableData(settings, function(data) {
				_this_.createTableBody(data, settings);
			});
		});

		$(container).on('click', 'thead th .expand-btn', function(e) {
			var attributeName = $(this).data('attribute');
			settings.expandAttributes = settings.expandAttributes || {};
			settings.expandAttributes[attributeName] = null;

			_this_.createTable(settings);
		});

		$(container).on('click', 'thead th .collapse-btn', function(e) {
			var attributeName = $(this).data('attribute');
			delete settings.expandAttributes[attributeName];

			_this_.createTable(settings);
		});

		// toggle edit table mode
		$(container).on('click', '.edit-table-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();
			if (molgenis.ie9) {
				bootbox.alert("Sorry. In-place editing is not supported in Internet Explorer 9.<br/>Please use a modern browser instead.");
				return;
			}
			settings.editenabled = !settings.editenabled;
			_this_.createTableHeader(settings);
			_this_.createTableBody(settings.data, settings);
			if (settings.editenabled) {
				_this_.createTableBody(settings.data, settings);
				$('.molgenis-table tbody').addClass('editable');
				$('.molgenis-table tbody td:not(.trash)', settings.container).first().focus();
				$('.add-row-btn').show();

				$('.edit-table-btn').html('Done');
			} else {
				$('.add-row-btn').hide();
				_this_.getTableData(settings, function(data) {
					_this_.createTableBody(data, settings);
				});
				$('.molgenis-table tbody').removeClass('editable');
				$('.edit-table-btn').html('<span class="glyphicon glyphicon-edit"></span>');
			}
		});

		// Add row
		$(container).on('click', '.add-row-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();
			_this_.getCreateForm(settings.entityMetaData);
		});

		_this_.getCreateForm(entityMetaData);

		// edit row
		$(container).on('click', '.edit-row-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();

			React.render(Form({
				entity : settings.entityMetaData.name,
				entityInstance : $(this).closest('tr').data('id'),
				mode : 'edit',
				showHidden : true,
				modal : true,
				onSubmitSuccess : function() {
					settings.start = 0;
					_this_.refresh(settings);
				}
			}), $('<div>')[0]);
		});

		// delete row
		$(container).on('click', '.delete-row-btn', function(e) {
			e.preventDefault();
			e.stopPropagation();

			if (confirm('Are you sure you want to delete this row?')) {
				var href = $(this).closest('tr').data('id');
				restApi.remove(href, {
					success : function() {
						settings.start = 0;
						_this_.refresh(settings);
					}
				});
			}
		});

		// update values on losing focus on cell
		$(container).on('keydown', '.molgenis-table tbody.editable td', function(e) {
			var cell = $(this);
			switch (e.keyCode) {
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

		// handle table cell focus out event (do not use focusout, since it
		// triggers on children taking focus)
		$(container).on('blur', '.molgenis-table tbody.editable td[contenteditable="true"]', function(e) {
			// determine if focus was lost to child:
			// http://marc.codewisp.com/2013/01/18/detecting-blur-child-elements-jquery/
			setTimeout($.proxy(function() {
				var target = document.activeElement;
				if (target !== null) {
					if ($(target).is('td')) {
						e.preventDefault();
						e.stopPropagation();
						var cell = $(this);
						_this_.persistCell(cell, settings);
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
			_this_.persistCell(cell, settings);
		});

		// CATEGORICAL
		$(container).on('change', '.molgenis-table tbody.editable .categorical-select', function(e) {
			var cell = $(this).closest('td');
			_this_.persistCell(cell, settings);
		});

		// DATE, DATE_TIME
		$(container).on('dp.change', function(e) {
			var cell = $(e.target).closest('td');
			_this_.persistCell(cell, settings);
		});

		// DECIMAL, INT, LONG
		$(container).on('change', '.molgenis-table tbody.editable .number-input', function(e) {
			var cell = $(this).closest('td');
			_this_.persistCell(cell, settings);
		});

		// XREF, MREF
		$(container).on('change', '.molgenis-table tbody.editable .ref-select', function(e) {
			var cell = $(this).closest('td');
			_this_.persistCell(cell, settings);
		});

		$(container).on('click', '.molgenis-table.table-hover tbody:not(.editable) tr', function(e) {
			// Issue #1400 ask for IdAttribute directly
			var entityData = $(this).data('entity').href.split('/');
			var entityId = decodeURIComponent(entityData.pop());
			var entityName = decodeURIComponent(entityData.pop());

			$('#entityReport').load("dataexplorer/details", {
				entityName : entityName,
				entityId : entityId
			}, function() {
				$('#entityReportModal').modal("show");

				// Button event handler when a button is placed inside an entity
				// report ftl
				$(".modal-body button", "#entityReport").on('click', function() {
					$.download($(this).data('href'), {
						entityName : entityName,
						entityId : entityId
					}, "GET");
				});
			});
		});

		return this;
	};

	/**
	 * Create input element for a molgenis data type
	 * 
	 * @param dataType
	 *            molgenis data type
	 * @param attrs
	 *            input attributes
	 * @param val
	 *            input value
	 * @param lbl
	 *            input label (for checkbox and radio inputs)
	 * 
	 * @deprecated use AttributeControl.js
	 * @memberOf MolgenisTable
	 */
	exports.createInput = function(attr, attrs, val, lbl) {
		function createBasicInput(type, attrs, val) {
			var $input = $('<input type="' + type + '">');
			if (attrs) {
				$input.attr(attrs);
			}
			if (val !== undefined) {
				$input.val(val);
			}
			return $input;
		}
		var dataType = attr.fieldType;
		var label, $input, $div, opts;
		switch (dataType) {
		case 'BOOL':
			label = $('<label class="radio">');
			$input = createBasicInput('radio', attrs, val);
			return label.append($input).append(val ? 'True' : 'False');
		case 'CATEGORICAL':
			label = $('<label>');
			$input = createBasicInput('checkbox', attrs, val);
			return $('<div class="checkbox">').append(label.append($input).append(lbl));
		case 'DATE':
		case 'DATE_TIME':
			$div = $('<div>').addClass('group-append date input-group');
			$input = createBasicInput('text', attrs, val).addClass('form-control').attr('data-date-format', dataType === 'DATE' ? 'YYYY-MM-DD' : 'YYYY-MM-DDTHH:mm:ssZZ')
					.appendTo($div);
			if (attr.nillable) {
				$input.addClass('nillable');
				$('<span>').addClass('input-group-addon').append($('<span>').addClass('glyphicon glyphicon-remove empty-date-input clear-date-time-btn')).appendTo($div);
			}
			$('<span>').addClass('input-group-addon datepickerbutton').append($('<span>').addClass('glyphicon glyphicon-calendar')).appendTo($div);
			$div.datetimepicker(dataType === 'DATE' ? {
				format : 'YYYY-MM-DD'
			} : {
				format : 'YYYY-MM-DDTHH:mm:ssZZ'
			});
			return $div;
		case 'DECIMAL':
			$input = createBasicInput('number', $.extend({}, attrs, {
				'step' : 'any'
			}), val).addClass('form-control');
			if (!attr.nillable) {
				$input.prop('required', true);
			}
			return $input;
		case 'INT':
		case 'LONG':
			opts = $.extend({}, attrs, {
				'step' : '1'
			});
			if (attr.range) {
				if (typeof attr.range.min) {
					opts.min = attr.range.min;
				}
				if (typeof attr.range.max !== 'undefined') {
					opts.max = attr.range.max;
				}
			}
			$input = createBasicInput('number', opts, val).addClass('form-control');
			if (!attr.nillable) {
				$input.prop('required', true);
			}
			return $input;
		case 'EMAIL':
			return createBasicInput('email', attrs, val).addClass('form-control');
		case 'HTML':
		case 'HYPERLINK':
		case 'STRING':
		case 'TEXT':
		case 'ENUM':
		case 'SCRIPT':
			return createBasicInput('text', attrs, val).addClass('form-control');
		case 'CATEGORICAL_MREF':
		case 'MREF':
		case 'XREF':
			return createBasicInput('hidden', attrs, val).addClass('form-control');
		case 'FILE':
		case 'IMAGE':
			throw 'Unsupported data type: ' + dataType;
		default:
			throw 'Unknown data type: ' + dataType;
		}
	}

	/**
	 * Create a table cell to show data of a certain type Is used by the
	 * dataexplorer and the forms plugin
	 * 
	 * @memberOf MolgenisTable
	 */
	exports.formatTableCellValue = function(rawValue, dataType, editable, nillable) {
		var htmlElement;

		if (dataType === undefined) {
			return '<span>&nbsp;</span>';
		}

		if (dataType.toLowerCase() == 'bool') {
			htmlElement = '<input type="checkbox" ';
			if (rawValue === true) {
				htmlElement += 'checked ';
			}
			if (editable !== true) {
				htmlElement += 'disabled="disabled"';
			}

			htmlElement += '/>';

			if (dataType.toLowerCase() == 'bool' && nillable === true && (rawValue === undefined || rawValue === '')) {
				htmlElement = $(htmlElement);
				htmlElement.prop('indeterminate', true);
			}

			return htmlElement;
		}

		if (typeof rawValue === 'undefined' || rawValue === null) {
			return '<span>&nbsp;</span>';
		}

		if (dataType.toLowerCase() === "hyperlink") {
			return htmlElement = '<a target="_blank" href="' + rawValue + '">' + htmlEscape(rawValue) + '</a>';

		} else if (dataType.toLowerCase() === "email") {
			return htmlElement = '<a href="mailto:' + rawValue + '">' + htmlEscape(rawValue) + '</a>';

		} else if (dataType.toLowerCase() != 'html') {
			if (rawValue.length > 50) {
				var abbr = htmlEscape(abbreviate(rawValue, 50));
				return htmlElement = '<span class="show-popover"  data-content="' + htmlEscape(rawValue) + '" data-toggle="popover">' + abbr + "</span>";
			} else {
				return '<span>' + htmlEscape(rawValue) + '</span>';
			}

		} else {
			return '<span>' + htmlEscape(rawValue) + '</span>';
		}
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.getCreateForm = function(entityMetaData) {
		React.render(Form({
			mode : 'create',
			showHidden : true,
			entity : entityMetaData.name,
			modal : true,
			onSubmitSuccess : function() {
				settings.start = 0;
				_this_.refresh(settings);
			}
		}), $('<div>')[0]);
	}

	/**
	 * @memberOf MolgenisTable
	 */
	exports.getDefaultTreeSettings = {
		'entityMetaData' : null,
		'maxRows' : 20,
		'attributes' : null,
		'query' : null,
		'editable' : false,
		'rowClickable' : false,
		'onDataChange' : function() {
		}
	};
});
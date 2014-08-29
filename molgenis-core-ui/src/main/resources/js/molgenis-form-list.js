(function($, molgenis) {
	"use strict";
	
	var ns = molgenis.form = molgenis.form || {};
	var restApi = new molgenis.RestClient(false);
	var NR_ROWS_PER_PAGE = 10;
	var currentPages = [];
	var selectedEntityLabelValue = null;//Value of the labelAttribute of the selected entity
	var search = null;
	
	ns.buildTableBody = function(formIndex, isPageChange) {
		var currentPage = currentPages[formIndex];
		var expands = null;
		$.each(forms[formIndex].meta.fields, function(index, field) {
			if (field.xref || field.mref) {
				if (!expands) {
					expands = [];
				}
				expands.push(field.name);
			}
		});
		
		var uri = '/api/v1/' + forms[formIndex].meta.name;
		var options = {
			start: (currentPage-1) * NR_ROWS_PER_PAGE,
			num: NR_ROWS_PER_PAGE,
			expand: expands
		};
		if ((formIndex == 0) && (search != null) && (search.value != '')) {
			options.q = [ {
				field : search.field,
				operator : search.operator,
				value : search.value
			} ];
		}
		
		if ((formIndex > 0) && (selectedEntityLabelValue != null)) {
			options.q = [ {
				field : forms[formIndex].xrefFieldName,
				operator : 'EQUALS',
				value : selectedEntityLabelValue
			} ];
		} 
		
		var entities = {};
		if ((formIndex > 0) && (selectedEntityLabelValue == null)) {
			//No selected master item, don't bother calling the api
			entities.items = [];
			entities.total = 0;
		} else {
			entities = restApi.get(uri, options);
		}
		
		var items = [];
		$.each(entities.items, function(index, entity) {
			var id = restApi.getPrimaryKeyFromHref(entity.href);
			var labelValue = entity[forms[formIndex].meta.labelFieldName];
			var editPageUrl = forms[formIndex].baseUri + '/' + id + '?back=' + encodeURIComponent(CURRENT_URI);
			var deleteApiUrl = '/api/v1/' + forms[formIndex].meta.name + '/' +  id;
				
			//Select first row when table is shown and we have master/detail
			if ((forms.length > 1) && (formIndex == 0) && (selectedEntityLabelValue == null)) {
				selectedEntityLabelValue = labelValue;
			}
				
			if (labelValue && (selectedEntityLabelValue == labelValue)) {
				items.push('<tr data-id="' + id + '" data-label="' + labelValue + '" class="info">');
			} else {
				items.push('<tr data-id="' + id + '"  data-label="' + labelValue + '">');
			}
				
			items.push('<td class="edit-entity"><a href="' + editPageUrl + '"><img src="/img/editview.gif"></a></td>');
			if (forms[formIndex].hasWritePermission) {
				items.push('<td class="delete-entity"><a href="#" class="delete-entity-' + formIndex + '" data-href="' + deleteApiUrl + '"><img src="/img/delete.png"></a></td>');
			}
				
			$.each(forms[formIndex].meta.fields, function(index, field) {
				var fieldName = field.name;
				var value = '';
					
				if (entity.hasOwnProperty(fieldName)) {
					//TODO support deeper nesting of xref fields
					if (field.mref) {
						
						$.each(entity[fieldName]['items'], function(index, mrefEntity) {
							if (index > 0) {
								value += ', ';
							}
							value += mrefEntity[field.xrefLabelName];
						});
							 
					} else if (field.xref) {
						value =  entity[fieldName][field.xrefLabelName];
							
					} else {
						value =  entity[fieldName];
					}
				} 
				
				items.push('<td>' + formatTableCellValue(value, field.type) + '</td>');
			});
				
			items.push('</tr>');
		});
		
		$('#entity-table-body-' + formIndex).html(items.join(''));
		$('.show-popover').popover({trigger:'hover', placement: 'bottom'});
			
		//Add master row click handler
		if ((forms.length > 1) && (formIndex == 0)) {
			$('#entity-table-body-' + formIndex + ' td').not($('td.edit-entity')).not('td.delete-entity').on('click', function() {
				//Remove old selection
				$('#entity-table-body-' + formIndex).find('tr.info').removeClass('info');
				
				//Color row
				var tr = $(this).parent();
				tr.addClass('info');
					
				//Update subforms
				selectedEntityLabelValue = tr.attr('data-label');
				ns.updateSubForms();
				return false;
			});
		}
			
		$('a.delete-entity-' + formIndex).on('click', function(e) {
			e.preventDefault();
			ns.deleteEntity($(this).attr('data-href'), formIndex);
			return false;
		});
			
		$('#entity-count-' + formIndex).html(entities.total);
		if(!isPageChange) {
			ns.updatePager(formIndex, entities.total, NR_ROWS_PER_PAGE);
		}
	};
	
	ns.deleteEntity = function(uri, formIndex) {
		ns.hideAlerts();
		
		if (confirm('Delete this ' + forms[formIndex].title + '?')) {
			restApi.remove(uri, {
				success: function() {
					//Refresh table
					ns.buildTableBody(formIndex);
					$('#success-message-content').html(forms[formIndex].title + ' deleted.');
					$('#success-message').show();
				},
				error: function(xhr) {
					var messages = [];
					$.each(JSON.parse(xhr.responseText).errors, function(index, error) {
						messages.push(error.message);
					});
					$('#error-message-content').html('Could not delete ' + forms[formIndex].title + '.');
					$('#error-message-details').html('Details: ' + messages.join('\n'));
					$('#error-message').show();
				}
			});
		}
	};
	
	ns.updateSubForms = function() {
		for (var i = 1; i < forms.length; i++) {
			currentPages[i] = 1;
			ns.buildTableBody(i);
			
			//Update url of create buttons of subforms so xref dropdown is preselected
			var href = forms[i].baseUri + '/create?' + forms[i].xrefFieldName + '=' + selectedEntityLabelValue + '&back=' + encodeURIComponent(CURRENT_URI);
			$('#create-' + i).attr('href', href);
		}
	};
	
	ns.hideAlerts = function() {
		$('#success-message').hide();
		$('#error-message').hide();
	};
	
	ns.updatePager = function(formIndex, nrRows, nrRowsPerPage) {
		currentPages[formIndex] = 1;
		$('#data-table-pager-' + formIndex).pager({
			'nrItems' : nrRows,
			'nrItemsPerPage': nrRowsPerPage,
			'onPageChange' : function(data) {
				currentPages[formIndex] = data.page;
				ns.buildTableBody(formIndex, true);
			}
		});
	};
	
	ns.refresh = function() {
		//Build master tables
		currentPages[0] = 1;
		ns.buildTableBody(0);
		
		//Build subforms if available
		if (forms.length > 1) {
			ns.updateSubForms();
		}
	};
	
	$(function() {

		$('#success-message .close').on('click', function() {
			$('#success-message').hide();
		});
		
		$('#error-message .close').on('click', function() {
			$('#error-message').hide();
		});
		
		ns.refresh();
		
		$('form.form-search').on('submit', function() {
			search = {
				field: $('#query-fields option:selected').attr('id'),
				operator: $('#operators option:selected').attr('id'),
				value: $('input[type=search]').val()
			};
			
			//Build master tables
			selectedEntityLabelValue = null;
			ns.refresh();
			
			return false;
		});
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
(function($, molgenis) {
	"use strict";
	
	var ns = molgenis.form = molgenis.form || {};
	var restApi = new molgenis.RestClient(false);
	var NR_ROWS_PER_PAGE = 10;
	var currentPages = [];
	var selectedEntityId = null;//Id of the selected entity in the master list
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
		
		var uri = '/api/v1/' + forms[formIndex].meta.name + '?num=' + NR_ROWS_PER_PAGE + '&start=' + (currentPage-1) * NR_ROWS_PER_PAGE;
		
		if ((formIndex == 0) && (search != null) && (search.value != '')) {
			uri += '&q[0].field=' + search.field + '&q[0].operator=' + search.operator + '&q[0].value=' + encodeURIComponent(search.value);
		}
		
		if ((formIndex > 0) && (selectedEntityId != null)) {
			uri += '&q[0].field=' + forms[formIndex].xrefFieldName + '&q[0].operator=EQUALS&q[0].value=' + selectedEntityId;
		} 
		
		var entities = {};
		if ((formIndex > 0) && (selectedEntityId == null)) {
			//No selected master item, don't bother calling the api
			entities.items = [];
			entities.total = 0;
		} else {
			entities = restApi.get(uri, expands, null);
		}
		
		var items = [];
		$.each(entities.items, function(index, entity) {
			var id = restApi.getPrimaryKeyFromHref(entity.href);
			var editPageUrl = forms[formIndex].baseUri + '/' + id + '?back=' + encodeURIComponent(CURRENT_URI);
			var deleteApiUrl = '/api/v1/' + forms[formIndex].meta.name + '/' +  id;
				
			//Select first row when table is shown and we have master/detail
			if ((forms.length > 1) && (formIndex == 0) && (selectedEntityId == null)) {
				selectedEntityId = id;
			}
				
			if (selectedEntityId == id) {
				items.push('<tr data-id="' + id + '" class="info">');
			} else {
				items.push('<tr data-id="' + id + '">');
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
				selectedEntityId = tr.attr('data-id');
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
			var href = forms[i].baseUri + '/create?' + forms[i].xrefFieldName + '=' + selectedEntityId + '&back=' + encodeURIComponent(CURRENT_URI);
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
			selectedEntityId = null;
			ns.refresh();
			
			return false;
		});
	});
	
}($, window.top.molgenis = window.top.molgenis || {}));
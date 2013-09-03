(function($, w) {
	"use strict";
	
	var molgenis = w.molgenis = w.molgenis || {};
	var ns = molgenis.form = molgenis.form || {};
	var restApi = new molgenis.RestClient();
	var NR_ROWS_PER_PAGE = 10;
	var currentPage = 1;
	
	ns.buildTableBody = function() {
		var expands = null;
		$.each(form.meta.fields, function(index, field) {
			if (field.xref || field.mref) {
				if (!expands) {
					expands = [];
				}
				expands.push(field.name);
			}
		});
		
		var uri = '/api/v1/' + form.meta.name.toLowerCase() + '?num=' + NR_ROWS_PER_PAGE + '&start=' + (currentPage-1) * NR_ROWS_PER_PAGE;
		
		restApi.getAsync(uri, expands, null, function(entities) {
			var items = [];
			
			$.each(entities.items, function(index, entity) {
				var id = 'entity-' + index;
				items.push('<tr id="' + id + '">');
				
				var editPageUrl = CONTEXT_URL + '/' + form.meta.name + '/' + restApi.getPrimaryKeyFromHref(entity.href);
				
				if (form.hasWritePermission) {
					items.push('<td><a href="' + editPageUrl + '"><img src="/img/editview.gif"></a></td>');
				}
				
				$.each(form.meta.fields, function(index, field) {
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
							value = entity[fieldName][field.xrefLabelName];
						
						} else if (field.type == 'BOOL') {
							value = entity[fieldName] ? 'yes' : 'no';
						
						} else {
							value =  entity[fieldName];
						}
					} 
					
					items.push('<td>' + $('#entity-table-body').text(value).html() + '</td>');//Html escape value
				});
				
				items.push('</tr>');
				
				if (form.hasWritePermission) {
					$(document).on('click', '#' + id, function() {
						document.location.href = editPageUrl;
					});
				}
			});
			
			$('#entity-table-body').html(items.join(''));
		
			$('#entity-count').html(entities.total);
			ns.updatePager(entities.total, NR_ROWS_PER_PAGE);
		});
	}
	
	//TODO make general pager also for dataexplorer
	ns.updatePager = function(nrRows, nrRowsPerPage) {
		$('#data-table-pager').empty();
		var nrPages = Math.ceil(nrRows / nrRowsPerPage);
		if (nrPages <= 1)
			return;

		var pager = $('#data-table-pager');
		var ul = $('<ul>');
		pager.append(ul);

		if (currentPage == 1) {
			ul.append($('<li class="disabled"><span>&laquo;</span></li>'));
		} else {
			var prev = $('<li><a href="#">&laquo;</a></li>');
			prev.click(function(e) {
				currentPage--;
				ns.buildTableBody();
				return false;
			});
			ul.append(prev);
		}

		for ( var i = 1; i <= nrPages; ++i) {
			if (i == currentPage) {
				ul.append($('<li class="active"><span>' + i + '</span></li>'));

			} else if ((i == 1) || (i == nrPages) || ((i > currentPage - 3) && (i < currentPage + 3)) || ((i < 7) && (currentPage < 5))
					|| ((i > nrPages - 6) && (currentPage > nrPages - 4))) {

				var p = $('<li><a href="#">' + i + '</a></li>');
				p.click((function(pageNr) {
					return function() {
						currentPage = pageNr;
						ns.buildTableBody();
						return false;
					};
				})(i));

				ul.append(p);
			} else if ((i == 2) || (i == nrPages - 1)) {
				ul.append($('<li class="disabled"><span>...</span></li>'));

			}
		}

		if (currentPage == nrPages) {
			ul.append($('<li class="disabled"><span>&raquo;</span></li>'));
		} else {
			var next = $('<li><a href="#">&raquo;</a></li>');
			next.click(function() {
				currentPage++;
				ns.buildTableBody();
				return false;
			});
			ul.append(next);
		}

		pager.append($('</ul>'));
	}
	
	$(function() {
		ns.buildTableBody();
	});
	
}($, window.top));
(function($, molgenis) {
	"use strict";

	function serializeMenu(list, menu) {
		list.children('li').each(function() {
			if ($(this).hasClass('highlight')) {
				var sublist = $(this).find('ol');
				var submenu = [];
				menu.push({
					'id' : $(this).data('id'),
					'label' : $(this).data('label'),
					'children' : submenu,
					'type': 'menu'
				});
				serializeMenu(sublist, submenu);

			} else {
				menu.push({
					'id' : $(this).data('id'),
					'label' : $(this).data('label'),
					'type' : 'plugin'
				});
			}
		});
	}
	
	$(function() {
		$('#menu-item-select').select2({width: 'resolve'});
		
		// create sortable menu edit control
		var oldContainer;
		$('ol.vertical').sortable({
			group : 'nested',
			handle: 'i.icon-move',
			afterMove : function(placeholder, container) {
				if (oldContainer != container) {
					if (oldContainer)
						oldContainer.el.removeClass('active');
					container.el.addClass('active');
					oldContainer = container;
				}
			},
			onDrop : function(item, container, _super) {
				container.el.removeClass('active');
				_super(item);
			}
		});
		
		// delete menu item
		var container = $('#menu-editor-container');
		$(container).on('click', '.icon-trash', function(e) {
			e.preventDefault();
			e.stopPropagation();
			$(this).closest('li').remove();
		});
		
		// edit menu item
		var editForm = $('form[name="edit-menu-item-form"]');
		
		$(container).on('click', '.icon-edit', function () {
			editForm.data('element', $(this));
			$('input[name="label"]', editForm).val($(this).closest('li').data('label'));
		});
		
		editForm.validate();
		editForm.submit(function(e) {
			e.preventDefault();
			if($(this).valid()) {
				var label = $('input[name="label"]', editForm).val();
				var element = editForm.data('element');
				element.data('label', label);
				element.closest('li').find('span').html(label);
				$('#edit-menu-item-modal').modal('hide');
			}
		});
		
		// add menu group
		var addGroupForm = $('form[name="add-menu-group-form"]');
		addGroupForm.validate();
		addGroupForm.submit(function(e) {
			e.preventDefault();
			if($(this).valid()) {
				var label = $('input[name="group-name"]', addGroupForm).val();
				if(label.length === 0) label = 'Untitled';
				
				var list = $('li.root ol', container);
				var items = [];
				items.push('<li class="node highlight">');
				items.push('<i class="icon-move"></i>');
				items.push('<span>' + label + '</span>');
				items.push('<div class="pull-right">');
				items.push('<i class="icon-edit" data-toggle="modal" data-target="#edit-menu-item-modal" data-label="' + label + '"></i>');
				items.push('<i class="icon-trash"></i>');
				items.push('</div>');
				items.push('</li>');
				list.prepend(items.join(''));
			}
		});
		
		// add menu item
		var addItemForm = $('form[name="add-menu-item-form"]');
		addItemForm.validate();
		addItemForm.submit(function(e) {
			e.preventDefault();
			if($(this).valid()) {
				var label = $('input[name="menu-item-name"]', addItemForm).val();
				if(label.length === 0) label = 'Untitled';
				
				var list = $('li.root ol', container);
				var items = [];
				items.push('<li class="node">');
				items.push('<i class="icon-move"></i>');
				items.push('<span>' + label + '</span>');
				items.push('<div class="pull-right">');
				items.push('<i class="icon-edit" data-toggle="modal" data-target="#edit-menu-item-modal" data-label="' + label + '"></i>');
				items.push('<i class="icon-trash"></i>');
				items.push('</div>');
				items.push('</li>');
				list.prepend(items.join(''));
			}
		});
		
		// create menu item group
		$('#add-menu-btn').click(function(e){
			var list = $('li.root ol', container);
			var items = [];
			items.push('<li class="node highlight">');
			items.push('<i class="icon-move"></i>');
			items.push('<span>Untitled</span>');
			items.push('<div class="pull-right">');
			items.push('<i class="icon-edit" data-toggle="modal" data-target="#edit-menu-item-modal" data-label="Untitled"></i>');
			items.push('<i class="icon-trash"></i>');
			items.push('</div>');
			items.push('</li>');
			list.prepend(items.join(''));
		});
		
		var saveMenuForm = $('form[name="save-menu-form"]');
		saveMenuForm.validate();
		saveMenuForm.submit(function(e) {
			e.preventDefault();
			e.stopPropagation();
			if($(this).valid()) {
				var list = $('li.root ol', container);
				var menu = {
					'items' : []
				};
				serializeMenu(list, menu.items);
				
				$.ajax({
					type : $(this).attr('method'),
					url : $(this).attr('action'),
					data : JSON.stringify(menu),
					contentType: 'application/json'
				}).done(function() {
					molgenis.createAlert([{'message' : 'Menu saved'}], 'success');
				});
			}
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));
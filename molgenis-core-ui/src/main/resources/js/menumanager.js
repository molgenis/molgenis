(function ($, molgenis) {
    "use strict";

    function serializeMenu(container) {
        function serializeMenuRec(list, menu) {
            list.children('li').each(function () {
                var id = $(this).data('id');
                var label = $(this).data('label');
                if ($(this).hasClass('highlight')) {
                    var sublist = $(this).children('ol');
                    var submenu = [];
                    menu.push({
                        'id': id,
                        'label': label,
                        'items': submenu,
                        'type': 'menu'
                    });
                    serializeMenuRec(sublist, submenu);

                } else {
                    menu.push({
                        'id': id,
                        'label': label,
                        'params': $(this).data('params'),
                        'type': 'plugin'
                    });
                }
            });
        }

        var list = $('ol.root', container);
        var menu = [];
        serializeMenuRec(list, menu);
        return menu[0];
    }

    $(function () {
        var menuTemplate = Handlebars.compile($("#menu-template").html());
        var itemTemplate = Handlebars.compile($("#item-template").html());

        $('#menu-item-select').select2({
            width: 'resolve'
        });

        // create sortable menu edit control
        var oldContainer = null;
        $('ol.vertical').sortable({
            group: 'nested',
            handle: 'span.glyphicon-move',
            afterMove: function (placeholder, container) {
                if (oldContainer != container) {
                    if (oldContainer)
                        oldContainer.el.removeClass('active');
                    container.el.addClass('active');
                    oldContainer = container;
                }
            },
            onDrop: function (item, container, _super) {
                container.el.removeClass('active');
                _super(item);
            }
        });

        // delete menu / menu item
        var container = $('#menu-editor-container');
        $(container).on('click', '.glyphicon-trash', function (e) {
            e.preventDefault();
            e.stopPropagation();
            $(this).closest('li').remove();
        });

        // edit menu
        var editMenuForm = $('form[name="edit-menu-form"]');
        editMenuForm.validate();
        editMenuForm.submit(function (e) {
            e.preventDefault();
            if ($(this).valid()) {
                var element = editMenuForm.data('element');
                element.replaceWith($(menuTemplate({
                    id: $('input[name="menu-id"]', editMenuForm).val(),
                    label: $('input[name="menu-name"]', editMenuForm).val()
                })).append(element.children('ol')));
                $('#edit-menu-modal').modal('hide');
            }
        });

        $(container).on('click', '.edit-menu-btn', function () {
            var element = $(this).closest('li');
            $('input[name="menu-id"]', editMenuForm).val(element.data('id'));
            $('input[name="menu-name"]', editMenuForm).val(element.data('label'));
            editMenuForm.data('element', element);
        });

        // add menu group
        var addGroupForm = $('form[name="add-menu-group-form"]');
        addGroupForm.validate();
        addGroupForm.submit(function (e) {
            e.preventDefault();
            if ($(this).valid()) {
                $('li.root>ol', container).prepend(menuTemplate({
                    id: $('input[name="menu-id"]', addGroupForm).val(),
                    label: $('input[name="menu-name"]', addGroupForm).val()
                }));
            }
        });
        $('input[name="menu-name"]', addGroupForm).keyup(function () {
            var id = $(this).val().trim().replace(/\s+/g, '').toLowerCase();
            $('input[name="menu-id"]', addGroupForm).val(id);
        });

        // add menu item
        var addItemForm = $('form[name="add-menu-item-form"]');
        addItemForm.validate();
        addItemForm.submit(function (e) {
            e.preventDefault();
            if ($(this).valid()) {
                $('li.root>ol', container).prepend(itemTemplate({
                    id: $('select[name="menu-item-select"]', addItemForm).val(),
                    label: $('input[name="menu-item-name"]', addItemForm).val(),
                    params: $('input[name="menu-item-params"]', addItemForm).val()
                }));
            }
        });

        // edit menu item
        var editItemForm = $('form[name="edit-item-form"]');
        editItemForm.validate();
        editItemForm.submit(function (e) {
            e.preventDefault();
            if ($(this).valid()) {
                editItemForm.data('element').replaceWith(itemTemplate({
                    id: $('select[name="menu-item-select"]', editItemForm).val(),
                    label: $('input[name="menu-item-name"]', editItemForm).val(),
                    params: $('input[name="menu-item-params"]', editItemForm).val()
                }));
                $('#edit-item-modal').modal('hide');
            }
        });

        $(container).on('click', '.edit-item-btn', function () {
            var element = $(this).closest('li');
            $('select[name="menu-item-select"]', editItemForm).val(element.data('id'));
            $('input[name="menu-item-name"]', editItemForm).val(element.data('label'));
            $('input[name="menu-item-params"]', editItemForm).val(element.data('params'));
            editItemForm.data('element', element);
        });

        // save menu
        var saveMenuForm = $('form[name="save-menu-form"]');
        saveMenuForm.validate();
        saveMenuForm.submit(function (e) {
            e.preventDefault();
            if ($(this).valid()) {
                $.ajax({
                    type: $(this).attr('method'),
                    url: $(this).attr('action'),
                    data: JSON.stringify(serializeMenu(container)),
                    contentType: 'application/json'
                }).done(function () {
                    location.reload();
                });
            }
        });
    });
}($, window.top.molgenis = window.top.molgenis || {}));
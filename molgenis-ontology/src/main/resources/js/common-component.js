(function ($, molgenis) {
    "use strict";
    molgenis.createModalCallback = function (title, components) {

        var modal = $('<div />').addClass('modal');
        var modalDialog = $('<div />').addClass('modal-dialog');
        var modalContent = $('<div />').addClass('modal-content');

        var header = $('<div />').css('cursor', 'pointer');
        header.addClass('modal-header');
        header.append('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
        header.append('<h3>' + title + '</h3>');

        var body = $('<div />').append(components.body);
        body.addClass('modal-body').css('overflow', 'none');

        var footer = $('<div />');
        footer.addClass('modal-footer');
        footer.append('<button class="btn btn-default" data-dismiss="modal" aria-hidden="true">Close</button>');
        footer.prepend(components.footer);

        modalContent.append(header);
        modalContent.append(body);
        modalContent.append(footer);

        modalDialog.append(modalContent).appendTo(modal);

        return modal;
    };
}($, window.top.molgenis = window.top.molgenis || {}));

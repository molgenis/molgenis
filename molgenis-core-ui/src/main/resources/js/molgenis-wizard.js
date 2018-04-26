$(function () {
    $('ul.pager a').on('click', function (e) {
        e.preventDefault();
        if (!$(this).parent().hasClass('disabled')) {
            var a = $(this);
            showSpinner(function () {
                var form = $('#wizardForm');
                form.attr('action', a.attr('href'));
                form.submit();
            });
        }

        return false;
    });

    // Call bootstrap js to give input buttons of type file a nice bootstrap look
    $('input[type=file]').bootstrapFileInput();
    $('.file-inputs').bootstrapFileInput();
});
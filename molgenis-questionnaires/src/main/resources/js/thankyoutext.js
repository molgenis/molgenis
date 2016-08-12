(function ($, molgenis) {
    "use strict";

    $(function () {
        $('#questionnairesDropdown').select2();

        $('#questionnairesDropdown').on('change', function (e) {
            document.location = $('#questionnairesDropdown').val();
        });

        $('#editButton').click(function (e) {
            document.location = $('#questionnairesDropdown').val() + '&edit=true';
        });

        if ($('#content').length > 0) {
            tinymce.init({
                selector: "textarea#content",
                theme: "modern",
                plugins: [
                    "advlist autolink lists link charmap print preview anchor",
                    "searchreplace visualblocks code fullscreen",
                    "insertdatetime table contextmenu paste"
                ],
                convert_urls: false,
                toolbar: "insertfile undo redo | styleselect fontselect fontsizeselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link",
                setup: function (ed) {
                    ed.on('change', function (e) {
                        $('#submitBtn').prop('disabled', false);
                    });
                }
            });
        }
    });

}($, window.top.molgenis = window.top.molgenis || {}));
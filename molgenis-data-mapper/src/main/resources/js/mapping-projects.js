(function ($, molgenis) {
    "use strict";
    /**
     * Updates a table's column widths.
     *
     * @param $table
     *            the scrollable table whose header and body cells should be
     *            realigned
     */
    function updateColumnWidths($table) {
        var $bodyCells = $table.find('tbody tr:first').children(),
            colWidth;

        // Get the tbody columns width array
        colWidth = $bodyCells.map(function () {
            return $(this).width();
        }).get();

        // Set the width of thead columns
        $table.find('thead tr').children().each(function (i, v) {
            $(v).width(colWidth[i]);
        });
    };

    $(function () {
        var $table = $('table.scroll');
        $(window).resize(function () {
            updateColumnWidths($table);
        });
        updateColumnWidths($table);

        $('form.verify').submit(function (e) {
            var currentForm = this;
            e.preventDefault();
            bootbox.confirm("Are you sure?", function (result) {
                if (result) {
                    currentForm.submit();
                }
            });
        });

        $('#submit-new-mapping-project-btn').click(function () {
            $('#create-new-mapping-project-form').submit();
        });

        $('#create-new-mapping-project-form').validate();

        $('select[name="target-entity"]').select2();
    });

}($, window.top.molgenis = window.top.molgenis || {}));
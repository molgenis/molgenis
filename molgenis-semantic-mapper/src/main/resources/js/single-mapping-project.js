(function ($, molgenis) {

    //A helper function to perform post-redirect action
    function redirect(method, url, data) {
        showSpinner();
        var form = '';
        if (data) {
            $.each(data, function (key, value) {
                form += '<input type="hidden" name="' + key.replace('"', '\"') + '" value="' + value.replace('"', '\"') + '">';
            });
        }
        $('<form action="' + url + '" method="' + method + '">' + form + '</form>').appendTo('body').submit();
    }

    /**
     * This message only appears when there is a attribute to curate that has no status Discuss or Curated
     * If the data is unknown the the message will not be shown
     */
    function showNextToCurateAttributeInfoMessage() {
        $.post(molgenis.getContextUrl() + "/firstattributemapping", {
            mappingProjectId: $('input[name="mappingProjectId"]').val(),
            target: $('input[name="target"]').val(),
            'skipAlgorithmStates': ['DISCUSS', 'CURATED']
        }, function (data) {
            if (data.length !== 0) {
                molgenis.createAlert([{message: 'The next attribute to map is <a id="nextAttributeMaping" style="cursor: pointer;">' + data.targetAttribute + '</a>. "Target <span class="glyphicon glyphicon-arrow-right"></span> ' + data.target + '", "Source <span class="glyphicon glyphicon-arrow-right"></span>  ' + data.source + '"'}], "info");
                $('#nextAttributeMaping').on('click', function () {
                    redirect('get', molgenis.getContextUrl() + '/attributeMapping', data);
                });
            }
        });
    }

    $(function () {
        var $table = $('table.scroll'),
            $bodyCells = $table.find('tbody tr:first').children(),
            colWidth;

        $('#attribute-mapping-table').scrollTableBody({rowsToDisplay: 10});

        $('.ace.readonly').each(function () {
            var id = $(this).attr('id'),
                editor = ace.edit(id);
            editor.setTheme("ace/theme/eclipse");
            editor.getSession().setMode("ace/mode/javascript");
            editor.setReadOnly(true);
            editor.renderer.setShowGutter(false);
            editor.setHighlightActiveLine(false);
        });

        $('form.verify').submit(function () {
            var currentForm = this;
            bootbox.confirm("Are you sure?", function (result) {
                if (result) {
                    currentForm.submit();
                }
            });
            return false;
        });

        $('#submit-new-source-column-btn').click(function () {
            $('#create-new-source-form').submit();
        });

        $('#create-integrated-entity-btn').click(function () {
            $('#create-integrated-entity-form').submit();
        });

        $('select[name="source"]').select2();

        showNextToCurateAttributeInfoMessage();

        // Adjust the width of thead cells when window resizes
        $(window).resize(function () {
            // Get the tbody columns width array
            colWidth = $bodyCells.map(function () {
                return $(this).width();
            }).get();

            // Set the width of thead columns
            $table.find('thead tr').children().each(function (i, v) {
                $(v).width(colWidth[i]);
            });
        }).resize(); // Trigger resize handler

        $.validator.addMethod(
            "regex",
            function (value, element, regexp) {
                var re = new RegExp(regexp);
                return this.optional(element) || re.test(value);
            },
            "Please check your input."
        );
        $('#newIntegratedDatasetForm').validate();
    });
}($, window.top.molgenis = window.top.molgenis || {}));
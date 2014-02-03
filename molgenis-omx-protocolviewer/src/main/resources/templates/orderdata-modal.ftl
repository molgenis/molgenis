<#-- Bootstrap order data modal for protocol viewer -->
<div id="orderdata-modal" class="modal hide" tabindex="-1">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="#orderdata-modal" data-backdrop="true"
                aria-hidden="true">&times;</button>
        <h3>Submit Study Request</h3>
    </div>
    <div class="modal-body">
    <#-- order data form -->
        <form id="orderdata-form" class="form-horizontal" enctype="multipart/form-data">
            <div class="control-group">
                <label class="control-label" for="orderdata-name">Project title *</label>

                <div class="controls">
                    <input type="text" id="orderdata-name" name="name" required>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="orderdata-file">Request form *</label>

                <div class="controls">
                    <input type="file" id="orderdata-file" name="file" required>
                </div>
            </div>
        </form>
        <div id="orderdata-selection-table-container">
            <table id="orderdata-selection-table" class="table table-striped table-condensed"></table>
        </div>
    </div>
    <div class="modal-footer">
        <a href="#" id="orderdata-btn-close" class="btn" aria-hidden="true">Cancel</a>
        <a href="#" id="orderdata-btn" class="btn btn-primary" aria-hidden="true">Submit</a>
    </div>
</div>
<script type="text/javascript">
    $(function () {
    	var nrFeatures = 0;
        var deletedFeatures = [];
        var modal = $('#orderdata-modal');
        var submitBtn = $('#orderdata-btn');
        var cancelBtn = $('#orderdata-btn-close');
        var form = $('#orderdata-form');
        var pluginUri = molgenis.getContextUrl();

    <#-- set current selected catalog -->
        if ($('#orderdata-modal-container')) {
            var catalogId = $('#orderdata-modal-container').data('catalog-id');
            if (catalogId) {
                $('#orderdata-form').prepend('<input type="hidden" name="catalogId" value="' + catalogId + '">');
            }
        }

        form.validate();

    <#-- modal events -->
        modal.on('show', function () {
        	submitBtn.addClass('disabled');
            deletedFeatures = [];
            $.ajax({
                type: 'GET',
                url: pluginUri + '/selection/' + catalogId,
                success: function (selection) {
                	nrFeatures = selection.length;
                    var container = $('#orderdata-selection-table-container');
                    if (nrFeatures === 0) {
                        container.append('<p>no variables selected</p>');
                    } else {
                    	submitBtn.removeClass('disabled');
                        var table = $('<table id="orderdata-selection-table" class="table table-striped table-condensed listtable"></table>');
                        table.append($('<thead><tr><th>Variable</th><th>Description</th><th>Remove</th></tr></thead>'));
                        var body = $('<tbody>');

                        $.each(selection, function (i, featureUri) {
                        	var feature = molgenis.Catalog.getFeature(featureUri);
                            var row = $('<tr>');
                            row.append('<td>' + feature.name + '</td>');
                            row.append('<td>' + (feature.description ? molgenis.i18n.get(feature.description) : '') + '</td>');

                            var deleteCol = $('<td class="center">');
                            var deleteBtn = $('<i class="icon-remove"></i>');
                            deleteBtn.click(function () {
                                deletedFeatures.push({
                                    'feature': feature.id
                                });
                                row.remove();
                                --nrFeatures;
                                if(nrFeatures === 0) {
                                	submitBtn.addClass('disabled');
                                	container.html('<p>no variables selected</p>');
                                }
                                // restore focus
                                form.find('input:visible:first').focus();
                            });
                            deleteBtn.appendTo(deleteCol);

                            row.append(deleteCol);
                            body.append(row);
                        });
                        table.append(body).appendTo(container);
                    }
                }
            });
        });
        modal.on('shown', function () {
            form.find('input:visible:first').focus();
        });
        modal.on('hide', function () {
            form[0].reset();
            $('#orderdata-selection-table-container').empty();

        });
        $('.close', modal).click(function (e) {<#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
            e.preventDefault();
            modal.modal('hide');
        });
        modal.keydown(function (e) {<#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
            if (e.which == 27) {
                e.preventDefault();
                e.stopPropagation();
                modal.modal('hide');
            }
        });
        cancelBtn.click(function () {
            modal.modal('hide');
        });

    <#-- form events -->
        form.submit(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (form.valid()) {
                if (deletedFeatures.length > 0) {
                    $.ajax({
                        type: 'POST',
                        url: pluginUri + '/cart/remove',
                        data: JSON.stringify({features: deletedFeatures}),
                        contentType: 'application/json',
                        success: function () {
                            order();
                        },
                        error: function (xhr) {
                            molgenis.createAlert(JSON.parse(xhr.responseText).errors, 'error', $('.modal-body', modal));
                            modal.modal('hide');
                        }
                    });
                }
                else {
                    order();
                }
            }
        });
        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if(!submitBtn.hasClass('disabled'))
            	form.submit();
        });
        $('input', form).add(submitBtn).keydown(function (e) { <#-- use keydown, because keypress doesn't work cross-browser -->
            if (e.which == 13) {
                e.preventDefault();
                e.stopPropagation();
                form.submit();
            }
        });

        function order() {
            showSpinner();
            submitBtn.addClass('disabled');
            $.ajax({
                type: 'POST',
                url: pluginUri + '/order',
                data: new FormData($('#orderdata-form')[0]),
                cache: false,
                contentType: false,
                processData: false,
                success: function () {
                    hideSpinner();
                    $(document).trigger('molgenis-order-placed', 'Your submission has been received');
                    modal.modal('hide');
                },
                error: function (xhr) {
                    hideSpinner();
                    molgenis.createAlert(JSON.parse(xhr.responseText).errors);
                    modal.modal('hide');
                }
            });
        }
    });
</script>
<#-- Bootstrap order data modal for protocol viewer -->
<div id="orderdata-modal" class="modal" tabindex="-1" aria-labelledby="orderdata-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="orderdata-modal-label">Submit Study Request</h4>
            </div>
            <div class="modal-body">
                <#-- order data form -->
                <form id="orderdata-form" class="form-horizontal" enctype="multipart/form-data">
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="orderdata-name">Project title *</label>
                        <div class="col-md-9">
                            <input type="text" class="form-control" id="orderdata-name" name="name" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-md-3 control-label" for="orderdata-file">Request form *</label>
                        <div class="col-md-9">
                            <input type="file" class="form-control" id="orderdata-file" name="file" required>
                        </div>
                    </div>
                </form>
                <div id="orderdata-selection-container">
                    <div id="orderdata-selection-table-container"></div>
                    <div id="orderdata-selection-table-pager"></div>
                </div>
            </div>
            <div class="modal-footer">
                <a href="#" id="orderdata-btn-close" class="btn btn-default" aria-hidden="true">Cancel</a>
                <a href="#" id="orderdata-btn" class="btn btn-primary" aria-hidden="true">Submit</a>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
    	var nrFeatures = 0;
        var pendingDeletes = [];
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
        modal.on('show.bs.modal', function () {
        	submitBtn.addClass('disabled');
            pendingDeletes = [];
            
            function updateFeatureSelectionContainer(page) {
	            var nrItemsPerPage = 20;
				var start = page ? page.start : 0;
				var end = page ? page.end : nrItemsPerPage;
				
				$.ajax({
					url: molgenis.getContextUrl() + '/selection/' + catalogId + '?start=' + start + '&end=' + end + '&excludes[]=' + pendingDeletes.join(','),
					success : function(selection) {
						var selectionTable = $('#orderdata-selection-table-container');
						var selectionTablePager = $('#orderdata-selection-table-pager');
						
						if(selection.total === 0) {
							submitBtn.addClass('disabled');
							selectionTable.html('<p>No variables selected</p>');
							selectionTablePager.empty();
						} else {
							submitBtn.removeClass('disabled');
								if(page === undefined) {
								selectionTablePager.pager({
									'nrItems' : selection.total,
									'nrItemsPerPage' : nrItemsPerPage,
									'onPageChange' : updateFeatureSelectionContainer
								});	
							}
							var table = $('<table id="orderdata-selection-table" class="table table-striped table-condensed listtable"></table>');
	                        table.append($('<thead><tr><th>Variable</th><th>Description</th><th>Remove</th></tr></thead>'));
	                        var body = $('<tbody>');
	
	                        $.each(selection.items, function (i, item) {
	                        	var feature = molgenis.Catalog.getFeature(item.feature);
	                            var row = $('<tr>');
	                            row.append('<td>' + feature.Name + '</td>');
	                            row.append('<td>' + (feature.description ? molgenis.i18n.get(feature.description) : '') + '</td>');
	
	                            var deleteCol = $('<td class="center">');
	                            var deleteBtn = $('<span class="glyphicon glyphicon-remove"></span>');
	                            deleteBtn.click(function () {
	                                pendingDeletes.push(feature.href);
	                                updateFeatureSelectionContainer();
	                                // restore focus
	                                form.find('input:visible:first').focus();
	                            });
	                            deleteBtn.appendTo(deleteCol);
	
	                            row.append(deleteCol);
	                            body.append(row);
	                        });
	                        table.append(body);
	                        selectionTable.html(table);
						}
					},
					error : function(xhr) {
						molgenis.createAlert(JSON.parse(xhr.responseText).errors);
					}
				});
            };
            
            // create selection table with pager
			updateFeatureSelectionContainer();
        });
        modal.on('hide.bs.modal', function () {
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
                if (pendingDeletes.length > 0) {
                    $.ajax({
                        type: 'POST',
                        url: pluginUri + '/cart/remove/' + catalogId,
                        data: JSON.stringify({features: pendingDeletes}),
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
            submitBtn.addClass('disabled');
            $.ajax({
                type: 'POST',
                url: pluginUri + '/order',
                data: new FormData($('#orderdata-form')[0]),
                cache: false,
                contentType: false,
                processData: false,
                success: function () {
                    $(document).trigger('molgenis-order-placed', 'Your submission has been received');
                    modal.modal('hide');
                },
                error: function (xhr) {
                    modal.modal('hide');
                }
            });
        }
    });
</script>
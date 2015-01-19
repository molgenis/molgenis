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
        <div id="orderdata-selection-container">
            <div id="orderdata-selection-table-container"></div>
            <div id="orderdata-selection-table-pager"></div>
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
            
            function updateFeatureSelectionContainer(page) {
	            var nrItemsPerPage = 20;
				var start = page ? page.start : 0;
				var end = page ? page.end : nrItemsPerPage;
				
				$.ajax({
					url: molgenis.getContextUrl() + '/selection/' + catalogId + '?start=' + start + '&end=' + end,
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
	                        table.append($('<thead><tr><th>Variable</th><th>Group</th></tr></thead>'));
	                        var body = $('<tbody>');
	
	                        $.each(selection.items, function (i, item) {
	                        	var protocol = molgenis.Catalog.getProtocol(item.protocol);
	                            var row = $('<tr>');
	                            row.append('<td>' + protocol.Name + '</td>');
	                            row.append('<td>' + (this.group ? this.group.map(window.htmlEscape).join(' &rarr; ') : '') + '</td>');
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
        
        modal.on('shown', function () {
            form.find('input:visible:first').focus();
        });
        
        modal.on('hide', function () {
            form[0].reset();
            $('#orderdata-selection-table-container').empty();

        });
        
        <#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
        $('.close', modal).click(function (e) {
            e.preventDefault();
            modal.modal('hide');
        });
        
        <#-- workaround: Bootstrap closes the whole stack of modals when closing one modal -->
        modal.keydown(function (e) {
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
            if (form.valid()) {order();}
        });
        
        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if(!submitBtn.hasClass('disabled'))
            	form.submit();
        });
        
        <#-- use keydown, because keypress doesn't work cross-browser -->
        $('input', form).add(submitBtn).keydown(function (e) { 
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
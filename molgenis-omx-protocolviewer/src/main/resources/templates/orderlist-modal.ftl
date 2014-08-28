<#-- Bootstrap order list modal for protocol viewer -->
<div id="orderlist-modal" class="modal" tabindex="-1" aria-labelledby="orderdata-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="orderlist-modal-label">Your Submissions</h4>
            </div>
            <div class="modal-body">
                <div id="order-list-container"></div>
            </div>
            <div class="modal-footer">
                <a href="#" id="orderlist-btn" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Ok</a>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        var modal = $('#orderlist-modal');
        var okBtn = $('#orderlist-btn');
        var pluginUri = molgenis.getContextUrl();

    <#-- modal events -->
        modal.on('shown.bs.modal', function () {
            $.ajax({
                type: 'GET',
                url: pluginUri + '/orders',
                success: function (data) {
                    var container = $('#order-list-container');
                    var items = [];
                    if (data.orders.length > 0) {
                        items.push('<table class="table">');
                        items.push('<thead><th>#<th>Study</th><th>Status</th><th></th></thead><tbody>');
                        $.each(data.orders, function (i, order) {
                        	if(order){
	                            var clazz;
	                            if (order.orderStatus === 'approved') clazz = 'success';
	                            else if (order.orderStatus === 'draft') clazz = 'warning';
	                            else if (order.orderStatus === 'submitted') clazz = 'warning';
	                            else if (order.orderStatus === 'rejected') clazz = 'error';
	                            else clazz = 'error';
	                            var containerId = 'orderdetails' + order.id + 'modal-container';
	                            items.push('<tr class=' + clazz + '>');
	                            items.push('<td>' + order.id + '</td><td>' + order.name + '</td><td>' + order.orderStatus + '</td>');
	                            items.push('<td><a class="modal-href" href="' + pluginUri + '/orders/' + order.id + '/view" data-target="' + containerId + '">view</a></td>');
	                            items.push('</tr>');
	                            modal.after($('<div id=' + containerId + '>'));
                            }
                        });
                        items.push('</tbody></table>');
                    } else {
                        items.push('<p>You did not place any orders</p>');
                    }
                    container.html(items.join(''));
                },
                error: function (xhr) {
                    molgenis.createAlert(JSON.parse(xhr.responseText).errors);
                    modal.modal('hide');
                }
            });
        });
        
        modal.on('hide.bs.modal', function (e) {
            e.stopPropagation();
            $('#order-list-container').empty();
        });

        okBtn.keydown(function (e) { <#-- use keydown, because keypress doesn't work cross-browser -->
            if (e.which == 13) {
                e.preventDefault();
                e.stopPropagation();
                modal.modal('hide');
            }
        });
    });
</script>
<#-- Bootstrap order list modal for protocol viewer -->
<div id="orderlist-modal" class="modal hide" tabindex="-1">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="#orderlist-modal" data-backdrop="true"
                aria-hidden="true">&times;</button>
        <h3>Your Submissions</h3>
    </div>
    <div class="modal-body">
        <div id="order-list-container"></div>
        <div id="orderdetails-model-container"></div>
    </div>
    <div class="modal-footer">
        <a href="#" id="orderlist-btn" class="btn btn-primary" aria-hidden="true">Ok</a>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        var modal = $('#orderlist-modal');
        var okBtn = $('#orderlist-btn');
        var pluginUri = molgenis.getContextUrl();

    <#-- modal events -->
        modal.on('shown', function () {
            $.ajax({
                type: 'GET',
                url: pluginUri + '/orders',
                success: function (data) {
                    var container = $('#order-list-container');
                    var items = [];
                    if (data.orders.length > 0) {
                        items.push('<table class="table">');
                        items.push('<thead><th>#<th>Study</th><th>Submission Date</th><th>Status</th><th></th></thead><tbody>');
                        $.each(data.orders, function (i, order) {
                            var clazz;
                            if (order.orderStatus === 'approved') clazz = 'success';
                            else if (order.orderStatus === 'draft') clazz = 'warning';
                            else if (order.orderStatus === 'submitted') clazz = 'warning';
                            else if (order.orderStatus === 'rejected') clazz = 'error';
                            else clazz = 'error';
                            var containerId = 'orderdetails' + order.id + 'modal-container';
                            items.push('<tr class=' + clazz + '>');
                            items.push('<div id="' + containerId + '"></div>')
                            items.push('<td>' + order.id + '</td><td>' + order.name + '</td><td>' + order.orderDate + '</td><td>' + order.orderStatus + '</td>');
                            items.push('<td><a class="modal-href" href="' + pluginUri + '/orders/' + order.id + '/view" data-target="' + containerId + '">view</a></td>');
                            items.push('</tr>');
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
        modal.on('hide', function () {
            $('#order-list-container').empty();

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

        okBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            modal.modal('hide');
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
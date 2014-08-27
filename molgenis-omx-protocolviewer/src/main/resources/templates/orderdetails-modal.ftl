<#-- Bootstrap order details modal -->
<div class="modal" id="orderdetails-${order.id}-modal" tabindex="-1" aria-labelledby="orderdetails-${order.id}-modal-label" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="orderdetails-${order.id}-modal-label">Submission details</h4>
            </div>
            <div class="modal-body">
                <h5>Project title: ${order.name}</h5>
                <table class="table table-striped table-condensed listtable">
                    <thead>
                    <th>Variable</th>
                    <th>Description</th>
                    </thead>
                    <tbody>
                    <#list order.items as item>
                    <tr>
                        <td><#if item.name??>${item.name}</#if></td>
                        <td><#if item.description??>${i18n.get(item.description)}</#if></td>
                    </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <a href="#" id="orderdetails-${order.id}-btn-close" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Ok</a>
            </div>
        </div>
    </div>    
</div>
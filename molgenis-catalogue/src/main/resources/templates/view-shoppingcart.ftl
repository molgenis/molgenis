<#-- Contents of the shopping cart modal -->
 <div class="modal-dialog">
 	<div class="modal-content">
    	<div class="modal-header">
        	<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
        	<h4 class="modal-title">Shopping Cart</h4>
      	</div>
      	<div class="modal-body">
        	<div id="cart-contents">
     		<#if !attributes?has_content>
			<p>Cart is empty</p>
			<#else>
				<div id="shoppingcart">
					<table class="table">
						<tr><th/><th>name</th><th>type</th><th>description</th></tr>
						<#list attributes as attribute>
						<tr>
							<td><a class="remove-attribute" data-attribute-name='${attribute.name?html}'><span class="glyphicon glyphicon-remove"></span></a></td>
							<th>${attribute.label!?html}</th>
							<td>${attribute.dataType!?html}</td>
							<td>${attribute.description!?html}</td>
						</tr>
						</#list>
					</table>
				</div>
			</#if>
			</div>
 		</div>
      	<div class="modal-footer">
        	<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        	<button type="button" class="btn btn-default" disabled="disabled">Submit</button>
        </div>
	</div>
</div>
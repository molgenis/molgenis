<div class="modal-header">
TEST HEADER
</div>
<div class="modal-body">


<#list dataRepo.iterator() as row>
	${row.getString("POS")}
</#list>


</div>
<div class="modal-footer">
	<button type="button" class="btn btn-default" data-dismiss="modal">close</button>
</div>
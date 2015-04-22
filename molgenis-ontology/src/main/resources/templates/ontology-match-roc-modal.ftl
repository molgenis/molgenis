<#macro ShowROCModal>
<div class="modal">
	<div class="modal-dialog" style="width:75%;">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title">ROC plot for ${entityName}</h4>
			</div>
			<div class="modal-body" style="margin:10px;">
				<div class="row">
					<p>
						<strong>${validatedNumber?html}</strong> out of <strong>${totalNumber?html}</strong> terms have been validated by the user.
						The plot is made based on <strong>${validatedNumber?html}</strong> validated matches. 
						<br><br>
					</p>
				</div>
				<div class="row">
					<div class="col-md-6" style="border-top-width:1px;border-top-style:solid;border-top-color:#428bca;" align="middle" > 
						<img src="/scripts/roc/run?filePath=${rocfilePath}" alt="Not available"/>
					</div>
					<div class="col-md-6">
						<table class="table">
							<tr><th>Cutoff</th><th>TPR</th><th>FPR</th></tr>
							<#list rocEntities as entity>
							<#if (entity_index < 10) || (entity_index == rocEntities?size - 1)>
							<tr><td>${entity.Cutoff}</td><td>${entity.TPR}</td><td>${entity.FPR}</td></tr>
							</#if>
							</#list>
						</table>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>
<script>
	$(document).ready(function(){
		$('.modal:first').modal({
			'backdrop' : true,
			'show' : true
		});
	});
</script>
</#macro>
<form method="post" id="wizardForm" name="wizardForm" action="" role="form">
	<#if wizard.mustChangeEntityName>
	<div class="row">
		<div class="col-md-4">
			<div class="form-group">
            	<label class="control-label" for="name">Entity name *</label>
            	<input type="text" class="form-control" name="name" required placeholder="Enter entity name">
        	</div>
        </div>
    </div>
    </#if>
	
	<div class="row">
		<div class="col-md-4">
			<div class="panel">
				<div class="panel-heading"><h4 class="panel-title">Entity options</h4></div>
				<div class="panel-body">
					<#list wizard.supportedDatabaseActions as action>
						<#if action == 'ADD_UPDATE_EXISTING'>
							<div class="radio">
								<label>
									<input type="radio" name="entity_option" value="add_update"><strong>Add entities / update existing</strong>
								</label>
							</div>
							<span>Importer adds new entities or updates existing entities<span>
						</#if>
						<#if action == 'ADD'>
							<div class="radio">
								<label>
									<input type="radio" name="entity_option" value="add" checked><strong>Add entities</strong>		
								</label>
							</div>
							<span>Importer adds new entities or fails if entity exists</span>
						</#if>
						<#if action == 'UPDATE'>
							<div class="radio">
								<label>
									<input type="radio" name="entity_option" value="update"><strong>Update entities</strong>
								</label>
							</div>
							<span>Importer updates existing entities or fails if entity does not exist</span>
						</#if>
					</#list>
				</div>
			</div>	  		
		</div>
	</div>
</form>
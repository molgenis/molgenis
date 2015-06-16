<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['']>
<#assign js=['attribute-mapping-feedback.js']>

<@header css js/>

<div class="row">
	<div class="col-md-12">
		<a href="${context_url}/attributeMapping?mappingProjectId=${mappingProjectId}&target=${target}&source=${source}&targetAttribute=${targetAttribute.name}&showSuggestedAttributes=true" class="btn btn-default btn-xs">
			<span class="glyphicon glyphicon-chevron-left"></span> Back to project
		</a>	
		<hr></hr>
	</div>
</div>

<div class="row">
	<div class="col-md-6">
	
		<div id="algorithm-result-feedback-container">
	
			<form class="form-inline">		
				<div class="form-group">
					<div id="result-text-container">Success: ${success}, Missing: ${missing}, Error: ${error}</div>
				</div>
				<div class="form-group pull-right">
					<div class="checkbox">
	    				<label>
	      					<input id="errors-only-checkbox" type="checkbox"> Errors only
	    				</label>
	  				</div>
	  				
					<div class="input-group">
	      				<span class="input-group-btn">
	        				<button id="result-search-btn" class="btn btn-default" type="button"><span class="glyphicon glyphicon-search"></button>
	      				</span>
	      				<input id="result-search-field" type="text" class="form-control" placeholder="Search">
	    			</div>
				</div>
			</form>
			<br/>
			<table class="table table-bordered">
				<thead>
				<#list sourceAttributeNames as sourceAttributeName>
					<th>${sourceAttributeName?html}</th>
				</#list>
					<th>${targetAttribute.name?html}</th>
				</thead>
				<tbody>
					<#list feedbackRows as feedbackRow>
						<tr>
							<#list sourceAttributeNames as sourceAttributeName>
								<td>${feedbackRow.sourceEntity.getString(sourceAttributeName)?html}</td>
							</#list>
							<#if feedbackRow.success>
								<#if feedbackRow.value??>
									<td>${feedbackRow.value?html}</td>
								<#else>	
									<td><i>null</i></td>
								</#if>
							<#else>
								<td>
									<button class="btn btn-sm btn-danger show-error-message" data-message="${feedbackRow.exception.message!""?html}">
										Error, click for more details
									</button>
								</td>
							</#if>
						</tr>
					</#list>
				</tbody>
			</table>
		</div>
		
	</div>
	<div class="col-md-6">
		<div id="algorithm-error-message-container"></div>
	</div>
</div>

<@footer />
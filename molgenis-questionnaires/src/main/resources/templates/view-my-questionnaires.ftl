<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=[]>

<@header css js/>

<div class="row">
	<div class="col-md-6">
		<h1>My questionnaires</h1>
		<p>Submitted and open questionnares</p>
	</div>  	
</div>
<div class="row">
	<div class="col-md-6">
		<table class="table table-bordered">
	 		<thead>
	 			<tr>
	 				<th>Questionnaire</th>
	 				<th>Status</th>
	 				<th></th>
	 			</tr>
	 		</thead>
	 		<tbody>
	 			<#list questionnaires as questionnaire>
	 				<tr>	
	 					<td>${questionnaire.label!?html}</td>
						<#if questionnaire.status == 'NOT_STARTED'>
		 					<td>Not started yet</td>
		 					<td> 
								<a class="btn btn-primary" href="${context_url}/${questionnaire.name?url('UTF-8')}">Start questionnaire</a>
							</td>
						<#elseif questionnaire.status == 'OPEN'>
							<td>Open</td>
		 					<td> 
								<a class="btn btn-primary" href="${context_url}/${questionnaire.name?url('UTF-8')}">Continue questionnaire</a>
							</td>
						<#elseif questionnaire.status == 'SUBMITTED'>
							<td>Submitted</td>
		 					<td>
		 						<a class="btn btn-primary" href="${context_url}/${questionnaire.name?url('UTF-8')}">View questionnaire</a>
		 					</td>
						</#if>
		 			</tr>
		 		</#list>
		 	</tbody>
		 </table>	
	</div>
</div>
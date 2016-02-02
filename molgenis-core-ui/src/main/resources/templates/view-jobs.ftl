<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["jobs.js"]>

<@header css js/>

<div class="row">
	<h1>JOBS HERE</h1>
	<div class="col-md-12">
		<div class="col-md-4">
			<div id="job-container">
				<h3>WHEN PENDING</h3>
				<div class="progress">
			  		<div class="progress-bar progress-bar-info" role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em; width: 100%;">
			    		Pending Import job...
			  		</div>
				</div>
				<h3>WHEN RUNNING AND KNOWN MAX</h3>
				<div class="progress">
			  		<div class="progress-bar progress-bar-striped active " role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em; width: 33%;">
			    		Importing... 100 / 300
			  		</div>
				</div>
				<h3>WHEN RUNNING AND UNKNOWN MAX</h3>
				<div class="progress">
			  		<div class="progress-bar progress-bar-striped active " role="progressbar" aria-valuenow="60" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em; width: 100%;">
			    		Importing... 100
			  		</div>
				</div>
				<div id="barbar"></div>
			</div>
		</div>
		
		<div class="col-md-4">
			<table class="table table-striped">
				<thead>
					<th>Job ID</th>
					<th>Status</th>
					<th>Username</th>
					<th>New</th>
				</thead>
				<tbody>
					<tr>
						<td>B12345</td>
						<td>Success</td>
						<td>admin</td>
						<td>New!!</td>
					</tr>
					<tr>
						<td></td>
						<td>Failed</td>
						<td>admin</td>
						<td>Seen</td>
					</tr>
				</tbody>
			</table>
		</div>
		</div>
	</div>
</div>

<@footer/>
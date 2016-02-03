<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[]>
<#assign js=["jobs.js"]>

<@header css js/>

<div class="row">
	<div class="col-md-12">
	<h2>Job and process overview</h2>
		<div class="col-md-4">
			<h3>Latest jobs</h3>
			<div id="job-container"></div>
		</div>
		
		<div class="col-md-8">
			<h3>Job history</h3>
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
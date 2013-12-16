<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["variome.css"]>
<#assign js=["variome.js"]>

<@header css js/>
		<div class="span12">
			<h2>Variome explorer</h2>
			
			<div class="row-fluid">
			
				<div class="span4">
					<form role="form" action="${context_url}/upload" method="post" enctype="multipart/form-data">
						
						<div class="form-group">
							<label>
								<input class="form-control" type="file" name="file"></input>
							</label>
						</div>
						
						<button type="submit" class="btn">Generate</button>	
					</form>	
				</div>
	
			</div>
			
			<div class="row-fluid">
				<#if path?exists>
						${path}
				</#if>	
			</div>
			
		</div>	
<@footer/>
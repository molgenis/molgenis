<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["variome.css"]>
<#assign js=["variome.js"]>

<@header css js/>
		<div class="span12">
			<h2>Variome explorer</h2>
			
			<div class="row-fluid">
			
				<div class="span4">
					<form role="form" action="${context_url}/generateTable" method="post">
						
						<div class="form-group">
							<label>
								<textarea class="form-control" name="tableInputArea" autofocus placeholder="input data"><#if userInput?exists>${userInput}</#if></textarea>
							</label>
						</div>
						
						<button type="submit" class="btn">Generate</button>	
					</form>	
				</div>
	
			</div>
			
			<div class="row-fluid">
				<#if userInput?exists>
						${userInput}
				</#if>	
			</div>
			
		</div>	
<@footer/>
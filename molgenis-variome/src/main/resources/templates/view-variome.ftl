<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["variome.css"]>
<#assign js=["variome.js"]>

<@header css js/>
	<div class="row-fluid">
		<div class="span1"></div>
		<div class="span10">
			<div class="row-fluid">	
				<div class="tab-content">
					<textarea id='inputTableArea' style="width:450px;height:100px; margin-left:100px;"></textarea>
				</div>
			</div>
		</div>
	</div>
<@footer/>
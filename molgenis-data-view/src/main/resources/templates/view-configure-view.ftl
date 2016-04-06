<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css js/>
<div class="row">
	<div class="col-md-12">
		<h1>Entity View configuration</h1>
		<p>Create, view, and edit Entity views.</p>
	</div>
</div>
<div id="view-configuration-container">
	<script>
		React.render(molgenis.ui.EntityViewContainer({
			tableContentUrl: 'EntityView'
		}), $('#view-configuration-container')[0]);
	</script>
</div>
<@footer/>
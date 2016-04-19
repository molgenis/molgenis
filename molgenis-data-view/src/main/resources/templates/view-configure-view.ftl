<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css js/>
<div id="view-configuration-container">
	<script>
		React.render(molgenis.ui.EntityViewContainer({
			tableContentUrl: 'EntityView'
		}), $('#view-configuration-container')[0]);
	</script>
</div>
<@footer/>
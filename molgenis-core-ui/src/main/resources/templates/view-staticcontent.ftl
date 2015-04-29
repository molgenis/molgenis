<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<div id="table-container" />
<script>
    React.render(molgenis.ui.Table({
        entity: '/api/v1/Entity/meta',
    }), $('#table-container')[0]);
</script>
<@footer/>

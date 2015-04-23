<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<div id="table-container" />
<script>
    React.render(molgenis.ui.Table({
        entity: '/api/v1/org_molgenis_test_TypeTest/meta',
    }), $('#table-container')[0]);
</script>
<@footer/>

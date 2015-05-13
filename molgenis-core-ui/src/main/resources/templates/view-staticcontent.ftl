<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<div id="table-container-0"></div>
<div id="table-container-1"></div>
<div id="table-container-2"></div>
<div id="table-container-3"></div>
<script>
    $(function(){
        /*React.render(molgenis.ui.Table({
            entity: 'org_molgenis_test_TypeTest',
        }), $('#table-container-0')[0]);*/
        React.render(molgenis.ui.Table({
            entity: 'CEntity',
            attrs: [{id: 'label'}, {id: 'xref', children: [{id: 'label'}]}],
            mode: 'edit',
            onRowClick: function(e){console.log(e);}
        }), $('#table-container-1')[0]);
        /*React.render(molgenis.ui.Table({
            entity: 'CEntity',
        }), $('#table-container-2')[0]);
        React.render(molgenis.ui.Table({
            entity: 'CEntity',
            attrs: [{id: 'label'}, {id: 'xref', children: [{id: 'label'}]}]
        }), $('#table-container-3')[0]);*/
    });
</script>
<@footer/>

<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<div id="btn-container"></div>
<script>
    React.render(molgenis.ui.Button({
        text: 'Load',
        style: 'primary',
        onClick: function() {
            $.post(molgenis.getContextUrl() + '/load');            
        }
    }, 'Load'), $('#btn-container')[0]);
</script>
<@footer/>
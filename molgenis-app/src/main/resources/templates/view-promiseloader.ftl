<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<div class="row">
    <div class="col-md-1">
        <div id="load-btn-container"></div>
    </div>
    <div class="col-md-1">
        <div id="map-btn-container"></div>
    </div>
</div>
<script>
    React.render(molgenis.ui.Button({
        text: 'Load',
        style: 'primary',
        onClick: function() {
            $.post(molgenis.getContextUrl() + '/load');            
        }
    }, 'Load'), $('#load-btn-container')[0]);
    React.render(molgenis.ui.Button({
        text: 'Map',
        style: 'primary',
        onClick: function() {
            $.post(molgenis.getContextUrl() + '/map');            
        }
    }, 'Map'), $('#map-btn-container')[0]);
</script>
<@footer/>
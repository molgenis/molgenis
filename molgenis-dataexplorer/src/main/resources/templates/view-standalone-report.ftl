<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css js/>

<button class="btn btn-primary" id="view-full-dataset-btn">View all ${entityTypeId}</button>
<hr>
<#include viewName+".ftl">

<@footer/>

<script>
    var entityTypeId = '${entityTypeId}'
    $('body').on('click', '#view-full-dataset-btn', function () {
        window.open(window.location.origin + molgenis.getContextUrl() + '?entity=' + entityTypeId, '_self')
    })
</script>

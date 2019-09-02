<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css js/>

<button class="btn btn-primary" id="back-btn">Back</button>
<hr>
<#include viewName+".ftl">

<@footer/>

<script>
    var entityTypeId = '${entityTypeId}'
    $('body').on('click', '#view-full-dataset-btn', function () {
        window.open(window.location.origin + molgenis.getContextUrl() + '?entity=' + entityTypeId, '_self')
    })

    $('#back-btn').click(function () {
        window.history.back();
    })
</script>

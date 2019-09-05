<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css js/>

<button class="btn btn-primary" id="back-btn">Back</button>
<hr>
<#include viewName+".ftl">

<@footer/>

<script>
    $('#back-btn').click(function () {
        window.history.back();
    })
</script>

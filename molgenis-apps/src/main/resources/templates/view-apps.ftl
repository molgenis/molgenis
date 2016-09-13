<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css js/>
<div class="row">
    <div class="col-md-12">
        <button class="btn btn-default" id="create-app-btn">Create new App</button>
    </div>
</div>


<script>
    $('#create-app-btn').on('click', function(){
        alert('click!');
    });
</script>

<@footer/>
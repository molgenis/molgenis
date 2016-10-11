<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js = ["directory.js"]>
<#assign css = []>

<@header css js />

<div class="row">
    <div class="col-md-12">
        <button id="negotiate-btn" class="btn btn-default">Negotiate!</button>
    </div>
</div>

<script>
    $(function(){
        $('#negotiate-btn').on('click', function () {
            $.ajax({
                'type': 'GET',
                'url': molgenis.getContextUrl() + '/query',
                'success': function (data) {
                   alert('success!')
                }
            });
        })
    })
</script>

<@footer />
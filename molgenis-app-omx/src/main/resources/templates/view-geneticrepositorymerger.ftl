<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<form id="genetic-repository-merger-form" class="form-horizontal">
    <div>
        Merge all repositories containing '#CHROM','POS','REF' and 'ALT' columns, result data is refresh if already existing
    </div>
    <div class="control-group">
        <div class="col-md-9">
            <h4>Merge Repositories</h4>
            <input id="genetic-repository-merge-button" type="submit" value="Merge" class="btn btn-default" style="margin-top: 20px" />
        </div>
    </div>
</form>
<script>
    var submitBtn = $('#genetic-repository-merge-button');
    var form = $('#genetic-repository-merger-form');

    submitBtn.click(function (e) {
        e.preventDefault();
        e.stopPropagation();
        form.submit();
    });

    form.submit(function (e) {
        e.preventDefault();
        e.stopPropagation();
        if (form.valid()) {
            $.ajax({
                type: 'POST',
                url: '${context_url}/mergeRepositories',
                success: function (name) {
                    molgenis.createAlert([{'message': 'Merge completed. <a href=/menu/main/dataexplorer?dataset=VKGL>Show result</a>'}], 'success');
                }
            });
        }
    });
</script>
<@footer/>
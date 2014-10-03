<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<form id="genetic-repository-merger-form" class="form-horizontal" role="form">
    <div class="well">
        <div>
            <legend>Merge repositories containing '#CHROM','POS','REF' and 'ALT' columns, result data is refresh if already existing</legend>
        </div>
        <div class="form-group">
            <div class="col-md-10">
                Result dataset name * <input type="text" name="resultDataset" required>
            </div>
        </div>
        <div class="form-group">
            <div class="col-md-10">
                <legend>Select datasets to merge:</legend>
                <table class="table table-condensed table-borderless" id="plugin-geneticerepositorymerger-table">
                    <thead>
                        <tr>
                            <th>Dataset</th>
                            <th>Selected</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list entitiesMeta.iterator() as entityMeta>
                            <tr>
                                <td><#if entityMeta.label?has_content>${entityMeta.label}<#else>${entityMeta.name}</#if></td>
                                <td><input type="checkbox" name=datasets value="${entityMeta.name}" checked></td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="form-group">
            <div class="col-md-10">
                <input id="genetic-repository-merge-button" type="submit" value="Merge" class="btn btn-default" style="margin-top: 20px" />
            </div>
        </div>
    </div>
</form>
<script>
    $(function() {
        var submitBtn = $('#genetic-repository-merge-button');
        var form = $('#genetic-repository-merger-form');

        form.submit(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (form.valid()) {
                $.ajax({
                    type: 'POST',
                    data: form.serialize(),
                    contentType: 'application/x-www-form-urlencoded',
                    url: '${context_url}/mergeRepositories',
                    success: function (name) {
                        molgenis.createAlert([{'message': 'Merge completed. <a href=/menu/main/dataexplorer?dataset='+name+'>Show result</a>'}], 'success');
                    }
                });
            }
        });

        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            form.submit();
        });
    });
</script>
<@footer/>
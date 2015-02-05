<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<@header/>
<form id="genetic-repository-merger-form" class="form-horizontal" role="form">
    <div class="well">
        <div>
            <legend>Merge repositories containing '#CHROM','POS','REF' and 'ALT' columns</legend>
        </div>
        <div class="form-group">
            <div class="col-md-10">
                Result dataset name * <input type="text" name="resultDataset" required>
            </div>
        </div>
            <div class="col-md-10">
                <div class="form-group">
                <legend>Select datasets to merge:</legend>
                    <a href="#" class="btn btn-link pull-left select-all-btn">Select all</a>
                    <a href="#" class="btn btn-link pull-left deselect-all-btn">Deselect all</a>
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
                                <td><#if entityMeta.label?has_content>${entityMeta.label?html}<#else>${entityMeta.name?html}</#if></td>
                                <td><input type="checkbox" name="datasets" value="${entityMeta.name?html}" checked></td>
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
        var restApi = new molgenis.RestClient();

        form.submit(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if($("input[name='datasets']:checked").size() < 2){
                molgenis.createAlert([
                    {'message': 'Select at least 2 datasets to merge.'}
                ], 'warning');
            }
            else if(restApi.entityExists("/api/v1/"+$('[name=resultDataset]').val())){
                molgenis.createAlert([
                    {'message': 'An entity with this name already exists.'}
                ], 'warning');
            }
            else if (form.valid()) {
                $.ajax({
                    type: 'POST',
                    data: form.serialize(),
                    contentType: 'application/x-www-form-urlencoded',
                    url: '${context_url?html}/mergeRepositories',
                    success: function (name) {
                        molgenis.createAlert([{'message': 'Merge completed. <a href=/menu/main/dataexplorer?entity='+name+'>Show result</a>'}], 'success');
                    }
                });
            }
        });

        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            form.submit();
        });

        $('.select-all-btn').click(function(e) {
            $("input[name='datasets']").each(function() {
                this.checked = true;
            });
        });

        $('.deselect-all-btn').click(function(e) {
            $("input[name='datasets']").each(function() {
                this.checked = false;
            });
        });
    });
</script>
<@footer/>
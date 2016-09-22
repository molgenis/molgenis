<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">
<#assign css=[]>
<#assign js=[]>
<@header css js/>

<form method="post" id="wizardForm" name="wizardForm" enctype="multipart/form-data"
      action="/plugin/genenetwork/import/" role="form">
    <div class="row">
        <div class="col-md-12">
            <h4>Upload a file</h4>
            Gene networks file path: <input type="text" name="genenetworkfile" data-filename-placement="inside"
                   title="Select the gene networks file...">
            Gene mapping (Ensembl to HGNC) file path: <input type="text" name="genemappingfile" data-filename-placement="inside"
                   title="Select the gene mapping file...">
            <input type="submit" value="Submit">
            <hr></hr>
        </div>
    </div>
</form>

<@footer/>
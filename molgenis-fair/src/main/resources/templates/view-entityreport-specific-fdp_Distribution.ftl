<#-- modal header -->
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h4 class="modal-title">Distribution ${entity.get("label")?html}</h4>
</div>

<#-- modal body -->
<div class="modal-body">
    <pre id="rdf">
    </pre>
</div>

<#-- modal footer -->
<div class="modal-footer">
    <button type="button" class="btn btn-default" data-dismiss="modal">close</button>
</div>
<script>
    $.get('/fdp/CATALOG/DATASET/${entity.get("identifier")}', function(data){
        $('#rdf').text(data);
    });
</script>
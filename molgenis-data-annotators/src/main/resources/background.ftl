 <div class="row">
        <div class="col-md-12">
            <div>Diagnostics portal - Your diagnosis at the push of a button!</div>
            <br>
        </div>
    </div>
    <div class="row">
        <div class="col-md-1" >&nbsp</div>
        <div class="col-md-4 well" >
            <div> Phenotypes used for this analysis result:
                <div id="terms-used-container">&nbsp;</div>
            </div>
        </div>
        <div class="col-md-1" ></div>
        <div class="col-md-4 well">
            <div> Phenotypes not used for this analysis result:
                <div id="terms-not-used-container">&nbsp;</div>
            </div>
        </div>
        <div class="col-md-2" ></div>
    </div>
    <div class="row"><div class="col-md-12">&nbsp</div></div>

<div class="row">
    <div class="col-md-12" id="data-table-container"></div>
</div>

<script>
function GetURLParameter(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) {
            return sParameterName[1];
        }
    }
}

molgenis.RestClient.prototype.getAsync('/api/v1/Project/', {'q' : [ {
            'field' : 'ID',
            'operator' : 'EQUALS',
            'value' : GetURLParameter('project')
        } ]},
        function(project) {
            var attributes = {
                '#CHROM':null,
                'POS':null,
                'REF':null,
                'ALT':null,
                'MOLGENIS_cadd':{'CADD_SCALED':null}
                //,
                //'molgenis_annotated_GeneNetwork':{
                //    'Gene network link': null,
                //    'GeneDescription': null,
                //    'weightedZScore': null
                //}
            };
            $('#terms-used-container').html(" "+project.items[0].termsFound);
            $('#terms-not-used-container').html(" "+project.items[0].termsNotFound);
            Table = React.render(molgenis.ui.Table({
                entity: project.items[0].ID,
                attrs: attributes,
                sort: {
                    attr: {
                        name: 'CADD_SCALED'
                    },
                    order: 'desc',
                    path: []
                },
                enableAdd: false,
                enableEdit: false,
                enableDelete: false,
                enableInspect: false
            }), $('#data-table-container')[0]);
        });
</script>
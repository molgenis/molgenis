<div class="row searchcontainer">
    <div class="row searchcontainer">
        <div class="col-md-12">
            <div class="searchheader noselect defaultcursor" data-reactid=".0.1.0">Diagnostics portal - Your diagnosis at the push of a button!</div>
            <div id="entity-select-container"></div>
        </div>
    </div>
    <div class="row searchcontainer">
        <div class="col-md-12">
            <div id="ontology-select-container"></div>
        </div>
    </div>
    <div class="row searchcontainer">
        <div class="col-md-6">
            <form name="upload-form" action="/plugin/importer/importFile/"
                  enctype="multipart/form-data" method="post">
                <div style="background:white;"><input type="file" name="file" data-filename-placement="inside" title="Select a file...">      </div> <br>
                <input name="uploadbutton" type="submit" value="Upload file" class="btn btn-primary disabled" style="min-width: 180px;color:#4d4d4d;background:rgb(255,225,0)">
            </form>
        </div>
        <div class="col-md-4"><div id="importRun"></div><div class="col-md-12" id="annotateRun"></div></div>
    </div>
</div>

        <form name="annotation-form" action="/annotators/annotate-data/"
              enctype="multipart/form-data" method="post">
            <input type="hidden" name="dataset-identifier" size="40">
            <input type="hidden" name="annotatorNames" size="40" value="snpEff,cadd,GeneNetwork">
        </form>

<script>
    var refreshIntervalId;
    var filename;
    var selectedProject;

    $('form[name=upload-form]').submit(function(e) {
        e.preventDefault();
        e.stopPropagation();
        if($(this).valid()) {
            $.ajax({
                type : $(this).attr('method'),
                url : $(this).attr('action'),
                data : new FormData($(this)[0]), // not supported in IE9
                contentType: false,
                processData: false,
                success: function(name) {
                    clearInterval(refreshIntervalId);
                    refreshIntervalId = setInterval(
                            function ()
                            {
                                molgenis.RestClient.prototype.getAsync('/api/v1/ImportRun/', {'q' : [ {
                                            'field' : 'id',
                                            'operator' : 'EQUALS',
                                            'value' : name
                                        } ]},
                                        function(importRun) {
                                            var container = $('#importRun');
                                            container.html("");
                                            importRun.items.forEach(function(entry) {
                                                container.append('<img id="img" src="http://2.bp.blogspot.com/-uGns9Rcyr2E/UlQDjhKxx1I/AAAAAAAADFc/0PMbw67Jwhw/s1600/progress_bar.gif" />');
                                                if(entry.status==="FINISHED"){
                                                    container.hide();
                                                    clearInterval(refreshIntervalId);
                                                    $('[name="file"]').addClass('disabled');
                                                    $('[name="uploadbutton"]').addClass('disabled');
                                                    filename = $('[name="file"]').val().split("\\").pop().slice(0, -4);
                                                    $('[name="dataset-identifier"]').val(filename);
                                                    selectedProject['entityName'] = filename;
                                                    selectedProject['Phenotypes'] = selectedPhenotypes;
                                                    selectedProject['Patient'] = selectedProject.Patient.ID;
                                                    var entities;
                                                    entities = {"entities":[selectedProject]}
                                                    $.ajax({
                                                        type: 'PUT',
                                                        url: "/api/v2/Project/",
                                                        contentType: "application/json",
                                                        data: JSON.stringify(entities)
                                                    }).done(function() {
                                                        $('form[name=annotation-form]').submit();
                                                    });
                                                }
                                            });
                                        });
                            }, 500);
                }
            });
        }
    });


    $('form[name=annotation-form]').submit(function(e) {
        e.preventDefault();
        e.stopPropagation();
        if($(this).valid()) {
            $.ajax({
                type : $(this).attr('method'),
                url : $(this).attr('action'),
                data : new FormData($(this)[0]), // not supported in IE9
                contentType: false,
                processData: false,
                success: function(name) {
                    clearInterval(refreshIntervalId);
                    refreshIntervalId = setInterval(
                            function ()
                            {
                                molgenis.RestClient.prototype.getAsync('/api/v1/AnnotationRun/', {'q' : [ {
                                            'field' : 'id',
                                            'operator' : 'EQUALS',
                                            'value' : name
                                        } ]},
                                        function(importRun) {
                                            var container = $('#annotateRun');
                                            container.html("");
                                            importRun.items.forEach(function(entry) {
                                                container.append('<img id="img" src="http://2.bp.blogspot.com/-uGns9Rcyr2E/UlQDjhKxx1I/AAAAAAAADFc/0PMbw67Jwhw/s1600/progress_bar.gif" />');
                                                if(entry.status==="FINISHED"){
                                                    container.hide();
                                                    window.location.href = "/menu/plugins/background?project="+selectedProject.ID;
                                                }
                                            });
                                        });
                            }, 2000);
                }
            });
        }
    });

    EntitySelectBox = React.render(molgenis.ui.EntitySelectBox({
        entity: 'Project',
        placeholder: "Select a project",
        onValueChange: function(val){
            selectedProject = val.value;
            $('[name="uploadbutton"]').removeClass('disabled');
        }
    }), $('#entity-select-container')[0]);

    ontologySelectBox = React.render(molgenis.ui.EntitySelectBox({
        entity: 'Ontology_OntologyTerm',
        multiple: true,
        placeholder: "Select phenotypes",
        onValueChange: function(val){
            selectedPhenotypes = val.value.map(function(a) {return a.id;});
        }
    }), $('#ontology-select-container')[0]);

</script>
   <div class="well">
       <div class="row">
            <div class="col-md-12">
                <div><h2>Diagnostics portal - Your diagnosis at the push of a button!</h2></div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div id="ontology-select-container"></div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <form name="upload-form" action="/plugin/importwizard/importFile/"
                      enctype="multipart/form-data" method="post">
                    <input type="file" name="file" data-filename-placement="inside" title="Select a file..."><br>
                    <input type="hidden" name="filename" size="40">
                    <input name="uploadbutton" type="submit" value="Diagnose!" class="btn btn-primary" style="min-width: 180px;">
                </form><br>
            </div>
            <div class="col-md-4"><div id="importRun"></div><div class="col-md-12" id="annotateRun"></div></div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <div id="progress-container" style="min-height: 120px;"></div>
            </div>
    </div>
       </div>

        <form name="annotation-form" action="/annotators/annotate-data/"
              enctype="multipart/form-data" method="post">
            <input type="hidden" name="dataset-identifier" size="40">
            <input type="hidden" name="annotatorNames" size="40" value="snpEff,cadd">
        </form>

<script>
    var refreshIntervalId;
    var filename;
    var selectedProject;

    $.get("/plugin/importwizard/uuid/")
            .done(function(uuid) {
        filename = uuid + ".vcf";
        $('[name="filename"]').val(filename);
    });

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
                                                progress.setProps({value : "Importing VCF file"});
                                                if(entry.status==="FINISHED"){
                                                    container.hide();
                                                    clearInterval(refreshIntervalId);
                                                    $('[name="file"]').addClass('disabled');
                                                    $('[name="uploadbutton"]').addClass('disabled');
                                                    filename = filename.split("\\").pop().slice(0, -4);
                                                    $('[name="dataset-identifier"]').val(filename);

                                                    selectedProject= {
                                                        'ID':filename,
                                                        'Fileame':$('[name="file"]').val(),
                                                        'Phenotypes':selectedPhenotypes,
                                                        'ImportRun': entry.id
                                                    }
                                                    var entities;
                                                    entities = {"entities":[selectedProject]}
                                                    $.ajax({
                                                        type: 'POST',
                                                        url: "/api/v2/Project/",
                                                        contentType: "application/json",
                                                        data: JSON.stringify(entities)
                                                    }).done(function() {
                                                        $('form[name=annotation-form]').submit();
                                                    });
                                                }
                                                if(entry.status==="FAILED"){
                                                    container.hide();
                                                    progress.setProps({value : entry.message});
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
                                                var text = "Annotators selected: " + entry.annotatorsSelected+"\n";
                                                text = text + "Currently annotating with: " + entry.annotatorsStarted+"\n";
                                                text = text + "Annotators already completed: " + entry.annotatorsFinished+"\n";
                                                progress.setProps({value : text});
                                                container.append('<img id="img" src="http://2.bp.blogspot.com/-uGns9Rcyr2E/UlQDjhKxx1I/AAAAAAAADFc/0PMbw67Jwhw/s1600/progress_bar.gif" />');
                                                if(entry.status==="FINISHED"){
                                                    container.hide();
                                                    window.location.href = "/menu/plugins/background?project="+selectedProject.ID;
                                                }
                                                if(entry.status==="FAILED"){
                                                    container.hide();
                                                    progress.setProps({value : entry.message});
                                                }
                                            });
                                        });
                            }, 2000);
                }
            });
        }
    });

    ontologySelectBox = React.render(molgenis.ui.EntitySelectBox({
        entity: 'Ontology_OntologyTerm',
        multiple: true,
        placeholder: "Select phenotypes",
        onValueChange: function(val){
            selectedPhenotypes = val.value.map(function(a) {return a.id;});
        }
    }), $('#ontology-select-container')[0]);

    progress = React.render(molgenis.ui.TextArea({
        readOnly: true,
        value: ""
    }), $('#progress-container')[0]);

    progress = React.render(molgenis.ui.Input({
        type: "file"
    }), $('#file-container')[0]);

</script>
$(function () {
    var form = $('#gavin-form');

    if(form.length){
        React.render(molgenis.ui.UploadContainer({
            'id': 'gavin-fileupload',
            'url': '/plugin/gavin-app/annotate-file',
            'type': 'file',
            'name': 'gavin-uploader',
            'width': '12',
            onSubmit: function(job){
                console.log('onSubmit', job);
            },
            onCompletion: function(job){
                console.log(job, job.resultUrl);
                if(job.resultUrl){
                    document.location = job.resultUrl;
                }
            }
        }), form[0]);
    } else {
        $('#gavin-view').on('click', '.glyphicon-cog', function(e) {
            var annotator = $(e.target).data('name');
            React.unmountComponentAtNode($('#form')[0]);
            React.render(molgenis.ui.Form({
                entity: 'settings_' + annotator,
                entityInstance: annotator,
                mode: 'edit',
                modal: true,
                enableOptionalFilter: false,
                enableFormIndex: false,
                onSubmitSuccess: function() {
                    location.reload();
                }
            }), $('#form')[0]);
        });
    }
});
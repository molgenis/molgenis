$(function () {
    var form = $('#gavin-form');

    if(form.length){
        React.render(molgenis.ui.UploadContainer({
            'id': 'gavin-fileupload',
            'url': '/plugin/gavin/annotate-file',
            'type': 'file',
            'name': 'gavin-uploader',
            'width': '12'
        }), form[0]);
    }
});
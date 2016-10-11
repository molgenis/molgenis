$(function(){
    $('#negotiate-btn').on('click', function () {
        $.get(molgenis.getContextUrl() + '/query', function(data) {
            console.log('uri:', data);
        });
    })
})
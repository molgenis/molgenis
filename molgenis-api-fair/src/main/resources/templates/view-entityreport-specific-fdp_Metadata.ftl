<#-- modal header -->
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h4 class="modal-title">Metadata ${entity.get("identifier")?html}</h4>
</div>

<#-- modal body -->
<div class="modal-body">
    <pre id="rdf">
    </pre>
    <hr/>
    <div id="popup-alert"></div>
    <button class="btn btn-success" onclick="pingHome()">Ping https://home.fairdatapoint.org</button>
</div>

<#-- modal footer -->
<div class="modal-footer">
    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
</div>
<script>
    $.get('/api/fdp', function (data) {
        $('#rdf').text(data);
    });

    function pingHome () {
        molgenis.showSpinner()
        $.post({
            url: 'https://home.fairdatapoint.org',
            data: JSON.stringify({
                clientUrl: window.location.origin + '/api/fdp'
            }),
            contentType: 'application/json',
            crossDomain: true,
            dataType: 'json'
        }).done(function () {
            molgenis.createAlert(
                [{message: 'Pinged https://home.fairdatapoint.org'}],
                'info',
                $('#popup-alert'))
        }).fail(function (error) {
            const alert = [{message: 'Failed to ping https://home.fairdatapoint.org'}]
            if (error.responseJSON) {
                alert.push(error.responseJSON)
            }
            molgenis.createAlert(alert,
                'error',
                $('#popup-alert'))
        }).always(function () {
            molgenis.hideSpinner()
        })
    }
</script>
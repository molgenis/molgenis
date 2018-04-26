<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
<link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
<link rel="stylesheet" href="<@resource_href "/css/bootstrap-pincode-input.css"/>" type="text/css">
<script src="<@resource_href "/js/dist/molgenis-vendor-bundle.js"/>"></script>
<script src="<@resource_href "/js/dist/molgenis-global-ui.js"/>"></script>
<script src="<@resource_href "/js/dist/molgenis-global.js"/>"></script>
<script src="<@resource_href "/js/bootstrap-pincode-input.js"/>"></script>

<script type="application/javascript">
    $(function () {
        var modal = $('#2fa-modal')
        modal.modal()

    <#-- modal events -->
        modal.on('hide.bs.modal', function (e) {
            e.stopPropagation()
            form[0].reset()
            $('.text-error', modal).remove()
            $('.alert', modal).remove()
        })
    })
</script>

<script type="application/javascript">
    $(function () {
        var modal = $('#2fa-modal')
        modal.modal()

    <#-- modal events -->
        modal.on('hide.bs.modal', function (e) {
            e.stopPropagation()
            form[0].reset()
            $('.text-error', modal).remove()
            $('.alert', modal).remove()
        })
    )
    })
</script>










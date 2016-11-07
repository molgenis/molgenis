<!doctype html>
<html lang="en">
<head>
    <title>React Redux Starter Kit</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
    <link rel="stylesheet" href="/css/app.ad10d30aa9c928ba177c14b5f8b4f4b9.css">
    <link rel="shortcut icon" href="/img/favicon.ico">
</head>
<body>
<div id="root" style="height: 100%"></div>
<script type="text/javascript">
    ___INITIAL_STATE__ = {
        session : {
            server : {
                apiUrl: '${apiUrl}',
            },
            username : '${username}'
        },
        Directory : {
            <#if nToken??>
            'nToken' : '${nToken}',
            </#if>
            <#if filters??>
            'filters' : ${filters}
            <#else>
            'filters' : {
                'materials' : [
                    {
                        operator: 'AND',
                        value : [
                            {
                                id: 'PLASMA',
                                label: 'Plasma'
                            },
                            {
                                id: 'TISSUE_FROZEN',
                                label: 'Cryo tissue'
                            }
                        ]
                    },
                    'OR',
                    {
                        value : {
                            id : 'NAV',
                            label : 'Not available'
                        }
                    }
                ]
            }
            </#if>
        }
    }
</script>
<script src="/js/vendor.3314456e224ab12c33f9.js"></script>
<script src="/js/app.2a13bba86ca4ca5c6585.js"></script>

</body>
</html>


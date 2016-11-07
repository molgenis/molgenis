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
    window.___INITIAL_STATE__ = {
        'baseUrl': '${baseUrl}',
        'session' : {
            'server' : {
                'apiUrl': '${apiUrl}'
            },
            'username' : '${username}'
        },
        'Directory' : {
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
<script src="/js/vendor.4c7f9ea304fb2662af46.js"></script>
<script src="/js/app.b03cbb62092f9d90a56f.js"></script>

</body>
</html>


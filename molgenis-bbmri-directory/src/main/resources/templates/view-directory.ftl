<!doctype html>
<html lang="en">
<head>
    <title>React Redux Starter Kit</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
</head>
<body>
<div id="root" style="height: 100%"></div>
</body>
</html>

<script type="text/javascript">
___INITIAL_STATE__ = {
    'baseUrl': '${baseUrl}',
    'session' : {
        'server' : {
            'apiUrl': '${apiUrl}'
        },
        'username' : '${username}'
    },
    'Directory' : {
        'filters' : {
            materials : [
                {
                    operator : 'AND',
                    value    : [{
                        id    : 'PLASMA', label : 'Plasma'
                    }, {
                        id    : 'TISSUE_FROZEN', label : 'Cryo tissue'
                    }]
                },
                'OR',
                {
                    value : { id : 'NAV', label : 'Not available' }
                }
            ],
            sample_access_fee : true
        }
    }
}
</script>

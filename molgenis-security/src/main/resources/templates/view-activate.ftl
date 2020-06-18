<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>Activate account</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
    <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
</head>
<body class="mg-page">
<div class="container-fluid mg-page-content">
    <div class="jumbotron jumbotron-fluid">
        <div class="container">
            <h1>Activate Account</h1>
            <form action="/account/activate" method="post">
                <div class="form-group">
                    <label for="exampleInputEmail1">Code</label>
                    <input type="text" class="form-control" id="activation-code" name="activationCode" placeholder="Activation code">
                </div>
                <button type="submit" class="btn btn-success">Activate</button>
                <div id="error-box" class="alert alert-danger" role="alert" style="display: none; margin-top: 1rem;"></div>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">
    function getParameterByName(name) {
        name = name.replace(/[\[\]]/g, '\\$&');
        var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
            results = regex.exec(window.location.href);
        if (!results) return null;
        if (!results[2]) return '';
        return decodeURIComponent(results[2].replace(/\+/g, ' '));
    }

    window.onload = function () {
        var errorMessage = getParameterByName('errorMessage');
        if(errorMessage) {
            document.getElementById('error-box').innerHTML = errorMessage;
            document.getElementById('error-box').style.display = 'block';
        }
    }
</script>
</body>
</html>	

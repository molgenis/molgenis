<#include "resource-macros.ftl">
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    <link rel="stylesheet" href="<@resource_href "/css/bootstrap.min.css"/>" type="text/css">
    <link rel="stylesheet" href="<@resource_href "/css/molgenis.css"/>" type="text/css">
</head>
<body>
<div class="container">
    <div class="row">
        <div class="column">
            <h1>Activate Account</h1>
            <form action="/account/activate" method="post">
                <div class="form-group">
                    <label for="exampleInputEmail1">Email address</label>
                    <input type="text" class="form-control" id="activation-code" name="activationCode" placeholder="Activation code">
                </div>

                <button type="submit" class="btn btn-primary">Activate Account</button>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript">

</script>
</body>
</html>	

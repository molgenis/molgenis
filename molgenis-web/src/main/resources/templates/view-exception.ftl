<!DOCTYPE html>
<html>
<head>
    <title>MOLGENIS</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="/img/favicon.ico" type="image/x-icon">
</head>
<body>
<h1>${httpStatusCode}</h1><br/>
        <#list errorMessageResponse.errors as error>
            <h2>${error.message}<#if error.code??> (${error.code})</#if></h2>
        </#list>
        <#if stackTrace??>
        <br>
            Stacktrace: <br>
            <#list stackTrace as stackTraceElement>
                ${stackTraceElement}<br>
            </#list>
        </#if>
</body>
</html>

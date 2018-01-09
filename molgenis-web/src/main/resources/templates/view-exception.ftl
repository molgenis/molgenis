<!DOCTYPE html>
<html>
    <head>
        <title>MOLGENIS</title>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta http-equiv="X-UA-Compatible" content="chrome=1">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="icon" href="<@resource_href "/img/favicon.ico"/>" type="image/x-icon">
    </head>
    <body>
<#list errorMessageResponse.errors as error>
        <h1>${error.message}<#if error.code??> (${error.code})</#if></h1>
</#list>
    </body>
</html>

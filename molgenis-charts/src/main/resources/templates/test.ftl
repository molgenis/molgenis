<html>
<head>
    <link rel="stylesheet" href="/css/jquery-ui-1.10.3.custom.css"/>
    <link rel="stylesheet" href="/css/jquery.qtip.min.css"/>
    <script src="/js/jquery-2.1.1.min.js"></script>
    <script src="/js/bootstrap.min.js"></script>

    <script type="text/javascript">
        $(function () {
            //enable error output with .fail()
            //$.getScript("http://localhost:8080/charts/heatmap.js?cA=y", function(){
            //&y=X3&y=X4&y=X5&y=X6&y=X7&y=X8&y=X9&y=X10&y=X11&y=X12&y=X13&y=X14&y=X15&y=X16&y=X17&y=X18&y=X19&y=X20
            //$.getScript("http://localhost:8080/charts/heatmap?entity=heatmap&x=genes&y=s26008&y=s4006&y=s63001&y=s28028&y=s28032&y=s31007&y=s24005&y=s19005&y=s16004&y=s15004&y=s22010&y=s24001&y=s28019&y=s30001&y=s28021&y=s15005&y=s9008&y=s11005&y=s28036&y=s62001&y=s27003&y=s26003&y=s62002&y=s65005&y=s84004&y=s3002&y=s20002&y=s12012&y=s22013&y=s37013&y=s14016&y=s27004&y=s49006&y=s24011&y=s8011&y=s62003&y=s12026&y=s31011&y=s43001&y=s24017&y=s68003&y=s12006&y=s24010&y=s24022&y=s8001&y=s12007&y=s1005", function(){
            $.getScript("http://localhost:8080/charts/heatmap?${queryString?js_string}", function () {
            }).fail(function () {
                if (arguments[0].readyState == 0) {
                    //script failed to load
                } else {
                    //script loaded but failed to parse
                    console.log(arguments[2].toString());
                }
            });
        });
    </script>
</head>
<body>

<div id="container"></div>
<div id="test"></div>
</body>
</html>
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Entity type list</title>
        <meta charset="utf-8">
    </head>
    <body>
        <h1>Entity types</h1>
        <ul id="entityTypeList"></ul>
        <script>
            var xhr = new XMLHttpRequest();
            xhr.open("GET", "/api/v2/sys_md_EntityType", true);
            xhr.onload = function (e) {
              if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                  var response = JSON.parse(xhr.responseText);
                  for(var i = 0; i < response.items.length; ++i) {
                    var item = response.items[i];
                    var node = document.createElement("LI");
                    var textNode = document.createTextNode(item.label);
                    node.appendChild(textNode);
                    document.getElementById("entityTypeList").appendChild(node);
                  }
                } else {
                  console.error(xhr.statusText);
                }
              }
            };
            xhr.onerror = function (e) {
              console.error(xhr.statusText);
            };
            xhr.send(null);
        </script>
    </body>
</html>
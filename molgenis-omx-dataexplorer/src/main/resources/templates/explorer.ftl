<!doctype html>
<html>
	<head>
		<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>
		
		<#if dataset??>
			<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
			<script>
				function search() {
					$.ajax({
						contentType: "application/json",
						method: "POST",
						url: "/search/",
						data: $('#request').val(),
						success: function( data ) {
							$('#response').val(JSON.stringify(data, undefined, 2));
						}
					});
				}
			</script>
		</#if>
	</head>
	<body>
		<h2>${message}</h2>
		<a href="/explorer/index">index datasets</a>
		
		<#if dataset??>
			<div>
				<textarea cols=75 rows=10 id="request">{entityName:"${dataset}", queryRules:[{operator:SEARCH, value:"diabetes"}]}</textarea>
				<button onclick="search()">SEARCH</button>
			</div>
			<div>
				<textarea cols=100 rows=40 id="response"></textarea>
			</div>
		</#if>
		
		
	</body>
</html>
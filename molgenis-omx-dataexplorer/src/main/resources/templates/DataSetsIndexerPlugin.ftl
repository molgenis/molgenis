<!DOCTYPE html>
<html>
	<head>
		<title>Data indexer plugin</title>
		<meta charset="utf-8">
		<link rel="stylesheet" href="/css/jquery-ui-1.9.2.custom.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap.min.css" type="text/css">
		<link rel="stylesheet" href="/css/bootstrap-datetimepicker.min.css" type="text/css">
        <#if app_href_css??>
            <link rel="stylesheet" href="${app_href_css}" type="text/css">
        </#if>
		<script type="text/javascript" src="/js/jquery-1.8.3.min.js"></script>
		<script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js"></script>
		<script type="text/javascript" src="/js/molgenis-all.js"></script>
		<script type="text/javascript" src="/js/bootstrap.min.js"></script>
		<script type="text/javascript">
			$(document).ready(function(){
				$('#select-all').click(function() {
					$('.dataset-chk').attr('checked', true);
					return false;
				});
				
				$('#deselect-all').click(function() {
					$('.dataset-chk').attr('checked', false);
					return false;
				});
				
				$('input[type=submit]').click(function(){
					var selectedDataSets = [];
					$('.dataset-chk').each(function(){
						if($(this).is(':checked')){
							selectedDataSets.push($(this).val());
						} 
					});
					var indexRequest = {
						'selectedDataSets' : selectedDataSets
					};
					$.ajax({
						type : 'POST',
						url : '/plugin/dataindexer/index',
						async : false,
						data : JSON.stringify(indexRequest),
						contentType : 'application/json',
					}).done(function(response){
						$('#alertMessage').remove();
						var alertMessage = $('<div id="alertMessage" class="alert"></div>');
						var button = $('<button type="button" class="close" data-dismiss="alert">&times;</button>');
						var textMessage = $('<p />');
						alertMessage.append(button).append(textMessage);
						if(!response.isRunning)
							alertMessage.addClass('alert-error');
						else
							alertMessage.addClass('alert-success');
						textMessage.html('<strong>Message : </strong>' + response.message).css('text-align', 'center');
						$('.dataset-chk').attr('checked', false);
						$('body div:eq(0)').before(alertMessage);
					});
					return false;
				});
			});
		</script>
	</head>
	<body>
		<div class="container-fluid">
			<div class="row-fluid">
				<div class="span9">
				<h4>Index datasets:</h4>
				<div style="width:400px">
					<a href="#" id="deselect-all" style="float:right;margin-left:10px">Deselect all</a>
					<a href="#" id="select-all" style="float:right">Select all</a>
				</div>
				<div class="well" style="width: 400px; max-height:400px; overflow:auto">
					<#list dataSets as dataSet>
						<label style="display: block; padding-left: 15px;">
							<input id="d${dataSet.id?c}" class="dataset-chk" type="checkbox" name="dataset" value="${dataSet.id?c}" /> ${dataSet.name}
						</label> 
					</#list>
				</div>
				<input type="submit" value="Start indexing" class="btn" style="margin-top: 20px" />
				</div>
			</div>
		</div>
	</body>
</html>
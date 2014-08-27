(function($, molgenis) {
	
	var stages = ['DeleteMapping', 'CreateMapping', 'StoreMapping'];
	var restApi = new molgenis.RestClient();
	
	molgenis.AlgorithmReport = function AlgorithmReport(){};
	
	molgenis.AlgorithmReport.prototype.progress = function(infoDiv, currentStatus) {
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + '/progress',
			contentType : 'application/json',
			success : function(response){
				
				//Clear the info from previous check
				infoDiv.empty();
				//Query information for the target dataset and source datasets
				var targetDataSet = restApi.get('/api/v1/dataset/' + response.targetDataSetId);
				var selectedSourceDataSets = [];
				if(response.selectedDataSetIds.length > 0){
					var sourceDataSets = restApi.get('/api/v1/dataset/', {
						'q' : {
							'q' : [{
								'field' : 'id',
								'operator' : 'IN',
								'value' :response.selectedDataSetIds
							}]
						}
					});
					$.each(sourceDataSets.items, function(index, dataSet){
						selectedSourceDataSets.push(dataSet.Name);
					});
				}
				var targetDataSetDiv = $('<div />').addClass('row').appendTo(infoDiv);
				var sourceDataSetDiv = $('<div />').addClass('row').appendTo(infoDiv);
				
				$('<div />').addClass('col-md-5').append('<strong>Desired items : </strong>').appendTo(targetDataSetDiv);
				$('<div />').addClass('col-md-offset-2 col-md-5').append('<div class="float-right">' + targetDataSet.Name + '</div>').appendTo(targetDataSetDiv);
				$('<div />').addClass('col-md-5').append('<strong>Selected catalogues : </strong>').appendTo(sourceDataSetDiv);
				$('<div />').addClass('col-md-offset-2 col-md-5').append('<div class="float-right">' + selectedSourceDataSets.join(',') + '</div>').appendTo(sourceDataSetDiv);
				
				//Create link out to the harmonized dataset when the value generation is done
				if(response.derivedDataSetId)
				{
					var derivedDataSet = restApi.get('/api/v1/dataset/' + response.derivedDataSetId);
					var derivedDataSetLinkOutDiv = $('<div />').addClass('row').appendTo(infoDiv);
					$('<div />').addClass('col-md-5').append('<strong>Link out to harmonized data :  </strong>').appendTo(derivedDataSetLinkOutDiv);
					$('<div />').addClass('col-md-offset-2 col-md-5').append('<div class="float-right"><a href="/menu/main/dataexplorer?dataset=' + derivedDataSet.Identifier + '">' + derivedDataSet.Identifier + '</a></div>').appendTo(derivedDataSetLinkOutDiv);
				}
				//Update progressbar accordingly
				if(response.isRunning)
				{
					$.each(stages, function(index, stage){						
						currentStatus[stage].show();
						if(response.stage === stage){
							return false;
						}else{
							var innerProgressBar = currentStatus[stage].find('div.bar:eq(0)');
							$(innerProgressBar).width('100%').parents('div:eq(0)').removeClass('active');
							$(innerProgressBar).append('<p style="font-size:14px;padding-top:4px;">Finished!</p>');
						}
					});
					var progressBar = currentStatus[response.stage].children('.progress:eq(0)');					
					var width = $(progressBar).find('.bar:eq(0)').width();
					var parentWidth = $(progressBar).find('.bar:eq(0)').parent().width();
					var percent = (100 * width / parentWidth) + (1 / response.totalUsers);
                    if(percent < response.matchPercentage) percent = response.matchPercentage;
                    progressBar.find('div.bar:eq(0)').width((percent > 100 ? 100 : percent) + '%');
					
					if(response.totalUsers > 1){
						var warningDiv = null;
						if($('#other-user-alert').length > 0) warningDiv = $('#other-user-alert');
						else warningDiv = $('<div id="other-user-alert" class="row" style="margin-bottom:10px;"></div>');
						warningDiv.empty().append('<div class="span12"><span style="display: block;font-size:16px;text-align:center;">Other users are using BiobankConnect, it might slow down the process. Please be patient!</span></div>');
						$('body').find('.progress:eq(0)').parents('div:eq(0)').before(warningDiv);
					}else{
						$('#other-user-alert').remove();
					}

					setTimeout(function(){
						molgenis.AlgorithmReport.prototype.progress(infoDiv, currentStatus);
					}, 3000);
				}else {
					$.each(currentStatus, function(stageName, progressBar){
						progressBar.show();
						var innerProgressBar = progressBar.find('div.bar:eq(0)');
						$(innerProgressBar).width('100%').parents('div:eq(0)').removeClass('active');
						$(innerProgressBar).append('<p style="font-size:14px;padding-top:4px;">Finished!</p>');
					});
					$('ul.pager li').removeClass('disabled');
				}
			}
		});
	};
}($, window.top.molgenis = window.top.molgenis || {}));
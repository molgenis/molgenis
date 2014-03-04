(function($, molgenis) {
	
	var restApi = new molgenis.RestClient();
	var searchApi = new molgenis.SearchClient();
	var algorithmEditor = new molgenis.AlgorithmEditor();
	
	var editorHeight = 400;
	
	molgenis.hrefToId = function (href){
		return href.substring(href.lastIndexOf('/') + 1); 
	};
	
	molgenis.BiobankAnalysis = function BiobankAnalysis(){};
	
	molgenis.BiobankAnalysis.prototype.retrieveAnalyses = function (analysisRequest, container){
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/retrievescript',
			async : false,
			data : JSON.stringify(analysisRequest),
			contentType : 'application/json',
			success : function(data, textStatus, request){	
				var parentDiv = $('#table-container');
				if(parentDiv.length == 0)
					parentDiv = $('<div />').attr('id', 'table-container');
				parentDiv.empty().appendTo(container);
				
				var analyses = data['analyses'];
				if(Object.keys(analyses).length == 0){
					parentDiv.append('No datasets are selected for this analysis and please choose a dataset from pulldown!')
					return;
				}
				var table = $('<table />').attr('class', 'table table-bordered');
				var tableHeader = $('<thead><th style="width:20%;">Edit</th><th style="width:30%;">DataSet</th><th>Script</th><th>Status</th></thead>');
				var tableBody = $('<tbody />');
				$.each(analyses, function(index, eachAnalysis){
					var row = $('<tr />');
					var editButton = $('<button />').attr({
						'type' : 'button',
						'class' : 'btn'
					}).append('<i class="icon-pencil"></i>');
					var runningButton = $('<button />').attr({
						'type' : 'button',
						'class' : 'btn'
					}).append('<i class="icon-play"></i>');
					var removeButton = $('<button />').attr({
						'type' : 'button',
						'class' : 'btn'
					}).css('float', 'right').append('<i class="icon-trash"></i>');
					
					var dataSet = restApi.get('/api/v1/dataset/' + eachAnalysis.sourceDataSetId);
					$('<td />').append(editButton).append(' ').append(runningButton).append(removeButton).appendTo(row);
					$('<td />').append(dataSet.name).appendTo(row);
					$('<td />').append(eachAnalysis.script).appendTo(row);
					$('<td />').append('').appendTo(row);
					tableBody.append(row);
					editButton.click(function(){ 
						container.hide();
						var editorContainer = $('<div />').addClass('row-fluid');
						var header = $('<div />').addClass('row-fluid').appendTo(editorContainer);
						var content = $('<div />').addClass('row-fluid').appendTo(editorContainer);
						var footer = $('<div />').addClass('row-fluid').appendTo(editorContainer);
						container.parents('div:eq(0)').after(editorContainer);
						editorHeader(dataSet, header);
						retrieveAllFeatures(dataSet, content);
						var editor = initEditor(eachAnalysis, content, container);
						editorFooter(eachAnalysis, editor, footer, container);
					});
					runningButton.click(function(){
						console.log('runningButton is clicked!');
					});
					removeButton.click(function(){
						console.log('removeButton is clicked!');
					});
				});
				table.append(tableHeader).append(tableBody).appendTo(parentDiv);
			},
			error : function(request, textStatus, error){
				console.log(error);
			}
		});
	};
	
	function retrieveAllFeatures(dataSet, editorContainer){
		var request = {
			documentType : 'protocolTree-' + molgenis.hrefToId(dataSet.href),
			query : {
				rules : [[{
					field : 'type',
					operator : 'EQUALS',
					value : 'observablefeature'
				}]]
			}
		};
		searchApi.search(request, function(searchResponse){
			console.log($('body').height());
			var catalogDiv = $('<div />').addClass('span6 well').css({
				'overflow-y' : 'scroll',
				'height' : editorHeight
			}).appendTo(editorContainer);
			
			var hits = searchResponse.searchHits;
			if(hits.length > 0){
				algorithmEditor.createTableForRetrievedMappings(searchResponse.searchHits, catalogDiv, null);
			}else{
				catalogDiv.append('There are no data items!');
			}
		});
	}
	
	function editorHeader(dataSet, header){
		var layoutDiv = $('<div />').addClass('span12');
		var divRow = $('<div />').addClass('row-fluid').appendTo(layoutDiv);
		$('<div />').addClass('span6').append('Define analysis script for : <strong>' + dataSet.name + '</strong>').appendTo(divRow);
		$('<div />').addClass('span6').append('<strong>Data items</strong>').appendTo(divRow);
		layoutDiv.appendTo(header);
	}
	
	function editorFooter(eachAnalysis, editor, footer, container){
		var saveButton = $('<button class="btn btn-primary" type="button">Save</button>');
		var backButton = $('<button class="btn" type="button">Back</button>');
		var editorContainer = footer.parents('div:eq(0)');
		footer.append(saveButton).append(' ').append(backButton);
		
		backButton.click(function(){
			editorContainer.remove();
			container.show();
		});
		
		saveButton.click(function(){
			eachAnalysis['script'] = editor.getValue();
			saveScript(eachAnalysis, editorContainer, container);
		});
	}
	
	function initEditor (eachAnalysis, editorContainer){
		var algorithmEditorDiv = $('<div id="algorithmEditorDiv"></div>').addClass('span6 well').css('height', editorHeight).appendTo(editorContainer);
		var dataSet = restApi.get('/api/v1/dataset/' + eachAnalysis.sourceDataSetId);
		
		var langTools = ace.require("ace/ext/language_tools");
		var editor = ace.edit('algorithmEditorDiv');
		editor.setOptions({
		    enableBasicAutocompletion: true
		});
		editor.setValue(eachAnalysis.script);
		editor.setTheme("ace/theme/chrome");
		editor.getSession().setMode("ace/mode/javascript");
		var algorithmEditorCompleter = {
	        getCompletions: function(editor, session, pos, prefix, callback) {
	            if (prefix.length === 0) { callback(null, []); return }
	            searchApi.search(searchFeatureByName('protocolTree-' + molgenis.hrefToId(dataSet.href), prefix), function(searchResponse){
	            	console.log(searchResponse);
                    callback(null, searchResponse.searchHits.map(function(hit) {
                    	var map = hit.columnValueMap;
                        return {name: '$(\'' + map.name + '\')', value: '$(\'' + map.name + '\')', score: map.score, meta: dataSet.name};
                    }));
	            });
	        }
	    }
		langTools.addCompleter(algorithmEditorCompleter);
		return editor;
	}
	
	function saveScript(analysisRequest, editorContainer, container){
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/savescript',
			async : false,
			data : JSON.stringify(analysisRequest),
			contentType : 'application/json',
			success : function(data, textStatus, request){	
				console.log(data);
				molgenis.BiobankAnalysis.prototype.retrieveAnalyses(analysisRequest, container);
				editorContainer.remove();
				container.show();
			},
			error : function(request, textStatus, error){
				console.log(error);
			}
		});
	}
	
	function searchFeatureByName(documentType, prefix){
		var queryRules = [];
        queryRules.push({
        	field : 'name',
			operator : 'LIKE',
			value : prefix
		});
        queryRules.push({
        	operator : 'AND'
        });
        queryRules.push({
        	field : 'type',
			operator : 'EQUALS',
			value : '"observablefeature"'
        });
        var autoCompletionSearchRequest = {
    		documentType : documentType,
    		query : {
				pageSize: 10000,
				rules: [queryRules]
			}
        };
        return autoCompletionSearchRequest;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));
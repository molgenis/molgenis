(function ($, molgenis) {
    "use strict";
    var restApi = new molgenis.RestClient();
    var selectedEntity;
    var selectedEntityName;
    var inputArray = [];
 
    $(function () {
    	/* the naming of the classes for these divs can be confusing.
    	** some naming conflicts with that of bootstrap, so here are the
    	** names that are used for various element classes:
    	** input-group: bootstrap class for an input group
    	** term-input: the text input for HPO terms
    	** term-group: a group of inputs
    	** term-is-recursive: checkbox for recursiveness
    	*/
    	
		function getInputHtml(id, group) {
			var html = ['<div class="input-group" id="input-group-'+group+'-'+ id +'">',
				'<span class="input-group-addon">Term</span>',
				'<input type="text" class="form-control" id="term-input-'+group+'-'+ id +'" placeholder="HP:1234567">',
				'<span class="input-group-addon">',
				'<span data-toggle="tooltip" data-placement="right" title="Search undelying terms"><input type="checkbox" class="term-is-recursive" id="term-is-recursive-'+ group +'-'+ id +'" checked> <span class="glyphicon glyphicon-option-vertical"></span></span>',
				'</span>',
				'</div>',
				].join('');
				return html;
		}
		
		function getGroupHtml(group) {
			var html1 = ['<div class="panel panel-primary term-group" id="term-group-'+ group +'">',
			'<div class="btn-group pull-right">'
			].join('');
			var html2 = '<button class="btn btn-primary rem-group" id="rem-group-'+group+'"><span class="glyphicon glyphicon-trash"></span></button>';
			var html3 = ['<button class="btn btn-success add-input" id="add-input-'+group+'"><span class="glyphicon glyphicon-plus"></span></button>',
			'<button class="btn btn-danger rem-input" id="rem-input-'+group+'"><span class="glyphicon glyphicon-minus"></span></button>',
			'</div>',
			'<div class="panel-heading">',
			'<h4 class="panel-title" id="panel-title-'+group+'">Group '+(inputArray.length)+'</h4>',
			'</div>',
			'</div>'
			].join('');
			var html = html1;
			if (group != 0) {
				html = html.concat(html2);
			}
			html = html.concat(html3);
			return html;
		}
		
		// adds an input to a group
		function addInput(group) {
			$('#term-group-'+group).append(getInputHtml(inputArray[group], group));
			inputArray[group]++;
       		$('[data-toggle="tooltip"]').tooltip();
		}
		
		// removes an input from a group
		function removeInput(group) {
			if (inputArray[group] > 1) {
				var j = inputArray[group] - 1;
				$('#input-group-'+group+'-'+j).remove();
				inputArray[group]--;
			}
		}
		
		// adds a group
		function addGroup() {
			inputArray.push(0);
			$('#inputs').append(getGroupHtml(inputArray.length-1));
			addInput(inputArray.length - 1);
		}
		
		/* removes a group and does a whole lot of
		** stuff to make the IDs appropriate again */
		function removeGroup(group) {
			$('#term-group-'+(group)).remove();
			for (var i = group; i < inputArray.length-1; i++) {
				var j = parseInt(i) + parseInt(1);
				document.getElementById('term-group-'+j).setAttribute("id", "term-group-"+i);
				document.getElementById('panel-title-'+j).setAttribute("id", "panel-title-"+i);
				document.getElementById('rem-group-'+j).setAttribute("id", "rem-group-"+i);
				document.getElementById('rem-input-'+j).setAttribute("id", "rem-input-"+i);
				document.getElementById('add-input-'+j).setAttribute("id", "add-input-"+i);
				$('#inputs').find('[id|="input-group-'+j+'"]').each(function() {
					var split = this.id.split('-');
					this.setAttribute('id', 'input-group-'+i+'-'+split[3]);
					document.getElementById('term-input-'+j+'-'+split[3]).setAttribute('id', 'term-input-'+i+'-'+split[3]);
				});
				$('#panel-title-'+(i)).html('Group '+j);
			}
			inputArray.splice(group, 1);
		}
    	
    	// listen for click events on the add- and remove-input buttons
    	$('#inputs').on('click', '.add-input', function() {
			addInput(this.id.substring(10));
    	});
    	$('#inputs').on('click', '.rem-input', function() {
			removeInput(this.id.substring(10));
    	});
    	$('#inputs').on('click', '.rem-group', function() {
			removeGroup(this.id.substring(10));
    	});
    	
    	// same for the add- and remove-group buttons
    	$('#addgroup').on('click', null, function() {
    		addGroup();
    	});	
    	
    	// enables the tooltips that exist on the page when it loads
        $('[data-toggle="tooltip"]').tooltip();
        
        // loads the details of an entity when clicked
        $('.entity-dropdown-item').click(function() {
            var entityUri = $(this).attr('id');
            load(entityUri);
        });

		// listen for input on hpo terms for autocompletion
        /**$('#term-input').on('keyup paste', null, function(){
            $('#term-input').dropdown('toggle');
            $.get(molgenis.getContextUrl() + '/ac',
                {search:$('#term-input').val()},
                function(data){
                $('#ac-menu').html(data);
            });
        });*/
        
        function alertEntityNotSelected() {
        
        }
        
        $('#filter-submit').on('click', null, function() {
        	if (selectedEntity) {
	        	var termArray = [];
	        	// loop through each *term* group
	        	$('.term-group').each(function() {
	        		// loop through each collection of inputs
	        		$(this).find('.input-group').each(function () {
	        			var id = this.id.substring(12);
	        			var rec = document.getElementById('term-is-recursive-'+id).checked ? true : false;
	        			var value = document.getElementById('term-input-'+id).value;
	        			termArray.push(id + '-' + rec + '-' + value);
	        		});
	        		
	        	});
	            $.post(molgenis.getContextUrl() + '/filter',
	            {
	            terms:termArray.join(),
	            entity:selectedEntity.label, 
	            target:document.getElementById('name-input').value
	            });
			}else{
				alertEntityNotSelected();
			}
        });
        
        addGroup();
    });
        
    if (selectedEntityName) {
        load('/api/v1/' + selectedEntityName);
    }
    
    function load(entityUri) {
        restApi.getAsync(entityUri + '/meta', {'expand': ['attributes']}, function(entityMetaData) {
            selectedEntity = entityMetaData;
            createHeader(entityMetaData);
        });
    }
    
    function createHeader(entityMetaData) {
        $('#filter-title').html("Filtering '"+entityMetaData.label+"' by HPO");
        $('#dropdown-menu-entities').html(entityMetaData.label+" <span class=\"caret\"></span>");
    }
}($, window.top.molgenis = window.top.molgenis || {}));
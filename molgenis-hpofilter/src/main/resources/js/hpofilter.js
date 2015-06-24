(function ($, molgenis) {
    "use strict";
    var restApi = new molgenis.RestClient();
    var selectedEntity;
    var selectedEntityName;
    var inputCount = 0;
 
    $(function () {
		function getHtml(id) {
			var html = ['<div class="input-group" id="term-group-'+id+'">',
				'<span class="input-group-addon">Term</span>',
				'<input type="text" class="form-control" placeholder="HP:1234567" id="term-input-'+id+'">',
				'<span class="input-group-addon">',
				'<input type="checkbox" data-toggle="tooltip" data-placement="bottom" title="Search undelying terms"><span class="glyphicon-repeat"></span>',
				'</span>',
				'<span class="input-group-btn">',
				'<button class="btn btn-primary add" id="add-term-'+id+'">+</button>',
				'<button class="btn btn-danger rem" id="remove-term-'+id+'">-</button>',
				'</span>',
				'</div>'
				].join();
				return html;
		}
		inputCount++;
		$('#inputs').append(getHtml(inputCount));
    
    	$('.add').on('click', null, function() {
    		inputCount++;
    		$('#inputs').append(getHtml(inputCount));
    	});

    	$('.rem').on('click', null, function() {
    		if (inputCount > 1) {
    			$('#term-group-'+inputCount).remove();
    			inputCount--;
    		}
    	});    	
    	
        $('[data-toggle="tooltip"]').tooltip();
        
        $('.entity-dropdown-item').click(function() {
            var entityUri = $(this).attr('id');
            load(entityUri);
        });

        /**$('#term-input').on('keyup paste', null, function(){
            $('#term-input').dropdown('toggle');
            $.get(molgenis.getContextUrl() + '/ac',
                {search:$('#term-input').val()},
                function(data){
                $('#ac-menu').html(data);
            });
        });*/
        
        $('#filter-submit').on('click', null, function() {
            $.post(molgenis.getContextUrl() + '/filter',
            {terms:$('#term-input').val(), 
            entity:selectedEntity.label, 
            target:$('#name-input').val(), 
            recursive:true},
            function(data){
                console.log(data);
            });
        });
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
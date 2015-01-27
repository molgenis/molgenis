$(function() {
	var template = Handlebars.compile($("#attr-filter-form-template").html());
	
	var query = {
		'xdate': JSON.parse('{"operator":"NESTED","nestedRules":[{"field":"xdate","operator":"GREATER_EQUAL","value":"2015-01-21"},{"operator":"OR"},{"field":"xdate","operator":"LESS_EQUAL","value":"2015-01-22"},{"operator":"OR"},{"operator":"NESTED","nestedRules":[{"field":"xdate","operator":"GREATER_EQUAL","value":"2015-01-23"},{"operator":"AND"},{"field":"xdate","operator":"LOWER_EQUAL","value":"2015-01-24"}]}]}')
	}
	$.get('/api/v1/org_molgenis_test_TypeTest/meta?expand=attributes').done(function(meta) {
		var $container = $('#input-container');
		var keys = Object.keys(meta.attributes).sort();
		
		for(var i = 0; i < keys.length; ++i) {
			if(!meta.attributes.hasOwnProperty(keys[i])) continue;

			var attr = meta.attributes[keys[i]];
			if(attr.name === 'id') continue;
			
		    var type = 'x';
		    $container.append(template({
		    	name: attr.name,
		    	type: type
		    }));
// 		    try {
    		    molgenis.filters.create(attr, {
                    'onQueryChange' : function(q) {
                        $('#query-result-header').html('Filter query: ');
                    	$('#query-result').html(JSON.stringify(q, null, '\t'));
                    }
                }, $('#' + attr.name + type + '-filter-container'));
// 		    } catch(err) {
// 		    	console.log(err);
// 		    }
		}
		/**
		var $valueContainer = $('#value-input-container');
		for(var key in meta.attributes) {
            var attr = meta.attributes[key];
            if(attr.name === 'id') continue;
            
            var type = 'x';
            $valueContainer.append(template({
                name: attr.name,
                type: type
            }));
            //try {
                molgenis.filters.create(attr, {
                    'onQueryChange' : function(q) {
                        $('#query-result-header').html('Filter query: ');
                        $('#query-result').html(JSON.stringify(q, null, '\t'));
                    }
                }, $('#' + attr.name + type + '-value-filter-container'));
            //} catch(err) {
                //console.log(err);
            //}
        }*/
	});	
});
$(function() {
	var template = Handlebars.compile($("#attr-filter-form-template").html());
	
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

			molgenis.filters.create(attr, {
				'onQueryChange' : function(q) {
					$('#query-result-header').html('Filter query: ');
					$('#query-result').html(JSON.stringify(q, null, '\t'));
				}
			}, $('#' + attr.name + type + '-filter-container'));
		}

		var queries = {
			'xbool': {field: 'xbool', operator: 'EQUALS', value: true},
			'xboolnillable': {field: 'xboolnillable', operator: 'EQUALS', value: null},
			'xcategorical_value': {field: 'xcategorical_value', operator: 'EQUALS', value: {value: 'ref1', label: 'label1'}},
			'xcategoricalnillable_value': {field: 'xcategorical_value', operator: 'EQUALS', value: [null]},
			'xdate': {operator: 'NESTED', nestedRules: [
				{field: 'xdate', operator: 'GREATER_EQUAL', value: '2012-01-02'},
				{operator: 'OR'},
				{field: 'xdate', operator: 'LESS_EQUAL', value: '2013-01-02'}]},
			'xdatenillable': {operator: 'NESTED', nestedRules: [
				{field: 'xdate', operator: 'RANGE', value: ['2012-01-02','2013-01-02']},
				{operator: 'OR'},
				{field: 'xdate', operator: 'EQUALS', value: null}]},
			'xdatetime': {operator: 'NESTED', nestedRules: [
				{field: 'xdatetime', operator: 'GREATER_EQUAL', value: '2012-01-02T01:23:45+0100'},
				{operator: 'OR'},
				{field: 'xdatetime', operator: 'LESS_EQUAL', value: '2013-01-02T01:23:45+0100'}]},
			'xdatetimenillable': {operator: 'NESTED', nestedRules: [
				{field: 'xdatetime', operator: 'RANGE', value: ['2012-01-02T01:23:45+0100','2013-01-02T01:23:45+0100']},
				{operator: 'OR'},
				{field: 'xdatetime', operator: 'EQUALS', value: null}]},
			'xdecimal': {field: 'xdecimal', operator: 'GREATER_EQUAL', value: 3.1415},
			'xdecimalnillable': {operator: 'NESTED', nestedRules: [
				{field: 'xdecimalnillable', operator: 'RANGE', value: [3.1415, 8]},
				{operator: 'OR'},
				{field: 'xdecimalnillable', operator: 'EQUALS', value: null}]},
			'xemail': {field: 'xemail', operator: 'EQUALS', value: 'test@mail.com'},
			'xemailnillable': {operator: 'NESTED', nestedRules: [
				{field: 'xemailnillable', operator: 'EQUALS', value: 'test0@mail.com'},
				{operator: 'OR'},
				{field: 'xemailnillable', operator: 'EQUALS', value: 'test1@mail.com'},
				{operator: 'OR'},
				{field: 'xemailnillable', operator: 'EQUALS', value: null}]},
			'xenum': {field: 'xenum', operator: 'EQUALS', value: ['enum1']},
			'xenumnillable': {operator: 'NESTED', nestedRules: [
				{field: 'xenumnillable', operator: 'EQUALS', value: ['enum1']},
				{operator: 'OR'},
				{field: 'xenumnillable', operator: 'EQUALS', value: ['enum2']},
				{operator: 'OR'},
				{field: 'xenumnillable', operator: 'EQUALS', value: [null]}]},
			'xhtml': {field: 'xhtml', operator: 'EQUALS', value: '<h1>cool html</h1>'},
			'xhtmlnillable': {operator: 'NESTED', nestedRules: [
				{field: 'xhtmlnillable', operator: 'EQUALS', value: '<h1>cool html</h1>'},
				{operator: 'OR'},
				{field: 'xhtmlnillable', operator: 'EQUALS', value: '<span>text</span>'},
				{operator: 'OR'},
				{field: 'xhtmlnillable', operator: 'EQUALS', value: null}]},
			'xhyperlink': {field: 'xhyperlink', operator: 'EQUALS', value: 'http://www.molgenis.org/'},
			'xhyperlinknillable': {operator: 'NESTED', nestedRules: [
				{field: 'xhyperlinknillable', operator: 'EQUALS', value: 'http://www.molgenis.org/page0'},
				{operator: 'OR'},
				{field: 'xhyperlinknillable', operator: 'EQUALS', value: 'http://www.molgenis.org/page1'},
				{operator: 'OR'},
				{field: 'xhyperlinknillable', operator: 'EQUALS', value: null}]},
			'xint': {field: 'xint', operator: 'GREATER_EQUAL', value: 3},
			'xintnillable': {operator: 'NESTED', nestedRules: [
				{field: 'xintnillable', operator: 'RANGE', value: [3, 8]},
				{operator: 'OR'},
				{field: 'xintnillable', operator: 'EQUALS', value: null}]},
			'xintrange': {field: 'xintrange', operator: 'RANGE', value: [3, 8]},
			'xintrangenillable': {operator: 'NESTED', nestedRules: [
				{field: 'xintrangenillable', operator: 'RANGE', value: [3, 8]},
				{operator: 'OR'},
				{field: 'xintrangenillable', operator: 'EQUALS', value: null}]},
			'xlong': {field: 'xlong', operator: 'GREATER_EQUAL', value: 3},
			'xlongnillable': {operator: 'NESTED', nestedRules: [
				{field: 'xlongnillable', operator: 'RANGE', value: [3, 8]},
				{operator: 'OR'},
				{field: 'xlongnillable', operator: 'EQUALS', value: null}]},				
			'xlongrange': {field: 'xlongrange', operator: 'RANGE', value: [3, 8]},
			'xlongrangenillable': {operator: 'NESTED', nestedRules: [
				{field: 'xlongrangenillable', operator: 'RANGE', value: [3, 8]},
				{operator: 'OR'},
				{field: 'xlongrangenillable', operator: 'EQUALS', value: null}]},
			'xmref_value': {field: 'xmref_value', operator: 'EQUALS', value: {value: 'ref1', label: 'label1'}},
			'xmrefnillable_value': {field: 'xmrefnillable_value', operator: 'EQUALS', value: null},
			'xstring': {field: 'xstring', operator: 'EQUALS', value: 'str0'},
			'xstringnillable': {operator: 'NESTED', nestedRules: [
				{field: 'xstringnillable', operator: 'EQUALS', value: 'str1'},
				{operator: 'OR'},
				{field: 'xstringnillable', operator: 'EQUALS', value: 'str2'},
				{operator: 'OR'},
				{field: 'xstringnillable', operator: 'EQUALS', value: null}]},
			'xtext': {field: 'xtext', operator: 'EQUALS', value: 'cool text'},
			'xtextnillable': {operator: 'NESTED', nestedRules: [
				{field: 'xtextnillable', operator: 'EQUALS', value: 'cool text'},
				{operator: 'OR'},
				{field: 'xtextnillable', operator: 'EQUALS', value: 'text'},
				{operator: 'OR'},
				{field: 'xtextnillable', operator: 'EQUALS', value: null}]},
			'xxref_value': {field: 'xxref_value', operator: 'EQUALS', value: {value: 'ref1', label: 'label1'}},
			'xxrefnillable_value': {field: 'xxrefnillable_value', operator: 'EQUALS', value: null}			
		};

		var $valueContainer = $('#values');
		for(var i = 0; i < keys.length; ++i) {
			if(!meta.attributes.hasOwnProperty(keys[i])) continue;

			var attr = meta.attributes[keys[i]];
			if(attr.name === 'id') continue;
			
		    var type = 'x';
		    $valueContainer.append(template({
		    	name: attr.name,
		    	type: type,
		    	prefix: '-value'
		    }));
			if(queries[attr.name] !== undefined) {
				molgenis.filters.create(attr, {
					'query': queries[attr.name],
					'onQueryChange' : function(q) {
						$('#query-result-header').html('Filter query: ');
						$('#query-result').html(JSON.stringify(q, null, '\t'));
					}
				}, $('#' + attr.name + type + '-value-filter-container'));
			}
		}

		molgenis.filters.createGroup(meta.attributes, {
				'onQueryChange' : function(q) {
					$('#query-result-header').html('Filter query: ');
					$('#query-result').html(JSON.stringify(q, null, '\t'));
				}
			}, $('#group-input-container'));
	});	
});
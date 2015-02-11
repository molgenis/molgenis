$(function() {
	var template = Handlebars.compile($("#attr-control-form-template").html());

	$.get('/api/v1/org_molgenis_test_TypeTest/meta?expand=attributes').done(function(meta) {
		var $container = $('#input-container');
		for(var key in meta.attributes) {
			var attr = meta.attributes[key];
			if(attr.name !== 'id') {

				if(attr.name === 'xcategorical_value') {
					
					function onValueChange(event) {
						console.log(event);
					}
					$container.append(template({name: attr.name}));
					var value = [{value: "ref2", label: "label2"}];
//					React.render(<molgenis.controls.CategoricalControl entity={attr.refEntity} multiple={true} onValueChange={onValueChange} value={value} />, $container[0]);
					React.render(molgenis.controls.CategoricalControl({entity: attr.refEntity, multiple: true, onValueChange: onValueChange, value: value}), $container[0]);

					//try {
// 						molgenis.controls.create(attr, {
// 							attr: attr,
// 							onValueChange : function(value) {
// 								console.log(value);
// 							}
// 						}, $('#' + attr.name + '-filter-container'));
					//} catch(err) {
						//console.log(err);
					//}
				}
				else if(attr.name === 'xenum') {
					var $container = $('#input-container-enum');
					var value = ["enum2"];
//					React.render(<molgenis.controls.EnumControl options={["enum1", "enum2", "enum3"]} multiple={true} onValueChange={onValueChange} value={value} />, $container[0]);
					React.render(molgenis.controls.EnumControl({options: ["enum1", "enum2", "enum3"], multiple: true, onValueChange: onValueChange, value: value}), $container[0]);
				}
			}
		}
	});
});
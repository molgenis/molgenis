$(function() {
	var steps = [
         {name: 'Step #0', content: React.DOM.h1({}, 'Content #0')},
         {name: 'Step #1', content: React.DOM.h1({}, 'Content #1')},
         {name: 'Step #2', content: React.DOM.h1({}, 'Content #2')},
         {name: 'Step #3', content: React.DOM.h1({}, 'Content #3')}
	];
	React.render(molgenis.control.Wizard({steps: steps}), $('#wizard-container')[0]);
});
require([ 'main' ], function() {
	console.log('TEST:', 'loaded main');
	require([ 'react', 'component/Button', 'jquery' ], function(React, Button, $) {
		console.log('TEST:', 'loaded test function');

		React.render(Button({
			style : 'button',
			size : 'large',
			text : 'EUREKA IT WORKS HAHAHAHAHA',
			disabled : false
		}), $('#test_div')[0]);

	});
});
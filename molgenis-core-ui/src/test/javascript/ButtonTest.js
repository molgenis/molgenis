// src/test/modules/react-components/ButtonTest.js
jest.dontMock('../../../main/javascript/modules/react-components/Button');

// import does not work
// require('react') does not work (cant find it)
var React = require('../../../../../molgenis-app/node_modules/react');
var ReactDOM = require('../../../../../molgenis-app/node_modules/react-dom');
var TestUtils = require('../../../../../molgenis-app/node_modules/react-addons-test-utils');

// import located in the Button module does not work
const Button = require('../../../main/javascript/modules/react-components/Button');

describe('Button', function(){
	it('Generates an event after a click', function() {

		console.log('RUNNING TEST');
		
		// Returns a freshly generated, unused mock function
		var callback = jest.genMockFunction();

		// Render a button that
		var Button = TestUtils.renderIntoDocument(
				Button({onClick: callback})
		    );
	    
	    // Simulate a click
	    TestUtils.Simulate.click(TestUtils.findRenderedDOMComponentWithTag(Button, 'button'));
	    
	    // Expect the click to generate a callback
	    expect(callback).toBeCalled();	  
	});
});

// TODO TEST ALL THESE OTHER PROPS
// id : React.PropTypes.string,
// type : React.PropTypes.oneOf([ 'button', 'submit', 'reset' ]),
// style : React.PropTypes.oneOf([ 'default', 'primary', 'success', 'info',
// 'warning', 'danger', 'link' ]),
// size : React.PropTypes.oneOf([ 'xsmall', 'small', 'medium', 'large' ]),
// text : React.PropTypes.string,
// icon : React.PropTypes.string,
// css : React.PropTypes.object,
// name : React.PropTypes.string,
// title : React.PropTypes.string,
// value : React.PropTypes.string,
// disabled : React.PropTypes.bool,

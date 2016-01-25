// __tests__/CheckboxWithLabel-test.js
jest.dontMock('../../../main/javascript/modules/react-components/CheckboxWithLabel');

import React from 'react/addons';
const ReactDOM = React;
const TestUtils = React.addons.TestUtils;
const CheckboxWithLabel = require('../../../main/javascript/modules/react-components/CheckboxWithLabel').default;

describe('CheckboxWithLabel', () => {

    it('changes the text after click', () => {

        // Render a checkbox with label in the document
        var checkbox = TestUtils.renderIntoDocument(
            <CheckboxWithLabel labelOn="On" labelOff="Off"/>
        );

        var checkboxNode = ReactDOM.findDOMNode(checkbox);

        // Verify that it's Off by default
        expect(checkboxNode.textContent).toEqual('Off');

        // Simulate a click and verify that it is now On
        TestUtils.Simulate.change(
            TestUtils.findRenderedDOMComponentWithTag(checkbox, 'input')
        );
        expect(checkboxNode.textContent).toEqual('On');
    });

});
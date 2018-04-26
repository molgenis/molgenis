import test from "tape";
import React from "react";
import sd from "skin-deep";
import Button from "react-components/Button";


test('Test if the Button onClick() gets mounted', assert => {
    let clicked = false;

    const tree = sd.shallowRender(Button({
        onClick: c => clicked = true
    }));
    const instance = tree.getMountedInstance();
    const vdom = tree.getRenderOutput();

    tree.subTree('button').props.onClick();
    assert.ok(clicked, "Button generates an event after a click");
    assert.end();
});

test('Test if the Button is rendered with icon and text', assert => {
    const tree = sd.shallowRender(Button({
        icon: "thumbs-up",
        text: "Okay"
    }));

    assert.equal(tree.toString(),
        '<button class="btn btn-default" type="button"><span><span class="glyphicon glyphicon-thumbs-up" aria-hidden="true"></span><span class="sr-only">thumbs-up</span></span> Okay</button>',
        "Button renders the icon as a glyphicon");

    assert.end();
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

import {groupBySelector, parser} from "rest-client/rsql";
import test from "tape";

test('Test that a single comparison gets mapped', assert => {
    const actual = groupBySelector(parser.parse("xbool==false"))
    const expected = {
        "xbool": {
            "selector": "xbool",
            "comparison": "==",
            "arguments": "false"
        }
    }
    assert.deepEqual(actual, expected);
    assert.end();
});

test('Test that multiple comparisons get mapped', assert => {
    const actual = groupBySelector(parser.parse("xbool==false;(xxref==ref1,xxref==ref2)"))
    const expected = {
        "xbool": {
            "selector": "xbool",
            "comparison": "==",
            "arguments": "false"
        },
        "xxref": {
            "operator": "OR",
            "operands": [{
                "selector": "xxref",
                "comparison": "==",
                "arguments": "ref1"
            }, {
                "selector": "xxref",
                "comparison": "==",
                "arguments": "ref2"
            }]
        }
    }

    assert.deepEqual(actual, expected);
    assert.end();
});


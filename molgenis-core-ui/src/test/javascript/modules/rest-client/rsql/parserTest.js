import { parser } from "rest-client/rsql";
import test from "tape";

test('Test if a simple equals comparison gets parsed', assert => {
    const actual = parser.parse("xbool==false")
    const expected = {
        "selector": "xbool",
        "comparison": "==",
        "arguments": "false"
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test('Test if a nested rsql string gets parsed', assert => {
    const actual = parser.parse("xbool==false;xdate=ge=2017-01-11;(count=ge=1;count=le=5);age==20;number==2;(str1=q=3,str1=q=5)")
    const expected = {
        "operator": "AND",
        "operands": [
            {
                "selector": "xbool",
                "comparison": "==",
                "arguments": "false"
            },
            {
                "selector": "xdate",
                "comparison": "=ge=",
                "arguments": "2017-01-11"
            },
            {
                "operator": "AND",
                "operands": [
                    {
                        "selector": "count",
                        "comparison": "=ge=",
                        "arguments": "1"
                    },
                    {
                        "selector": "count",
                        "comparison": "=le=",
                        "arguments": "5"
                    }
                ]
            },
            {
                "selector": "age",
                "comparison": "==",
                "arguments": "20"
            },
            {
                "selector": "number",
                "comparison": "==",
                "arguments": "2"
            },
            {
                "operator": "OR",
                "operands": [
                    {
                        "selector": "str1",
                        "comparison": "=q=",
                        "arguments": "3"
                    },
                    {
                        "selector": "str1",
                        "comparison": "=q=",
                        "arguments": "5"
                    }
                ]
            }
        ]
    }

    assert.deepEqual(actual,expected);
    assert.end();
})


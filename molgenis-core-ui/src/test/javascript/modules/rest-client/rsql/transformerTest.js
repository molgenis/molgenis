import {parser} from "rest-client/rsql";
import {
    toRange,
    toRangeLine,
    groupBySelector,
    toText,
    toSimpleRef,
    toComplexRef,
    toComplexLine,
    toBool
} from "rest-client/rsql/transformer";
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
})

test('Test that single OR comparison gets mapped', assert => {
    const actual = groupBySelector(parser.parse("(xxref==ref1,xxref==ref2)"))
    const expected = {
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
})

test('Test selector in xref', assert => {
    const actual = groupBySelector(parser.parse("xxref.label==ref2"))
    const expected = {
        "xxref.label": {
            "selector": "xxref.label",
            "comparison": "==",
            "arguments": "ref2"
        }
    }

    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toRangeLine for int values both specified", assert => {
    const actual = toRangeLine(parser.parse("(count=ge=1;count=le=5)"))
    const expected = {from: "1", to: "5"}
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toRangeLine for int values one specified", assert => {
    const actual = toRangeLine(parser.parse("(count=ge=1)"))
    const expected = {from: "1"}
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toRange two int ranges", assert => {
    const actual = toRange(parser.parse("(count=ge=1;count=le=5),(count=ge=8;count=le=10)"))
    const expected = {
        type: "RANGE",
        lines: [{from: "1", to: "5"}, {from: "8", to: "10"}]
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toRange one int range", assert => {
    const actual = toRange(parser.parse("(count=ge=1;count=le=5)"))
    const expected = {
        type: "RANGE",
        lines: [{from: "1", to: "5"}]
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toBool", assert => {
    const actual = toBool(parser.parse("xbool==false"))
    const expected = {
        'type': 'BOOL',
        'value': 'false'
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toText two string values", assert => {
    const actual = toText(parser.parse("(xstring=q=str1,xstring=q=str2)"))
    const expected = {
        'type': 'TEXT',
        'lines': ['str1', 'str2']
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toSimpleRef one value", assert => {
    const actual = toSimpleRef({"ref1": "label1"}, parser.parse("xxref==ref1"))
    const expected = {
        'type': 'SIMPLE_REF',
        'values': [{'label': 'label1', 'value': 'ref1'}]
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toSimpleRef two values", assert => {
    const actual = toSimpleRef({"ref1": "label1", "ref2": "label2"},
        parser.parse("(xxref==ref1,xxref==ref2)"))
    const expected = {
        'type': 'SIMPLE_REF',
        'values': [{'label': 'label1', 'value': 'ref1'}, {'label': 'label2', 'value': 'ref2'}]
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toComplexLine one value selected", assert => {
    const actual = toComplexLine({"ref1": "label1"}, parser.parse("xmref==ref1"))
    const expected = {'operator': undefined, 'values': [{'label': 'label1', 'value': 'ref1'}]}
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toComplexRef five lines with random ANDs and ORs", assert => {
    const actual = toComplexRef({
            "ref1": "label1",
            "ref2": "label2",
            "ref3": "label3",
            "ref4": "label4",
            "ref5": "label5"
        },
        parser.parse("(((xmref==ref1;xmref==ref2);(xmref==ref3,xmref==ref4)),(xmref==ref5,xmref==ref1),((xmref==ref2;xmref==ref3);(xmref==ref4,xmref==ref5)))"))
    const expected = {
        'type': 'COMPLEX_REF',
        'lines': [
            {
                operator: 'AND',
                values: [{label: 'label1', value: 'ref1'}, {label: 'label2', value: 'ref2'}]
            },
            'AND',
            {
                operator: 'OR',
                values: [{label: 'label3', value: 'ref3'}, {label: 'label4', value: 'ref4'}]
            },
            'OR',
            {
                operator: "OR",
                values: [{label: 'label5', value: 'ref5'}, {label: 'label1', value: 'ref1'}]
            },
            'OR',
            {
                operator: 'AND',
                values: [{label: 'label2', value: 'ref2'}, {label: 'label3', value: 'ref3'}]
            },
            'AND',
            {
                operator: 'OR',
                values: [{label: 'label4', value: 'ref4'}, {label: 'label5', value: 'ref5'}]
            }]
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toComplexRef one value selected", assert => {
    const actual = toComplexRef({"ref1": "label1"}, parser.parse("xmref==ref1"))
    const expected = {
        'type': 'COMPLEX_REF',
        'lines': [{'operator': undefined, 'values': [{'label': 'label1', 'value': 'ref1'}]}]
    }
    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test toModelPart")

import {containsRsqlReservedCharacter, rsqlEscape, createRsqlQuery, encodeRsqlValue} from "rest-client/rsql";
import { toRsqlValue } from 'rest-client/rsql/createRsqlQuery'
import test from "tape";

test('Test if containsRsqlReservedCharacter checks for reserved characters', assert => {
    assert.ok(containsRsqlReservedCharacter("abc\"def"), "Checks for \"")
    assert.ok(containsRsqlReservedCharacter("abc'def"), "Checks for '")
    assert.ok(containsRsqlReservedCharacter("abc(def"), "Checks for (")
    assert.ok(containsRsqlReservedCharacter("abc)def"), "Checks for )")
    assert.ok(containsRsqlReservedCharacter("abc;def"), "Checks for ;")
    assert.ok(containsRsqlReservedCharacter("abc,def"), "Checks for ,")
    assert.ok(containsRsqlReservedCharacter("abc=def"), "Checks for =")
    assert.ok(containsRsqlReservedCharacter("abc!def"), "Checks for !")
    assert.ok(containsRsqlReservedCharacter("abc~def"), "Checks for ~")
    assert.ok(containsRsqlReservedCharacter("abc<def"), "Checks for <")
    assert.ok(containsRsqlReservedCharacter("abc>def"), "Checks for >")
    assert.ok(containsRsqlReservedCharacter("abc def"), "Checks for space")
    assert.ok(!containsRsqlReservedCharacter("@#&"), "Doesn't check for other characters")
    assert.end()
})

test('rsqlEscape escapes rsql strings properly', assert => {
    assert.equals(rsqlEscape("abc\"def"), "'abc\"def'", "Picks ' as delimiter to escape \"")
    assert.equals(rsqlEscape("abc'def"), "\"abc'def\"", "Picks \" as delimiter to escape '")
    assert.equals(rsqlEscape("abc=def"), "'abc=def'", "Prefers ' as delimiter")
    assert.equals(rsqlEscape("abc\"'def"), "'abc\"\\'def'", "Escapes delimiter if it occurs inside the string")
    assert.equals(rsqlEscape("abc def"), "'abc def'", "Escapes entire value if space is in string")
    assert.end()
})

test('createRsqlQuery', assert => {
    assert.equals(createRsqlQuery([]), "", "Empty query")
    assert.equals(createRsqlQuery([{operator: 'EQUALS', field: 'field', value: 5}]), "field==5", "field==5")
    assert.equals(createRsqlQuery([
        {operator: 'EQUALS', field: 'field1', value: 5},
        {operator: 'AND'},
        {operator: 'IN', field: 'field2', value: [6, 7]}]), "field1==5;field2=in=(6,7)", "field1==5;field2=in=(6,7)")
    assert.equals(createRsqlQuery([
        {operator: 'EQUALS', field: 'fiel=d', value: 5},
        {operator: 'AND'},
        {operator: 'IN', field: 'field2', value: ["va(*&lue", "value\""]}
    ]), "'fiel=d'==5;field2=in=('va(*&lue','value\"')", "Escapes field names and values")
    assert.equals(createRsqlQuery([
        {
            operator: 'NESTED',
            nestedRules: [{operator: 'EQUALS', field: 'field', value: 5}]
        }]), "field==5", "Simplifies single nested rule")
    assert.equals(createRsqlQuery([
        {
            operator: 'NESTED',
            nestedRules: [{operator: 'EQUALS', field: 'field', value: 5},
                {operator: 'OR'},
                {operator: 'EQUALS', field: 'field', value: 6}]
        },
        {operator: 'AND'},
        {
            operator: 'EQUALS',
            field: 'field',
            value: 7
        }]), "(field==5,field==6);field==7", "(field==5,field==6);field==7")
    assert.end()
})

test('encodeRsqlValue', assert => {
    assert.equals(encodeRsqlValue('=:;,-()"\'~<>'), '=:;,-()"\'~<>', 'Leaves rsql syntax =:;,-()"\'~<> readable')
    assert.equals(encodeRsqlValue('a==b&'), 'a==b%26', 'Encodes &')
    assert.equals(encodeRsqlValue('a==b#'), 'a==b%23', 'Encodes #')
    assert.equals(encodeRsqlValue('a==b+'), 'a==b%2B', 'Encodes +')
    assert.equals(encodeRsqlValue('a=="b lah"'), 'a=="b%20lah"', 'Encodes space')
    assert.end()
})

test('toRsqlValue', assert => {
    assert.equals(toRsqlValue("abc OR def"), "'abc OR def'")
    assert.end()
})
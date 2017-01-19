import {getHumanReadable} from "rest-client/rsql";
import {parser} from "rest-client/rsql";
import {groupBySelector} from "rest-client/rsql/transformer";
import test from "tape";

test("Test translating a simple RSQL string into a human readable string", assert => {
    const actual = getHumanReadable("(name=q=Graz,name=q=Berlin)")
    const expected = "name is equal to Graz or name is equal to Berlin"

    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test translating a complex RSQL string into a human readable string", assert => {
    const actual = getHumanReadable("access_fee==false;((country==Netherlands,country==Belgium);country==France);sampleSize=ge=5")
    const expected = "access_fee is equal to false and country is equal to Netherlands or country is equal to Belgium and country is equal to France and sampleSize is greater or equal then 5"

    assert.deepEqual(actual, expected);
    assert.end();
})

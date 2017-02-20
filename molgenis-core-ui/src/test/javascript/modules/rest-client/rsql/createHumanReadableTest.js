import {getHumanReadable, parser} from "rest-client/rsql";
import {groupBySelector} from "rest-client/rsql/transformer";
import test from "tape";

test("Test translating a simple RSQL string into a human readable string", assert => {
    const actual = getHumanReadable("(name=q=Graz,name=q=Berlin)")
    const expected = "name contains Graz or name contains Berlin"

    assert.deepEqual(actual, expected);
    assert.end();
})

test("Test translating a complex RSQL string into a human readable string", assert => {
    const actual = getHumanReadable("access_fee==false;((country==Netherlands,country==Belgium);country==France);sampleSize=ge=5")
    const expected = "access_fee equals false\ncountry equals Netherlands or country equals Belgium and country equals France\nsampleSize is greater than or equal to 5"

    assert.deepEqual(actual, expected);
    assert.end();
})
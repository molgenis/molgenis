import {fetchAttribute, fetchLabels} from "rest-client/rsql/filtermetafetch";
import test from "blue-tape";

const metas = {
    '/api/v1/entityName/meta/xref': {
        fieldType: "XREF",
        refEntity: {
            href: '/api/v1/refEntityName/meta'
        }
    },
    '/api/v1/refEntityName/meta/label': {
        fieldType: "STRING"
    }
}

const restApi = {
    getAsync: (href) => Promise.resolve(metas[href])
}

const responsesV2 = {
    '/api/v2/refEntityName': {meta: {idAttribute: "idAttr", labelAttribute: "labelAttr"}},
    '/api/v2/refEntityName?attrs=idAttr,labelAttr&q=idAttr=in=("id1","id2")': {
        items: [{idAttr: "id1", labelAttr: "label1"}, {idAttr: "id2", labelAttr: "label2"}]
    }
}

const restApiV2 = {
    get: (href) => Promise.resolve(responsesV2[href])
}

test('Test fetchAttribute refEntity', assert => {
    return fetchAttribute(restApi, "entityName", "xref.label").then((actual) =>
        assert.equals(actual, metas['/api/v1/refEntityName/meta/label'])
    )
})

test("Test fetchLabels", assert => {
    return fetchLabels(restApiV2, "refEntityName", ["id1", "id2"])
        .then(data => assert.deepEquals(data, {"id1": "label1", "id2": "label2"}))
})
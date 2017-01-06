import parser from "./parser";
import transformer, {getArguments} from "./transformer";
import {htmlEscape} from "../../utils/HtmlUtils";

/**
 * Fetches attributes and labels.
 * @param rsql the RSQL string to use to fetch
 * @param entityName
 * @param restApi to fetch the attribute
 * @param restApiV2 to fetch the label values
 * @returns Promise for an object with structure {
 *  [selector] : {
 *      attribute,
 *      filterModel
 *  }
 * }
 */
function fetch(rsql, entityName, restApi, restApiV2) {
    const tree = parser.parse(rsql)
    const constraintsBySelector = transformer.groupBySelector(tree)
    const selectors = Object.keys(constraintsBySelector)
    const attributePromises = selectors.map(selector => fetchAttribute(restApi, entityName, selector))
    return Promise
        .all(attributePromises)
        .then(attributes => fetchLabelsAndCombine(selectors, attributes, constraintsBySelector, restApiV2))
}

/**
 * Fetches the labels for all refEntities.
 * Does two queries per refEntity, one to fetch the metadata and one to fetch the labels.
 * @param selectors array containing the the selectors that are the keys of the map
 * @param attributes array containing the retrieved attributes for the selectors
 * @param constraintsBySelector object mapping selector to the constraint for that selector
 * @param restApiV2 api to retrieve the labels with
 * @returns Promise for the result.
 */
function fetchLabelsAndCombine(selectors, attributes, constraintsBySelector, restApiV2) {
    const idsPerRefEntity = groupIdsPerRefEntity(selectors, attributes, constraintsBySelector)
    const refEntities = Object.keys(idsPerRefEntity)
    const labelPromises = refEntities.map(refEntity => fetchLabels(restApiV2, refEntity, idsPerRefEntity[refEntity]))
    return Promise
        .all(labelPromises)
        .then(labelMaps => {
            combineResults(selectors, attributes, constraintsBySelector, labelMaps);
        })
}

/**
 * Combines the retrieved results into a single result object.
 * @param selectors array containing the the selectors that are the keys of the map
 * @param attributes array containing the retrieved attributes for the selectors
 * @param constraintsBySelector object mapping selector to the constraint for that selector
 * @param labelsPerEntity object mapping refEntityName to an object mapping id values to label values
 * @returns object mapping selector to its attribute and filter model
 */
export function combineResults(selectors, attributes, constraintsBySelector, labelsPerEntity) {
    return Object.assign(...selectors.map((selector, i) => {
        const attribute = attributes[i]
        const constraint = constraintsBySelector[selector]
        let labels = undefined
        if (attribute.refEntity) {
            const refEntityName = attribute.refEntity.hrefCollection.subString('/api/v1/'.length)
            labels = labelsPerEntity[refEntityName]
        }
        const modelPart = transformer.transformModelPart(attribute.fieldType, labels, constraint);
        return {[selector]: {attribute, modelPart}}
    }))
}

/**
 * Groups all IDs mentioned in constraints per refEntity so that they can be retrieved together.
 * @param selectors array with selectors
 * @param attributes array with attributes
 * @param constraintsBySelector map mapping selector to constraint, used to find id values in the constraint
 */
function groupIdsPerRefEntity(selectors, attributes, constraintsBySelector) {
    const indicesWithRefentity = [...attributes.keys()].filter(i => attributes[i].refEntity)
    return indicesWithRefentity.reduce((acc, index) => {
        const selector = selectors[index]
        const attribute = attributes[index]
        const refEntityName = attribute.refEntity.hrefCollection.subString('/api/v1/'.length)
        const ids = new Set([
            ...(acc[refEntityName] || new Set()),
            ...getArguments(constraintsBySelector[selector])
        ])
        return {...acc, [refEntityName]: ids}
    });
}

/**
 * Fetches the label values for an entity given the ids using the RestApiV2
 * @param restApiV2 rest api to use to fetch the metadata (to figure out what is the idAttribute and the labelAttribute)
 * and to fetch the label values
 * @param entityName escaped entity name
 * @param ids ids to fetch
 */
export function fetchLabels(restApiV2, entityName, ids) {
    return restApiV2.get(`/api/v2/${entityName}`).then(
        (result) => {
            const {labelAttribute, idAttribute} = result.meta
            const uri = `/api/v2/${entityName}?attrs=${idAttribute},${labelAttribute}&q=${idAttribute}=in=("${ids.join('","')}")`
            return restApiV2.get(uri).then(
                (data) => Object.assign(...data.items.map(item => ({[item[idAttribute]]: item[labelAttribute]})))
            )
        }
    )
}

/**
 * Fetches a single Attribute through the V1 rest api.
 * @param restApi the restApi to use
 * @param entityName the fully qualified name of the entity to fetch
 * @param selector the selector for the attribute
 */
export function fetchAttribute(restApi, entityName, selector) {
    const initialValue = Promise.resolve({
        href: '/api/v1/' + htmlEscape(entityName) + '/meta',
        attribute: undefined,
    })
    const hop = (promise, attributeName) => promise.then((state) =>
        restApi.getAsync(state.href + "/" + attributeName).then(
            (attribute) => {
                return {
                    href: attribute.refEntity && attribute.refEntity.href,
                    attribute
                }
            }
        )
    )
    return selector.split('.').reduce(hop, initialValue).then(acc => acc.attribute)
}

export default fetch
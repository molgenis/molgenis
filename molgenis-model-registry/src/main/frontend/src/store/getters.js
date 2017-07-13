import type { State } from 'utils/flow.types'

// xref 1 - 1
// mref 1 - N
// categorical 1 - 1
// categorical_mref 1 - N
// onetomany 1 - N
const types = {
  'xref': {nullable: '0...1', notnullable: '1...1'},
  'mref': {nullable: '0...N', notnullable: '1...N'},
  'categorical': {nullable: '0...1', notnullable: '1...1'},
  'categorical_mref': {nullable: '0...N', notnullable: '1...N'},
  'onetomany': {nullable: '0...N', notnullable: '1...N'}
}

let nodeData = []
let linkData = []

const mapAttributeToNode = attribute => {
  let figure = 'Cubel'
  let color = 'Blue'
  let isKey = false
  if (isRef(attribute)) {
    figure = 'Decision'
    color = 'red'
    isKey = true
  }
  return {
    name: attribute.label || attribute.name, iskey: isKey, figure: figure, color: color
  }
}

const mapNodeData = (entityTypes) => entityTypes.map(entityType => {
  let nodeKey = (entityType.isAbstract) ? '<abstract>\n' + entityType.id : entityType.id
  if (entityType.extends && entityType.extends.id) {
    nodeKey = '<extends ' + (entityType.extends.name || entityType.extends.label) + '>\n' + nodeKey
    const abstractEntityType = entityType.extends.id
    entityType = entityTypes.find(entityType => entityType.id === abstractEntityType)
    linkData.push({from: abstractEntityType, to: entityType.id, text: '', toText: ''})
  }
  return {
    key: nodeKey,
    items: entityType.attributes.map(mapAttributeToNode)
  }
})

const isRef = (attribute) => types[attribute.type]

const determineRefEntityType = (entityTypes, refEntityType) => entityTypes.find(entityType => entityType.id === refEntityType)
const determineRefAttribute = (entityTypes, refEntityType) => {
  const referenceEntityType = determineRefEntityType(entityTypes, refEntityType)
  if (referenceEntityType && referenceEntityType.attributes) {
    return referenceEntityType.attributes.find(attribute => attribute.isIdAttribute === true)
  }
}

const mapLinkData = (entityTypes) => {
  entityTypes.forEach(entityType => {
    entityType.attributes.forEach(attribute => {
      if (isRef(attribute)) {
        const refAttribute = determineRefAttribute(entityTypes, attribute.refEntityType.id)
        const attributeDesc = attribute.label || attribute.name
        let refAttributeType = types[attribute.type].notnullable
        if (attribute.isNullable) refAttributeType = types[attribute.type].nullable
        let refAttributeDesc = refAttributeType
        if (refAttribute) refAttributeDesc = (refAttribute.label || refAttribute.name) + ' | ' + refAttributeType
        // mapEnvironmentEntityTypes(attribute.refEntityType.id)
        linkData.push({from: entityType.id, to: attribute.refEntityType.id, text: attributeDesc, toText: refAttributeDesc})
      }
    })
  })
}

// const mapEnvironmentEntityTypes = (refEntityTypeId) => {
//   console.log(nodeData)
//   const isPresent = nodeData.some(node => {
//     node.key === refEntityTypeId
//   })
//   console.log(isPresent)
//   if (!isPresent) {
//     console.log(refEntityTypeId)
//     nodeData.push({key: refEntityTypeId, items: {}})
//   }
// }

export default {

  umlData: (state: State) => {
    if (state.umlData.entityTypes) {
      nodeData = mapNodeData(state.umlData.entityTypes)
      linkData = mapLinkData(state.umlData.entityTypes)
      return {
        nodeData: nodeData,
        linkData: linkData
      }
    }
  }

}

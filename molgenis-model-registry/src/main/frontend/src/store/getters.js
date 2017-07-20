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
  }
  if (attribute.isIdAttribute) {
    isKey = true
    color = 'yellow'
  }
  let attributeTypeRef = ''
  let attributeType = types[attribute.type]
  if (attributeType) {
    attributeTypeRef = ' [ ' + types[attribute.type].notnullable + ' ]'
    if (attribute.isNullable) attributeTypeRef = '[ ' + types[attribute.type].nullable + ' ]'
  }
  return {
    name: (attribute.label || attribute.name) + ': ' + capitalize(attribute.type) + attributeTypeRef,
    iskey: isKey,
    figure: figure,
    color: color
  }
}

const capitalize = (string) => (string[0].toUpperCase() + string.slice(1))

const mapNodeData = (entityTypes) => entityTypes.filter(entityType => (!entityType.extends)).map(entityType => {
  return {
    key: entityType.id,
    items: entityType.attributes.map(mapAttributeToNode)
  }
})

const mapExtendedNodeData = (entityTypes) => entityTypes.filter(entityType => (entityType.extends && !entityType.isAbstract)).map(entityType => {
  const abstractEntityType = entityTypes.filter(aEntityType => (aEntityType.id === entityType.extends.id))[0]
  linkData.push({from: abstractEntityType.id, to: entityType.id, text: 'isAbstract', toText: '<extends ' + abstractEntityType.id + '>'})
  nodeData.push({key: entityType.id, items: entityType.attributes.map(mapAttributeToNode)})
})

const isRef = (attribute) => types[attribute.type]
//
const mapLinkData = (entityTypes) => {
  entityTypes.forEach(entityType => {
    entityType.attributes.forEach(attribute => {
      if (isRef(attribute)) {
        const attributeName = attribute.label || attribute.name
        let refAttributeType = types[attribute.type].notnullable
        if (attribute.isNullable) refAttributeType = types[attribute.type].nullable
        // mapEnvironmentEntityTypes(attribute.refEntityType.id)
        linkData.push({
          from: entityType.id,
          to: attribute.refEntityType.id,
          text: attributeName,
          toText: refAttributeType
        })
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
      mapExtendedNodeData(state.umlData.entityTypes)
      mapLinkData(state.umlData.entityTypes)
      return {
        nodeData: nodeData,
        linkData: linkData
      }
    }
  }

}

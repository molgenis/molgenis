import type { State } from 'utils/flow.types'

// xref 1 - 1
// mref 1 - N
// categorical 1 - 1
// categorical_mref 1 - N
// onetomany 1 - N
const types = {
  'xref': '1...1',
  'mref': '1...N',
  'categorical': '1...1',
  'categorical_mref': '1...N',
  'onetomany': '1...N'
}

let nodeData = {}

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

const mapNodeData = (entityTypes) => entityTypes.map(entityType => ({
  key: entityType.id,
  items: entityType.attributes.map(mapAttributeToNode)
}))

const isRef = (attribute) => types[attribute.type]

const determineRefEntityType = (entityTypes, refEntityType) => entityTypes.find(entityType => entityType.id === refEntityType)
const determineRefAttribute = (entityTypes, refEntityType) => {
  const referenceEntityType = determineRefEntityType(entityTypes, refEntityType)
  if (referenceEntityType && referenceEntityType.attributes) {
    return referenceEntityType.attributes.find(attribute => attribute.isIdAttribute === true)
  }
}

const mapLinkData = (entityTypes) => {
  const links = []
  entityTypes.forEach(entityType => {
    entityType.attributes.forEach(attribute => {
      if (isRef(attribute)) {
        const refAttribute = determineRefAttribute(entityTypes, attribute.refEntityType.id)
        const attributeDesc = attribute.label || attribute.name
        let refAttributeDesc = types[attribute.type]
        if (refAttribute) {
          refAttributeDesc = (refAttribute.label || refAttribute.name) + ' | ' + types[attribute.type]
        }
        mapEnvironmentEntityTypes(attribute.refEntityType.id)
        links.push({from: entityType.id, to: attribute.refEntityType.id, text: attributeDesc, toText: refAttributeDesc})
      }
    })
  })
  return links
}

const mapEnvironmentEntityTypes = (refEntityTypeId) => {
  console.log(nodeData)
  const isPresent = nodeData.some(node => {
    node.key === refEntityTypeId
  })
  console.log(isPresent)
  if (!isPresent) {
    console.log(refEntityTypeId)
    nodeData.push({key: refEntityTypeId, items: {}})
  }
}

export default {

  umlData: (state: State) => {
    if (state.umlData.entityTypes) {
      nodeData = mapNodeData(state.umlData.entityTypes)
      return {
        nodeData: nodeData,
        linkData: mapLinkData(state.umlData.entityTypes)
      }
    }
  }

}

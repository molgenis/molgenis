import type { State } from 'utils/flow.types'

// xref 1 - 1
// mref 1 - N
// categorical 1 - 1
// categorical_mref 1 - N
// onetomany 1 - N
const types = {
  'xref': {
    src: '1',
    ref: '1'
  },
  'mref': {
    src: '1',
    ref: 'N'
  },
  'categorical': {
    src: '1',
    ref: '1'
  },
  'categorical_mref': {
    src: '1',
    ref: 'N'
  },
  'onetomany': {
    src: '1',
    ref: 'N'
  }
}

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
    name: attribute.name, iskey: isKey, figure: figure, color: color
  }
}

const mapNodeData = (entityTypes) => entityTypes.map(entityType => ({
  key: entityType.id,
  items: entityType.attributes.map(mapAttributeToNode)
}))

const isRef = (attribute) => types[attribute.type]

const determineRefEntityType = (entityTypes, refEntityType) => entityTypes.find(entityType => entityType.id === refEntityType) !== null
const determineRefAttribute = (entityTypes, refEntityType) => {
  const referenceEntityType = determineRefEntityType(entityTypes, refEntityType)
  if (referenceEntityType && referenceEntityType.attributes) {
    console.log(JSON.stringify(referenceEntityType.attributes))
    return referenceEntityType.attributes.find(attribute => {
      if (attribute) return attribute.name === 'id'
    })
  }
}

const mapLinkData = (entityTypes) => {
  const links = []
  entityTypes.forEach(entityType => {
    entityType.attributes.forEach(attribute => {
      if (isRef(attribute)) {
        const refAttribute = determineRefAttribute(entityTypes, attribute.refEntityType.id)
        const attributeDesc = types[attribute.type].src + ' | ' + attribute.name
        let refAttributeDesc = ''
        if (refAttribute) {
          refAttributeDesc = refAttribute.name + ' | ' + types[attribute.type].ref
        }
        links.push({from: entityType.id, to: attribute.refEntityType.id, text: attributeDesc, toText: refAttributeDesc})
      }
    })
  })
  return links
}

export default {

  umlData: (state: State) => {
    if (state.umlData.entityTypes) {
      return {
        nodeData: mapNodeData(state.umlData.entityTypes),
        linkData: mapLinkData(state.umlData.entityTypes)
      }
    }
  }

}

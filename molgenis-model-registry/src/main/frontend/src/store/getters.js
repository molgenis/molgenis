import type { State } from 'utils/flow.types'

// FigureTypes: Decision, Cubel, MagneticData, TriangleUp, TriangleDown
// Colors: yellow, green, blue
const mapAttributeToNode = attribute => {
  let figure = 'Cubel'
  let color = 'Blue'
  let isKey = false
  if (attribute.type === 'xref') {
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

const mapLinkData = (entityTypes) => {
  const links = []
  entityTypes.forEach(entityType => {
    entityType.attributes.forEach(attribute => {
      if (attribute.type === 'xref') {
        console.log(attribute.name)
        const attributeDesc = attribute.name + ' | 0..N'
        const refAttributeDesc = attribute.refEntityType.id + ' | 1'
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

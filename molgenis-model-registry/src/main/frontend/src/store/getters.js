import type { State } from 'utils/flow.types'
// import { _ } from 'lodash'

const mapNodeData = (entityTypes) => {
  const nodeDataArray = []
  console.log(JSON.stringify(entityTypes, null, 2))
  entityTypes.forEach(function (entityType) {
    // FigureTypes: Decision, Cubel, MagneticData, TriangleUp, TriangleDown
    // Colors: yellow, green, blue

    const items = []

    for (const val of Object.values(entityType)) {
      console.log(JSON.stringify(val, null, 2))
    }

    for (const attributeKey in entityType.attributes) {
      console.log(JSON.stringify(attributeKey, null, 2))
      const attribute = entityType.attributes[attributeKey]
      const nodeData = {
        name: attribute.getName,
        iskey: false,
        figure: 'Decision',
        color: 'blue'
      }
      items.push(nodeData)
    }
    const nodeData = {
      key: entityType.__labelValue,
      items: items
    }
    nodeDataArray.push(nodeData)
  })
  return nodeDataArray
}

export default {

  graph: (state: State) => {
    if (state.rawData.entityTypes) {
      return {
        nodeData: mapNodeData(state.rawData.entityTypes),
        linkData: []
      }
    }
  }

}

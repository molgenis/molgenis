import type { State } from 'utils/flow.types'
// import { _ } from 'lodash'

function mapNodeData (entityTypes) {
  let entityTypeKey = ''

  const items = []
  entityTypes.forEach(function (entityType) {
    // FigureTypes: Decision, Cubel, MagneticData, TriangleUp, TriangleDown
    // Colors: yellow, green, blue

    if (entityTypeKey !== entityType.__entityTypeId) {
      entityTypeKey = entityType.__entityTypeId
    }

    const nodeData = {
      name: entityType.__labelValue,
      iskey: false,
      figure: 'Decision',
      color: 'blue'
    }

    items.push(nodeData)
  })
  return [
    {
      key: entityTypeKey,
      items: items
    }
  ]
}

export default {
  graph: (state: State) => ({
    nodeData: mapNodeData(state.rawData.entityTypes),
    linkData: []
  })
}

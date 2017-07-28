import type { Link, Node, State } from 'utils/flow.types'

const types = {
  'xref': {nullable: '0...1', notnullable: '1...1'},
  'mref': {nullable: '0...N', notnullable: '1...N'},
  'categorical': {nullable: '0...1', notnullable: '1...1'},
  'categorical_mref': {nullable: '0...N', notnullable: '1...N'},
  'onetomany': {nullable: '0...N', notnullable: '1...N'}
}

let nodeData: Array<Node> = []
let linkData: Array<Link> = []

const mapAttributeToNode = attribute => {
  let isKey = false
  if (attribute.isIdAttribute) {
    isKey = true
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
    figure: attributeFigure(attribute),
    color: attributeColor(attribute)
  }
}

const attributeFigure = (attribute) => {
  const attributeFigure = 'Cubel'
  const keyAttributeFigure = 'Decision'
  const idAttributeFigure = 'Triangle'
  let figure = attributeFigure
  if (isRef(attribute)) {
    figure = keyAttributeFigure
  }
  if (attribute.isIdAttribute) {
    figure = idAttributeFigure
  }
  return figure
}

const attributeColor = (attribute) => {
  const attributeColor = '#99B0F0'
  const keyAttributeColor = '#F099B0'
  const idAttributeColor = '#F0EF9A'
  let color = attributeColor
  if (isRef(attribute)) {
    color = keyAttributeColor
  }
  if (attribute.isIdAttribute) {
    color = idAttributeColor
  }
  return color
}

const capitalize = (string) => (string[0].toUpperCase() + string.slice(1))

const mapNodeData = (entityTypes) => entityTypes.map(entityType => {
  nodeData.push({
    key: entityType.id,
    group: entityType.package.id,
    extends: isExtendedFrom(entityType),
    abstract: isAbstract(entityType),
    color: entityTypeColor(entityType),
    items: entityType.attributes.map(mapAttributeToNode)
  })
})

const isAbstract = (entityType) => {
  let abstract = ''
  if (entityType.isAbstract) abstract = 'isAbstract'
  return abstract
}

const isExtendedFrom = (entityType) => {
  let extendedClass = ''
  if (entityType.extends && entityType.extends.id !== entityType.id) extendedClass = 'extends [ ' + entityType.extends.id + ' ]'
  return extendedClass
}

const mapPackageNodeData = (entityTypes) => entityTypes.map(entityType => {
  if (!nodeData.find(node => node.key === entityType.package.id)) {
    nodeData.push({
      key: entityType.package.id,
      color: '#F0F0E9',
      isGroup: true
    })
  }
})

const mapExtendedNodeData = (entityTypes) => entityTypes.filter(entityType => (entityType.extends)).map(entityType => {
  if (!nodeData.find(node => node.key === entityType.extends.id)) {
    nodeData.push({
      key: entityType.extends.id,
      color: entityTypeColor(entityType.extends),
      group: entityType.extends.package.id,
      items: []
    })
    mapPackageNodeData([entityType.extends])
  }
  if (!linkData.find(link => (link.from === entityType.extends.id && link.to === entityType.id))) {
    linkData.push({
      from: entityType.extends.id,
      to: entityType.id,
      text: '',
      toText: ''
    })
  }
  if (!nodeData.find(node => node.key === entityType.id)) {
    nodeData.push({
      key: entityType.id,
      color: entityTypeColor(entityType),
      group: entityType.package.id,
      items: entityType.attributes.map(mapAttributeToNode)
    })
  }
})

const entityTypeColor = (entityType) => {
  const entityTypeColor = '#FFFFFF'
  const extendEntityTypeColor = '#BDC4DA'
  const abstractEntityTypeColor = '#8A9FD8'
  let color = entityTypeColor
  if (entityType.isAbstract) color = abstractEntityTypeColor
  if (entityType.extends) color = extendEntityTypeColor
  return color
}

const isRef = (attribute) => types[attribute.type]
//
const mapLinkData = (entityTypes) => {
  entityTypes.forEach(entityType => {
    entityType.attributes.forEach(attribute => {
      if (isRef(attribute)) {
        const attributeName = attribute.label || attribute.name
        let refAttributeType = types[attribute.type].notnullable
        if (attribute.isNullable) refAttributeType = types[attribute.type].nullable
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

export default {

  umlData: (state: State) => {
    if (state.umlData.entityTypes) {
      console.log(state.umlData.entityTypes)
      mapNodeData(state.umlData.entityTypes)
      mapExtendedNodeData(state.umlData.entityTypes)
      mapPackageNodeData(state.umlData.entityTypes)
      console.log(nodeData)
      mapLinkData(state.umlData.entityTypes)
      return {
        nodeData: nodeData,
        linkData: linkData
      }
    }
  }

}

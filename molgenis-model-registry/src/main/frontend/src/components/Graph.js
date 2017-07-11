import go from 'gojs'

export function newGraph (targetDiv, graphData) {
  // if (window.goSamples) goSamples()  // init for these samples -- you don't need to call this
  const graphObject = go.GraphObject.make  // for conciseness in defining templates
  const diagram =
    graphObject(go.Diagram, targetDiv,
      {
        initialContentAlignment: go.Spot.Center,
        allowDelete: false,
        allowCopy: false,
        layout: graphObject(go.ForceDirectedLayout),
        'undoManager.isEnabled': true
      })
  // define several shared Brushes

  const lightgrad = graphObject(go.Brush, 'Linear', {1: '#E6E6FA', 0: '#FFFAF0'})

  // the template for each attribute in a node's array of item data
  const itemTempl =
    graphObject(go.Panel, 'Horizontal',
      graphObject(go.Shape,
        {desiredSize: new go.Size(10, 10)},
        new go.Binding('figure', 'figure'),
        new go.Binding('fill', 'color')),
      graphObject(go.TextBlock,
        {
          stroke: '#333333',
          font: 'bold 14px sans-serif'
        },
        new go.Binding('text', 'name'))
    )
  // define the Node template, representing an entity
  diagram.nodeTemplate =
    graphObject(go.Node, 'Auto',  // the whole node panel
      {
        selectionAdorned: true,
        resizable: true,
        layoutConditions: go.Part.LayoutStandard & ~go.Part.LayoutNodeSized,
        fromSpot: go.Spot.AllSides,
        toSpot: go.Spot.AllSides,
        isShadowed: true,
        shadowColor: '#C5C1AA'
      },
      new go.Binding('location', 'location').makeTwoWay(),
      // whenever the PanelExpanderButton changes the visible property of the "LIST" panel,
      // clear out any desiredSize set by the ResizingTool.
      new go.Binding('desiredSize', 'visible', function (v) { return new go.Size(NaN, NaN) }).ofObject('LIST'),
      // define the node's outer shape, which will surround the Table
      graphObject(go.Shape, 'Rectangle',
        {
          fill: lightgrad,
          stroke: '#756875',
          strokeWidth: 3
        }),
      graphObject(go.Panel, 'Table',
        {margin: 8, stretch: go.GraphObject.Fill},
        graphObject(go.RowColumnDefinition,
          {
            row: 0,
            sizing: go.RowColumnDefinition.None
          }),
        // the table header
        graphObject(go.TextBlock,
          {
            row: 0,
            alignment: go.Spot.Center,
            margin: new go.Margin(0, 14, 0, 2),  // leave room for Button
            font: 'bold 16px sans-serif'
          },
          new go.Binding('text', 'key')),
        // the collapse/expand button
        graphObject('PanelExpanderButton', 'LIST',  // the name of the element whose visibility this button toggles
          {
            row: 0,
            alignment: go.Spot.TopRight
          }),
        // the list of Panels, each showing an attribute
        graphObject(go.Panel, 'Vertical',
          {
            name: 'LIST',
            row: 1,
            padding: 3,
            alignment: go.Spot.TopLeft,
            defaultAlignment: go.Spot.Left,
            stretch: go.GraphObject.Horizontal,
            itemTemplate: itemTempl
          },
          new go.Binding('itemArray', 'items'))
      )  // end Table Panel
    )  // end Node
  // define the Link template, representing a relationship
  diagram.linkTemplate =
    graphObject(go.Link,  // the whole link panel
      {
        selectionAdorned: true,
        layerName: 'Foreground',
        reshapable: true,
        routing: go.Link.AvoidsNodes,
        corner: 5,
        curve: go.Link.JumpOver
      },
      graphObject(go.Shape,  // the link shape
        {
          stroke: '#303B45',
          strokeWidth: 2.5
        }),
      graphObject(go.TextBlock,  // the "from" label
        {
          textAlign: 'center',
          font: 'bold 14px sans-serif',
          stroke: '#1967B3',
          segmentIndex: 0,
          segmentOffset: new go.Point(NaN, NaN),
          segmentOrientation: go.Link.OrientUpright
        },
        new go.Binding('text', 'text')),
      graphObject(go.TextBlock,  // the "to" label
        {
          textAlign: 'center',
          font: 'bold 14px sans-serif',
          stroke: '#1967B3',
          segmentIndex: -1,
          segmentOffset: new go.Point(NaN, NaN),
          segmentOrientation: go.Link.OrientUpright
        },
        new go.Binding('text', 'toText'))
    )
  diagram.model = new go.GraphLinksModel(graphData.nodeData, graphData.linkData)
  console.log(diagram)
}

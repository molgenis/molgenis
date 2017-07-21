import go from 'gojs'

export function newGraph (targetDiv, graphData) {
  const graphObject = go.GraphObject.make  // for conciseness in defining templates

  const headerFont = 'bold 11px sans-serif'
  const textFont = '10px sans-serif'
  const extendsFont = 'italic 10px sans-serif'

  // const lightGrad = graphObject(go.Brush, 'Linear', {1: '#f2f2f2', 0: '#f2f2f2'})

  const diagram =
    graphObject(go.Diagram, targetDiv,
      {
        initialContentAlignment: go.Spot.Center,
        allowDelete: false,
        allowCopy: false,
        layout: graphObject(go.ForceDirectedLayout),
        'undoManager.isEnabled': true
      })

  const itemTempl =
    graphObject(go.Panel, 'Horizontal',
      graphObject(go.Shape,
        {desiredSize: new go.Size(10, 10)},
        new go.Binding('figure', 'figure'),
        new go.Binding('fill', 'color')),
      graphObject(go.TextBlock,
        {
          stroke: '#333333',
          font: textFont
        },
        new go.Binding('text', 'name')))

  diagram.nodeTemplate =
    graphObject(go.Node, 'Auto',  // the whole node panel
      {
        selectionAdorned: true,
        resizable: true,
        layoutConditions: go.Part.LayoutStandard & ~go.Part.LayoutNodeSized,
        fromSpot: go.Spot.AllSides,
        toSpot: go.Spot.AllSides
      },

      new go.Binding('location', 'location').makeTwoWay(),
      new go.Binding('desiredSize', 'visible', function (v) {
        return new go.Size(NaN, NaN)
      }).ofObject('LIST'),
      graphObject(go.Shape, 'Rectangle',
        {
          stroke: '#756875',
          strokeWidth: 1
        },
        new go.Binding('fill', 'color')),
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
            font: headerFont
          },
          new go.Binding('text', 'key')),
        // expand button
        graphObject('PanelExpanderButton', 'LIST',  // the name of the element whose visibility this button toggles
          {
            row: 0,
            alignment: go.Spot.TopRight
          }),
        // show extended class
        graphObject(go.TextBlock,
          {
            row: 1,
            alignment: go.Spot.Center,
            margin: new go.Margin(0, 14, 0, 2),  // leave room for Button
            font: extendsFont
          },
          new go.Binding('text', 'extends')),
        // the list of Panels, each showing an attribute
        graphObject(go.Panel, 'Vertical',
          {
            name: 'LIST',
            row: 2,
            padding: 3,
            margin: 2,
            alignment: go.Spot.TopLeft,
            defaultAlignment: go.Spot.Left,
            stretch: go.GraphObject.Horizontal,
            itemTemplate: itemTempl
          },
          new go.Binding('itemArray', 'items'))
      )
    )

  diagram.groupTemplate =
    graphObject(go.Group, 'Vertical',
      graphObject(go.TextBlock,
        { alignment: go.Spot.Center, font: 'Bold 12pt Sans-Serif' },
        new go.Binding('text', 'key')),
      graphObject(go.Panel, 'Auto',
        graphObject(go.Shape, 'RoundedRectangle',
          {
            parameter1: 14
          },
          new go.Binding('fill', 'color')
        ),
        graphObject(go.Placeholder,
          {padding: 5})
      )
    )

  diagram.linkTemplate =
    graphObject(go.Link,  // the whole link panel
      {
        selectionAdorned: true,
        layerName: 'Foreground',
        reshapable: true,
        routing: go.Link.AvoidsNodes,
        corner: 2,
        curve: go.Link.JumpOver
      },
      graphObject(go.Shape,  // the link shape
        {
          stroke: '#303B45',
          strokeWidth: 1
        }),
      graphObject(go.TextBlock,  // the "from" label
        {
          textAlign: 'center',
          font: textFont,
          stroke: '#1967B3',
          segmentIndex: 0,
          segmentOffset: new go.Point(NaN, NaN),
          segmentOrientation: go.Link.OrientUpright
        },
        new go.Binding('text', 'text')),
      graphObject(go.TextBlock,  // the "to" label
        {
          textAlign: 'center',
          font: textFont,
          stroke: '#1967B3',
          segmentIndex: -1,
          segmentOffset: new go.Point(NaN, NaN),
          segmentOrientation: go.Link.OrientUpright
        },
        new go.Binding('text', 'toText'))
    )
  diagram.model = new go.GraphLinksModel(graphData.nodeData, graphData.linkData)
}

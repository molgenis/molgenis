<template>
  <div id="sample">
    <div style="border: solid 1px black; width:600px; height:600px" ref="targetDiv"></div>
  </div>
</template>

<script>

  import { newGraph } from './Graph.js'

  const graphData = {
    nodeData: [
      {
        key: 'Products',
        items: [{name: 'ProductID', iskey: true, figure: 'Decision', color: 'yellow'},
          {name: 'ProductName', iskey: false, figure: 'Cube1', color: 'blue'},
          {name: 'SupplierID', iskey: false, figure: 'Decision', color: 'purple'},
          {name: 'CategoryID', iskey: false, figure: 'Decision', color: 'purple'}]
      },
      {
        key: 'Suppliers',
        items: [{name: 'SupplierID', iskey: true, figure: 'Decision', color: 'yellow'},
          {name: 'CompanyName', iskey: false, figure: 'Cube1', color: 'blue'},
          {name: 'ContactName', iskey: false, figure: 'Cube1', color: 'blue'},
          {name: 'Address', iskey: false, figure: 'Cube1', color: 'blue'}]
      },
      {
        key: 'Categories',
        items: [{name: 'CategoryID', iskey: true, figure: 'Decision', color: 'yellow'},
          {name: 'CategoryName', iskey: false, figure: 'Cube1', color: 'blue'},
          {name: 'Description', iskey: false, figure: 'Cube1', color: 'blue'},
          {name: 'Picture', iskey: false, figure: 'TriangleUp', color: 'red'}]
      },
      {
        key: 'Order Details',
        items: [{name: 'OrderID', iskey: true, figure: 'Decision', color: 'yellow'},
          {name: 'ProductID', iskey: true, figure: 'Decision', color: 'yellow'},
          {name: 'UnitPrice', iskey: false, figure: 'MagneticData', color: 'green'},
          {name: 'Quantity', iskey: false, figure: 'MagneticData', color: 'green'},
          {name: 'Discount', iskey: false, figure: 'MagneticData', color: 'green'}]
      }
    ],
    linkData: [
      {from: 'Products', to: 'Suppliers', text: '0..N', toText: '1'},
      {from: 'Products', to: 'Categories', text: '0..N', toText: '1'},
      {from: 'Order Details', to: 'Products', text: '0..N', toText: '1'}
    ]
  }

  export default {
    name: 'model-registry-uml-viewer',
    mounted: function () {
      const targetDiv = this.$refs.targetDiv
      newGraph(targetDiv, graphData)
    }

  }

</script>

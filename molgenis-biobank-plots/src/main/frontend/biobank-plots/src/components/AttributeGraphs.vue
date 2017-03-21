<template>
  <div>
    <vue-chart :rows="chart.rows" :columns="chart.columns" :options="options"
               chartType="ColumnChart" :chartEvents="chartEvents"
               v-for="chart in attributeCharts"></vue-chart>
  </div>
</template>

<script>
  import {mapState} from 'vuex'

  export default {
    data: function () {
      return {
        chartEvents: {
          'select': function () {
            console.log('Select (but what?)!')
          }
        },
        options: {
          width: 600,
          height: 400,
          legend: { position: 'top', maxLines: 3 },
          bar: { groupWidth: '25%' },
          isStacked: true
        }
      }
    },
    computed: {
      ...mapState(['attributeCharts'])
    },
    name: 'attribute-graphs'
  }
</script>

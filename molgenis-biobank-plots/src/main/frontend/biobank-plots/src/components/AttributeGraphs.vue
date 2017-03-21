<template>
  <div>
    <template v-for="chart in attributeCharts">
    <h4>{{chart.title}}</h4>
    <vue-chart :rows="chart.rows" :columns="chart.columns" :options="options"
               chartType="ColumnChart" :chartEvents="chartEvents"></vue-chart>
    </template>
  </div>
</template>

<script>
  import {mapState} from 'vuex'

  export default {
    name: 'attribute-graphs',
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
    }
  }
</script>

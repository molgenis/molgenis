<template>
  <b-card>
      <h6 class="chart-header">{{data.title}} </h6>
      <vue-chart ref="chart" :rows="data.rows" :columns="data.columns"
        :options="options" chartType="ColumnChart" :chartEvents="chartEvents">
      </vue-chart>
  </b-card>
</template>

<script>
  import {resizeEventBus} from '../utils'

  export default {
    name: 'attribute-graph',
    props: ['data', 'colors', 'height'],
    data: function () {
      return {
        chartEvents: {
          'select': function () {
            console.log('Select (but what?)!')
          }
        },
        options: {
          legend: { position: 'top', maxLines: 10 },
          isStacked: true,
          width: '100%',
          height: this.height,
          colors: this.colors
        }
      }
    },
    created () {
      const self = this
      resizeEventBus.$on('resize', () => self.$refs.chart.drawChart())
    }
  }
</script>
<style scoped>
  .chart-header{
    color: #292b2c;
    text-align: center;
  }
</style>

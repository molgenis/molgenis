<template>
  <b-card>
      <h6 class="chart-header">{{data.title}} </h6>
      <vue-chart ref="chart" :rows="rows" :columns="columns"
        chartType="PieChart" :chartEvents="chartEvents">
      </vue-chart>
  </b-card>
</template>

<script>
  import {resizeEventBus, zip} from '../utils'

  export default {
    name: 'pie-graph',
    props: ['data'],
    data: function () {
      return {
        columns: ['string', 'number'],
        chartEvents: {
          'select': function () {
            console.log('Select (but what?)!')
          }
        }
      }
    },
    computed: {
      rows () {
        return zip([this.data.columns.map((column) => column.label), this.data.rows[0]]).slice(1)
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

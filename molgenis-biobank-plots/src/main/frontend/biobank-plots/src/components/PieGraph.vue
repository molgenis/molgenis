<template>
  <b-card>
      <h6 class="chart-header">{{data.title}} </h6>
      <vue-chart ref="chart" :rows="data.rows" :columns="data.columns"
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
      var labels = ['<20', '20-30', '30-40', '40-50', '50-60', '60-70', '70-80', 'undefined']
      this.data.rows[0].shift()
      this.data.rows = zip([labels, this.data.rows[0].map(row => row)])
      this.data.columns = ['string', 'number']
      console.log(this.data.rows)
      return {
        chartEvents: {
          'select': function () {
            console.log('Select (but what?)!')
          }
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

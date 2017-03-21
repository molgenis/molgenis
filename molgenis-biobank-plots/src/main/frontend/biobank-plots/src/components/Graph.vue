<template>
  <div>
    <h4>Number of Samples per Biobank</h4>
    <vue-chart :columns="columns" :rows="aggs" :options="options"
               chartType="BarChart" :chartEvents="chartEvents"
               ref="sampleCounts"></vue-chart>
  </div>
</template>

<script>
  import {mapState} from 'vuex'
  import Vue from 'vue'
  import VueCharts from 'vue-charts'

  import { SET_BIOBANK } from '../store/actions'

  Vue.use(VueCharts)

  export default {
    name: 'graph',
    data: function () {
      const self = this
      return {
        chartEvents: {
          'select': function () {
            const row = self.$refs.sampleCounts.chart.getSelection()[0].row
            const biobank = self.aggs[row][0]
            self.$store.dispatch(SET_BIOBANK, biobank)
          }
        },
        columns: [{
          'type': 'string',
          'label': 'Biobank'
        }, {
          'type': 'number',
          'label': 'SampleCount'
        }],
        options: {
          hAxis: {
            title: 'Number of Samples'
          },
          vAxis: {
            title: 'Biobank'
          },
          height: 500
        }
      }
    },
    computed: {
      ...mapState(['aggs'])
    }
  }
</script>

<template>
  <vue-chart :columns="columns" :rows="aggs" :options="options"
             chartType="BarChart" :chartEvents="chartEvents"
             ref="sampleCounts"></vue-chart>
</template>

<script>
  import {mapState} from 'vuex'
  import Vue from 'vue'
  import VueCharts from 'vue-charts'

  Vue.use(VueCharts)

  export default {
    data: function () {
      const self = this
      return {
        chartEvents: {
          'select': function () {
            const row = self.$refs.sampleCounts.chart.getSelection()[0].row
            const biobank = self.aggs[row][0]
            self.$store.dispatch('setBiobank', biobank)
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
          title: 'Number of Samples per Biobank',
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
    },
    name: 'graph'
  }
</script>

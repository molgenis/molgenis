<template>
  <b-card show-header variant="primary" v-if="aggs.length">
    <div slot="header">
      <h4>Number of Samples per Biobank</h4>
      <h6 class="card-subtitle mb-2">Total number of samples: {{numberOfSamples}}</h6>
    </div>
    <div class="card">
      <vue-chart :columns="columns" :rows="aggs" :options="options"
                 chartType="BarChart" :chartEvents="chartEvents"
                 ref="sampleCounts"></vue-chart>
    </div>
  </b-card>
</template>

<script>
  import Vue from 'vue'
  import VueCharts from 'vue-charts'
  import {resizeEventBus, chartColors} from '../utils'

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
          type: 'string',
          label: 'Biobank'
        }, {
          type: 'number',
          label: 'Samples'
        }, {
          type: 'string',
          role: 'style'
        }],
        options: {
          hAxis: {
            title: 'Number of Samples',
            maxValue: 5500
          },
          vAxis: {
            title: 'Biobank'
          },
          height: 700,
          width: '100%'
        }
      }
    },
    computed: {
      numberOfSamples: {
        get () {
          return this.$store.state.numberOfSamples
        }
      },
      aggs () {
        return this.$store.state.aggs && this.$store.state.aggs.map(row => [...row, row[0] === this.$store.state.biobank
            ? chartColors[1] : chartColors[0]])
      }
    },
    created () {
      const self = this
      resizeEventBus.$on('resize', () => self.$refs.sampleCounts.drawChart())
    }
  }
</script>

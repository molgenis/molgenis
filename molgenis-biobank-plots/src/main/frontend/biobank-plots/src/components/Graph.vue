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
          'label': 'Samples'
        }],
        options: {
          hAxis: {
            title: 'Number of Samples',
            maxValue: 2500
          },
          vAxis: {
            title: 'Biobank'
          },
          height: 325,
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
      ...mapState(['aggs'])
    }
  }
</script>

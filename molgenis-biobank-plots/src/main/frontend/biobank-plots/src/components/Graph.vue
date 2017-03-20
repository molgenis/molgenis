<template>
  <div>
    <vue-chart :columns="columns" :rows="aggs" :options="options" chartType="BarChart"></vue-chart>
  </div>
</template>

<script>
  import {mapGetters, mapState} from 'vuex'
  import Vue from 'vue'
  import VueCharts from 'vue-charts'

  Vue.use(VueCharts)

  export default {
    data: function () {
      return {
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
      ...mapState(['aggs']),
      ...mapGetters(['rsql'])
    },
    name: 'graph'
  }
</script>

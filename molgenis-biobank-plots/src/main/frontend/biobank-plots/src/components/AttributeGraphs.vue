<template>
  <div v-if="attributeCharts.length">
    <b-card show-header variant="primary">
      <div slot="header">
        <h4 v-if="biobank">Sample makeup: {{biobank}}</h4>
        <h4 v-else>Sample makeup: All biobanks</h4>
      </div>
      <div class="row">
        <div class="col-4" v-for="chart in attributeCharts">
          <b-card>
            <h6 class="chart-header">{{chart.title}}</h6>
            <vue-chart ref="chart" :rows="chart.rows" :columns="chart.columns" :options="options"
                       chartType="ColumnChart" :chartEvents="chartEvents"></vue-chart>
          </b-card>
        </div>
      </div>
    </b-card>
  </div>
</template>

<script>
  import {mapState} from 'vuex'
  import {resizeEventBus} from '../utils'

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
          legend: { position: 'top', maxLines: 3 },
          isStacked: true,
          width: '100%'
        }
      }
    },
    computed: {
      ...mapState(['attributeCharts', 'biobank'])
    },
    created () {
      const self = this
      resizeEventBus.$on('resize', () => self.$refs.chart.map(child => child.drawChart()))
    }
  }
</script>
<style scoped>
  .chart-header{
    color: #292b2c;
    text-align: center;
  }
</style>

<template>
    <div v-if="attributeCharts && attributeCharts.gender && attributeCharts.smoking">
      <b-card show-header variant="primary">
        <div slot="header">
          <h4 v-if="biobank">Sample makeup: {{biobank}}</h4>
          <h4 v-else>Sample makeup: All biobanks </h4>
        </div>
        <div class="row">
          <div class="col-12">

            <b-card>
              <h6 class="chart-header">{{attributeCharts.gender.title}}</h6>
              <vue-chart ref="chart" :rows="attributeCharts.gender.rows" :columns="attributeCharts.gender.columns"
                :options="options"
                chartType="ColumnChart" :chartEvents="chartEvents"></vue-chart>
            </b-card>
            <b-card>
              <h6 class="chart-header">{{attributeCharts.smoking.title}}</h6>
              <vue-chart ref="chart" :rows="attributeCharts.smoking.rows" :columns="attributeCharts.smoking.columns"
                         :options="options"
                         chartType="ColumnChart" :chartEvents="chartEvents"></vue-chart>
            </b-card>
          </div>
        </div>
      </b-card>
    </div>

</template>

<script>
  import {mapState, mapGetters} from 'vuex'
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
          legend: { position: 'top', maxLines: 10 },
          isStacked: true,
          width: '100%'
        }
      }
    },
    computed: {
      ...mapGetters(['attributeCharts']),
      ...mapState(['biobank'])
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

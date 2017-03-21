<template>
  <div v-if="attributeCharts.length">
    <b-card show-header>
      <div slot="header">
        <h4 v-if="biobank">Sample makeup: {{biobank}}</h4>
        <h4 v-else>Sample makeup: All biobanks</h4>
      </div>
      <div class="row">
        <div v-for="chart in attributeCharts" class="col-md-4">
          {{chart.title}}
          <vue-chart :rows="chart.rows" :columns="chart.columns" :options="options"
                     chartType="ColumnChart" :chartEvents="chartEvents"></vue-chart>
        </div>
      </div>
    </b-card>
  </div>
</template>

<script>
  import {mapState} from 'vuex'

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
    }
  }
</script>

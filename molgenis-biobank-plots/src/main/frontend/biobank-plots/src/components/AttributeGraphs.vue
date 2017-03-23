<template>
    <div v-if="attributeCharts && attributeCharts.gender && attributeCharts.smoking">
      <b-card show-header variant="primary">
        <div slot="header">
          <h4 v-if="biobank">Sample makeup: {{biobank}}</h4>
          <h4 v-else>Sample makeup: All biobanks </h4>
        </div>
        <div class="row">
          <div class="col-md-12">
            <attribute-graph :data="attributeCharts.data_types" height="220"></attribute-graph>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6">
            <div class="row">
              <div class="col-md-12">
                <attribute-graph :data="attributeCharts.smoking" height="220"></attribute-graph>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">
                <attribute-graph :data="attributeCharts.gender" height="220"></attribute-graph>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <pie-graph :data="attributeCharts.age" height="507"></pie-graph>
          </div>
        </div>
      </b-card>
    </div>

</template>

<script>
  import {mapState, mapGetters} from 'vuex'
  import AttributeGraph from './AttributeGraph'
  import PieGraph from './PieGraph'

  export default {
    name: 'attribute-graphs',
    components: {AttributeGraph, PieGraph},
    computed: {
      ...mapGetters(['attributeCharts']),
      ...mapState(['biobank'])
    }
  }
</script>
<style scoped>
  .chart-header{
    color: #292b2c;
    text-align: center;
  }
</style>

<template>
  <div>
    <dl>
      <template v-for="agg in aggs">
        <dt>{{agg.biobank}}</dt>
        <dd>{{agg.sampleCount}}</dd>
      </template>
    </dl>
  </div>
</template>

<script>
  import {mapGetters, mapState} from 'vuex'

  export default {
    computed: {
      ...mapState(['aggs']),
      ...mapGetters(['rsql'])
    },
    name: 'graph'
  }
</script>

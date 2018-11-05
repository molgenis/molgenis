<template>
  <div
    v-if="jobs.length > 0"
    class="row">
    <div class="col"><!-- TODO dismissable when variant is success/failed -->
      <b-alert
        v-for="(job, index) in jobs"
        :key="index"
        :variant="getVariant(job)"
        show
        @dismissed="removeAlert(index)">
        <span>copying ...</span>
      </b-alert>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { REMOVE_ALERT } from '../store/mutations'

export default {
  name: 'Jobs',
  computed: {
    ...mapState(['jobs'])
  },
  methods: {
    getVariant: function (job) {
      let variant
      switch (job.status) {
        case 'running':
          variant = 'info'
          break
        case 'success':
          variant = 'success'
          break
        case 'failed':
          variant = 'danger'
          break
        default:
          throw new Error('unexpected job type ' + job.type)
      }
      return variant
    },
    removeAlert: function (index) {
      this.$store.commit(REMOVE_ALERT, index)
    }
  }
}
</script>

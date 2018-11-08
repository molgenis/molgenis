<template>
  <div
    v-if="jobs.length > 0"
    class="row">
    <div class="col">
      <b-alert
        v-for="(job, index) in jobs"
        :key="index"
        :variant="getVariant(job)"
        :dismissible="job.status !== 'running'"
        show
        @dismissed="removeAlert(index)">
        <span v-if="job.status === 'running'">Preparing download ...</span>
        <span v-else >Download ready, click
          <a
            :href="job.resultUrl"
            download>here</a> to download your file</span>
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
          throw new Error('unexpected job status ' + job.status)
      }
      return variant
    },
    removeAlert: function (index) {
      this.$store.commit(REMOVE_ALERT, index)
    }
  }
}
</script>

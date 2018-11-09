<template>
  <div
    v-if="jobs.length > 0"
    class="row">
    <div class="col">
      <div
        v-for="(job, index) in jobs"
        :key="index">
        <b-alert
          :variant="getVariant(job)"
          :dismissible="job.status !== 'running'"
          show
          @dismissed="removeJob(job)">
          <div v-if="job.type === 'copy'">
            <span v-if="job.status === 'running'">
              {{ 'progress-copy-running' | i18n }}
              <span v-if="job.progress">
                &nbsp;{{ job.progress }}/{{ job.progressMax }}
              </span>
              &nbsp;...
            </span>
            <span v-else-if="job.status === 'success'">
              {{ 'progress-copy-success' | i18n }}
            </span>
            <span v-else-if="job.status === 'failed'">
              {{ 'progress-copy-failed' | i18n }}
            </span>
          </div>
          <div v-else-if="job.type === 'download'">
            <span v-if="job.status === 'running'">
              {{ 'progress-download-running' | i18n }}
              <span v-if="job.progress">
                &nbsp;{{ job.progress }}/{{ job.progressMax }}
              </span>
              &nbsp;...
            </span>
            <span v-else-if="job.status === 'success'">
              {{ 'progress-download-success' | i18n }}<br>
              {{ 'progress-download-success-action-pre' | i18n }}
              <a
                :href="job.resultUrl"
                download>{{ 'progress-download-success-action' | i18n }}</a>
              {{ 'progress-download-success-action-post' | i18n }}
            </span>
            <span v-else-if="job.status === 'failed'">
              {{ 'progress-download-failed' | i18n }}
            </span>
          </div>
        </b-alert>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { REMOVE_JOB } from '../store/mutations'

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
    removeJob: function (job) {
      this.$store.commit(REMOVE_JOB, job)
    }
  }
}
</script>

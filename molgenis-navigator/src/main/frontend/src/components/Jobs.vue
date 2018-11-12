<template>
  <div class="fixed-bottom job-alerts">
    <div
      v-if="jobs.length > 0"
      class="row">
      <div class="col">
        <div
          v-for="(job, index) in jobs"
          :key="index">
          <b-alert
            :variant="getVariant(job)"
            :dismissible="job.status !== 'RUNNING'"
            show
            @dismissed="removeJob(job)">
            <div v-if="job.type === 'COPY'">
              <span v-if="job.status === 'RUNNING'">
                <font-awesome-icon :icon="['far', 'hourglass']"/> {{ 'progress-copy-running' | i18n }}
                <span v-if="job.progress">
                  &nbsp;{{ job.progress }}/{{ job.progressMax }}
                </span>
                &nbsp;...
              </span>
              <span v-else-if="job.status === 'SUCCESS'">
                {{ 'progress-copy-success' | i18n }}
              </span>
              <span v-else-if="job.status === 'FAILED'">
                {{ 'progress-copy-failed' | i18n }}
              </span>
            </div>
            <div v-else-if="job.type === 'DOWNLOAD'">
              <span v-if="job.status === 'RUNNING'">
                <font-awesome-icon :icon="['far', 'hourglass']"/> {{ 'progress-download-running' | i18n }}
                <span v-if="job.progress">
                  &nbsp;{{ job.progress }}/{{ job.progressMax }}
                </span>
                &nbsp;...
              </span>
              <span v-else-if="job.status === 'SUCCESS'">
                {{ 'progress-download-success' | i18n }}<br>
                {{ 'progress-download-success-action-pre' | i18n }}
                <a
                  :href="job.resultUrl"
                  download>{{ 'progress-download-success-action' | i18n }}</a>
                {{ 'progress-download-success-action-post' | i18n }}
              </span>
              <span v-else-if="job.status === 'FAILED'">
                {{ 'progress-download-failed' | i18n }}
              </span>
            </div>
          </b-alert>
        </div>
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
        case 'RUNNING':
          variant = 'info'
          break
        case 'SUCCESS':
          variant = 'success'
          break
        case 'FAILED':
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

<template>
  <div class="fixed-bottom job-alerts">
    <div
      v-if="jobs.length > 0"
      class="row">
      <div class="col">
        <div
          v-for="job in jobs"
          :key="job.id">
          <b-alert
            :variant="getVariant(job)"
            :dismissible="job.status !== 'RUNNING'"
            show
            @dismissed.prevent="removeJob(job)">
            <span v-if="job.status === 'RUNNING'">
              <font-awesome-icon :icon="['far', 'hourglass']"/>
              <span class="alert-message">{{ job.progressMessage }}</span>
              <span v-if="job.progress">{{ job.progress }}/{{ job.progressMax }}</span>
            </span>
            <span v-else-if="job.status === 'SUCCESS'">
              <font-awesome-icon :icon="['far', 'check-circle']"/>
              <span class="alert-message">{{ job.progressMessage }}
              </span>
              <span v-if="job.type === 'DOWNLOAD'">
                <span>{{ 'progress-download-success-action-pre' | i18n }}</span>
                <a
                  :href="job.resultUrl"
                  download
                  class="alert-link">{{ progress-download-success-action | i18n }}</a>
                <span>{{ 'progress-download-success-action-post' | i18n }}</span>
              </span>
            </span>
            <span v-else-if="job.status === 'FAILED'">
              <font-awesome-icon :icon="['far', 'times-circle']"/>
              <span class="alert-message">{{ job.progressMessage }}</span>
              <span v-if="job.progress">{{ job.progress }}/{{ job.progressMax }}</span>
            </span>
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

<style scoped>
  .alert-message {
    margin-left: .75rem
  }
</style>

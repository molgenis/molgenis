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
            @dismissed="removeJob(job)">
            <div class="row">
              <div class="col-1">
                <font-awesome-icon :icon="['far', getIcon(job)]"/>
              </div>
              <div class="col-11">
                <span v-if="job.progressMessage">{{ job.progressMessage }}</span>
                <span v-if="showProgress(job)">{{ job.progress }}/{{ job.progressMax }}</span>
                <span v-if="job.type === 'DOWNLOAD' && job.status === 'SUCCESS'">
                  <br>
                  <span>{{ 'progress-download-success-action-pre' | i18n }}</span>
                  <a
                    :href="job.resultUrl"
                    download
                    class="alert-link">{{ 'progress-download-success-action' | i18n }}</a>
                  <span>{{ 'progress-download-success-action-post' | i18n }}</span>
                </span>
              </div>
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
    getIcon: function (job) {
      let icon
      switch (job.status) {
        case 'RUNNING':
          icon = 'hourglass'
          break
        case 'SUCCESS':
          icon = 'check-circle'
          break
        case 'FAILED':
          icon = 'times-circle'
          break
        default:
          throw new Error('unexpected job status ' + job.status)
      }
      return icon
    },
    removeJob: function (job) {
      this.$store.commit(REMOVE_JOB, job)
    },
    showProgress: function (job) {
      return job.status === 'RUNNING' && job.progress && job.progressMax
    }
  }
}
</script>

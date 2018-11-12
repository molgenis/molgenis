<template>
  <div
    v-if="alerts.length > 0"
    class="row">
    <div class="col">
      <div
        v-for="(alert, index) in alerts"
        :key="index">
        <b-alert
          :variant="getVariant(alert)"
          show
          dismissible
          @dismissed.prevent="removeAlert(index)">
          <span>{{ alert.message }}</span>
          <span v-if="alert.code"> ({{ alert.code }})</span>
        </b-alert>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { REMOVE_ALERT } from '../store/mutations'

export default {
  name: 'Alerts',
  computed: {
    ...mapState(['alerts'])
  },
  methods: {
    getVariant: function (alert) {
      let variant
      switch (alert.type) {
        case 'INFO':
          variant = 'info'
          break
        case 'SUCCESS':
          variant = 'success'
          break
        case 'WARNING':
          variant = 'warning'
          break
        case 'ERROR':
          variant = 'danger'
          break
        default:
          throw new Error('unexpected alert type ' + alert.type)
      }
      return variant
    },
    removeAlert: function (index) {
      this.$store.commit(REMOVE_ALERT, index)
    }
  }
}
</script>

<template>
  <div
    v-if="alerts.length > 0"
    class="row">
    <div class="col">
      <div
        v-for="(alert, index) in alerts"
        :key="index"
        :class="{'alert':true, 'alert-danger': alert.type === 'ERROR', 'alert-warning': alert.type === 'WARNING', 'alert-info': alert.type === 'INFO'}"
        role="alert">
        <button
          type="button"
          class="close"
          @click="removeAlert(index)">
          <span aria-hidden="true">&times;</span>
        </button>
        <span>{{ alert.message }}</span>
        <span v-if="alert.code"> ({{ alert.code }})</span>
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
    removeAlert: function (index) {
      this.$store.commit(REMOVE_ALERT, index)
    }
  }
}
</script>

<style scoped>

</style>

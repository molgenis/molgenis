<template>
  <span>
    <b-btn
      v-b-tooltip.hover
      :title="$t('action-upload')"
      variant="secondary"
      to="/menu/importdata/importwizard">
      <font-awesome-icon
        icon="upload"
        size="lg"/>
    </b-btn>
    <b-btn
      v-b-tooltip.hover
      :title="$t('action-download')"
      :disabled="nrSelectedItems > 0 ? false : true"
      variant="secondary"
      class="button-last"
      @click="downloadSelectedItems">
      <font-awesome-icon
        :class="nrSelectedItems > 0 ? '' : 'fa-disabled'"
        icon="download"
        size="lg"/>
    </b-btn>
  </span>
</template>

<script>
import { SCHEDULE_DOWNLOAD_SELECTED_ITEMS } from '../store/actions'
import { mapGetters } from 'vuex'

export default {
  name: 'NavigatorActionsTransfer',
  computed: {
    ...mapGetters(['nrSelectedItems'])
  },
  methods: {
    downloadSelectedItems: function () {
      this.$store.dispatch(SCHEDULE_DOWNLOAD_SELECTED_ITEMS)
    }
  }
}
</script>

<style scoped>

</style>

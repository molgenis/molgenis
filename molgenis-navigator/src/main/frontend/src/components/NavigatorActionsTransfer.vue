<template>
  <span>
    <div
      v-b-tooltip.d500
      :title="$t('action-upload')"
      class="btn-tooltip-wrapper">
      <b-btn
        :disabled="!canUpload"
        variant="secondary"
        to="/menu/importdata/importwizard">
        <font-awesome-icon
          :class="{'fa-disabled' : !canUpload}"
          icon="upload"
          size="lg"/>
      </b-btn>
    </div>
    <div
      v-b-tooltip.d500
      :title="$t('action-download')"
      class="btn-tooltip-wrapper">
      <b-btn
        :disabled="!canDownload"
        variant="secondary"
        @click="downloadSelectedItems">
        <font-awesome-icon
          :class="{'fa-disabled' : !canDownload}"
          icon="download"
          size="lg"/>
      </b-btn>
    </div>
  </span>
</template>

<script>
import { DOWNLOAD_SELECTED_ITEMS } from '../store/actions'
import { mapGetters } from 'vuex'

export default {
  name: 'NavigatorActionsTransfer',
  computed: {
    ...mapGetters(['nrSelectedItems', 'query']),
    canDownload () {
      return this.nrSelectedItems > 0
    },
    canUpload () {
      return !this.query
    }
  },
  methods: {
    downloadSelectedItems: function () {
      this.$store.dispatch(DOWNLOAD_SELECTED_ITEMS)
    }
  }
}
</script>

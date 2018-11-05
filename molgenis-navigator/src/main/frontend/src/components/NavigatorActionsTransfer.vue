<template>
  <span>
    <b-btn
      v-b-tooltip.hover
      :title="$t('action-upload')"
      :disabled="!canUpload"
      variant="secondary"
      to="/menu/importdata/importwizard">
      <font-awesome-icon
        :class="{'fa-disabled' : !canUpload}"
        icon="upload"
        size="lg"/>
    </b-btn>
    <b-btn
      v-b-tooltip.hover
      :title="$t('action-download')"
      :disabled="!canDownload"
      variant="secondary"
      @click="downloadSelectedItems">
      <font-awesome-icon
        :class="{'fa-disabled' : !canDownload}"
        icon="download"
        size="lg"/>
    </b-btn>
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

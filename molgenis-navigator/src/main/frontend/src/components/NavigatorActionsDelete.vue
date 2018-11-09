<template>
  <div
    v-b-tooltip.d500
    :title="$t('action-delete')"
    class="btn-tooltip-wrapper">
    <b-btn
      v-b-modal.itemDeleteModal
      :disabled="!canDelete"
      variant="danger">
      <font-awesome-icon
        :class="{'fa-disabled': !canDelete}"
        icon="trash"
        size="lg"/>
    </b-btn>
  </div>
</template>

<script>
import { DOWNLOAD_SELECTED_ITEMS } from '../store/actions'
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'NavigatorActionsTransfer',
  computed: {
    ...mapGetters(['nrSelectedItems']),
    ...mapState(['folder']),
    getSelectedItemType () {
      return this.selectedItems[0].type
    },
    canDelete () {
      return this.nrSelectedItems > 0 && !(this.folder && this.folder.readonly)
    }
  },
  methods: {
    downloadSelectedItems: function () {
      this.$store.dispatch(DOWNLOAD_SELECTED_ITEMS)
    }
  }
}
</script>

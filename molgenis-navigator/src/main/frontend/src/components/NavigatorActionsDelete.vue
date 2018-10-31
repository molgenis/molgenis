<template>
  <b-btn
    v-b-tooltip.hover
    v-b-modal.itemDeleteModal
    :title="$t('action-delete')"
    :disabled="!canDelete"
    variant="danger">
    <font-awesome-icon
      :class="{'fa-disabled': !canDelete}"
      icon="trash"
      size="lg"/>
  </b-btn>
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

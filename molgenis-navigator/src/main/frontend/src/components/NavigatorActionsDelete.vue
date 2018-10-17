<template>
  <b-btn
    v-b-tooltip.hover
    v-b-modal.itemDeleteModal
    :title="$t('action-delete')"
    :disabled="nrSelectedItems > 0 ? false : true"
    variant="danger">
    <font-awesome-icon
      :class="nrSelectedItems > 0 ? '' : 'fa-disabled'"
      icon="trash"
      size="lg"/>
  </b-btn>
</template>

<script>
import { SCHEDULE_DOWNLOAD_SELECTED_ITEMS } from '../store/actions'
import { mapGetters } from 'vuex'

export default {
  name: 'NavigatorActionsTransfer',
  computed: {
    ...mapGetters(['nrSelectedItems']),
    getSelectedItemType () {
      return this.selectedItems[0].type
    }
  },
  methods: {
    downloadSelectedItems: function () {
      this.$store.dispatch(SCHEDULE_DOWNLOAD_SELECTED_ITEMS)
    }
  }
}
</script>

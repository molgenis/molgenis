<template>
  <span>
    <b-dd
      v-b-tooltip.hover
      :disabled="query ? true : false"
      :title="$t('action-create')"
      variant="primary">
      <template slot="button-content">
        <font-awesome-icon
          :class="query ? 'fa-disabled' : ''"
          icon="plus"
          size="lg"/>
      </template>
      <b-dd-item v-b-modal.packageCreateModal>
        <font-awesome-icon :icon="['far', 'folder-open']"/> {{ 'action-create-package' | i18n }}
      </b-dd-item>
      <b-dd-item to="/menu/dataintegration/metadata-manager">
        <font-awesome-icon icon="list"/> {{ 'action-create-entity-type' | i18n }}
      </b-dd-item>
    </b-dd>
    <b-btn
      v-b-tooltip.hover
      v-b-modal.packageUpdateModal
      v-if="nrSelectedItems === 1 && getSelectedItemType === 'package'"
      :title="$t('action-edit')"
      variant="secondary"
      class="button-last">
      <font-awesome-icon
        icon="edit"
        size="lg"/>
    </b-btn>
    <b-btn
      v-b-tooltip.hover
      v-else-if="nrSelectedItems === 1 && getSelectedItemType === 'entityType'"
      :to="'/menu/dataintegration/metadata-manager/' + selectedEntityTypeIds[0]"
      :title="$t('action-edit')"
      variant="secondary"
      class="button-last">
      <font-awesome-icon
        icon="edit"
        size="lg"/>
    </b-btn>
    <b-btn
      v-b-tooltip.hover
      v-else
      :title="$t('action-edit')"
      :disabled="true"
      variant="secondary"
      class="button-last">
      <font-awesome-icon
        icon="edit"
        size="lg"
        class="fa-disabled"/>
    </b-btn>
  </span>
</template>

<script>
import { SCHEDULE_DOWNLOAD_SELECTED_ITEMS } from '../store/actions'
import { mapGetters } from 'vuex'

export default {
  name: 'NavigatorActionsTransfer',
  computed: {
    ...mapGetters(['nrSelectedItems', 'query']),
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

<template>
  <span>
    <b-dd
      v-b-tooltip.hover
      :disabled="!canCreate"
      :title="$t('action-create')"
      variant="primary">
      <template slot="button-content">
        <font-awesome-icon
          :class="{'fa-disabled' : !canCreate}"
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
      :disabled="!canEdit"
      :title="$t('action-edit')"
      variant="secondary"
      class="button-last">
      <font-awesome-icon
        :class="{'fa-disabled' : !canEdit}"
        icon="edit"
        size="lg"/>
    </b-btn>
    <b-btn
      v-b-tooltip.hover
      v-else-if="nrSelectedItems === 1 && getSelectedItemType === 'entityType'"
      :to="'/menu/dataintegration/metadata-manager/' + selectedItems[0].id"
      :disabled="!canEdit"
      :title="$t('action-edit')"
      variant="secondary"
      class="button-last">
      <font-awesome-icon
        :class="{'fa-disabled' : !canEdit}"
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
        :class="{'fa-disabled' : !canEdit}"
        icon="edit"
        size="lg"/>
    </b-btn>
  </span>
</template>

<script>
import { DOWNLOAD_SELECTED_ITEMS } from '../store/actions'
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'NavigatorActionsCreateEdit',
  computed: {
    ...mapGetters(['nrSelectedItems', 'query']),
    ...mapState(['folder', 'selectedItems']),
    getSelectedItemType () {
      return this.selectedItems[0].type
    },
    canCreate () {
      return !(this.query || (this.folder && this.folder.readonly))
    },
    canEdit () {
      return !(this.query || (this.folder && this.folder.readonly)) && this.nrSelectedItems === 1
    }
  },
  methods: {
    downloadSelectedItems: function () {
      this.$store.dispatch(DOWNLOAD_SELECTED_ITEMS)
    }
  }
}
</script>

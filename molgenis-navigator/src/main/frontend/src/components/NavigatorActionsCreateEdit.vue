<template>
  <span>
    <div
      v-b-tooltip.d500
      :title="$t('action-create')"
      class="btn-tooltip-wrapper">
      <b-dd
        :disabled="!canCreate"
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
    </div>
    <div
      v-b-tooltip.d500
      :title="$t('action-edit')"
      class="btn-tooltip-wrapper">
      <b-btn
        v-b-modal.packageUpdateModal
        v-if="nrSelectedItems === 1 && getSelectedItemType === 'PACKAGE'"
        :disabled="!canEdit"
        variant="secondary"
        class="button-last">
        <font-awesome-icon
          :class="{'fa-disabled' : !canEdit}"
          icon="edit"
          size="lg"/>
      </b-btn>
      <b-btn
        v-else-if="nrSelectedItems === 1 && (getSelectedItemType === 'ENTITY_TYPE' || getSelectedItemType === 'ENTITY_TYPE_ABSTRACT')"
        :to="'/menu/dataintegration/metadata-manager/' + selectedItems[0].id"
        :disabled="!canEdit"
        variant="secondary"
        class="button-last">
        <font-awesome-icon
          :class="{'fa-disabled' : !canEdit}"
          icon="edit"
          size="lg"/>
      </b-btn>
      <b-btn
        v-else
        :disabled="true"
        variant="secondary"
        class="button-last">
        <font-awesome-icon
          :class="{'fa-disabled' : !canEdit}"
          icon="edit"
          size="lg"/>
      </b-btn>
    </div>
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

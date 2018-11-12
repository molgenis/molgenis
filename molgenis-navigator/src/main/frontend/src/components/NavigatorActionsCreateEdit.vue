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
        v-if="nrSelectedResources === 1 && getSelectedResourceType === 'PACKAGE'"
        :disabled="!canEdit"
        variant="secondary"
        class="button-last">
        <font-awesome-icon
          :class="{'fa-disabled' : !canEdit}"
          icon="edit"
          size="lg"/>
      </b-btn>
      <b-btn
        v-else-if="nrSelectedResources === 1 && (getSelectedResourceType === 'ENTITY_TYPE' || getSelectedResourceType === 'ENTITY_TYPE_ABSTRACT')"
        :to="'/menu/dataintegration/metadata-manager/' + selectedResources[0].id"
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
import { DOWNLOAD_SELECTED_RESOURCES } from '../store/actions'
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'NavigatorActionsCreateEdit',
  computed: {
    ...mapGetters(['nrSelectedResources', 'query']),
    ...mapState(['folder', 'selectedResources']),
    getSelectedResourceType () {
      return this.selectedResources[0].type
    },
    canCreate () {
      return !(this.query || (this.folder && this.folder.readonly))
    },
    canEdit () {
      return !(this.query || (this.folder && this.folder.readonly)) && this.nrSelectedResources === 1
    }
  },
  methods: {
    downloadSelectedResources: function () {
      this.$store.dispatch(DOWNLOAD_SELECTED_RESOURCES)
    }
  }
}
</script>

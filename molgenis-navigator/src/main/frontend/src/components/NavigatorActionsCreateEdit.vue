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
        <b-dd-item
          v-if="metadataManagerUrl"
          :href="metadataManagerUrl">
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
        v-else-if="metadataManagerUrl && nrSelectedResources === 1 && (getSelectedResourceType === 'ENTITY_TYPE' || getSelectedResourceType === 'ENTITY_TYPE_ABSTRACT')"
        :href="metadataManagerUrl + '/' + selectedResources[0].id"
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
  data () {
    return {
      metadataManagerUrl: window.__INITIAL_STATE__.pluginUrls['metadata-manager']
    }
  },
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
      let canEdit = this.nrSelectedResources === 1 && !(this.query || this.selectedResources[0].readonly)
      if (canEdit) {
        switch (this.getSelectedResourceType) {
          case 'PACKAGE':
            break
          case 'ENTITY_TYPE':
          case 'ENTITY_TYPE_ABSTRACT':
            canEdit &= this.metadataManagerUrl !== undefined
            break
        }
      }
      return canEdit
    }
  },
  methods: {
    downloadSelectedResources: function () {
      this.$store.dispatch(DOWNLOAD_SELECTED_RESOURCES)
    }
  }
}
</script>

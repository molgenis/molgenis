<template>
  <div>
    <div class="row">
      <div class="col-10">
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
            <font-awesome-icon :icon="['far', 'folder-open']" /> {{ 'action-create-package' | i18n }}
          </b-dd-item>
          <b-dd-item to="/menu/dataintegration/metadata-manager">
            <font-awesome-icon icon="list" /> {{ 'action-create-entity-type' | i18n }}
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
        <NavigatorActionsClipboard class="button-last"/>
        <b-btn
          v-b-tooltip.hover
          :title="$t('action-upload')"
          variant="secondary"
          to="/menu/importdata/importwizard">
          <font-awesome-icon
            icon="upload"
            size="lg"/>
        </b-btn>
        <b-btn
          v-b-tooltip.hover
          :title="$t('action-download')"
          :disabled="nrSelectedItems > 0 ? false : true"
          variant="secondary"
          class="button-last"
          @click="downloadSelectedItems">
          <font-awesome-icon
            :class="nrSelectedItems > 0 ? '' : 'fa-disabled'"
            icon="download"
            size="lg"/>
        </b-btn>
      </div>
      <div class="col-2">
        <div class="float-right">
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
        </div>
      </div>
    </div>
    <NavigatorModalPackageCreate/>
    <NavigatorModalPackageUpdate/>
    <NavigatorModalItemDelete/>
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex'
import NavigatorModalPackageCreate from './NavigatorModalPackageCreate'
import NavigatorModalPackageUpdate from './NavigatorModalPackageUpdate'
import NavigatorModalItemDelete from './NavigatorModalItemDelete'
import NavigatorActionsClipboard from './NavigatorActionsClipboard'

export default {
  name: 'NavigatorActions',
  components: {
    NavigatorActionsClipboard,
    NavigatorModalPackageCreate,
    NavigatorModalPackageUpdate,
    NavigatorModalItemDelete
  },
  data () {
    return {
      selectedMoveItems: []
    }
  },
  computed: {
    ...mapGetters(['nrSelectedItems', 'query']),
    ...mapState(['selectedItems', 'clipboard']),
    getSelectedItemType () {
      return this.selectedItems[0].type
    },
    nrMovableItems () {
      var nrMovableItems = 0
      if (this.clipboard.mode === 'cut' && this.clipboard.packageIds) {
        nrMovableItems += this.clipboard.packageIds.length
      }
      if (this.clipboard.mode === 'cut' && this.clipboard.entityTypeIds) {
        nrMovableItems += this.clipboard.entityTypeIds.length
      }
      return nrMovableItems
    }
  },
  methods: {
    downloadSelectedItems: function () {
      alert('TODO download selection as EMX')
    }
  }
}
</script>

<style scoped>
  .button-last {
    margin-right: 1rem;
  }
</style>

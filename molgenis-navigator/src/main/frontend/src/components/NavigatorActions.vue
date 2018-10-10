<template>
  <div>
    <div class="row">
      <div class="col-10">
        <b-dd variant="primary" v-b-tooltip.hover :title="$t('action-create')" :class="query ? 'invisible' : ''">
          <template slot="button-content">
            <font-awesome-icon icon="plus" size="lg"/>
          </template>
          <b-dd-item v-b-modal.packageCreateModal>
            <font-awesome-icon :icon="['far', 'folder-open']" /> {{'action-create-package' | i18n }}
          </b-dd-item>
          <b-dd-item to="/menu/dataintegration/metadata-manager">
            <font-awesome-icon icon="list" /> {{'action-create-entity-type' | i18n }}
          </b-dd-item>
        </b-dd>
        <b-btn variant="secondary" v-if="nrSelectedItems === 1 && getSelectedItemType === 'package'" v-b-modal.packageUpdateModal v-b-tooltip.hover :title="$t('action-edit')" class="button-last">
          <font-awesome-icon icon="edit" size="lg"/>
        </b-btn>
        <b-btn variant="secondary" v-else-if="nrSelectedItems === 1 && getSelectedItemType === 'entityType'" :to="'/menu/dataintegration/metadata-manager/' + selectedEntityTypeIds[0]" v-b-tooltip.hover :title="$t('action-edit')" class="button-last">
          <font-awesome-icon icon="edit" size="lg"/>
        </b-btn>
        <b-btn variant="secondary" v-else :disable="true" v-b-tooltip.hover :title="$t('action-edit')" class="button-last">
          <font-awesome-icon icon="edit" size="lg" class="fa-disabled"/>
        </b-btn>
        <NavigatorActionsClipboard class="button-last"/>
        <b-btn variant="secondary" to="/menu/importdata/importwizard" v-b-tooltip.hover :title="$t('action-upload')" :class="query ? 'invisible' : ''">
          <font-awesome-icon icon="upload" size="lg"/>
        </b-btn>
        <b-btn variant="secondary" :disable="nrSelectedItems > 0 ? false : true" class="button-last" v-b-tooltip.hover :title="$t('action-download')" @click="downloadSelectedItems">
          <font-awesome-icon icon="download" size="lg" :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
        </b-btn>
      </div>
      <div class="col-2">
        <div class="float-right">
          <b-btn variant="danger" :disable="nrSelectedItems > 0 ? false : true" v-b-modal.itemDeleteModal v-b-tooltip.hover :title="$t('action-delete')">
            <font-awesome-icon icon="trash" size="lg" :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
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
        return Object.keys(this.selectedItems)[0]
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
  .invisible {
    visibility: hidden;
  }

  .button-last {
    margin-right: 1rem;
  }

  button[disable=true] {
    cursor: not-allowed !important;
  }
</style>

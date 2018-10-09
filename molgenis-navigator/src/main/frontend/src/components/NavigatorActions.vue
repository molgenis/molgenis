<template>
  <div>
    <div class="row">
      <div class="col-10">
        <b-dd :class="query ? 'invisible' : ''" variant="primary" v-b-tooltip.hover :title="$t('action-create')">
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
        <b-btn variant="secondary" to="/menu/importdata/importwizard" v-b-tooltip.hover :title="$t('action-upload')">
          <font-awesome-icon icon="upload" size="lg"/>
        </b-btn>
        <b-btn variant="secondary" :disable="nrSelectedItems > 0 ? false : true" class="button-last" v-b-tooltip.hover :title="$t('action-download')" @click="downloadSelectedItems">
          <font-awesome-icon icon="download" size="lg" :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
        </b-btn>
        <b-btn variant="secondary" :disable="nrSelectedItems > 0 ? false : true" v-b-tooltip.hover :title="$t('action-cut')" @click="selectClipboardItems('cut')">
          <font-awesome-icon icon="cut" size="lg" :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
        </b-btn>
        <b-btn variant="secondary" :disable="nrSelectedItems > 0 ? false : true" v-b-tooltip.hover :title="$t('action-copy')" @click="cloneSelectedItems">
          <font-awesome-icon icon="clone" size="lg" :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
        </b-btn>
        <b-btn variant="secondary" :disable="canMoveItems() ? false : true" v-b-tooltip.hover :title="$t('action-paste')" class="button-last" @click="moveClipboardItems">
          <font-awesome-icon icon="paste" size="lg" :class="canMoveItems() ? 'fa-enabled' : 'fa-disabled'"/>
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
  import { mapState } from 'vuex'
  import { MOVE_CLIPBOARD_ITEMS } from '../store/actions'
  import { SET_CLIPBOARD, RESET_CLIPBOARD } from '../store/mutations'
  import NavigatorModalPackageCreate from './NavigatorModalPackageCreate'
  import NavigatorModalPackageUpdate from './NavigatorModalPackageUpdate'
  import NavigatorModalItemDelete from './NavigatorModalItemDelete'

  export default {
    name: 'NavigatorActions',
    components: {NavigatorModalPackageCreate, NavigatorModalPackageUpdate, NavigatorModalItemDelete},
    data () {
      return {
        selectedMoveItems: []
      }
    },
    computed: {
      ...mapState(['query', 'selectedItems', 'clipboard']),
      nrSelectedItems () {
        return Object.keys(this.selectedItems).length
      },
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
      },
      cloneSelectedItems: function () {
        alert('TODO clone selection (=dataexplorer copy button)')
      },
      selectClipboardItems: function (mode) {
        // TODO render cut rows differently or show number as overlay on icon?
        const clipboard = {
          mode: mode,
          source: this.$route.params.package,
          packageIds: this.selectedPackageIds,
          entityTypeIds: this.selectedEntityTypeIds
        }
        this.$store.commit(SET_CLIPBOARD, clipboard)
      },
      canMoveItems: function () {
        return this.nrMovableItems > 0 && this.clipboard.source !== this.$route.params.package
      },
      moveClipboardItems: function () {
        this.$store.dispatch(MOVE_CLIPBOARD_ITEMS, this.$route.params.package)
        this.$store.commit(RESET_CLIPBOARD)
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

  .fa-enabled {
  }

  .fa-disabled {
    opacity: 0.6;
  }
</style>

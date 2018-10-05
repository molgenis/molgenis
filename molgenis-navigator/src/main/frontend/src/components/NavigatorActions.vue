<template>
  <div>
    <div class="row">
      <div class="col-10">
        <b-dropdown variant="primary" class="button-last">
          <template slot="button-content">
            <font-awesome-icon icon="plus" size="lg"/>
          </template>
          <b-dropdown-item v-b-modal.packageCreateModal>
            {{'action-create-package' | i18n }}
          </b-dropdown-item>
          <b-dropdown-item to="/menu/dataintegration/metadata-manager">
            {{'action-create-entity-type' | i18n }}
          </b-dropdown-item>
        </b-dropdown>
        <b-button variant="secondary" to="/menu/importdata/importwizard">
          <font-awesome-icon icon="upload" size="lg"/>
        </b-button>
        <b-button :disable="nrSelectedItems > 0 ? false : true" variant="secondary" class="button-last" @click="downloadSelectedItems">
          <font-awesome-icon icon="download" size="lg" :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
        </b-button>
        <b-button :disable="nrSelectedItems > 0 ? false : true" variant="secondary" @click="selectClipboardItems('cut')">
          <font-awesome-icon icon="cut" size="lg" :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
        </b-button>
        <b-button :disable="nrSelectedItems > 0 ? false : true" variant="secondary" @click="cloneSelectedItems">
          <font-awesome-icon icon="clone" size="lg" :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
        </b-button>
        <b-button :disable="canMoveItems() ? false : true" variant="secondary" class="button-last" @click="moveClipboardItems">
          <font-awesome-icon icon="paste" size="lg" :class="canMoveItems() ? 'fa-enabled' : 'fa-disabled'"/>
        </b-button>
      </div>
      <div class="col-2">
        <div class="float-right">
          <b-button v-if="nrSelectedPackages === 1" v-b-modal.packageUpdateModal variant="secondary" class="button-last">
            <font-awesome-icon icon="edit" size="lg"/>
          </b-button>
          <b-button v-else-if="nrSelectedEntityTypes === 1" :to="'/menu/dataintegration/metadata-manager/' + selectedEntityTypeIds[0]" variant="secondary" class="button-last">
            <font-awesome-icon icon="edit" size="lg"/>
          </b-button>
          <b-button v-else :disable="true" variant="secondary" class="button-last">
            <font-awesome-icon icon="edit" size="lg" class="fa-disabled"/>
          </b-button>
          <b-button :disable="nrSelectedItems > 0 ? false : true" variant="danger"
                    v-b-modal.itemDeleteModal>
            <font-awesome-icon icon="trash" size="lg"
                               :class="nrSelectedItems > 0 ? 'fa-enabled' : 'fa-disabled'"/>
          </b-button>
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
      ...mapState(['selectedEntityTypeIds', 'selectedPackageIds', 'clipboard']),
      nrSelectedEntityTypes () {
        return this.selectedEntityTypeIds.length
      },
      nrSelectedPackages () {
        return this.selectedPackageIds.length
      },
      nrSelectedItems () {
        return this.selectedEntityTypeIds.length + this.selectedPackageIds.length
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

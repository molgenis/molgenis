<template>
  <div>
    <div class="row">
      <div class="col-1">
        <b-dropdown variant="primary">
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
      </div>
      <div class="col-11">
        <div class="float-right">
          <b-button v-if="nrSelectedPackages === 1" v-b-modal.packageUpdateModal variant="secondary" class="button-delete">
            <font-awesome-icon icon="edit" size="lg"/>
          </b-button>
          <b-button v-else-if="nrSelectedEntityTypes === 1" @click="editSelectedItem" variant="secondary" class="button-delete">
            <font-awesome-icon icon="edit" size="lg"/>
          </b-button>
          <b-button v-else :disable="true" variant="secondary" class="button-delete">
            <font-awesome-icon icon="edit" size="lg" class="fa-disabled"/>
          </b-button>
          <b-button :disable="nrSelectedItems > 0 ? false : true" variant="danger"
                    v-b-modal.itemDeleteModal class="button-delete">
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
  import NavigatorModalPackageCreate from './NavigatorModalPackageCreate'
  import NavigatorModalPackageUpdate from './NavigatorModalPackageUpdate'
  import NavigatorModalItemDelete from './NavigatorModalItemDelete'

  export default {
    name: 'NavigatorActions',
    components: {NavigatorModalPackageCreate, NavigatorModalPackageUpdate, NavigatorModalItemDelete},
    computed: {
      ...mapState(['selectedEntityTypeIds', 'selectedPackageIds']),
      nrSelectedEntityTypes () {
        return this.selectedEntityTypeIds.length
      },
      nrSelectedPackages () {
        return this.selectedPackageIds.length
      },
      nrSelectedItems () {
        return this.selectedEntityTypeIds.length + this.selectedPackageIds.length
      }
    },
    methods: {
      editSelectedItem: function () {
        if (this.nrSelectedItems === 1) {
          if (this.selectedEntityTypeIds.length === 1) {
            this.$router.push(
              {path: `/menu/dataintegration/metadata-manager/` + this.selectedEntityTypeIds[0]})
          } else {
            console.log(this.$refs)
            this.$refs.packageUpdateModal.show()
          }
        }
      }
    }
  }
</script>

<style scoped>
  button[disable=true] {
    cursor: not-allowed !important;
  }

  .button-delete {
    margin-left: 1rem;
  }

  .fa-enabled {
  }

  .fa-disabled {
    opacity: 0.6;
  }
</style>

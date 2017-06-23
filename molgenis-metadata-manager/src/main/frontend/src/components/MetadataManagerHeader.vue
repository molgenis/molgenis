<template>
  <div>
    <div class="row">
      <div class="col-md-3 col-sm-12 col-xs-12">
        <h2>Metadata manager</h2>
      </div>
      <div class="col-md-4 col-sm-12 col-xs-12">
        <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                     selectLabel="" deselectLabel="" placeholder="Select an Entity..."></multiselect>
      </div>
      <div class="col-md-5 col-sm-12 col-xs-12">
        <div class="btn-group" role="group">
          <button @click="createNewEntityType" class="btn btn-primary">
            <i class="fa fa-plus-circle"></i>
            {{ 'create-entity-button' | i18n }}
          </button>

          <button @click="deleteEntityType(selectedEntityType.id)" class="btn btn-danger" v-if="selectedEntityType">
            <i class="fa fa-trash-o"></i>
            {{ 'delete-entity-button' | i18n }}
          </button>
          <button @click="deleteEntityType(selectedEntityType.id)" class="btn btn-danger" v-else disabled>
            <i class="fa fa-trash-o"></i>
            {{ 'delete-entity-button' | i18n }}
          </button>

          <save-button :onClick="saveEntityType">
            <i class="fa fa-save"></i>
            {{ 'save-changes-button' | i18n }}
          </save-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapState } from 'vuex'
  import { CREATE_ENTITY_TYPE, DELETE_ENTITY_TYPE, SAVE_EDITOR_ENTITY_TYPE } from '../store/actions'
  import { SET_SELECTED_ENTITY_TYPE } from '../store/mutations'
  import { getConfirmBeforeLeavingProperties, getConfirmBeforeDeletingProperties } from '../store/getters'

  import Multiselect from 'vue-multiselect'
  import SaveButton from './generic-components/SaveButton'

  export default {
    name: 'metadata-manager-header',
    methods: {
      createNewEntityType: function () {
        if (this.entityEdited) {
          this.$swal(getConfirmBeforeLeavingProperties()).then(() => {
            this.$store.dispatch(CREATE_ENTITY_TYPE)
          }).catch(this.$swal.noop)
        } else {
          this.$store.dispatch(CREATE_ENTITY_TYPE)
        }
      },
      deleteEntityType (selectedEntityTypeId) {
        this.$swal(getConfirmBeforeDeletingProperties(selectedEntityTypeId)).then(() => {
          this.$router.push({ path: '/' })
          this.$store.dispatch(DELETE_ENTITY_TYPE, selectedEntityTypeId)
        }).catch(this.$swal.noop)
      },
      saveEntityType () {
        this.$store.dispatch(SAVE_EDITOR_ENTITY_TYPE)
      }
    },
    computed: {
      ...mapState(['entityTypes']),
      selectedEntityType: {
        get () {
          return this.$store.state.selectedEntityType
        },
        set (value) {
          this.$store.commit(SET_SELECTED_ENTITY_TYPE, value)
          this.$router.push({ path: '/' + value.id })
        }
      }
    },
    components: {
      Multiselect,
      SaveButton
    }
  }
</script>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

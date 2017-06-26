<template>
  <div>
    <div class="row">
      <div class="col-md-4 col-sm-12 col-xs-12">
        <h2>Metadata manager</h2>
      </div>
      <div class="col-md-4 col-sm-12 col-xs-12">
        <div class="input-group">
          <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                       selectLabel="" deselectLabel="" placeholder="Select an Entity..."></multiselect>

          <span class="input-group-btn">
            <button @click="createNewEntityType" class="btn btn-primary"><i class="fa fa-plus"></i></button>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapState, mapGetters } from 'vuex'
  import { CREATE_ENTITY_TYPE } from '../store/actions'
  import { SET_SELECTED_ENTITY_TYPE_ID } from '../store/mutations'
  import { getConfirmBeforeLeavingProperties } from '../store/getters'

  import Multiselect from 'vue-multiselect'

  export default {
    name: 'metadata-manager-header',
    methods: {
      createNewEntityType () {
        if (this.isEntityTypeEdited) {
          this.$swal(getConfirmBeforeLeavingProperties()).then(() => {
            this.$store.dispatch(CREATE_ENTITY_TYPE)
          }).catch(this.$swal.noop)
        } else {
          this.$store.dispatch(CREATE_ENTITY_TYPE)
        }
      }
    },
    computed: {
      ...mapState(['entityTypes']),
      ...mapGetters({
        isEntityTypeEdited: 'getEditorEntityTypeHasBeenEdited'
      }),
      selectedEntityType: {
        get () {
          return this.$store.getters.getSelectedEntityType
        },
        set (selectedEntityType) {
          // Check if there are unsaved changes.
          // If yes, ask for confirmation before changing the selectedEntityType
          if (!this.isEntityTypeEdited) this.$store.commit(SET_SELECTED_ENTITY_TYPE_ID, selectedEntityType.id)
          else {
            this.$swal(getConfirmBeforeLeavingProperties()).then(() => {
              this.$store.commit(SET_SELECTED_ENTITY_TYPE_ID, selectedEntityType.id)
            }).catch(this.$swal.noop)
          }
        }
      }
    },
    components: {
      Multiselect
    }
  }
</script>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

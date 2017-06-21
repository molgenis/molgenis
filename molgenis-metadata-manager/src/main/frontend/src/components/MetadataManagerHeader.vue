<template>
  <div>
    <div class="row">
      <div class="col-md-4 col-sm-12 col-xs-12">
        <h2>Metadata manager</h2>
      </div>
      <div class="col-md-4 col-sm-12 col-xs-12">
        <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                     selectLabel="" deselectLabel="" placeholder="Select an Entity..."></multiselect>
      </div>
      <div class="col-md-4 col-sm-12 col-xs-12">
        <div class="btn-group" role="group">
          <button @click="createNewEntityType" class="btn btn-primary">New</button>

          <click-confirm placement="bottom" :messages="{title:'Do you really want to delete this entity?'}">
            <button @click="deleteEntityType" class="btn btn-danger" v-if="selectedEntityType"><i
              class="fa fa-trash-o"></i>
              Delete
            </button>
            <button @click="deleteEntityType" class="btn btn-danger" v-else disabled><i class="fa fa-trash-o"></i>
              Delete
            </button>
          </click-confirm>

          <b-button @click="saveEntityType" variant="success" class="float-right">Save everything!</b-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapState } from 'vuex'
  import { CREATE_ENTITY_TYPE, DELETE_ENTITY_TYPE, SAVE_EDITOR_ENTITY_TYPE } from '../store/actions'
  import { SET_SELECTED_ENTITY_TYPE } from '../store/mutations'

  import Multiselect from 'vue-multiselect'
  import ClickConfirm from 'click-confirm/src/ClickConfirm'

  export default {
    name: 'metadata-manager-header',
    methods: {
      createNewEntityType: function () {
        this.$store.dispatch(CREATE_ENTITY_TYPE)
      },
      deleteEntityType () {
        const selectedEntityTypeID = this.$store.state.selectedEntityType.id
        this.$store.dispatch(DELETE_ENTITY_TYPE)
          .then(response => {
            // After delete, route to path
            this.$router.push({ path: '/' })
            this.$notice('Successfully deleted ' + selectedEntityTypeID, { duration: 3000, style: 'success' })
          })
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
      ClickConfirm
    }
  }
</script>
<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

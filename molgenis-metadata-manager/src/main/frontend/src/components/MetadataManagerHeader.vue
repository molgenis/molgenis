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
        <div class="btn-group" role="group" aria-label="Basic example">
          <button @click="createNewEntity" class="btn btn-primary">New</button>

          <click-confirm placement="bottom" :messages="{title:'Do you really want to delete this entity?'}">
            <button @click="deleteEntity" class="btn btn-danger" v-if="selectedEntityType"><i class="fa fa-trash-o"></i> Delete</button>
            <button @click="deleteEntity" class="btn btn-danger" v-else disabled><i class="fa fa-trash-o"></i> Delete</button>
          </click-confirm>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapState } from 'vuex'
  import { CREATE_ENTITY_TYPE, DELETE_ENTITY_TYPE, GET_ENTITY_TYPES } from '../store/actions'
  import { SET_SELECTED_ENTITY_TYPE, SET_EDITOR_ENTITY_TYPE } from '../store/mutations'

  import Multiselect from 'vue-multiselect'
  import ClickConfirm from 'click-confirm/src/ClickConfirm'

  export default {
    name: 'metadata-manager-header',
    methods: {
      createNewEntity: function () {
        this.$store.dispatch(CREATE_ENTITY_TYPE)
      },
      deleteEntity () {
        this.$store.dispatch(DELETE_ENTITY_TYPE)
          .then(response => {
            this.$store.dispatch(GET_ENTITY_TYPES)
            this.$store.commit(SET_EDITOR_ENTITY_TYPE, null)
            this.$router.push({path: '/'})
          })
      }
    },
    computed: {
      ...mapState(['entityTypes']),
      selectedEntityType: {
        get () { return this.$store.state.selectedEntityType },
        set (value) {
          this.$store.commit(SET_SELECTED_ENTITY_TYPE, value)
          this.$router.push({path: '/' + value.id})
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

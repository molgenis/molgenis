<template>
  <div>
    <div class="row">
      <div class="col">
        <metadata-manager-header></metadata-manager-header>
        <hr>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <metadata-manager-entity-edit-form v-if="editorEntityType !== null"></metadata-manager-entity-edit-form>
        <hr>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <metadata-manager-attribute-edit-form v-if="editorEntityType !== null"></metadata-manager-attribute-edit-form>
      </div>
    </div>
  </div>
</template>

<script>
  import MetadataManagerHeader from './MetadataManagerHeader'
  import MetadataManagerEntityEditForm from './MetadataManagerEntityEditForm'
  import MetadataManagerAttributeEditForm from './MetadataManagerAttributeEditForm'

  import { CREATE_ALERT, SET_SELECTED_ENTITY_TYPE, SET_SELECTED_ATTRIBUTE_ID } from '../store/mutations'
  import { GET_ENTITY_TYPES, GET_PACKAGES, GET_ATTRIBUTE_TYPES, GET_EDITOR_ENTITY_TYPE } from '../store/actions'
  import { getConfirmBeforeLeavingProperties } from '../store/state'
  import { mapState, mapGetters } from 'vuex'

  export default {
    name: 'metadata-manager',
    computed: {
      ...mapState(['alert', 'editorEntityType']),
      ...mapGetters({
        entityEdited: 'getEditorEntityTypeHasBeenEdited'
      })
    },
    components: {
      MetadataManagerHeader,
      MetadataManagerEntityEditForm,
      MetadataManagerAttributeEditForm
    },
    watch: {
      '$route' (to, from) {
        // When switching attributes in the same entity do not trigger reloads
        const entityTypeID = to.params.entityTypeID
        if (entityTypeID && from.params.entityTypeID !== entityTypeID) {
          // Always clear alert on an entityType switch
          this.$store.commit(CREATE_ALERT, { type: null, message: null })

          const selectedEntityType = this.$store.state.entityTypes.find(entityType => entityType.id === entityTypeID)

          this.$store.commit(SET_SELECTED_ENTITY_TYPE, selectedEntityType)
          this.$store.commit(SET_SELECTED_ATTRIBUTE_ID, null)

          this.$store.dispatch(GET_EDITOR_ENTITY_TYPE, entityTypeID)
        }
      },
      alert (alert) {
        if (alert.message !== null) {
          this.$toaster.add(alert.message, { theme: 'v-toast-' + alert.type })
        }
      }
    },
    beforeRouteUpdate (to, from, next) {
      // Listens to route changes and prompts alert when editorEntityType has been edited
      if (this.entityEdited) {
        this.$swal(getConfirmBeforeLeavingProperties()).then(() => {
          next()
        }).catch(this.$swal.noop)
      }
    },
    created: function () {
      // Retrieve entities for dropdown
      this.$store.dispatch(GET_ENTITY_TYPES)

      // Retrieve packages for package select
      this.$store.dispatch(GET_PACKAGES)

      // Retrieve attribute types for Type selection
      this.$store.dispatch(GET_ATTRIBUTE_TYPES)
    }
  }
</script>

<style lang="scss">
  /*Before notie is imported:
  $notie-color-success: #57BF57;
  $notie-color-warning: #D6A14D;
  $notie-color-error: #E1715B;
  $notie-color-info: #4D82D6;
  $notie-color-neutral: #A0A0A0;

  See https://github.com/jaredreich/notie for more options

  */
  @import 'node_modules/notie/src/notie.scss';
</style>

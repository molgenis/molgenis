<template>
  <div>
    <b-card show-header show-footer>
      <div slot="header">
        <metadata-manager-header></metadata-manager-header>
      </div>

      <alert v-if="alert.message !== null" :onDismiss="onDismiss" :alert="alert"></alert>

      <metadata-manager-entity-edit-form v-if="editorEntityType !== null"></metadata-manager-entity-edit-form>

      <div slot="footer">
        <metadata-manager-attribute-edit-form v-if="editorEntityType !== null"></metadata-manager-attribute-edit-form>
      </div>
    </b-card>
  </div>
</template>

<script>
  import Alert from './generic-components/Alert'
  import MetadataManagerHeader from './MetadataManagerHeader'
  import MetadataManagerEntityEditForm from './MetadataManagerEntityEditForm'
  import MetadataManagerAttributeEditForm from './MetadataManagerAttributeEditForm'

  import { GET_ENTITY_TYPES, GET_PACKAGES, GET_ATTRIBUTE_TYPES, GET_EDITOR_ENTITY_TYPE } from '../store/actions'
  import { CREATE_ALERT, SET_SELECTED_ENTITY_TYPE, SET_SELECTED_ATTRIBUTE_ID } from '../store/mutations'
  import { mapState } from 'vuex'

  export default {
    name: 'metadata-manager',
    computed: {
      ...mapState(['alert', 'editorEntityType'])
    },
    methods: {
      onDismiss: function () {
        this.$store.commit(CREATE_ALERT, {type: null, message: null})
      }
    },
    components: {
      Alert,
      MetadataManagerHeader,
      MetadataManagerEntityEditForm,
      MetadataManagerAttributeEditForm
    },
    created: function () {
      // Retrieve entities for dropdown
      this.$store.dispatch(GET_ENTITY_TYPES)

      // Retrieve packages for package select
      this.$store.dispatch(GET_PACKAGES)

      // Retrieve attribute types for Type selection
      this.$store.dispatch(GET_ATTRIBUTE_TYPES)
    },
    watch: {
      '$route' (to, from) {
        // Always clear alert on an entityType or attribute switch
        this.$store.commit(CREATE_ALERT, {type: null, message: null})

        // When switching attributes in the same entity,
        // do not trigger reloads
        if (from.params.entityTypeID !== to.params.entityTypeID) {
          const entityTypeID = to.params.entityTypeID
          const selectedEntityType = this.$store.state.entityTypes.find(entityType => entityType.id === entityTypeID)

          this.$store.commit(SET_SELECTED_ENTITY_TYPE, selectedEntityType)
          this.$store.commit(SET_SELECTED_ATTRIBUTE_ID, null)

          if (entityTypeID !== undefined) {
            this.$store.dispatch(GET_EDITOR_ENTITY_TYPE, entityTypeID)
          }
        }
      }
    }
  }
</script>

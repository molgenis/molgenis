<template>
  <div>
    <div class="row">
      <div class="col">
        <metadata-manager-header></metadata-manager-header>
        <hr>
      </div>
    </div>
    <div v-if="loading" class="row">
      <div class="col">
        <div class="row">
          <spinner></spinner>
        </div>
      </div>
    </div>
    <div v-else>
      <div class="row">
        <div class="col">
          <div v-if="editorEntityType && editorEntityType.id !== ''">
            <metadata-manager-entity-edit-form></metadata-manager-entity-edit-form>
            <hr>
          </div>
          <h4 v-else class="text-muted text-center">{{ 'no-entity-type-selected-text' | i18n }}</h4>
        </div>
      </div>
      <div class="row">
        <div class="col">
          <metadata-manager-attribute-edit-form
            v-if="editorEntityType && editorEntityType.id !== ''"></metadata-manager-attribute-edit-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import MetadataManagerHeader from './MetadataManagerHeader'
  import MetadataManagerEntityEditForm from './MetadataManagerEntityEditForm'
  import MetadataManagerAttributeEditForm from './MetadataManagerAttributeEditForm'
  import Spinner from './generic-components/Spinner'

  import { SET_SELECTED_ENTITY_TYPE_ID, SET_SELECTED_ATTRIBUTE_ID } from '../store/mutations'
  import { GET_ENTITY_TYPES, GET_PACKAGES, GET_ATTRIBUTE_TYPES, GET_EDITOR_ENTITY_TYPE } from '../store/actions'
  import { mapState, mapGetters } from 'vuex'

  export default {
    name: 'metadata-manager',
    computed: {
      ...mapState(['alert', 'editorEntityType', 'loading']),
      ...mapGetters({
        selectedEntityType: 'getSelectedEntityType',
        selectedAttribute: 'getSelectedAttribute'
      })
    },
    watch: {
      alert (alert) {
        if (alert.message !== null) {
          this.$toaster.add(alert.message, { theme: 'v-toast-' + alert.type })
        }
      },
      selectedEntityType (selectedEntityType) {
        if (selectedEntityType) {
          if (!selectedEntityType.isNew) this.$router.push('/' + selectedEntityType.id)
        } else {
          this.$router.push('/')
        }
      },
      selectedAttribute (selectedAttribute) {
        if (selectedAttribute && this.$store.state.selectedEntityTypeId) {
          if (!selectedAttribute.isNew) this.$router.push('/' + this.$store.state.selectedEntityTypeId + '/' + selectedAttribute.id)
        }
      },
      '$route' (to, from) {
        const fromEntityTypeId = from.params.entityTypeId
        const toEntityTypeId = to.params.entityTypeId

        // The route change changed the EntityType we are looking at
        if (toEntityTypeId && fromEntityTypeId !== toEntityTypeId) {
          this.$store.commit(SET_SELECTED_ATTRIBUTE_ID, null)
          this.$store.dispatch(GET_EDITOR_ENTITY_TYPE, toEntityTypeId)
        }
      }
    },
    created: function () {
      const entityTypeId = this.$route.params.entityTypeId
      const attributeId = this.$route.params.attributeId

      if (entityTypeId) {
        this.$store.commit(SET_SELECTED_ENTITY_TYPE_ID, entityTypeId)
        this.$store.dispatch(GET_EDITOR_ENTITY_TYPE, entityTypeId)
      }
      if (attributeId) this.$store.commit(SET_SELECTED_ATTRIBUTE_ID, attributeId)

      this.$store.dispatch(GET_ENTITY_TYPES)
      this.$store.dispatch(GET_PACKAGES)
      this.$store.dispatch(GET_ATTRIBUTE_TYPES)
    },
    components: {
      MetadataManagerHeader,
      MetadataManagerEntityEditForm,
      MetadataManagerAttributeEditForm,
      Spinner
    }
  }
</script>

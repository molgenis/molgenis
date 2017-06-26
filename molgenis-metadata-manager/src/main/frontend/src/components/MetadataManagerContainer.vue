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

  import { SET_SELECTED_ENTITY_TYPE_ID, SET_SELECTED_ATTRIBUTE_ID } from '../store/mutations'
  import { GET_ENTITY_TYPES, GET_PACKAGES, GET_ATTRIBUTE_TYPES, GET_EDITOR_ENTITY_TYPE } from '../store/actions'
  import { mapState, mapGetters } from 'vuex'

  export default {
    name: 'metadata-manager',
    computed: {
      ...mapState(['alert', 'editorEntityType']),
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
        if (selectedEntityType) this.$router.push('/' + selectedEntityType.id)
      },
      selectedAttribute (selectedAttribute) {
        if (selectedAttribute) this.$router.push('/' + this.$store.state.selectedEntityTypeId + '/' + selectedAttribute.id)
      },
      '$route' (to, from) {
        const fromEntityTypeId = from.params.entityTypeId
        const toEntityTypeId = to.params.entityTypeId

        // The route change changed the EntityType we are looking at
        if (fromEntityTypeId !== toEntityTypeId) {
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
      MetadataManagerAttributeEditForm
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

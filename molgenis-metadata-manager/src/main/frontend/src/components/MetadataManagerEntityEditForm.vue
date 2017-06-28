<template>
  <div>
    <div class="row">
      <!-- Column containing  Entity ID, Extends, Extended by, Abstract-->
      <div class="col-md-4 col-sm-12 col-xs-12 inner-column">
        <div class="form-group row">
          <label class="col-4 col-form-label">Extends</label>
          <div class="col">
            <multiselect v-model="entityTypeParent" :options="abstractEntities" label="label"
                         selectLabel="" deselectLabel="" placeholder="Select an EntityType"></multiselect>
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label">Abstract</label>
          <div class="col checkbox-column">
            <input v-model="abstract0" class="form-control" type="checkbox">
          </div>
        </div>

        <div class="btn-toolbar float-right" role="toolbar" aria-label="Toolbar with button groups">
          <div class="btn-group mr-2" role="group">
            <save-button :onClick="saveEntityType" :disabled="!isEntityTypeEdited">
              {{ 'save-changes-button' | i18n }}
            </save-button>
          </div>
          <div class="btn-group" role="group">
            <button @click="deleteEntityType(editorEntityType.id)" class="btn btn-danger btn-sm left">
              Delete EntityType
            </button>
          </div>
        </div>
      </div>

      <!-- Column containing: Label, Description and Package -->
      <div class="col-md-4 col-sm-12 col-xs-12 inner-column">
        <div class="form-group row">
          <label class="col-4 col-form-label">Label</label>
          <div class="col">
            <input v-model="label" class="form-control" type="text" placeholder="add a label...">
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label">Description</label>
          <div class="col">
            <input v-model="description" class="form-control" type="text" placeholder="add a description...">
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label">Package</label>
          <div class="col">
            <multiselect v-model="package0" :options="packages" label="label"
                         selectLabel="" deselectLabel="" placeholder="Select a package"></multiselect>
          </div>
        </div>
      </div>

      <!-- Column containing ID attribute, Label attribute and LookupAttributes -->
      <div class="col-md-4 col-sm-12 col-xs-12 outer-column">
        <div class="form-group row">
          <label class="col-4 col-form-label">ID attribute</label>
          <div v-if="entityTypeParent === null || entityTypeParent === undefined" class="col">
            <multiselect v-model="idAttribute" :options="attributes" label="label"
                         selectLabel="" deselectLabel="" placeholder="Select an attribute"></multiselect>
          </div>

          <div v-else class="col">
            <multiselect v-model="idAttribute" :options="attributes" label="label"
                         selectLabel="" deselectLabel="" placeholder="Select an attribute" disabled></multiselect>
          </div>

        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label">Label attribute</label>
          <div class="col">
            <multiselect v-model="labelAttribute" :options="attributes" label="label"
                         selectLabel="" deselectLabel="" placeholder="Select an attribute"></multiselect>
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label">Lookup attributes</label>
          <div class="col">
            <multiselect v-model="lookupAttributes" :options="attributes" label="label"
                         selectLabel="" deselectLabel="" placeholder="Select attributes" multiple></multiselect>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
<style lang="scss">
  @import "~variables";
  @import "~mixins";

  .checkbox-column {
    padding-top: 11px
  }

  @include media-breakpoint-up(md) {
    .col-md-4.inner-column {
      border-right: solid black thin;
    }
  }

  .multiselect__tag {
    background-color: $brand-danger;
  }
</style>

<script>
  import { mapState, mapGetters } from 'vuex'
  import { UPDATE_EDITOR_ENTITY_TYPE } from '../store/mutations'
  import { SAVE_EDITOR_ENTITY_TYPE, DELETE_ENTITY_TYPE } from '../store/actions'
  import { getConfirmBeforeDeletingProperties } from '../store/getters'

  import Multiselect from 'vue-multiselect'
  import SaveButton from './generic-components/SaveButton'

  export default {
    name: 'metadata-manager-entity-edit-form',
    methods: {
      saveEntityType () {
        this.$store.dispatch(SAVE_EDITOR_ENTITY_TYPE)
      },
      deleteEntityType (selectedEntityTypeId) {
        this.$swal(getConfirmBeforeDeletingProperties(selectedEntityTypeId)).then(() => {
          this.$store.dispatch(DELETE_ENTITY_TYPE, selectedEntityTypeId)
        }).catch(this.$swal.noop)
      }
    },
    computed: {
      ...mapState(['editorEntityType', 'packages']),
      ...mapGetters({
        abstractEntities: 'getAbstractEntities',
        attributes: 'getEditorEntityTypeAttributes',
        isEntityTypeEdited: 'getEditorEntityTypeHasBeenEdited'
      }),
      entityTypeParent: {
        get () {
          return this.$store.state.editorEntityType.entityTypeParent
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'entityTypeParent', value: value })
        }
      },
      abstract0: {
        get () {
          return this.$store.state.editorEntityType.abstract0
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'abstract0', value: value })
        }
      },
      label: {
        get () {
          return this.$store.state.editorEntityType.label
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'label', value: value })
        }
      },
      description: {
        get () {
          return this.$store.state.editorEntityType.description
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'description', value: value })
        }
      },
      package0: {
        get () {
          return this.$store.state.editorEntityType.package0
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'package0', value: value })
        }
      },
      idAttribute: {
        get () {
          return this.$store.state.editorEntityType.idAttribute
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'idAttribute', value: value })
        }
      },
      labelAttribute: {
        get () {
          return this.$store.state.editorEntityType.labelAttribute
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'labelAttribute', value: value })
        }
      },
      lookupAttributes: {
        get () {
          return this.$store.state.editorEntityType.lookupAttributes
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'lookupAttributes', value: value })
        }
      }
    },
    components: {
      Multiselect,
      SaveButton
    }
  }
</script>

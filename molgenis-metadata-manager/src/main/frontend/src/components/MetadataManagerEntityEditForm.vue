<template>
  <div>
    <div class="row">
      <!-- Column containing  Entity ID, Extends, Extended by, Abstract-->
      <div class="col-md-4 col-sm-12 col-xs-12 inner-column">
        <div class="form-group row">
          <label class="col-4 col-form-label text-muted">{{ 'entity-edit-form-extends-label' | i18n }}</label>
          <div class="col">
            <multiselect v-model="entityTypeParent" :options="abstractEntities" label="label"
                         selectLabel="" deselectLabel=""
                         :placeholder="$t('entity-edit-form-extends-placeholder')"></multiselect>
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label text-muted">{{ 'entity-edit-form-abstract-label' | i18n }}</label>
          <div class="col checkbox-column">
            <input v-model="abstract0" class="form-control" type="checkbox">
          </div>
        </div>

        <div class="form-group-row float-right">
          <save-button :onClick="saveEntityType" :disabled="!isEntityTypeEdited">
            {{ 'save-changes-button' | i18n }}
          </save-button>
          <button @click="deleteEntityType(editorEntityType.id)" class="btn btn-danger"
                  :disabled="editorEntityType.isNew">
            {{ 'delete-entity-button' | i18n }}
          </button>
        </div>
      </div>

      <!-- Column containing: Label, Description and Package -->
      <div class="col-md-4 col-sm-12 col-xs-12 inner-column">
        <div class="form-group row">
          <label class="col-4 col-form-label text-muted">{{ 'entity-edit-form-label-label' | i18n }}</label>
          <div class="col">
            <input v-model="label" class="form-control" type="text"
                   :placeholder="$t('entity-edit-form-label-placeholder')">
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label text-muted">{{ 'entity-edit-form-description-label' | i18n }}</label>
          <div class="col">
            <input v-model="description" class="form-control" type="text"
                   :placeholder="$t('entity-edit-form-description-placeholder')">
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label text-muted">{{ 'entity-edit-form-package-label' | i18n }}</label>
          <div class="col">
            <multiselect v-model="package0" :options="packages" label="label"
                         selectLabel="" deselectLabel=""
                         :placeholder="$t('entity-edit-form-package-placeholder')"></multiselect>
          </div>
        </div>
      </div>

      <!-- Column containing ID attribute, Label attribute and LookupAttributes -->
      <div class="col-md-4 col-sm-12 col-xs-12 outer-column">
        <div class="form-group row">
          <label class="col-4 col-form-label text-muted">{{ 'entity-edit-form-id-attribute-label' | i18n }}</label>
          <div class="col">
            <multiselect v-model="idAttribute" :options="attributes" label="label"
                         selectLabel="" deselectLabel="" :placeholder="$t('entity-edit-form-id-attribute-placeholder')"
                         :disabled="entityTypeParent !== undefined"></multiselect>
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label text-muted">{{ 'entity-edit-form-label-attribute-label' | i18n }}</label>
          <div class="col">
            <multiselect v-model="labelAttribute" :options="attributes" label="label"
                         selectLabel="" deselectLabel=""
                         :placeholder="$t('entity-edit-form-label-attribute-placeholder')"></multiselect>
          </div>
        </div>

        <div class="form-group row">
          <label class="col-4 col-form-label text-muted">{{ 'entity-edit-form-lookup-attributes-label' | i18n }}</label>
          <div class="col">
            <multiselect
              v-model="lookupAttributes"
              :options="attributes"
              label="label"
              selectLabel=""
              deselectLabel=""
              track-by="id"
              :placeholder="$t('entity-edit-form-lookup-attributes-placeholder')" multiple>
            </multiselect>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
<style>

  .checkbox-column {
    padding-top: 11px
  }

  /*screen-md border on inner column when columns aligned next to each other*/
  @media (min-width: 768px) {
    .col-md-4.inner-column {
      border-right: solid black thin;
    }
  }

  .multiselect__tag, .multiselect__tag-icon:hover {
    background-color: #5bc0de; /*bootstrap brand-info*/
  }
</style>

<script>
  import { mapState, mapGetters } from 'vuex'
  import { UPDATE_EDITOR_ENTITY_TYPE, CREATE_ALERT } from '../store/mutations'
  import { SAVE_EDITOR_ENTITY_TYPE, DELETE_ENTITY_TYPE } from '../store/actions'
  import { getConfirmBeforeDeletingProperties } from '../store/getters'

  import Multiselect from 'vue-multiselect'
  import SaveButton from './generic-components/SaveButton'

  export default {
    name: 'metadata-manager-entity-edit-form',
    methods: {
      saveEntityType () {
        if (this.editorEntityType.idAttribute === null || this.editorEntityType.idAttribute === undefined) {
          this.$store.commit(CREATE_ALERT, {type: 'warning', message: 'ID attribute can not be empty'})
        } else if (this.editorEntityType.labelAttribute === null || this.editorEntityType.labelAttribute === undefined) {
          this.$store.commit(CREATE_ALERT, {type: 'warning', message: 'Label attribute can not be empty'})
        } else {
          this.$store.dispatch(SAVE_EDITOR_ENTITY_TYPE, this.$t)
        }
      },
      deleteEntityType (selectedEntityTypeId) {
        this.$swal(getConfirmBeforeDeletingProperties(selectedEntityTypeId, this.$t)).then(() => {
          this.$store.dispatch(DELETE_ENTITY_TYPE, selectedEntityTypeId)
        }).catch(this.$swal.noop)
      }
    },
    computed: {
      ...mapState(['editorEntityType', 'packages', 'selectedEntityTypeId']),
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
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'entityTypeParent', value: value})
        }
      },
      abstract0: {
        get () {
          return this.$store.state.editorEntityType.abstract0
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'abstract0', value: value})
        }
      },
      label: {
        get () {
          return this.$store.state.editorEntityType.label
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'label', value: value})
        }
      },
      description: {
        get () {
          return this.$store.state.editorEntityType.description
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'description', value: value})
        }
      },
      package0: {
        get () {
          return this.$store.state.editorEntityType.package0
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'package0', value: value})
        }
      },
      idAttribute: {
        get () {
          return this.$store.state.editorEntityType.idAttribute
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'idAttribute', value: value})
        }
      },
      labelAttribute: {
        get () {
          return this.$store.state.editorEntityType.labelAttribute
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'labelAttribute', value: value})
        }
      },
      lookupAttributes: {
        get () {
          return this.$store.state.editorEntityType.lookupAttributes
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'lookupAttributes', value: value})
        }
      }
    },
    components: {
      Multiselect,
      SaveButton
    }
  }
</script>

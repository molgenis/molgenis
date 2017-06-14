<template>
  <div class="row">
    <!-- Column containing  Entity ID, Extends, Extended by, Abstract-->
    <!-- TODO: delete button-->
    <div class="col-md-3 col-sm-12 col-xs-12 inner-column">
      <div class="form-group row">
        <label class="col-4 col-form-label">Entity</label>
        <div class="col">
          {{editorEntityType.id}}
        </div>
      </div>

      <div class="form-group row">
        <label class="col-4 col-form-label">Extends</label>
        <div class="col">
          <entity-select-box id="parent-entity-select" :value="editorEntityType.parent"
                             :options="abstractEntities"
                             :onChange="updateParentEntity"></entity-select-box>
        </div>
      </div>

      <div class="form-group row">
        <label class="col-4 col-form-label">Abstract</label>
        <div class="col checkbox-column">
            <input :checked="editorEntityType.abstract0" @click="updateIsAbstract" class="form-control" type="checkbox">
        </div>
      </div>
    </div>

    <!-- Column containing: Label, Description and Package -->
    <div class="col-md-4 col-sm-12 col-xs-12 inner-column">
      <div class="form-group row">
        <label for="editor-entity-type-label" class="col-4 col-form-label">Label</label>
        <div class="col">
          <input :value="editorEntityType.label" @input="updateLabel" class="form-control" type="text" id="editor-entity-type-label">
        </div>
      </div>

      <div class="form-group row">
        <label for="editor-entity-type-description" class="col-4 col-form-label">Description</label>
        <div class="col">
          <input :value="editorEntityType.description" @input="updateDescription" class="form-control" type="text"
                 id="editor-entity-type-description">
        </div>
      </div>

      <div class="form-group row">
        <label class="col-4 col-form-label">Package</label>
        <div class="col">
          <entity-select-box id="package-select" :value="editorEntityType.package0" :options="packages"
                             :onChange="updatePackage"></entity-select-box>
        </div>
      </div>
    </div>

    <!-- Column containing ID attribute, Label attribute and LookupAttributes -->
    <div class="col-md-4 col-sm-12 col-xs-12 outer-column">
      <div class="form-group row">
        <label class="col-4 col-form-label">ID attribute</label>
        <div class="col">
          <entity-select-box id="id-attribute-select" :value="editorEntityType.idAttribute"
                             :options="editorEntityType.attributes"
                             :onChange="updateIdAttribute"></entity-select-box>
        </div>
      </div>

      <div class="form-group row">
        <label class="col-4 col-form-label">Label attribute</label>
        <div class="col">
          <entity-select-box id="label-attribute-select" :value="editorEntityType.labelAttribute"
                             :options="editorEntityType.attributes"
                             :onChange="updateLabelAttribute"></entity-select-box>
        </div>
      </div>

      <div class="form-group row">
        <label class="col-4 col-form-label">Lookup attributes</label>
        <div class="col">
          <entity-select-box id="lookup-attribute-select" :value="editorEntityType.lookupAttributes"
                             :options="editorEntityType.attributes"
                             :onChange="updateLookupAttributes" multiple></entity-select-box>
        </div>
      </div>
    </div>

    <!-- Column containing Save button -->
    <div class="col-md-1 col-sm-12 col-xs-12">
      <b-button @click="save" variant="success" class="entity-save-btn">Save</b-button>
    </div>
  </div>
</template>

<style>
  .entity-save-btn {
    float: right
  }

  .checkbox-column {
    padding-top: 11px
  }

  /*screen-md border on inner column when columns aligned next to each other*/
  @media (min-width: 768px) {
    .col-md-3.inner-column,
    .col-md-4.inner-column{border-right: solid black thin;}
  }
</style>

<script>
  import { mapGetters } from 'vuex'
  import { SAVE_EDITOR_ENTITY_TYPE } from '../store/actions'
  import { UPDATE_EDITOR_ENTITY_TYPE } from '../store/mutations'

  import EntitySelectBox from './generic-components/EntitySelectBox'

  export default {
    name: 'metadata-manager-entity-edit-form',
    methods: {
      save: function () {
        this.$store.dispatch(SAVE_EDITOR_ENTITY_TYPE, this.editorEntityType)
      },
      updateParentEntity: function (value) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'parent', value: value})
      },
      updateIsAbstract: function (event) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'abstract0', value: event.target.checked})
      },
      updateLabel: function (event) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'label', value: event.target.value})
      },
      updateDescription: function (event) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'description', value: event.target.value})
      },
      updatePackage: function (value) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'package0', value: value})
      },
      updateIdAttribute: function (value) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'idAttribute', value: value})
      },
      updateLabelAttribute: function (value) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'labelAttribute', value: value})
      },
      updateLookupAttributes: function (value) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'lookupAttributes', value: value})
      }
    },
    computed: {
      ...mapGetters({
        packages: 'getPackages',
        abstractEntities: 'getAbstractEntities',
        editorEntityType: 'getEditorEntityType'
      })
    },
    components: {
      EntitySelectBox
    }
  }
</script>

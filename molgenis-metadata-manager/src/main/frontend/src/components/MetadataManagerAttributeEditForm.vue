<template>
  <div class="row">
    <div class="col-md-3 attribute-tree">
      <strong>Attributes</strong>
      <button @click="addAttribute" class="btn btn-primary btn-sm float-right">Add attribute</button>

      <hr>

      <attribute-tree :selectedAttribute="selectedAttribute" :attributes="attributeTree"
                      :onAttributeSelect="onAttributeSelect"></attribute-tree>

      <p v-if="editorEntityType.parent !== undefined">
        Parent attributes from <strong>{{editorEntityType.parent.label}}:</strong><br>
        <span v-for="attribute in editorEntityType.parent.attributes">{{attribute.label}}</span>
      </p>
    </div>

    <div class="col-md-9 attribute-edit-form" v-if="selectedAttribute !== undefined">
      <div class="row">
        <div class="col">
          <div class="form-group row">
            <label for="editor-entity-type-label" class="col-2 col-form-label">Attribute</label>
            <div class="col">
              {{selectedAttribute.label}}
            </div>
          </div>

          <div class="form-group row">
            <label for="editor-entity-type-label" class="col-4 col-form-label">Label</label>
            <div class="col">
              <input :value="selectedAttribute.label" @input="updateLabel" class="form-control" type="text"
                     id="editor-entity-type-label">
            </div>
          </div>

          <div class="form-group row">
            <label for="editor-entity-type-description" class="col-4 col-form-label">Description</label>
            <div class="col">
              <input :value="selectedAttribute.description" @input="updateDescription" class="form-control"
                     type="text"
                     id="editor-entity-type-description">
            </div>

            <!--<div class="form-group row">-->
            <!--<label class="col-4 col-form-label">Package</label>-->
            <!--<div class="col">-->
            <!--<entity-select-box id="package-select" :value="attribute.type" :options="attributeTypes"-->
            <!--:onChange="updatePackage"></entity-select-box>-->
            <!--</div>-->
            <!--</div>-->

          </div>
        </div>

        <div class="col">
          <!-- TODO nullable, auto, visible, unique, read-only, aggregatable -->
        </div>
      </div>
    </div>
  </div>
</template>

<style>
  /*screen-md border on inner column when columns aligned next to each other*/
  @media (min-width: 768px) {
    .col-md-3.attribute-tree {
      border-right: solid black thin;
    }
  }
</style>

<script>
  import AttributeTree from './generic-components/AttributeTree'
  import { SET_SELECTED_ATTRIBUTE_ID } from '../store/mutations'
  import { mapGetters } from 'vuex'

  export default {
    name: 'metadata-manager-attribute-edit-form',
    methods: {
      onAttributeSelect: function (selectedAttribute) {
        // On attribute select, update the path to consist of /<entityTypeID>/<attrributeID>
        const entityTypeID = this.$route.params.entityTypeID
        this.$router.push({path: '/' + entityTypeID + '/' + selectedAttribute.id})
        this.$store.commit(SET_SELECTED_ATTRIBUTE_ID, selectedAttribute.id)
      },
      addAttribute: function () {
        alert('Not yet implemented :)')
      },
      updateLabel: function (event) {
        this.selectedAttribute.label = event.target.value
        // this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'label', value: event.target.value})
      },
      updateDescription: function (event) {
        this.selectedAttribute.description = event.target.value
        // this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, {key: 'description', value: event.target.value})
      }
    },
    computed: {
      ...mapGetters({
        editorEntityType: 'getEditorEntityType',
        attributeTree: 'getAttributeTree',
        selectedAttribute: 'getSelectedAttribute',
        attributeTypes: 'getAttributeTypes'
      })
    },
    watch: {
      editorEntityType: function () {
        this.selectedAttribute = null
      }
    },
    components: {
      AttributeTree
    }
  }
</script>

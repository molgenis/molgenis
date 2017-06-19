<template>
  <div class="row">
    <!-- Attribute tree -->
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

    <!-- Attribute form inputs -->
    <div v-if="selectedAttribute" class="col-md-9">
      <div class="row">
        <div class="col">
          <strong>Attribute:</strong> {{selectedAttribute.label}}
          <hr>
        </div>
      </div>

      <div class="row">
        <div class="col-md-6">
          <div class="form-group row">
            <label class="col-3 col-form-label">Label</label>
            <div class="col">
              <input v-model="label" class="form-control" type="text">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-3 col-form-label">Description</label>
            <div class="col">
              <input v-model="description" class="form-control" type="text">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-3 col-form-label">Type</label>
            <div class="col">
              <multiselect v-model="type" :options="attributeTypes"
                           selectLabel="" deselectLabel="" placeholder="Select a type"></multiselect>
            </div>
          </div>

          <div v-if="isReferenceType" class="form-group row">
            <label class="col-3 col-form-label">Type</label>
            <div class="col">
              <multiselect v-model="refEntityType" :options="entityTypes" label="label"
                           selectLabel="" deselectLabel="" placeholder="Select a reference entity"></multiselect>
            </div>
          </div>
        </div>

        <div class="col-md-3">
          <div class="form-group row">
            <label class="col-6 col-form-label">Nullable</label>
            <div class="col checkbox-column">
              <input v-model="nullable" class="form-control" type="checkbox">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-6 col-form-label">Auto</label>
            <div class="col checkbox-column">
              <input v-model="auto" class="form-control" type="checkbox">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-6 col-form-label">Visible</label>
            <div class="col checkbox-column">
              <input v-model="visible" class="form-control" type="checkbox">
            </div>
          </div>
        </div>

        <div class="col-md-3">
          <div class="form-group row">
            <label class="col-6 col-form-label">Unique</label>
            <div class="col checkbox-column">
              <input v-model="unique" class="form-control" type="checkbox">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-6 col-form-label">Read-only</label>
            <div class="col checkbox-column">
              <input v-model="readonly" class="form-control" type="checkbox">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-6 col-form-label">Aggregatable</label>
            <div class="col checkbox-column">
              <input v-model="aggregatable" class="form-control" type="checkbox">
            </div>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col">
          <div class="form-group">
            <label>Computed value expression</label>
            <textarea v-model="expression" class="form-control" rows="3"></textarea>
          </div>
        </div>
        <div class="col">
          <div class="form-group">
            <label>Visible expression</label>
            <textarea v-model="visibleExpression" class="form-control" rows="3"></textarea>
          </div>
        </div>
        <div class="col">
          <div class="form-group">
            <label>Validation expression</label>
            <textarea v-model="validationExpression" class="form-control" rows="3"></textarea>
          </div>
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
  import { mapState, mapGetters } from 'vuex'
  import { SET_SELECTED_ATTRIBUTE_ID, UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE } from '../store/mutations'
  import { CREATE_ATTRIBUTE } from '../store/actions'

  import Multiselect from 'vue-multiselect'

  export default {
    name: 'metadata-manager-attribute-edit-form',
    methods: {
      onAttributeSelect (value) {
        this.$store.commit(SET_SELECTED_ATTRIBUTE_ID, value.id)
        this.$router.push({path: '/' + this.$route.params.entityTypeID + '/' + value.id})
      },
      addAttribute () { this.$store.dispatch(CREATE_ATTRIBUTE) }
    },
    computed: {
      ...mapState(['editorEntityType', 'attributeTypes', 'entityTypes']),
      ...mapGetters({
        selectedAttribute: 'getSelectedAttribute',
        editorEntityTypeAttributes: 'getEditorEntityTypeAttributes',
        attributeTree: 'getAttributeTree'
      }),
      isReferenceType: function () {
        return this.selectedAttribute.type === 'XREF' ||
          this.selectedAttribute.type === 'MREF' ||
          this.selectedAttribute.type === 'CATEGORICAL' ||
          this.selectedAttribute.type === 'CATEGORICAL_MREF'
      },
      editorEntityTypeAttributes: {
        get () { return this.$store.editorEntityType.attributes }
      },
      label: {
        get () { return this.selectedAttribute.label },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'label', value: value}) }
      },
      description: {
        get () { return this.selectedAttribute.description },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'description', value: value}) }
      },
      type: {
        get () { return this.selectedAttribute.type },
        // FIXME when type is set, all other input fields become empty...
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'type', value: value}) }
      },
      refEntityType: {
        get () { return this.selectedAttribute.refEntityType },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'refEntityType', value: value}) }
      },
      nullable: {
        get () { return this.selectedAttribute.nullable },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'nullable', value: value}) }
      },
      auto: {
        get () { return this.selectedAttribute.auto },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'auto', value: value}) }
      },
      visible: {
        get () { return this.selectedAttribute.visible },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'visible', value: value}) }
      },
      unique: {
        get () { return this.selectedAttribute.unique },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'unique', value: value}) }
      },
      readonly: {
        get () { return this.selectedAttribute.readonly },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'readonly', value: value}) }
      },
      aggregatable: {
        get () { return this.selectedAttribute.aggregatable },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'aggregatable', value: value}) }
      },
      expression: {
        get () { return this.selectedAttribute.expression },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'expression', value: value}) }
      },
      visibleExpression: {
        get () { return this.selectedAttribute.visibleExpression },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'visibleExpression', value: value}) }
      },
      validationExpression: {
        get () { return this.selectedAttribute.validationExpression },
        set (value) { this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, {key: 'validationExpression', value: value}) }
      }
    },
    components: {
      AttributeTree,
      Multiselect
    }
  }
</script>

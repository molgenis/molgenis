<template>
  <div class="row">
    <!-- Attribute tree -->
    <div class="col-md-3 attribute-tree">
      <div class="row">
        <div class="col">
          <strong>Attributes</strong>
          <button @click="addAttribute" class="btn btn-primary btn-sm float-right"><i class="fa fa-plus"></i></button>
        </div>
      </div>

      <hr>

      <div class="row">
        <div class="col">
          <div class="btn-toolbar float-right" role="toolbar" aria-label="Toolbar with button groups">
            <div class="btn-group mr-2" role="group">
              <button @click="moveAttribute('up')" class="btn btn-secondary btn-sm"
                      :disabled="!selectedAttribute || selectedAttributeIndex === 0">

                <i class="fa fa-chevron-up"></i>
              </button>
              <button @click="moveAttribute('down')" class="btn btn-secondary btn-sm"
                      :disabled="!selectedAttribute || selectedAttributeIndex === editorEntityType.attributes.length - 1">

                <i class="fa fa-chevron-down"></i>
              </button>
            </div>
            <div class="btn-group" role="group">
              <button @click="deleteAttribute(selectedAttribute)" class="btn btn-danger float-right btn-sm"
                      :disabled="!selectedAttribute">

                <i class="fa fa-trash-o"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      <attribute-tree :selectedAttribute="selectedAttribute" :attributeTree="attributeTree"
                      :onAttributeSelect="onAttributeSelect"></attribute-tree>

      <p v-if="editorEntityType.entityTypeParent !== undefined">
        Parent attributes from <strong>{{editorEntityType.entityTypeParent.label}}:</strong><br>
        <span v-for="attribute in editorEntityType.entityTypeParent.attributes">- {{attribute.label}} <br></span>
      </p>
    </div>

    <!-- Attribute form inputs -->
    <div v-if="!selectedAttribute" class="col-md-9">
      <div class="row">
        <div class="col">
          <h4 class="text-muted text-center">{{ 'no-attribute-selected-text' | i18n }}</h4>
        </div>
      </div>
    </div>
    <div v-else class="col-md-9">
      <div class="row">
        <div class="col attribute-form-header">
          <strong>Attribute:</strong> {{selectedAttribute.label}}
          <hr>
        </div>
      </div>

      <div class="row">
        <div class="col-md-6">
          <div class="form-group row">
            <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-name-label' | i18n }}</label>
            <div class="col">
              <input v-model="name" class="form-control" type="text" :placeholder="$t('attribute-edit-form-name-placeholder')">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-label-label' | i18n }}</label>
            <div class="col">
              <input v-model="label" class="form-control" type="text" :placeholder="$t('attribute-edit-form-label-placeholder')">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-description-label' | i18n }}</label>
            <div class="col">
              <input v-model="description" class="form-control" type="text" :placeholder="$t('attribute-edit-form-description-placeholder')">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-type-label' | i18n }}</label>
            <div class="col">
              <multiselect v-model="type" :options="attributeTypes"
                           selectLabel="" deselectLabel="" :placeholder="$t('attribute-edit-form-type-placeholder')"></multiselect>
            </div>
          </div>

          <div class="form-group row">
            <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-parent-label' | i18n }}</label>
            <div class="col">
              <multiselect v-model="parent" :options="compoundAttributes" label="label"
                           selectLabel="" deselectLabel="" :placeholder="$t('attribute-edit-form-parent-placeholder')"></multiselect>
            </div>
          </div>

          <div v-if="isReferenceType" class="form-group row">
            <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-reference-entity-label' | i18n }}</label>
            <div class="col">
              <multiselect v-model="refEntityType" :options="entityTypes" label="label"
                           selectLabel="" deselectLabel="" :placeholder="$t('attribute-edit-form-reference-entity-placeholder')"></multiselect>
            </div>
          </div>

          <div v-else-if="isNumericType">
            <div class="form-group row">
              <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-minimum-range-label' | i18n }}</label>
              <div class="col">
                <input v-model.number="rangeMin" class="form-control" type="number" :placeholder="$t('attribute-edit-form-minimum-range-placeholder')">
              </div>
            </div>

            <div class="form-group row">
              <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-maximum-range-label' | i18n }}</label>
              <div class="col">
                <input v-model.number="rangeMax" class="form-control" type="number" :placeholder="$t('attribute-edit-form-maximum-range-placeholder')">
              </div>
            </div>
          </div>

          <div v-else-if="isEnumType" class="form-group row">
            <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-enum-options-label' | i18n }}</label>
            <div class="col">
              <input v-model.lazy="enumOptions" class="form-control" type="text" :placeholder="$t('attribute-edit-form-enum-options-placeholder')">
            </div>
          </div>

          <div v-else-if="isOneToManyType">
            <div class="form-group row">
              <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-mapped-by-label' | i18n }}</label>
              <div class="col">
                <multiselect v-model="mappedByEntityType" :options="entityTypes" label="label"
                             selectLabel="" deselectLabel="" :placeholder="$t('attribute-edit-form-mapped-by-placeholder')"></multiselect>
              </div>
            </div>

            <div class="form-group row">
              <label class="col-3 col-form-label text-muted">{{ 'attribute-edit-form-order-by-label' | i18n }}</label>
              <div class="col">
                <input v-model="orderBy" class="form-control" type="text" :placeholder="$t('attribute-edit-form-order-by-placeholder')">
              </div>
            </div>
          </div>
        </div>

        <div class="col-md-3">
          <div class="form-group row">
            <label class="col-6 col-form-label text-muted">{{ 'attribute-edit-form-nullable-label' | i18n }}</label>
            <div class="col checkbox-column">
              <input v-model="nullable" class="form-control" type="checkbox">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-6 col-form-label text-muted">{{ 'attribute-edit-form-auto-label' | i18n }}</label>
            <div class="col checkbox-column">
              <input v-model="auto" class="form-control" type="checkbox">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-6 col-form-label text-muted">{{ 'attribute-edit-form-visible-label' | i18n }}</label>
            <div class="col checkbox-column">
              <input v-model="visible" class="form-control" type="checkbox">
            </div>
          </div>
        </div>

        <div class="col-md-3">
          <div class="form-group row">
            <label class="col-6 col-form-label text-muted">{{ 'attribute-edit-form-unique-label' | i18n }}</label>
            <div class="col checkbox-column">
              <input v-model="unique" class="form-control" type="checkbox">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-6 col-form-label text-muted">{{ 'attribute-edit-form-readonly-label' | i18n }}</label>
            <div class="col checkbox-column">
              <input v-model="readonly" class="form-control" type="checkbox">
            </div>
          </div>

          <div class="form-group row">
            <label class="col-6 col-form-label text-muted">{{ 'attribute-edit-form-aggregatable-label' | i18n }}</label>
            <div class="col checkbox-column">
              <input v-model="aggregatable" class="form-control" type="checkbox">
            </div>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="col">
          <div class="form-group">
            <label class="text-muted">{{ 'attribute-edit-form-computed-expression-label' | i18n }}</label>
            <textarea v-model="expression" class="form-control" rows="3" :placeholder="$t('attribute-edit-form-computed-expression-placeholder')"></textarea>
          </div>
        </div>
        <div class="col">
          <div class="form-group">
            <label class="text-muted">{{ 'attribute-edit-form-visible-expression-label' | i18n }}</label>
            <textarea v-model="visibleExpression" class="form-control" rows="3" :placeholder="$t('attribute-edit-form-visible-expression-placeholder')"></textarea>
          </div>
        </div>
        <div class="col">
          <div class="form-group">
            <label class="text-muted">{{ 'attribute-edit-form-validation-expression-label' | i18n }}</label>
            <textarea v-model="validationExpression" class="form-control" rows="3" :placeholder="$t('attribute-edit-form-validation-expression-placeholder')"></textarea>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";

  @include media-breakpoint-up(md) {
    .col-md-3.attribute-tree {
      border-right: solid $black thin;
    }
  }
</style>

<script>
  import AttributeTree from './generic-components/AttributeTree'
  import { mapState, mapGetters } from 'vuex'
  import {
    SET_SELECTED_ATTRIBUTE_ID,
    UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE,
    DELETE_SELECTED_ATTRIBUTE,
    UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER
  } from '../store/mutations'

  import { CREATE_ATTRIBUTE } from '../store/actions'
  import { getConfirmBeforeDeletingProperties } from '../store/getters'

  import Multiselect from 'vue-multiselect'

  export default {
    name: 'metadata-manager-attribute-edit-form',
    methods: {
      deleteAttribute (selectedAttribute) {
        this.$swal(getConfirmBeforeDeletingProperties(selectedAttribute.label)).then(() => {
          this.$store.commit(DELETE_SELECTED_ATTRIBUTE, selectedAttribute.id)
        }).catch(this.$swal.noop)
      },
      onAttributeSelect (selectedAttribute) {
        this.$store.commit(SET_SELECTED_ATTRIBUTE_ID, selectedAttribute.id)
      },
      addAttribute () {
        this.$store.dispatch(CREATE_ATTRIBUTE)
      },
      moveAttribute (moveOrder) {
        this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER, {
          moveOrder: moveOrder,
          selectedAttributeIndex: this.selectedAttributeIndex
        })
      }
    },
    computed: {
      ...mapState(['editorEntityType', 'attributeTypes', 'entityTypes']),
      ...mapGetters({
        selectedAttributeIndex: 'getIndexOfSelectedAttribute',
        selectedAttribute: 'getSelectedAttribute',
        editorEntityTypeAttributes: 'getEditorEntityTypeAttributes',
        attributeTree: 'getAttributeTree',
        compoundAttributes: 'getCompoundAttributes'
      }),
      isReferenceType: function () {
        return ['XREF', 'MREF', 'CATEGORICAL', 'CATEGORICAL_MREF'].includes(this.selectedAttribute.type)
      },
      isNumericType: function () {
        return ['INT', 'LONG'].includes(this.selectedAttribute.type)
      },
      isEnumType: function () {
        return this.selectedAttribute.type === 'ENUM'
      },
      isOneToManyType: function () {
        return this.selectedAttribute.type === 'ONETOMANY'
      },
      name: {
        get () {
          return this.selectedAttribute.name
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'name', value: value })
        }
      },
      label: {
        get () {
          return this.selectedAttribute.label
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'label', value: value })
        }
      },
      description: {
        get () {
          return this.selectedAttribute.description
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'description', value: value })
        }
      },
      parent: {
        get () {
          return this.selectedAttribute.parent
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'parent', value: value })
        }
      },
      type: {
        get () {
          return this.selectedAttribute.type
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'type', value: value })
        }
      },
      refEntityType: {
        get () {
          return this.selectedAttribute.refEntityType
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'refEntityType', value: value })
        }
      },
      nullable: {
        get () {
          return this.selectedAttribute.nullable
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'nullable', value: value })
        }
      },
      auto: {
        get () {
          return this.selectedAttribute.auto
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'auto', value: value })
        }
      },
      visible: {
        get () {
          return this.selectedAttribute.visible
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'visible', value: value })
        }
      },
      unique: {
        get () {
          return this.selectedAttribute.unique
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'unique', value: value })
        }
      },
      readonly: {
        get () {
          return this.selectedAttribute.readonly
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'readonly', value: value })
        }
      },
      aggregatable: {
        get () {
          return this.selectedAttribute.aggregatable
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'aggregatable', value: value })
        }
      },
      expression: {
        get () {
          return this.selectedAttribute.expression
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'expression', value: value })
        }
      },
      visibleExpression: {
        get () {
          return this.selectedAttribute.visibleExpression
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'visibleExpression', value: value })
        }
      },
      validationExpression: {
        get () {
          return this.selectedAttribute.validationExpression
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'validationExpression', value: value })
        }
      },
      enumOptions: {
        get () {
          return this.selectedAttribute.enumOptions.join(',')
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'enumOptions', value: value.split(',') })
        }
      },
      mappedByEntityType: {
        get () {
          return this.selectedAttribute.mappedByEntityType
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'mappedByEntityType', value: value })
        }
      },
      orderBy: {
        get () {
          return this.selectedAttribute.orderBy
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'orderBy', value: value })
        }
      },
      rangeMin: {
        get () {
          return this.selectedAttribute.rangeMin
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'rangeMin', value: value })
        }
      },
      rangeMax: {
        get () {
          return this.selectedAttribute.rangeMax
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE, { key: 'rangeMax', value: value })
        }
      }
    },
    components: {
      AttributeTree,
      Multiselect
    }
  }
</script>

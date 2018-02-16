<template>
  <div :class="'pl-' + level">
    <template v-for="attribute in attributes">

      <template v-if="attribute.fieldType === 'COMPOUND'">
        <h4>{{ attribute.label }}</h4>
        <hr>
        <questionnaire-overview-entry
          :attributes="attribute.attributes"
          :data="data"
          :level="level + 2">
        </questionnaire-overview-entry>
      </template>

      <template v-else>
        <dl class="row" v-if="hasValue(attribute)">
          <dt class="col-sm-3">{{ attribute.label }}</dt>
          <dd class="col-sm-9">{{ getReadableValue(attribute) }}</dd>
        </dl>
      </template>

    </template>
  </div>
</template>

<script>
  export default {
    name: 'QuestionnaireOverviewEntry',
    props: {
      attributes: {
        type: Array,
        required: true
      },
      data: {
        type: Object,
        required: true
      },
      level: {
        type: Number,
        required: false,
        default: 1
      }
    },
    methods: {
      hasValue (attribute) {
        const value = this.data[attribute.name]
        return !((Array.isArray(value) && value.length === 0) || value === undefined)
      },
      getReadableValue (attribute) {
        const value = this.data[attribute.name]

        if (value) {
          switch (attribute.fieldType) {
            case 'MREF':
            case 'CATEGORICAL_MREF':
              return value.map(v => v[attribute.refEntity.labelAttribute]).join(', ')
            case 'BOOL':
              return value ? this.$t('questionnaire_bool_true') : this.$t('questionnaire_bool_false')
            case 'ENUM':
              return value.join(', ')
            case 'XREF':
            case 'CATEGORICAL':
              return value[attribute.refEntity.labelAttribute]
            default:
              return value
          }
        } else {
          return ''
        }
      }
    }
  }
</script>

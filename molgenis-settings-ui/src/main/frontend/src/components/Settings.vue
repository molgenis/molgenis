<template>
  <div>
    <h1>{{ 'plugin-title' | i18n }}</h1>
    <form-component id="settings-form" :schema="schema"></form-component>
  </div>
</template>

<script>
  import { FormComponent } from '@molgenis/molgenis-ui-form'
  import { GET_SETTINGS } from '../store/actions'

  export default {
    name: 'Settings',
    components: {
      FormComponent
    },
    created: function () {
      this.$store.dispatch(GET_SETTINGS)
    },
    computed: {
      schema () {
        return {
          fields: this.$store.state.formFields
        }
      }
    }
  }
</script>

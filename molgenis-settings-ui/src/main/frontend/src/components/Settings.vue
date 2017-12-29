<template>
    <div>
        <h1>{{ 'plugin-title' | i18n }}</h1>
        <form-component id="settings-form" :schema="schema"></form-component>
    </div>
</template>

<script>
  import { FormComponent } from '@molgenis/molgenis-ui-form'
  import { GET_SETTINGS } from '../store/actions'
  import { mapState, mapGetters } from 'vuex'

  export default {
    name: 'Settings',
    components: {
      FormComponent
    },
    created: function () {
      this.$store.dispatch(GET_SETTINGS)
    },
    computed: {
      ...mapState(['rawSettings']),
      ...mapGetters(['getMappedFields']),
      schema () {
        return {
          fields: this.getMappedFields
        }
      }
    }
  }
</script>

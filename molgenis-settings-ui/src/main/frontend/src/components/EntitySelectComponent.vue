<template>
  <div class="container">
    <!--
    /**
      For creating options that do not exist:
        - taggable = true
        - pushTags = true
        - createOption = Function
    */
    -->
    <v-select v-model="selectedSetting"
              :options="settings"
              :filterable="true">
    </v-select>


  </div>
</template>

<script>
  import vSelect from 'vue-select'
  import { GET_SETTINGS_BY_ID } from '../store/actions'

  export default {
    name: 'EntitySelectComponent',
    computed: {
      settings () {
        return this.$store.state.settings
      },
      selectedSetting: {
        get () {
          return this.$store.state.selectedSetting
        },
        set (selectedSetting) {
          this.$store.dispatch(GET_SETTINGS_BY_ID, selectedSetting.id)
        }
      }
    },
    components: {
      vSelect
    }
  }
</script>

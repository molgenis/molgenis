<template>
  <div>
    <multiselect :id="id"
                 :value="value"
                 :options="options"
                 :searchable="true"
                 :close-on-select="true"
                 :allow-empty="true"
                 @input="onChange"
                 label="label"
                 :multiple="multiple"
                 selectLabel=""
                 deselectLabel=""
                 placeholder="Select an entity"
                 track-by="id"></multiselect>
  </div>
</template>
<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
<script>
  import Multiselect from 'vue-multiselect'

  export default {
    name: 'entity-select-box',
    data: function () {
      return {
        selectedValue: this.value
      }
    },
    props: {
      id: {
        type: String,
        required: false,
        default: 'entity-select-box'
      },
      /**
       * Preselected option(s)
       */
      value: {
        type: [Object, Array, String, Number],
        required: false
      },
      /**
       * List of options
       */
      options: {
        type: Array,
        required: true
      },
      onChange: {
        type: Function,
        required: true
      },
      multiple: {
        type: Boolean,
        required: false,
        default: false
      }
    },
    components: {
      Multiselect
    }
  }
</script>

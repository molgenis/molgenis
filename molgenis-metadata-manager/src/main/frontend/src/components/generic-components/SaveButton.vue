<template>
  <button @click="onClick" type="button" :class="[classObject]" :disabled="disabled">
    <slot>Save</slot>
  </button>
</template>

<style scoped src="bootstrap/dist/css/bootstrap.css" />

<script>
  /**
   * MOLGENIS save button component
   * Renders a button with Bootstrap-4 styling
   *
   * Default content: 'Save'
   *
   * Invoke <molgenis-save-button>'Your text here'</molgenis-save-button> to override default content
   *
   * Accepted properties: type, size, outline, disabled, onClick
   */
  export default {
    name: 'save-button',
    props: {
      type: {
        type: String,
        required: false,
        default: 'success',
        validator (type) {
          const availableTypes = ['primary', 'secondary', 'info', 'success', 'warning', 'danger', 'link']
          return availableTypes.indexOf(type) > 0
        }
      },
      size: {
        type: String,
        required: false,
        default: null,
        validator (size) {
          const availableSizes = ['sm', 'lg']
          return availableSizes.indexOf(size) > 0
        }
      },
      outline: {
        type: Boolean,
        required: false,
        default: false
      },
      disabled: {
        type: Boolean,
        required: false,
        default: false
      },
      onClick: {
        type: Function,
        required: false
      }
    },
    computed: {
      classObject () {
        const type = this.outline ? 'btn btn-outline-' + this.type : 'btn btn-' + this.type
        const size = this.size ? 'btn-' + this.size : ''
        return type + ' ' + size
      }
    }
  }
</script>

<template>
    <div class="create-new-app-form-container">
        {{ app }}
        <button class="btn btn-dark float-right" @click="save">Save changes</button>
    </div>
</template>

<script>
  export default {
    name: 'EditExistingAppComponent',
    props: ['selectedApp'],
    data () {
      return {
        app: this.selectedApp
      }
    },
    methods: {
      save () {
        this.$emit('save', this.app)
      }
    }
  }
</script>
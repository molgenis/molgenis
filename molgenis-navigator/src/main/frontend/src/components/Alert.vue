<template>
  <div v-if="error != undefined" class="alert alert-danger" role="alert">
    <button @click="error=null" type="button" class="close"><span
      aria-hidden="true">&times;</span></button>
    {{error}}
  </div>
</template>

<script>
  import { SET_ERROR } from '../store/mutations'

  export default {
    name: 'Alert',
    computed: {
      error: {
        get () {
          return this.$store.state.error
        },
        set (error) {
          this.$store.commit(SET_ERROR, error)
        }
      }
    }
  }
</script>

<style scoped>

</style>

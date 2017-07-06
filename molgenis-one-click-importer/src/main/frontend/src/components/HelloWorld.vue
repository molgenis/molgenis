<template>
  <div class="row">
    <div class="col text-center">
      <h1>This is the MOLGENIS HelloWorld Component</h1>
      <p>message: {{ message }}</p>
      <button @click="showSweetAlert" class="btn btn-secondary btn-lg">Click me for a sweet alert</button>
    </div>
  </div>
</template>

<script>
  import { mapState } from 'vuex'

  export default {
    name: 'hello-world',
    methods: {
      showSweetAlert () {
        // if you want to learn more about sweetalert2's api,
        // please go to its homepage https://github.com/limonte/sweetalert2
        this.$swal('Hello sweet world!')
      }
    },
    computed: {
      ...mapState(['message'])
    }
  }
</script>

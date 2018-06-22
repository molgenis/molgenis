<template>

  <div class="row">
    <div class="col-md-12">
      <div id="alert-message fade" v-if="toast" :class="'alert alert-' + toast.type" role="alert">
        <button @click="clearToast()" type="button" class="close">
          <span aria-hidden="true">&times;</span>
        </button>
        <span id="message-span">{{toast.message}}</span>
      </div>
    </div>
  </div>

</template>

<script>
  import { mapGetters, mapMutations } from 'vuex'

  export default {
    name: 'ToastMessage',
    computed: {
      ...mapGetters([
        'toast'
      ])
    },
    methods: {
      ...mapMutations([
        'clearToast'
      ])
    }
  }
</script>


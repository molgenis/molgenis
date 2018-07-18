<template>
  <div class="row">
    <div class="col-md-12">
      <transition name="slide">
        <div id="alert-message fade" v-if="toast" :class="'alert alert-' + toast.type" role="alert">
          <button @click="clearToast()" type="button" class="close">
            <span aria-hidden="true">&times;</span>
          </button>
          <span id="message-span">{{toast.message}}</span>
        </div>
      </transition>
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

<style>
  .slide-enter-active, .slide-leave-active {
    transition: transform 500ms ease-in-out;
  }

  .slide-enter, .slide-leave-to {
    transform: translateY(-100%);
  }

  .slide-enter-to, .slide-leave {
    transform: translateY(0);
  }
</style>


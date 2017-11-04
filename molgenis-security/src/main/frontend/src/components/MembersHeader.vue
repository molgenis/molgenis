<template>
  <div class="row mb-2">
    <div class="col">
      <h2>Group members & roles</h2>
      <div class="alert" :class="{'alert-danger':alert.type === 'error'}" role="alert" v-for="(alert, index) in alerts">
        <button type="button" class="close" @click="dismiss(index)" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
        <strong>{{alert.type}}!</strong>{{alert.message}}
      </div>
    </div>
  </div>
</template>

<script>
  import { mapState, mapMutations } from 'vuex'
  import { DISMISS_ALERT } from '../store/mutations'
  export default {
    computed: {
      ...mapState(['alerts'])
    },
    methods: {
      ...mapMutations({'dismiss': DISMISS_ALERT})
    }
  }
</script>

<template>
  <div class="row mb-">
    <div class="col">
      <ol class="breadcrumb" v-if="context">
        <li class="breadcrumb-item"><a href="#" @click.stop="doSomething">{{context.label}}</a></li>
        <li class="breadcrumb-item active">Members</li>
      </ol>
    </div>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  export default {
    name: 'members-breadcrumb',
    computed: {
      ...mapGetters(['context'])
    },
    methods: {
      doSomething: function () {
        alert('TODO do some unknown thing here')
      }
    }
  }
</script>

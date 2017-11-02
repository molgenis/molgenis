<template>
  <div class="row mb-">
    <div class="col">
      <ol class="breadcrumb">
        <li class="breadcrumb-item">
          <router-link :to="{name: 'members', params: {contextId: contextId}}">{{context.label}}</router-link>
        </li>
        <li class="breadcrumb-item" v-if="member">{{member.label}}</li>
        <li class="breadcrumb-item" v-else>Members</li>
      </ol>
    </div>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  export default {
    name: 'members-breadcrumb',
    computed: {
      ...mapGetters(['context', 'member', 'contextId'])
    },
    methods: {
      doSomething: function () {
        alert('TODO do some unknown thing here')
      }
    }
  }
</script>

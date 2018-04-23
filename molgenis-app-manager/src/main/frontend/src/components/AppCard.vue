<template>
    <div class="card app-card-component mb-3">
        <div class="card-header">
            <button :disabled="app.isActive" class="btn btn-danger btn-sm mx-1 float-right" @click="deleteApp(app)"
                    title="delete">
                <i class="fa fa-trash"></i>
            </button>

            <h5>{{ app.label }}</h5>
            <span>
                <toggle-button
                        :labels="{checked: ' Active', unchecked: ' Inactive'}"
                        :width="72" :value="app.isActive"
                        @change="toggleAppActiveState(app)" :sync="true"
                        title="toggle active status"></toggle-button>
            </span>
        </div>

        <div class="card-body app-card-body">
            <p class="lead"> {{ app.description }} </p>
        </div>

        <div class="card-footer">
            <a :href="appUrl">Go to app</a>
        </div>
    </div>
</template>

<style scoped>
    .app-card-component .card-body {
        height: 150px;
    }
</style>

<script>
  import Vue from 'vue'
  import ToggleButton from 'vue-js-toggle-button'
  Vue.use(ToggleButton)

  const {baseUrl} = window.__INITIAL_STATE__ || {}

  export default {
    name: 'AppCard',
    props: ['app'],
    data () {
      return {
        appUrl: baseUrl
      }
    },
    methods: {
      deleteApp (app) {
        this.$store.dispatch('DELETE_APP', app.id)
      },

      toggleAppActiveState (app) {
        if (app.isActive) {
          this.$store.dispatch('DEACTIVATE_APP', app.id)
        } else {
          this.$store.dispatch('ACTIVATE_APP', app.id)
        }
      }
    }
  }
</script>

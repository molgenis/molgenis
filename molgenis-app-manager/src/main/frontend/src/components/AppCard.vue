<template>
    <div class="card app-card-component mb-5">
        <div class="card-header">
            <button :disabled="app.isActive" class="btn btn-info btn-sm mx-1 float-right" @click="upgradeApp(app)"
                    title="upgrade">
                <i class="fa fa-arrow-circle-o-up"></i>
            </button>

            <button :disabled="app.isActive" class="btn btn-danger btn-sm mx-1 float-right" @click="deleteApp(app)"
                    title="delete">
                <i class="fa fa-trash"></i>
            </button>

            <button :disabled="app.isActive" class="btn btn-secondary btn-sm mx-1 float-right"
                    @click="openEditMode(app)" title="edit">
                <i class="fa fa-pencil"></i>
            </button>

            <h5>{{ app.label }}</h5>
            <span>
                Status:
                <span v-if="app.isActive" style="color:#75c791">Active</span>
                <span v-else style="color:#dc3545">Disabled</span>
            </span>
        </div>

        <div class="card-body app-card-body">
            <p class="lead"> {{ app.description }} </p>
        </div>

        <div class="card-footer">
            <a :href="'/menu/plugins/app/' + app.uri">Go to app</a>
            <div class="float-right" title="toggle active status">
                <toggle-button :value="app.isActive" @change="toggleAppActiveState(app)" :sync="true"></toggle-button>
            </div>
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

  export default {
    name: 'AppCard',
    props: ['app'],
    methods: {
      deleteApp (app) {
        this.$store.dispatch('DELETE_APP', app.id)
      },

      openEditMode (app) {
        this.$emit('openEditMode', app)
      },

      toggleAppActiveState (app) {
        if (app.isActive) {
          this.$store.dispatch('DEACTIVATE_APP', app.id)
        } else {
          this.$store.dispatch('ACTIVATE_APP', app.id)
        }
      },

      upgradeApp (app) {
        console.log(app)
      }
    }
  }
</script>
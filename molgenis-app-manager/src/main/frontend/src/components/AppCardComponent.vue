<template>
    <div class="card app-card-component mb-5">
        <div class="card-header">

            <button :disabled="app.isActive" class="btn btn-danger btn-sm mx-1 float-right" @click="deleteApp(app)">
                <i class="fa fa-trash"></i>
            </button>

            <button :disabled="app.isActive" class="btn btn-secondary btn-sm mx-1 float-right" @click="openEditMode(app)">
                <i class="fa fa-pencil"></i>
            </button>

            <h3>{{ app.label }}</h3>
            <span>
                Status:
                <span v-if="app.isActive" style="color:green">Active</span>
                <span v-else style="color:red">Disabled</span>
            </span>
        </div>

        <div class="card-body app-card-body">
            <p class="lead"> {{ app.description }} </p>
            <p><a :href="'/menu/plugins/app/' + app.uri">Go to app</a></p>
        </div>

        <div class="card-footer">
            <div class="float-right">
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
    name: 'AppCardComponent',
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
      }
    }
  }
</script>
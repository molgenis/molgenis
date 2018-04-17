<template>
    <div class="app-card-carousel">
        <div v-if="appToBeEdited">
            <edit-app-component :app="appToBeEdited"></edit-app-component>
        </div>

        <div class="app-card-container row" v-else>
            <div v-for="app in apps" :key="app.id" class="col-4">
                <app-card-component :app="app" @openEditMode="openEditMode"></app-card-component>
            </div>
        </div>
    </div>
</template>

<script>
  import AppCardComponent from './AppCardComponent.vue'
  import EditAppComponent from './EditAppComponent.vue'

  export default {
    name: 'AppCardCarousel',
    props: ['apps'],
    data () {
      return {
        appToBeEdited: null
      }
    },
    methods: {
      openEditMode (app) {
        this.appToBeEdited = app
      }
    },
    components: {
      AppCardComponent,
      EditAppComponent
    }
  }
</script>
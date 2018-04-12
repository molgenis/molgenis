<template>
    <div class="create-new-app-form-container">
        <div class="row mb-4">
            <div class="col-md-4">
                <button class="btn btn-block btn-dark mb-2" @click="setSection('general-info')">
                    <i class="fa fa-id-card"></i> General info
                </button>
            </div>
            <div class="col-md-4">
                <button class="btn btn-block btn-dark mb-2" @click="setSection('resources')">
                    <i class="fa fa-folder-open"></i> Resources
                </button>
            </div>
            <div class="col-md-4">
                <button class="btn btn-block btn-dark mb-2" @click="setSection('configuration')">
                    <i class="fa fa-cog"></i> Configuration
                </button>
            </div>
        </div>

        <form class="form app-creation-form">
            <template v-if="section === 'general-info'">
                <div class="form-group">
                    <input id="app-id-input"
                           class="form-control"
                           type="text"
                           v-model="app.id"
                           aria-describedby="app-id-description"
                           placeholder="Enter the ID of your app">
                    <small id="app-id-description"
                           class="form-text text-muted">
                        The ID will be used as the URL of your app.
                    </small>
                </div>

                <div class="form-group">
                    <input id="app-label-input"
                           class="form-control"
                           type="text"
                           v-model="app.label"
                           aria-describedby="app-label-description"
                           placeholder="Enter the label of your app">
                    <small id="app-label-description"
                           class="form-text text-muted">
                        The label with which you can easily find your app in the app manager table.
                    </small>
                </div>

                <div class="form-group">
                    <textarea id="app-description-input"
                              class="form-control"
                              v-model="app.description"
                              aria-describedby="app-description-description"
                              placeholder="Enter the description of your app">
                    </textarea>
                    <small id="app-description-description"
                           class="form-text text-muted">
                        The description of your app.
                    </small>
                </div>
            </template>

            <template v-else-if="section === 'resources'">
                Resources
            </template>

            <template v-else-if="section === 'configuration'">
                Configuration
            </template>
        </form>

        <hr>

        <button type="button" class="btn btn-dark float-right" @click="save">
            Save app
        </button>
    </div>
</template>

<style scoped>
    .app-creation-form {
        height: 15em;
    }
</style>

<script>
  export default {
    name: 'CreateNewAppComponent',
    data () {
      return {
        app: {},
        section: 'general-info'
      }
    },
    methods: {
      save () {
        this.$emit('save', this.app)
      },

      setSection (section) {
        this.section = section
      }
    }
  }
</script>
<template>
  <div>
    <!-- Loading spinner -->
    <template v-if="loading">
      <div class="spinner-container text-muted d-flex flex-column justify-content-center align-items-center">
        <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
        <p>{{ 'questionnaire_loading_text' | i18n }}...</p>
      </div>
    </template>

    <template v-else>
      <div class="row">

        <div class="col-lg-8 col-md-12">
          <p v-html="questionnaireDescription"></p>

          <router-link @click="startQuestionnaire" class="btn btn-lg btn-primary mt-2"
                       :to="'/' + questionnaireId + '/chapter/1'">
            {{ 'questionnaire_start' | i18n }}
          </router-link>
        </div>

      </div>
    </template>

  </div>
</template>

<script>
  export default {
    name: 'QuestionnaireStart',
    props: ['questionnaireId'],
    data () {
      return {
        loading: true
      }
    },
    methods: {
//      /**
//       * On submit handler
//       * 1) Set status to SUBMITTED
//       * 2) Trigger form validation
//       * 3) Post
//       */
//      onSubmit () {
//        // Trigger submit
//        this.formData.status = 'SUBMITTED'
//        this.formState.$submitted = true
//        this.formState._validate()
//
//        // Check if form is valid
//        if (this.formState.$valid) {
//          // Generate submit timestamp
//          this.formData.submitDate = moment().toISOString()
//          const options = {
//            body: JSON.stringify(this.formData)
//          }
//
//          // Submit to server and redirect to thank you page
//          api.post('/api/v1/' + this.questionnaire.name + '/' + this.questionnaireId + '?_method=PUT', options).then(() => {
//            this.$router.push({path: '/' + this.questionnaireId + '/thanks'})
//          }).catch(error => {
//            console.log('Something went wrong submitting the questionnaire', error)
//          })
//        } else {
//          this.formData.status = 'OPEN'
//          this.formState.$submitted = false
//        }
//      }
    },
    computed: {
      questionnaireDescription () {
        return this.$store.state.questionnaireDescription
      }
    },
    created () {
      this.$store.dispatch('GET_QUESTIONNAIRE', this.questionnaireId).then(() => {
        this.loading = false
      })
    }
  }
</script>

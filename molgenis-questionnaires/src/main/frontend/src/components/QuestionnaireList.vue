<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col-md-8">
        <h1>{{ 'questionnaires_title' | i18n }}</h1>
        <p>{{ 'questionnaires_description' | i18n }}</p>
      </div>
    </div>

    <div class="row">
      <div class="col-md-8">

        <template v-if="questionnaireList.length == 0 && !loading">
          <h3>{{ 'questionnaires_no_questionnaires_found_message' | i18n }}</h3>
        </template>

        <template v-else-if="questionnaireList.length > 0 && !loading">
          <table class="table table-bordered">

            <thead>
            <tr>
              <th>{{ 'questionnaires_table_questionnaire_header' | i18n }}</th>
              <th>{{ 'questionnaires_table_status_header' | i18n }}</th>
              <th></th>
            </tr>
            </thead>

            <tbody>

            <tr v-for="questionnaire in questionnaireList">
              <td>{{ questionnaire.label }}</td>

              <td v-if="questionnaire.status === 'NOT_STARTED'">
                {{ 'questionnaires_table_status_not_started' | i18n }}
              </td>

              <td v-else-if="questionnaire.status === 'OPEN'">
                {{ 'questionnaires_table_status_open' | i18n }}
              </td>

              <td v-else-if="questionnaire.status === 'SUBMITTED'">
                {{ 'questionnaires_table_status_submitted' | i18n}}
              </td>

              <td>
                <router-link v-if="questionnaire.status === 'SUBMITTED'" target="_blank" :to="'/' + questionnaire.id + '/overview'"
                             class="btn btn-primary">
                  {{ 'questionnaires_view_questionnaire' | i18n }}
                </router-link>

                <router-link v-else :to="'/' + questionnaire.id" class="btn btn-primary">
                  {{ 'questionnaires_view_questionnaire' | i18n }}
                </router-link>
              </td>
            </tr>
            </tbody>

          </table>
        </template>

      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'QuestionnaireList',
    data () {
      return {
        loading: true
      }
    },
    computed: {
      questionnaireList () {
        return this.$store.state.questionnaireList
      }
    },
    created () {
      this.$store.dispatch('GET_QUESTIONNAIRE_LIST').then(() => {
        this.loading = false
      })
    }
  }
</script>

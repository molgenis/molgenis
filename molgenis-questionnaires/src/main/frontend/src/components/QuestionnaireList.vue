<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col-md-6">
        <h1>{{ 'questionnaires_title' | i18n }}</h1>
        <p>{{ 'questionnaires_description' | i18n }}</p>
      </div>
    </div>

    <div class="row">
      <div class="col-md-6">

        <template v-if="questionnaires.length == 0 && !loading">
          <h3>{{ 'questionnaires_no_questionnaires_found_message' | i18n }}</h3>
        </template>

        <template v-else-if="questionnaires.length > 0 && !loading">
          <table class="table table-bordered">
            <thead>
            <tr>
              <th>{{ 'questionnaires_table_questionnaire_header' | i18n }}</th>
              <th></th>
            </tr>
            </thead>
            <tbody>

            <tr v-for="questionnaire in questionnaires">
              <td>{{ questionnaire.label }}</td>
              <td>
                <router-link :to="'/' + questionnaire.id" class="btn btn-primary">
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
  import api from '@molgenis/molgenis-api-client'

  export default {
    name: 'QuestionnaireList',
    data () {
      return {
        questionnaires: [],
        loading: true
      }
    },
    created () {
      api.get('/menu/plugins/questionnaires/meta/list').then(response => {
        this.questionnaires = response
        this.loading = false
      })
    }
  }
</script>

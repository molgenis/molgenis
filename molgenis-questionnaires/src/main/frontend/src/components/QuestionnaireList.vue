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
              <th>{{ 'questionnaires_table_status_header' | i18n }}</th>
              <th></th>
            </tr>
            </thead>
            <tbody>

            <tr v-for="questionnaire in questionnaires">
              <td>{{ questionnaire.label }}</td>

              <template v-if="questionnaire.status === 'NOT_STARTED'">
                <td>{{ 'questionnaires_table_status_not_started' | i18n }}</td>
                <td>
                  <router-link :to="'/' + questionnaire.name" class="btn btn-primary">
                    {{ 'questionnaires_table_start_questionnaire_button' | i18n }}
                  </router-link>
                </td>
              </template>

              <template v-if="questionnaire.status === 'OPEN'">
                <td>{{ 'questionnaires_table_status_open' | i18n }}</td>
                <td>
                  <router-link :to="'/' + questionnaire.name" class="btn btn-primary">
                    {{ 'questionnaires_table_continue_questionnaire_button' | i18n }}
                  </router-link>
                </td>
              </template>

              <template v-if="questionnaire.status === 'SUBMITTED'">
                <td>{{ 'questionnaires_table_status_submitted' | i18n }}</td>
                <td>
                  <router-link :to="'/' + questionnaire.name + '/overview'" class="btn btn-primary">
                    {{ 'questionnaires_table_view_questionnaire_button' | i18n }}
                  </router-link>
                </td>
              </template>
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
      api.get('/menu/plugins/questionnaires/list').then(response => {
        this.questionnaires = response
        this.loading = false
      })
    }
  }
</script>

<template>
  <div class="container-fluid">
    <div class="row my-5">
      <div class="col-12">
        <template>
          <loading-spinner :message="$t('questionnaire_loading_chapter_text')"></loading-spinner>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
  import LoadingSpinner from '../components/LoadingSpinner'

  export default {
    name: 'ChangePage',
    props: ['chapterId', 'questionnaireId'],
    created () {
      setTimeout(() => {
        this.$router.push('/' + this.questionnaireId + '/chapter/' + this.chapterId)
      }, 200)
    },
    components: {
      LoadingSpinner
    }
  }
</script>

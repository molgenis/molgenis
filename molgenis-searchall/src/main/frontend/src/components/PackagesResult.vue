<template>
  <div class="col-lg-12">
    <h3>{{'matching-packages-label' | i18n }}</h3>

    <div class="row">
      <div class="col-lg-12 padding-card-bottom">
        <div class="card">
          <ul class="list-group list-group-flush">
            <li v-for="pack in this.$store.state.result.packages" class="list-group-item">
              <a v-if="navigator" :href="navigator + '/' + pack.id">
                <i class="fa fa-folder-open-o" aria-hidden="true"></i>
                <span v-html="highlight(pack.label)"></span>
              </a>
              <span v-else v-html="highlight(pack.label)"></span>
              &nbsp;
              <small v-if="pack.description" class="text-muted">
                <i class="text-small" v-html="highlight(pack.description)"></i>
              </small>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'packages-result',
    props: ['package', 'navigator', 'highlight']
  }
</script>

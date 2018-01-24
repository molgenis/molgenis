<template>
  <div class="card">
    <div class="card-header">
      <h4 v-html="highlight(entityType.label)"></h4>
      <a v-if="navigator" :href="navigator + '/' + entityType.packageId" class="card-link"><i
        class="fa fa-folder-open-o" aria-hidden="true"></i>
        {{'show-in-navigator-link' | i18n }}</a>
      <a v-if="dataexplorer" :href="dataexplorer + '?entity=' + entityType.id" class="card-link"><i
        class="fa fa-align-justify" aria-hidden="true"></i>
        {{'show-in-dataexplorer-link' | i18n }}</a>
    </div>
    <div class="container">
      <div class="row" v-if="entityType.description">
        <div class="col-lg-4"><b>{{'description-label' | i18n }}</b>
        </div>
        <div class="col-lg-8"><i v-html="highlight(entityType.description)"></i>
        </div>
      </div>
      <div class="row mt-1">
        <div class="col-lg-4"><b>{{'data-label' | i18n }}</b></div>
        <div class="col-lg-8"><a v-if="entityType.nrOfMatchingEntities > 0 && dataexplorer"
                                 :href="dataexplorer + '?entity=' + entityType.id +'&query[q][0][operator]=SEARCH&query[q][0][value]='+this.$store.state.query"
                                 class="card-link"><i
          class="fa fa-align-justify" aria-hidden="true"></i>
          {{entityType.nrOfMatchingEntities}} {{'rows-found-label' | i18n }}</a>
          <span v-else><i
            class="fa fa-align-justify" aria-hidden="true"></i>
                      {{entityType.nrOfMatchingEntities}} {{'rows-found-label' | i18n }}</span></div>
      </div>
      <div class="row mt-1">
        <div class="col-lg-4"><b>{{'attributes-label' | i18n }}</b></div>
        <div class="col-lg-8"><span
          v-if="entityType.attributes.length === 0"><i>{{'no-attributes-found-label' | i18n }}</i></span>
          <ul v-else class="list-unstyled no-margin-bottom">
            <li v-for="attr in entityType.attributes"><span
              v-html="highlight(attr.label)"></span>(<i
              v-html="highlight(attr.description)"></i>)
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'entity-result',
    props: ['entityType', 'dataexplorer', 'navigator', 'highlight']
  }
</script>

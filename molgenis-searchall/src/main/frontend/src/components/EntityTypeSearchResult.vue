<template>
  <div class="card entity-type-card">

    <div class="card-header">
      <h4 v-html="highlight(entityType.label)"></h4>
      <navigator-link :packageId="entityType.packageId" :label="$t('show-in-navigator-link')"></navigator-link>
      <dataexplorer-link :entityTypeId="entityType.id"></dataexplorer-link>
    </div>

    <div class="card-body">
      <dl class="row entity-type-info">
        <dt v-if="entityType.description" class="col-md-3">{{'description-label' | i18n }}</dt>
        <dd v-if="entityType.description" class="col-md-9" v-html="highlight(entityType.description)"></dd>

        <dt class="col-md-3">{{'data-label' | i18n }}</dt>
        <dd class="col-md-9">

          <a v-if="entityType.nrOfMatchingEntities > 0" :href="dataexplorerSearchUri" class="card-link">
            <i class="fa fa-align-justify" aria-hidden="true"></i>
            {{entityType.nrOfMatchingEntities}} {{'rows-found-label' | i18n }}
          </a>

          <span v-else>
            <i class="fa fa-align-justify" aria-hidden="true"></i>
            {{entityType.nrOfMatchingEntities}} {{'rows-found-label' | i18n }}
          </span>
        </dd>

        <dt class="col-md-3">{{'attributes-label' | i18n }}</dt>
        <dd class="col-md-9">
          <span v-if="entityType.attributes.length === 0">
            <i>{{'no-attributes-found-label' | i18n }}</i>
          </span>

            <ul v-else class="list-unstyled">
              <li v-for="attr in entityType.attributes">
                <span v-html="highlight(attr.label)"></span>(<i v-html="highlight(attr.description)"></i>)
              </li>
            </ul>
        </dd>
      </dl>
    </div>

  </div>
</template>

<style>
  .entity-type-card {
    margin-bottom: 1rem;
  }

  .entity-type-info {
    margin-bottom: 0;
  }
</style>

<script>
  import NavigatorLink from './NavigatorLink'
  import DataexplorerLink from './DataexplorerLink'

  export default {
    name: 'entity-type-search-result',
    props: ['entityType', 'highlight', 'query'],
    data () {
      return {
        dataexplorerSearchUri: window.searchall.dataExplorerBaseUrl + '?entity=' + this.entityType.id + '&query[q][0][operator]=SEARCH&query[q][0][value]=' + this.query
      }
    },
    components: {
      NavigatorLink,
      DataexplorerLink
    }
  }
</script>

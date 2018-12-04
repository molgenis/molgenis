<template>
  <div class="table-container">
    <b-table
      :items="tableResources"
      :fields="fields"
      :filter="filter"
      :empty-text="$t('table-no-results')"
      class="text-left"
      show-empty>
      <template
        slot="HEAD_selected"
        slot-scope="data">
        <b-form-checkbox
          :class="tableResources.length == 0 ? 'invisible' : ''"
          :checked="isAllSelected()"
          @click.native.stop
          @change="toggleAllSelected">
          <!-- workaround for https://github.com/twbs/bootstrap/issues/26221 -->
          <span class="text-hide">placeholder</span>
        </b-form-checkbox>
      </template>
      <template
        slot="selected"
        slot-scope="row">
        <b-form-checkbox
          :checked="isSelected(row.item)"
          @click.native.stop
          @change="toggleSelected(row.item, $event)">
          <!-- workaround for https://github.com/twbs/bootstrap/issues/26221 -->
          <span class="text-hide">placeholder</span>
        </b-form-checkbox>
      </template>
      <template
        slot="label"
        slot-scope="label">
        <span v-if="label.item.type === 'ENTITY_TYPE' && dataExplorerUrl">
          <a :href="dataExplorerUrl + '?entity=' + label.item.id + '&hideselect=true'">
            <font-awesome-icon
              icon="list"
              fixed-width/> {{ label.item.label }}
          </a>
        </span>
        <span v-else-if="label.item.type === 'ENTITY_TYPE' || label.item.type === 'ENTITY_TYPE_ABSTRACT'">
          <font-awesome-icon
            icon="list"
            fixed-width/> {{ label.item.label }}
        </span>
        <span v-else>
          <router-link :to="label.item.id">
            <font-awesome-icon
              :icon="['far', 'folder-open']"
              fixed-width/> {{ label.item.label }}
          </router-link>
        </span>
      </template>
    </b-table>
  </div>
</template>

<script>
import {
  SELECT_ALL_RESOURCES,
  DESELECT_ALL_RESOURCES,
  DESELECT_RESOURCE,
  SELECT_RESOURCE
} from '../store/actions'
import { mapState } from 'vuex'

export default {
  name: 'NavigatorTable',
  data () {
    return {
      fields: {
        selected: {
          'class': 'compact align-middle',
          tdClass: this.cellClass
        },
        label: {
          label: this.$t('table-col-header-name'),
          sortable: true,
          'class': 'text-nowrap',
          tdClass: this.cellClass
        },
        description: {
          label: this.$t('table-col-header-description'),
          sortable: false,
          'class': 'd-none d-md-table-cell',
          tdClass: this.cellClass
        }
      },
      filter: null,
      allSelected: false,
      dataExplorerUrl: window.__INITIAL_STATE__.pluginUrls['dataexplorer']
    }
  },
  computed: {
    ...mapState(['resources', 'selectedResources', 'showHiddenResources', 'clipboard']),
    tableResources () {
      return this.resources.filter(resource => this.showHiddenResources || !resource.hidden).map(
        resource => Object.assign({}, resource)).sort((a, b) => {
        if (a.type === 'PACKAGE' && b.type !== 'PACKAGE') {
          return -1
        } else if (b.type === 'PACKAGE' && a.type !== 'PACKAGE') {
          return 1
        } else {
          return a.label.localeCompare(b.label)
        }
      })
    },
    nrResources () {
      return this.resources.filter(resource => this.showHiddenResources || !resource.hidden).length
    },
    nrSelectedResources () {
      return Object.keys(this.selectedResources).length
    }
  },
  watch: {
    '$route' (to, from) {
      this.allSelected = false
    }
  },
  methods: {
    toggleSelected: function (resource, checked) {
      if (checked) {
        this.$store.dispatch(SELECT_RESOURCE, resource)
        this.allSelected = this.nrResources === this.nrSelectedResources
      } else {
        this.$store.dispatch(DESELECT_RESOURCE, resource)
        this.allSelected = false
      }
    },
    isSelected: function (resource) {
      return this.selectedResources.some(selectedResource => selectedResource.type === resource.type && selectedResource.id === resource.id)
    },
    toggleAllSelected: function (checked) {
      if (checked) {
        this.$store.dispatch(SELECT_ALL_RESOURCES)
      } else {
        this.$store.dispatch(DESELECT_ALL_RESOURCES)
      }
    },
    isAllSelected: function (resource) {
      return this.nrResources > 0 && this.nrResources === this.nrSelectedResources
    },
    isClipboardResource: function (resource) {
      return this.clipboard && this.clipboard.resources && this.clipboard.resources.some(
        clipboardResource => clipboardResource.type === resource.type && clipboardResource.id === resource.id)
    },
    cellClass: function (value, key, resource) {
      return this.isClipboardResource(resource) ? 'bg-warning' : ''
    }
  }
}
</script>

<style>
  .table-container {
    width: 100%;
    height: 70vh; /* no page scrollbar when using default header and footer */
    overflow-y: auto;
  }

  .invisible {
    visibility: hidden;
  }

  .compact {
    width: 1px;
    white-space: nowrap;
  }
</style>

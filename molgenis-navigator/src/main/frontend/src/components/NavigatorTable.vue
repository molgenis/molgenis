<template>
  <b-table
    :items="tableItems"
    :fields="fields"
    :filter="filter"
    :empty-text="$t('table-no-results')"
    class="text-left"
    show-empty>
    <template
      slot="HEAD_selected"
      slot-scope="data">
      <b-form-checkbox
        :class="tableItems.length == 0 ? 'invisible' : ''"
        :checked="isAllSelected()"
        @click.native.stop
        @change="toggleAllSelected"/>
    </template>
    <template
      slot="selected"
      slot-scope="row">
      <b-form-checkbox
        :checked="isSelected(row.item)"
        @click.native.stop
        @change="toggleSelected(row.item, $event)"/>
    </template>
    <template
      slot="label"
      slot-scope="label">
      <span v-if="label.item.type === 'entityType'">
        <a :href="'/menu/main/dataexplorer?entity=' + label.item.id + '&hideselect=true'">
          <font-awesome-icon icon="list"/> {{ label.item.label }}
        </a>
      </span>
      <span v-else>
        <router-link :to="label.item.id">
          <font-awesome-icon :icon="['far', 'folder-open']"/> {{ label.item.label }}
        </router-link>
      </span>
    </template>
  </b-table>
</template>

<script>
import {
  SELECT_ALL_ITEMS,
  DESELECT_ALL_ITEMS,
  DESELECT_ITEM,
  SELECT_ITEM
} from '../store/actions'
import { mapState } from 'vuex'

export default {
  name: 'NavigatorTable',
  data () {
    return {
      fields: {
        selected: {
          'class': 'compact',
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
      allSelected: false
    }
  },
  computed: {
    ...mapState(['items', 'selectedItems', 'clipboard']),
    tableItems () {
      return this.items.map(item => Object.assign({}, item))
    },
    nrItems () {
      return this.items.length
    },
    nrSelectedItems () {
      return Object.keys(this.selectedItems).length
    }
  },
  watch: {
    '$route' (to, from) {
      this.allSelected = false
    }
  },
  methods: {
    toggleSelected: function (item, checked) {
      if (checked) {
        this.$store.dispatch(SELECT_ITEM, item)
        this.allSelected = this.nrItems === this.nrSelectedItems
      } else {
        this.$store.dispatch(DESELECT_ITEM, item)
        this.allSelected = false
      }
    },
    isSelected: function (item) {
      return this.selectedItems.some(selectedItem => selectedItem.type === item.type && selectedItem.id === item.id)
    },
    toggleAllSelected: function (checked) {
      if (checked) {
        this.$store.dispatch(SELECT_ALL_ITEMS)
      } else {
        this.$store.dispatch(DESELECT_ALL_ITEMS)
      }
    },
    isAllSelected: function (item) {
      return this.items.length === this.selectedItems.length
    },
    isClipboardItem: function (item) {
      return this.clipboard.items && this.clipboard.items.some(
        clipboardItem => clipboardItem.type === item.type && clipboardItem.id === item.id)
    },
    cellClass: function (value, key, item) {
      return this.isClipboardItem(item) ? 'bg-warning' : ''
    }
  }
}
</script>

<style>
  .invisible {
    visibility: hidden;
  }

  .compact {
    width: 1px;
    white-space: nowrap;
  }

  .clipped {
    background-color: pink;
  }
</style>

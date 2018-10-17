<template>
  <span>
    <b-btn
      v-b-tooltip.hover
      :title="$t('action-cut')"
      :disabled="nrSelectedItems > 0 ? false : true"
      variant="secondary"
      @click="selectClipboardItems('cut')">
      <font-awesome-icon
        :class="nrSelectedItems == 0 ? 'fa-disabled' : ''"
        icon="cut"
        size="lg"/>
    </b-btn>
    <b-btn
      v-b-tooltip.hover
      :title="$t('action-copy')"
      :disabled="nrSelectedItems > 0 ? false : true"
      variant="secondary"
      @click="selectClipboardItems('copy')">
      <font-awesome-icon
        :class="nrSelectedItems == 0 ? 'fa-disabled' : ''"
        icon="clone"
        size="lg"/>
    </b-btn>
    <b-btn
      v-b-tooltip.hover
      :title="$t('action-paste')"
      :disabled="query || nrClipboardItems === 0 ? true : false"
      variant="secondary"
      @click="pasteClipboardItems">
      <font-awesome-icon
        :class="nrClipboardItems > 0 ? '' : 'fa-disabled'"
        icon="paste"
        size="lg"/>
    </b-btn>
  </span>
</template>

<script>
import { mapGetters, mapState } from 'vuex'
import { MOVE_CLIPBOARD_ITEMS } from '../store/actions'
import { SET_CLIPBOARD } from '../store/mutations'

export default {
  name: 'NavigatorActionsClipboard',
  computed: {
    ...mapGetters(['nrSelectedItems', 'packageId', 'query', 'nrClipboardItems']),
    ...mapState(['clipboard', 'selectedItems'])
  },
  methods: {
    selectClipboardItems: function (mode) {
      const clipboard = {
        mode: mode,
        items: this.selectedItems.slice()
      }
      this.$store.commit(SET_CLIPBOARD, clipboard)
    },
    pasteClipboardItems: function () {
      if (this.clipboard.mode === 'cut') {
        this.$store.dispatch(MOVE_CLIPBOARD_ITEMS, this.packageId)
      } else {
        alert('TODO clone selection (=dataexplorer copy button)')
      }
    }
  }
}
</script>

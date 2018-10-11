<template>
  <span>
    <b-btn variant="secondary" :disabled="nrSelectedItems > 0 ? false : true" v-b-tooltip.hover :title="$t('action-cut')" @click="selectClipboardItems('cut')">
      <font-awesome-icon icon="cut" size="lg" :class="nrSelectedItems == 0 ? 'fa-disabled' : ''"/>
    </b-btn>
    <b-btn variant="secondary" :disabled="nrSelectedItems > 0 ? false : true" v-b-tooltip.hover :title="$t('action-copy')" @click="selectClipboardItems('copy')">
      <font-awesome-icon icon="clone" size="lg" :class="nrSelectedItems == 0 ? 'fa-disabled' : ''"/>
    </b-btn>
    <b-btn variant="secondary" :disabled="query || nrClipboardItems === 0 ? true : false" v-b-tooltip.hover :title="$t('action-paste')" @click="pasteClipboardItems">
      <font-awesome-icon icon="paste" size="lg" :class="nrClipboardItems > 0 ? '' : 'fa-disabled'"/>
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

<style scoped>

</style>

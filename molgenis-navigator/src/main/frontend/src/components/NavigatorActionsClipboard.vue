<template>
  <span>
    <div
      v-b-tooltip.d500
      :title="$t('action-cut')"
      class="btn-tooltip-wrapper">
      <b-btn
        :disabled="!canCut"
        variant="secondary"
        @click="selectClipboardItems('cut')">
        <font-awesome-icon
          :class="{'fa-disabled' : !canCut}"
          icon="cut"
          size="lg"/>
      </b-btn>
    </div>
    <div
      v-b-tooltip.d500
      :title="$t('action-copy')"
      class="btn-tooltip-wrapper">
      <b-btn
        :disabled="!canCopy"
        variant="secondary"
        @click="selectClipboardItems('copy')">
        <font-awesome-icon
          :class="{'fa-disabled' : !canCopy}"
          icon="clone"
          size="lg"/>
      </b-btn>
    </div>
    <div
      v-b-tooltip.d500
      :title="$t('action-paste')"
      class="btn-tooltip-wrapper">
      <b-btn
        :disabled="!canPaste"
        variant="secondary"
        @click="pasteClipboardItems">
        <font-awesome-icon
          :class="{'fa-disabled' : !canPaste}"
          icon="paste"
          size="lg"/>
      </b-btn>
    </div>
  </span>
</template>

<script>
import { mapGetters, mapState } from 'vuex'
import { COPY_CLIPBOARD_ITEMS, MOVE_CLIPBOARD_ITEMS } from '../store/actions'
import { SET_CLIPBOARD } from '../store/mutations'

export default {
  name: 'NavigatorActionsClipboard',
  computed: {
    ...mapGetters(['nrSelectedItems', 'folderId', 'query', 'nrClipboardItems']),
    ...mapState(['clipboard', 'folder', 'selectedItems']),
    canCut () {
      return this.nrSelectedItems > 0 && !(this.folder && this.folder.readonly)
    },
    canCopy () {
      return this.nrSelectedItems > 0
    },
    canPaste () {
      return !this.query && this.nrClipboardItems > 0 && !(this.folder && this.folder.readonly)
    }
  },
  methods: {
    selectClipboardItems: function (mode) {
      this.$emit('bv::disable::tooltip')
      const clipboard = {
        mode: mode,
        items: this.selectedItems.slice()
      }
      this.$store.commit(SET_CLIPBOARD, clipboard)
    },
    pasteClipboardItems: function () {
      const action = this.clipboard.mode === 'cut' ? MOVE_CLIPBOARD_ITEMS : COPY_CLIPBOARD_ITEMS
      this.$store.dispatch(action, this.folder)
    }
  }
}
</script>

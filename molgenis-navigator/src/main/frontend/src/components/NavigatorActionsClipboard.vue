<template>
  <span>
    <div
      v-b-tooltip.d500
      :title="$t('action-cut')"
      class="btn-tooltip-wrapper">
      <b-btn
        :disabled="!canCut"
        variant="secondary"
        @click="selectClipboardResources('CUT')">
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
        @click="selectClipboardResources('COPY')">
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
        @click="pasteClipboardResources">
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
import { COPY_CLIPBOARD_RESOURCES, MOVE_CLIPBOARD_RESOURCES } from '../store/actions'
import { SET_CLIPBOARD } from '../store/mutations'

export default {
  name: 'NavigatorActionsClipboard',
  computed: {
    ...mapGetters(['nrSelectedResources', 'folderId', 'query', 'nrClipboardResources']),
    ...mapState(['clipboard', 'folder', 'selectedResources']),
    canCut () {
      return this.nrSelectedResources > 0 && !(this.folder && this.folder.readonly)
    },
    canCopy () {
      return this.nrSelectedResources > 0
    },
    canPaste () {
      return !this.query && this.nrClipboardResources > 0 && !(this.folder && this.folder.readonly)
    }
  },
  methods: {
    selectClipboardResources: function (mode) {
      this.$emit('bv::disable::tooltip')
      const clipboard = {
        mode: mode,
        resources: this.selectedResources.slice()
      }
      this.$store.commit(SET_CLIPBOARD, clipboard)
    },
    pasteClipboardResources: function () {
      const action = this.clipboard.mode === 'CUT' ? MOVE_CLIPBOARD_RESOURCES : COPY_CLIPBOARD_RESOURCES
      this.$store.dispatch(action, this.folder)
    }
  }
}
</script>

<template>
  <b-modal
    id="resourceDeleteModal"
    :title="$t('delete-confirmation-title')"
    :ok-title="$t('delete-confirmation-ok-text')"
    :cancel-title="$t('delete-confirmation-cancel-text')"
    ok-variant="danger"
    cancel-variant="secondary"
    @ok="deleteSelectedResources">
    {{ 'delete-confirmation-text' | i18n }}
  </b-modal>
</template>

<script>
import { DELETE_SELECTED_RESOURCES } from '../store/actions'

export default {
  name: 'NavigatorDeleteModal',
  methods: {
    deleteSelectedResources: function () {
      this.$store.dispatch(DELETE_SELECTED_RESOURCES)
    }
  }
}
</script>

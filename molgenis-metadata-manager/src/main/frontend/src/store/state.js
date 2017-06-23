export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

export const getConfirmBeforeLeavingProperties = () => {
  return {
    title: 'There are unsaved changes',
    text: 'All unsaved changes will be lost, are you sure you want to continue?',
    type: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Yes',
    cancelButtonText: 'No, stay'
  }
}

export const getConfirmBeforeDeletingProperties = (identifier) => {
  return {
    title: 'You are about to delete ' + identifier,
    text: 'Are you sure you want to continue?',
    type: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Yes, delete',
    cancelButtonText: 'Cancel'
  }
}

const state = {
  alert: {
    message: null,
    type: null
  },
  packages: [],
  entityTypes: [],
  selectedEntityType: null,
  attributeTypes: [],
  initialEditorEntityType: null,
  editorEntityType: null,
  selectedAttributeID: null
}

export default state

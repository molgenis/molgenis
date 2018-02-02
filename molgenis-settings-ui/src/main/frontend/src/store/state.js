const {initialSetting, settingEntities} = window.__INITIAL_STATE__

export default {
  /* Setting state */
  selectedSetting: initialSetting,
  settings: settingEntities,

  /* Form state */
  formFields: [],
  formData: null,

  /* Application alert state */
  alert: null
}

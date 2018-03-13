export type VuexContext = {
  state: Object,
  commit: Function,
  dispatch: Function,
  getters: Object
}

export type UpdatedAttribute = {
  attribute: string,
  value: string | boolean | number
}

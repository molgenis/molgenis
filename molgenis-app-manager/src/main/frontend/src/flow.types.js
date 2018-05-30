export type App = {
  id: string,
  description: string,
  isActive: boolean,
  label: string,
  includeMenuAndFooter: boolean,
  appConfig?: string,
  resourceFolder: string,
  templateContent: string,
  uri: string,
  version: string
}

export type AppManagerState = {|
  apps: Array<App>,
  error: string,
  loading: boolean
|}

export type VuexContext = {
  state: AppManagerState,
  commit: Function,
  dispatch: Function,
  getters: Object
}

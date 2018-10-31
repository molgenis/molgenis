// @flow

export type AlertType = 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR'

export type Alert = {
  type: AlertType,
  message: string,
  code: ?string
}

export type JobType = 'copy' | 'download'
export type JobStatus = 'running' | 'success' | 'failed'

export type Job = {
  type: JobType,
  id: string,
  status: JobStatus
}

export type Folder = {
  id: string,
  label: string,
  parent: ?Folder,
  readonly: boolean
}

export type PathComponent = {
  id: string,
  label: string
}

export type ItemType = 'package' | 'entityType'

export type Item = {
  type: ItemType,
  id: string,
  label: string,
  description: ?string,
  readonly: boolean
}

export type Clipboard = {
  mode: "cut" | "copy",
  items: Array<Item>
}

export type State = {
  route: Object,
  token: ?string,
  alerts: Array<Alert>,
  jobs: Array<Job>,
  query: ?string,
  folder: ?Folder,
  items: Array<Item>,
  selectedItems: Array<Item>,
  clipboard: ?Clipboard
}

export type ApiItemType = 'PACKAGE' | 'ENTITY_TYPE'

export type ApiItem = {
  type: ApiItemType,
  id: string,
  label: ?string,
  description: ?string,
}

export type ApiPackage = {
  id: string,
  label: string,
  description: ?string,
  parent: ?ApiPackage
}

export type RestApiPackage = {
  id: string,
  label: string,
  description: ?string,
  parent: string
}

export type ApiEntityType = {
  id: string,
  label: string,
  description: ?string,
  abstract: boolean
}

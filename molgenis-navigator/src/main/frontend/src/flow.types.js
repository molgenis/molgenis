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

export type FolderState = {
  folder: ?Folder,
  resources: Array<Item>
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

// @flow

export type AlertType = 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR'

export type Alert = {
  type: AlertType,
  message: string,
  code: ?string
}

export type JobType = 'COPY' | 'DOWNLOAD' | 'DELETE'

export type JobStatus = 'RUNNING' | 'SUCCESS' | 'FAILED'

export type Job = {
  type: JobType,
  id: string,
  status: JobStatus,
  progress: ?number,
  progressMax: ?number,
  progressMessage: ?string,
  resultUrl: ?string
}

export type Folder = {
  id: string,
  label: string,
  parent: ?Folder,
  readonly: boolean
}

export type FolderState = {
  folder: ?Folder,
  resources: Array<Resource>
}

export type PathComponent = {
  id: string,
  label: string
}

export type ResourceType = 'PACKAGE' | 'ENTITY_TYPE' | 'ENTITY_TYPE_ABSTRACT'

export type Resource = {
  type: ResourceType,
  id: string,
  label: string,
  description: ?string,
  hidden: boolean,
  readonly: boolean
}

export type Clipboard = {
  mode: 'CUT' | 'COPY',
  resources: Array<Resource>
}

export type State = {
  route: Object,
  token: ?string,
  alerts: Array<Alert>,
  jobs: Array<Job>,
  query: ?string,
  folder: ?Folder,
  resources: Array<Resource>,
  selectedResources: Array<Resource>,
  showHiddenResources: boolean,
  clipboard: ?Clipboard
}

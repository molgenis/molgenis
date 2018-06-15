export type Toast = {
  type: 'danger' | 'success',
  message: string
}

export type Group = {
  name: string,
  label: string
}

export type SecurityModel = {
  groups: Array<Group>,
  toast: ?Toast
}

// @flow
export type State = {
  token: string,
  loggedIn: boolean,
  total: number
}

export const state: State = {
  token: 'token',
  loggedIn: false,
  total: 10
}

export function setState (token: string, loggedIn: boolean, total: number) {
  state.token = token
  state.loggedIn = loggedIn
  state.total = total
}

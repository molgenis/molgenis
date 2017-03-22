import fetch from 'isomorphic-fetch'

const jsonContentHeaders = {
  'Accept': 'application/json',
  'Content-Type': 'application/json'
}

function callApi (url, method, token) {
  const settings = {
    method: method,
    headers: jsonContentHeaders
  }

  if (token) {
    // for cross-origin requests, use a molgenis token
    settings.headers = { ...jsonContentHeaders, 'x-molgenis-token': token }
  } else {
    // for same origin requests, use the JSESSIONID cookie
    settings.credentials = 'same-origin'
  }

  return fetch(url, settings)
    .then(response => response.json()
      .then(json => ({ json, response })))
    .then(({ json, response }) => {
      if (!response.ok) {
        return Promise.reject(json)
      }
      return json
    })
}

export function get (url, token) {
  return callApi(url, 'get', token)
}

export function login (server, username, password) {
  return fetch(server.apiUrl + 'v1/login', {
    method: 'post',
    headers: jsonContentHeaders,
    body: JSON.stringify({ username: username, password: password })
  }).then(response => response.json())
}

export function logout (server, token) {
  return fetch(server.apiUrl + 'v1/logout', {
    method: 'get',
    headers: { ...jsonContentHeaders, 'x-molgenis-token': token }
  })
}

export default { login, logout, get, callApi }

// @flow

import type {Constraint} from './rsql/Constraint'
import fetch from 'isomorphic-fetch'
import { transformToRSQL } from './rsql/transformer'
export type EntityCollection = {
  items: Array<any>
}

type AttributeResponseV2 = any // TODO!

export type EntityAggregatesResponse = {
  aggs: AggregateResult,
  xAttr: AttributeResponseV2,
  yAttr: AttributeResponseV2
}

export type AggregateResult = {
  matrix: Array<Array<number>>,
  xLabels: Array<any>,
  yLabels: Array<string>,
  threshold: number
}

type FetchSettings = {
  method?: string,
  headers?: {[header : string]: string},
  mode?: string,
  cache?: string,
  credentials?: string // TODO: complete
}

const jsonContentHeaders = {
  'Accept': 'application/json',
  'Content-Type': 'application/json'
}

function callApi (url: URL, method: string, token: ?string) {
  const settings: FetchSettings = {
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

export function getEntityCollection (apiUrl: string,
                                    entityName: string,
                                    token: ?string): Promise<EntityCollection> {
  const url = new URL(`${apiUrl}/v2/${entityName}`)
  return callApi(url, 'get', token)
}

export function aggregateX (apiUrl: string,
                            entityName: string,
                            xAttr: string,
                            rsql: ?Constraint,
                            token: ?string): Promise<EntityAggregatesResponse> {
  const url = new URL(`${apiUrl}/v2/${entityName}`)
  if (rsql) {
    url.searchParams.append('q', transformToRSQL(rsql))
  }
  url.searchParams.set('aggs', `x==${xAttr}`)
  return callApi(url, 'get', token)
}

export default { getEntityCollection, aggregateX }

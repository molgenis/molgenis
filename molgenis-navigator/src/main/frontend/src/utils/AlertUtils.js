// @flow
import type { Alert } from '../flow.types'
import {createAlertError} from '../models/Alert'

function createAlertFromApiError (error: Object): Alert {
  return createAlertError(error.message, error.code)
}

export function createAlertsFromApiError (response: Object): Array<Alert> {
  return response.errors.map(createAlertFromApiError)
}

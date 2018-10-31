// @flow
import type { Alert } from '../flow.types'
import {createAlertError} from '../models/Alert'

export class AlertError extends Error {
  alerts: Array<Alert>

  constructor (alerts: Array<Alert>) {
    super()
    this.alerts = alerts
  }
}

function createAlertFromApiError (error: Object): Alert {
  return createAlertError(error.message, error.code)
}

export function createAlertErrorFromApiError (response: Object): AlertError {
  const alerts = response.errors.map(createAlertFromApiError)
  return new AlertError(alerts)
}

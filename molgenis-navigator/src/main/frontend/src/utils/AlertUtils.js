// @flow
import type { Alert } from '../flow.types'
import {createAlertError} from '../models/Alert'

export class AlertError extends Error {
  constructor (alerts) {
    super()
    this.alerts = alerts
  }
}

function createAlertFromApiError (error: Object): Alert {
  return createAlertError(error.message, error.code)
}

export function createAlertsFromApiError (response: Object): Array<Alert> {
  const alerts = response.errors.map(createAlertFromApiError)
  return new AlertError(alerts)
}

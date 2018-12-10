// @flow
import type { Alert } from '../flow.types'

export class AlertError extends Error {
  alerts: Array<Alert>

  constructor (alerts: Array<Alert>) {
    super()
    this.alerts = alerts
  }
}

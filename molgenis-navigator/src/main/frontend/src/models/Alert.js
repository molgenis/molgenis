// @flow
import type { Alert, AlertType } from '../flow.types'

export function createAlertError (message: string, code?: string): Alert {
  return createAlert('ERROR', message, code)
}

export function createAlert (type: AlertType, message: string, code?: string): Alert {
  return {type: type, message: message, code: code}
}

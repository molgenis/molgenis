// @flow
import {encode} from 'mdurl'

export function containsRsqlReservedCharacter (value: string): boolean {
  return /["'();,=!~<> ]/.test(value)
}

/**
 * Escapes an rsql value by putting it between quotes.
 */
export function rsqlEscape (value: string): string {
  const doubleQuotes = (value.match(/["]/g) || []).length
  const singleQuotes = (value.match(/[']/g) || []).length

  const quoteChar = (doubleQuotes >= singleQuotes) ? "'" : '"'
  return quoteChar + value.split(quoteChar).join('\\' + quoteChar) + quoteChar
}

export function toRsqlValue (value: string): string {
  return containsRsqlReservedCharacter(value) ? rsqlEscape(value) : value
}

/**
 * URLEncodes an rsql value, leaving as many rsql-relevant characters as possible unencoded
 */
export function encodeRsqlValue (str: string): string {
  return encode(str, encode.componentChars + "=:,;\"'<>#", false)
}

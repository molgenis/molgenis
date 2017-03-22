import {encode} from 'mdurl'

export function containsRsqlReservedCharacter (value) {
  return /["'();,=!~<> ]/.test(value)
}

/**
 * Escapes an rsql value by putting it between quotes.
 */
export function rsqlEscape (value) {
  const doubleQuotes = (value.match(/["]/g) || []).length
  const singleQuotes = (value.match(/[']/g) || []).length

  const quoteChar = (doubleQuotes >= singleQuotes) ? "'" : '"'
  return quoteChar + value.split(quoteChar).join('\\' + quoteChar) + quoteChar
}

export function toRsqlValue (value) {
  return containsRsqlReservedCharacter(value) ? rsqlEscape(value) : value
}

/**
 * URLEncodes an rsql value, leaving as many rsql-relevant characters as possible unencoded
 */
export function encodeRsqlValue (str) {
  return encode(str, encode.componentChars + "=:,;\"'<>#", false)
}

export function capitalizeFirstLetter (string: string): string {
  return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase()
}

export function toUpper (string: string): string {
  return string.toUpperCase()
}

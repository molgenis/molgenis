declare module '@molgenis/molgenis-api-client' {
  declare function get(uri: string, options?: Object): Promise<*>

  declare function post(uri: string, options?: Object): Promise<*>
}

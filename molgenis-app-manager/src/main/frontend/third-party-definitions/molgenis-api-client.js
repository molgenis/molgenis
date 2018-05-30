declare module '@molgenis/molgenis-api-client' {
  declare function get(uri: string, options?: Object): Promise<*>
  declare function post(uri: string, options?: Object): Promise<*>
  declare function delete_(uri: string, options?: Object): Promise<*>
  declare function postFile(uri: string, file: Object): Promise<*>
}

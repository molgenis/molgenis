// @flow
export type Update = {
  key: string,
  value: any
}

export type UpdateOrder = {
  moveOrder: string,
  selectedAttributeIndex: number
}

export type State = {
  alert: Alert,
  packages: Array<EditorPackageIdentifier>,
  entityTypes: Array<Object>,
  attributeTypes: Array<string>,
  selectedEntityTypeId: ?string,
  selectedAttributeId: ?string,
  editorEntityType: EditorEntityType,
  initialEditorEntityType: ?EditorEntityType,
  loading: number,
  languageCodes: Array<string>
}

export type Alert = {
  'message': ?string,
  'type': ?string
}

export type Tag = {
  'id': string,
  'label': ?string
}

export type Package = {
  'id': string,
  'label': ?string,
  'description': ?string,
  'parent': ?Package,
  'children': ?Array<Package>,
  'entityTypes': ?Array<any>,
  'tags': ?Array<any>
}

export type EditorPackageIdentifier = {
  'id': string,
  'label': ?string
}

export type EditorEntityTypeParent = {
  'id': string,
  'label': ?string,
  'attributes': Array<EditorAttributeIdentifier>,
  'parent': ?EditorEntityTypeParent
}

export type EditorEntityTypeIdentifier = {
  'id': string,
  'label': ?string
}

export type EditorEntityType = {
  'id': string,
  'label': string,
  'labelI18n'?: ?any,
  'description'?: ?string,
  'descriptionI18n'?: ?any,
  'abstract0'?: ?boolean,
  'backend'?: ?string,
  'package0'?: ?EditorPackageIdentifier,
  'entityTypeParent'?: ?EditorEntityTypeParent,
  'attributes': Array<EditorAttribute>,
  'referringAttributes': Array<EditorAttribute>,
  'tags'?: ?Array<Tag>,
  'idAttribute'?: EditorAttributeIdentifier,
  'labelAttribute'?: EditorAttributeIdentifier,
  'lookupAttributes'?: ?Array<EditorAttributeIdentifier>,
  'isNew'?: ?boolean
}

export type EditorOrder = {
  'attributeName': string,
  'direction': ?string
}

export type EditorSort = {
  'orders': Array<EditorOrder>
}

export type EditorAttributeIdentifier = {
  'id': string,
  'label': ?string,
  'entity'?: EditorEntityTypeIdentifier
}

export type EditorAttribute = {
  'id': string,
  'name': string,
  'type'?: string,
  'parent': ?EditorAttributeIdentifier,
  'refEntityType': ?EditorEntityTypeIdentifier,
  'cascadeDelete': ?boolean,
  'mappedByAttribute': ?EditorAttributeIdentifier,
  'orderBy': ?EditorSort,
  'expression': ?string,
  'nullable': ?boolean,
  'auto': ?boolean,
  'visible': ?boolean,
  'label': ?string,
  'labelI18n': ?any,
  'description': ?string,
  'descriptionI18n': ?any,
  'aggregatable': ?boolean,
  'enumOptions': ?Array<string>,
  'rangeMin': ?number,
  'rangeMax': ?number,
  'readonly': ?boolean,
  'unique': ?boolean,
  'tags': ?Array<Tag>,
  'nullableExpression': ?string,
  'visibleExpression': ?string,
  'validationExpression': ?string,
  'defaultValue': ?string,
  'sequenceNumber': number,
  'isNew': ?boolean
}

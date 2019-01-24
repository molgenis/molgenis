import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'
import api from '@molgenis/molgenis-api-client'

const entityMapperSettings = {
  showNonVisibleAttributes: true
}

const isFileIncluded = (formData, formFields) => {
  const fieldsWithFile = formFields
    .filter((field) => field.type === 'file')
    .find((field) => typeof formData[field.id] !== 'string')

  return !!fieldsWithFile
}

const buildFormData = (data, fields) => {
  const formData = new FormData()
  Object.entries(data).forEach((pair) => {
    const [key, value] = pair
    const isFile = fields.find((field) => {
      return field.id === key && field.type === 'file' && typeof value !==
        'string'
    })

    if (isFile) {
      formData.append(key, value, value.name)
    } else {
      const stringValue = value === undefined || value === null ? '' : value
      formData.append(key, stringValue)
    }
  })
  return formData
}

const doPost = (uri, formData, formFields) => {
  const containsFileData = isFileIncluded(formData, formFields)
  const options = {
    headers: {
      'Accept': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    },
    body: containsFileData ? buildFormData(formData, formFields) : JSON.stringify(formData),
    method: 'POST',
    credentials: 'same-origin'
  }

  return api.post(uri, options, containsFileData)
}

/**
 * response mixes data and metadata, this function creates a new object with separate properties for data and metadata
 * @param response
 * @returns {{_meta: *, rowData: *}}
 */
const parseEditResponse = (response) => {
  // noinspection JSUnusedLocalSymbols
  const { _meta, _href, ...rowData } = response
  return {_meta, rowData}
}

const fetchForCreate = (tableId) => {
  return new Promise((resolve, reject) => {
    api.get('/api/v2/' + tableId + '?num=0').then((response) => {
      const mappedData = EntityToFormMapper.generateForm(response.meta, {}, { mapperMode: 'CREATE', ...entityMapperSettings })
      resolve({formLabel: response.meta.label, ...mappedData})
    }, reject)
  })
}

const fetchForUpdate = (tableId, rowId) => {
  return new Promise((resolve, reject) => {
    api.get('/api/v2/' + tableId + '/' + rowId).then((response) => {
      const {_meta, rowData} = parseEditResponse(response)
      const mappedData = EntityToFormMapper.generateForm(_meta, rowData, { mapperMode: 'UPDATE', ...entityMapperSettings })
      resolve({formLabel: response._meta.label, ...mappedData})
    }, reject)
  })
}

const create = (formData, formFields, tableId) => {
  return doPost('/api/v1/' + tableId + '?_method=PUT', formData, formFields)
}

const update = (formData, formFields, tableId, rowId) => {
  return doPost('/api/v1/' + tableId + '/' + rowId + '?_method=PUT', formData, formFields)
}

const save = (formData, formFields, tableId, rowId) => {
  return rowId === null ? create(formData, formFields, tableId) : update(formData, formFields, tableId, rowId)
}

const fetch = (tableId, rowId) => rowId === null ? fetchForCreate(tableId) : fetchForUpdate(tableId, rowId)

export {
  save, fetch
}

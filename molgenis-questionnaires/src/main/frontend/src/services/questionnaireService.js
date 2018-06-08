import type { QuestionnaireEntityResponse } from '../flow.types.js'
import {ResponseMetaAttribute} from "../flow.types"

const compoundFields = (compound) => {
  return compound.attributes.reduce((accum, attribute: ResponseMetaAttribute) => {
    if(attribute.fieldType !== 'COMPOUND') {
      accum[attribute.name] = []
    } else {
      accum = {...accum, ...compoundFields(attribute)}
    }
    return accum
  }, {})
}

export default {
  /**
   * Build a object to hold the questionnaire answers in, based on the questionnaire's metadata structure
   * @param questionnaireResp
   * @returns Object key values map with question ids as key and empty values
   */
  buildFormDataObject: function (questionnaireResp: QuestionnaireEntityResponse) {
    return questionnaireResp.meta.attributes.reduce((accum, attribute: ResponseMetaAttribute) => {
      if(attribute.fieldType !== 'COMPOUND') {
        accum[attribute.name] = []
      } else {
        accum = {...accum, ...compoundFields(attribute)}
      }
      return accum
    }, {})
  }
}

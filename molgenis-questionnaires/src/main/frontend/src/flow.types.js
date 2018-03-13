export type QuestionnaireState = {
  questionnaire: Object,
  questionnaireList: Array<*>,
  chapterFields: Array<*>,
  formData: Object,
  questionnaireRowId: string,
  submissionText: string,
  mapperOptions: Object,
  loading: boolean,
  error: string
}

export type VuexContext = {
  state: QuestionnaireState,
  commit: Function,
  dispatch: Function,
  getters: Object
}

export type UpdatedAttribute = {
  attribute: string,
  value: string | boolean | number
}

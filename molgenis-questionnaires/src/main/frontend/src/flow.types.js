export type QuestionnaireState = {
  chapterFields: Array<*>,
  error: string,
  formData: Object,
  loading: boolean,
  mapperOptions: Object,
  navigationBlocked: boolean,
  questionnaire: Object,
  questionnaireList: Array<*>,
  questionnaireRowId: string,
  saving: boolean,
  submissionText: string
}

export type VuexContext = {
  state: QuestionnaireState,
  commit: Function,
  dispatch: Function,
  getters: Object
}

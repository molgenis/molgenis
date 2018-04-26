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
  numberOfOutstandingCalls: number,
  submissionText: string
}

export type VuexContext = {
  state: QuestionnaireState,
  commit: Function,
  dispatch: Function,
  getters: Object
}

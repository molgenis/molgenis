export type QuestionnaireState = {
  chapters: Array<Chapter>,
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

export type ChapterField = {
  id: string,
  label: string,
  description?: string,
  visible: Function,
  required: Function
}

export type ChapterFieldGroup = {
  id: string,
  label: string,
  children?: Array<ChapterField | ChapterFieldGroup>
}

export type Chapter = {
  id: string,
  label: string,
  description?: string,
  children?: Array<ChapterField | ChapterFieldGroup>
}


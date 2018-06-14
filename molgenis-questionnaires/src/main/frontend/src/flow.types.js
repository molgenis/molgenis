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

export type EntityFieldType = 'BOOL' | 'CATEGORICAL' | 'ENUM' | 'XREF' | 'MREF' | 'ONETOMANY' |
  'INT' | 'DECIMAL' | 'LONG' | 'TEXT' | 'SCRIPT' | 'HTML' | 'DATE' | 'DATE_TIME' | 'CATEGORICAL_MREF' |
  'STRING' | 'HYPERLINK' | 'EMAIL' | 'FILE' | 'ONE_TO_MANY' | 'COMPOUND'

export type ResponseMetaAttribute = {
  attributes: Array<ResponseMetaAttribute>,
  fieldType: EntityFieldType,
  name: string
}

export type ResponseMeta = {
  attributes: Array<ResponseMetaAttribute>
}

export type QuestionnaireEntityResponse = {
  href: string,
  items: Array,
  meta: ResponseMeta,
  num: number,
  start: number,
  total: number
}

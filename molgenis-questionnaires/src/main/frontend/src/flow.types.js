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

export type QuestionnaireStatus = 'NOT_STARTED' | 'OPEN' | 'SUBMITTED'

export type QuestionnaireType = {
  id: string,
  label: string,
  description? : string,
  status: QuestionnaireStatus
}

export type ReportHeaderData = {
  introText?: string,
  logoDataUrl?: string
}

export type QuestionnaireState = {
  username: string,
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
  submissionText: string,
  reportData: ?ReportHeaderData
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
  name: string,
  label: string,
  refEntity?: {
    labelAttribute: string
  }
}

export type ResponseMeta = {
  label: string,
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

export type OverViewAnswer = {
  questionId: string,
  questionLabel: string,
  answerLabel: string
}

export type OverViewSection = {
  title: string,
  chapterSections: Array<OverViewAnswer | OverViewSection>
}

export type OverViewChapter = {
  id: string,
  title: string,
  chapterSections: Array<OverViewAnswer | OverViewSection>
}

export type OverView = {
  title: string,
  intoText?: string,
  logoData?: string,
  chapters: Array<OverViewChapter>
}

export type Translation = {
  trueLabel: string,
  falseLabel: string
}

export type PdfSection = {
  text: string,
  style?: string
}

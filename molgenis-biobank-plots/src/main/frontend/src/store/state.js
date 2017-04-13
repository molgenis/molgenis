// @flow

export type State = {
  entities: {
    biobanks: string,
    samples: string
  },
  numberOfSamples: number,
  transcriptome: boolean,
  methylome: boolean,
  genotypes: boolean,
  wbcc: boolean,
  metabolome: boolean,
  wgs: boolean,
  male: boolean,
  female: boolean,
  smoking: boolean,
  nonSmoking: boolean,
  belowTwenty: boolean,
  twentyThirty: boolean,
  thirtyFourty: boolean,
  fourtyFifty: boolean,
  fiftySixty: boolean,
  sixtySeventy: boolean,
  seventyEighty: boolean,
  aboveEigthy: boolean,
  biobank: ?string,
  aggs: Array<{}>,
  biobanks: Array<Biobank>,
  charts?: Charts,
  apiUrl: string,
  token?: string
}

export type Biobank = {id: string, abbr: string}

export type Charts = {
  gender: Chart,
  smoking: Chart,
  data_types: Chart,
  age: Chart
}

export type Chart = {
  columns: Array<{key:string, label:string, type: string}>,
  rows: Array<{label: string}>,
  title: string
}

let state: State = {
  entities: {
    biobanks: 'leiden_biobanks',
    samples: 'leiden_RP'
  },
  numberOfSamples: 0,
  transcriptome: false,
  methylome: false,
  genotypes: false,
  wbcc: false,
  metabolome: false,
  wgs: false,
  male: false,
  female: false,
  smoking: false,
  nonSmoking: false,
  belowTwenty: false,
  twentyThirty: false,
  thirtyFourty: false,
  fourtyFifty: false,
  fiftySixty: false,
  sixtySeventy: false,
  seventyEighty: false,
  aboveEigthy: false,
  biobank: null,
  aggs: [],
  biobanks: [],
  charts: undefined,
  apiUrl: window.location.origin + '/api'
}

if (process.env.NODE_ENV === 'development') {
  state = {
    ...state,
    apiUrl: 'https://molgenis09.gcc.rug.nl/api',
    token: 'test'
  }
} else if (process.env.NODE_ENV === 'production') {
  state = {
    ...state,
    ...(window.__INITIAL_STATE__ || {})
  }
}

export default state

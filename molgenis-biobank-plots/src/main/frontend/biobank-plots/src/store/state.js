
let state = {
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
  charts: {
    gender: null,
    smoking: null,
    data_types: null,
    age: null
  },
  apiUrl: window.location.origin + '/api/'
}

if (process.env.NODE_ENV === 'development') {
  state = {
    ...state,
    apiUrl: 'https://molgenis09.gcc.rug.nl/api/',
    token: 'test'
  }
}

export default state

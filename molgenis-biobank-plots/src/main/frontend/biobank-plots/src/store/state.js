export const state = {
  numberOfSamples: 0,
  rnaseq: false,
  DNAm: false,
  DNA: false,
  wbcc: false,
  metabolomics: false,
  male: false,
  female: false,
  smoking: false,
  nonSmoking: false,
  biobank: null,
  aggs: [],
  biobanks: [],
  attributeCharts: [{
    title: 'Sex',
    rows: [
      ['sex', 194, 127, 456]
    ],
    columns: [
      {type: 'string', label: 'label'},
      {type: 'number', label: 'female'},
      {type: 'number', label: 'male'},
      {type: 'number', label: 'Unknown'}
    ]
  }, {
    title: 'DNA',
    rows: [
      ['DNA', 13, 764, 0],
      ['rnaseq', 473, 304, 0]
    ],
    columns: [
      {type: 'string', label: 'label'},
      {type: 'number', label: 'True'},
      {type: 'number', label: 'False'},
      {type: 'number', label: 'Unknown'}
    ]
  }],
  server: {
    apiUrl: 'https://molgenis09.gcc.rug.nl/api/'
  },
  token: 'test'
}

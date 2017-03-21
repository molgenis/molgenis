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
  attributeCharts: [],
  server: {
    apiUrl: 'https://molgenis09.gcc.rug.nl/api/'
  },
  token: 'test'
}

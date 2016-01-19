window.top.$ = require('jquery');
window.top.jQuery = require('jquery');
window.top.molgenis = window.top.molgenis || {};
import RestClientV1 from './modules/RestClientV1';
import RestClientV2 from './modules/RestClientV2';

window.top.molgenis.RestClient = RestClientV1;
window.top.molgenis.RestClientV2 = RestClientV2;


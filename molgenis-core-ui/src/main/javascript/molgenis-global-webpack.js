window.top.$ = require('jquery');
window.top.jQuery = require('jquery');
window.top.molgenis = window.top.molgenis || {};
import RestClientV1 from 'rest-client/RestClientV1';
import RestClientV2 from 'rest-client/RestClientV2';

window.top.molgenis.RestClient = RestClientV1;
window.top.molgenis.RestClientV2 = RestClientV2;


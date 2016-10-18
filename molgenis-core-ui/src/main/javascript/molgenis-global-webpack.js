window.top.$ = require('jquery');
window.top.jQuery = require('jquery');
window.top._ = require('underscore');
window.top.molgenis = window.top.molgenis || {};

import RestClientV1 from "rest-client/RestClientV1";
import RestClientV2, {createRsqlQuery} from "rest-client/RestClientV2";
import { packageSeparator } from 'rest-client';
import {
    getAtomicAttributes,
    getCompoundAttributes,
    getAllAttributes,
    getAttributeLabel,
    isRefAttr,
    isXrefAttr,
    isMrefAttr,
    isCompoundAttr
} from "rest-client/AttributeFunctions";
import {htmlEscape} from "utils/HtmlUtils";

window.top.molgenis.RestClient = RestClientV1;
window.top.molgenis.RestClientV2 = RestClientV2;
window.top.molgenis.packageSeparator = packageSeparator;

window.top.molgenis.createRsqlQuery = createRsqlQuery;

window.top.molgenis.getAtomicAttributes = getAtomicAttributes;
window.top.molgenis.getCompoundAttributes = getCompoundAttributes;
window.top.molgenis.getAllAttributes = getAllAttributes;
window.top.molgenis.getAttributeLabel = getAttributeLabel;
window.top.molgenis.isRefAttr = isRefAttr;
window.top.molgenis.isXrefAttr = isXrefAttr;
window.top.molgenis.isMrefAttr = isMrefAttr;
window.top.molgenis.isCompoundAttr = isCompoundAttr;

window.htmlEscape = htmlEscape;
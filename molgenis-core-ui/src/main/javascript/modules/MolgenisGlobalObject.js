/**
 * @module MolgenisGlobalObject
 */
import ui from './react-components';
import alert from './MolgenisAlert';
import i18n from './MolgenisInternationalization';

/*
 * Old style molgenis object to put in the global scope for backwards
 * compatibility
 */
export default {
	'ui' : ui,
	'createAlert' : alert.createAlert,
	'i18n' : i18n
};
module molgenis.data.i18n {
	requires guava;
	requires spring.tx;
	requires slf4j.api;

	requires molgenis.i18n;
	requires molgenis.security.core;
	requires molgenis.data;
	requires spring.context;
	requires spring.core;
	requires jsr305;
	requires molgenis.settings;
	requires spring.beans;
}
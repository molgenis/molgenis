module molgenis.data.security {
	requires molgenis.data;
	requires jsr305;
	requires spring.context;
	requires spring.security.core;
	requires guava;
	requires molgenis.security.core;
	requires slf4j.api;
	requires molgenis.settings;

	exports org.molgenis.data.security.owned;
	exports org.molgenis.data.security.meta to molgenis.data.validation;
}
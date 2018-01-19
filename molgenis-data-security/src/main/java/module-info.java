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
	exports org.molgenis.data.security.permission;
	exports org.molgenis.data.security.meta to molgenis.data.validation;
	exports org.molgenis.data.security.auth to molgenis.security, molgenis.web;
	exports org.molgenis.data.security.user to molgenis.security;
}
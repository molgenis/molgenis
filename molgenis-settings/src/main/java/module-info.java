module molgenis.settings {
	requires jsr305;
	requires spring.beans;
	requires spring.context;
	requires molgenis.data;
	requires molgenis.security.core;
	requires molgenis.util;

	exports org.molgenis.settings;
}
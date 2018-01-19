module molgenis.data.validation {
	requires slf4j.api;
	requires guava;
	requires commons.lang3;
	requires spring.context;
	requires org.hibernate.validator;

	requires molgenis.data;
	requires molgenis.data.file;
	requires molgenis.data.security;
	requires molgenis.js;
	requires molgenis.security.core;
	requires molgenis.util;

	exports org.molgenis.data.validation;
}
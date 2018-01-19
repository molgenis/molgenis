module molgenis.data {
	requires spring.context;
	requires molgenis.i18n;
	requires guava;
	requires molgenis.util;
	requires jsr305;

	exports org.molgenis.data;
	exports org.molgenis.data.listeners;
	exports org.molgenis.data.meta;
	exports org.molgenis.data.meta.model;
	exports org.molgenis.data.populate;
	exports org.molgenis.data.support;
	exports org.molgenis.data.system.model;
	exports org.molgenis.data.util;
}
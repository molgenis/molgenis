module molgenis.i18n {
	requires guava;
	requires spring.context;
	requires jsr305;
	requires commons.lang3;

	exports org.molgenis.i18n;
	exports org.molgenis.i18n.format;
	exports org.molgenis.i18n.properties;
}
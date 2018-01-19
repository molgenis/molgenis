module molgenis.security.core {
	requires aspectjweaver;
	requires spring.core;
	requires spring.security.core;
	requires spring.context;
	exports org.molgenis.security.core;
	exports org.molgenis.security.core.runas;
	exports org.molgenis.security.core.utils;
}
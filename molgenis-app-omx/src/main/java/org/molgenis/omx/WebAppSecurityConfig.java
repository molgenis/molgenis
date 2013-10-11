package org.molgenis.omx;

import static org.molgenis.security.SecurityUtils.defaultPluginAuthorities;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends MolgenisWebAppSecurityConfig
{
	@Override
	protected void configureUrlAuthorization(ExpressionUrlAuthorizationConfigurer<HttpSecurity> euac)
	{
		euac.antMatchers("/").permitAll()

		.antMatchers("/plugin/home/**").hasAnyAuthority(defaultPluginAuthorities("home"))

		.antMatchers("/plugin/genomebrowser/**").hasAnyAuthority(defaultPluginAuthorities("genomebrowser"))
		
		.antMatchers("/plugin/protocolviewer/**").hasAnyAuthority(defaultPluginAuthorities("protocolviewer"))

		.antMatchers("/plugin/dataexplorer/**").hasAnyAuthority(defaultPluginAuthorities("dataexplorer"))

		.antMatchers("/plugin/entityexplorer/**").hasAnyAuthority(defaultPluginAuthorities("entityexplorer"))

		.antMatchers("/plugin/importwizard/**").hasAnyAuthority(defaultPluginAuthorities("importwizard"))

		.antMatchers("/plugin/news/**").hasAnyAuthority(defaultPluginAuthorities("news"))

		.antMatchers("/plugin/background/**").hasAnyAuthority(defaultPluginAuthorities("background"))

		.antMatchers("/plugin/references/**").hasAnyAuthority(defaultPluginAuthorities("references"))

		.antMatchers("/plugin/contact/**").hasAnyAuthority(defaultPluginAuthorities("contact"))

		.antMatchers("/plugin/cbmtoomxconverter/**").hasAnyAuthority(defaultPluginAuthorities("cbmtoomxconverter"))

		.antMatchers("/plugin/form.DataSet").hasAnyAuthority(defaultPluginAuthorities("formdataSet"))

		.antMatchers("/plugin/form.Protocol?subForms=DataSet.ProtocolUsed")
				.hasAnyAuthority(defaultPluginAuthorities("formprotocol"))

				.antMatchers("/plugin/form.ObservableFeature?subForms=Category.observableFeature")
				.hasAnyAuthority(defaultPluginAuthorities("formobservablefeature"))

				.antMatchers("/plugin/form.Category").hasAnyAuthority(defaultPluginAuthorities("formcategory"))

				.antMatchers("/plugin/form.BoolValue").hasAnyAuthority(defaultPluginAuthorities("formboolvalue"))

				.antMatchers("/plugin/form.CategoricalValue")
				.hasAnyAuthority(defaultPluginAuthorities("formcategoricalvalue"))

				.antMatchers("/plugin/form.DateValue").hasAnyAuthority(defaultPluginAuthorities("formdatevalue"))

				.antMatchers("/plugin/form.DateTimeValue")
				.hasAnyAuthority(defaultPluginAuthorities("formdatetimevalue"))

				.antMatchers("/plugin/form.DecimalValue").hasAnyAuthority(defaultPluginAuthorities("formdecimalvalue"))

				.antMatchers("/plugin/form.EmailValue").hasAnyAuthority(defaultPluginAuthorities("formemailvalue"))

				.antMatchers("/plugin/form.HtmlValue").hasAnyAuthority(defaultPluginAuthorities("formhtmlvalue"))

				.antMatchers("/plugin/form.HyperlinkValue")
				.hasAnyAuthority(defaultPluginAuthorities("formhyperlinkvalue"))

				.antMatchers("/plugin/form.IntValue").hasAnyAuthority(defaultPluginAuthorities("formintvalue"))

				.antMatchers("/plugin/form.LongValue").hasAnyAuthority(defaultPluginAuthorities("formlongvalue"))

				.antMatchers("/plugin/form.MrefValue").hasAnyAuthority(defaultPluginAuthorities("formmrefvalue"))

				.antMatchers("/plugin/form.StringValue").hasAnyAuthority(defaultPluginAuthorities("formstringvalue"))

				.antMatchers("/plugin/form.TextValue").hasAnyAuthority(defaultPluginAuthorities("formtextvalue"))

				.antMatchers("/plugin/form.XrefValue").hasAnyAuthority(defaultPluginAuthorities("formxrefvalue"))

				.antMatchers("/plugin/form.StudyDataRequest")
				.hasAnyAuthority(defaultPluginAuthorities("formstudydatarequest"))

				.antMatchers("/plugin/form.RuntimeProperty")
				.hasAnyAuthority(defaultPluginAuthorities("formruntimeproperty"))

				// TODO specify for each plugin configuration

				.antMatchers("/plugin/permissionmanager/**")
				.hasAnyAuthority(defaultPluginAuthorities("permissionmanager"))

				.antMatchers("/plugin/catalogmanager/**").hasAnyAuthority(defaultPluginAuthorities("catalogmanager"))

				.antMatchers("/plugin/studymanager/**").hasAnyAuthority(defaultPluginAuthorities("studymanager"))

				.antMatchers("/plugin/dataindexer/**").hasAnyAuthority(defaultPluginAuthorities("dataindexer"))

				.antMatchers("/plugin/datasetdeleter/**").hasAnyAuthority(defaultPluginAuthorities("datasetdeleter"))

				.antMatchers("/plugin/useraccount/**").hasAnyAuthority(defaultPluginAuthorities("useraccount"));
	}

	@Override
	public RoleHierarchy roleHierarchy()
	{
		StringBuilder hierarchyBuilder = new StringBuilder();

		// Plugins: WRITE -> READ
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_BACKGROUND > ROLE_PLUGIN_READ_BACKGROUND").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_CATALOGMANAGER > ROLE_PLUGIN_READ_CATALOGMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_CBMTOOMXCONVERTER > ROLE_PLUGIN_READ_CBMTOOMXCONVERTER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_CONTACT > ROLE_PLUGIN_READ_CONTACT").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_DATAEXPLORER > ROLE_PLUGIN_READ_DATAEXPLORER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_DATAINDEXER > ROLE_PLUGIN_READ_DATAINDEXER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_DATASETDELETER > ROLE_PLUGIN_READ_DATASETDELETER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_ENTITYEXPLORER > ROLE_PLUGIN_READ_ENTITYEXPLORER").append(' ');
		// TODO add form plugins
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_HOME > ROLE_PLUGIN_READ_HOME").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_IMPORTWIZARD > ROLE_PLUGIN_READ_IMPORTWIZARD").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_NEWS > ROLE_PLUGIN_READ_NEWS").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_PERMISSIONMANAGER > ROLE_PLUGIN_READ_PERMISSIONMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_PROTOCOLMANAGER > ROLE_PLUGIN_READ_PROTOCOLMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_PROTOCOLVIEWER > ROLE_PLUGIN_READ_PROTOCOLVIEWER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_REFERENCES > ROLE_PLUGIN_READ_REFERENCES").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_STUDY > ROLE_PLUGIN_READ_STUDY").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_STUDYMANAGER > ROLE_PLUGIN_READ_STUDYMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_USERACCOUNT > ROLE_PLUGIN_READ_USERACCOUNT").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_VOID > ROLE_PLUGIN_READ_VOID").append(' ');

		// Entities: WRITE > READ
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_ACCESSION > ROLE_ENTITY_READ_ACCESSION").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_BOOLVALUE > ROLE_ENTITY_READ_BOOLVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_CATEGORICALVALUE > ROLE_ENTITY_READ_CATEGORICALVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_CATEGORY > ROLE_ENTITY_READ_CATEGORY").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_CHARACTERISTIC > ROLE_ENTITY_READ_CHARACTERISTIC").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_DATASET > ROLE_ENTITY_READ_DATASET").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_DATETIMEVALUE > ROLE_ENTITY_READ_DATETIMEVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_DATEVALUE > ROLE_ENTITY_READ_DATEVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_DECIMALVALUE > ROLE_ENTITY_READ_DECIMALVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_EMAILVALUE > ROLE_ENTITY_READ_EMAILVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_HTMLVALUE > ROLE_ENTITY_READ_HTMLVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_HYPERLINKVALUE > ROLE_ENTITY_READ_HYPERLINKVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_INDIVIDUAL > ROLE_ENTITY_READ_INDIVIDUAL").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_INSTITUTE > ROLE_ENTITY_READ_INSTITUTE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_INTVALUE > ROLE_ENTITY_READ_INTVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_LONGVALUE > ROLE_ENTITY_READ_LONGVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_MOLGENISFILE > ROLE_ENTITY_READ_MOLGENISFILE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_MREFVALUE > ROLE_ENTITY_READ_MREFVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_OBSERVABLEFEATURE > ROLE_ENTITY_READ_OBSERVABLEFEATURE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_OBSERVATIONSET > ROLE_ENTITY_READ_OBSERVATIONSET").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_OBSERVATIONTARGET > ROLE_ENTITY_READ_OBSERVATIONTARGET").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_OBSERVEDVALUE > ROLE_ENTITY_READ_OBSERVEDVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_ONTOLOGY > ROLE_ENTITY_READ_ONTOLOGY").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_ONTOLOGYTERM > ROLE_ENTITY_READ_ONTOLOGYTERM").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_PANEL > ROLE_ENTITY_READ_PANEL").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_PANELSOURCE > ROLE_ENTITY_READ_PANELSOURCE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_PERSON > ROLE_ENTITY_READ_PERSON").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_PERSONROLE > ROLE_ENTITY_READ_PERSONROLE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_PROTOCOL > ROLE_ENTITY_READ_PROTOCOL").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_RUNTIMEPROPERTY > ROLE_ENTITY_READ_RUNTIMEPROPERTY").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_SPECIES > ROLE_ENTITY_READ_SPECIES").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_STRINGVALUE > ROLE_ENTITY_READ_STRINGVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_STUDYDATAREQUEST > ROLE_ENTITY_READ_STUDYDATAREQUEST").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_TEXTVALUE > ROLE_ENTITY_READ_TEXTVALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_VALUE > ROLE_ENTITY_READ_VALUE").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_XREFVALUE > ROLE_ENTITY_READ_XREFVALUE").append(' ');

		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_PROTOCOLVIEWER > ROLE_ENTITY_READ_DATASET").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_READ_PROTOCOLVIEWER > ROLE_ENTITY_READ_DATASET").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_PROTOCOLVIEWER > ROLE_ENTITY_WRITE_STUDYDATAREQUEST").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_READ_PROTOCOLVIEWER > ROLE_ENTITY_READ_STUDYDATAREQUEST").append(' ');

		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_DATASET > ROLE_ENTITY_WRITE_OBSERVATIONSET").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_OBSERVATIONSET > ROLE_ENTITY_WRITE_OBSERVEDVALUE").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_READ_DATASET > ROLE_ENTITY_READ_OBSERVATIONSET").append(' ');
		hierarchyBuilder.append("ROLE_ENTITY_READ_OBSERVATIONSET > ROLE_ENTITY_READ_OBSERVEDVALUE").append(' ');

		RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl();
		roleHierarchyImpl.setHierarchy(hierarchyBuilder.toString());
		return roleHierarchyImpl;
	}
}
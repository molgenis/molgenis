package org.molgenis.omx;

import static org.molgenis.security.SecurityUtils.defaultPluginAuthorities;
import static org.molgenis.security.SecurityUtils.getPluginReadAuthority;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends MolgenisWebAppSecurityConfig
{
	// TODO automate URL authorization configuration (ticket #2133)
	@Override
	protected void configureUrlAuthorization(ExpressionUrlAuthorizationConfigurer<HttpSecurity> euac)
	{
		euac.antMatchers("/")
				.permitAll()

				// main menu
				.antMatchers("/menu/main")
				.hasAnyAuthority(
						defaultPluginAuthorities("home", "protocolviewer", "dataexplorer", "entityexplorer",
								"importwizard", "news", "background", "references", "contact", "useraccount"))

				// main menu plugins
				.antMatchers("/menu/main/home/**", "/plugin/home/**")
				.hasAnyAuthority(defaultPluginAuthorities("home"))

				.antMatchers("/menu/main/protocolviewer/**", "/plugin/protocolviewer/**")
				.hasAnyAuthority(defaultPluginAuthorities("protocolviewer"))

				.antMatchers("/menu/main/dataexplorer/**", "/plugin/dataexplorer/**")
				.hasAnyAuthority(defaultPluginAuthorities("dataexplorer"))

				.antMatchers("/menu/main/entityexplorer/**", "/plugin/entityexplorer/**")
				.hasAnyAuthority(defaultPluginAuthorities("entityexplorer"))

				.antMatchers("/menu/main/importwizard/**", "/plugin/workflowdataentry/**")
				.hasAnyAuthority(defaultPluginAuthorities("workflowdataentry"))

				.antMatchers("/menu/main/importwizard/**", "/plugin/importwizard/**")
				.hasAnyAuthority(defaultPluginAuthorities("importwizard"))

				.antMatchers("/menu/main/news/**", "/plugin/news/**")
				.hasAnyAuthority(defaultPluginAuthorities("news"))

				.antMatchers("/menu/main/background/**", "/plugin/background/**")
				.hasAnyAuthority(defaultPluginAuthorities("background"))

				.antMatchers("/menu/main/references/**", "/plugin/references/**")
				.hasAnyAuthority(defaultPluginAuthorities("references"))

				.antMatchers("/menu/main/contact/**", "/plugin/contact/**")
				.hasAnyAuthority(defaultPluginAuthorities("contact"))

				.antMatchers("/menu/main/useraccount/**", "/plugin/useraccount/**")
				.hasAnyAuthority(defaultPluginAuthorities("useraccount"))

				// converters menu
				.antMatchers("/menu/converters")
				.hasAnyAuthority(defaultPluginAuthorities("cbmtoomxconverter"))

				// converters menu plugins
				.antMatchers("/menu/converters/cbmtoomxconverter/**", "/plugin/cbmtoomxconverter/**")
				.hasAnyAuthority(defaultPluginAuthorities("cbmtoomxconverter"))

				// entities menu
				.antMatchers("/menu/entities")
				.hasAnyAuthority(
						defaultPluginAuthorities("formdataSet", "formprotocol", "formobservablefeature",
								"formcategory", "formstudydatarequest", "formruntimeproperty"))

				// entities menu plugins
				.antMatchers("/menu/entities/form.DataSet", "/plugin/form.DataSet")
				.hasAnyAuthority(defaultPluginAuthorities("formdataSet"))

				.antMatchers("/menu/entities/form.Protocol?subForms=DataSet.ProtocolUsed",
						"/plugin/form.Protocol?subForms=DataSet.ProtocolUsed")
				.hasAnyAuthority(defaultPluginAuthorities("formprotocol"))

				.antMatchers("/menu/entities/form.ObservableFeature?subForms=Category.observableFeature",
						"/plugin/form.ObservableFeature?subForms=Category.observableFeature")
				.hasAnyAuthority(defaultPluginAuthorities("formobservablefeature"))

				.antMatchers("/menu/entities/form.Category", "/plugin/form.Category")
				.hasAnyAuthority(defaultPluginAuthorities("formcategory"))

				.antMatchers("/menu/entities/form.StudyDataRequest", "/plugin/form.StudyDataRequest")
				.hasAnyAuthority(defaultPluginAuthorities("formstudydatarequest"))

				.antMatchers("/menu/entities/form.RuntimeProperty", "/plugin/form.RuntimeProperty")
				.hasAnyAuthority(defaultPluginAuthorities("formruntimeproperty"))

				// values menu
				.antMatchers("/menu/values")
				.hasAnyAuthority(
						defaultPluginAuthorities("formboolvalue", "formcategoricalvalue", "formdatevalue",
								"formdatetimevalue", "formdecimalvalue", "formemailvalue", "formhtmlvalue",
								"formhyperlinkvalue", "formintvalue", "formlongvalue", "formmrefvalue",
								"formstringvalue", "formtextvalue", "formxrefvalue"))

				// values menu plugins
				.antMatchers("/menu/values/form.BoolValue", "/plugin/form.BoolValue")
				.hasAnyAuthority(defaultPluginAuthorities("formboolvalue"))

				.antMatchers("/menu/values/form.CategoricalValue", "/plugin/form.CategoricalValue")
				.hasAnyAuthority(defaultPluginAuthorities("formcategoricalvalue"))

				.antMatchers("/menu/values/form.DateValue", "/plugin/form.DateValue")
				.hasAnyAuthority(defaultPluginAuthorities("formdatevalue"))

				.antMatchers("/menu/values/form.DateTimeValue", "/plugin/form.DateTimeValue")
				.hasAnyAuthority(defaultPluginAuthorities("formdatetimevalue"))

				.antMatchers("/menu/values/form.DecimalValue", "/plugin/form.DecimalValue")
				.hasAnyAuthority(defaultPluginAuthorities("formdecimalvalue"))

				.antMatchers("/menu/values/form.EmailValue", "/plugin/form.EmailValue")
				.hasAnyAuthority(defaultPluginAuthorities("formemailvalue"))

				.antMatchers("/menu/values/form.HtmlValue", "/plugin/form.HtmlValue")
				.hasAnyAuthority(defaultPluginAuthorities("formhtmlvalue"))

				.antMatchers("/menu/values/form.HyperlinkValue", "/plugin/form.HyperlinkValue")
				.hasAnyAuthority(defaultPluginAuthorities("formhyperlinkvalue"))

				.antMatchers("/menu/values/form.IntValue", "/plugin/form.IntValue")
				.hasAnyAuthority(defaultPluginAuthorities("formintvalue"))

				.antMatchers("/menu/values/form.LongValue", "/plugin/form.LongValue")
				.hasAnyAuthority(defaultPluginAuthorities("formlongvalue"))

				.antMatchers("/menu/values/form.MrefValue", "/plugin/form.MrefValue")
				.hasAnyAuthority(defaultPluginAuthorities("formmrefvalue"))

				.antMatchers("/menu/values/form.StringValue", "/plugin/form.StringValue")
				.hasAnyAuthority(defaultPluginAuthorities("formstringvalue"))

				.antMatchers("/menu/values/form.TextValue", "/plugin/form.TextValue")
				.hasAnyAuthority(defaultPluginAuthorities("formtextvalue"))

				.antMatchers("/menu/values/form.XrefValue", "/plugin/form.XrefValue")
				.hasAnyAuthority(defaultPluginAuthorities("formxrefvalue"))

				// admin menu
				.antMatchers("/menu/admin")
				.hasAnyAuthority(
						defaultPluginAuthorities("permissionmanager", "catalogmanager", "studymanager", "dataindexer",
								"datasetdeleter"))

				// admin menu plugins
				.antMatchers("/menu/admin/permissionmanager/**", "/plugin/permissionmanager/**")
				.hasAnyAuthority(defaultPluginAuthorities("permissionmanager"))

				.antMatchers("/menu/admin/catalogmanager/**", "/plugin/catalogmanager/**")
				.hasAnyAuthority(defaultPluginAuthorities("catalogmanager"))

				.antMatchers("/menu/admin/studymanager/**", "/plugin/studymanager/**")
				.hasAnyAuthority(defaultPluginAuthorities("studymanager"))

				.antMatchers("/menu/admin/dataindexer/**", "/plugin/dataindexer/**")
				.hasAnyAuthority(defaultPluginAuthorities("dataindexer"))

				.antMatchers("/menu/admin/datasetdeleter/**", "/plugin/datasetdeleter/**")
				.hasAnyAuthority(defaultPluginAuthorities("datasetdeleter"))

				// protocol viewer plugin dependencies
				.antMatchers("/plugin/study/**").hasAnyAuthority(defaultPluginAuthorities("protocolviewer"))

				.antMatchers("/cart/**").hasAnyAuthority(defaultPluginAuthorities("protocolviewer"));
	}

	@Override
	protected List<GrantedAuthority> createAnonymousUserAuthorities()
	{
		return AuthorityUtils.createAuthorityList(getPluginReadAuthority("home"));
	}

	// TODO automate role hierarchy configuration (ticket #2134)
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
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_CATALOGMANAGER > ROLE_PLUGIN_READ_CATALOGMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_STUDYMANAGER > ROLE_PLUGIN_READ_STUDYMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_DATASETINDEXER > ROLE_PLUGIN_READ_DATASETINDEXER").append(' '); // why
																													// datasetindexer
																													// instead
																													// of
																													// dataindexer?
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_DATASETDELETER > ROLE_PLUGIN_READ_DATASETDELETER").append(' ');

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
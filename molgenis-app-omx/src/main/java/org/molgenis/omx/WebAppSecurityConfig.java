package org.molgenis.omx;

import static org.molgenis.security.SecurityUtils.getPluginReadAuthority;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.security.MolgenisWebAppSecurityConfig;
import org.molgenis.security.permission.MolgenisAccessDecisionVoter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.expression.WebExpressionVoter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAppSecurityConfig extends MolgenisWebAppSecurityConfig
{
	private static final Logger logger = Logger.getLogger(WebAppSecurityConfig.class);

	@Autowired
	private MolgenisAccessDecisionVoter molgenisAccessDecisionVoter;

	@Autowired
	private RoleVoter roleVoter;

	// TODO automate URL authorization configuration (ticket #2133)
	@Override
	protected void configureUrlAuthorization(
			ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry)
	{
		@SuppressWarnings("rawtypes")
		List<AccessDecisionVoter> listOfVoters = new ArrayList<AccessDecisionVoter>();
		listOfVoters.add(new WebExpressionVoter());
		listOfVoters.add(new MolgenisAccessDecisionVoter());
		expressionInterceptUrlRegistry.accessDecisionManager(new AffirmativeBased(listOfVoters));

		expressionInterceptUrlRegistry.antMatchers("/").permitAll()
		// DAS datasource uses the database, unautheticated users can
		// not see any data
				.antMatchers("/das/**").permitAll()

				.antMatchers("/myDas/**").permitAll()

				.antMatchers("/charts/**").authenticated();
	}

	@Override
	protected List<GrantedAuthority> createAnonymousUserAuthorities()
	{
		String s = getPluginReadAuthority("home");
		return AuthorityUtils.createAuthorityList(s);
	}

	// TODO automate role hierarchy configuration (ticket #2134)
	@Override
	public RoleHierarchy roleHierarchy()
	{
		StringBuilder hierarchyBuilder = new StringBuilder();

		// Plugins: WRITE -> READ
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_CATALOGMANAGER > ROLE_PLUGIN_READ_CATALOGMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_CBMTOOMXCONVERTER > ROLE_PLUGIN_READ_CBMTOOMXCONVERTER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_DATAEXPLORER > ROLE_PLUGIN_READ_DATAEXPLORER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_ENTITYEXPLORER > ROLE_PLUGIN_READ_ENTITYEXPLORER").append(' ');
		// TODO add form plugins
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_HOME > ROLE_PLUGIN_READ_HOME").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_NEWS > ROLE_PLUGIN_READ_NEWS").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_CONTACT > ROLE_PLUGIN_READ_CONTACT").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_BACKGROUND > ROLE_PLUGIN_READ_BACKGROUND").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_REFERENCES > ROLE_PLUGIN_READ_REFERENCES").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_IMPORTWIZARD > ROLE_PLUGIN_READ_IMPORTWIZARD").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_PERMISSIONMANAGER > ROLE_PLUGIN_READ_PERMISSIONMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_USERMANAGER > ROLE_PLUGIN_READ_USERMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_PROTOCOLMANAGER > ROLE_PLUGIN_READ_PROTOCOLMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_PROTOCOLVIEWER > ROLE_PLUGIN_READ_PROTOCOLVIEWER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_STUDY > ROLE_PLUGIN_READ_STUDY").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_STUDYMANAGER > ROLE_PLUGIN_READ_STUDYMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_USERACCOUNT > ROLE_PLUGIN_READ_USERACCOUNT").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_VOID > ROLE_PLUGIN_READ_VOID").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_CATALOGMANAGER > ROLE_PLUGIN_READ_CATALOGMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_STUDYMANAGER > ROLE_PLUGIN_READ_STUDYMANAGER").append(' ');
		hierarchyBuilder.append("ROLE_PLUGIN_WRITE_DATAINDEXER > ROLE_PLUGIN_READ_DATAINDEXER").append(' ');
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

		// System entity
		hierarchyBuilder.append("ROLE_ENTITY_WRITE_MOLGENISUSER > ROLE_ENTITY_READ_MOLGENISUSER").append(' ');

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

	@Bean
	public MolgenisAccessDecisionVoter molgenisAccessDecisionVoter()
	{
		return new MolgenisAccessDecisionVoter();
	}
}
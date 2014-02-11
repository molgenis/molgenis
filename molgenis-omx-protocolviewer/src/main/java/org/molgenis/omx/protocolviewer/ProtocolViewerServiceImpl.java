package org.molgenis.omx.protocolviewer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogItem;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.CatalogService;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Writable;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.utils.ProtocolUtils;
import org.molgenis.security.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.molgenis.studymanager.StudyManagerService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Service
public class ProtocolViewerServiceImpl implements ProtocolViewerService
{
	private static final Logger logger = Logger.getLogger(ProtocolViewerServiceImpl.class);
	@Autowired
	@Qualifier("catalogService")
	private CatalogService catalogService;
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private MolgenisSettings molgenisSettings;
	@Autowired
	private FileStore fileStore;
	@Autowired
	private StudyManagerService studyManagerService;
	@Autowired
	private MolgenisUserService molgenisUserService;
	@Autowired
	private DataService dataService;

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_PROTOCOLVIEWER')")
	@Transactional(readOnly = true)
	public Iterable<CatalogMeta> getCatalogs()
	{
		return Iterables.filter(catalogService.getCatalogs(), new Predicate<CatalogMeta>()
		{
			@Override
			public boolean apply(CatalogMeta catalogMeta)
			{
				try
				{
					return catalogService.isCatalogLoaded(catalogMeta.getId());
				}
				catch (UnknownCatalogException e)
				{
					logger.error(e);
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_PROTOCOLVIEWER')")
	@Transactional(readOnly = true)
	public StudyDefinition getStudyDefinitionDraftForCurrentUser(String catalogId) throws UnknownCatalogException
	{
		List<StudyDefinition> studyDefinitions = studyManagerService.getStudyDefinitions(
				SecurityUtils.getCurrentUsername(), StudyDefinition.Status.DRAFT);
		for (StudyDefinition studyDefinition : studyDefinitions)
		{
			Catalog catalogOfStudyDefinition;
			try
			{
				catalogOfStudyDefinition = catalogService.getCatalogOfStudyDefinition(studyDefinition.getId());
			}
			catch (UnknownCatalogException e)
			{
				logger.error("", e);
				throw new RuntimeException(e);
			}
			catch (UnknownStudyDefinitionException e)
			{
				logger.error("", e);
				throw new RuntimeException(e);
			}
			if (catalogOfStudyDefinition.getId().equals(catalogId))
			{
				return studyDefinition;
			}
		}
		return null;
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_PROTOCOLVIEWER')")
	@Transactional
	public StudyDefinition createStudyDefinitionDraftForCurrentUser(String catalogId) throws UnknownCatalogException
	{
		return studyManagerService.createStudyDefinition(SecurityUtils.getCurrentUsername(), catalogId);
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_PROTOCOLVIEWER')")
	@Transactional(readOnly = true)
	public List<StudyDefinition> getStudyDefinitionsForCurrentUser()
	{
		List<StudyDefinition> studyDefinitions = new ArrayList<StudyDefinition>();
		String username = SecurityUtils.getCurrentUsername();
		for (StudyDefinition.Status status : StudyDefinition.Status.values())
		{
			studyDefinitions.addAll(studyManagerService.getStudyDefinitions(username, status));
		}
		return studyDefinitions;
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_PROTOCOLVIEWER')")
	@Transactional(readOnly = true)
	public StudyDefinition getStudyDefinitionForCurrentUser(Integer id) throws UnknownStudyDefinitionException
	{
		MolgenisUser user = molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
		StudyDefinition studyDefinition = studyManagerService.getStudyDefinition(id.toString());
		if (!studyDefinition.getAuthorEmail().equals(user.getEmail()))
		{
			throw new MolgenisDataAccessException("Access denied to study definition [" + id + "]");
		}
		return studyDefinition;
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_PROTOCOLVIEWER')")
	@Transactional(rollbackFor =
	{ MessagingException.class, IOException.class, UnknownCatalogException.class, UnknownStudyDefinitionException.class })
	public void submitStudyDefinitionDraftForCurrentUser(String studyName, Part requestForm, String catalogId)
			throws MessagingException, IOException, UnknownCatalogException, UnknownStudyDefinitionException
	{
		if (studyName == null) throw new IllegalArgumentException("study name is null");
		if (requestForm == null) throw new IllegalArgumentException("request form is null");

		StudyDefinition studyDefinition = getStudyDefinitionDraftForCurrentUser(catalogId);
		if (studyDefinition == null) throw new UnknownStudyDefinitionException("no study definition draft for user");

		Iterable<CatalogItem> catalogItems = studyDefinition.getItems();
		if (catalogItems == null || !catalogItems.iterator().hasNext())
		{
			throw new IllegalArgumentException("feature list is null or empty");
		}

		// update study definition
		studyDefinition.setName(studyName);
		studyManagerService.updateStudyDefinition(studyDefinition);

		// submit study definition
		studyManagerService.submitStudyDefinition(studyDefinition.getId(), catalogId);

		// create excel attachment for study data request
		String appName = molgenisSettings.getProperty("app.name", "MOLGENIS");
		long timestamp = System.currentTimeMillis();
		String fileName = appName + "-request_" + timestamp + ".doc";
		File orderFile = fileStore.store(requestForm.getInputStream(), fileName);
		String variablesFileName = appName + "-request_" + timestamp + "-variables.xls";
		InputStream variablesIs = createStudyDefinitionXlsStream(studyDefinition);
		File variablesFile = fileStore.store(variablesIs, variablesFileName);

		// send order confirmation to user and admin
		MolgenisUser molgenisUser = molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(molgenisUser.getEmail());
		helper.setBcc(molgenisUserService.getSuEmailAddresses().toArray(new String[]
		{}));
		helper.setSubject("Submission confirmation from " + appName);
		helper.setText(createOrderConfirmationEmailText(appName));
		helper.addAttachment(fileName, new FileSystemResource(orderFile));
		helper.addAttachment(variablesFileName, new FileSystemResource(variablesFile));
		mailSender.send(message);
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_PROTOCOLVIEWER')")
	@Transactional(rollbackFor = UnknownCatalogException.class)
	public void addToStudyDefinitionDraftForCurrentUser(String resourceUri, String catalogId)
			throws UnknownCatalogException
	{
		final Catalog catalog = catalogService.getCatalog(catalogId);

		List<String> catalogItemIds = getCatalogItemIds(resourceUri);

		StudyDefinition studyDefinition = getStudyDefinitionDraftForCurrentUser(catalogId);
		if (studyDefinition == null)
		{
			studyDefinition = createStudyDefinitionDraftForCurrentUser(catalogId);
		}

		Iterable<CatalogItem> catalogItems = Iterables.transform(catalogItemIds, new Function<String, CatalogItem>()
		{
			@Override
			public CatalogItem apply(String catalogItemId)
			{
				return catalog.findItem(catalogItemId);
			}
		});
		studyDefinition.setItems(Iterables.concat(studyDefinition.getItems(), catalogItems));

		try
		{
			studyManagerService.updateStudyDefinition(studyDefinition);
		}
		catch (UnknownStudyDefinitionException e)
		{
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_PROTOCOLVIEWER')")
	@Transactional(rollbackFor = UnknownCatalogException.class)
	public void removeFromStudyDefinitionDraftForCurrentUser(String resourceUri, String catalogId)
			throws UnknownCatalogException
	{
		final Catalog catalog = catalogService.getCatalog(catalogId);

		final Set<String> catalogItemIds = new HashSet<String>(getCatalogItemIds(resourceUri));

		StudyDefinition studyDefinition = getStudyDefinitionDraftForCurrentUser(catalogId);
		if (studyDefinition == null)
		{
			studyDefinition = createStudyDefinitionDraftForCurrentUser(catalogId);
		}

		// verify that items to remove are part of this catalog
		for (String catalogItemId : catalogItemIds)
			catalog.findItem(catalogItemId);

		Iterable<CatalogItem> newCatalogItems = Iterables.filter(studyDefinition.getItems(),
				new Predicate<CatalogItem>()
				{
					@Override
					public boolean apply(CatalogItem catalogItem)
					{
						return !catalogItemIds.contains(catalogItem.getId());
					}
				});
		studyDefinition.setItems(newCatalogItems);

		try
		{
			studyManagerService.updateStudyDefinition(studyDefinition);
		}
		catch (UnknownStudyDefinitionException e)
		{
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_PROTOCOLVIEWER')")
	@Transactional(rollbackFor =
	{ IOException.class, UnknownCatalogException.class })
	public void createStudyDefinitionDraftXlsForCurrentUser(OutputStream outputStream, String catalogId)
			throws IOException, UnknownCatalogException
	{
		StudyDefinition studyDefinition = getStudyDefinitionDraftForCurrentUser(catalogId);
		if (studyDefinition == null) return;
		writeStudyDefinitionXls(studyDefinition, outputStream);
	}

	private String createOrderConfirmationEmailText(String appName)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("Dear Researcher,\n\n");
		strBuilder.append("Thank you for submitting to ").append(appName)
				.append(", attached are the details of your submission.\n");
		strBuilder.append("The ").append(appName)
				.append(" Research Office will contact you upon receiving your application.\n\n");
		strBuilder.append("Sincerely,\n");
		strBuilder.append(appName);
		return strBuilder.toString();
	}

	private InputStream createStudyDefinitionXlsStream(StudyDefinition studyDefinition) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			writeStudyDefinitionXls(studyDefinition, bos);
			return new ByteArrayInputStream(bos.toByteArray());
		}
		finally
		{
			bos.close();
		}
	}

	private void writeStudyDefinitionXls(StudyDefinition studyDefinition, OutputStream outputStream) throws IOException
	{
		if (studyDefinition == null) return;

		// write excel file
		List<String> header = Arrays.asList("Id", "Variable", "Description");

		List<CatalogItem> catalogItems = Lists.newArrayList(studyDefinition.getItems());
		if (catalogItems != null)
		{
			Collections.sort(catalogItems, new Comparator<CatalogItem>()
			{
				@Override
				public int compare(CatalogItem feature1, CatalogItem feature2)
				{
					return feature1.getId().compareTo(feature2.getId());
				}
			});
		}

		ExcelWriter excelWriter = new ExcelWriter(outputStream);
		try
		{
			Writable writable = excelWriter.createWritable("Variables", header);
			try
			{
				if (catalogItems != null)
				{
					for (CatalogItem catalogItem : catalogItems)
					{
						Entity entity = new MapEntity();
						entity.set(header.get(0), catalogItem.getId());
						entity.set(header.get(1), catalogItem.getName());
						entity.set(header.get(2), catalogItem.getDescription());
						writable.add(entity);
					}
				}
			}
			finally
			{
				writable.close();
			}
		}
		finally
		{
			excelWriter.close();
		}
	}

	/**
	 * 
	 * @param resourceUri
	 *            e.g. /api/v1/protocol/123
	 * @return
	 */
	private List<String> getCatalogItemIds(String resourceUri)
	{
		String[] tokens = resourceUri.split("/");
		String entityName = tokens[tokens.length - 2];
		String entityId = tokens[tokens.length - 1];
		if (ObservableFeature.ENTITY_NAME.equalsIgnoreCase(entityName))
		{
			return Arrays.<String> asList(entityId);
		}
		else if (Protocol.ENTITY_NAME.equalsIgnoreCase(entityName))
		{
			Protocol rootProtocol = dataService
					.findOne(Protocol.ENTITY_NAME, Integer.valueOf(entityId), Protocol.class);
			List<String> featureIds = new ArrayList<String>();
			for (Protocol protocol : ProtocolUtils.getProtocolDescendants(rootProtocol))
			{
				for (ObservableFeature feature : protocol.getFeatures())
					featureIds.add(feature.getId().toString());
			}
			return featureIds;
		}
		else
		{
			throw new IllegalArgumentException("invalid entity name [" + entityName + "]");
		}
	}
}

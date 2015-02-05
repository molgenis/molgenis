package org.molgenis.omx.protocolviewer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogFolder;
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
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.molgenis.studymanager.StudyManagerService;
import org.molgenis.util.FileStore;
import org.molgenis.util.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

	@Value("${catalog.mail}")
	private String catalogMail;

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
		List<StudyDefinition> studyDefinitions = studyManagerService.getStudyDefinitions(SecurityUtils
				.getCurrentUsername());

		for (StudyDefinition studyDefinition : studyDefinitions)
		{
			if (studyDefinition.getStatus() == StudyDefinition.Status.DRAFT)
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
		String username = SecurityUtils.getCurrentUsername();
		return studyManagerService.getStudyDefinitions(username);
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_READ_PROTOCOLVIEWER')")
	@Transactional(readOnly = true)
	public StudyDefinition getStudyDefinitionForCurrentUser(Integer id) throws UnknownStudyDefinitionException
	{
		MolgenisUser user = molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
		StudyDefinition studyDefinition = studyManagerService.getStudyDefinition(id.toString());

		if (!studyDefinition.getAuthorEmail().endsWith(user.getEmail()))
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

		Iterable<CatalogFolder> catalogItems = studyDefinition.getItems();
		if (catalogItems == null || !catalogItems.iterator().hasNext())
		{
			throw new IllegalArgumentException("feature list is null or empty");
		}

		String appName = molgenisSettings.getProperty("app.name", "MOLGENIS");
		long timestamp = System.currentTimeMillis();

		String originalFileName = FileUploadUtils.getOriginalFileName(requestForm);
		String extension = StringUtils.getFilenameExtension(originalFileName);
		String fileName = appName + "-request_" + timestamp + "." + extension;
		File orderFile = fileStore.store(requestForm.getInputStream(), fileName);

		// update study definition
		studyDefinition.setName(studyName);
		studyDefinition.setRequestProposalForm(fileName);
		studyManagerService.updateStudyDefinition(studyDefinition);

		// submit study definition
		studyManagerService.submitStudyDefinition(studyDefinition.getId(), catalogId);

		// create excel attachment for study data request
		String variablesFileName = appName + "-request_" + timestamp + "-variables.xls";
		InputStream variablesIs = createStudyDefinitionXlsStream(studyDefinition);
		File variablesFile = fileStore.store(variablesIs, variablesFileName);

		// send order confirmation to user and admin
		MolgenisUser molgenisUser = molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(molgenisUser.getEmail());
		helper.setBcc(catalogMail);
		helper.setSubject("Submission confirmation from " + appName);
		helper.setText(createOrderConfirmationEmailText(appName));
		helper.addAttachment(fileName, new FileSystemResource(orderFile));
		helper.addAttachment(variablesFileName, new FileSystemResource(variablesFile));
		mailSender.send(message);
	}

	@Override
	@PreAuthorize("isAuthenticated() and hasAnyRole('ROLE_SU', 'ROLE_PLUGIN_WRITE_PROTOCOLVIEWER')")
	@Transactional(rollbackFor = UnknownCatalogException.class)
	public void addToStudyDefinitionDraftForCurrentUser(String protocolId, String catalogId)
			throws UnknownCatalogException
	{
		StudyDefinition studyDefinition = getStudyDefinitionDraftForCurrentUser(catalogId);
		if (studyDefinition == null)
		{
			studyDefinition = createStudyDefinitionDraftForCurrentUser(catalogId);
		}

		List<CatalogFolder> orderableItems = ProtocolViewerUtils.findOrderableItems(protocolId, dataService);
		studyDefinition.setItems(Iterables.concat(studyDefinition.getItems(), orderableItems));

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
	public void removeFromStudyDefinitionDraftForCurrentUser(final String protocolId, String catalogId)
			throws UnknownCatalogException
	{
		StudyDefinition studyDefinitionDraftForCurrentUser = getStudyDefinitionDraftForCurrentUser(catalogId);
		final StudyDefinition studyDefinition;
		if (studyDefinitionDraftForCurrentUser == null)
		{
			studyDefinition = createStudyDefinitionDraftForCurrentUser(catalogId);
		}
		else
		{
			studyDefinition = studyDefinitionDraftForCurrentUser;
		}

		// verify that item to remove is part of this catalog
		final List<CatalogFolder> catalogItems = ProtocolViewerUtils.findOrderableItems(protocolId, dataService);
		Iterable<CatalogFolder> newCatalogItems = Iterables.filter(studyDefinition.getItems(),
				new Predicate<CatalogFolder>()
				{
					@Override
					public boolean apply(CatalogFolder catalogItem)
					{
						for (CatalogFolder omxCatalogItem : catalogItems)
						{
							if (catalogItem.getId().equals(omxCatalogItem.getId()))
							{
								return false;
							}
						}
						return true;
					}
				});
		studyDefinition.setItems(newCatalogItems);

		try
		{
			if (Iterables.isEmpty(newCatalogItems))
			{
				// TODO remove StudyDefinition, empty item list is invalid according to the xsd
			}
			else
			{
				studyManagerService.updateStudyDefinition(studyDefinition);
			}
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
	public void createStudyDefinitionDraftXlsForCurrentUser(ExcelWriter writer, String catalogId) throws IOException,
			UnknownCatalogException
	{
		StudyDefinition studyDefinition = getStudyDefinitionDraftForCurrentUser(catalogId);
		if (studyDefinition == null) return;
		writeStudyDefinitionXls(studyDefinition, writer);
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
			writeStudyDefinitionXls(studyDefinition, new ExcelWriter(bos));
			return new ByteArrayInputStream(bos.toByteArray());
		}
		finally
		{
			bos.close();
		}
	}

	void writeStudyDefinitionXls(StudyDefinition studyDefinition, ExcelWriter excelWriter) throws IOException
	{
		if (studyDefinition == null) return;

		// write excel file
		List<String> header = Arrays.asList("Id", "Variable", "Description", "Group");

		List<CatalogFolder> catalogItems = Lists.newArrayList(studyDefinition.getItems());
		if (catalogItems != null)
		{
			Collections.sort(catalogItems, new Comparator<CatalogFolder>()
			{
				@Override
				public int compare(CatalogFolder feature1, CatalogFolder feature2)
				{
					return feature1.getExternalId().compareTo(feature2.getExternalId());
				}
			});
		}

		try
		{
			Writable writable = excelWriter.createWritable("Variables", header);
			try
			{
				if (catalogItems != null)
				{
					for (CatalogFolder catalogItem : catalogItems)
					{
						Entity entity = new MapEntity();
						entity.set(header.get(0), catalogItem.getExternalId());
						entity.set(header.get(1), catalogItem.getName());
						entity.set(header.get(2), catalogItem.getDescription());
						entity.set(header.get(3),
								org.apache.commons.lang3.StringUtils.join(catalogItem.getGroup(), '\u2192'));
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

}

package org.molgenis.rdconnect;

import static java.util.Objects.requireNonNull;
import static org.molgenis.rdconnect.BiobankMetadataController.URI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class BiobankMetadataController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(BiobankMetadataController.class);

	public static final String ID = "biobankmeta";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private final BiobankMetadataService biobankMetadataService;
	private final DataService dataService;

	@Autowired
	public BiobankMetadataController(DataService dataService, BiobankMetadataService biobankMetadataService)
	{
		super(URI);
		this.biobankMetadataService = requireNonNull(biobankMetadataService);
		this.dataService = requireNonNull(dataService);
	}

	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String init(Model model) throws Exception
	{
		return "view-biobankrefresh";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/refresh")
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String refreshMetadata(Model model) throws Exception
	{
		BiobankMetadataService biobankMetadataService = new BiobankMetadataService();
		Map<String, Object> biobankMetadata = biobankMetadataService
				.getBiobankMetadata("http://catalogue.rd-connect.eu/api/jsonws/BiBBoxCommonServices-portlet.logapi/regbb/organization-id/10779");
		System.out.println(biobankMetadata.toString());
		EntityMetaData emd = dataService.getEntityMetaData("regbb");
		MapEntity regbbMapEntity = new MapEntity(emd);

		EntityMetaData emdUrl = dataService.getEntityMetaData("url");

		regbbMapEntity.set("OrganizationID", biobankMetadata.get("OrganizationID"));
		regbbMapEntity.set("type", biobankMetadata.get("type"));

		List<String> urls = (List<String>) biobankMetadata.get("url");
		
		List<MapEntity> list  = new ArrayList();
		for (int i = 0; i < urls.size(); i++)
		{
			String url = urls.get(i);
			MapEntity urlMapEntity = new MapEntity(emdUrl);

			try
			{
				urlMapEntity.set("url", url);
				list.add(urlMapEntity);
				dataService.add("url", urlMapEntity);
			}
			catch (Exception e)
			{
				// DOE NIETS
			}
		}
		
		regbbMapEntity.set("url", list);
		regbbMapEntity.set("title", ((Map<String, Object>) biobankMetadata.get("main contact")).get("title"));

		try
		{
			dataService.add("regbb", regbbMapEntity);
		}
		catch (Exception e)
		{
			dataService.update("regbb", regbbMapEntity);
		}

		//
		// "OrganizationID": 10779,
		// "type": "registry",
		// "also listed in": [],
		// "url": [
		// "https://3q29deletion.patientcrossroads.org/"
		// ],
		// "main contact": {
		// "title": "Welcome Jennifer Mulle!",
		// "first name": "Jennifer",
		// "email": "jmulle@emory.edu",
		// "last name": "Mulle"
		// },
		// "last activities": "Mon Jan 12 19:37:12 GMT 2015",
		// "date of inclusion": "Mon Jan 05 18:02:13 GMT 2015",
		// "address": {
		// "street2": "",
		// "name of host institution": "Department of Epideminology",
		// "zip": "3042",
		// "street1": "1518 Clifton Road",
		// "country": "",
		// "city": "Atlanta"
		// },
		// "name": "3q29 deletion Registry",
		// "ID": "http://catalogue.rd-connect.eu/id/organization-id/10779",
		// "type of host institution": "University/Research Institute",
		// "target population": "National"
		//
		//
		return init(model);
	}

	@ExceptionHandler(value = Throwable.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleThrowable(Throwable t)
	{
		LOG.error("", t);
		return new ErrorMessageResponse(new ErrorMessage(t.getMessage()));
	}
}

package org.molgenis.data.idcard.indexer;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.molgenis.util.ErrorMessageResponse.ErrorMessage;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.idcard.indexer.IdCardIndexerController.URI;
import static org.molgenis.data.idcard.model.IdCardBiobankMetaData.ID_CARD_BIOBANK;

@Controller
@RequestMapping(URI)
public class IdCardIndexerController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(IdCardIndexerController.class);

	public static final String ID = "idcardindexer";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final IdCardIndexerService idCardIndexerService;

	@Autowired
	public IdCardIndexerController(IdCardIndexerService idCardIndexerService)
	{
		super(URI);
		this.idCardIndexerService = requireNonNull(idCardIndexerService);
	}

	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_ENTITY_READ_IDCARDINDEXER')")
	public String init(Model model) throws Exception
	{
		model.addAttribute("id_card_biobank_registry_entity_name", ID_CARD_BIOBANK);
		return "view-idcardbiobankindexer";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/reindex")
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_ENTITY_WRITE_IDCARDINDEXER')")
	@ResponseBody
	public IndexRebuildStatus scheduleIndexRebuild(Model model) throws Exception
	{
		TriggerKey triggerKey = idCardIndexerService.scheduleIndexRebuild();
		TriggerState triggerStatus = idCardIndexerService.getIndexRebuildStatus(triggerKey);
		return new IndexRebuildStatus(triggerKey, triggerStatus);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/status/{triggerGroup}/{triggerName}")
	@PreAuthorize("hasAnyRole('ROLE_SU, ROLE_ENTITY_READ_IDCARDINDEXER')")
	@ResponseBody
	public IndexRebuildStatus getIndexRebuildStatus(@PathVariable String triggerGroup, @PathVariable String triggerName)
			throws Exception
	{
		TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);
		TriggerState triggerStatus = idCardIndexerService.getIndexRebuildStatus(triggerKey);
		return new IndexRebuildStatus(triggerKey, triggerStatus);
	}

	@ExceptionHandler(value = Throwable.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorMessageResponse handleThrowable(Throwable t)
	{
		LOG.error("", t);
		return new ErrorMessageResponse(new ErrorMessage(t.getMessage()));
	}

	private static class IndexRebuildStatus
	{
		private final String triggerName;
		private final String triggerGroup;
		private final String triggerStatus;

		public IndexRebuildStatus(TriggerKey triggerKey, TriggerState triggerStatus)
		{
			this.triggerName = requireNonNull(triggerKey).getName();
			this.triggerGroup = requireNonNull(triggerKey).getGroup();
			this.triggerStatus = requireNonNull(triggerStatus).toString();
		}

		@SuppressWarnings("unused")
		public String getTriggerName()
		{
			return triggerName;
		}

		@SuppressWarnings("unused")
		public String getTriggerGroup()
		{
			return triggerGroup;
		}

		@SuppressWarnings("unused")
		public String getTriggerStatus()
		{
			return triggerStatus;
		}
	}
}

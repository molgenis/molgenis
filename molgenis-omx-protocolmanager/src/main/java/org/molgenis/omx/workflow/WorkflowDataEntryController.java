package org.molgenis.omx.workflow;

import static org.molgenis.omx.workflow.WorkflowDataEntryController.URI;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(URI)
public class WorkflowDataEntryController extends MolgenisPluginController
{
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "workflowdataentry";
	
	private static final Logger logger = Logger.getLogger(WorkflowDataEntryController.class);

	private final WorkflowService workflowService;

	@Autowired
	public WorkflowDataEntryController(WorkflowService workflowService)
	{
		super(URI);
		if(workflowService == null) throw new IllegalArgumentException("WorkflowService is null");
		this.workflowService = workflowService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws Exception
	{
		model.addAttribute("workflows", workflowService.getWorkflows());
		return "view-workflowdataentry";
	}
	
	@RequestMapping(value="/workflowstep/{protocolId}", method = RequestMethod.GET)
	@ResponseBody
	public WorkflowStep getWorkflowStep(@PathVariable Integer protocolId) throws DatabaseException
	{
		List<Protocol> protocols = workflowService.getWorkflowStep(protocolId);
		List<WorkflowProtocol> workflowProtocols = Lists.transform(protocols, new Function<Protocol, WorkflowProtocol>(){
			@Override
			public WorkflowProtocol apply(Protocol protocol) {
				return protocol != null ? new WorkflowProtocol(protocol) : null;
			}});
		return new WorkflowStep(workflowProtocols);
	}
	
	@RequestMapping(value="/protocol/{protocolId}", method = RequestMethod.GET)
	public String getWorkflowProtocol(@PathVariable Integer protocolId, Model model) throws DatabaseException
	{
		model.addAttribute("workflowProtocol", workflowService.getWorkflowProtocol(protocolId));
		return "view-workflowdataentry-pane";
	}
	
	public static class WorkflowStep {
		private final List<WorkflowProtocol> workflowProtocols;

		public WorkflowStep(List<WorkflowProtocol> workflowProtocols) {
			this.workflowProtocols = workflowProtocols;
		}
		
		public List<WorkflowProtocol> getWorkflowProtocols() {
			return workflowProtocols;
		}
	}
	
	public static class WorkflowProtocol {
		@SuppressWarnings("unused")
		private final Integer id;
		@SuppressWarnings("unused")
		private final String name;
		@SuppressWarnings("unused")
		private final List<WorkflowFeature> features;

		public WorkflowProtocol(Protocol protocol) {
			if(protocol == null) throw new IllegalArgumentException("Protocol is null");
			this.id = protocol.getId();
			this.name = protocol.getName();
			this.features = Lists.transform(protocol.getFeatures(), new Function<ObservableFeature, WorkflowFeature>(){
				@Override
				public WorkflowFeature apply(ObservableFeature feature) {
					return feature != null ? new WorkflowFeature(feature) : null;
				}});
		}
	}
	
	public static class WorkflowFeature {
		@SuppressWarnings("unused")
		private final Integer id;
		@SuppressWarnings("unused")
		private final String name;
		
		public WorkflowFeature(ObservableFeature feature) {
			this.id = feature.getId();
			this.name = feature.getName();
		}
	}
}

package org.molgenis.core.ui.jobs;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.jobs.JobsController.URI;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.molgenis.jobs.model.JobExecutionMetaData.SUBMISSION_DATE;
import static org.molgenis.jobs.model.JobExecutionMetaData.USER;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.auth.User;
import org.molgenis.jobs.JobsService;
import org.molgenis.jobs.model.JobExecutionMetaData;
import org.molgenis.jobs.schedule.JobScheduler;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class JobsController extends PluginController {
  public static final String ID = "jobs";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
  private static final int MAX_JOBS_TO_RETURN = 20;

  private final JobsService jobsService;
  private final UserAccountService userAccountService;
  private final DataService dataService;
  private final JobExecutionMetaData jobMetaDataMetaData;
  private final JobScheduler jobScheduler;
  private final MenuReaderService menuReaderService;
  private final UserPermissionEvaluator userPermissionEvaluator;

  JobsController(
      JobsService jobsService,
      UserAccountService userAccountService,
      DataService dataService,
      JobExecutionMetaData jobMetaDataMetaData,
      JobScheduler jobScheduler,
      MenuReaderService menuReaderService,
      UserPermissionEvaluator userPermissionEvaluator) {
    super(URI);
    this.userAccountService = requireNonNull(userAccountService);
    this.dataService = requireNonNull(dataService);
    this.jobMetaDataMetaData = requireNonNull(jobMetaDataMetaData);
    this.jobScheduler = requireNonNull(jobScheduler);
    this.menuReaderService = requireNonNull(menuReaderService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
    this.jobsService = requireNonNull(jobsService);
  }

  @GetMapping
  public String init(Model model) {
    model.addAttribute("username", userAccountService.getCurrentUser().getUsername());
    return "view-jobs";
  }

  @GetMapping("/viewJob")
  public String viewJob(
      Model model,
      @RequestParam(name = "jobHref") String jobHref,
      @RequestParam(name = "refreshTimeoutMillis", defaultValue = "10000")
          Integer refreshTimeoutMillis) {
    model.addAttribute("jobHref", jobHref);
    model.addAttribute("refreshTimeoutMillis", refreshTimeoutMillis);
    return "view-job";
  }

  @GetMapping(value = "/latest", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public List<Entity> findLastJobs() {
    final List<Entity> jobs = new ArrayList<>();

    Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
    User currentUser = userAccountService.getCurrentUser();

    dataService
        .getMeta()
        .getEntityTypes()
        .filter(this::isAllowedJobExecutionEntityType)
        .forEach(
            e -> {
              Query<Entity> q =
                  dataService.query(e.getId()).ge(JobExecutionMetaData.SUBMISSION_DATE, weekAgo);
              if (!currentUser.isSuperuser()) {
                q.and().eq(USER, currentUser.getUsername());
              }
              dataService.findAll(e.getId(), q).forEach(jobs::add);
            });

    jobs.sort(
        (job1, job2) ->
            job2.getInstant(SUBMISSION_DATE).compareTo(job1.getInstant(SUBMISSION_DATE)));
    if (jobs.size() > MAX_JOBS_TO_RETURN) {
      return jobs.subList(0, MAX_JOBS_TO_RETURN);
    }

    return jobs;
  }

  @PostMapping("/cancel/{jobExecutionType}/{jobExecutionId}")
  @ResponseStatus(NO_CONTENT)
  public void cancel(
      @PathVariable("jobExecutionType") String jobExecutionType,
      @PathVariable("jobExecutionId") String jobExecutionId) {
    jobsService.cancel(jobExecutionType, jobExecutionId);
  }

  @PostMapping("/run/{scheduledJobId}")
  @ResponseStatus(NO_CONTENT)
  public void runNow(@PathVariable("scheduledJobId") String scheduledJobId) {
    jobScheduler.runNow(scheduledJobId);
  }

  public String createJobExecutionViewHref(String jobHref, int refreshTimeoutMillis) {
    String jobControllerURL = menuReaderService.findMenuItemPath(ID);
    return format(
        "%s/viewJob/?jobHref=%s&refreshTimeoutMillis=%s",
        jobControllerURL, jobHref, refreshTimeoutMillis);
  }

  /** Package-private for testability. */
  boolean isAllowedJobExecutionEntityType(EntityType entityType) {
    return isJobExecutionEntityType(entityType) && currentUserCanReadEntityTypeData(entityType);
  }

  private boolean isJobExecutionEntityType(EntityType entityType) {
    EntityType parentEntityType = entityType.getExtends();
    return parentEntityType != null && parentEntityType.getId().equals(jobMetaDataMetaData.getId());
  }

  private boolean currentUserCanReadEntityTypeData(EntityType entityType) {
    return userPermissionEvaluator.hasPermission(new EntityTypeIdentity(entityType), READ_DATA);
  }
}

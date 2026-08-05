package com.epam.reportportal.extension.azure.command;

import static com.epam.reportportal.extension.azure.AzureExtension.PROJECT;
import static com.epam.reportportal.extension.azure.AzureExtension.URL;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TicketRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.azure.client.AzureApiClientProvider;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.api.WorkItemsApi;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItem;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetIssueCommand extends AbstractExtensionCommand<Ticket> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetIssueCommand.class);
  private static final String API_VERSION = "6.0";
  private static final String TICKET_ID_PARAM = "ticketId";
  private static final String PROJECT_ID_PARAM = "projectId";

  private final AzureApiClientProvider clientProvider;
  private final TicketRepository ticketRepository;
  private final IntegrationRepository integrationRepository;

  public GetIssueCommand(AzureApiClientProvider clientProvider, TicketRepository ticketRepository,
      IntegrationRepository integrationRepository, ProjectRepository projectRepository,
      OrganizationUserRepository organizationUserRepository,
      OrganizationRepository organizationRepository, ProjectUserRepository projectUserRepository) {
    super(projectRepository, organizationUserRepository, organizationRepository, projectUserRepository);
    this.clientProvider = clientProvider;
    this.ticketRepository = ticketRepository;
    this.integrationRepository = integrationRepository;

    this.minProjectRole = ProjectRole.VIEWER;
    this.minOrgRole = OrganizationRole.MEMBER;
    this.minUserRole = UserRole.USER;
  }

  @Override
  public String getName() {
    return "getIssue";
  }

  @Override
  public Ticket executeCommand(PluginCommandRQ pluginCommandRq) {
    var params = pluginCommandRq.getArguments();

    var ticketId = Optional.ofNullable(params.get(TICKET_ID_PARAM))
        .map(String::valueOf)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            TICKET_ID_PARAM + " must be provided"
        ));
    ticketRepository.findByTicketId(ticketId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Ticket not found with id " + ticketId
        ));

    final Long projectId = (Long) Optional.ofNullable(params.get(PROJECT_ID_PARAM))
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            PROJECT_ID_PARAM + " must be provided"
        ));

    final String btsUrl = Optional.ofNullable(params.get(URL))
        .map(String::valueOf)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Url is not specified."
        ));
    final String btsProject = Optional.ofNullable(params.get(PROJECT))
        .map(String::valueOf)
        .orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            "Project is not specified."
        ));

    final Integration integration =
        integrationRepository.findProjectBtsByUrlAndLinkedProject(btsUrl, btsProject, projectId)
            .orElseGet(() -> integrationRepository.findGlobalBtsByUrlAndLinkedProject(btsUrl, btsProject)
                .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
                    "Integration with provided url and project isn't found"
                )));
    return getTicket(ticketId, integration);
  }

  protected WorkItemsApi buildWorkItemsApi(ApiClient client) {
    return new WorkItemsApi(client);
  }

  private Ticket getTicket(String ticketId, Integration integration) {
    ApiClient client = clientProvider.provide(integration);
    String orgName = clientProvider.extractOrganizationName(client,
        integration.getParams().getParams().get(URL).toString()
    );
    String projectName = integration.getParams().getParams().get(PROJECT).toString();

    WorkItemsApi api = buildWorkItemsApi(client);
    try {
      WorkItem workItem = api.workItemsGetWorkItem(orgName, Integer.valueOf(ticketId), projectName,
          API_VERSION, null, null, null
      );
      return toTicket(workItem);
    } catch (ApiException e) {
      LOGGER.error("Unable to load ticket: {}", e.getMessage(), e);
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
          "Unable to load ticket from Azure DevOps"
      );
    }
  }

  private Ticket toTicket(WorkItem workItem) {
    Ticket ticket = new Ticket();
    String id = workItem.getId().toString();
    String url = workItem.getUrl().substring(0, workItem.getUrl().lastIndexOf(id))
        .replace("apis/wit/", "") + "edit/" + id;
    ticket.setId(id);
    ticket.setTicketUrl(url);
    ticket.setStatus(workItem.getFields().get("System.State").toString());
    ticket.setSummary(workItem.getFields().get("System.Title").toString());
    return ticket;
  }
}

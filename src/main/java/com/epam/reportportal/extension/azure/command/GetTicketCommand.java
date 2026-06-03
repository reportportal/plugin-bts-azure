package com.epam.reportportal.extension.azure.command;

import static com.epam.reportportal.extension.azure.AzureExtension.PROJECT;
import static com.epam.reportportal.extension.azure.AzureExtension.URL;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
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

public class GetTicketCommand extends AbstractExtensionCommand<Optional<Ticket>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTicketCommand.class);
  private static final String API_VERSION = "6.0";
  private static final String TICKET_ID_PARAM = "ticketId";

  private final AzureApiClientProvider clientProvider;

  public GetTicketCommand(AzureApiClientProvider clientProvider,
      ProjectRepository projectRepository, OrganizationRepositoryCustom organizationRepository) {
    super(projectRepository, organizationRepository);
    this.clientProvider = clientProvider;
  }

  @Override
  public String getName() {
    return "getTicket";
  }

  protected WorkItemsApi buildWorkItemsApi(ApiClient client) {
    return new WorkItemsApi(client);
  }

  @Override
  protected Optional<Ticket> invokeCommand(Integration integration, PluginCommandRQ rq) {
    String ticketId = (String) Optional.ofNullable(rq.getArguments().get(TICKET_ID_PARAM))
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            TICKET_ID_PARAM + " must be provided"
        ));

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
      return Optional.of(toTicket(workItem));
    } catch (ApiException e) {
      LOGGER.error("Unable to load ticket: {}", e.getMessage(), e);
      return Optional.empty();
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

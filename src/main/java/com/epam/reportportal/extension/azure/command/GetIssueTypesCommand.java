package com.epam.reportportal.extension.azure.command;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.reportportal.extension.azure.AzureExtension.PROJECT;
import static com.epam.reportportal.extension.azure.AzureExtension.URL;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.azure.client.AzureApiClientProvider;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.api.WorkItemTypesApi;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemType;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetIssueTypesCommand extends AbstractExtensionCommand<List<String>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetIssueTypesCommand.class);
  private static final String API_VERSION = "6.0";

  private final AzureApiClientProvider clientProvider;

  public GetIssueTypesCommand(AzureApiClientProvider clientProvider,
      ProjectRepository projectRepository, OrganizationRepositoryCustom organizationRepository) {
    super(projectRepository, organizationRepository);
    this.clientProvider = clientProvider;
  }

  @Override
  public String getName() {
    return "getIssueTypes";
  }

  protected WorkItemTypesApi buildWorkItemTypesApi(ApiClient client) {
    return new WorkItemTypesApi(client);
  }

  @Override
  protected List<String> invokeCommand(Integration integration, PluginCommandRQ rq) {
    ApiClient client = clientProvider.provide(integration);
    String orgName = clientProvider.extractOrganizationName(client,
        integration.getParams().getParams().get(URL).toString()
    );
    String projectName = integration.getParams().getParams().get(PROJECT).toString();

    WorkItemTypesApi api = buildWorkItemTypesApi(client);
    try {
      List<WorkItemType> types = api.workItemTypesList(orgName, projectName, API_VERSION);
      return types.stream().map(WorkItemType::getName).collect(Collectors.toList());
    } catch (ApiException e) {
      LOGGER.error("Unable to load issue types: {}", e.getMessage(), e);
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
          String.format("Unable to load issue types. Code: %s", e.getCode()), e
      );
    }
  }
}

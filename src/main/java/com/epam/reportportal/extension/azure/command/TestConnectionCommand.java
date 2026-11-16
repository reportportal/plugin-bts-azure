package com.epam.reportportal.extension.azure.command;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.reportportal.extension.azure.AzureExtension.PROJECT;
import static com.epam.reportportal.extension.azure.AzureExtension.URL;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.azure.client.AzureApiClientProvider;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.ApiResponse;
import com.epam.reportportal.extension.azure.rest.client.api.ProjectsApi;
import com.epam.reportportal.extension.azure.rest.client.model.TeamProject;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConnectionCommand extends AbstractExtensionCommand<Boolean> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestConnectionCommand.class);
  private static final String API_VERSION = "6.0";

  private final AzureApiClientProvider clientProvider;

  public TestConnectionCommand(AzureApiClientProvider clientProvider,
      ProjectRepository projectRepository, OrganizationUserRepository organizationUserRepository,
      OrganizationRepository organizationRepository, ProjectUserRepository projectUserRepository) {
    super(projectRepository, organizationUserRepository, organizationRepository,
        projectUserRepository
    );
    this.clientProvider = clientProvider;
  }

  @Override
  public String getName() {
    return "testConnection";
  }

  @Override
  protected Boolean invokeCommand(Integration integration, PluginCommandRQ rq) {
    ApiClient client = clientProvider.provide(integration);
    String orgName = clientProvider.extractOrganizationName(client,
        integration.getParams().getParams().get(URL).toString()
    );
    String projectName = integration.getParams().getParams().get(PROJECT).toString();

    ProjectsApi projectsApi = new ProjectsApi(client);
    try {
      ApiResponse<TeamProject> response =
          projectsApi.projectsGetWithHttpInfo(orgName, projectName, API_VERSION, false, false);
      return response.getStatusCode() == 200;
    } catch (ApiException e) {
      LOGGER.error("Unable to connect to Azure DevOps: {}", e.getMessage(), e);
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
          String.format("Unable to connect to Azure DevOps. Code: %s", e.getCode()), e
      );
    }
  }
}

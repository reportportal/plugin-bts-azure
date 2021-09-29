package com.epam.reportportal.extension.azure.command.connection;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.ApiResponse;
import com.epam.reportportal.extension.azure.rest.client.Configuration;
import com.epam.reportportal.extension.azure.rest.client.api.ProjectsApi;
import com.epam.reportportal.extension.azure.rest.client.auth.HttpBasicAuth;
import com.epam.reportportal.extension.azure.rest.client.model.TeamProject;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.epam.reportportal.extension.azure.AzureExtension.*;

public class TestConnectionCommand implements PluginCommand<Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestConnectionCommand.class);
    private static final String API_VERSION = "6.0";

    @Override
    public Boolean executeCommand(Integration integration, Map<String, Object> params) {

        ApiClient defaultClient = Configuration.getDefaultApiClient();

        String organizationUrl = params.get(URL).toString();
        String organizationName = organizationUrl.replace(defaultClient.getBasePath(), "");
        String projectName = params.get(PROJECT).toString();
        String personalAccessToken = params.get(OAUTH_ACCESS_KEY).toString();

        HttpBasicAuth basicAuth = (HttpBasicAuth) defaultClient.getAuthentication("accessToken");
        basicAuth.setPassword(personalAccessToken);

        ProjectsApi projectsApi = new ProjectsApi(defaultClient);

        try {
            ApiResponse<TeamProject> response = projectsApi.projectsGetWithHttpInfo(organizationName, projectName,
                    API_VERSION, false, false);
            return response.getStatusCode() == 200;
        } catch (ApiException e) {
            LOGGER.error("Unable to connect to Azure DevOps: " + e.getMessage(), e);
            throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
                    String.format("Unable to connect to Azure DevOps. Code: %s, Message: %s", e.getCode(),
                            e.getMessage()), e);
        }
    }
}

package com.epam.reportportal.extension.azure.client;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.Configuration;
import com.epam.reportportal.extension.azure.rest.client.auth.HttpBasicAuth;
import com.epam.reportportal.extension.bugtracking.BtsConstants;
import org.jasypt.util.text.BasicTextEncryptor;

public class AzureApiClientProvider {

  private static final String AUTH_NAME = "accessToken";

  private final BasicTextEncryptor textEncryptor;

  public AzureApiClientProvider(BasicTextEncryptor textEncryptor) {
    this.textEncryptor = textEncryptor;
  }

  public ApiClient provide(Integration integration) {
    String personalAccessToken = textEncryptor.decrypt(
        BtsConstants.OAUTH_ACCESS_KEY.getParam(integration.getParams(), String.class)
            .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
                "OAUTH key cannot be NULL"
            )));
    ApiClient client = Configuration.getDefaultApiClient();
    HttpBasicAuth basicAuth = (HttpBasicAuth) client.getAuthentication(AUTH_NAME);
    basicAuth.setPassword(personalAccessToken);
    return client;
  }

  public String extractOrganizationName(ApiClient client, String organizationUrl) {
    return organizationUrl.replace(client.getBasePath(), "");
  }
}

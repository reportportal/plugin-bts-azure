package com.epam.reportportal.extension.azure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.command.ExtensionCommand;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AzureExtensionTest {

  @InjectMocks
  private AzureExtension azureExtension = new AzureExtension(new HashMap<>());

  @Test
  void getPluginParams() {
    Map<String, ?> params = azureExtension.getPluginParams();
    assertEquals("https://reportportal.io/docs/plugins/AzureDevOps/", params.get("documentationLink"));
    assertEquals("Azure DevOps", params.get("name"));
    assertTrue(params.containsKey(AzureExtension.ALLOWED_COMMANDS));
    assertTrue(params.containsKey(AzureExtension.COMMON_COMMANDS));
  }

  @Test
  void getIntegrationGroup() {
    assertEquals(IntegrationGroupEnum.BTS, azureExtension.getIntegrationGroup());
  }

  @Test
  void getIntegrationCommandReturnsNull() {
    assertNull(azureExtension.getIntegrationCommand("testConnection"));
  }

  @Test
  void getCommonCommandReturnsNull() {
    assertNull(azureExtension.getCommonCommand("getTicket"));
  }

  @Test
  void integrationExtensionCommandsContainsExpectedCommands() {
    Map<String, ExtensionCommand<?>> commands = azureExtension.getIntegrationExtensionCommands();
    assertTrue(commands.containsKey("testConnection"));
    assertTrue(commands.containsKey("getIssueTypes"));
    assertTrue(commands.containsKey("getIssueFields"));
    assertTrue(commands.containsKey("postTicket"));
  }

  @Test
  void commonExtensionCommandsContainsExpectedCommands() {
    Map<String, ExtensionCommand<?>> commands = azureExtension.getCommonExtensionCommands();
    assertTrue(commands.containsKey("getTicket"));
  }

  @Test
  void pluginParamsAllowedCommandsMatchIntegrationCommands() {
    Map<String, ?> params = azureExtension.getPluginParams();
    List<?> allowedCommands = (List<?>) params.get(AzureExtension.ALLOWED_COMMANDS);
    Map<String, ExtensionCommand<?>> integrationCommands =
        azureExtension.getIntegrationExtensionCommands();
    assertEquals(integrationCommands.keySet().size(), allowedCommands.size());
    assertTrue(allowedCommands.containsAll(integrationCommands.keySet()));
  }
}

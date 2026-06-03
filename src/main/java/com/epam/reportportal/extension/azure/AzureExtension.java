package com.epam.reportportal.extension.azure;

import com.epam.reportportal.base.core.events.domain.PluginUploadedEvent;
import com.epam.reportportal.base.infrastructure.persistence.binary.impl.AttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataEncoder;
import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.NamedPluginCommand;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.azure.client.AzureApiClientProvider;
import com.epam.reportportal.extension.azure.command.GetIssueTypesCommand;
import com.epam.reportportal.extension.azure.command.GetTicketCommand;
import com.epam.reportportal.extension.azure.command.GetTicketFieldsCommand;
import com.epam.reportportal.extension.azure.command.PostTicketCommand;
import com.epam.reportportal.extension.azure.command.TestConnectionCommand;
import com.epam.reportportal.extension.azure.event.plugin.PluginLoadedEventListener;
import com.epam.reportportal.extension.azure.info.impl.PluginInfoProviderImpl;
import com.epam.reportportal.extension.azure.utils.MemoizingSupplier;
import com.epam.reportportal.extension.command.ExtensionCommand;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.jasypt.util.text.BasicTextEncryptor;
import org.pf4j.Extension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Extension
public class AzureExtension implements ReportPortalExtensionPoint, DisposableBean {

  public static final String BINARY_DATA_PROPERTIES_FILE_ID = "azure-binary-data.properties";
  public static final String SCHEMA_SCRIPTS_DIR = "schema";

  public static final String URL = "url";
  public static final String PROJECT = "project";
  public static final String OAUTH_ACCESS_KEY = "oauthAccessKey";

  private static final String DOCUMENTATION_LINK_FIELD = "documentationLink";
  private static final String DOCUMENTATION_LINK =
      "https://reportportal.io/docs/plugins/AzureDevOps/";
  private static final String PLUGIN_ID = "Azure DevOps";
  private static final String PLUGIN_NAME_FIELD = "name";
  private static final String PLUGIN_NAME = "Azure DevOps";

  private final String resourcesDir;
  private final Supplier<AzureApiClientProvider> clientProvider;

  private final Supplier<Map<String, ExtensionCommand<?>>> pluginCommandMapping =
      new MemoizingSupplier<>(this::getIntegrationExtensionCommands);
  private final Supplier<Map<String, ExtensionCommand<?>>> commonPluginCommandMapping =
      new MemoizingSupplier<>(this::getCommonExtensionCommands);

  private final Supplier<ApplicationListener<PluginUploadedEvent>> pluginLoadedListenerSupplier;

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private IntegrationTypeRepository integrationTypeRepository;

  @Autowired
  private IntegrationRepository integrationRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private OrganizationRepositoryCustom organizationRepository;

  @Autowired
  private TestItemRepository itemRepository;

  @Autowired
  private AttachmentDataStoreService attachmentDataStoreService;

  @Autowired
  private DataEncoder dataEncoder;

  @Autowired
  private BasicTextEncryptor basicTextEncryptor;

  @Autowired
  private LogRepository logRepository;

  public AzureExtension(Map<String, Object> initParams) {
    resourcesDir =
        IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams).map(String::valueOf)
            .orElse("");
    clientProvider = new MemoizingSupplier<>(() -> new AzureApiClientProvider(basicTextEncryptor));
    pluginLoadedListenerSupplier = new MemoizingSupplier<>(
        () -> new PluginLoadedEventListener(PLUGIN_ID, integrationTypeRepository,
            integrationRepository,
            new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID)
        ));
  }

  @Override
  public Map<String, ?> getPluginParams() {
    Map<String, Object> params = new HashMap<>();
    params.put(DOCUMENTATION_LINK_FIELD, DOCUMENTATION_LINK);
    params.put(PLUGIN_NAME_FIELD, PLUGIN_NAME);
    params.put(ALLOWED_COMMANDS, new ArrayList<>(pluginCommandMapping.get().keySet()));
    params.put(COMMON_COMMANDS, new ArrayList<>(commonPluginCommandMapping.get().keySet()));
    return params;
  }

  @Override
  public CommonPluginCommand<?> getCommonCommand(String commandName) {
    return null;
  }

  @Override
  public PluginCommand<?> getIntegrationCommand(String commandName) {
    return null;
  }

  @Override
  public Map<String, ExtensionCommand<?>> getIntegrationExtensionCommands() {
    List<ExtensionCommand<?>> commands = new ArrayList<>();
    commands.add(new TestConnectionCommand(clientProvider.get(), projectRepository, organizationRepository));
    commands.add(new GetIssueTypesCommand(clientProvider.get(), projectRepository, organizationRepository));
    commands.add(new GetTicketFieldsCommand(clientProvider.get(), projectRepository, organizationRepository));
    commands.add(new PostTicketCommand(clientProvider.get(), itemRepository, logRepository,
        attachmentDataStoreService, dataEncoder, projectRepository, organizationRepository
    ));
    return commands.stream()
        .collect(Collectors.toMap(NamedPluginCommand::getName, command -> command));
  }

  @Override
  public Map<String, ExtensionCommand<?>> getCommonExtensionCommands() {
    List<ExtensionCommand<?>> commands = new ArrayList<>();
    commands.add(new GetTicketCommand(clientProvider.get(), projectRepository, organizationRepository));
    return commands.stream()
        .collect(Collectors.toMap(NamedPluginCommand::getName, command -> command));
  }

  @Override
  public IntegrationGroupEnum getIntegrationGroup() {
    return IntegrationGroupEnum.BTS;
  }

  @PostConstruct
  public void createIntegration() throws IOException {
    initListeners();
    initSchema();
  }

  @Override
  public void destroy() {
    removeListeners();
  }

  private void initListeners() {
    ApplicationEventMulticaster multicaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class
    );
    multicaster.addApplicationListener(pluginLoadedListenerSupplier.get());
  }

  private void initSchema() throws IOException {
    try (Stream<Path> paths = Files.list(Paths.get(resourcesDir, SCHEMA_SCRIPTS_DIR))) {
      FileSystemResource[] scripts =
          paths.sorted().map(FileSystemResource::new).toArray(FileSystemResource[]::new);
      new ResourceDatabasePopulator(scripts).execute(dataSource);
    }
  }

  private void removeListeners() {
    ApplicationEventMulticaster multicaster = applicationContext.getBean(
        AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
        ApplicationEventMulticaster.class
    );
    multicaster.removeApplicationListener(pluginLoadedListenerSupplier.get());
  }
}

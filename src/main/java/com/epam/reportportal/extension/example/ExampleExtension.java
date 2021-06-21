package com.epam.reportportal.extension.example;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.event.StartLaunchEvent;
import com.epam.reportportal.extension.example.command.binary.GetFileCommand;
import com.epam.reportportal.extension.example.command.entity.CreateEntityCommand;
import com.epam.reportportal.extension.example.command.entity.DeleteEntityCommand;
import com.epam.reportportal.extension.example.command.entity.GetProjectEntities;
import com.epam.reportportal.extension.example.command.utils.RequestEntityConverter;
import com.epam.reportportal.extension.example.dao.EntityRepository;
import com.epam.reportportal.extension.example.dao.impl.EntityRepositoryImpl;
import com.epam.reportportal.extension.example.event.launch.ExampleStartLaunchEventListener;
import com.epam.reportportal.extension.example.event.plugin.ExamplePluginEventListener;
import com.epam.reportportal.extension.example.event.plugin.PluginEventHandlerFactory;
import com.epam.reportportal.extension.example.info.impl.PluginInfoProviderImpl;
import com.epam.reportportal.extension.example.service.EntityService;
import com.epam.reportportal.extension.example.utils.MemoizingSupplier;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jooq.DSLContext;
import org.pf4j.Extension;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Extension
public class ExampleExtension implements ReportPortalExtensionPoint, DisposableBean {

	public static final String BINARY_DATA_PROPERTIES_FILE_ID = "example-binary-data.properties";

	public static final String SCHEMA_SCRIPTS_DIR = "schema";

	private static final String PLUGIN_ID = "example";

	private final String resourcesDir;

	private final Supplier<Map<String, PluginCommand<?>>> pluginCommandMapping = new MemoizingSupplier<>(this::getCommands);

	private final ObjectMapper objectMapper;
	private final RequestEntityConverter requestEntityConverter;

	private final Supplier<ApplicationListener<PluginEvent>> pluginLoadedListenerSupplier;
	private final Supplier<ApplicationListener<StartLaunchEvent>> startLaunchEventListenerSupplier;

	private final Supplier<EntityRepository> entityRepositorySupplier;

	private final Supplier<EntityService> entityServiceSupplier;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private DSLContext dsl;

	@Autowired
	private IntegrationTypeRepository integrationTypeRepository;

	@Autowired
	private IntegrationRepository integrationRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private LaunchRepository launchRepository;

	public ExampleExtension(Map<String, Object> initParams) {
		resourcesDir = IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams).map(String::valueOf).orElse("");
		objectMapper = configureObjectMapper();

		pluginLoadedListenerSupplier = new MemoizingSupplier<>(() -> new ExamplePluginEventListener(PLUGIN_ID,
				new PluginEventHandlerFactory(integrationTypeRepository,
						integrationRepository,
						new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID)
				)
		));
		startLaunchEventListenerSupplier = new MemoizingSupplier<>(() -> new ExampleStartLaunchEventListener(launchRepository));

		requestEntityConverter = new RequestEntityConverter(objectMapper);

		entityRepositorySupplier = new MemoizingSupplier<>(() -> new EntityRepositoryImpl(dsl));

		entityServiceSupplier = new MemoizingSupplier<>(() -> new EntityService(entityRepositorySupplier.get()));
	}

	protected ObjectMapper configureObjectMapper() {
		ObjectMapper om = new ObjectMapper();
		om.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
		om.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.registerModule(new JavaTimeModule());
		return om;
	}

	@Override
	public Map<String, ?> getPluginParams() {
		Map<String, Object> params = new HashMap<>();
		params.put(ALLOWED_COMMANDS, new ArrayList<>(pluginCommandMapping.get().keySet()));
		return params;
	}

	@Override
	public PluginCommand<?> getCommandToExecute(String commandName) {
		return pluginCommandMapping.get().get(commandName);
	}

	@PostConstruct
	public void createIntegration() throws IOException {
		initListeners();
		initSchema();
	}

	private void initListeners() {
		ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
				ApplicationEventMulticaster.class
		);
		applicationEventMulticaster.addApplicationListener(pluginLoadedListenerSupplier.get());
		applicationEventMulticaster.addApplicationListener(startLaunchEventListenerSupplier.get());
	}

	private void initSchema() throws IOException {
		try (Stream<Path> paths = Files.list(Paths.get(resourcesDir, SCHEMA_SCRIPTS_DIR))) {
			FileSystemResource[] scriptResources = paths.sorted().map(FileSystemResource::new).toArray(FileSystemResource[]::new);
			ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator(scriptResources);
			resourceDatabasePopulator.execute(dataSource);
		}
	}

	@Override
	public void destroy() {
		removeListeners();
	}

	private void removeListeners() {
		ApplicationEventMulticaster applicationEventMulticaster = applicationContext.getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
				ApplicationEventMulticaster.class
		);
		applicationEventMulticaster.removeApplicationListener(pluginLoadedListenerSupplier.get());
		applicationEventMulticaster.removeApplicationListener(startLaunchEventListenerSupplier.get());
	}

	private Map<String, PluginCommand<?>> getCommands() {
		Map<String, PluginCommand<?>> pluginCommandMapping = new HashMap<>();
		pluginCommandMapping.put("getFile", new GetFileCommand(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID));
		pluginCommandMapping.put("createEntity",
				new CreateEntityCommand(projectRepository, requestEntityConverter, entityServiceSupplier.get())
		);
		pluginCommandMapping.put("getProjectEntities", new GetProjectEntities(projectRepository, entityServiceSupplier.get()));
		pluginCommandMapping.put("deleteEntity", new DeleteEntityCommand(projectRepository, entityServiceSupplier.get()));
		pluginCommandMapping.put("testConnection", (integration, params) -> true);
		return pluginCommandMapping;
	}
}

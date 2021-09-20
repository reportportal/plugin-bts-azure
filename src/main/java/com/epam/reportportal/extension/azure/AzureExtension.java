package com.epam.reportportal.extension.azure;

import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_TO_LOAD_BINARY_DATA;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.azure.command.connection.TestConnectionCommand;
import com.epam.reportportal.extension.azure.entity.model.IntegrationParameters;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.Configuration;
import com.epam.reportportal.extension.azure.rest.client.api.*;
import com.epam.reportportal.extension.azure.rest.client.auth.HttpBasicAuth;
import com.epam.reportportal.extension.azure.rest.client.model.AttachmentInfo;
import com.epam.reportportal.extension.azure.rest.client.model.AttachmentReference;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.JsonPatchOperation;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemClassificationNode;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemField;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemType;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemTypeFieldWithReferences;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItem;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.reportportal.extension.bugtracking.InternalTicket;
import com.epam.reportportal.extension.bugtracking.InternalTicketAssembler;
import com.epam.reportportal.extension.common.IntegrationTypeProperties;
import com.epam.reportportal.extension.event.PluginEvent;
import com.epam.reportportal.extension.event.StartLaunchEvent;
import com.epam.reportportal.extension.azure.command.binary.GetFileCommand;
import com.epam.reportportal.extension.azure.command.entity.CreateEntityCommand;
import com.epam.reportportal.extension.azure.command.entity.DeleteEntityCommand;
import com.epam.reportportal.extension.azure.command.entity.GetProjectEntities;
import com.epam.reportportal.extension.azure.command.utils.RequestEntityConverter;
import com.epam.reportportal.extension.azure.dao.EntityRepository;
import com.epam.reportportal.extension.azure.dao.impl.EntityRepositoryImpl;
import com.epam.reportportal.extension.azure.event.launch.AzureStartLaunchEventListener;
import com.epam.reportportal.extension.azure.event.plugin.AzurePluginEventListener;
import com.epam.reportportal.extension.azure.event.plugin.PluginEventHandlerFactory;
import com.epam.reportportal.extension.azure.info.impl.PluginInfoProviderImpl;
import com.epam.reportportal.extension.azure.service.EntityService;
import com.epam.reportportal.extension.azure.utils.MemoizingSupplier;
import com.epam.ta.reportportal.binary.impl.AttachmentDataStoreService;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.attachment.Attachment;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataEncoder;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Extension
public class AzureExtension implements ReportPortalExtensionPoint, DisposableBean, BtsExtension {

	public static final Logger LOGGER = LoggerFactory.getLogger(AzureExtension.class);

	public static final String BINARY_DATA_PROPERTIES_FILE_ID = "azure-binary-data.properties";

	public static final String SCHEMA_SCRIPTS_DIR = "schema";

	private static final String PLUGIN_ID = "Azure DevOps";

	private static final String API_VERSION = "6.0";

	private static final String EXPAND = "All";

	private static final String AREA = "area";

	private static final String ITERATION = "iteration";

	private static final String BACK_LINK_HEADER = "<h3><i>Backlink to Report Portal:</i></h3>";

	private static final String BACK_LINK_PATTERN = "<a href=\"%s\">Link to defect</a>";

	private static final String COMMENTS_HEADER = "<h3><i>Test Item comments:</i></h3>";

	private static final String LOGS_HEADER = "<h3><i>Test execution logs:</i></h3>";

	private static final String IMAGE_CONTENT = "image";

	private static final Integer DEPTH = 15;

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

	@Autowired
	private TestItemRepository itemRepository;

	@Autowired
	private AttachmentDataStoreService attachmentDataStoreService;

	@Autowired
	private DataEncoder dataEncoder;

	@Autowired
	private LogRepository logRepository;
	private IntegrationParameters params;
	private ApiClient defaultClient;
	private String organizationName;

	private Supplier<InternalTicketAssembler> ticketAssembler = Suppliers.memoize(() -> new InternalTicketAssembler(logRepository,
			itemRepository,
			attachmentDataStoreService,
			dataEncoder
	));

	public AzureExtension(Map<String, Object> initParams) {
		resourcesDir = IntegrationTypeProperties.RESOURCES_DIRECTORY.getValue(initParams).map(String::valueOf).orElse("");
		objectMapper = configureObjectMapper();

		pluginLoadedListenerSupplier = new MemoizingSupplier<>(() -> new AzurePluginEventListener(PLUGIN_ID,
				new PluginEventHandlerFactory(integrationTypeRepository,
						integrationRepository,
						new PluginInfoProviderImpl(resourcesDir, BINARY_DATA_PROPERTIES_FILE_ID)
				)
		));
		startLaunchEventListenerSupplier = new MemoizingSupplier<>(() -> new AzureStartLaunchEventListener(launchRepository));

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

	@Override
	public IntegrationGroupEnum getIntegrationGroup() {
		return IntegrationGroupEnum.BTS;
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
		pluginCommandMapping.put("testConnection", new TestConnectionCommand());
		return pluginCommandMapping;
	}

	@Override
	// Never called method. Connection is tested via the command.
	public boolean testConnection(Integration integration) {
		return false;
	}

	@Override
	public Optional<Ticket> getTicket(String id, Integration integration) {
		initFields(integration);

		WorkItemsApi workItemsApi = new WorkItemsApi(defaultClient);
		try {
			WorkItem workItem = workItemsApi
					.workItemsGetWorkItem(organizationName, Integer.valueOf(id), params.getProjectName(), API_VERSION,
							null, null, null);
			return Optional.of(convertWorkItemToTicket(workItem));
		} catch (ApiException e) {
			LOGGER.error("Unable to load ticket: " + e.getMessage(), e);
			return Optional.empty();
		}
	}

	@Override
	public Ticket submitTicket(PostTicketRQ ticketRQ, Integration integration) {
		initFields(integration);
		List<AttachmentInfo> attachmentsURL = new ArrayList<>();

		List<JsonPatchOperation> patchOperationList = new ArrayList<>();

		uploadAttachmentToAzure(ticketRQ, attachmentsURL);

		String issueType = null;
		List<PostFormField> fields = ticketRQ.getFields();
		issueType = getPatchOperationsForFields(ticketRQ, patchOperationList, issueType, fields, attachmentsURL);

		WorkItemsApi workItemsApi = new WorkItemsApi(defaultClient);
		WorkItem workItem = null;
		List<JsonPatchOperation> patchOperationsForAttachment = new ArrayList<>();

		try {
			workItem = workItemsApi
					.workItemsCreate(organizationName, patchOperationList, params.getProjectName(), issueType,
							API_VERSION, null, null, null, null);

			if (!attachmentsURL.isEmpty()) {
				getPatchOperationsForAttachments(patchOperationsForAttachment, attachmentsURL);
				workItemsApi.workItemsUpdate(organizationName, patchOperationsForAttachment, workItem.getId(), params.getProjectName(),
						API_VERSION, null, null, null, null);
			}
			return convertWorkItemToTicket(workItem);
		} catch (ApiException e) {
			LOGGER.error("Unable to post issue: " + e.getMessage(), e);
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					String.format("Unable to post issue. Code: %s, Message: %s log - ", e.getCode(), e.getMessage()), e);
		}
	}

	private String getPatchOperationsForFields(PostTicketRQ ticketRQ, List<JsonPatchOperation> patchOperationList, String issueType, List<PostFormField> fields, List<AttachmentInfo> attachmentsURL) {
		for (PostFormField field : fields) {
			String id = replaceSeparators(field.getId());
			String operation = "add";
			String path = "/fields/" + id;
			String value = field.getValue().get(0);

			if ("issuetype".equals(field.getId())) {
				issueType = field.getValue().get(0);
				continue;
			}
			if ("System.Description".equals(id)) {
				path = "/fields/System.Description";
				value = field.getValue().get(0) + getDescriptionFromTestItem(ticketRQ, attachmentsURL);
			}
			patchOperationList.add(new JsonPatchOperation(null, operation, path, value));
		}
		return issueType;
	}

	private void getPatchOperationsForAttachments(List<JsonPatchOperation> patchOperationsForAttachment, List<AttachmentInfo> attachmentsURL) {
		String operation = "add";
		String path = "/relations/-";
		for (AttachmentInfo attachmentURL : attachmentsURL) {
			Map<String, Object> value = new HashMap<>();

			value.put("rel", "AttachedFile");
			value.put("url", attachmentURL.getUrl());
			Map<String, String> attributes = new HashMap<>();
			attributes.put("comment", "");
			value.put("attributes", attributes);

			patchOperationsForAttachment.add(new JsonPatchOperation(null, operation, path, value));
		}
	}

	@Override
	public List<PostFormField> getTicketFields(String issueType, Integration integration) {
		initFields(integration);
		String projectName = params.getProjectName();

		ClassificationNodesApi nodesApi = new ClassificationNodesApi(defaultClient);
		Map<String, List<WorkItemClassificationNode>> classificationNodes = getClassificationNodes(nodesApi,
				organizationName, projectName);
		List<WorkItemClassificationNode> areaNodes = classificationNodes.get(AREA);
		List<WorkItemClassificationNode> iterationNodes = classificationNodes.get(ITERATION);

		WorkItemTypesFieldApi issueTypeFieldsApi = new WorkItemTypesFieldApi(defaultClient);
		FieldsApi fieldsApi = new FieldsApi(defaultClient);
		List<PostFormField> ticketFields = new ArrayList<>();
		try {
			List<WorkItemTypeFieldWithReferences> issueTypeFields = issueTypeFieldsApi
					.workItemTypesFieldList(organizationName, projectName, issueType, API_VERSION, EXPAND);

			for (WorkItemTypeFieldWithReferences field : issueTypeFields) {
				WorkItemField detailedField = getFieldDetails(fieldsApi, organizationName, projectName, field);
				// Skip fields that return 404 on request
				if (detailedField == null) {
					continue;
				}
				// Skip read-only fields and Work Item Type field cause we have the same custom field
				if (detailedField.isReadOnly() || detailedField.getName().equals("Work Item Type")) {
					continue;
				}

				List<AllowedValue> allowedValues = prepareAllowedValues(field, areaNodes, iterationNodes);
				List<String> defaultValue = prepareDefaultValue(field);

				ticketFields.add(new PostFormField(replaceIllegalCharacters(field.getReferenceName()), field.getName(),
						detailedField.getType().toString(), field.isAlwaysRequired(), defaultValue, allowedValues));
			}
			return sortTicketFields(ticketFields, issueType);
		} catch (ApiException e) {
			LOGGER.error("Unable to load ticket fields: " + e.getMessage(), e);
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					String.format("Unable to load ticket fields. Code: %s, Message: %s", e.getCode(), e.getMessage()), e);
		}
	}

	@Override
	public List<String> getIssueTypes(Integration integration) {
		initFields(integration);

		WorkItemTypesApi issueTypesApi = new WorkItemTypesApi(defaultClient);
		try {
			List<WorkItemType> issueTypes = issueTypesApi.workItemTypesList(organizationName, params.getProjectName(), API_VERSION);
			return issueTypes.stream().map(WorkItemType::getName).collect(Collectors.toList());
		} catch (ApiException e) {
			LOGGER.error("Unable to load issue types: " + e.getMessage(), e);
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					String.format("Unable to load issue types. Code: %s, Message: %s", e.getCode(), e.getMessage()), e);
		}
	}

	private void initFields(Integration integration) {
		params = getParams(integration);
		defaultClient = getConfiguredApiClient(params.getPersonalAccessToken());
		organizationName = extractOrganizationNameFromUrl(defaultClient, params.getOrganizationUrl());
	}

	private IntegrationParameters getParams(Integration integration) {
		IntegrationParameters result = new IntegrationParameters();
		Map<String, Object> params = integration.getParams().getParams();
		result.setOrganizationUrl(params.get("url").toString());
		result.setProjectName(params.get("project").toString());
		result.setPersonalAccessToken(params.get("oauthAccessKey").toString());
		return result;
	}

	private ApiClient getConfiguredApiClient(String personalAccessToken) {
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		HttpBasicAuth basicAuth = (HttpBasicAuth) defaultClient.getAuthentication("accessToken");
		basicAuth.setPassword(personalAccessToken);
		return defaultClient;
	}

	private String extractOrganizationNameFromUrl(ApiClient client, String organizationUrl) {
		return organizationUrl.replace(client.getBasePath(), "");
	}

	private Ticket convertWorkItemToTicket(WorkItem workItem) {
		Ticket ticket = new Ticket();
		String ticketId = workItem.getId().toString();
		String ticketUrl = workItem.getUrl().substring(0, workItem.getUrl().lastIndexOf(ticketId))
				.replace("apis/wit/", "") + "edit/" + ticketId;
		ticket.setId(ticketId);
		ticket.setTicketUrl(ticketUrl);
		ticket.setStatus(workItem.getFields().get("System.State").toString());
		ticket.setSummary(workItem.getFields().get("System.Title").toString());
		return ticket;
	}

	private List<WorkItemClassificationNode> extractNestedNodes(WorkItemClassificationNode node) {
		List<WorkItemClassificationNode> nodes = new ArrayList<>();
		nodes.add(node);

		if (node.isHasChildren()) {
			for (WorkItemClassificationNode childrenNode : node.getChildren()) {
				nodes.addAll(extractNestedNodes(childrenNode));
			}
		}
		return nodes;
	}

	private Map<String, List<WorkItemClassificationNode>> getClassificationNodes(
			ClassificationNodesApi nodesApi, String organizationName, String projectName
	) {
		List<WorkItemClassificationNode> areaNodes = new ArrayList<>();
		List<WorkItemClassificationNode> iterationNodes = new ArrayList<>();
		Map<String, List<WorkItemClassificationNode>> nodesGroupedByType = new HashMap<>();
		try {
			List<WorkItemClassificationNode> nodes = nodesApi
					.classificationNodesGetRootNodes(organizationName, projectName, API_VERSION, DEPTH);
			for (WorkItemClassificationNode node : nodes) {
				if (node.getStructureType().equals(AREA)) {
					areaNodes = extractNestedNodes(node);
				} else if (node.getStructureType().equals(ITERATION)) {
					iterationNodes = extractNestedNodes(node);
				}
			}
			nodesGroupedByType.put(AREA, areaNodes);
			nodesGroupedByType.put(ITERATION, iterationNodes);
			return nodesGroupedByType;
		} catch (ApiException e) {
			LOGGER.error("Unable to load classification nodes: " + e.getMessage(), e);
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
					String.format("Unable to load classification nodes. Code: %s, Message: %s", e.getCode(),
							e.getMessage()), e);
		}
	}

	private WorkItemField getFieldDetails(
			FieldsApi fieldsApi, String organizationName, String projectName, WorkItemTypeFieldWithReferences field
	) throws ApiException {
		try {
			return fieldsApi.fieldsGet(organizationName, field.getReferenceName(), projectName, API_VERSION);
		} catch (ApiException e) {
			// Some special fields return 404 on request, we will skip them
			if (e.getCode() == 404) {
				return null;
			} else {
				throw e;
			}
		}
	}

	private List<AllowedValue> prepareAllowedValues(
			WorkItemTypeFieldWithReferences field, List<WorkItemClassificationNode> areaNodes,
			List<WorkItemClassificationNode> iterationNodes
	) {
		List<AllowedValue> allowed = new ArrayList<>();
		switch (field.getName()) {
			case "Iteration ID":
				for (WorkItemClassificationNode node : iterationNodes) {
					allowed.add(new AllowedValue(node.getId().toString(), node.getName()));
				}
				break;
			case "Area ID":
				for (WorkItemClassificationNode node : areaNodes) {
					allowed.add(new AllowedValue(node.getId().toString(), node.getName()));
				}
				break;
			default:
				for (Object value : field.getAllowedValues()) {
					allowed.add(new AllowedValue(replaceIllegalCharacters(value.toString()), value.toString()));
				}
				break;
		}

		// Add an empty line to each field with a non-empty list of allowed values
		if (allowed.size() > 0) {
			allowed.add(0, new AllowedValue("Empty_String", ""));
		}
		return allowed;
	}

	private List<String> prepareDefaultValue(WorkItemTypeFieldWithReferences field) {
		List<String> defaultValue = new ArrayList<>();
		if (field.getDefaultValue() != null) {
			defaultValue.add(replaceIllegalCharacters(field.getDefaultValue().toString()));
		}
		return defaultValue;
	}

	// ID value should not contain spaces and dots
	private String replaceIllegalCharacters(String id) {
		return id.replace(" ", "_").replace(".", "_");
	}

	// Replace ID separators back. The method is the opposite of the method above.
	private String replaceSeparators(String id) {
		return id.replace("_", ".");
	}

	private List<PostFormField> sortTicketFields(List<PostFormField> ticketFields, String issueType) {
		List<PostFormField> sortedTicketFields = ticketFields.stream()
				.sorted(Comparator.comparing(PostFormField::getIsRequired).reversed()
						.thenComparing(PostFormField::getFieldName)).collect(Collectors.toList());

		// Add to the top a custom field representing the work item type
		sortedTicketFields.add(0, new PostFormField("issuetype", "Issue Type", "issuetype",
				true, List.of(issueType), new ArrayList<AllowedValue>()));
		return sortedTicketFields;
	}

	private String getDescriptionFromTestItem(PostTicketRQ ticketRQ, List<AttachmentInfo> attachmentsURL) {
		StringBuilder descriptionBuilder = new StringBuilder();

		TestItem item = itemRepository.findById(ticketRQ.getTestItemId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, ticketRQ.getTestItemId()));

		ticketRQ.getBackLinks().keySet().forEach(backLinkId -> updateDescriptionBuilder(descriptionBuilder, ticketRQ, backLinkId, item, attachmentsURL));
		return descriptionBuilder.toString();
	}

	private void updateDescriptionBuilder(StringBuilder descriptionBuilder, PostTicketRQ ticketRQ, Long backLinkId, TestItem item, List<AttachmentInfo> attachmentsURL) {
		if (StringUtils.isNotBlank(ticketRQ.getBackLinks().get(backLinkId))) {
			descriptionBuilder.append(BACK_LINK_HEADER)
					.append(String.format(BACK_LINK_PATTERN, ticketRQ.getBackLinks().get(backLinkId)));
		}

		if (ticketRQ.getIsIncludeComments()) {
			if (StringUtils.isNotBlank(ticketRQ.getBackLinks().get(backLinkId))) {
				// Add a comment to the issue description, if there is one in the test-item
				ofNullable(item.getItemResults()).flatMap(result -> ofNullable(result.getIssue())).ifPresent(issue -> {
					if (StringUtils.isNotBlank(issue.getIssueDescription())) {
						descriptionBuilder.append(COMMENTS_HEADER).append(issue.getIssueDescription());
					}
				});
			}
		}
		// Add logs to the issue description, if they are in the test-item
		addLogsInfoToDescription(descriptionBuilder, backLinkId, ticketRQ, attachmentsURL);
	}

	private void addLogsInfoToDescription(StringBuilder descriptionBuilder, Long backLinkId, PostTicketRQ ticketRQ, List<AttachmentInfo> attachmentsURL) {
		itemRepository.findById(backLinkId).ifPresent(item -> ofNullable(item.getLaunchId()).ifPresent(launchId -> {
			List<Log> logs = logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(launchId,
					Collections.singletonList(item.getItemId()),
					ticketRQ.getNumberOfLogs()
			);
			if (CollectionUtils.isNotEmpty(logs) && (ticketRQ.getIsIncludeLogs() || ticketRQ.getIsIncludeScreenshots())) {
				descriptionBuilder.append(LOGS_HEADER);
				logs.forEach(log -> updateWithLog(descriptionBuilder,
						log,
						ticketRQ.getIsIncludeLogs(),
						ticketRQ.getIsIncludeScreenshots(),
						attachmentsURL));
			}
		}));
	}

	private void updateWithLog(StringBuilder descriptionBuilder, Log log, boolean includeLog, boolean includeScreenshot, List<AttachmentInfo> attachmentsURL) {
		if (includeLog) {
			descriptionBuilder.append("<div><pre>").append(getFormattedMessage(log)).append("</pre></div>");
		}
		if (includeScreenshot) {
			ofNullable(log.getAttachment()).ifPresent(attachment -> addAttachmentToDescription(descriptionBuilder, attachment, attachmentsURL));
		}
	}

	private void addAttachmentToDescription(StringBuilder descriptionBuilder, Attachment attachment, List<AttachmentInfo> attachmentsURL) {
		if (StringUtils.isNotBlank(attachment.getContentType()) && StringUtils.isNotBlank(attachment.getFileId())) {
			AttachmentInfo attachmentInfo = null;
			for (AttachmentInfo info : attachmentsURL) {
				if (info.getFileId().equals(attachment.getFileId())) {
					attachmentInfo = info;
					break;
				}
			}
			String url = attachmentInfo.getUrl();

			if (attachmentInfo.getContentType().contains(IMAGE_CONTENT)) {
				descriptionBuilder.append("Attachment:<br>" + "<img src=\"" + url + "\" alt=\"" + attachmentInfo.getFileName() + "\">");
			} else {
				descriptionBuilder.append("Attachment - " + "<a href=\"" + url + "\">" + attachmentInfo.getFileName() + "</a>");
			}
		}
	}

	private void uploadAttachmentToAzure(PostTicketRQ ticketRQ, List<AttachmentInfo> attachmentsURL) {
		List<InternalTicket.LogEntry> logs = ofNullable(ticketAssembler.get().apply(ticketRQ).getLogs()).orElseGet(Lists::newArrayList);
		List<InternalTicket.LogEntry> attachments = new ArrayList<>();
		logs.stream()
				.filter(InternalTicket.LogEntry::isHasAttachment)
				.forEach(entry -> attachments.add(entry));
		for (InternalTicket.LogEntry attachment : attachments) {
			Optional<InputStream> fileOptional = attachmentDataStoreService.load(attachment.getFileId());
			if (fileOptional.isPresent()) {
				try (InputStream file = fileOptional.get()) {
					byte[] bytes = ByteStreams.toByteArray(file);
					AttachmentsApi attachmentsApi = new AttachmentsApi(defaultClient);
					String fileName = attachment.getDecodedFileName();
					AttachmentReference attachmentReference = attachmentsApi.attachmentsCreate(organizationName, bytes, params.getProjectName(), API_VERSION,
							fileName, null, null);
					attachmentsURL.add(new AttachmentInfo(attachment.getDecodedFileName(),
							attachment.getFileId(), attachmentReference.getUrl(), attachment.getContentType()));
				} catch (IOException | ApiException e) {
					LOGGER.error("Unable to post ticket : " + e.getMessage(), e);
					throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Unable to post ticket: " + e.getMessage(), e);
				}
			} else {
				throw new ReportPortalException(UNABLE_TO_LOAD_BINARY_DATA);
			}
		}
	}

	private String getFormattedMessage(Log log) {
		StringBuilder messageBuilder = new StringBuilder();
		ofNullable(log.getLogTime()).ifPresent(logTime -> messageBuilder.append("Time: ")
				.append(logTime.format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))).append(", "));
		ofNullable(log.getLogLevel()).ifPresent(logLevel -> messageBuilder.append("Level: ").append(logLevel).append(", "));
		messageBuilder.append("<br>").append("Log: ").append(log.getLogMessage());
		return messageBuilder.toString();
	}
}

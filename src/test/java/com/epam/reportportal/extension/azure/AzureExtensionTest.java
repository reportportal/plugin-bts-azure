package com.epam.reportportal.extension.azure;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.azure.command.connection.TestConnectionCommand;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.api.ClassificationNodesApi;
import com.epam.reportportal.extension.azure.rest.client.api.FieldsApi;
import com.epam.reportportal.extension.azure.rest.client.api.WorkItemTypesApi;
import com.epam.reportportal.extension.azure.rest.client.api.WorkItemTypesFieldApi;
import com.epam.reportportal.extension.azure.rest.client.api.WorkItemsApi;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItem;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemClassificationNode;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemField;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemType;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemTypeFieldWithReferences;
import com.epam.reportportal.model.externalsystem.AllowedValue;
import com.epam.reportportal.model.externalsystem.PostFormField;
import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.ta.reportportal.binary.impl.AttachmentDataStoreService;
import com.epam.ta.reportportal.dao.IntegrationRepository;
import com.epam.ta.reportportal.dao.IntegrationTypeRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.TestItemResults;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.filesystem.DataEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class AzureExtensionTest {

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	private DataSource dataSource;

	@Mock
	private IntegrationTypeRepository integrationTypeRepository;

	@Mock
	private IntegrationRepository integrationRepository;

	@Mock
	private LaunchRepository launchRepository;

	@Mock
	private TestItemRepository itemRepository;

	@Mock
	private AttachmentDataStoreService attachmentDataStoreService;

	@Mock
	private DataEncoder dataEncoder;

	@Mock
	private BasicTextEncryptor basicTextEncryptor;

	@Mock
	private LogRepository logRepository;

	@Mock
	private WorkItemsApi workItemsApi;

	@Mock
	private ClassificationNodesApi classificationNodesApi;

	@Mock
	private WorkItemTypesFieldApi workItemTypesFieldApi;

	@Mock
	private FieldsApi fieldsApi;

	@Mock
	private WorkItemTypesApi workItemTypesApi;

	@InjectMocks
	private AzureExtension azureExtension = new AzureExtension(new HashMap<>());

	private Integration integration;
	private WorkItem workItem;

	@BeforeEach
	public void init(){
		Map<String, Object> params = new HashMap<>();
		params.put("url", "https://dev.azure.com/some");
		params.put("project", "ADOPlugin");
		params.put("oauthAccessKey", "token");
		IntegrationParams integrationParams = new IntegrationParams(params);
		integration = new Integration(1L, new Project(1L, "ProjectName"), new IntegrationType(),
				integrationParams, Instant.now());
		workItem = new WorkItem();
		workItem.setId(23);
		workItem.setUrl("https://dev.azure.com/some/some/_workitems/edit/23");
		Map<String, Object> fields = new HashMap<>();
		fields.put("System.State", "Doing");
		fields.put("System.Title", "Test_Item");
		workItem.setFields(fields);
	}

	@Test
	void getPluginParams() {
		Map<String, Object> expectMap = new HashMap<>();
		expectMap.put("allowedCommands", Arrays.asList("testConnection"));
		expectMap.put("documentationLink", "https://reportportal.io/docs/plugins/AzureDevOps/");
		Map<String, ?> pluginParams = azureExtension.getPluginParams();
		assertEquals(expectMap, pluginParams);
	}

	@Test
	void getCommandToExecute() {
		PluginCommand<?> testConnection = azureExtension.getIntegrationCommand("testConnection");
		TestConnectionCommand testConnectionCommand = new TestConnectionCommand(basicTextEncryptor);
		assertEquals(testConnectionCommand.getClass(), testConnection.getClass());
	}

	@Test
	void getIntegrationGroup() {
		IntegrationGroupEnum integrationGroup = azureExtension.getIntegrationGroup();
		assertEquals(IntegrationGroupEnum.BTS, integrationGroup);
	}

	@Test
	void testConnection() {
		assertFalse(azureExtension.testConnection(new Integration()));
	}


	@Test
	void getTicket() throws ApiException {
		when(basicTextEncryptor.decrypt(any())).thenReturn("token");
		when(workItemsApi.workItemsGetWorkItem(any(), any(), any(), any(), any(), any(), any())).thenReturn(workItem);
		Ticket ticket = azureExtension.getTicket("23", integration).get();
		assertEquals("Test_Item", ticket.getSummary());
		assertEquals("Doing", ticket.getStatus());
		assertEquals("23", ticket.getId());
	}

	@Test
	void submitTicket() throws ApiException {
		when(basicTextEncryptor.decrypt(any())).thenReturn("token");
		when(workItemsApi.workItemsCreate(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(workItem);

		TestItem testItem = new TestItem();

		TestItemResults itemResults = new TestItemResults();
		IssueEntity issue = new IssueEntity();
		issue.setIssueDescription("Some issue description");
		itemResults.setIssue(issue);
		testItem.setItemResults(itemResults);

		when(itemRepository.findById(12L)).thenReturn(Optional.of(testItem));

		PostTicketRQ postTicketRQ = new PostTicketRQ();
		ArrayList<PostFormField> data = new ArrayList<>();
		ArrayList<AllowedValue> defValues = new ArrayList<>();
		defValues.add(new AllowedValue("14", "ADOPlugin"));
		data.add(new PostFormField("issuetype", "Issue Type", "issuetype", true,
				List.of("Issue"), new ArrayList<>()));
		data.add(new PostFormField("System_AreaId", "Area ID", "integer", true,
				List.of("ADOPlugin"), defValues));
		data.add(new PostFormField("System_IterationId", "Iteration  ID", "integer", true,
				List.of("ADOPlugin"), List.of(new AllowedValue("11", "ADOPlugin"))));
		data.add(new PostFormField("System_State", "State", "string", true,
				List.of("Doing"), List.of(new AllowedValue("Doing", "Doing"))));
		data.add(new PostFormField("System_Title", "Title", "string", true,
				List.of("Test_Item"), new ArrayList<>()));
		data.add(new PostFormField("System_Description", "Description", "html", false,
				List.of("Some description"), new ArrayList<>()));
		postTicketRQ.setFields(data);
		postTicketRQ.setIsIncludeLogs(true);
		postTicketRQ.setIsIncludeComments(true);
		postTicketRQ.setIsIncludeScreenshots(true);
		postTicketRQ.setTestItemId(12L);
		Map<Long, String> backLinks = new HashMap<>();
		backLinks.put(12L, "https://alpha.reportportal.io/");
		postTicketRQ.setBackLinks(backLinks);
		Ticket ticket = azureExtension.submitTicket(postTicketRQ, integration);

		assertEquals("Test_Item", ticket.getSummary());
		assertEquals("Doing", ticket.getStatus());
		assertEquals("23", ticket.getId());
	}

	@Test
	void getTicketFields() throws ApiException {
		when(basicTextEncryptor.decrypt(any())).thenReturn("token");
		List<WorkItemClassificationNode> nodes = new ArrayList<>();
		WorkItemClassificationNode node1 = new WorkItemClassificationNode();
		node1.setId(14);
		node1.setName("ADOPlugin");
		node1.setStructureType("area");
		node1.hasChildren(false);

		WorkItemClassificationNode node2 = new WorkItemClassificationNode();
		node2.setId(11);
		node2.setName("ADOPlugin");
		node2.setStructureType("iteration");
		node2.hasChildren(true);

		WorkItemClassificationNode node3 = new WorkItemClassificationNode();
		node3.setId(13);
		node3.setName("Sprint 1");
		node3.setStructureType("iteration");
		node3.hasChildren(false);

		WorkItemClassificationNode node4 = new WorkItemClassificationNode();
		node4.setId(15);
		node4.setName("Sprint 2");
		node4.setStructureType("iteration");
		node4.hasChildren(false);

		node2.setChildren(Arrays.asList(node3, node4));

		nodes.add(node1);
		nodes.add(node2);
		when(classificationNodesApi.classificationNodesGetRootNodes(any(), any(), any(), any())).thenReturn(nodes);

		List<WorkItemTypeFieldWithReferences> issueTypeFields = new ArrayList<>();
		WorkItemTypeFieldWithReferences field1 = new WorkItemTypeFieldWithReferences();
		field1.setReferenceName("System.IterationPath");
		field1.setName("Iteration Path");
		field1.setAllowedValues(List.of());
		field1.setAlwaysRequired(false);

		WorkItemTypeFieldWithReferences field2 = new WorkItemTypeFieldWithReferences();
		field2.setReferenceName("System.IterationId");
		field2.setName("Iteration ID");
		field2.setAllowedValues(List.of());
		field2.setAlwaysRequired(true);

		WorkItemTypeFieldWithReferences field3 = new WorkItemTypeFieldWithReferences();
		field3.setReferenceName("System.AreaId");
		field3.setName("Area ID");
		field3.setAllowedValues(List.of());
		field3.setAlwaysRequired(true);

		WorkItemTypeFieldWithReferences field4 = new WorkItemTypeFieldWithReferences();
		field4.setReferenceName("System.Title");
		field4.setName("Title");
		field4.setAllowedValues(List.of());
		field4.setAlwaysRequired(true);

		issueTypeFields.add(field1);
		issueTypeFields.add(field2);
		issueTypeFields.add(field3);
		issueTypeFields.add(field4);
		when(workItemTypesFieldApi.workItemTypesFieldList(any(), any(), any(), any(), any())).thenReturn(issueTypeFields);


		WorkItemField workItemField1 = new WorkItemField();
		workItemField1.setName("Iteration Path");
		workItemField1.setReadOnly(false);
		workItemField1.setType("treePath");

		WorkItemField workItemField2 = new WorkItemField();
		workItemField2.setName("Iteration ID");
		workItemField2.setReadOnly(false);
		workItemField2.setType("integer");

		WorkItemField workItemField3 = new WorkItemField();
		workItemField3.setName("Area ID");
		workItemField3.setReadOnly(false);
		workItemField3.setType("integer");

		WorkItemField workItemField4 = new WorkItemField();
		workItemField4.setName("Title");
		workItemField4.setReadOnly(false);
		workItemField4.setType("string");

		when(fieldsApi.fieldsGet(any(), any(), any(), any())).thenReturn(workItemField1, workItemField2, workItemField3, workItemField4);

		List<PostFormField> issue = azureExtension.getTicketFields("Issue", integration);

		assertEquals(5, issue.size());
		assertEquals("Issue Type", issue.get(0).getFieldName());
		assertEquals("Area ID", issue.get(1).getFieldName());
		assertEquals("Iteration ID", issue.get(2).getFieldName());
		assertEquals("Title", issue.get(3).getFieldName());
		assertEquals("Iteration Path", issue.get(4).getFieldName());
	}

	@Test
	void getIssueTypes() throws ApiException {
		List<WorkItemType> workItemTypes = new ArrayList<>();
		List<String> types = Arrays.asList("Issue", "Epic", "Task", "Test Case", "Test Plan", "Test Suite",
				"Shared Steps", "Shared Parameter", "Code Review Request", "Code Review Response", "Feedback Request", "Feedback Response");
		for (String type: types){
			WorkItemType workItemType = new WorkItemType();
			workItemType.setName(type);
			workItemTypes.add(workItemType);
		}
		when(workItemTypesApi.workItemTypesList(any(), any(), any())).thenReturn(workItemTypes);

		List<String> issueTypes = azureExtension.getIssueTypes(integration);
		assertArrayEquals(types.toArray(), issueTypes.toArray());
	}
}

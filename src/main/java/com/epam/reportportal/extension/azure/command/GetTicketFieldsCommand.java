package com.epam.reportportal.extension.azure.command;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.reportportal.extension.azure.AzureExtension.PROJECT;
import static com.epam.reportportal.extension.azure.AzureExtension.URL;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.AllowedValue;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostFormField;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.azure.client.AzureApiClientProvider;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.api.ClassificationNodesApi;
import com.epam.reportportal.extension.azure.rest.client.api.FieldsApi;
import com.epam.reportportal.extension.azure.rest.client.api.WorkItemTypesFieldApi;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemClassificationNode;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemField;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemTypeFieldWithReferences;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTicketFieldsCommand extends AbstractExtensionCommand<List<PostFormField>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetTicketFieldsCommand.class);
  private static final String API_VERSION = "6.0";
  private static final String EXPAND = "All";
  private static final String AREA = "area";
  private static final String ITERATION = "iteration";
  private static final String ISSUE_TYPE_PARAM = "issueType";
  private static final Integer DEPTH = 15;

  private final AzureApiClientProvider clientProvider;

  public GetTicketFieldsCommand(AzureApiClientProvider clientProvider,
      ProjectRepository projectRepository, OrganizationRepositoryCustom organizationRepository) {
    super(projectRepository, organizationRepository);
    this.clientProvider = clientProvider;
  }

  @Override
  public String getName() {
    return "getIssueFields";
  }

  protected ClassificationNodesApi buildClassificationNodesApi(ApiClient client) {
    return new ClassificationNodesApi(client);
  }

  protected WorkItemTypesFieldApi buildWorkItemTypesFieldApi(ApiClient client) {
    return new WorkItemTypesFieldApi(client);
  }

  protected FieldsApi buildFieldsApi(ApiClient client) {
    return new FieldsApi(client);
  }

  @Override
  protected List<PostFormField> invokeCommand(Integration integration, PluginCommandRQ rq) {
    String issueType = (String) Optional.ofNullable(rq.getArguments().get(ISSUE_TYPE_PARAM))
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            ISSUE_TYPE_PARAM + " must be provided"
        ));

    ApiClient client = clientProvider.provide(integration);
    String orgName = clientProvider.extractOrganizationName(client,
        integration.getParams().getParams().get(URL).toString()
    );
    String projectName = integration.getParams().getParams().get(PROJECT).toString();

    Map<String, List<WorkItemClassificationNode>> classificationNodes =
        getClassificationNodes(buildClassificationNodesApi(client), orgName, projectName);
    List<WorkItemClassificationNode> areaNodes = classificationNodes.get(AREA);
    List<WorkItemClassificationNode> iterationNodes = classificationNodes.get(ITERATION);

    WorkItemTypesFieldApi issueTypeFieldsApi = buildWorkItemTypesFieldApi(client);
    FieldsApi fieldsApi = buildFieldsApi(client);
    List<PostFormField> ticketFields = new ArrayList<>();
    try {
      List<WorkItemTypeFieldWithReferences> issueTypeFields =
          issueTypeFieldsApi.workItemTypesFieldList(orgName, projectName, issueType, API_VERSION,
              EXPAND
          );
      for (WorkItemTypeFieldWithReferences field : issueTypeFields) {
        Optional<WorkItemField> detailedField = getFieldDetails(fieldsApi, orgName, projectName,
            field
        );
        detailedField.filter(f -> !f.isReadOnly() && !f.getName().equals("Work Item Type"))
            .ifPresent(f -> {
              List<AllowedValue> allowedValues =
                  prepareAllowedValues(field, areaNodes, iterationNodes);
              List<String> defaultValue = new ArrayList<>();
              if (!allowedValues.isEmpty()) {
                defaultValue.add(allowedValues.get(0).getValueName());
              }
              ticketFields.add(
                  new PostFormField(replaceIllegalCharacters(field.getReferenceName()),
                      field.getName(), f.getType().toString(), field.isAlwaysRequired(),
                      defaultValue, allowedValues
                  ));
            });
      }
      return sortTicketFields(ticketFields, issueType);
    } catch (ApiException e) {
      LOGGER.error("Unable to load ticket fields: {}", e.getMessage(), e);
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
          String.format("Unable to load ticket fields. Code: %s", e.getCode()), e
      );
    }
  }

  private Map<String, List<WorkItemClassificationNode>> getClassificationNodes(
      ClassificationNodesApi nodesApi, String orgName, String projectName) {
    List<WorkItemClassificationNode> areaNodes = new ArrayList<>();
    List<WorkItemClassificationNode> iterationNodes = new ArrayList<>();
    try {
      List<WorkItemClassificationNode> nodes =
          nodesApi.classificationNodesGetRootNodes(orgName, projectName, API_VERSION, DEPTH);
      for (WorkItemClassificationNode node : nodes) {
        if (AREA.equals(node.getStructureType())) {
          areaNodes = extractNestedNodes(node);
        } else if (ITERATION.equals(node.getStructureType())) {
          iterationNodes = extractNestedNodes(node);
        }
      }
    } catch (ApiException e) {
      LOGGER.error("Unable to load classification nodes: {}", e.getMessage(), e);
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
          String.format("Unable to load classification nodes. Code: %s, Message: %s", e.getCode(),
              e.getMessage()
          ), e
      );
    }
    Map<String, List<WorkItemClassificationNode>> result = new HashMap<>();
    result.put(AREA, areaNodes);
    result.put(ITERATION, iterationNodes);
    return result;
  }

  private List<WorkItemClassificationNode> extractNestedNodes(WorkItemClassificationNode node) {
    List<WorkItemClassificationNode> nodes = new ArrayList<>();
    nodes.add(node);
    if (node.isHasChildren()) {
      for (WorkItemClassificationNode child : node.getChildren()) {
        nodes.addAll(extractNestedNodes(child));
      }
    }
    return nodes;
  }

  private Optional<WorkItemField> getFieldDetails(FieldsApi fieldsApi, String orgName,
      String projectName, WorkItemTypeFieldWithReferences field) throws ApiException {
    try {
      return Optional.ofNullable(
          fieldsApi.fieldsGet(orgName, field.getReferenceName(), projectName, API_VERSION));
    } catch (ApiException e) {
      if (e.getCode() == 404) {
        return Optional.empty();
      }
      throw e;
    }
  }

  private List<AllowedValue> prepareAllowedValues(WorkItemTypeFieldWithReferences field,
      List<WorkItemClassificationNode> areaNodes,
      List<WorkItemClassificationNode> iterationNodes) {
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
      case "State":
        String defaultValue = field.getDefaultValue().toString();
        allowed.add(new AllowedValue(replaceIllegalCharacters(defaultValue), defaultValue));
        break;
      default:
        for (Object value : field.getAllowedValues()) {
          allowed.add(
              new AllowedValue(replaceIllegalCharacters(value.toString()), value.toString()));
        }
    }
    return allowed;
  }

  private List<PostFormField> sortTicketFields(List<PostFormField> ticketFields, String issueType) {
    List<PostFormField> sorted = ticketFields.stream().sorted(
        Comparator.comparing(PostFormField::getIsRequired).reversed()
            .thenComparing(PostFormField::getFieldName)).collect(Collectors.toList());
    sorted.add(0, new PostFormField("issuetype", "Issue Type", "issuetype", true,
        List.of(issueType), new ArrayList<>()
    ));
    return sorted;
  }

  private String replaceIllegalCharacters(String id) {
    return id.replace(" ", "_").replace(".", "_");
  }
}

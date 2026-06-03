package com.epam.reportportal.extension.azure.command;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_TO_LOAD_BINARY_DATA;
import static com.epam.reportportal.extension.azure.AzureExtension.PROJECT;
import static com.epam.reportportal.extension.azure.AzureExtension.URL;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.AllowedValue;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostFormField;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.binary.impl.AttachmentDataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepositoryCustom;
import com.epam.reportportal.base.infrastructure.persistence.entity.attachment.Attachment;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.filesystem.DataEncoder;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.azure.client.AzureApiClientProvider;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.api.AttachmentsApi;
import com.epam.reportportal.extension.azure.rest.client.api.WorkItemsApi;
import com.epam.reportportal.extension.azure.rest.client.model.AttachmentInfo;
import com.epam.reportportal.extension.azure.rest.client.model.AttachmentReference;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.JsonPatchOperation;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItem;
import com.epam.reportportal.extension.bugtracking.InternalTicketAssembler;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.google.common.base.Suppliers;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostTicketCommand extends AbstractExtensionCommand<Ticket> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostTicketCommand.class);
  private static final String API_VERSION = "6.0";
  private static final String IMAGE_CONTENT = "image";
  private static final String BACK_LINK_HEADER = "<h3><i>Back link to Report Portal:</i></h3>";
  private static final String BACK_LINK_PATTERN = "<a href=\"%s\">Link to defect</a>";
  private static final String COMMENTS_HEADER = "<h3><i>Test Item comments:</i></h3>";
  private static final String LOGS_HEADER = "<h3><i>Test execution logs:</i></h3>";
  private static final String ENTITY_PARAM = "entity";

  private final AzureApiClientProvider clientProvider;
  private final TestItemRepository itemRepository;
  private final LogRepository logRepository;
  private final AttachmentDataStoreService attachmentDataStoreService;
  private final DataEncoder dataEncoder;
  private final MimeTypes mimeRepository;
  private final Supplier<InternalTicketAssembler> ticketAssembler;

  public PostTicketCommand(AzureApiClientProvider clientProvider,
      TestItemRepository itemRepository, LogRepository logRepository,
      AttachmentDataStoreService attachmentDataStoreService, DataEncoder dataEncoder,
      ProjectRepository projectRepository, OrganizationRepositoryCustom organizationRepository) {
    super(projectRepository, organizationRepository);
    this.clientProvider = clientProvider;
    this.itemRepository = itemRepository;
    this.logRepository = logRepository;
    this.attachmentDataStoreService = attachmentDataStoreService;
    this.dataEncoder = dataEncoder;
    this.mimeRepository = TikaConfig.getDefaultConfig().getMimeRepository();
    this.ticketAssembler = Suppliers.memoize(
        () -> new InternalTicketAssembler(logRepository, itemRepository, attachmentDataStoreService,
            dataEncoder
        ));
  }

  @Override
  public String getName() {
    return "postTicket";
  }

  protected WorkItemsApi buildWorkItemsApi(ApiClient client) {
    return new WorkItemsApi(client);
  }

  @Override
  protected Ticket invokeCommand(Integration integration, PluginCommandRQ rq) {
    PostTicketRQ ticketRQ = getEntity(ENTITY_PARAM, rq.getArguments(), PostTicketRQ.class);

    ApiClient client = clientProvider.provide(integration);
    String orgName = clientProvider.extractOrganizationName(client,
        integration.getParams().getParams().get(URL).toString()
    );
    String projectName = integration.getParams().getParams().get(PROJECT).toString();

    List<AttachmentInfo> attachments = uploadAttachments(ticketRQ, orgName, projectName, client);
    List<JsonPatchOperation> patchOps = new ArrayList<>();
    String issueType = buildPatchOperations(ticketRQ, patchOps, attachments);

    WorkItemsApi workItemsApi = buildWorkItemsApi(client);
    try {
      WorkItem workItem = workItemsApi.workItemsCreate(orgName, patchOps, projectName, issueType,
          API_VERSION, null, null, null, null
      );
      if (!attachments.isEmpty()) {
        List<JsonPatchOperation> attachmentOps = buildAttachmentPatchOperations(attachments);
        workItemsApi.workItemsUpdate(orgName, attachmentOps, workItem.getId(), projectName,
            API_VERSION, null, null, null, null
        );
      }
      return toTicket(workItem);
    } catch (ApiException e) {
      LOGGER.error("Unable to post issue: {}", e.getMessage(), e);
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
          String.format("Unable to post issue. Code: %s ", e.getCode()), e
      );
    }
  }

  private <T> T getEntity(String key, Map<String, Object> params, Class<T> clazz) {
    try {
      Object raw = params.get(key);
      if (raw == null) {
        throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            "Parameter '" + key + "' was not provided"
        );
      }
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      return mapper.readValue(mapper.writeValueAsString(raw), clazz);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, e.getMessage());
    }
  }

  private String buildPatchOperations(PostTicketRQ ticketRQ, List<JsonPatchOperation> ops,
      List<AttachmentInfo> attachments) {
    String issueType = null;
    String description = "";
    String operation = "add";

    for (PostFormField field : ticketRQ.getFields()) {
      String id = field.getId().replace("_", ".");
      String path = "/fields/" + id;
      String value;
      if ("System_AreaId".equals(field.getId()) || "System_IterationId".equals(field.getId())) {
        String searched = field.getValue().get(0);
        value = field.getDefinedValues().stream()
            .filter(av -> av.getValueName().equals(searched))
            .findFirst().map(AllowedValue::getValueId)
            .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
                "Allowed value not found: " + searched
            ));
      } else if (field.getValue().isEmpty() && !field.getIsRequired()) {
        continue;
      } else {
        value = field.getValue().get(0);
      }

      if ("issuetype".equals(field.getId())) {
        issueType = value;
        continue;
      }
      if ("System.Description".equals(id)) {
        description = value;
        continue;
      }
      ops.add(new JsonPatchOperation(null, operation, path, value));
    }

    description += buildDescription(ticketRQ, attachments);
    ops.add(new JsonPatchOperation(null, operation, "/fields/System.Description", description));
    return issueType;
  }

  private List<JsonPatchOperation> buildAttachmentPatchOperations(List<AttachmentInfo> attachments) {
    List<JsonPatchOperation> ops = new ArrayList<>();
    for (AttachmentInfo info : attachments) {
      Map<String, Object> value = new HashMap<>();
      value.put("rel", "AttachedFile");
      value.put(URL, info.getUrl());
      value.put("attributes", Collections.singletonMap("comment", ""));
      ops.add(new JsonPatchOperation(null, "add", "/relations/-", value));
    }
    return ops;
  }

  private Ticket toTicket(WorkItem workItem) {
    Ticket ticket = new Ticket();
    String id = workItem.getId().toString();
    String url = workItem.getUrl().substring(0, workItem.getUrl().lastIndexOf(id))
        .replace("apis/wit/", "") + "edit/" + id;
    ticket.setId(id);
    ticket.setTicketUrl(url);
    ticket.setStatus(workItem.getFields().get("System.State").toString());
    ticket.setSummary(workItem.getFields().get("System.Title").toString());
    return ticket;
  }

  private List<AttachmentInfo> uploadAttachments(PostTicketRQ ticketRQ, String orgName,
      String projectName, ApiClient client) {
    if (!ticketRQ.getIsIncludeScreenshots() || MapUtils.isEmpty(ticketRQ.getBackLinks())) {
      return Collections.emptyList();
    }
    return ticketRQ.getBackLinks().keySet().stream()
        .map(itemRepository::findById)
        .map(item -> item.map(it -> findLogs(it, ticketRQ.getNumberOfLogs()))
            .orElseGet(Collections::emptyList))
        .flatMap(List::stream)
        .map(Log::getAttachment)
        .filter(Objects::nonNull)
        .map(a -> uploadAttachment(a, orgName, projectName, client))
        .collect(Collectors.toList());
  }

  private AttachmentInfo uploadAttachment(Attachment attachment, String orgName, String projectName,
      ApiClient client) {
    try (InputStream file = attachmentDataStoreService.load(attachment.getFileId())
        .orElseThrow(() -> new ReportPortalException(UNABLE_TO_LOAD_BINARY_DATA))) {
      MimeType mimeType = mimeRepository.forName(attachment.getContentType());
      byte[] bytes = ByteStreams.toByteArray(file);
      String fileName = attachment.getFileId() + mimeType.getExtension();
      AttachmentReference ref = new AttachmentsApi(client).attachmentsCreate(orgName, bytes,
          projectName, API_VERSION, fileName, null, null
      );
      return new AttachmentInfo(fileName, attachment.getFileId(), ref.getUrl(),
          attachment.getContentType()
      );
    } catch (IOException | ApiException | MimeTypeException e) {
      LOGGER.error("Unable to upload attachment: {}", e.getMessage(), e);
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
          "Unable to post ticket: " + e.getMessage(), e
      );
    }
  }

  private String buildDescription(PostTicketRQ ticketRQ, List<AttachmentInfo> attachments) {
    StringBuilder sb = new StringBuilder();
    ticketRQ.getBackLinks().keySet().forEach(
        backLinkId -> appendBackLinkSection(sb, ticketRQ, backLinkId, attachments));
    return sb.toString();
  }

  private void appendBackLinkSection(StringBuilder sb, PostTicketRQ ticketRQ, Long backLinkId,
      List<AttachmentInfo> attachments) {
    String backLink = ticketRQ.getBackLinks().get(backLinkId);
    if (StringUtils.isNotBlank(backLink)) {
      sb.append(BACK_LINK_HEADER).append(String.format(BACK_LINK_PATTERN, backLink));
    }

    if (ticketRQ.getIsIncludeComments() && StringUtils.isNotBlank(backLink)) {
      itemRepository.findById(backLinkId).map(item -> item.getItemResults())
          .flatMap(results -> ofNullable(results).map(r -> r.getIssue())).ifPresent(issue -> {
            if (StringUtils.isNotBlank(issue.getIssueDescription())) {
              sb.append(COMMENTS_HEADER).append(issue.getIssueDescription());
            }
          });
    }

    if (ticketRQ.getIsIncludeLogs() || ticketRQ.getIsIncludeScreenshots()) {
      itemRepository.findById(backLinkId)
          .map(item -> findLogs(item, ticketRQ.getNumberOfLogs()))
          .filter(CollectionUtils::isNotEmpty).ifPresent(logs -> {
            sb.append(LOGS_HEADER);
            logs.forEach(log -> appendLog(sb, log, ticketRQ, attachments));
          });
    }
  }

  private void appendLog(StringBuilder sb, Log log, PostTicketRQ ticketRQ,
      List<AttachmentInfo> attachments) {
    if (ticketRQ.getIsIncludeLogs()) {
      sb.append("<div><pre>").append(formatLogMessage(log)).append("</pre></div>");
    }
    if (ticketRQ.getIsIncludeScreenshots()) {
      ofNullable(log.getAttachment())
          .ifPresent(att -> appendAttachmentRef(sb, att, attachments));
    }
  }

  private void appendAttachmentRef(StringBuilder sb, Attachment attachment,
      List<AttachmentInfo> attachments) {
    if (StringUtils.isBlank(attachment.getContentType()) || StringUtils.isBlank(
        attachment.getFileId())) {
      return;
    }
    attachments.stream().filter(info -> info.getFileId().equals(attachment.getFileId())).findFirst()
        .ifPresent(info -> {
          if (info.getContentType().contains(IMAGE_CONTENT)) {
            sb.append("Attachment:<br>").append("<img src=\"").append(info.getUrl())
                .append("\" alt=\"").append(info.getFileName()).append("\">");
          } else {
            sb.append("Attachment - ").append("<a href=\"").append(info.getUrl()).append("\">")
                .append(info.getFileName()).append("</a>");
          }
        });
  }

  private String formatLogMessage(Log log) {
    StringBuilder sb = new StringBuilder();
    ofNullable(log.getLogTime()).ifPresent(t -> sb.append("Time: ")
        .append(t.atOffset(ZoneOffset.UTC).toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))).append(", "));
    ofNullable(log.getLogLevel()).ifPresent(l -> sb.append("Level: ").append(l).append(", "));
    sb.append("<br>").append("Log: ").append(log.getLogMessage());
    return sb.toString();
  }

  private List<Log> findLogs(TestItem item, int logCount) {
    return ofNullable(item.getLaunchId()).map(
        launchId -> logRepository.findAllUnderTestItemByLaunchIdAndTestItemIdsWithLimit(launchId,
            Collections.singletonList(item.getItemId()), logCount
        )).orElseGet(Collections::emptyList);
  }
}

package com.epam.reportportal.extension.example.command.entity;

import com.epam.reportportal.extension.example.command.role.ProjectMemberCommand;
import com.epam.reportportal.extension.example.command.utils.CommandParamUtils;
import com.epam.reportportal.extension.example.entity.model.EntityResource;
import com.epam.reportportal.extension.example.service.EntityService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.List;
import java.util.Map;

import static com.epam.reportportal.extension.example.command.utils.CommandParamUtils.URL_PARAM;
import static java.util.Optional.ofNullable;

public class GetProjectEntities extends ProjectMemberCommand<List<EntityResource>> {

	private final EntityService entityService;

	public GetProjectEntities(ProjectRepository projectRepository, EntityService entityService) {
		super(projectRepository);
		this.entityService = entityService;
	}

	@Override
	protected List<EntityResource> invokeCommand(Integration integration, Map<String, Object> params) {
		final Long projectId = CommandParamUtils.retrieveLong(params, CommandParamUtils.PROJECT_ID_PARAM);
		final List<EntityResource> entities = entityService.getAllForProject(projectId);
		ofNullable(integration.getParams()).flatMap(it -> ofNullable(it.getParams()))
				.map(details -> details.get(URL_PARAM))
				.map(String::valueOf)
				.filter(url -> UrlValidator.getInstance().isValid(url))
				.ifPresent(url -> entities.forEach(e -> e.setUrl(url + "/" + e.getName())));
		return entities;
	}
}

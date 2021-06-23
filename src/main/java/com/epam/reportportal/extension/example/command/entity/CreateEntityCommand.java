package com.epam.reportportal.extension.example.command.entity;

import com.epam.reportportal.extension.example.command.role.ProjectManagerCommand;
import com.epam.reportportal.extension.example.command.utils.CommandParamUtils;
import com.epam.reportportal.extension.example.command.utils.RequestEntityConverter;
import com.epam.reportportal.extension.example.command.utils.RequestEntityValidator;
import com.epam.reportportal.extension.example.entity.model.CreateEntity;
import com.epam.reportportal.extension.example.service.EntityService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

import java.util.Map;

import static com.epam.reportportal.extension.example.command.utils.CommandParamUtils.ENTITY_PARAM;

public class CreateEntityCommand extends ProjectManagerCommand<OperationCompletionRS> {

	private final RequestEntityConverter requestEntityConverter;
	private final EntityService entityService;

	public CreateEntityCommand(ProjectRepository projectRepository, RequestEntityConverter requestEntityConverter,
			EntityService entityService) {
		super(projectRepository);
		this.requestEntityConverter = requestEntityConverter;
		this.entityService = entityService;
	}

	@Override
	protected OperationCompletionRS invokeCommand(Integration integration, Map<String, Object> params) {
		Long projectId = CommandParamUtils.retrieveLong(params, CommandParamUtils.PROJECT_ID_PARAM);
		CreateEntity createRq = requestEntityConverter.getEntity(ENTITY_PARAM, params, CreateEntity.class);
		RequestEntityValidator.validate(createRq);
		return entityService.create(projectId, createRq);
	}
}

package com.epam.reportportal.extension.example.command.entity;

import com.epam.reportportal.extension.example.command.role.ProjectManagerCommand;
import com.epam.reportportal.extension.example.command.utils.CommandParamUtils;
import com.epam.reportportal.extension.example.service.EntityService;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

import java.util.Map;

public class DeleteEntityCommand extends ProjectManagerCommand<OperationCompletionRS> {

	private final EntityService entityService;

	public DeleteEntityCommand(ProjectRepository projectRepository, EntityService entityService) {
		super(projectRepository);
		this.entityService = entityService;
	}

	@Override
	protected OperationCompletionRS invokeCommand(Integration integration, Map<String, Object> params) {
		final Long id = CommandParamUtils.retrieveLong(params, CommandParamUtils.ID_PARAM);
		return entityService.delete(id);
	}
}

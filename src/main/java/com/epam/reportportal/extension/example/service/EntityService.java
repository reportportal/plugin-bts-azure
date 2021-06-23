package com.epam.reportportal.extension.example.service;

import com.epam.reportportal.extension.example.dao.EntityRepository;
import com.epam.reportportal.extension.example.entity.model.CreateEntity;
import com.epam.reportportal.extension.example.entity.model.EntityResource;
import com.epam.reportportal.extension.example.jooq.tables.pojos.JEntity;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

import java.util.List;
import java.util.stream.Collectors;

import static com.epam.reportportal.extension.example.utils.converter.EntityConverter.TO_RESOURCE;

public class EntityService {

	private final EntityRepository entityRepository;

	public EntityService(EntityRepository entityRepository) {
		this.entityRepository = entityRepository;
	}

	public OperationCompletionRS create(Long projectId, CreateEntity createRq) {
		final JEntity entity = new JEntity();
		entity.setName(createRq.getName());
		entity.setDescription(createRq.getDescription());
		entity.setProjectId(projectId);
		entityRepository.save(entity);
		return new OperationCompletionRS("Entity created");
	}

	public List<EntityResource> getAllForProject(Long projectId) {
		final List<JEntity> entities = entityRepository.findAllByProjectId(projectId);
		return entities.stream().map(TO_RESOURCE).collect(Collectors.toList());
	}

	public OperationCompletionRS delete(Long id) {
		entityRepository.delete(id);
		return new OperationCompletionRS("Entity removed");
	}
}

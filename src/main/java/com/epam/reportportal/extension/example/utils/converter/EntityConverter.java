package com.epam.reportportal.extension.example.utils.converter;

import com.epam.reportportal.extension.example.entity.model.EntityResource;
import com.epam.reportportal.extension.example.jooq.tables.pojos.JEntity;

import java.util.function.Function;

public class EntityConverter {

	public static final Function<JEntity, EntityResource> TO_RESOURCE = e -> {
		final EntityResource resource = new EntityResource();
		resource.setId(e.getId());
		resource.setName(e.getName());
		resource.setDescription(e.getDescription());
		return resource;
	};
}

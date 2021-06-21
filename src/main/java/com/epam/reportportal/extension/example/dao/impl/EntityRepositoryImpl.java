package com.epam.reportportal.extension.example.dao.impl;

import com.epam.reportportal.extension.example.dao.EntityRepository;
import com.epam.reportportal.extension.example.jooq.tables.pojos.JEntity;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.reportportal.extension.example.jooq.tables.JEntity.ENTITY;

public class EntityRepositoryImpl implements EntityRepository {

	private final DSLContext dslContext;

	public EntityRepositoryImpl(DSLContext dslContext) {
		this.dslContext = dslContext;
	}

	@Override
	public JEntity save(JEntity entity) {
		final Long id = dslContext.insertInto(ENTITY).set(getInsertParams(entity)).returning(ENTITY.ID).fetchOne().into(Long.class);
		entity.setId(id);
		return entity;
	}

	private Map<Field<?>, ?> getInsertParams(JEntity ruleResult) {
		Map<Field<?>, Object> insertParams = new HashMap<>();
		insertParams.put(ENTITY.NAME, ruleResult.getName());
		insertParams.put(ENTITY.DESCRIPTION, ruleResult.getDescription());
		return insertParams;
	}

	@Override
	public void delete(Long id) {
		dslContext.deleteFrom(ENTITY).where(ENTITY.ID.eq(id)).execute();
	}

	@Override
	public List<JEntity> findAllByProjectId(Long projectId) {
		return dslContext.select().from(ENTITY).where(ENTITY.PROJECT_ID.eq(projectId)).orderBy(ENTITY.ID).fetchInto(JEntity.class);
	}
}

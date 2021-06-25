package com.epam.reportportal.extension.azure.dao;

import com.epam.reportportal.extension.azure.jooq.tables.pojos.JEntity;

import java.util.List;

public interface EntityRepository extends DaoRepository<JEntity, Long> {

	JEntity save(JEntity entity);

	void delete(Long id);

	List<JEntity> findAllByProjectId(Long projectId);
}

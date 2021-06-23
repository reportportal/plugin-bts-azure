package com.epam.reportportal.extension.example.dao;

import com.epam.reportportal.extension.example.jooq.tables.pojos.JEntity;

import java.util.List;

public interface EntityRepository extends DaoRepository<JEntity, Long> {

	JEntity save(JEntity entity);

	void delete(Long id);

	List<JEntity> findAllByProjectId(Long projectId);
}

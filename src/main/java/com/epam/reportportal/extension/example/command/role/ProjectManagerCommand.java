package com.epam.reportportal.extension.example.command.role;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class ProjectManagerCommand<T> extends ProjectMemberCommand<T> {

	protected ProjectManagerCommand(ProjectRepository projectRepository) {
		super(projectRepository);
	}

	@Override
	protected void validatePermissions(ReportPortalUser user, Project project) {
		ProjectRole projectRole = ofNullable(user.getProjectDetails()).flatMap(detailsMapping -> ofNullable(detailsMapping.get(project.getName())))
				.map(ReportPortalUser.ProjectDetails::getProjectRole)
				.orElseThrow(() -> new ReportPortalException(ErrorType.ACCESS_DENIED));

		BusinessRule.expect(projectRole, ProjectRole.PROJECT_MANAGER::sameOrLowerThan).verify(ErrorType.ACCESS_DENIED);
	}
}

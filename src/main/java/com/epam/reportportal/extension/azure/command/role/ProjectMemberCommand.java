package com.epam.reportportal.extension.azure.command.role;

import static com.epam.reportportal.extension.azure.command.utils.CommandParamUtils.PROJECT_ID_PARAM;
import static com.epam.reportportal.extension.azure.command.utils.CommandParamUtils.retrieveLong;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public abstract class ProjectMemberCommand<T> extends AbstractRoleBasedCommand<T> {

  protected final ProjectRepository projectRepository;

  protected ProjectMemberCommand(ProjectRepository projectRepository) {
    this.projectRepository = projectRepository;
  }

  @Override
  public void validateRole(Map<String, Object> params) {
    Long projectId = retrieveLong(params, PROJECT_ID_PARAM);
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));

    ReportPortalUser user =
        (ReportPortalUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    BusinessRule.expect(user, Objects::nonNull).verify(ErrorType.ACCESS_DENIED);

    validatePermissions(user, project);
  }

  protected void validatePermissions(ReportPortalUser user, Project project) {
    BusinessRule.expect(
        ofNullable(user.getProjectDetails()).flatMap(
            detailsMapping -> ofNullable(detailsMapping.get(project.getName()))),
        Optional::isPresent
    ).verify(ErrorType.ACCESS_DENIED);
  }
}

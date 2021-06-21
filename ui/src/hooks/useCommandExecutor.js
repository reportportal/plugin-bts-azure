export const useCommandExecutor = ({
  lib: { useSelector },
  selectors: { activeProjectSelector, globalIntegrationsSelector, projectInfoSelector },
  utils: { fetch, URLS },
}) => {
  const activeProject = useSelector(activeProjectSelector);
  const integrations = useSelector(globalIntegrationsSelector);
  const projectInfo = useSelector(projectInfoSelector);

  return (command, data = {}, params = {}) => {
    if (!integrations || !integrations[0]) {
      return Promise.reject(new Error('No integrations found'));
    }
    return fetch(URLS.projectIntegrationByIdCommand(activeProject, integrations[0].id, command), {
      method: 'PUT',
      data: {
        projectId: String(projectInfo.projectId),
        ...data,
      },
      ...params,
    });
  };
};

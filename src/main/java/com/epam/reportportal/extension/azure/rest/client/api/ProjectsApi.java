/*
 * Core
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 6.1-preview
 * Contact: nugetvss@microsoft.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.epam.reportportal.extension.azure.rest.client.api;

import com.epam.reportportal.extension.azure.rest.client.ApiCallback;
import com.epam.reportportal.extension.azure.rest.client.ApiClient;
import com.epam.reportportal.extension.azure.rest.client.ApiException;
import com.epam.reportportal.extension.azure.rest.client.ApiResponse;
import com.epam.reportportal.extension.azure.rest.client.Configuration;
import com.epam.reportportal.extension.azure.rest.client.Pair;
import com.epam.reportportal.extension.azure.rest.client.ProgressRequestBody;
import com.epam.reportportal.extension.azure.rest.client.ProgressResponseBody;
import com.epam.reportportal.extension.azure.rest.client.model.TeamProject;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectsApi {
    private ApiClient apiClient;

    public ProjectsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public ProjectsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Build call for projectsGet
     * @param organization The name of the Azure DevOps organization. (required)
     * @param projectId  (required)
     * @param apiVersion Version of the API to use.  This should be set to &#39;6.1-preview.4&#39; to use this version of the api. (required)
     * @param includeCapabilities Include capabilities (such as source control) in the team project result (default: false). (optional)
     * @param includeHistory Search within renamed projects (that had such name in the past). (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public com.squareup.okhttp.Call projectsGetCall(String organization, String projectId, String apiVersion, Boolean includeCapabilities, Boolean includeHistory, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/{organization}/_apis/projects/{projectId}"
            .replaceAll("\\{" + "organization" + "\\}", apiClient.escapeString(organization.toString()))
            .replaceAll("\\{" + "projectId" + "\\}", apiClient.escapeString(projectId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (includeCapabilities != null) {
            localVarQueryParams.addAll(apiClient.parameterToPair("includeCapabilities", includeCapabilities));
        }
        if (includeHistory != null) {
            localVarQueryParams.addAll(apiClient.parameterToPair("includeHistory", includeHistory));
        }
        if (apiVersion != null) {
            localVarQueryParams.addAll(apiClient.parameterToPair("api-version", apiVersion));
        }

        Map<String, String> localVarHeaderParams = new HashMap<String, String>();

        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        if(progressListener != null) {
            apiClient.getHttpClient().networkInterceptors().add(new com.squareup.okhttp.Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                    .build();
                }
            });
        }

        String[] localVarAuthNames = new String[] { "oauth2", "accessToken" };
        return apiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAuthNames, progressRequestListener);
    }

    @SuppressWarnings("rawtypes")
    private com.squareup.okhttp.Call projectsGetValidateBeforeCall(String organization, String projectId, String apiVersion, Boolean includeCapabilities, Boolean includeHistory, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        
        // verify the required parameter 'organization' is set
        if (organization == null) {
            throw new ApiException("Missing the required parameter 'organization' when calling projectsGet(Async)");
        }
        
        // verify the required parameter 'projectId' is set
        if (projectId == null) {
            throw new ApiException("Missing the required parameter 'projectId' when calling projectsGet(Async)");
        }
        
        // verify the required parameter 'apiVersion' is set
        if (apiVersion == null) {
            throw new ApiException("Missing the required parameter 'apiVersion' when calling projectsGet(Async)");
        }
        

        com.squareup.okhttp.Call call = projectsGetCall(organization, projectId, apiVersion, includeCapabilities, includeHistory, progressListener, progressRequestListener);
        return call;

    }

    /**
     * 
     * Get project with the specified id or name, optionally including capabilities.
     * @param organization The name of the Azure DevOps organization. (required)
     * @param projectId  (required)
     * @param apiVersion Version of the API to use.  This should be set to &#39;6.1-preview.4&#39; to use this version of the api. (required)
     * @param includeCapabilities Include capabilities (such as source control) in the team project result (default: false). (optional)
     * @param includeHistory Search within renamed projects (that had such name in the past). (optional)
     * @return TeamProject
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public TeamProject projectsGet(String organization, String projectId, String apiVersion, Boolean includeCapabilities, Boolean includeHistory) throws ApiException {
        ApiResponse<TeamProject> resp = projectsGetWithHttpInfo(organization, projectId, apiVersion, includeCapabilities, includeHistory);
        return resp.getData();
    }

    /**
     * 
     * Get project with the specified id or name, optionally including capabilities.
     * @param organization The name of the Azure DevOps organization. (required)
     * @param projectId  (required)
     * @param apiVersion Version of the API to use.  This should be set to &#39;6.1-preview.4&#39; to use this version of the api. (required)
     * @param includeCapabilities Include capabilities (such as source control) in the team project result (default: false). (optional)
     * @param includeHistory Search within renamed projects (that had such name in the past). (optional)
     * @return ApiResponse&lt;TeamProject&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<TeamProject> projectsGetWithHttpInfo(String organization, String projectId, String apiVersion, Boolean includeCapabilities, Boolean includeHistory) throws ApiException {
        com.squareup.okhttp.Call call = projectsGetValidateBeforeCall(organization, projectId, apiVersion, includeCapabilities, includeHistory, null, null);
        Type localVarReturnType = new TypeToken<TeamProject>(){}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    /**
     *  (asynchronously)
     * Get project with the specified id or name, optionally including capabilities.
     * @param organization The name of the Azure DevOps organization. (required)
     * @param projectId  (required)
     * @param apiVersion Version of the API to use.  This should be set to &#39;6.1-preview.4&#39; to use this version of the api. (required)
     * @param includeCapabilities Include capabilities (such as source control) in the team project result (default: false). (optional)
     * @param includeHistory Search within renamed projects (that had such name in the past). (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public com.squareup.okhttp.Call projectsGetAsync(String organization, String projectId, String apiVersion, Boolean includeCapabilities, Boolean includeHistory, final ApiCallback<TeamProject> callback) throws ApiException {

        ProgressResponseBody.ProgressListener progressListener = null;
        ProgressRequestBody.ProgressRequestListener progressRequestListener = null;

        if (callback != null) {
            progressListener = new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    callback.onDownloadProgress(bytesRead, contentLength, done);
                }
            };

            progressRequestListener = new ProgressRequestBody.ProgressRequestListener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
                    callback.onUploadProgress(bytesWritten, contentLength, done);
                }
            };
        }

        com.squareup.okhttp.Call call = projectsGetValidateBeforeCall(organization, projectId, apiVersion, includeCapabilities, includeHistory, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<TeamProject>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
}

/*
 * WorkItemTracking
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
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemTypeFieldWithReferencesList;
import com.epam.reportportal.extension.azure.rest.client.model.workitem.WorkItemTypeFieldWithReferences;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkItemTypesFieldApi {
    private ApiClient apiClient;

    public WorkItemTypesFieldApi() {
        this(Configuration.getDefaultApiClient());
    }

    public WorkItemTypesFieldApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Build call for workItemTypesFieldList
     * @param organization The name of the Azure DevOps organization. (required)
     * @param project Project ID or project name (required)
     * @param type Work item type. (required)
     * @param apiVersion Version of the API to use.  This should be set to &#39;6.1-preview.3&#39; to use this version of the api. (required)
     * @param expand Expand level for the API response. Properties: to include allowedvalues, default value, isRequired etc. as a part of response; None: to skip these properties. (optional)
     * @param progressListener Progress listener
     * @param progressRequestListener Progress request listener
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public Call workItemTypesFieldListCall(String organization, String project, String type, String apiVersion, String expand, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/{organization}/{project}/_apis/wit/workitemtypes/{type}/fields"
            .replaceAll("\\{" + "organization" + "\\}", apiClient.escapeString(organization.toString()))
            .replaceAll("\\{" + "project" + "\\}", apiClient.escapeString(project.toString()))
            .replaceAll("\\{" + "type" + "\\}", apiClient.escapeString(type.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        if (expand != null) {
            localVarQueryParams.addAll(apiClient.parameterToPair("$expand", expand));
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
    private Call workItemTypesFieldListValidateBeforeCall(String organization, String project, String type, String apiVersion, String expand, final ProgressResponseBody.ProgressListener progressListener, final ProgressRequestBody.ProgressRequestListener progressRequestListener) throws ApiException {
        
        // verify the required parameter 'organization' is set
        if (organization == null) {
            throw new ApiException("Missing the required parameter 'organization' when calling workItemTypesFieldList(Async)");
        }
        
        // verify the required parameter 'project' is set
        if (project == null) {
            throw new ApiException("Missing the required parameter 'project' when calling workItemTypesFieldList(Async)");
        }
        
        // verify the required parameter 'type' is set
        if (type == null) {
            throw new ApiException("Missing the required parameter 'type' when calling workItemTypesFieldList(Async)");
        }
        
        // verify the required parameter 'apiVersion' is set
        if (apiVersion == null) {
            throw new ApiException("Missing the required parameter 'apiVersion' when calling workItemTypesFieldList(Async)");
        }

        Call call = workItemTypesFieldListCall(organization, project, type, apiVersion, expand, progressListener, progressRequestListener);
        return call;
    }

    /**
     * 
     * Get a list of fields for a work item type with detailed references.
     * @param organization The name of the Azure DevOps organization. (required)
     * @param project Project ID or project name (required)
     * @param type Work item type. (required)
     * @param apiVersion Version of the API to use.  This should be set to &#39;6.1-preview.3&#39; to use this version of the api. (required)
     * @param expand Expand level for the API response. Properties: to include allowedvalues, default value, isRequired etc. as a part of response; None: to skip these properties. (optional)
     * @return List&lt;WorkItemTypeFieldWithReferences&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public List<WorkItemTypeFieldWithReferences> workItemTypesFieldList(String organization, String project, String type, String apiVersion, String expand) throws ApiException {
        ApiResponse<List<WorkItemTypeFieldWithReferences>> resp = workItemTypesFieldListWithHttpInfo(organization, project, type, apiVersion, expand);
        return resp.getData();
    }

    /**
     * 
     * Get a list of fields for a work item type with detailed references.
     * @param organization The name of the Azure DevOps organization. (required)
     * @param project Project ID or project name (required)
     * @param type Work item type. (required)
     * @param apiVersion Version of the API to use.  This should be set to &#39;6.1-preview.3&#39; to use this version of the api. (required)
     * @param expand Expand level for the API response. Properties: to include allowedvalues, default value, isRequired etc. as a part of response; None: to skip these properties. (optional)
     * @return ApiResponse&lt;List&lt;WorkItemTypeFieldWithReferences&gt;&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<List<WorkItemTypeFieldWithReferences>> workItemTypesFieldListWithHttpInfo(String organization, String project, String type, String apiVersion, String expand) throws ApiException {
        Call call = workItemTypesFieldListValidateBeforeCall(organization, project, type, apiVersion, expand, null, null);
        Type localVarReturnType = new TypeToken<WorkItemTypeFieldWithReferencesList>(){}.getType();
        ApiResponse<WorkItemTypeFieldWithReferencesList> response = apiClient.execute(call, localVarReturnType);
        return new ApiResponse<>(response.getStatusCode(), response.getHeaders(), response.getData().getValue());
    }

    /**
     *  (asynchronously)
     * Get a list of fields for a work item type with detailed references.
     * @param organization The name of the Azure DevOps organization. (required)
     * @param project Project ID or project name (required)
     * @param type Work item type. (required)
     * @param apiVersion Version of the API to use.  This should be set to &#39;6.1-preview.3&#39; to use this version of the api. (required)
     * @param expand Expand level for the API response. Properties: to include allowedvalues, default value, isRequired etc. as a part of response; None: to skip these properties. (optional)
     * @param callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public Call workItemTypesFieldListAsync(String organization, String project, String type, String apiVersion, String expand, final ApiCallback<List<WorkItemTypeFieldWithReferences>> callback) throws ApiException {

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

        Call call = workItemTypesFieldListValidateBeforeCall(organization, project, type, apiVersion, expand, progressListener, progressRequestListener);
        Type localVarReturnType = new TypeToken<List<WorkItemTypeFieldWithReferences>>(){}.getType();
        apiClient.executeAsync(call, localVarReturnType, callback);
        return call;
    }
}

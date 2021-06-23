package com.epam.reportportal.extension.example.command.binary;

import com.epam.reportportal.extension.PluginCommand;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.io.IOUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import static java.util.Optional.ofNullable;

/**
 * Command that finds the file by provided 'fileKey' and loads it to the response
 *
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public class GetFileCommand implements PluginCommand<Boolean> {

	private static final String FILE_KEY = "fileKey";

	private static final Integer BUFFER_BYTE_SIZE = 1024 * 16;

	private final String resourcesDir;
	private final String propertyFile;

	public GetFileCommand(String resourcesDir, String propertyFile) {
		this.resourcesDir = resourcesDir;
		this.propertyFile = propertyFile;
	}

	@Override
	public Boolean executeCommand(Integration integration, Map<String, Object> params) {
		Properties binaryDataProperties = loadProperties();
		String fileName = retrieveFileName(params.get(FILE_KEY), binaryDataProperties);
		ServletRequestAttributes requestAttributes = getRequestAttributes();
		return loadFileToResponse(requestAttributes, fileName);
	}

	private Properties loadProperties() {
		Properties binaryDataProperties = new Properties();
		try (InputStream propertiesStream = Files.newInputStream(Paths.get(resourcesDir, propertyFile))) {
			binaryDataProperties.load(propertiesStream);
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage());
		}
		return binaryDataProperties;
	}

	private String retrieveFileName(Object fileKey, Properties binaryDataProperties) {
		String key = ofNullable(fileKey).map(String::valueOf)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "File key is not provided"));

		return ofNullable(binaryDataProperties.getProperty(key)).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA,
				"File with such file key doesn't exist"
		));
	}

	private ServletRequestAttributes getRequestAttributes() {
		return ofNullable(RequestContextHolder.getRequestAttributes()).map(attributes -> (ServletRequestAttributes) attributes)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to get request attributes"));
	}

	private Boolean loadFileToResponse(ServletRequestAttributes requestAttributes, String fileKey) {
		return ofNullable(requestAttributes.getResponse()).map(response -> {
			try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream(), BUFFER_BYTE_SIZE);
					InputStream inputStream = Files.newInputStream(Paths.get(resourcesDir, fileKey))) {
				IOUtils.copy(inputStream, bufferedOutputStream, BUFFER_BYTE_SIZE);
				return true;
			} catch (IOException e) {
				throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, e.getMessage());
			}
		}).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, "Unable to obtain response"));
	}

}

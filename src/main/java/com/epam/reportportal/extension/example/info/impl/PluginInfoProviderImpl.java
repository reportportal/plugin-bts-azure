package com.epam.reportportal.extension.example.info.impl;

import com.epam.reportportal.extension.example.info.PluginInfoProvider;
import com.epam.ta.reportportal.entity.integration.IntegrationType;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PluginInfoProviderImpl implements PluginInfoProvider {

	private static final String BINARY_DATA_KEY = "binaryData";
	private static final String DESCRIPTION_KEY = "description";
	private static final String METADATA_KEY = "metadata";

	private static final String PLUGIN_DESCRIPTION =
			"Example plugin";
	public static final Map<String, Object> PLUGIN_METADATA = new HashMap<>();
	static {
        PLUGIN_METADATA.put("embedded", true);
        PLUGIN_METADATA.put("multiple", true);
    }


	private final String resourcesDir;
	private final String propertyFile;

	public PluginInfoProviderImpl(String resourcesDir, String propertyFile) {
		this.resourcesDir = resourcesDir;
		this.propertyFile = propertyFile;
	}

	@Override
	public IntegrationType provide(IntegrationType integrationType) {
		loadBinaryDataInfo(integrationType);
		updateDescription(integrationType);
		updateMetadata(integrationType);
		return integrationType;
	}

	private void loadBinaryDataInfo(IntegrationType integrationType) {
		Map<String, Object> details = integrationType.getDetails().getDetails();
		if (ofNullable(details.get(BINARY_DATA_KEY)).isEmpty()) {
			try (InputStream propertiesStream = Files.newInputStream(Paths.get(resourcesDir, propertyFile))) {
				Properties binaryDataProperties = new Properties();
				binaryDataProperties.load(propertiesStream);
				Map<String, String> binaryDataInfo = binaryDataProperties.entrySet()
						.stream()
						.collect(HashMap::new,
								(map, entry) -> map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue())),
								HashMap::putAll
						);
				details.put(BINARY_DATA_KEY, binaryDataInfo);
			} catch (IOException ex) {
				throw new ReportPortalException(ErrorType.UNABLE_TO_LOAD_BINARY_DATA, ex.getMessage());
			}
		}
	}

	private void updateDescription(IntegrationType integrationType) {
		Map<String, Object> details = integrationType.getDetails().getDetails();
		details.put(DESCRIPTION_KEY, PLUGIN_DESCRIPTION);
	}

	private void updateMetadata(IntegrationType integrationType) {
        Map<String, Object> details = integrationType.getDetails().getDetails();
        details.put(METADATA_KEY, PLUGIN_METADATA);
    }
}

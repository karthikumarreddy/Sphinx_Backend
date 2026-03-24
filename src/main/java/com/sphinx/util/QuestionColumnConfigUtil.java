package com.sphinx.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.ofbiz.base.util.UtilProperties;

public class QuestionColumnConfigUtil {

	private static final String CONFIG_NAME = "questionColumnConfig";

	public static class ColumnConfig {
		public final int index;
		public final String field;
		public final String label;
		public final boolean required;
		public final String type;

		public ColumnConfig(int index, String field, String label, boolean required, String type) {
			this.index = index;
			this.field = field;
			this.label = label;
			this.required = required;
			this.type = type;
		}
	}

	// Cache — loaded once per server startup
	private static List<ColumnConfig> cachedConfigs = null;

	public static List<ColumnConfig> getColumnConfigs() {
		if (cachedConfigs != null)
			return cachedConfigs;

		List<ColumnConfig> configs = new ArrayList<>();

		// Read total column count first
		int count = Integer.parseInt(UtilProperties.getPropertyValue(CONFIG_NAME, "column.count", "0"));

		for (int i = 0; i < count; i++) {
			String prefix = "column." + i + ".";

			String label = UtilProperties.getPropertyValue(CONFIG_NAME, prefix + "label", "");
			String field = UtilProperties.getPropertyValue(CONFIG_NAME, prefix + "field", "");
			boolean required = "true".equalsIgnoreCase(UtilProperties.getPropertyValue(CONFIG_NAME, prefix + "required", "false"));
			String type = UtilProperties.getPropertyValue(CONFIG_NAME, prefix + "type", "String");

			if (!field.isEmpty()) {
				configs.add(new ColumnConfig(i, field, label, required, type));
			}
		}

		cachedConfigs = configs;
		return cachedConfigs;
	}


}
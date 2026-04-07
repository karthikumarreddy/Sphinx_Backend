package com.sphinx.util;

import java.util.Map;

public class ServiceHelper {

	public static boolean isError(Map<String, Object> serviceResult) {
		return serviceResult.containsKey("responseMessage") && "error".equalsIgnoreCase((String) serviceResult.get("responseMessage"));
	}

}

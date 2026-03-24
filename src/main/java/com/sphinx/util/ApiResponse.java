package com.sphinx.util;

import java.util.Map;

import org.apache.ofbiz.service.ServiceUtil;

public class ApiResponse {
//	{
//		  "success": True or false, 
//		  "statusCode": 200,
//		  "message": "User created successfully",
//		  "data": {},
//
//		}

	public static Map<String, Object> response(boolean success, int statusCode, String message, Object data) {
		Map<String, Object> result;

		if (success) {
			result = ServiceUtil.returnSuccess(message);
		}
		else {
			result = ServiceUtil.returnError(message);
		}

		result.put("success", success);
		result.put("statusCode", statusCode);
		result.put("data", data);
		return result;

	}

}

package com.sphinx.util;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
//	{
//		  "success": True or false, 
//		  "statusCode": 200,
//		  "message": "User created successfully",
//		  "data": {},
//
//		}

	public static Map<String, Object> response(boolean success, int statusCode, String message, Object data) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", success);
		result.put("statusCode", statusCode);
		result.put("message", message);
		result.put("data", data);
		return result;

	}

}

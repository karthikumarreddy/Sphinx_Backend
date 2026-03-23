package com.sphinx.events;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ofbiz.service.LocalDispatcher;

public class UserEvents {

	public static String loginUser(HttpServletRequest request, HttpServletResponse response) {
		LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

		Map<String, Object> context = new HashMap<String, Object>();
		try {
			String userName = request.getParameter("userName");
			String password = request.getParameter("password");
			String USERNAME_REGEX = "^[a-zA-Z0-9]{5,15}$";
			String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
			if (!Pattern.matches(USERNAME_REGEX, userName)) {
				return "error";
			}
			if (!Pattern.matches(PASSWORD_REGEX, password)) {
				return "error";
			}

			Map<String, Object> serviceResult = dispatcher.runSync("loginUser", context);

			return serviceResult.get("message").toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}

	}

}

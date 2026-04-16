package com.sphinx.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;

public class AuthenticationFilter extends HttpFilter {

	private static final long serialVersionUID = -2943659051309750903L;


	private boolean isAuthenticated(HttpSession session) {
		if (session == null)
			return false;

		if (session.getAttribute("userLogin") == null)
			return false;

		return true;
	}


	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
					throws IOException, ServletException {

		boolean isAuth = isAuthenticated(request.getSession(false));
		if (!isAuth) {

			response.setContentType("application/json");
			response.getWriter().write(
							"{\"responseMessage\": \"error\", \"errorMessage\":\"Please Sign In to your Account to procedd further!\" }");
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);
			return;
		}
		super.doFilter(request, response, chain);

	}

}

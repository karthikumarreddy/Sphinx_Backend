package com.sphinx.security;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.webapp.control.JWTManager;
import org.apache.ofbiz.ws.rs.common.AuthenticationScheme;

public class ApiAuthFilter implements ContainerRequestFilter {

	@Context
	HttpServletRequest request;

	@Context
	ServletContext servletContext;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		// intercept the request if the authorization header is not present.
		if (!isTokenBasedAuthentication(authorizationHeader)) {
			Response response = Response.status(Response.Status.UNAUTHORIZED.getStatusCode())
							.entity(Response.Status.UNAUTHORIZED.getReasonPhrase()).build();
			requestContext.abortWith(response);
		}
		
		Delegator delegator = (Delegator) servletContext.getAttribute("delegator");

		// in built method
		String jwtToken = JWTManager.getHeaderAuthBearerToken(request);

		JWTManager.getJWTKey(delegator);

	}

	private boolean isTokenBasedAuthentication(String authorizationHeader) {
		return authorizationHeader != null
						&& authorizationHeader.toLowerCase().startsWith(AuthenticationScheme.BEARER.getScheme().toLowerCase() + " ");
	}

}

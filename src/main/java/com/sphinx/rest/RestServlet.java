package com.sphinx.rest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestServlet extends ResourceConfig {
	
	public RestServlet() {
		packages("com.sphinx.resourse");
		register(JacksonFeature.class);
		System.out.println(" in rest servlet ");
	}
	

}

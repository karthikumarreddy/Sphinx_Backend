package com.sphinx.rest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestFrontControllerServlet extends ResourceConfig {
	
	public RestFrontControllerServlet() {
		packages("com.sphinx.resources");   
		register(JacksonFeature.class);
		register(MultiPartFeature.class);
		System.out.println(" in rest servlet ");
	}
	

}

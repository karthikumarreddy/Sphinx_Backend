package com.sphinx.resources;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoginResource {

	@Context
	private HttpServletRequest request;

	@Context
	private ServletContext servletContext;

	private Delegator getDelegator() {
		Delegator delegator = (Delegator) servletContext.getAttribute("delegator");
		if (delegator == null) {
			delegator = DelegatorFactory.getDelegator("default");
		}
		return delegator;
	}

	private LocalDispatcher getDispatcher() {
		LocalDispatcher dispatcher = (LocalDispatcher) servletContext.getAttribute("dispatcher");
		if (dispatcher == null) {
			dispatcher = ServiceContainer.getLocalDispatcher("Sphinx", getDelegator());
		}
		return dispatcher;
	}

	@POST
	@Path("/login")
	public Response loginUser(Map<String, Object> input) {
		try {
			String userName = (String) input.get("userName");
			String password = (String) input.get("password");

			if (userName == null || password == null)
				return Response.status(400).entity(Map.of("error", "Username and password are required.")).build();

			if (!Pattern.matches("^[a-zA-Z0-9]{4,20}$", userName))
				return Response.status(400).entity(Map.of("error",
						"Invalid username. It must be 4–20 characters long \n and contain only letters and numbers."))
						.build();

			if (!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", password))
				return Response.status(400).entity(Map.of("error",
						"Invalid password format. \n Password must be at least 8 characters \n and include uppercase, lowercase, a number, and a special character."))
						.build();

			Delegator delegator = getDelegator();
			GenericValue user = delegator.findOne("UserLogin", true, UtilMisc.toMap("userLoginId", userName));

			if (user == null)
				return Response.status(404).entity(Map.of("error", "No account found with the provided username."))
						.build();

			if (!user.get("enabled").equals("Y"))
				return Response.status(403)
						.entity(Map.of("error",
								"Your account has been suspended. Please contact the administrator for assistance."))
						.build();

			LocalDispatcher dispatcher = getDispatcher();
			if (dispatcher == null)
				return Response.status(500)
						.entity(Map.of("error", "An internal server error occurred. \n Please try again later."))
						.build();

			Map<String, Object> result = dispatcher.runSync("loginUser", input);
			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500)
					.entity(Map.of("error", "An unexpected error occurred.\n Please try again later.")).build();
		}
	}

	@POST
	@Path("/signup")
	public Response signupUser(Map<String, Object> input) {
		System.out.println("INPUT : " + input);

		try {
			if (input == null)
				return Response.status(400).entity(Map.of("error", "Request body is missing or malformed.")).build();

			String userName = (String) input.get("userName");
			String firstName = (String) input.get("firstName");
			String lastName = (String) input.get("lastName");
			String mobileNo = (String) input.get("mobileNo");
			String email = (String) input.get("email");
			String password = (String) input.get("password");

			String role = (String) input.get("role");

			if (userName == null || firstName == null || lastName == null || mobileNo == null || email == null
					|| password == null || role == null)
				return Response.status(400)
						.entity(Map.of("error", "All fields are required. Please ensure no fields are left empty."))
						.build();

			if (!Pattern.matches("^[a-zA-Z0-9]{4,20}$", userName))
				return Response.status(400).entity(Map.of("error",
						"Invalid username. \n It must be 4–20 characters long \n and contain only letters and numbers."))
						.build();

			Delegator delegator = getDelegator();
			GenericValue user = delegator.findOne("UserLogin", true, UtilMisc.toMap("userLoginId", userName));

			if (user != null)
				return Response.status(409).entity(
						Map.of("error", "This username is already taken. \n Please choose a different username."))
						.build();

			if (!Pattern.matches("^[A-Za-z ]{2,20}$", firstName))
				return Response.status(400)
						.entity(Map.of("error",
								"Invalid first name. \n It must be 2–20 characters \n and contain only letters."))
						.build();

			if (!Pattern.matches("^[A-Za-z ]{1,20}$", lastName))
				return Response.status(400).entity(Map.of("error", "Invalid last name. ")).build();

			if (!Pattern.matches("^[6-9]\\d{9}$", mobileNo))
				return Response.status(400)
						.entity(Map.of("error",
								"Invalid mobile number. \n Please enter a valid 10-digit Indian mobile number."))
						.build();

			if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email))
				return Response.status(400)
						.entity(Map.of("error", "Invalid email address. \n Please provide a valid email.")).build();

			if (!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", password))
				return Response.status(400).entity(Map.of("error",
						"Password must be at least 8 characters \n and include uppercase, lowercase, a number, and a special character (@$!%*?&)."))
						.build();

			if (!role.equals("admin") && !role.equals("user"))
				return Response.status(400)
						.entity(Map.of("error", "Invalid role. Accepted values are 'admin' or 'user'.")).build();

			LocalDispatcher dispatcher = getDispatcher();
			Map<String, Object> result = dispatcher.runSync("signupUser", input);
			return Response.status(201).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity(
					Map.of("error", "An unexpected error occurred during registration.\n Please try again later."))
					.build();
		}
	}
}
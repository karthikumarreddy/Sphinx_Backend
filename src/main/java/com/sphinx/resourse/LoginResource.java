package com.sphinx.resourse;

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
	private ServletContext servletContext; // ← ADD THIS

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
			dispatcher = ServiceContainer.getLocalDispatcher("Sphinx", // must match localDispatcherName in web.xml
					getDelegator());
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
				return Response.status(400).entity(Map.of("error", "required fields are null")).build();

			Delegator delegator = getDelegator();
			GenericValue user = delegator.findOne("UserLogin", true,
					UtilMisc.toMap("userLoginId", input.get("userName")));
			// enabled y means account is active enabled n account inactive

			if (!Pattern.matches("^[a-zA-Z0-9]{4,20}$", userName))
				return Response.status(400).entity(Map.of("error",
						"invalid user name username length should be gteater than 4 and should contain numbers and letters only"))
						.build();

			if (user == null) {
				return Response.status(404).entity(Map.of("error", "User not found")).build();
			}
			if (!user.get("enabled").equals("Y")) {
				return Response.status(404).entity(Map.of("error", "User is bocked my the admin")).build();
			}
			if (!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", password))
				return Response.status(400).entity(Map.of("erreo", "invalid password")).build();

			LocalDispatcher dispatcher = getDispatcher();
			if (dispatcher == null) {
				return Response.status(500).entity(Map.of("error", "Dispatcher is still null")).build();
			}

			Map<String, Object> result = dispatcher.runSync("loginUser", input);

			return Response.status(200).entity(result).build();

		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();
		}
	}

	@POST
	@Path("/signup")
	public Response signupUser(Map<String, Object> input) {
		System.out.println(" INPUT : " + input);

		try {
			if (input == null) {
				return Response.status(400).entity(Map.of("error", "input is null ")).build();
			}
			String userName = (String) input.get("userName");
			String firstName = (String) input.get("firstName");
			String lastName = (String) input.get("lastName");
			String mobileNo = (String) input.get("mobileNo");
			String email = (String) input.get("email");
			String password = (String) input.get("password");
			String currentPassword = (String) input.get("currentPassword");
			String role = (String) input.get("role");

			Delegator delegator = getDelegator();

			GenericValue user = delegator.findOne("UserLogin", true, UtilMisc.toMap("userLoginId", userName));

			if (userName == null || firstName == null || lastName == null || mobileNo == null || email == null
					|| password == null || currentPassword == null || !currentPassword.equals(password) || role == null)
				return Response.status(400).entity(Map.of("error", "required fields are null")).build();

			if (!Pattern.matches("^[a-zA-Z0-9]{4,20}$", userName))
				return Response.status(400).entity(Map.of("error",
						"invalid user name username length should be gteater than 4 and should contain numbers and letters only"))
						.build();
			if (user != null) {
				return Response.status(409).entity(Map.of("error", "Username already exists")).build();
			}

			if (!Pattern.matches("^[A-Za-z ]{2,50}$", firstName))
				return Response.status(400).entity(Map.of("error", "first name should contain only alphabets")).build();

			if (!Pattern.matches("^[A-Za-z ]{2,50}$", lastName))
				return Response.status(400).entity(Map.of("error", "last name should contain only alphabets")).build();

			if (!Pattern.matches("^[6-9]\\d{9}$", mobileNo))
				return Response.status(400).entity(Map.of("error", "invalid mobile no it must be excatly 10 digits"))
						.build();

			if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email))
				return Response.status(400).entity(Map.of("error", "email is invalid ")).build();

			if (!Pattern.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", password))
				return Response.status(400).entity(Map.of("errer", "invalid password")).build();

			if (!currentPassword.equals(password))
				return Response.status(400).entity(Map.of("error", "current password does not match password "))
						.build();
			if (!role.equals("admin") && !role.equals("user"))
				return Response.status(400).entity(Map.of("error", "role is manditory")).build();

			LocalDispatcher dispatcher = getDispatcher();
			Map<String, Object> result = dispatcher.runSync("signupUser", input);

			return Response.status(201).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity(Map.of("error", e.getMessage())).build();

		}

	}
}

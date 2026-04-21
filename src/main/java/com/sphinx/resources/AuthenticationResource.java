package com.sphinx.resources;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.webapp.control.LoginWorker;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticationResource {

	@POST
	@Path("/login")
	public Response loginUser(@Context HttpServletRequest request, @Context HttpServletResponse response) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
			Delegator delegator = (Delegator)request.getAttribute("delegator");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}
			String userName = (String) request.getAttribute("userName");
			String password = (String) request.getAttribute("password");
			if (UtilValidate.isEmpty(userName)) {
				return Response.status(400).entity("UserName is required ").build();
			}
			if (UtilValidate.isEmpty(password)) {
				return Response.status(400).entity("Password is required ").build();
			}
			// Map<String, Object> input = new HashMap<String, Object>();
			// input.put("userName", userName);
			// input.put("password", password);
			//
			// Map<String, Object> result = dispatcher.runSync("loginUser", input);
			request.setAttribute("USERNAME", userName);
			request.setAttribute("PASSWORD", password);
			if ("success".equalsIgnoreCase(LoginWorker.login(request, response))) {
				Map<String, Object> result = ServiceUtil.returnSuccess("Signed In Successfully!");
				HttpSession session = request.getSession(false);
				if(UtilValidate.isNotEmpty(session)) {
					GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
					if(UtilValidate.isNotEmpty(userLogin)) {
						GenericValue userRole = EntityQuery.use(delegator).from("UserRoleWithDesc")
										.where("partyId", userLogin.getString("partyId")).queryFirst();
						session.setAttribute("userRole", userRole);
						result.put("userRole", userRole);
					}
				}
				
				return Response.status(200).entity(result).build();
			}
			Map<String, Object> result = ServiceUtil.returnError("Invalid Credentials!");
			// if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(result).build();
			// }

			// HttpSession session = request.getSession();
			// if (session != null) {
			// session.setAttribute("partyId", result.get("partyId"));
			// }
			// return Response.status(200).entity(result).build();

		} catch (Exception e) {
			return Response.status(500).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@POST
	@Path("/signup")
	public Response signupUser(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			if (UtilValidate.isEmpty(dispatcher)) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(ServiceUtil.returnError("Unexpected Error Occured! Try again after Sometime!")).build();
			}

			String userName = (String) request.getAttribute("userName");
			String firstName = (String) request.getAttribute("firstName");
			String lastName = (String) request.getAttribute("lastName");
			String email = (String) request.getAttribute("email");
			String password = (String) request.getAttribute("password");
			String confirmPassword = (String) request.getAttribute("confirmPassword");
			String role = (String) request.getAttribute("role");

			// if (UtilValidate.isEmpty(userName)) {
			// return Response.status(400).entity("UserName is required").build();
			// }
			// if (UtilValidate.isEmpty(firstName)) {
			// return Response.status(400).entity("FirstName is required").build();
			// }
			// if (UtilValidate.isEmpty(lastName)) {
			// return Response.status(400).entity("LastName is required").build();
			// }
			// if (UtilValidate.isEmpty(email)) {
			// return Response.status(400).entity("Email is required").build();
			// }
			// if (UtilValidate.isEmpty(password)) {
			// return Response.status(400).entity("Password is required").build();
			// }
			// if (UtilValidate.isEmpty(role)) {
			// return Response.status(400).entity("Role is required").build();
			// }

			Map<String, Object> result = dispatcher.runSync("signupUser",
							UtilMisc.toMap("userName", userName, "firstName", firstName, "lastName", lastName, "email", email, "password",
											password, "confirmPassword", confirmPassword, "role", role, "userLogin",
											request.getSession().getAttribute("userLogin")));
			if (result.containsKey("responseMessage") && "error".equalsIgnoreCase((String) result.get("responseMessage"))) {
				return Response.status(Response.Status.BAD_REQUEST).entity(result).build();
			}
			return Response.status(Response.Status.CREATED).entity(result).build();

		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}

	@GET
	@Path("/logout")
	public Response logout(@Context HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (UtilValidate.isNotEmpty(session)) {
			session.invalidate();
		}
		return Response.status(200).entity(ServiceUtil.returnSuccess("Signed Out Successfully!")).build();
	}

}

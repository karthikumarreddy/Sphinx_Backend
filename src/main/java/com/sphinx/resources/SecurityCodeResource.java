package com.sphinx.resources;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/securityCode")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SecurityCodeResource {

	private static final String MODULE = SecurityCodeResource.class.getName();

	@GET
	public Response sendSecurityCode(@Context HttpServletRequest request, @Context HttpServletResponse response) {

		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			HttpSession session = request.getSession(false);

			String examId = (String) request.getParameter("examId");

			String partyId = (String) request.getParameter("partyId");

			if (UtilValidate.isEmpty(partyId)) {
				if (UtilValidate.isNotEmpty(session)) {
					GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
					if (UtilValidate.isNotEmpty(userLogin)) {
						partyId = userLogin.getString("partyId");
					}
				}
			}

			Map<String, Object> result = dispatcher.runSync("sendSecurityCode", UtilMisc.toMap("examId", examId, "partyId", partyId));
			if (ServiceUtil.isError(result)) {
				return Response.status(HttpStatus.SC_BAD_REQUEST).entity(result).build();
			}
			return Response.status(HttpStatus.SC_OK).entity(result).build();

		} catch (Exception e) {
			Debug.logError(e, MODULE);
			return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(ServiceUtil.returnError(e.getMessage())).build();
		}
	}
}

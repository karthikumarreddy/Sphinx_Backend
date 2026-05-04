package com.sphinx.resources;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/examTopics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExamTopicResource {

	private static final String MODULE = ExamTopicResource.class.getName();
	private static final String UNEXPECTED_ERROR_MSG = "Unexpected Error Occured! Try Again After Sometime!";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllAssessmentTopics(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			HttpSession session = request.getSession(false);
			GenericValue userLogin = null;

			if (UtilValidate.isNotEmpty(session) && UtilValidate.isNotEmpty(session.getAttribute("userLogin"))) {
				userLogin = (GenericValue) session.getAttribute("userLogin");
			}

			String partyId = (String) request.getParameter("partyId");
			String examId = (String) request.getParameter("examId");

			if (UtilValidate.isEmpty(partyId)) {
				partyId = userLogin.getString("partyId");
			}

			Map<String, Object> result = dispatcher.runSync("getAllAssessmentTopics", UtilMisc.toMap("partyId", partyId, "examId", examId));
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(result).build();
			}
			return Response.ok().entity(result).build();

		} catch (Exception e) {
			Debug.log(MODULE);
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@POST
	@Path("mandatoryQuestions")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveMandatoryQuestionsForTopic(@Context HttpServletRequest request) {
		try {
			LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

			HttpSession session = request.getSession(false);
			GenericValue userLogin = null;

			if (UtilValidate.isNotEmpty(session) && UtilValidate.isNotEmpty(session.getAttribute("userLogin"))) {
				userLogin = (GenericValue) session.getAttribute("userLogin");
			}

			String examId = (String) request.getAttribute("examId");
			String topicId = (String) request.getAttribute("topicId");
			String questionIds = (String) request.getAttribute("questionIds");

			Map<String, Object> result = dispatcher.runSync("saveMandatoryQuestionsForTopic",
							UtilMisc.toMap("examId", examId, "topicId", topicId, "questionIds", questionIds));
			if (ServiceUtil.isError(result)) {
				return Response.status(400).entity(result).build();
			}
			return Response.ok().entity(result).build();

		} catch (Exception e) {
			Debug.log(MODULE);
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

}

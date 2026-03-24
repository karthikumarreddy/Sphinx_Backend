package com.sphinx.resources;

import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceContainer;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

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
            dispatcher = ServiceContainer.getLocalDispatcher("Sphinx",
                    getDelegator());
        }
        return dispatcher;
    }

    // ─────────────────────────────────────────────
    // User Services
    // ─────────────────────────────────────────────

    @POST
    @Path("/approve")
    public Response approveUser(Map<String, Object> input) {
        try {
            if (input.get("userName") == null)
                return Response.status(400)
                        .entity(Map.of("error", "The 'userName' field is required.")).build();

            LocalDispatcher dispatcher = getDispatcher();
            if (dispatcher == null)
                return Response.status(500)
                        .entity(Map.of("error", "Dispatcher not available.")).build();

            Map<String, Object> result = dispatcher.runSync("approveUser", input);
            return Response.ok(result).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    // ─────────────────────────────────────────────
    // Topic Services
    // ─────────────────────────────────────────────

    @POST
    @Path("/topic/create")
    public Response createTopic(Map<String, Object> input) {
        if (input.get("topicName") == null) {
            return Response.status(400).entity(Map.of("error", "topicName is required")).build();
        }

        try {
            // Remove topicId if sent as null to let OFBiz auto-sequence it
            input.remove("topicId");

            Map<String, Object> result = getDispatcher().runSync("createTopic", input);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    @POST
    @Path("/topic/update")
    public Response updateTopic(Map<String, Object> input) {
        if (input.get("topicId") == null) {
            return Response.status(400).entity(Map.of("error", "topicId is required")).build();
        }
        if (input.get("topicName") == null) {
            return Response.status(400).entity(Map.of("error", "topicName is required")).build();
        }

        try {
            Map<String, Object> result = getDispatcher().runSync("updateTopic", input);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    @POST
    @Path("/topic/delete")
    public Response deleteTopic(Map<String, Object> input) {
        if (input.get("topicId") == null) {
            return Response.status(400).entity(Map.of("error", "topicId is required")).build();
        }

        try {
            Map<String, Object> result = getDispatcher().runSync("deleteTopic", input);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    @POST
    @Path("/topic/getAll")
    public Response getAllTopics(Map<String, Object> input) {
        try {
            Map<String, Object> result = getDispatcher().runSync("getAllTopics", new HashMap<>());
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    @POST
    @Path("/topic/getById")
    public Response getTopicById(Map<String, Object> input) {
        if (input.get("topicId") == null) {
            return Response.status(400).entity(Map.of("error", "topicId is required")).build();
        }

        try {
            Map<String, Object> result = getDispatcher().runSync("getTopicById", input);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    // ─────────────────────────────────────────────
    // Exam Topic Services
    // ─────────────────────────────────────────────

    @POST
    @Path("/exam-topic/create")
    public Response createExamTopic(Map<String, Object> input) {
        if (input.get("examId") == null || input.get("topicId") == null) {
            return Response.status(400).entity(Map.of("error", "examId and topicId are required")).build();
        }
        if (input.get("topicName") == null) {
            return Response.status(400).entity(Map.of("error", "topicName is required")).build();
        }
        if (input.get("percentage") == null || input.get("startingQid") == null
                || input.get("endingQid") == null || input.get("questionsPerExam") == null
                || input.get("topicPassPercentage") == null) {
            return Response.status(400).entity(Map.of("error",
                    "percentage, startingQid, endingQid, questionsPerExam and topicPassPercentage are required")).build();
        }

        try {
            Map<String, Object> result = getDispatcher().runSync("createExamTopics", input);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    @POST
    @Path("/exam-topic/update")
    public Response updateExamTopic(Map<String, Object> input) {
        if (input.get("examId") == null || input.get("topicId") == null) {
            return Response.status(400).entity(Map.of("error", "examId and topicId are required")).build();
        }

        try {
            Map<String, Object> result = getDispatcher().runSync("updateExamTopics", input);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    @POST
    @Path("/exam-topic/delete")
    public Response deleteExamTopic(Map<String, Object> input) {
        if (input.get("examId") == null || input.get("topicId") == null) {
            return Response.status(400).entity(Map.of("error", "examId and topicId are required")).build();
        }

        try {
            Map<String, Object> result = getDispatcher().runSync("deleteExamTopics", input);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    @POST
    @Path("/exam-topic/getAll")
    public Response getAllExamTopics(Map<String, Object> input) {
        try {
            Map<String, Object> result = getDispatcher().runSync("getAllExamTopics", new HashMap<>());
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }

    @POST
    @Path("/exam-topic/getById")
    public Response getExamTopicById(Map<String, Object> input) {
        if (input.get("examId") == null || input.get("topicId") == null) {
            return Response.status(400).entity(Map.of("error", "examId and topicId are required")).build();
        }

        try {
            Map<String, Object> result = getDispatcher().runSync("getExamTopicById", input);
            return Response.ok(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500)
                    .entity(Map.of("error", e.getMessage(), "cause", e.getClass().getName()))
                    .build();
        }
    }
}
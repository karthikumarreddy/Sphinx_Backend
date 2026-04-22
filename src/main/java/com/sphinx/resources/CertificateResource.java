package com.sphinx.resources;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

@Path("/certificate")
public class CertificateResource {

    private static final String MODULE = CertificateResource.class.getName();

    @GET
    @Path("/download")
    @Produces("application/pdf")
    public Response downloadExamCertificate(@Context HttpServletRequest request) {

        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        String examId  = request.getParameter("examId");
        String partyId = request.getParameter("partyId");

        try {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("examId", examId);
            ctx.put("partyId", partyId);

            Map<String, Object> result =
                    dispatcher.runSync("downloadExamCertificate", ctx);

            if (ServiceUtil.isError(result)) {
                String errorMsg = ServiceUtil.getErrorMessage(result);
                Debug.logError(errorMsg, MODULE);

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorMsg)
                        .build();
            }

            byte[] pdfBytes = (byte[]) result.get("pdfBytes");
            String filename = (String) result.get("filename");

            return Response.ok(pdfBytes)
                    .type("application/pdf")
                    .header("Content-Disposition",
                            "attachment; filename=\"" + filename + "\"")
                    .header("Access-Control-Expose-Headers", "Content-Disposition")
                    .build();

        } catch (Exception e) {
            Debug.logError(e, MODULE);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error generating certificate: " + e.getMessage())
                    .build();
        }
    }
}
package com.sphinx.services;

import org.apache.ofbiz.base.util.*;
import org.apache.ofbiz.entity.*;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.service.*;

import java.util.*;
import java.io.*;

public class CertificateServices {

    private static final String MODULE = CertificateServices.class.getName();

    public static Map<String, Object> downloadExamCertificate(
            DispatchContext dctx, Map<String, Object> context) {

        Delegator delegator = dctx.getDelegator();

        String examId  = (String) context.get("examId");
        String partyId = (String) context.get("partyId");

        try {
            // 👉 VALIDATION
            if (UtilValidate.isEmpty(examId) || UtilValidate.isEmpty(partyId)) {
                return ServiceUtil.returnError("examId and partyId required");
            }

            // 👉 FETCH DATA
            GenericValue exam = EntityQuery.use(delegator)
                    .from("ExamMaster")
                    .where("examId", examId)
                    .queryOne();

            GenericValue person = EntityQuery.use(delegator)
                    .from("Person")
                    .where("partyId", partyId)
                    .queryOne();

            if (exam == null || person == null) {
                return ServiceUtil.returnError("Invalid exam or user");
            }

            // 👉 SIMPLE PDF CONTENT (you can plug your FOP logic here)
            String content = "Certificate\n\n" +
                    "Name: " + person.getString("firstName") + "\n" +
                    "Exam: " + exam.getString("examName");

            byte[] pdfBytes = content.getBytes(); // replace with FOP output

            String filename = "Certificate_" + examId + "_" + partyId + ".pdf";

            Map<String, Object> result = ServiceUtil.returnSuccess();
            result.put("pdfBytes", pdfBytes);
            result.put("filename", filename);

            return result;

        } catch (Exception e) {
            Debug.logError(e, MODULE);
            return ServiceUtil.returnError(e.getMessage());
        }
    }
}
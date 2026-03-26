package com.sphinx.services;

import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.ServiceUtil;

import com.sphinx.util.ApiResponse;

public class UserServices {
	private static final String MODULE = UserServices.class.getName();

	public static Map<String, ? extends Object> loginUser(DispatchContext dctx, Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		try {
			GenericValue user = delegator.findOne("UserLogin", false, Map.of("userLoginId", context.get("userName")));
			if (user == null)
				return ApiResponse.response(false, 400, "No account found with the provided credentials.", null);

			GenericValue party = delegator.findOne("Party", false,
					UtilMisc.toMap("partyId", user.getString("partyId")));

			List<GenericValue> partyRoles = delegator.findByAnd("PartyRole",
					UtilMisc.toMap("partyId", user.getString("partyId")), null, false);

			if (partyRoles == null || partyRoles.isEmpty())
				return ApiResponse.response(false, 400,
						"No role assigned to this account.\n Please contact the administrator.", null);

			GenericValue partyRole = partyRoles.get(0);

			if (!partyRole.get("roleTypeId").equals("SphinxUser") && !partyRole.get("roleTypeId").equals("SphinxAdmin"))
				return ApiResponse.response(false, 400, "Access denied. Your account does not have a recognized role.",
						null);

			if (!user.get("enabled").equals("Y"))
				return ApiResponse.response(false, 400,
						"Your account has been suspended.\n Please contact the administrator for further assistance.",
						null);

			if (!party.get("statusId").equals("PARTY_ENABLED"))
				return ApiResponse.response(false, 400,
						"Your account is pending administrator approval. You will be notified once access is granted.",
						null);

			if (user.get("currentPassword").equals(context.get("password"))) {
				return ApiResponse.response(true, 200, "Login successful. Welcome back!", null);
			}

		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ApiResponse.response(false, 500,
					"An error occurred while processing your login request. Please try again later.", null);
		}

		return ApiResponse.response(false, 500,
				"Invalid username or password. Please check your credentials and try again.", null);
	}

	public static Map<String, ? extends Object> signupUser(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		System.out.println("SERVICE CONTEXT: " + context);
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			String userName = (String) context.get("userName");
			String firstName = (String) context.get("firstName");
			String lastName = (String) context.get("lastName");
			String mobileNo = (String) context.get("mobileNo");
			String email = (String) context.get("email");
			String password = (String) context.get("password");
			String role = (String) context.get("role");

			if (userName == null || firstName == null || lastName == null || mobileNo == null || email == null
					|| password == null || role == null)
				return ApiResponse.response(false, 400,
						"All fields are required.\n Please ensure no fields are left empty.", null);

			Delegator delegator = dctx.getDelegator();

			// Insert into Party table
			String partyId = "SPX_" + delegator.getNextSeqId("Party");
			GenericValue party = delegator.makeValue("Party");
			party.set("partyId", partyId);
			party.set("partyTypeId", "PERSON");
			party.set("statusId", "PARTY_DISABLED"); // Pending admin approval

			delegator.create(party);

			// Insert into Person table
			GenericValue person = delegator.makeValue("Person");
			person.set("partyId", partyId);
			person.set("firstName", firstName);
			person.set("lastName", lastName);

			delegator.create(person);

			// Insert into UserLogin table
			GenericValue userLogin = delegator.makeValue("UserLogin");
			userLogin.set("userLoginId", userName);
			userLogin.set("partyId", partyId);
			userLogin.set("currentPassword", password);
			userLogin.set("enabled", "N"); // Account disabled until admin enables it

			delegator.create(userLogin);

			// Insert into PartyRole table
			GenericValue partyRole = delegator.makeValue("PartyRole");
			partyRole.set("partyId", partyId);
			if (role.equals("user")) {
				partyRole.set("roleTypeId", "SphinxUser");
			} else if (role.equals("admin")) {
				partyRole.set("roleTypeId", "SphinxAdmin");
			}
			delegator.create(partyRole);

			// Insert email contact mechanism
			String emailContactMechId = delegator.getNextSeqId("ContactMech");
			GenericValue emailContactMech = delegator.makeValue("ContactMech");
			emailContactMech.set("contactMechId", emailContactMechId);
			emailContactMech.set("contactMechTypeId", "EMAIL_ADDRESS");
			emailContactMech.set("infoString", email);
			delegator.create(emailContactMech);

			GenericValue emailPartyContactMech = delegator.makeValue("PartyContactMech");
			emailPartyContactMech.set("partyId", partyId);
			emailPartyContactMech.set("contactMechId", emailContactMechId);
			emailPartyContactMech.set("fromDate", UtilDateTime.nowTimestamp());
			delegator.create(emailPartyContactMech);

			// Insert phone contact mechanism
			String phoneContactMechId = delegator.getNextSeqId("ContactMech");
			GenericValue phoneContactMech = delegator.makeValue("ContactMech");
			phoneContactMech.set("contactMechId", phoneContactMechId);
			phoneContactMech.set("contactMechTypeId", "TELECOM_NUMBER");
			delegator.create(phoneContactMech);

			GenericValue telecomNumber = delegator.makeValue("TelecomNumber");
			telecomNumber.set("contactMechId", phoneContactMechId);
			telecomNumber.set("contactNumber", mobileNo);
			delegator.create(telecomNumber);

			GenericValue phonePartyContactMech = delegator.makeValue("PartyContactMech");
			phonePartyContactMech.set("partyId", partyId);
			phonePartyContactMech.set("contactMechId", phoneContactMechId);
			phonePartyContactMech.set("fromDate", UtilDateTime.nowTimestamp());
			delegator.create(phonePartyContactMech);

			return ApiResponse.response(true, 200,
					"Registration successful. Your account is pending administrator approval. You will receive a confirmation email once your account is activated.",
					null);

		} catch (Exception e) {
			return ApiResponse.response(false,500,"An unexpected error occurred during registration. Please try again later.", null);
		}
	}
}
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

public class UserServices {

	public static Map<String, ? extends Object> loginUser(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		try {
			GenericValue user = delegator.findOne("UserLogin", false, Map.of("userLoginId", context.get("userName")));
			if (user == null)
				return ServiceUtil.returnError("No account found with the provided credentials.");

			GenericValue party = delegator.findOne("Party", false,
					UtilMisc.toMap("partyId", user.getString("partyId")));

			List<GenericValue> partyRoles = delegator.findByAnd("PartyRole",
					UtilMisc.toMap("partyId", user.getString("partyId")), null, false);

			if (partyRoles == null || partyRoles.isEmpty())
				return ServiceUtil.returnError("No role assigned to this account./n Please contact the administrator.");

			GenericValue partyRole = partyRoles.get(0);

			if (!partyRole.get("roleTypeId").equals("SphinxUser") && !partyRole.get("roleTypeId").equals("SphinxAdmin"))
				return ServiceUtil.returnError("Access denied. Your account does not have a recognized role.");

			if (!user.get("enabled").equals("Y"))
				return ServiceUtil.returnError(
						"Your account has been suspended./n Please contact the administrator for further assistance.");

			if (!party.get("statusId").equals("PARTY_ENABLED"))
				return ServiceUtil.returnError(
						"Your account is pending administrator approval./n You will be notified once access is granted.");

			if (user.get("currentPassword").equals(context.get("password"))) {
				result.put("responseMessage", "Login successful. Welcome back!");
				return result;
			}

		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil
					.returnError("An error occurred while processing your login request./n Please try again later.");
		}

		return ServiceUtil.returnError("Invalid username or password./n Please check your credentials and try again.");
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
			String currentPassword = (String) context.get("currentPassword");
			String role = (String) context.get("role");

			if (userName == null || firstName == null || lastName == null || mobileNo == null || email == null
					|| password == null || currentPassword == null || !currentPassword.equals(password) || role == null)
				return ServiceUtil.returnError("All fields are required. Please ensure no fields are left empty.");

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
			userLogin.set("currentPassword", currentPassword);
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

			result.put("responseMessage",
					"Registration successful./n Your account is pending administrator approval./n You will receive a confirmation email once your account is activated.");
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError("An unexpected error occurred during registration./n Please try again later.");
		}
	}
}
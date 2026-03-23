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
				return ServiceUtil.returnError("Unable to find the user");

			GenericValue party = delegator.findOne("Party", false,
					UtilMisc.toMap("partyId", user.getString("partyId")));

			List<GenericValue> partyRoles = delegator.findByAnd("PartyRole",
					UtilMisc.toMap("partyId", user.getString("partyId")), null, false);
			if (partyRoles == null || partyRoles.isEmpty())
				return ServiceUtil.returnError("Unable to find the user");
			GenericValue partyRole = partyRoles.get(0);

			if (!partyRole.get("roleTypeId").equals("SphinxUser") && !partyRole.get("roleTypeId").equals("SphinxAdmin"))
				return ServiceUtil.returnError("Unable to find th role");

			// check admin approval
			if (!user.get("enabled").equals("Y")) {
				return ServiceUtil.returnError("user is blocked try later");
			}
			if (!party.get("statusId").equals("PARTY_ENABLED"))
				return ServiceUtil.returnError("admin approval is pending");

			if (user.get("currentPassword").equals(context.get("password"))) {
				result.put("responseMessage", "login successfully");
				return result;
			}

		} catch (GenericEntityException e) {
			e.printStackTrace();
			return ServiceUtil.returnError("login failed");
		}
		return ServiceUtil.returnError("login failed");
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
			String confirmPassword = (String) context.get("confirmPassword");
			String role = (String) context.get("role");

			if (userName == null || firstName == null || lastName == null || mobileNo == null || email == null
					|| password == null || confirmPassword == null || !confirmPassword.equals(password) || role == null)
				return ServiceUtil.returnError("required fields are null enter again");

			Delegator delegator = dctx.getDelegator();
			// inserting values into party table
			String partyId = "SPX_" + delegator.getNextSeqId("Party");
			GenericValue party = delegator.makeValue("Party");
			party.set("partyId", partyId);
			party.set("partyTypeId", "PERSON");
			party.set("statusId", "PARTY_DISABLED");// admin approval status

			delegator.create(party);

			// inserting value into person
			GenericValue person = delegator.makeValue("Person");
			person.set("partyId", partyId);
			person.set("firstName", firstName);
			person.set("lastName", lastName);

			delegator.create(person);

			GenericValue userLogin = delegator.makeValue("UserLogin");
			userLogin.set("userLoginId", userName);
			userLogin.set("partyId", partyId);
			userLogin.set("currentPassword", confirmPassword);
			userLogin.set("enabled", "N");// if user logedin Y if not N

			delegator.create(userLogin);

			GenericValue partyRole = delegator.makeValue("PartyRole");
			partyRole.set("partyId", partyId);
			// role checking
			if (role.equals("user")) {
				partyRole.set("roleTypeId", "SphinxUser");
			}
			if (role.equals("admin")) {
				partyRole.set("roleTypeId", "SphinxAdmin");
			}
			delegator.create(partyRole);

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
					"signup sucessfully wait for admin approval to login check mail for approval ");
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return ServiceUtil.returnError("something went wrong try later" + e.getMessage());

		}

	}

}
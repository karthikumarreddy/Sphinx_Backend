package com.sphinx.services;

import java.util.Map;

import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.DispatchContext;

public class QuestionService {

	// QUESTION SERVICE

	public Map<String, ? extends Object> addQuestions(DispatchContext dctx, Map<String, ? extends Object> context) {

		try {
			Delegator delegator = dctx.getDelegator();

			GenericValue questionRecord = delegator.makeValue("QuestionMaster");

			questionRecord.setNextSeqId();

			questionRecord.setNonPKFields(context);

			questionRecord.create();
		} catch (GenericEntityException e) {

		}

		return null;
	}
}

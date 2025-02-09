/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.pricing.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is used by SOAP remote services, specifically {@link com.liferay.commerce.pricing.service.http.CommercePricingClassCPDefinitionRelServiceSoap}.
 *
 * @author Riccardo Alberti
 * @deprecated As of Athanasius (7.3.x), with no direct replacement
 * @generated
 */
@Deprecated
public class CommercePricingClassCPDefinitionRelSoap implements Serializable {

	public static CommercePricingClassCPDefinitionRelSoap toSoapModel(
		CommercePricingClassCPDefinitionRel model) {

		CommercePricingClassCPDefinitionRelSoap soapModel =
			new CommercePricingClassCPDefinitionRelSoap();

		soapModel.setMvccVersion(model.getMvccVersion());
		soapModel.setCommercePricingClassCPDefinitionRelId(
			model.getCommercePricingClassCPDefinitionRelId());
		soapModel.setCompanyId(model.getCompanyId());
		soapModel.setUserId(model.getUserId());
		soapModel.setUserName(model.getUserName());
		soapModel.setCreateDate(model.getCreateDate());
		soapModel.setModifiedDate(model.getModifiedDate());
		soapModel.setCommercePricingClassId(model.getCommercePricingClassId());
		soapModel.setCPDefinitionId(model.getCPDefinitionId());

		return soapModel;
	}

	public static CommercePricingClassCPDefinitionRelSoap[] toSoapModels(
		CommercePricingClassCPDefinitionRel[] models) {

		CommercePricingClassCPDefinitionRelSoap[] soapModels =
			new CommercePricingClassCPDefinitionRelSoap[models.length];

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModel(models[i]);
		}

		return soapModels;
	}

	public static CommercePricingClassCPDefinitionRelSoap[][] toSoapModels(
		CommercePricingClassCPDefinitionRel[][] models) {

		CommercePricingClassCPDefinitionRelSoap[][] soapModels = null;

		if (models.length > 0) {
			soapModels = new CommercePricingClassCPDefinitionRelSoap
				[models.length][models[0].length];
		}
		else {
			soapModels = new CommercePricingClassCPDefinitionRelSoap[0][0];
		}

		for (int i = 0; i < models.length; i++) {
			soapModels[i] = toSoapModels(models[i]);
		}

		return soapModels;
	}

	public static CommercePricingClassCPDefinitionRelSoap[] toSoapModels(
		List<CommercePricingClassCPDefinitionRel> models) {

		List<CommercePricingClassCPDefinitionRelSoap> soapModels =
			new ArrayList<CommercePricingClassCPDefinitionRelSoap>(
				models.size());

		for (CommercePricingClassCPDefinitionRel model : models) {
			soapModels.add(toSoapModel(model));
		}

		return soapModels.toArray(
			new CommercePricingClassCPDefinitionRelSoap[soapModels.size()]);
	}

	public CommercePricingClassCPDefinitionRelSoap() {
	}

	public long getPrimaryKey() {
		return _CommercePricingClassCPDefinitionRelId;
	}

	public void setPrimaryKey(long pk) {
		setCommercePricingClassCPDefinitionRelId(pk);
	}

	public long getMvccVersion() {
		return _mvccVersion;
	}

	public void setMvccVersion(long mvccVersion) {
		_mvccVersion = mvccVersion;
	}

	public long getCommercePricingClassCPDefinitionRelId() {
		return _CommercePricingClassCPDefinitionRelId;
	}

	public void setCommercePricingClassCPDefinitionRelId(
		long CommercePricingClassCPDefinitionRelId) {

		_CommercePricingClassCPDefinitionRelId =
			CommercePricingClassCPDefinitionRelId;
	}

	public long getCompanyId() {
		return _companyId;
	}

	public void setCompanyId(long companyId) {
		_companyId = companyId;
	}

	public long getUserId() {
		return _userId;
	}

	public void setUserId(long userId) {
		_userId = userId;
	}

	public String getUserName() {
		return _userName;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}

	public Date getCreateDate() {
		return _createDate;
	}

	public void setCreateDate(Date createDate) {
		_createDate = createDate;
	}

	public Date getModifiedDate() {
		return _modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		_modifiedDate = modifiedDate;
	}

	public long getCommercePricingClassId() {
		return _commercePricingClassId;
	}

	public void setCommercePricingClassId(long commercePricingClassId) {
		_commercePricingClassId = commercePricingClassId;
	}

	public long getCPDefinitionId() {
		return _CPDefinitionId;
	}

	public void setCPDefinitionId(long CPDefinitionId) {
		_CPDefinitionId = CPDefinitionId;
	}

	private long _mvccVersion;
	private long _CommercePricingClassCPDefinitionRelId;
	private long _companyId;
	private long _userId;
	private String _userName;
	private Date _createDate;
	private Date _modifiedDate;
	private long _commercePricingClassId;
	private long _CPDefinitionId;

}
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

package com.liferay.dynamic.data.mapping.form.web.internal.display.context;

import com.liferay.dynamic.data.mapping.form.builder.context.DDMFormBuilderContextFactory;
import com.liferay.dynamic.data.mapping.form.builder.internal.context.DDMFormContextToDDMFormValues;
import com.liferay.dynamic.data.mapping.form.builder.settings.DDMFormBuilderSettingsRetriever;
import com.liferay.dynamic.data.mapping.form.field.type.DDMFormFieldTypeServicesTracker;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormRenderer;
import com.liferay.dynamic.data.mapping.form.renderer.DDMFormTemplateContextFactory;
import com.liferay.dynamic.data.mapping.form.values.factory.DDMFormValuesFactory;
import com.liferay.dynamic.data.mapping.form.web.internal.configuration.DDMFormWebConfiguration;
import com.liferay.dynamic.data.mapping.form.web.internal.configuration.activator.FFSubmissionsSettingsConfigurationActivator;
import com.liferay.dynamic.data.mapping.form.web.internal.instance.lifecycle.AddDefaultSharedFormLayoutPortalInstanceLifecycleListener;
import com.liferay.dynamic.data.mapping.io.DDMFormFieldTypesSerializer;
import com.liferay.dynamic.data.mapping.io.exporter.DDMFormInstanceRecordWriterTracker;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceSettings;
import com.liferay.dynamic.data.mapping.model.UnlocalizedValue;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceVersionLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureService;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.storage.DDMStorageAdapterTracker;
import com.liferay.dynamic.data.mapping.test.util.DDMFormTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMFormValuesTestUtil;
import com.liferay.dynamic.data.mapping.util.DDMFormValuesMerger;
import com.liferay.frontend.js.loader.modules.extender.npm.NPMResolver;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.resource.bundle.ResourceBundleLoader;
import com.liferay.portal.kernel.resource.bundle.ResourceBundleLoaderUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.LocaleThreadLocal;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.util.PropsImpl;

import java.util.Locale;
import java.util.TimeZone;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Adam Brandizzi
 */
@PrepareForTest(
	{LocaleUtil.class, ResourceBundleUtil.class, ResourceBundleLoaderUtil.class}
)
@RunWith(PowerMockRunner.class)
public class DDMFormAdminDisplayContextTest extends PowerMockito {

	@BeforeClass
	public static void setUpClass() throws Exception {
		PropsUtil.setProps(new PropsImpl());
	}

	@Before
	public void setUp() throws Exception {
		setUpPortalUtil();

		setUpLanguageUtil();
		setUpResourceBundleUtil();
		setUpResourceBundleLoaderUtil();

		setUpDDMFormDisplayContext();
	}

	@Test
	public void testFormExpired() throws Exception {
		DDMFormInstanceSettings ddmFormInstanceSettings = mock(
			DDMFormInstanceSettings.class);

		when(
			ddmFormInstanceSettings.expirationDate()
		).thenReturn(
			"1987-09-22"
		);

		when(
			ddmFormInstanceSettings.neverExpire()
		).thenReturn(
			false
		);

		Assert.assertTrue(
			_ddmFormAdminDisplayContext.isFormExpired(
				_mockDDMFormInstance(ddmFormInstanceSettings)));
	}

	@Test
	public void testFormNotExpiredWithInexistentForm() throws Exception {
		Assert.assertFalse(_ddmFormAdminDisplayContext.isFormExpired(null));
	}

	@Test
	public void testFormNotExpiredWithNeverExpireSetting() throws Exception {
		DDMFormInstanceSettings ddmFormInstanceSettings = mock(
			DDMFormInstanceSettings.class);

		when(
			ddmFormInstanceSettings.neverExpire()
		).thenReturn(
			true
		);

		Assert.assertFalse(
			_ddmFormAdminDisplayContext.isFormExpired(
				_mockDDMFormInstance(ddmFormInstanceSettings)));
	}

	@Test
	public void testGetDDMFormRenderingContextDDMFormValues() throws Exception {
		LocaleThreadLocal.setSiteDefaultLocale(LocaleUtil.US);

		setRenderRequestParamenter(
			"serializedSettingsContext",
			_read("ddm-form-settings-values.json"));

		DDMFormValues ddmFormValues = DDMFormValuesTestUtil.createDDMFormValues(
			DDMFormTestUtil.createDDMForm("workflowDefinition"));

		ddmFormValues.addDDMFormFieldValue(
			DDMFormValuesTestUtil.createDDMFormFieldValue(
				"eBvF8zup", "workflowDefinition",
				new UnlocalizedValue("[\"Single Approver\"]")));

		Assert.assertEquals(
			ddmFormValues,
			_ddmFormAdminDisplayContext.
				getDDMFormRenderingContextDDMFormValues());
	}

	@Test
	public void testGetPublishedFormURL() throws Exception {
		Assert.assertEquals(
			getSharedFormURL() + _SHARED_FORM_INSTANCE_ID,
			_ddmFormAdminDisplayContext.getPublishedFormURL(
				mockDDMFormInstance(_SHARED_FORM_INSTANCE_ID, true, true)));
		Assert.assertEquals(
			getSharedFormURL() + _SHARED_FORM_INSTANCE_ID,
			_ddmFormAdminDisplayContext.getPublishedFormURL(
				mockDDMFormInstance(_SHARED_FORM_INSTANCE_ID, true, false)));
		Assert.assertEquals(
			StringPool.BLANK,
			_ddmFormAdminDisplayContext.getPublishedFormURL(
				mockDDMFormInstance(_SHARED_FORM_INSTANCE_ID, false, false)));
	}

	@Test
	public void testGetSharedFormURL() throws Exception {
		Assert.assertEquals(
			getSharedFormURL(), _ddmFormAdminDisplayContext.getSharedFormURL());
	}

	@Test
	public void testGetSharedFormURLForSharedFormInstance() throws Exception {
		setRenderRequestParamenter(
			"formInstanceId", String.valueOf(_SHARED_FORM_INSTANCE_ID));

		Assert.assertEquals(
			getSharedFormURL(), _ddmFormAdminDisplayContext.getSharedFormURL());
	}

	@Test
	public void testIsShowPartialResultsToRespondents() throws Exception {
		Assert.assertFalse(
			_ddmFormAdminDisplayContext.isShowPartialResultsToRespondents(
				null));

		DDMFormInstanceSettings ddmFormInstanceSettings = mock(
			DDMFormInstanceSettings.class);

		DDMFormInstance ddmFormInstance = _mockDDMFormInstance(
			ddmFormInstanceSettings);

		Assert.assertFalse(
			_ddmFormAdminDisplayContext.isShowPartialResultsToRespondents(
				ddmFormInstance));

		when(
			ddmFormInstanceSettings.showPartialResultsToRespondents()
		).thenReturn(
			true
		);

		Assert.assertTrue(
			_ddmFormAdminDisplayContext.isShowPartialResultsToRespondents(
				ddmFormInstance));
	}

	protected String getSharedFormURL() {
		return StringBundler.concat(
			_PORTAL_URL, _PUBLIC_FRIENDLY_URL_PATH, _GROUP_FRIENDLY_URL_PATH,
			_PUBLIC_PAGE_FRIENDLY_URL_PATH, _FORM_APPLICATION_PATH);
	}

	protected DDMFormInstance mockDDMFormInstance(
			long formInstanceId, boolean requireAuthentication)
		throws PortalException {

		DDMFormInstance formInstance = mock(DDMFormInstance.class);

		when(
			formInstance.getFormInstanceId()
		).thenReturn(
			formInstanceId
		);

		DDMFormInstanceSettings settings = mockDDMFormInstanceSettings(
			requireAuthentication);

		when(
			formInstance.getSettingsModel()
		).thenReturn(
			settings
		);

		return formInstance;
	}

	protected DDMFormInstance mockDDMFormInstance(
			long formInstanceId, boolean published,
			boolean requireAuthentication)
		throws PortalException {

		DDMFormInstance ddmFormInstance = mockDDMFormInstance(
			formInstanceId, requireAuthentication);

		DDMFormInstanceSettings ddmFormInstanceSettings =
			ddmFormInstance.getSettingsModel();

		when(
			ddmFormInstanceSettings.published()
		).thenReturn(
			published
		);

		return ddmFormInstance;
	}

	protected DDMFormInstanceService mockDDMFormInstanceService()
		throws PortalException {

		DDMFormInstanceService ddmFormInstanceService = mock(
			DDMFormInstanceService.class);

		DDMFormInstance sharedDDMFormInstance = mockDDMFormInstance(
			_SHARED_FORM_INSTANCE_ID, false);

		when(
			ddmFormInstanceService.fetchFormInstance(
				Matchers.eq(_SHARED_FORM_INSTANCE_ID))
		).thenReturn(
			sharedDDMFormInstance
		);

		return ddmFormInstanceService;
	}

	protected DDMFormInstanceSettings mockDDMFormInstanceSettings(
		boolean requireAuthentication) {

		DDMFormInstanceSettings settings = mock(DDMFormInstanceSettings.class);

		when(
			settings.requireAuthentication()
		).thenReturn(
			requireAuthentication
		);

		return settings;
	}

	protected HttpServletRequest mockHttpServletRequest() {
		ThemeDisplay themeDisplay = mockThemeDisplay();

		_httpServletRequest = mock(HttpServletRequest.class);

		when(
			_httpServletRequest.getAttribute(WebKeys.THEME_DISPLAY)
		).thenReturn(
			themeDisplay
		);

		return _httpServletRequest;
	}

	protected ThemeDisplay mockThemeDisplay() {
		ThemeDisplay themeDisplay = mock(ThemeDisplay.class);

		when(
			themeDisplay.getPathFriendlyURLPublic()
		).thenReturn(
			_PUBLIC_FRIENDLY_URL_PATH
		);

		when(
			themeDisplay.getPortalURL()
		).thenReturn(
			_PORTAL_URL
		);

		when(
			themeDisplay.getTimeZone()
		).thenReturn(
			TimeZone.getDefault()
		);

		return themeDisplay;
	}

	protected void setRenderRequestParamenter(String parameter, String value) {
		when(
			_renderRequest.getParameter(Matchers.eq(parameter))
		).thenReturn(
			value
		);
	}

	protected void setUpDDMFormDisplayContext() throws Exception {
		_renderRequest = _mockRenderRequest();

		_ddmFormAdminDisplayContext = new DDMFormAdminDisplayContext(
			_renderRequest, mock(RenderResponse.class),
			new AddDefaultSharedFormLayoutPortalInstanceLifecycleListener(),
			mock(DDMFormBuilderContextFactory.class),
			mock(DDMFormBuilderSettingsRetriever.class),
			_getDDMFormContextToDDMFormValues(),
			mock(DDMFormFieldTypeServicesTracker.class),
			mock(DDMFormFieldTypesSerializer.class),
			mock(DDMFormInstanceLocalService.class),
			mock(DDMFormInstanceRecordLocalService.class),
			mock(DDMFormInstanceRecordWriterTracker.class),
			mockDDMFormInstanceService(),
			mock(DDMFormInstanceVersionLocalService.class),
			mock(DDMFormRenderer.class),
			mock(DDMFormTemplateContextFactory.class),
			mock(DDMFormValuesFactory.class), mock(DDMFormValuesMerger.class),
			mock(DDMFormWebConfiguration.class),
			mock(DDMStorageAdapterTracker.class),
			mock(DDMStructureLocalService.class),
			mock(DDMStructureService.class),
			mock(FFSubmissionsSettingsConfigurationActivator.class),
			mock(JSONFactory.class), mock(NPMResolver.class), null,
			mock(Portal.class));
	}

	protected void setUpLanguageUtil() {
		LanguageUtil languageUtil = new LanguageUtil();

		languageUtil.setLanguage(mock(Language.class));
	}

	protected void setUpPortalUtil() {
		PortalUtil portalUtil = new PortalUtil();

		Portal portal = mock(Portal.class);

		HttpServletRequest httpServletRequest = mockHttpServletRequest();

		when(
			portal.getHttpServletRequest(Matchers.any(PortletRequest.class))
		).thenReturn(
			httpServletRequest
		);

		portalUtil.setPortal(portal);
	}

	protected void setUpResourceBundleLoaderUtil() {
		ResourceBundleLoader resourceBundleLoader = mock(
			ResourceBundleLoader.class);

		ResourceBundleLoaderUtil.setPortalResourceBundleLoader(
			resourceBundleLoader);

		when(
			resourceBundleLoader.loadResourceBundle(Matchers.any(Locale.class))
		).thenReturn(
			ResourceBundleUtil.EMPTY_RESOURCE_BUNDLE
		);
	}

	protected void setUpResourceBundleUtil() {
		mockStatic(ResourceBundleUtil.class);

		when(
			ResourceBundleUtil.getBundle(
				Matchers.anyString(), Matchers.any(Locale.class),
				Matchers.any(ClassLoader.class))
		).thenReturn(
			ResourceBundleUtil.EMPTY_RESOURCE_BUNDLE
		);
	}

	private DDMFormContextToDDMFormValues _getDDMFormContextToDDMFormValues()
		throws Exception {

		DDMFormContextToDDMFormValues ddmFormContextToDDMFormValues =
			new DDMFormContextToDDMFormValues();

		field(
			DDMFormContextToDDMFormValues.class, "jsonFactory"
		).set(
			ddmFormContextToDDMFormValues, new JSONFactoryImpl()
		);

		return ddmFormContextToDDMFormValues;
	}

	private DDMFormInstance _mockDDMFormInstance(
			DDMFormInstanceSettings ddmFormInstanceSettings)
		throws Exception {

		DDMFormInstance ddmFormInstance = mock(DDMFormInstance.class);

		when(
			ddmFormInstance.getSettingsModel()
		).thenReturn(
			ddmFormInstanceSettings
		);

		return ddmFormInstance;
	}

	private RenderRequest _mockRenderRequest() {
		RenderRequest renderRequest = mock(RenderRequest.class);

		ThemeDisplay themeDisplay = mockThemeDisplay();

		when(
			renderRequest.getAttribute(Matchers.eq(WebKeys.THEME_DISPLAY))
		).thenReturn(
			themeDisplay
		);

		return renderRequest;
	}

	private String _read(String fileName) throws Exception {
		Class<?> clazz = getClass();

		return StringUtil.read(
			clazz.getResourceAsStream("dependencies/" + fileName));
	}

	private static final String _FORM_APPLICATION_PATH =
		Portal.FRIENDLY_URL_SEPARATOR + "form/";

	private static final String _GROUP_FRIENDLY_URL_PATH = "/forms";

	private static final String _PORTAL_URL = "http://localhost:9999";

	private static final String _PUBLIC_FRIENDLY_URL_PATH = "/web";

	private static final String _PUBLIC_PAGE_FRIENDLY_URL_PATH = "/shared";

	private static final long _SHARED_FORM_INSTANCE_ID =
		RandomTestUtil.randomLong();

	private DDMFormAdminDisplayContext _ddmFormAdminDisplayContext;
	private HttpServletRequest _httpServletRequest;
	private RenderRequest _renderRequest;

}
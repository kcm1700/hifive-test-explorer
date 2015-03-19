/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.response;

import java.io.Serializable;

import com.htmlhifive.testexplorer.model.Capability;
import com.htmlhifive.testexplorer.model.ExecutionMode;
import com.htmlhifive.testexplorer.model.TestCaseResult;
import com.htmlhifive.testexplorer.model.Screenshot;

/**
 * TestResult
 *
 */
public class TestResultDetail implements Serializable {
	/** serialVersionUID */
	private static final long serialVersionUID = 3123785836289311875L;

	private String id;
	private String executionTime;
	private String testClass;
	private String testMethod;
	private String screenshotId;
	private String platform;
	private String platformVersion;
	private String deviceName;
	private String browserName;
	private String browserVersion;
	private String label;
	private Boolean comparisonResult;
	private ExecutionMode mode;

	public TestResultDetail() {
	}

	public TestResultDetail(Screenshot screenshot) {
		TestCaseResult testCaseResult = screenshot.getTestCaseResult();
		mode = testCaseResult.getExpectedId() == null ? ExecutionMode.EXPECTED : ExecutionMode.ACTUAL;
		executionTime = testCaseResult.getExecuteTime();

		screenshotId = screenshot.getScreenshotId();
		comparisonResult = screenshot.getResult();

		Capability capability =  screenshot.getCapability();
		testClass = capability.getTestClass();
		testMethod = capability.getTestMethod();
		platform = capability.getPlatform();
		platformVersion = capability.getPlatformVersion();
		deviceName = capability.getDeviceName();
		browserName = capability.getBrowserName();
		browserVersion = capability.getBrowserVersion();
		label = capability.getLabel();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(String executionTime) {
		this.executionTime = executionTime;
	}

	public String getTestClass() {
		return testClass;
	}

	public void setTestClass(String testClass) {
		this.testClass = testClass;
	}

	public String getTestMethod() {
		return testMethod;
	}

	public void setTestMethod(String testMethod) {
		this.testMethod = testMethod;
	}

	public String getScreenshotId() {
		return screenshotId;
	}

	public void setScreenshotId(String screenshotId) {
		this.screenshotId = screenshotId;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatformVersion() {
		return platformVersion;
	}

	public void setPlatformVersion(String platformVersion) {
		this.platformVersion = platformVersion;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getBrowserName() {
		return browserName;
	}

	public void setBrowserName(String browserName) {
		this.browserName = browserName;
	}

	public String getBrowserVersion() {
		return browserVersion;
	}

	public void setBrowserVersion(String browserVersion) {
		this.browserVersion = browserVersion;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Boolean getComparisonResult() {
		return comparisonResult;
	}

	public void setComparisonResult(Boolean comparisonResult) {
		this.comparisonResult = comparisonResult;
	}

	public ExecutionMode getMode() {
		return mode;
	}

	public void setMode(ExecutionMode mode) {
		this.mode = mode;
	}

}
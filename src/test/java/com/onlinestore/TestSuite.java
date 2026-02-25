package com.onlinestore;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Online Store Test Suite")
@SelectPackages({
        "com.onlinestore.domain",
        "com.onlinestore.application",
        "com.onlinestore.infrastructure",
        "com.onlinestore.web"
})
public class TestSuite {
    // This will run all tests in the specified packages
}
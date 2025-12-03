package com.taskmanager.application.service

import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses(
    AuthServiceTest::class,
    UserServiceTest::class,
    EmployeeServiceTest::class,
    ProjectServiceTest::class,
    TeamServiceTest::class,
    TaskServiceTest::class,
    DocumentServiceTest::class,
    FinancialServiceTest::class
)
class AllServicesTestSuite
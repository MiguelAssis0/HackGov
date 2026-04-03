package com.fiap.hackgov;


import com.fiap.hackgov.controllers.AuthControllerTest;
import com.fiap.hackgov.controllers.EmployeeControllerTest;
import com.fiap.hackgov.services.AuthServiceTest;
import com.fiap.hackgov.services.TokenServiceTest;
import com.fiap.hackgov.services.TwoFactorAuthServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
		AuthControllerTest.class,
		EmployeeControllerTest.class,
		AuthServiceTest.class,
		TokenServiceTest.class,
		TwoFactorAuthServiceTest.class
})
class HackgovTestSuite {

}
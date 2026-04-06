package com.fiap.hackgov;


import com.fiap.hackgov.controllers.AuthControllerTest;
import com.fiap.hackgov.controllers.CityHallControllerTest;
import com.fiap.hackgov.controllers.EmployeeControllerTest;
import com.fiap.hackgov.controllers.StateControllerTest;
import com.fiap.hackgov.infra.config.JwtAuthenticationFilterTest;
import com.fiap.hackgov.infra.config.RateLimitFilterTest;
import com.fiap.hackgov.infra.config.SecurityConfigTest;
import com.fiap.hackgov.services.*;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
		AuthControllerTest.class,
		CityHallControllerTest.class,
		EmployeeControllerTest.class,
		AuthServiceTest.class,
		CityHallServiceTest.class,
		StateControllerTest.class,
		StateServiceTest.class,
		TokenServiceTest.class,
		TwoFactorAuthServiceTest.class,
		SecurityConfigTest.class,
		RateLimitFilterTest.class,
		LoginAttemptServiceTest.class,
		JwtAuthenticationFilterTest.class
})
class HackgovTestSuite {

}
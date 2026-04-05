package com.fiap.hackgov;


import com.fiap.hackgov.controllers.AuthControllerTest;
import com.fiap.hackgov.controllers.EmployeeControllerTest;
import com.fiap.hackgov.infra.config.JwtAuthenticationFilterTest;
import com.fiap.hackgov.infra.config.RateLimitFilterTest;
import com.fiap.hackgov.infra.config.SecurityConfigTest;
import com.fiap.hackgov.services.AuthServiceTest;
import com.fiap.hackgov.services.TokenServiceTest;
import com.fiap.hackgov.services.TwoFactorAuthServiceTest;
import com.fiap.hackgov.services.LoginAttemptServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
		AuthControllerTest.class,
		EmployeeControllerTest.class,
		AuthServiceTest.class,
		TokenServiceTest.class,
		TwoFactorAuthServiceTest.class,
		SecurityConfigTest.class,
		RateLimitFilterTest.class,
		LoginAttemptServiceTest.class,
		JwtAuthenticationFilterTest.class
})
class HackgovTestSuite {

}
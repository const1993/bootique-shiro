package io.bootique.shiro.basic;

import io.bootique.BQRuntime;
import io.bootique.shiro.ShiroModule;
import io.bootique.shiro.SubjectManager;
import io.bootique.test.junit.BQTestFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShiroBasicModuleIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    protected Realm mockRealm() {
        Realm mockRealm = mock(Realm.class);
        when(mockRealm.getName()).thenReturn("TestRealm");
        when(mockRealm.supports(any(AuthenticationToken.class))).then(invocation -> {
            AuthenticationToken token = invocation.getArgumentAt(0, AuthenticationToken.class);
            return token instanceof UsernamePasswordToken;
        });

        when(mockRealm.getAuthenticationInfo(any(AuthenticationToken.class))).then(invocation -> {

            UsernamePasswordToken token = invocation.getArgumentAt(0, UsernamePasswordToken.class);
            if (!"password".equals(new String(token.getPassword()))) {
                throw new AuthenticationException("Bad password");
            }

            return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), "TestRealm");
        });

        return mockRealm;
    }

    @After
    public void after() {
        SecurityUtils.setSecurityManager(null);
    }

    @Test
    public void testFullStack() {

        Realm mockRealm = mockRealm();

        BQRuntime runtime = testFactory.app()
                .module(b -> ShiroModule.extend(b).addRealm(mockRealm))
                .autoLoadModules()
                .createRuntime()
                .getRuntime();


        // setup static vars before we can run Shiro assertions
        SecurityUtils.setSecurityManager(runtime.getInstance(SecurityManager.class));

        SubjectManager subjectManager = runtime.getInstance(SubjectManager.class);
        Subject subject = subjectManager.subject();
        assertNotNull(subject);
        assertFalse(subject.isAuthenticated());

        // try bad login
        try {
            subject.login(new UsernamePasswordToken("uname", "badpassword"));
            Assert.fail("Should have thrown on bad auth");
        } catch (AuthenticationException authEx) {
            assertFalse(subject.isAuthenticated());
        }

        // try good login
        subject.login(new UsernamePasswordToken("uname", "password"));

        assertTrue(subject.isAuthenticated());
    }
}

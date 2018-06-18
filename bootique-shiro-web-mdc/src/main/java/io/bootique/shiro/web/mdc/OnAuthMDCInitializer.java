/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.shiro.web.mdc;

import io.bootique.shiro.mdc.PrincipalMDC;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Initializes MDC state after user authentication.
 *
 * @since 0.25
 */
public class OnAuthMDCInitializer implements AuthenticationListener {

    PrincipalMDC principalMDC;

    public OnAuthMDCInitializer(PrincipalMDC principalMDC) {
        this.principalMDC = principalMDC;
    }

    @Override
    public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {

        // TODO: will this mess things up if authentication happens outside of a web request? Should we set some request
        // attribute or a ThreadLocal from within PrincipalMDCCleaner.requestInitialized(..) and check it here?

        Object principal = info.getPrincipals().getPrimaryPrincipal();
        principalMDC.reset(principal);
    }

    @Override
    public void onFailure(AuthenticationToken token, AuthenticationException ae) {
        // do nothing... should we clear the MDC here?
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        principalMDC.clear();
    }
}

package com.zj.cas.config;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;

@Component
public class MyAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    public MyAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential, String originalPassword) throws GeneralSecurityException, PreventedException {


        try {
            Map<String, Object> userMap = jdbcTemplate.queryForMap("select * from zj_user where `account`  = ? limit 1", new String[]{credential.getUsername()});


            return createHandlerResult(credential, this.principalFactory.createPrincipal(credential.getUsername()), new ArrayList<>(0));
        } catch (EmptyResultDataAccessException e) {
            throw new AccountNotFoundException("该用户不存在");
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new FailedLoginException("Unable to authenticate " + credential.getId());
    }
}
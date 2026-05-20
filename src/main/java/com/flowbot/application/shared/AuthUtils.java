package com.flowbot.application.shared;

import com.flowbot.application.context.TenantThreads;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;

public final class AuthUtils {

    public static String identifyResourceOwner(final Object principal) {
        return extractResourceOwner((Jwt) principal);
    }

    public static String setTenant(String resourceOwner) {
        var length = resourceOwner.length();
        if (length < 4) {
            TenantThreads.setTenantId(resourceOwner);
            return resourceOwner;
        }
        var primeiras4caracteres = resourceOwner.substring(0, 4);
        var ultimas4caracteres = resourceOwner.substring(length - 4);
        var result = primeiras4caracteres + ultimas4caracteres;
        TenantThreads.setTenantId(result);
        return result;
    }

    public static String setTenantFromEmail(String email) {
        var processed = email.replace("-", "").replace(".", "");
        return setTenant(processed);
    }

    private static String extractResourceOwner(final Jwt jwt) {
        var keysSearcheds = List.of("sub");
        var resourceOwner = new ArrayList<>();
        for (String key : keysSearcheds) {
            if (jwt.getClaims().containsKey(key)) {
                resourceOwner.add(jwt.getClaims().get(key).toString() + "-");
            }
        }

        var joined = Strings.join(resourceOwner, '-');
        return joined
                .replace("-", "")
                .replace(".", "");
    }
}

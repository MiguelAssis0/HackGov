package com.fiap.hackgov.shared.infra.permissions;

import com.fiap.hackgov.shared.infra.security.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    private final SecurityContext securityContext;

    @Before("@annotation(requiresPermission)")
    public void checkPermission(RequiresPermission requiresPermission) {
        String required = requiresPermission.resource() + ":" + requiresPermission.action().name();

        System.out.println("REQUIRED: " + required);
        System.out.println("PERMISSIONS: " + securityContext.getCurrentPermissions());
        System.out.println(securityContext.getCurrentPermissions().contains(required));

        if (!securityContext.getCurrentPermissions().contains(required)) {
            throw new AccessDeniedException("Sem permissão para: " + required);
        }
    }
}

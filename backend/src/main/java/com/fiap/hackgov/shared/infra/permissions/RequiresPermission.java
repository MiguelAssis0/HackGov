package com.fiap.hackgov.shared.infra.permissions;

import com.fiap.hackgov.cityhall_management.internal.entities.enums.Actions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    String resource();
    Actions action();
}

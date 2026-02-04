package com.hrai.org.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Permission catalog for known permission codes.
 */
public final class PermissionCatalog {

    private static final List<String> DEFAULT_CODES = List.of(
            "*",
            "org:*",
            "org:unit:read",
            "org:unit:write",
            "org:user:read",
            "org:user:write",
            "org:role:read",
            "org:role:write",
            "org:audit:read",
            "hr:position:read",
            "hr:position:write",
            "hr:headcount:read",
            "hr:headcount:write",
            "hr:employee:read",
            "hr:employee:write",
            "hr:event:read",
            "hr:event:write",
            "hr:event:approve",
            "agent:*",
            "agent:use",
            "knowledge:*",
            "knowledge:read",
            "knowledge:write",
            "chat:use",
            "user:*",
            "role:*"
    );

    private PermissionCatalog() {
    }

    public static Set<String> defaultCodes() {
        return new LinkedHashSet<>(DEFAULT_CODES);
    }
}

package com.hrai.org.service;

import java.util.List;

/**
 * Permission query service.
 */
public interface PermissionService {

    List<String> listPermissions();

    boolean checkPermission(Long userId, String permission);
}

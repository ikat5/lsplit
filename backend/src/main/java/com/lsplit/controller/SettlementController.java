package com.lsplit.controller;

import org.springframework.web.bind.annotation.RestController;

/**
 * Settlement endpoints are handled directly in {@link GroupController}
 * under {@code /api/groups/{groupId}/settlements}.
 *
 * This class is intentionally empty and serves only as a marker so that
 * the package is present for future extension if needed.
 */
@RestController
public class SettlementController {
    // All settlement operations live in GroupController.
}

package com.emm.mybest.domain.models

/**
 * How many apps are selected for capture; capture is on only while at least one is (ADR 0002).
 */
data class CaptureSummary(
    val selectedAppCount: Int,
) {
    val isActive: Boolean = selectedAppCount > 0
}

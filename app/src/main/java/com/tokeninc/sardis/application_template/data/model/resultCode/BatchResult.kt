package com.tokeninc.sardis.application_template.data.model.resultCode

/**
 * This is enum class for holding results of Batch.
 */
enum class BatchResult(val resultCode: Int) {
    SUCCESS(0),
    ERROR(1);
}

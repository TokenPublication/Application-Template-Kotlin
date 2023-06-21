package com.tokeninc.sardis.application_template.data.entities.responses

import com.tokeninc.sardis.application_template.enums.BatchResult
import java.text.SimpleDateFormat

/**
 * This class is for holding data after batch close operation ends.
 */
class BatchCloseResponse(var batchResult: BatchResult, var date: SimpleDateFormat) {
}
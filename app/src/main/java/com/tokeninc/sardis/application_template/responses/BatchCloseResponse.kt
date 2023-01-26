package com.tokeninc.sardis.application_template.responses

import com.tokeninc.sardis.application_template.enums.BatchResult
import java.text.SimpleDateFormat

class BatchCloseResponse(var batchResult: BatchResult, var date: SimpleDateFormat) {
}
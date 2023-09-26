package com.tokeninc.sardis.application_template.data.repositories

import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.database.activation.ActivationDao
import javax.inject.Inject

/**
 * @param activationDao is Data Access Object which comes from Dependency Injection thanks to Inject annotation,
 */
class ActivationRepository @Inject constructor(private val activationDao: ActivationDao) {
    fun merchantID() = activationDao.getMerchantId()
    fun terminalID() = activationDao.getTerminalId()
    fun hostIP() = activationDao.getHostIP()
    fun hostPort() = activationDao.getHostPort()

    suspend fun updateActivation(terminalId: String?, merchantId: String?){
        activationDao.updateActivation(terminalId,merchantId)
    }

    suspend fun updateConnection(ip: String?, port: String?){
        activationDao.updateConnection(ip,port)
    }
}

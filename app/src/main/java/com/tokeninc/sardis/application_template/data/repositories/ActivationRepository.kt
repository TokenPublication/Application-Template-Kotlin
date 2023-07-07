package com.tokeninc.sardis.application_template.data.repositories

import com.tokeninc.sardis.application_template.data.database.activation.ActivationDao
import javax.inject.Inject

/**
 * @param activationDao is Data Access Object which comes from Dependency Injection thanks to Inject annotation,
 */

class ActivationRepository @Inject constructor(private val activationDao: ActivationDao) {
    val merchantID = activationDao.getMerchantId()
    val terminalID = activationDao.getTerminalId()
    val hostIP = activationDao.getHostIP()
    val hostPort = activationDao.getHostPort()

    suspend fun updateActivation(terminalId: String?, merchantId: String?, ip: String?){
        activationDao.updateActivation(terminalId,merchantId,ip)
    }

    suspend fun updateConnection(ip: String?, port: String?, old_ip: String?){
        activationDao.updateConnection(ip,port,old_ip)
    }

}
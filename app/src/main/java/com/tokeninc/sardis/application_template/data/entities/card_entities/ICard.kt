package com.tokeninc.sardis.application_template.data.entities.card_entities

/**
 * This interface to connect MSR and ICC cards onCardDataReceived method after reading Card
 * However this app was designed only ICC card, therefore to implement MSR read needs extra effort
 * Therefore it's TODO for now
 */
interface ICard {
    var resultCode : Int
    var mCardReadType: Int
    var mCardNumber: String?
}

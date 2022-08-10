package com.example.ergomosaik.mosaikapp

import org.ergoplatform.ergopay.ErgoPayResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class UserSessionController(val sessionService: UserSessionService) {
    private val logger = LoggerFactory.getLogger(UserSessionController::class.java)

    @GetMapping("/getUserAddress/{sessionId}")
    fun getUserAddress(@PathVariable sessionId: String?): String? {
        val userData = sessionService.getUserData(sessionId!!)
        return if (userData.p2pkAddress != null) userData.p2pkAddress else ""
    }

    @GetMapping("/setAddress/{sessionId}/{address}")
    fun setAddress(
        @PathVariable sessionId: String,
        @PathVariable address: String
    ): ErgoPayResponse {
        val userData: UserData = sessionService.getUserData(sessionId)
        logger.info("Received address $address for session $sessionId")
        val response = ErgoPayResponse()

        // check the address
        try {
            response.address = address
            userData.p2pkAddress = address
            response.message =
                "Connected to your address $address.\n\nYou can now continue using the dApp."
            response.messageSeverity = ErgoPayResponse.Severity.INFORMATION
        } catch (t: Throwable) {
            response.messageSeverity = ErgoPayResponse.Severity.ERROR
            response.message = t.message
        }
        return response
    }
}
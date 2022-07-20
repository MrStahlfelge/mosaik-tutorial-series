package com.example.ergomosaik.mosaikapp

import org.ergoplatform.appkit.*
import org.ergoplatform.ergopay.ErgoPayResponse
import org.ergoplatform.mosaik.*
import org.ergoplatform.mosaik.model.FetchActionResponse
import org.ergoplatform.mosaik.model.MosaikApp
import org.ergoplatform.mosaik.model.actions.Action
import org.ergoplatform.mosaik.model.ui.layout.Padding
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@CrossOrigin
class SendFundsAppController {
    private val idSenderAddress = "ergaddress"
    private val idRecipient = "recipient"
    private val idAmountToSend = "amount"
    private val idReloadAction = "reloadAction"

    @GetMapping("/sendfunds")
    fun getSendFundsApp(): MosaikApp {
        return mosaikApp(
            "Send Funds Mosaik App",
            appVersion = 1
        ) {
            card {
                column(Padding.DEFAULT) {

                    ergoAddressChooser(idSenderAddress)

                    ergAddressInputField(idRecipient, "Recipient", mandatory = true)

                    ergAmountInputField(idAmountToSend, "Amount", canUseFiatInput = true) {
                        minValue = 1000 * 1000
                    }

                    button("Send") {
                        onClickAction(backendRequest("sendfundsClicked"))
                    }
                }
            }

            // we add the reload action for use in sendFundsClicked
            reloadApp(idReloadAction)
        }
    }


    @PostMapping("/sendfunds/sendfundsClicked")
    fun sendFundsClicked(@RequestBody values: Map<String, Any?>, request: HttpServletRequest): FetchActionResponse {
        val sender = values[idSenderAddress] as? String
        val recipient = values[idRecipient] as? String
        // depending on the actual value, we get the amount as an Integer or a Long. This handles both
        val amount = (values[idAmountToSend] as? Number)?.toLong()

        // do some checks here like this
        // these are done by the Mosaik executor as well, but you never know
        val ok =
            sender != null && recipient != null && amount != null && (amount >= Parameters.MinChangeValue)

        val responseAction: Action = if (!ok) {
            showDialog("Inputs are not valid", id = "errorDialog")
        } else {
            // returns the URL this endpoint was reached; we use it to build the ErgoPay url with
            // the same hostname the wallet application used to connect to this endpoint
            val thisEndpointHostname = request.requestURL.toString().substringAfter("://").substringBefore("/")
            val ergoPayUrl = "ergopay://$thisEndpointHostname/sendFunds/$sender/$recipient/$amount"
            invokeErgoPay(ergoPayUrl, id = "epSendFunds") {
                // this will empty all inputs for the next iteration
                onFinished = idReloadAction
            }
        }

        return backendResponse(
            1,
            responseAction
        )
    }

    @GetMapping("/sendFunds/{sender}/{recipient}/{amount}")
    fun sendFundsErgoPaySigningRequest(
        @PathVariable sender: String,
        @PathVariable recipient: String,
        @PathVariable amount: Long
    ): ErgoPayResponse {
        val response = ErgoPayResponse()
        try {
            val reduced = getReducedSendTx(
                amount,
                Address.create(sender),
                Address.create(recipient)
            ).toBytes()
            response.reducedTx = Base64.getUrlEncoder().encodeToString(reduced)
            response.address = sender
            response.message = "Please sign the transaction."
            response.messageSeverity = ErgoPayResponse.Severity.INFORMATION
        } catch (t: Throwable) {
            response.messageSeverity = ErgoPayResponse.Severity.ERROR
            response.message = t.message
        }
        return response
    }

    private fun getReducedSendTx(
        amountToSend: Long,
        sender: Address,
        recipient: Address
    ): ReducedTransaction {
        val networkType = recipient.networkType
        return RestApiErgoClient.create(
            getDefaultNodeUrl(networkType),
            networkType,
            "",
            RestApiErgoClient.getDefaultExplorerUrl(networkType)
        ).execute { ctx: BlockchainContext ->
            val contract = recipient.toErgoContract()
            val unsignedTransaction = BoxOperations.createForSender(sender, ctx)
                .withAmountToSpend(amountToSend)
                .putToContractTxUnsigned(contract)
            ctx.newProverBuilder().build().reduce(unsignedTransaction, 0)
        }
    }

    // this class processes all requests from the an ErgoPay wallet application
    val nodeMainnet = "http://213.239.193.208:9053/"
    val nodeTestnet = "http://213.239.193.208:9052/"

    private fun getDefaultNodeUrl(networkType: NetworkType): String =
        if (networkType == NetworkType.MAINNET) nodeMainnet else nodeTestnet
}
package com.iie.android.domain.valuation

import java.math.BigDecimal
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EthereumRpcBurnCrossCheckerTest {
    private val contract = "0x00000000000000000000000000000000000000aa"
    private val tx = "0x" + "a".repeat(64)
    private val blockHash = "0x" + "b".repeat(64)
    private val event = VerifiedErc20BurnEvent(
        assetId = "asset:ethereum",
        chainId = 1,
        tokenContract = contract,
        transactionHash = tx,
        blockNumber = 16,
        logIndex = 2,
        observedAtEpochMs = 1_700_000_000_000L,
        rawAmount = BigInteger("100"),
        tokenAmount = BigDecimal("1.00"),
        tokenDecimals = 2,
        sourceUrl = "https://api.routescan.io/"
    )

    private fun log(amount: String = "64", hash: String = blockHash) = """
        {
          "address":"$contract",
          "topics":[
            "${RoutescanErc20BurnEvidenceSource.TRANSFER_TOPIC}",
            "0x0000000000000000000000000000000000000000000000000000000000000001",
            "${RoutescanErc20BurnEvidenceSource.ZERO_TOPIC}"
          ],
          "data":"0x$amount",
          "blockNumber":"0x10",
          "transactionHash":"$tx",
          "transactionIndex":"0x0",
          "blockHash":"$hash",
          "logIndex":"0x2",
          "removed":false
        }
    """.trimIndent()

    private fun block(hash: String = blockHash) = """
        {"jsonrpc":"2.0","id":1,"result":{
          "number":"0x10","hash":"$hash","timestamp":"0x6553f100"
        }}
    """.trimIndent()

    @Test fun confirmsExactLogAndCanonicalBlockTimestamp() {
        var calls = 0
        val checker = EthereumRpcBurnCrossChecker(EthereumRpcTransport { _, _ ->
            calls++
            if (calls == 1) """{"jsonrpc":"2.0","id":1,"result":[${log()}]}""" else block()
        }) { 1_700_000_100_000L }
        val result = checker.crosscheck(event)
        assertTrue(result is RpcBurnCrosscheckResult.Confirmed)
        val confirmed = result as RpcBurnCrosscheckResult.Confirmed
        assertEquals("RPC_CROSSCHECKED_ETHEREUM_MAINNET_PENDING_FINALITY", confirmed.event.evidenceStatus)
        assertEquals(blockHash, confirmed.canonicalBlockHash)
        assertEquals(blockHash, confirmed.event.rpcBlockHash)
        assertEquals(EthereumRpcBurnCrossChecker.ETHEREUM_MAINNET_RPC, confirmed.event.rpcEndpoint)
        assertEquals(1_700_000_100_000L, confirmed.event.rpcCheckedAtEpochMs)
        assertEquals(2, calls)
    }

    @Test fun rejectsAmountMismatch() {
        val checker = EthereumRpcBurnCrossChecker(EthereumRpcTransport { _, _ ->
            """{"jsonrpc":"2.0","id":1,"result":[${log(amount = "65")}]}"""
        })
        val result = checker.crosscheck(event)
        assertEquals("RPC_EVENT_AMOUNT_MISMATCH", (result as RpcBurnCrosscheckResult.Abstained).reasonCode)
    }

    @Test fun rejectsCanonicalBlockHashMismatch() {
        val different = "0x" + "c".repeat(64)
        var calls = 0
        val checker = EthereumRpcBurnCrossChecker(EthereumRpcTransport { _, _ ->
            calls++
            if (calls == 1) """{"jsonrpc":"2.0","id":1,"result":[${log()}]}""" else block(different)
        })
        val result = checker.crosscheck(event)
        assertEquals(
            "RPC_BLOCK_HASH_MISMATCH_REORG_OR_INCONSISTENT_INDEX",
            (result as RpcBurnCrosscheckResult.Abstained).reasonCode
        )
    }

    @Test fun refusesUnconfiguredChainsWithoutCallingRpc() {
        var called = false
        val checker = EthereumRpcBurnCrossChecker(EthereumRpcTransport { _, _ -> called = true; "{}" })
        val result = checker.crosscheck(event.copy(chainId = 137))
        assertTrue(!called)
        assertEquals(
            "RPC_ENDPOINT_NOT_CONFIGURED_FOR_CHAIN",
            (result as RpcBurnCrosscheckResult.Abstained).reasonCode
        )
    }
}

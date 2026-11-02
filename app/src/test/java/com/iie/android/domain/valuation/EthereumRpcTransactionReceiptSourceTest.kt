package com.iie.android.domain.valuation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EthereumRpcTransactionReceiptSourceTest {
    private val tx = "0x" + "a".repeat(64)
    private val blockHash = "0x" + "b".repeat(64)
    private val target = "0x090D4613473dEE047c3f2706764f49E0821D256e"
    private val token = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984"
    private val beneficiary = "0x2222222222222222222222222222222222222222"
    private val blockNumber = "0xa5a5a5"
    private val blockNumberLong = blockNumber.removePrefix("0x").toLong(16)
    private val timestamp = "0x5f62f8a2"

    private fun response(method: String, canonicalHash: String = blockHash, status: String = "0x1"): String =
        when {
            method.contains("eth_chainId") ->
                """{"jsonrpc":"2.0","id":1,"result":"0x1"}"""
            method.contains("eth_getTransactionByHash") ->
                """{"jsonrpc":"2.0","id":1,"result":{"hash":"__TX__","to":"__TARGET__","blockNumber":"__BLOCK__","blockHash":"__HASH__"}}"""
                    .replace("__TX__", tx).replace("__TARGET__", target)
                    .replace("__BLOCK__", blockNumber).replace("__HASH__", blockHash)
            method.contains("eth_getTransactionReceipt") -> {
                val claimLog = """{"address":"__TARGET__","topics":["0x4ec90e965519d92681267467f775ada5bd214aa92c0dc93d90a5e880ce9ed026"],"data":"0x__ZERO64__","transactionHash":"__TX__","blockNumber":"__BLOCK__","blockHash":"__HASH__","logIndex":"0x4"}"""
                    .replace("__TARGET__", target).replace("__ZERO64__", "0".repeat(64))
                    .replace("__TX__", tx).replace("__BLOCK__", blockNumber).replace("__HASH__", blockHash)
                val transferLog = """{"address":"__TOKEN__","topics":["0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef","__FROM__","__TO__"],"data":"0x__AMOUNT__","transactionHash":"__TX__","blockNumber":"__BLOCK__","blockHash":"__HASH__","logIndex":"0x5"}"""
                    .replace("__TOKEN__", token)
                    .replace("__FROM__", "0x" + target.removePrefix("0x").padStart(64, '0'))
                    .replace("__TO__", "0x" + beneficiary.removePrefix("0x").padStart(64, '0'))
                    .replace("__AMOUNT__", "0".repeat(63) + "1")
                    .replace("__TX__", tx).replace("__BLOCK__", blockNumber).replace("__HASH__", blockHash)
                """{"jsonrpc":"2.0","id":1,"result":{"transactionHash":"__TX__","to":"__TARGET__","blockNumber":"__BLOCK__","blockHash":"__HASH__","status":"__STATUS__","logs":[__CLAIM__,__TRANSFER__]}}"""
                    .replace("__TX__", tx).replace("__TARGET__", target).replace("__BLOCK__", blockNumber)
                    .replace("__HASH__", blockHash).replace("__STATUS__", status)
                    .replace("__CLAIM__", claimLog).replace("__TRANSFER__", transferLog)
            }
            method.contains("eth_getBlockByNumber") ->
                """{"jsonrpc":"2.0","id":1,"result":{"hash":"__HASH__","timestamp":"__TIME__"}}"""
                    .replace("__HASH__", canonicalHash).replace("__TIME__", timestamp)
            else -> """{"jsonrpc":"2.0","id":1,"error":{"code":-1,"message":"unexpected"}}"""
        }

    private fun source(canonicalHash: String = blockHash, status: String = "0x1") =
        EthereumRpcTransactionReceiptSource(
            transport = EthereumRpcTransport { _, body -> response(body, canonicalHash, status) },
            clock = { 1_790_000_000_000L }
        )

    @Test fun fetchesReceiptAndChecksCanonicalBlockAndAllLogReferences() {
        val result = source().fetch(tx)
        assertTrue(result is RpcTransactionReceiptResult.Confirmed)
        val receipt = (result as RpcTransactionReceiptResult.Confirmed).receipt
        assertEquals(1L, receipt.chainId)
        assertEquals(tx, receipt.transactionHash)
        assertEquals(target, receipt.transactionTo)
        assertEquals(blockNumberLong, receipt.blockNumber)
        assertEquals(blockHash, receipt.blockHash)
        assertTrue(receipt.succeeded)
        assertTrue(receipt.rpcCrosschecked)
        assertEquals(2, receipt.logs.size)
        assertEquals(4L, receipt.logs[0].logIndex)
        assertEquals(5L, receipt.logs[1].logIndex)
    }

    @Test fun refusesCanonicalBlockHashMismatch() {
        val result = source(canonicalHash = "0x" + "c".repeat(64)).fetch(tx)
        assertEquals("RPC_CANONICAL_BLOCK_HASH_MISMATCH",
            (result as RpcTransactionReceiptResult.Abstained).reasonCode)
    }

    @Test fun preservesFailedReceiptStatusForProtocolDecoderToReject() {
        val result = source(status = "0x0").fetch(tx)
        assertTrue(result is RpcTransactionReceiptResult.Confirmed)
        assertFalse((result as RpcTransactionReceiptResult.Confirmed).receipt.succeeded)
    }

    @Test fun rejectsMalformedTransactionHashBeforeRpc() {
        val result = source().fetch("0x1234")
        assertEquals("TRANSACTION_HASH_INVALID",
            (result as RpcTransactionReceiptResult.Abstained).reasonCode)
    }
}

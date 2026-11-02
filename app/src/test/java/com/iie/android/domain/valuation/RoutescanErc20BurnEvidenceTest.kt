package com.iie.android.domain.valuation

import org.junit.Assert.*
import org.junit.Test

class RoutescanErc20BurnEvidenceTest {
    private val contract = "0x00000000000000000000000000000000000000aa"
    private val tx = "0x" + "a".repeat(64)
    private val from = "0x" + "0".repeat(24) + "1".repeat(40)
    private val zero = RoutescanErc20BurnEvidenceSource.ZERO_TOPIC
    private val transfer = RoutescanErc20BurnEvidenceSource.TRANSFER_TOPIC

    private fun payload(to: String = zero, address: String = contract) = """
        {
          "status":"1",
          "message":"OK",
          "result":[{
            "address":"$address",
            "topics":["$transfer","$from","$to"],
            "data":"0x64",
            "blockNumber":"0x10",
            "timeStamp":"0x6553f100",
            "transactionHash":"$tx",
            "logIndex":"0x2"
          }]
        }
    """.trimIndent()

    @Test fun recognizesOnlyStandardTransferToZeroAddress() {
        val source = RoutescanErc20BurnEvidenceSource(RoutescanHttpTransport { payload() })
        val result = source.fetch("asset:test", 1, contract, 2, 1, 100)
        val events = (result as RoutescanBurnFetchResult.Ready).events
        assertEquals(1, events.size)
        assertEquals("1.00", events.single().tokenAmount.toPlainString())
        assertEquals(16L, events.single().blockNumber)
        assertEquals(2L, events.single().logIndex)
        assertEquals(1_700_000_000_000L, events.single().observedAtEpochMs)
        assertEquals("EXPLORER_INDEXED_EVENT_PENDING_RPC_CROSSCHECK", events.single().evidenceStatus)
    }

    @Test fun ignoresTransfersToNonZeroDeadAddress() {
        val deadAddress = "0x" + "0".repeat(24) + "d".repeat(40)
        val source = RoutescanErc20BurnEvidenceSource(RoutescanHttpTransport { payload(to = deadAddress) })
        val result = source.fetch("asset:test", 1, contract, 2, 1, 100)
        assertTrue((result as RoutescanBurnFetchResult.Ready).events.isEmpty())
    }

    @Test fun validatesDeploymentAddressAndRangeBeforeCallingProvider() {
        var called = false
        val source = RoutescanErc20BurnEvidenceSource(RoutescanHttpTransport { called = true; payload() })
        val result = source.fetch("asset:test", 1, "not-an-address", 18, 10, 1)
        assertFalse(called)
        assertEquals(
            "QUERY_RANGE_OR_TOKEN_METADATA_INVALID",
            (result as RoutescanBurnFetchResult.Abstained).reasonCode
        )
    }

    @Test fun filtersLogsFromDifferentContracts() {
        val otherContract = "0x00000000000000000000000000000000000000bb"
        val source = RoutescanErc20BurnEvidenceSource(
            RoutescanHttpTransport { payload(address = otherContract) }
        )
        val result = source.fetch("asset:test", 1, contract, 2, 1, 100)
        assertTrue((result as RoutescanBurnFetchResult.Ready).events.isEmpty())
    }
}

# TxMempool

## .properties configuration

.properties file are loaded by configurationServer service. These properties are almost auto-explicative:

## REST API

### Alarms api
`/alarms/list` List of alarms generated by this service. Errors or unexpected events on mempool.

### Algorithm differences API
`/algo/diffs/{height}` List algorithm differences for a block height (Algorithms includes ours or bitcoind's getblocktemplate.
`/algo/diffs/last` Same as above but for last block
`/algo/liveDiffs` Compares candidate blocks of one algoritm with the other, using the tx that are now in mempool

### Ignored Transactions  
`/liveIgnored/{algo}/txs` List the ignored txs depending on the algorithm ('ours' or 'bitcoind')
`/liveIgnored/{algo}/fullTxs` Same as above but returns all tx's data including tx ins and outs. 
`/liveIgnored/{algo}/txs/{txId}` List the ignored tx with txId depending on the algorithm ('ours' or 'bitcoind')
`/liveIgnored/{algo}/fullTxs/{txId}` Same as above but returns all tx's data including tx ins and outs.
`/liveIgnored/{algo}/txsNTimes/{nTimes}` List the ignored txs depending on the algorithm ('ours' or 'bitcoind') and having been ignored nTimes at least.
`/liveIgnored/{algo}/blocks` List statistics of all ignoring blocks in cache for an algorithm ('ours' or 'bitcoind')
`/liveIgnored/{algo}/blocks/{height}` List statistics of block with 'height' in cache for an algorithm ('ours' or 'bitcoind').
`/liveIgnored/{algo}/blocks/last` List statistics of last block in cache for an algorithm ('ours' or 'bitcoind').

### Live Mining Queue data
`/liveMiningQueue/graphicData` Returns a histogram of the miningQueue for graphical representation.


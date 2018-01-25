const crypto = require('crypto')
const base64url = require('base64url')
const IlpPluginBtp = require('ilp-plugin-btp') // to be used by receiver
const { createReceiver, sendSingleChunk, quoteDestinationAmount } = require('.')
const IlpPacket = require('ilp-packet')
const IlDcp = require('ilp-protocol-ildcp')
function sha256 (preimage) {
  return crypto.createHash('sha256').update(preimage).digest()
}

const receiverToken = 'VkCu6Pw-r-6QsdF97jCKPkp6KThorPRLme4jj8LXmEA'
const fulfillment = Buffer.from(receiverToken, 'base64')
const condition = sha256(fulfillment)
console.log(base64url(fulfillment), base64url(condition))
const plugin = new IlpPluginBtp({ server: `btp+wss://:${receiverToken}@amundsen.ilpdemo.org:1801` })
plugin.connect().then(() => {
  console.log('connected')
  plugin.sendData(IlDcp.serializeIldcpRequest())
  plugin.registerDataHandler(data => {
    console.log('got data!', data)
    return IlpPacket.serializeIlpFulfill({
      fulfillment,
      data: Buffer.from('thank you')
    })
  })
})

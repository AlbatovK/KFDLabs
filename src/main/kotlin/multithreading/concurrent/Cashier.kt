package multithreading.concurrent

/**
 * To demonstrate concurrency transactions have different time of executing
 */
@Suppress("MagicNumber")
class Cashier(private val cashierId: Long, private val bank: Bank) : Thread() {

    private fun getExchangeValue(fromCurrency: String, toCurrency: String, amount: Long): Long {
        val currencyDlt =
            bank.exchangeRates.getOrDefault(fromCurrency, 1)
                .toDouble() / bank.exchangeRates.getOrDefault(toCurrency, 1)
        return (amount * currencyDlt).toLong()
    }

    private fun log(
        client: Client,
        receiverClient: Client? = null,
        toCurrency: String? = null,
        transaction: Transaction
    ) {
        bank.notifyObservers(
            when (transaction) {
                is Transaction.WithdrawTransaction ->
                    "Cashier $cashierId - withdraw | id=${client.id}:" +
                        " ${client.balance.get() + transaction.amount} -> ${client.balance} " +
                        "by ${transaction.amount}"

                is Transaction.DepositTransaction ->
                    "Cashier $cashierId - deposit | id=${client.id}:" +
                        " ${client.balance.get() - transaction.amount} -> ${client.balance} " +
                        "by ${transaction.amount}"

                is Transaction.TransferTransaction -> {
                    receiverClient?.let {
                        "Cashier $cashierId - transfer | id=${client.id} -> id=${receiverClient.id}" +
                            " by ${transaction.amount} in" +
                            " ${client.currency} to ${receiverClient.currency}" +
                            " balance ${receiverClient.balance}"
                    } ?: "$cashierId ${client.id}: Invalid log data"
                }

                is Transaction.ExchangeCurrencyTransaction -> {
                    toCurrency?.let {
                        "Cashier $cashierId - exchange | id=${client.id}:" +
                            " -> ${client.balance} " +
                            "by ${client.currency} -> $toCurrency"
                    } ?: "$cashierId ${client.id}: Invalid log data"
                }
            }
        )
    }

    @Suppress("TooGenericExceptionThrown")
    override fun run() {
        while (true) {
            /**
             * From Java 1.5 docs: Retrieves and removes the head of this queue,
             * waiting if necessary until an element becomes available.
             */
            val transaction = bank.transactionQueue.take()

            val client = bank.clients[transaction.clientId]
            if (client == null) {
                bank.notifyObservers("Cashier $cashierId - No such bank client found")
                error("Client not authorized")
            }

            when (transaction) {
                is Transaction.DepositTransaction -> {
                    synchronized(client.lock) {
                        sleep(1000)
                        client.balance.addAndGet(transaction.amount)
                        log(client = client, transaction = transaction)
                    }
                }

                /**
                 * To avoid deadlock and livelock situation
                 * the order that locks are acquired in is dependent on their id value
                 * Actually locks here are not necessary since Client is already thread-safe because
                 * its members are either marked with volatile or atomic types
                 */
                is Transaction.TransferTransaction -> {
                    val receiverClient = bank.clients[transaction.receiverId]
                    if (receiverClient == null) {
                        bank.notifyObservers("Cashier $cashierId - No such bank client found")
                        error("Client not authorized")
                    }
                    synchronized(if (receiverClient.id > client.id) receiverClient.lock else client.lock) {
                        synchronized(if (receiverClient.id > client.id) client.lock else receiverClient.lock) {
                            sleep(3000)
                            val addAmount = getExchangeValue(
                                client.currency,
                                receiverClient.currency, transaction.amount
                            )
                            receiverClient.balance.addAndGet(addAmount)
                            client.balance.addAndGet(-transaction.amount)
                            log(client = client, receiverClient = receiverClient, transaction = transaction)
                        }
                    }
                }

                is Transaction.WithdrawTransaction -> {
                    synchronized(client.lock) {
                        sleep(1000)
                        client.balance.addAndGet(-transaction.amount)
                        log(client = client, transaction = transaction)
                    }
                }

                is Transaction.ExchangeCurrencyTransaction -> {
                    with(transaction) {
                        synchronized(client.lock) {
                            sleep(2000)
                            val newAmount = getExchangeValue(client.currency, toCurrency, client.balance.get())
                            client.balance.set(newAmount)
                            log(client = client, toCurrency = toCurrency, transaction = transaction)
                            client.currency = toCurrency
                        }
                    }
                }
            }
        }
    }
}

package multithreading

/**
 * To demonstrate concurrency transactions have different time of executing
 */
class Cashier(private val cashierId: Long, private val bank: Bank) : Thread() {

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
                return
            }

            when (transaction) {
                is Transaction.DepositTransaction -> {
                    synchronized(client.lock) {
                        sleep(1000)
                        client.balance.addAndGet(transaction.amount)
                        bank.notifyObservers(
                            "Cashier $cashierId - deposit | id=${client.id}:" +
                                    " ${client.balance.get() - transaction.amount} -> ${client.balance} " +
                                    "by ${transaction.amount}"
                        )
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
                        throw IllegalStateException("Client not authorized")
                    }
                    synchronized(if (receiverClient.id > client.id) receiverClient.lock else client.lock) {
                        synchronized(if (receiverClient.id > client.id) client.lock else receiverClient.lock) {
                            sleep(3000)
                            val clientCur = bank.exchangeRates.getOrDefault(client.currency, 1)
                            val receiverCur = bank.exchangeRates.getOrDefault(receiverClient.currency, 1)
                            val currencyDlt = clientCur.toDouble() / receiverCur
                            val addAmount = (currencyDlt * transaction.amount).toLong()
                            receiverClient.balance.addAndGet(addAmount)
                            client.balance.addAndGet(-transaction.amount)
                            bank.notifyObservers(
                                "Cashier $cashierId - transfer | id=${client.id} -> id=${receiverClient.id}" +
                                        " by ${transaction.amount} in" +
                                        " ${client.currency} to $addAmount in ${receiverClient.currency}" +
                                        " balance ${receiverClient.balance}"
                            )
                        }
                    }
                }

                is Transaction.WithdrawTransaction -> {
                    synchronized(client.lock) {
                        sleep(1000)
                        client.balance.addAndGet(-transaction.amount)
                        bank.notifyObservers(
                            "Cashier $cashierId - withdraw | id=${client.id}:" +
                                    " ${client.balance.get() + transaction.amount} -> ${client.balance} " +
                                    "by ${transaction.amount}"
                        )
                    }
                }

                is Transaction.ExchangeCurrencyTransaction -> {
                    with(transaction) {
                        synchronized(client.lock) {
                            sleep(2000)
                            val currencyDlt =
                                bank.exchangeRates.getOrDefault(client.currency, 1)
                                    .toDouble() / bank.exchangeRates.getOrDefault(toCurrency, 1)
                            val newAmount = (client.balance.get() * currencyDlt).toLong()
                            val oldBalance = client.balance.get()
                            client.balance.set(newAmount)
                            bank.notifyObservers(
                                "Cashier $cashierId - exchange | id=${client.id}:" +
                                        " $oldBalance -> ${client.balance} " +
                                        "by ${client.currency} -> $toCurrency"
                            )
                            client.currency = toCurrency
                        }
                    }
                }
            }
        }
    }
}
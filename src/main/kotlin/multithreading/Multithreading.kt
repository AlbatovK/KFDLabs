package multithreading

import multithreading.concurrent.Bank
import multithreading.concurrent.Client
import multithreading.concurrent.Transaction
import multithreading.logging.Logger


fun main() {
    val bank = Bank()
    val clientA = Client(0, 100, "EUR")
    val clientB = Client(1, 20, "RUB")
    val clientC = Client(2, 200, "USD")

    listOf(clientA, clientB, clientC).forEach { bank.addClient(it) }
    bank.addObserver(Logger())

    /**
     * Test transactions
     */
    listOf(
        Transaction.WithdrawTransaction(clientA.id, 20),
        Transaction.DepositTransaction(clientC.id, 40),
        Transaction.WithdrawTransaction(clientA.id, 80),
        Transaction.ExchangeCurrencyTransaction(clientA.id, "RUB"),
        Transaction.TransferTransaction(clientC.id, clientA.id, 20),
        Transaction.WithdrawTransaction(clientB.id, 10),
        Transaction.ExchangeCurrencyTransaction(clientA.id, "EUR"),
        Transaction.WithdrawTransaction(clientC.id, 20),
        Transaction.DepositTransaction(clientA.id, 100),
        Transaction.ExchangeCurrencyTransaction(clientB.id, "USD"),
        Transaction.ExchangeCurrencyTransaction(clientA.id, "USD")
    ).forEach {
        bank.transactionQueue.add(it)
    }

    bank.startServing()
}


/** Example from main
 * log: Cashier 1 - withdraw | id=0: 100 -> 80 by 20
 * log: Cashier 2 - deposit | id=2: 200 -> 240 by 40
 * log: Cashier 3 - withdraw | id=0: 80 -> 0 by 80
 * log: Cashier 3 - withdraw | id=1: 20 -> 10 by 10
 * log: Bank - update: {EUR=50, USD=126, RUB=94}
 * log: Clients - update: {0=0 0 EUR, 1=1 10 RUB, 2=2 240 USD}
 * log: Cashier 2 - transfer | id=2 -> id=0 by 20 in USD to 50 in EUR balance 50
 * log: Cashier 2 - withdraw | id=2: 220 -> 200 by 20
 * log: Cashier 1 - exchange | id=0: 50 -> 26 by EUR -> RUB
 * log: Cashier 2 - deposit | id=0: 26 -> 126 by 100
 * log: Cashier 1 - exchange | id=1: 10 -> 7 by RUB -> USD
 * log: Bank - update: {EUR=128, USD=63, RUB=161}
 * log: Clients - update: {0=0 126 RUB, 1=1 7 USD, 2=2 200 USD}
 * log: Cashier 3 - exchange | id=0: 126 -> 158 by RUB -> EUR
 * log: Cashier 2 - exchange | id=0: 158 -> 321 by EUR -> USD
 * log: Bank - update: {EUR=199, USD=129, RUB=166}
 * log: Clients - update: {0=0 321 USD, 1=1 7 USD, 2=2 200 USD}
 * log: Bank - update: {EUR=123, USD=174, RUB=209}
 * log: Clients - update: {0=0 321 USD, 1=1 7 USD, 2=2 200 USD}
 * log: Bank - update: {EUR=177, USD=70, RUB=194}
 * log: Clients - update: {0=0 321 USD, 1=1 7 USD, 2=2 200 USD}
 */
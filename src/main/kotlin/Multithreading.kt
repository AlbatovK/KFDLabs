import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs

class Bank {
    fun startServing() {
        cashiers.forEach(Thread::start)
    }

    fun addClient(client: Client) {
        clients[client.id] = client
    }

    /**
     * Thread-safe by Java implementation
     */
    val transactionQueue = LinkedBlockingQueue<Transaction>()

    /**
     * Kotlin objects (singletons) are thread-safe by implementation
     */
    object CurrencyConstants {
        /**
         * Thread-safe version of ArrayList from Java
         */
        val currencyList: List<String> = Collections.synchronizedList(
            listOf("USD", "EUR", "RUB")
        )
    }

    /**
     * Thread-safe by Java implementation
     */
    private val random = Random()

    /**
     * Thread-safe by Java implementation
     */
    val clients = ConcurrentHashMap<Long, Client>()

    /**
     * Thread-safe by Java implementation
     */
    private val cashiers = CopyOnWriteArrayList(
        listOf(
            Cashier(1, this),
            Cashier(2, this),
            Cashier(3, this)
        )
    )

    /**
     * Thread-safe by Java implementation
     */
    val exchangeRates = ConcurrentHashMap<String, Long>()

    init {

        val executor = ScheduledThreadPoolExecutor(1)

        val updateRunnable = {
            for (i in CurrencyConstants.currencyList) {
                exchangeRates[i] = abs(random.nextLong()) % 200
            }
            notifyObservers("Bank - update: $exchangeRates")
            notifyObservers("Clients - update: $clients")
        }

        executor.scheduleAtFixedRate(
            updateRunnable, 0, 5, TimeUnit.SECONDS
        )
    }

    private val observers = Collections.synchronizedList(mutableListOf<Observer>())
    fun addObserver(observer: Observer) {
        observers.add(observer)
    }

    fun notifyObservers(message: String) {
        observers.forEach {
            it.update(message)
        }
    }
}

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

            val client = bank.clients[transaction.clientId] ?: throw IllegalStateException("Client not authorized")

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
                        ?: throw IllegalStateException("Client not authorized")
                    synchronized(if (receiverClient.id > client.id) receiverClient.lock else client.lock) {
                        synchronized(if (receiverClient.id > client.id) client.lock else receiverClient.lock) {
                            sleep(3000)
                            val clientCur = bank.exchangeRates[client.currency]!!
                            val receiverCur = bank.exchangeRates[receiverClient.currency]!!
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
                                bank.exchangeRates[client.currency]!!.toDouble() / bank.exchangeRates[toCurrency]!!
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

class Client(
    val id: Long,
    balance: Long,
    @Volatile var currency: String
) {
    val balance = AtomicLong(balance)
    val lock = ReentrantLock()

    override fun toString(): String {
        return "$id $balance $currency"
    }
}

sealed class Transaction(val clientId: Long) {
    class DepositTransaction(
        clientId: Long,
        val amount: Long
    ) : Transaction(clientId)

    class WithdrawTransaction(
        clientId: Long,
        val amount: Long
    ) : Transaction(clientId)

    class ExchangeCurrencyTransaction(
        clientId: Long,
        val toCurrency: String,
    ) : Transaction(clientId)

    class TransferTransaction(
        clientId: Long,
        val receiverId: Long,
        val amount: Long
    ) : Transaction(clientId)
}

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

interface Observer {
    fun update(message: String)
}

class Logger : Observer {
    override fun update(message: String) {
        println("log: $message")
    }
}
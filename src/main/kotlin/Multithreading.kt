/**
 * ### Домашнее задание: Расширенное многопоточное программирование на Kotlin
 *
 * #### Описание задания:
 *
 * Создайте многопоточное приложение на Kotlin, которое не только симулирует работу банка с кассами, но и добавляет дополнительные слои сложности, такие как валютный обмен и переводы между клиентами.
 *
 * #### Компоненты:
 *
 * 1. **Клиент**: объект с уникальным ID, суммой денег, и валютой.
 *
 * 2. **Касса**: обработчик транзакций, каждая касса работает в отдельном потоке.
 *
 * 3. **Банк**: хранит информацию о всех клиентах и кассах, а также курсах валют.
 *
 * #### Требования:
 *
 * 1. Добавить функциональность для обмена валют: `exchangeCurrency(clientId: Int, fromCurrency: String, toCurrency: String, amount: Double)`.
 *
 * 2. Реализовать переводы между клиентами: `transferFunds(senderId: Int, receiverId: Int, amount: Double)`.
 *
 * 3. Использовать `ScheduledThreadPoolExecutor` для автоматического обновления курсов валют.
 *
 * 4. Реализовать очередь транзакций, которая обрабатывается асинхронно.
 *
 * 5. Применить паттерн Observer для логгирования.
 *
 * ### Дополнительные детали к домашнему заданию
 *
 * #### 1. Использование `ScheduledThreadPoolExecutor` для автоматического обновления курсов валют
 *
 * Цель этого требования — симулировать реальный мир, где курсы валют постоянно меняются. Создайте задачу, которая автоматически обновляет курсы валют в вашем `Bank` объекте.
 *
 * ```kotlin
 * import java.util.concurrent.ScheduledThreadPoolExecutor
 * import java.util.concurrent.TimeUnit
 *
 * class Bank {
 *     val exchangeRates = ConcurrentHashMap<String, Double>()
 *
 *     init {
 *         val executor = ScheduledThreadPoolExecutor(1)
 *         executor.scheduleAtFixedRate({
 *             // Здесь обновляйте курсы валют. Например:
 *             exchangeRates["USD"] = getRandomExchangeRate()
 *         }, 0, 1, TimeUnit.HOURS)
 *     }
 * }
 * ```
 *
 * #### 2. Реализация очереди транзакций, которая обрабатывается асинхронно
 *
 * Идея здесь в том, чтобы операции (переводы, пополнения, снятия и т.д.) не обрабатывались мгновенно, а добавлялись в очередь. Эта очередь затем асинхронно обрабатывается отдельными потоками (кассами).
 *
 * ```kotlin
 * import java.util.concurrent.LinkedBlockingQueue
 *
 * class Bank {
 *     val transactionQueue = LinkedBlockingQueue<Transaction>()
 *
 *         init {
 *             // Запускаем потоки-кассы для обработки очереди
 *         }
 *     }
 *
 *     class Cashier : Thread() {
 *         override fun run() {
 *             while (true) {
 *                 val transaction = Bank.transactionQueue.take()
 *                 // Обрабатываем транзакцию
 *             }
 *         }
 *     }
 * ```
 *
 * #### 3. Применение паттерна Observer для логгирования
 *
 * Паттерн Observer позволяет оповестить зарегистрированные объекты (наблюдатели) о событиях, происходящих в системе. В данной задаче, его можно использовать для логгирования всех операций и событий, таких как переводы, пополнения баланса и изменения курса валют.
 *
 * Сначала создайте интерфейс Observer:
 *
 * ```kotlin
 * interface Observer {
 *     fun update(message: String)
 * }
 * ```
 *
 * Затем добавьте его реализацию, которая будет записывать логи:
 *
 * ```kotlin
 * class Logger : Observer {
 *     override fun update(message: String) {
 *         // Здесь ваш код для логгирования, например:
 *         println("Log: $message")
 *     }
 * }
 * ```
 *
 * В классе `Bank`, добавьте методы для регистрации и оповещения наблюдателей:
 *
 * ```kotlin
 * class Bank {
 *     private val observers = mutableListOf<Observer>()
 *
 *         fun addObserver(observer: Observer) {
 *             observers.add(observer)
 *         }
 *
 *         fun notifyObservers(message: String) {
 *             observers.forEach {
 *                 it.update(message)
 *             }
 *         }
 *     }
 * ```
 *
 * Теперь, каждый раз при изменении курса валюты или выполнении транзакции, вызывайте `notifyObservers` с соответствующим сообщением:
 *
 * ```kotlin
 * fun deposit(clientId: Int, amount: Double) {
 *     // ... ваш код
 *     notifyObservers("Deposit successful for client $clientId, amount: $amount")
 * }
 * ```
 *
 * #### Примерная структура кода:
 *
 * ```kotlin
 * class Client(val id: Int, var balance: Double, var currency: String)
 *
 * class Cashier(val id: Int, val bank: Bank) : Thread() {
 *     fun deposit(clientId: Int, amount: Double) { /* ... */ }
 *     fun withdraw(clientId: Int, amount: Double) { /* ... */ }
 *     fun exchangeCurrency(clientId: Int, fromCurrency: String, toCurrency: String, amount: Double) { /* ... */ }
 *     fun transferFunds(senderId: Int, receiverId: Int, amount: Double) { /* ... */ }
 * }
 *
 * class Bank {
 *     val clients = ConcurrentHashMap<Int, Client>()
 *     val cashiers = ArrayList<Cashier>()
 *     val exchangeRates = ConcurrentHashMap<String, Double>()
 *
 *     // Другие функции и механизмы
 * }
 * ```
 * #### Концепции
 * В данном домашнем задании, многопоточность будет активно использоваться в нескольких местах:
 *
 * 1. Кассы (Cashiers):
 * Каждая касса будет работать в своём потоке, обрабатывая транзакции из общей очереди.
 * Потоки нужно будет синхронизировать таким образом, чтобы не возникло проблем с конкурентным доступом к данным клиентов.
 *
 * 2. Автоматическое обновление курсов валют:
 * Используя ScheduledThreadPoolExecutor, вы можете создать отдельный поток, который будет регулярно обновлять курсы валют.
 * Здесь ключевой момент в том, чтобы корректно обновлять информацию, доступ к которой могут иметь другие потоки.
 *
 * 3. Асинхронная очередь транзакций:
 * Элементы этой очереди будут обрабатываться асинхронно кассами.
 * Важно удостовериться, что операции с очередью потокобезопасны.
 *
 * 4. Логгирование с использованием паттерна Observer:
 * В этом случае, многопоточность может косвенно затрагивать логгирование, так как обновления могут приходить из разных потоков.
 * Нужно обеспечить, чтобы метод update в Observer был потокобезопасным.
 *
 * Все эти многопоточные операции требуют аккуратной синхронизации и возможно использование примитивов синхронизации для обеспечения корректного доступа к ресурсам
 *
 * #### Что ожидается:
 *
 * 1. Код должен быть написан с учетом принципов ООП и SOLID.
 *
 * 2. Аккуратное логгирование всех операций и ошибок.
 *
 * 3. Предоставить документацию и инструкцию по запуску.
 *
 */


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
                exchangeRates[i] = abs(random.nextLong()) % 200 + 20
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
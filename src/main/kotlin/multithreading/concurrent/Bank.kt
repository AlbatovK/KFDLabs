package multithreading.concurrent

import java.util.*
import java.util.concurrent.*
import kotlin.math.abs
import multithreading.logging.Observer

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
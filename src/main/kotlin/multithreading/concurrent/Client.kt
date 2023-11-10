package multithreading.concurrent

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock

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

package multithreading.logging

class Logger : Observer {
    override fun update(message: String) {
        println("log: $message")
    }
}

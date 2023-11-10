package multithreading

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
package app.sandy.improv.data

interface KeyStore {
    fun apiKey(): String
    fun setApiKey(value: String)
    fun model(): String
    fun setModel(value: String)
}

package app.sandy.improv.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PrefsKeyStore(ctx: Context) : KeyStore {
    private val prefs = EncryptedSharedPreferences.create(
        ctx,
        "sandy_secure",
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun apiKey(): String = prefs.getString("api_key", "") ?: ""
    override fun setApiKey(value: String) { prefs.edit().putString("api_key", value.trim()).apply() }
    override fun model(): String = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"
    override fun setModel(value: String) { prefs.edit().putString("model", value).apply() }
}

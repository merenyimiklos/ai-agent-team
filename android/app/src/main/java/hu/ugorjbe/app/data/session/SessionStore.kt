package hu.ugorjbe.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.ugorjbe.app.domain.Session
import hu.ugorjbe.app.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface SessionStore {
    val session: Flow<Session?>
    suspend fun currentToken(): String?
    suspend fun save(session: Session)
    suspend fun clear()
}

private val Context.sessionDataStore by preferencesDataStore(name = "secure_session")

@Singleton
class DataStoreSessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : SessionStore {
    private object Keys {
        val token = stringPreferencesKey("access_token")
        val id = stringPreferencesKey("user_id")
        val email = stringPreferencesKey("user_email")
        val name = stringPreferencesKey("user_display_name")
        val locale = stringPreferencesKey("user_locale")
        val created = stringPreferencesKey("user_created_at")
    }

    override val session: Flow<Session?> = context.sessionDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw exception
        }
        .map { values ->
            val token = values[Keys.token] ?: return@map null
            val user = User(
                id = values[Keys.id] ?: return@map null,
                email = values[Keys.email] ?: return@map null,
                displayName = values[Keys.name] ?: return@map null,
                locale = values[Keys.locale] ?: "hu-HU",
                createdAtUtc = values[Keys.created] ?: "",
            )
            Session(token, user)
        }

    override suspend fun currentToken(): String? = session.first()?.accessToken

    override suspend fun save(session: Session) {
        context.sessionDataStore.edit { values ->
            values[Keys.token] = session.accessToken
            values[Keys.id] = session.user.id
            values[Keys.email] = session.user.email
            values[Keys.name] = session.user.displayName
            values[Keys.locale] = session.user.locale
            values[Keys.created] = session.user.createdAtUtc
        }
    }

    override suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}

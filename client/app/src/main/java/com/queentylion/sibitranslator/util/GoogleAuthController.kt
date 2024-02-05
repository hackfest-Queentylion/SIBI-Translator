package com.queentylion.sibitranslator.util

import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleAuthController {
    private val credentialsJSON = "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"sturdy-hangar-409704\",\n" +
            "  \"private_key_id\": \"664dc3f662d031633759761895a0b1ff198bcd57\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDBXQERG+COp10z\\njMVV590594A112L9lxQf5Lw7BBf6AKh7fjAFE7AL0f8lcPlZKvKaW4tlVUjhP9Wd\\nH6JK9Cc6vMo0aFsaQnb5GYqy3FBjhVYm2e8i3H/C0PkUP/SQ7EIWlinU9/RRqjc+\\nxjH7dlI8z+V0rjwZ8r2FfZZ5bwHlvQBtqDGVYuycmYzylEyfnee/fKRkAlv1Tug+\\nPiEUI5yi1fS5cLYOnXZryFptyEF5Fr8eP2tg68IVfasi2U6uz/ukG6ziN4DKo28A\\ndXTVvvxAqwgVFsj8hl3zcGHlU9mxOV0mnWMP9XbZayhLqa3pokelQ3pOk1hh4DGk\\no2L1E/iFAgMBAAECggEAJeQeQeaHFh1I38Gtp9xkVygfeS2Iae4xlOBjXGM8eZKo\\ncW60ZUJK5L4VdBZT3nvSh5n50nUob29tYjlhObPnfhShwSxT8ezlWIH/UnZm0GHA\\nFKPvPxMbfCcsMCIqQD3z424wq1mdiGVFJyl6gO7aRa1tpvQ/tcwCBIcsNgahRg3w\\nmXvLE7GX1kLoidI1d4SYn11mLy+fYtotqKFQzXWqDRPy5Dzd++oqM8X1ej8uRtCz\\n9IAk6CkBDJwzK6+d0Gj/idayd6qYYtUU19OmEyilLa+Xk2750em1hh1KNiJhgqqe\\nTxw6fcuMtfYmuUB9HUANZpq07nyK5+80VvI5oMS8DwKBgQDp4y0upmWYPq0m2YPz\\ntokdAAJcY9pE2+ZDvT6P9QCN10TwsfPwgOn+LGXD7Ays7SYa+Il7WHiFojX101Dc\\nKUHH0LTlde2fU/ncJ41tsWUUjrC6fos2mpBphDyJ3Qpcnukk1VJnfc/rIN/9MiH0\\ncWM2o1fUS+RgzE+fzNeV8jzsVwKBgQDTpQO576n3OqK8xohuzaGP0H2hEC1hl6iF\\nMRL+ZK/KUVUTS3n5/mdj1ivVrtHiv0ADI0lo6mG65e83aTZFK/d5VAomgpA54aNK\\nfUffI3/m83mQtvTgiQ9zkNgznqp5QNYYkYJJe1JWQhb142plAWCxF8QMmyC4uTG4\\nGlgGFOw4gwKBgGSDylYjEsRUI0vv7QJfLxv0dg9IpnQzYQk5mlp5u4w5uJoMkD6K\\n2ITwhaemmWfz3w12RHdq9RjRNol4EGcdn/SEoEmA3ec8SsQvh1teAofMLu1nFuMX\\nl/qQ5weEpEBb1uyKdQifDC0LitegpPENjcrcdhF5sCNditatTPVXDpGvAoGBAL3o\\nVb2j1cknbhshsg5qqUvYcsHxOCdX5DkPXdGzGyHZdRNJKHwv0Sn+ZXrp0R87KP8n\\nzJk9ptADvnDkEXRkDT1rMWh4w12Mn+8ZF0KcIgpj8nLuGDDaC2lRUQ6QkrsWeIW3\\nG0dARxNrXhrpIvDbGZ4OizYchHH8iyZ9TPq4D3ZtAoGBAK1Ur58RH+5mcD0qiqCW\\nE7VoBrQxgEyjUhBlReUFjiqymyc/maG/x+WccCZpc37NwFN9tGsdGtxps2KZb6Wg\\nQTdFhIUkqq7ayXE32b+vzqo66Hera5qxh98ompDBw10CZhwepxIaOziBX0DVZdfc\\njW5p+d9U3CzrOgyyJxs9Str2\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"hackfest-deadliner@sturdy-hangar-409704.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"102289979521276475594\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/hackfest-deadliner%40sturdy-hangar-409704.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}"

    suspend fun getAccessToken(): String {
        val credentials: GoogleCredentials =
            GoogleCredentials.fromStream(credentialsJSON.byteInputStream())

        return withContext(Dispatchers.IO) {
            try{
                credentials.refreshIfExpired()
                credentials.accessToken.tokenValue
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

    }
}
package com.queentylion.sibitranslator.util

import com.google.auth.oauth2.GoogleCredentials

class GoogleAuthController {
    private val credentialsJSON = "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"sturdy-hangar-409704\",\n" +
            "  \"private_key_id\": \"db0c9d5f19898ea9b9a7be292de9eefcdfde0a15\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDJ/meR5XY3drB6\\nVG0R32ZcAiKIcyQB7p2bR56I/AGe5TcSKErBtKDkR7NBrYskPdeGwSQo9dFowrxs\\nRWNZjlqr+kuq34h619URR9k55edGQHDof2Jxbu9sv+reXYAWQ2Two5kcRPDA/UTv\\n1tVvD27MFE9ZFOwD99xAG4+Uk3UpqeB0FDja0xwcDA2CBwjVm19kLTgCfw8wn658\\nDpkEOMtwa8PG20tkLmM9S9nXhQ/7ZIf75VS9v9+6GtaIArT5lEF014LlUFC6+zDb\\n8Gqdn2IRwvVJF9+suDYkQ345oK31de27b+PovT0+XmrCHtmXE9TkFJTi3PjTmuxS\\n9W6laOJrAgMBAAECggEATnj/2o3HFfgNypCHCQT9uqv3p4P3zqpZX2x8+iHRLV5G\\nPU8a79MRGG6EhPT7U9qUoxzgw+rv7l+NHRD97lpf/mUQRXNvDa5Q79Q2X4hiB4hc\\ndO6cG45qBJkwkS/I5Z1MFzKvdmyQDaG3SOfw5iMcO7t+MhDvOgFudxdd+e4pe8P6\\nWCcjGwy35YTZAup2HJrv6dq3YRohsZ8GYRgLgBdtr6fzFOVbjpU2it3IvJ6nchRN\\nSIfi+7hL8gH7CLkhOPLHdEdq1rJTGKXzyswoLJbiTgzFbUZRe6yK6V/H3UzJTRK5\\nbdQvUoOLrdphDeuOk30T8vCZyG8kj0vdY7IdfUSsKQKBgQD6Phk+D2wBFqPd82kj\\ntkEqJX1OImXHWKmlGex2mG+wtIkd1RV5kKAg9sm6kpU5a656CyI3eXZZDeF/cF1l\\nQUH+uHUY4QCnNDg289/Zei6jhNVLtOqXIpoU7qc6UkHbfpp/S+C+wSkVXEDgggMD\\nQ7I8ybUrPvlC1Mad4D19vroRcwKBgQDOpCAzLMZuN5Py7diRyFzkdjVadzooJfqh\\ntlPwnkgMY9GwPK/CiJ1YR4+N5zJ4d178gx40n3nwL0YathevaSE5dbINSlifhGNh\\ngOcvzjsB8DlDvlGvI4DWNN/dwaDTZppzyM3pICEkYp1yrdIKRK9CCzwWgwlFSW/G\\n2od3CxHNKQKBgFYpLX3Nx400Y2WXWrseFJ/TWqqdc1fI8lhTbbSD1ekMsC1iYcuC\\nfW/8KQchU1n69o806CobmyEcg2jionWrm3J9xmuzhQsNEtHw9EEoLYjFwr8XYrJ5\\nCn5skY2mJuDRXZa45IApd+DP69KhUTI9i9AcT1G9lAtrwZs4S1PRaLV7AoGBAMjt\\nhDXeis/vENg9d8FBTzoCyxw9JHqXe140+OfWMH6DrQgt6kVBK6YEZ0z3CvdiMyVb\\npUpL63ilrwgYGW3BzsGddNVBfm0VgMD1Y1bztCLNYBFEBQ9EeWlQHoH1XhlRAkwl\\nbDsLt842aZxx8fN0F+ojHHlTTvdlUd/M673QMK4JAoGBAN6bbcSvpAEJSEjjoYID\\np9nVmMSHC7IY1guIqprUNvm3XvRUYnlRRu0VM9xApzl+DE+1lUuAwkijWn4yLmHx\\nxudUziCtSezRqV+VkVozzcKVCFlo9gaKzscuzR/8Nj73kWyPhucnaDzX5OO+gnRZ\\nAud4OMq3VIolhlC5DWeGszeN\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"267809006279-compute@developer.gserviceaccount.com\",\n" +
            "  \"client_id\": \"103241607536970060330\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/267809006279-compute%40developer.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}"

    fun getAccessToken(): String {
        val credentials: GoogleCredentials =
            GoogleCredentials.fromStream(credentialsJSON.byteInputStream())

        val thread = Thread {
            try{
                credentials.refreshIfExpired()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()



        return credentials.accessToken.tokenValue
    }
}
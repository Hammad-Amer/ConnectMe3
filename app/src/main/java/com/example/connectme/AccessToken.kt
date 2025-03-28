package com.example.connectme

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {

    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"

    fun getAccessToken():String?{

        try{
            val jasonString: String = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"connectme-bb3d0\",\n" +
                    "  \"private_key_id\": \"ab011d1d9d93b4ae0becbbf155ab3bb2b23d5b43\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDPHLq8A6TGhKpd\\nm5g2qKq/qeU+fHKf5jVhXgQFg2Aw+E+/FqYAFqotolTI5Gx8BZStHZhBS0qcm5IR\\n1bldennxFWJlnYLPAiLO3NplUxTSZM+WpqA9ziZejfgdJaB8/JzmtrfIlRECZXjs\\n618hSVi5WJ1LZVQ6UK7cOjCukmXkTmChp54hLYEhdJrdvc9A4Bf6a3h6gv+qhGrY\\nb7vZQGyL73SDwS4th+fxAsKJfSuIp9XXNOPk8+8OcLCCSnvUzIgbiK4jVLcEuMLP\\nlLRXGG3R2M5k218uEphqm0x9A+9mNGfQVDHeib5yNNltF81JKjKJm3CJ5s2f9YdZ\\nClcvs6QLAgMBAAECggEAS408ZUei/TP5/em7jfzbRM2SLcgx/Dh7MoicSPKSzWpX\\nlOqLJa8e50S3Tw1csaVLhhcsqmzvy89Iaq8fToVWJGbbnRvnUuKBlhqkpJ+5EcLq\\noxcnj0Q7ZPS/FMrcD5wLFHKE+mu+Z7Rid3KpIjhiYEJ+JrnRDvDNyNKFXblT6AMy\\nuacA6d+tRkqUaE15t0RwRUgWXQn5Sd1x4Fs95KMsQQ9anmHs2B64k3rK2JjyacGO\\nb8d213gKToFux8XF1ixhSIsbLwlTNqHpFCRhqytbxgOYea7nh9nipjTcf6JEdlsf\\npY4rGoANxUc1yPIbm7n7/HKswO4HUOK+FTTol8UbkQKBgQD785IcNM4dY4mXjzeh\\n+yFTr1nRoPMkCvvgT2wdyCmOOEHJxpA1OJjui8WYCMTBAO0Bq2bXS/5/WnpUn0Mu\\nM1DTNdUyDtfAeHKxAn/cb7FPGIPNiAWWy++PSeqv0iimnE2DndVIyBglFThLlLpK\\ntiHi8qxyxCyc83EmTO8K7rmSQwKBgQDScLUuedeu+k2Ku+4/z58uwbujDA7aajWm\\n7Ldv2gQ8x/Oor5YLXRXxx6HcsXHZZ9oYNQlP7l5IR8fUM5c/HHFWE8uqqLR+MEzM\\nH15jmMBG2qmf7evW2knmor7pGNI5ZPXkvHC0VQVC/5RXcjBsOmqEvYILWAZHmdom\\n3BSjuLU+mQKBgQDvw8A0xW+seg/zQiCALnttpv6DxnX7w9Qkh5Bs2xHovNKmkSak\\n7yJiSHMWlqmeviHNp+5rTm93hIPLye/lpIHzLVSgmY1qJXWHy8gf387NZZfwXNRs\\nx9BG/OGwxf9XZoFBkk7pPzny+Dmle3i5JpAi6CuB3/xQtD7mYupM7hd8/wKBgEJX\\nv/bf+Kw0Qv/q0WiSlveRogr8AS7Abxup4wbDL7TNwLY8bMw2U3W+3vuJgX/tqqcZ\\nCU+GsXiOEKSIgzUlTWG6qemqeASUS5HKjeJORMfRpcpCoqhAdy3bvt4Tsirf9llf\\nQxTG/PqrQmPzHurv26bva7ER1iD6kvjPRz56O8F5AoGBAJX72RRRPhSGzEsIOM7U\\nntxKllE73bD6+JsVOkVyTVCW5gmkhpJvGUQjpXfH72GMb/q9jo1yg6ghrDrtmeuE\\nsz2Nwre7GPGrjLnb9g2V/sv/NYQRLKGCwa7bb4BZMe192ANHiT2fmb49sL9e2gG/\\nZvWP2AgSm2Vy+361UsvmiNsE\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-fbsvc@connectme-bb3d0.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"118051491197290655750\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40connectme-bb3d0.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}"
            val stream = ByteArrayInputStream(jasonString.toByteArray(StandardCharsets.UTF_8))

            val googleCredential = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebaseMessagingScope))

            googleCredential.refresh()

            return googleCredential.accessToken.tokenValue
        }catch (e:IOException){
            return null
        }
    }
}
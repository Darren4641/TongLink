package io.tonglink.app.config

import nl.martijndwars.webpush.PushService
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.Security

@Configuration
class NotificationConfig (
    @Value("\${vapid.key.subject}")
    val subject: String,
    @Value("\${vapid.key.public}")
    val publicKey: String,
    @Value("\${vapid.key.private}")
    val privateKey: String,
) {

    @Bean
    fun PushService() : PushService {
        Security.addProvider(BouncyCastleProvider());
        return PushService(publicKey, privateKey, subject);
    }
}
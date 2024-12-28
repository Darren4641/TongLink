package io.tonglink.app.common.security.converter


import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.tonglink.app.common.security.util.ProviderType
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.slf4j.LoggerFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.http.RequestEntity
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.util.MultiValueMap
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Date


class CustomRequestEntityConverter(
    val keyFilePath: String
) : Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<*>> {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val defaultConverter: OAuth2AuthorizationCodeGrantRequestEntityConverter = OAuth2AuthorizationCodeGrantRequestEntityConverter()


    override fun convert(source: OAuth2AuthorizationCodeGrantRequest): RequestEntity<*>? {
        val registrationId = ProviderType.valueOf(source.clientRegistration.registrationId.uppercase())
        val entity = defaultConverter.convert(source) ?: return null
        val params = entity.body as MultiValueMap<String, String>

        if(registrationId == ProviderType.APPLE) {
            val clientId = params.getFirst("client_id")
            if (clientId != null) {
                params.set("client_secret", createAppleClientSecret(clientId, "LJMR3H8DA7","RC7RLMFNSF"))
            }
        }
        return RequestEntity(params, entity.headers, entity.method, entity.url)
    }

    private fun createAppleClientSecret(clientId: String, keyId: String, teamId: String) : String {
        lateinit var clientSecret: String
        try {
            val file = File(keyFilePath)


            val pemParser = PEMParser(FileReader(file))
            val converter = JcaPEMKeyConverter()
            val privateKeyInfo = pemParser.readObject() as PrivateKeyInfo

            val privateKey = converter.getPrivateKey(privateKeyInfo)

            clientSecret = Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, keyId)
                .setIssuer(teamId)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .setExpiration(Date(System.currentTimeMillis() + (1000 * 60 * 5)))
                .setIssuedAt(Date(System.currentTimeMillis()))
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact()

        } catch (e: IOException) {
            logger.error("Error_createAppleClientSecret : {}-{}", e.message, e.cause)
        }
        return clientSecret;
    }

}
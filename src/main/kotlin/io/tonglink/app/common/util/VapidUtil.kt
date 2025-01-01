package io.tonglink.app.common.util

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security


class VapidUtil {

    //최초 1회만 하고 yml에 등록
    companion object {
        fun generateVapidKeyPair() {
            Security.addProvider(BouncyCastleProvider())

            // ECDSA 키 생성 (prime256v1 곡선 사용)
            val ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1")
            val keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC")
            keyPairGenerator.initialize(ecSpec)
            val keyPair: KeyPair = keyPairGenerator.generateKeyPair()

            // 공개 키를 압축되지 않은 포인트 형식으로 변환
            val publicKey = keyPair.public as org.bouncycastle.jce.interfaces.ECPublicKey
            val uncompressedPublicKey = publicKey.q.getEncoded(false)

            // Base64 URL-safe 인코딩
            val publicKeyBase64 = Base64.toBase64String(uncompressedPublicKey)
                .replace('+', '-')
                .replace('/', '_')
                .replace("=", "")
            val privateKeyBase64 = Base64.toBase64String(keyPair.private.encoded)
                .replace('+', '-')
                .replace('/', '_')
                .replace("=", "")

            println("Public Key: $publicKeyBase64")
            println("Private Key: $privateKeyBase64")
        }
    }

}
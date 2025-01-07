package io.tonglink.app.common.security.config


import io.tonglink.app.common.security.converter.CustomRequestEntityConverter
import io.tonglink.app.common.security.filter.TokenAuthenticationFilter
import io.tonglink.app.common.security.handler.AuthenticationEntryPointHandler
import io.tonglink.app.common.security.handler.OAuth2AuthenticationSuccessHandler
import io.tonglink.app.common.security.handler.TokenAccessDeniedHandler
import io.tonglink.app.common.security.repository.OAuth2AuthorizationRequestBasedOnCookieRepository
import io.tonglink.app.common.security.resolver.CustomAuthorizationRequestResolver
import io.tonglink.app.common.security.service.OAuth2UserService
import io.tonglink.app.common.security.service.UserDetailService
import io.tonglink.app.common.security.token.AuthTokenProvider
import io.tonglink.app.config.AppProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig (
    private val authTokenProvider: AuthTokenProvider,
    private val appProperties: AppProperties,
    private val userDetailService: UserDetailService,
    private val oAuth2UserService: OAuth2UserService,
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val authenticationEntryPointHandler: AuthenticationEntryPointHandler,
    private val tokenAccessDeniedHandler: TokenAccessDeniedHandler,
    @Value("\${key.file.path}")
    val keyFilePath: String
) {

    @Bean
    fun securityFilterChain(http : HttpSecurity) : SecurityFilterChain {
        http
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/v1/member/signup").permitAll()
                    .requestMatchers("/v1/member/login").permitAll()
                    .requestMatchers("/v1/**").authenticated()
                    .anyRequest().permitAll()
            }
            .formLogin{ formLogin -> formLogin.disable() }
            .httpBasic{ httpBasic -> httpBasic.disable() }
            .exceptionHandling{
                ex ->
                //인증 되지 않은 사용자 접근시 핸들러 -401
                ex.authenticationEntryPoint(authenticationEntryPointHandler)
                //인증은 되었으나, 권한이 없는경우 - 403
                ex.accessDeniedHandler(tokenAccessDeniedHandler)
            }
            .userDetailsService(userDetailService)
            .oauth2Login{ oauth2 ->
                oauth2.tokenEndpoint { tokenEndpoint ->
                    tokenEndpoint.accessTokenResponseClient(accessTokenResponseClient())
                }
//                oauth2.authorizationEndpoint { authorizationEndpoint ->
//                    authorizationEndpoint.authorizationRequestResolver(customAuthorizationRequestResolver(clientRegistrationRepository))
//                    authorizationEndpoint.baseUri("/oauth2/authorization")
//                }

                oauth2.authorizationEndpoint { authorizationEndpoint ->
                    authorizationEndpoint.authorizationRequestRepository(oauth2AuthorizationRequestBasedOnCookieRepository())
                    authorizationEndpoint.baseUri("/oauth2/authorization")
                }
                oauth2.redirectionEndpoint { redirectionEndpoint ->
                    redirectionEndpoint.baseUri("/*/oauth2/code/*")
                }
                oauth2.userInfoEndpoint { userInfoEndpoint ->
                    userInfoEndpoint.userService(oAuth2UserService)
                }
                oauth2.successHandler(oauth2AuthenticationSuccessHandler())
//                oauth2.failureHandler(oauth2AuthenticationFailureHandler())

            }
            .logout { logout ->
                logout.logoutUrl("/logout")
                logout.logoutSuccessUrl("/home")
            }

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)



        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun tokenAuthenticationFilter(): TokenAuthenticationFilter = TokenAuthenticationFilter(authTokenProvider)

    @Bean
    @Throws(Exception::class)
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager? = configuration.authenticationManager

    @Bean
    fun oauth2AuthorizationRequestBasedOnCookieRepository(): OAuth2AuthorizationRequestBasedOnCookieRepository {
        return OAuth2AuthorizationRequestBasedOnCookieRepository()
    }

    @Bean
    fun accessTokenResponseClient() : OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
        val accessTokenResponseClient = DefaultAuthorizationCodeTokenResponseClient()
        accessTokenResponseClient.setRequestEntityConverter(CustomRequestEntityConverter(keyFilePath))
        return accessTokenResponseClient
    }

    @Bean
    fun customAuthorizationRequestResolver(
        clientRegistrationRepository: ClientRegistrationRepository
    ): CustomAuthorizationRequestResolver {
        return CustomAuthorizationRequestResolver(clientRegistrationRepository)
    }

    @Bean
    fun oauth2AuthenticationSuccessHandler() : OAuth2AuthenticationSuccessHandler {
        return OAuth2AuthenticationSuccessHandler(
            authTokenProvider = authTokenProvider,
            appProperties = appProperties,
            authorizationRequestRepository = oauth2AuthorizationRequestBasedOnCookieRepository()
        )
    }
}
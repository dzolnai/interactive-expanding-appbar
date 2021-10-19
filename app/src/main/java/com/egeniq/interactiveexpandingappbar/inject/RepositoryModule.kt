package com.egeniq.interactiveexpandingappbar.inject

import com.egeniq.interactiveexpandingappbar.repository.MovieDbRepository
import com.egeniq.interactiveexpandingappbar.util.Obfuscator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideHeaderInterceptor() = object : Interceptor {
        private val token = Obfuscator.deObfuscate("y77XrqSsy8zCqJvCnbecpdjEp8HMp" +
                "KzYoamcn82qvcXOy5eW3sPGt6fLoOK/qpihpseb17mvlsa4\n" +
                "tt7GzcSsxs22qLqjraqkya2oxpe52seXraTO3a6mwqLHncfNwqO+o8OkpKa2ls/Mw6XG3sLbw7nB\n" +
                "3bejtqXCo7ejuqbH2sent9/Cosedx822qMqmt9y7o8KYx97C3LvJtt23zMHfv8zBp7ret+CppsOY\n" +
                "z5a3o7a5wt/Hp8Gnv7msqMaXq+LXxqfC38eWu8fGp8GnrcmuqMaXqw==\n")

        override fun intercept(chain: Interceptor.Chain): Response {
            return chain.proceed(chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build())
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(headerInterceptor: Interceptor) = Retrofit.Builder()
            .client(OkHttpClient.Builder()
                    .addInterceptor(headerInterceptor)
                    .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
                    .build())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.themoviedb.org/3/")
            .build()

    @Provides
    @Singleton
    fun provideMovieDbRepository(retrofit: Retrofit) = MovieDbRepository(retrofit)
}
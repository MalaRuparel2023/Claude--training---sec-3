package ro.alexmamo.firebasesigninwithemailandpassword.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import ro.alexmamo.firebasesigninwithemailandpassword.data.repository.AuthRepositoryImpl
import ro.alexmamo.firebasesigninwithemailandpassword.data.session.SessionManager
import ro.alexmamo.firebasesigninwithemailandpassword.domain.repository.AuthRepository

@Module
@InstallIn(ViewModelComponent::class)
class AppModule {
    @Provides
    fun provideFirebaseAuth() = Firebase.auth

    @Provides
    @ViewModelScoped
    fun provideSessionManager(
        @ApplicationContext context: Context,
        auth: FirebaseAuth
    ): SessionManager = SessionManager(context, auth)

    @Provides
    @ViewModelScoped
    fun provideAuthRepository(
        auth: FirebaseAuth,
        sessionManager: SessionManager
    ): AuthRepository = AuthRepositoryImpl(auth, sessionManager)

    @Provides
    @ViewModelScoped
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context
    ): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("375452750804-s9g22vpeuriu2b327hb0ebu8ck9fg2aj.apps.googleusercontent.com")
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}
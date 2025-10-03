package com.example.eventapp.di;

import android.content.Context;
import com.example.eventapp.utils.SessionManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }
} 
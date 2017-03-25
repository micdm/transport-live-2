package micdm.transportlive.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import dagger.Module;
import dagger.Provides;
import micdm.transportlive.AppScope;
import micdm.transportlive.ComponentHolder;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

@Module
public class DataModule {

    @Provides
    @AppScope
    RoutesLoader provideRoutesLoader() {
        RoutesLoader instance = new RoutesLoader();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    VehiclesLoader provideVehiclesLoader() {
        VehiclesLoader instance = new VehiclesLoader();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }

    @Provides
    @AppScope
    ServerConnector provideServerConnector() {
        ServerConnector instance = new ServerConnector();
        ComponentHolder.getAppComponent().inject(instance);
        return instance;
    }

    @Provides
    @AppScope
    ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    @Provides
    @AppScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient, Converter.Factory converterFactory, CallAdapter.Factory callAdapterFactory) {
        return new Retrofit.Builder()
            .baseUrl("http://gpt.incom.tomsk.ru/api/")
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build();
    }

    @Provides
    @AppScope
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().addInterceptor(chain -> {
            LocalDateTime now = LocalDateTime.now();
            Response response = chain.proceed(chain.request());
            Timber.d("Request %s completed with status %s in %sms", response.request().url(), response.code(), new Period(now, LocalDateTime.now()).getMillis());
            return response;
        }).build();
    }

    @Provides
    @AppScope
    Converter.Factory provideConverterFactory(Gson gson) {
        return GsonConverterFactory.create(gson);
    }

    @Provides
    @AppScope
    Gson provideGson() {
        return new GsonBuilder().create();
    }

    @Provides
    @AppScope
    CallAdapter.Factory provideCallAdapterFactory() {
        return RxJava2CallAdapterFactory.create();
    }

    @Provides
    @AppScope
    DataRepository provideDataRepository() {
        return new DataRepository();
    }

    @Provides
    @AppScope
    SelectedRoutesStore provideSelectedRoutesStore() {
        SelectedRoutesStore instance = new SelectedRoutesStore();
        ComponentHolder.getAppComponent().inject(instance);
        instance.init();
        return instance;
    }
}

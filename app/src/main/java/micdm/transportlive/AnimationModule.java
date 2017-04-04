package micdm.transportlive;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
class AnimationModule {

    @Provides
    @Named("showRoutes")
    Animator provideShowRoutesAnimator(Context context) {
        return AnimatorInflater.loadAnimator(context, R.animator.show_routes);
    }

    @Provides
    @Named("hideRoutes")
    Animator provideHideRoutesAnimator(Context context) {
        return AnimatorInflater.loadAnimator(context, R.animator.hide_routes);
    }
}

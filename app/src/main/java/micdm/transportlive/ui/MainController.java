package micdm.transportlive.ui;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.RouterTransaction;

import javax.inject.Inject;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.R;

public class MainController extends BaseController {

    @Inject
    MapController mapController;
    @Inject
    RoutesController routesController;

    @BindView(R.id.v__main__container)
    ViewGroup containerView;

    @NonNull
    @Override
    View inflateContent(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.c__main, container, false);
    }

    @Override
    protected void initRouter() {
        getChildRouter(containerView).pushController(RouterTransaction.with(mapController));
    }

    @Override
    protected Disposable subscribeForEvents() {
        return subscribeForSelectRoutesRequest();
    }

    private Disposable subscribeForSelectRoutesRequest() {
        return mapController.getGoToSelectRoutesRequests().subscribe(o ->
            getChildRouter(containerView).pushController(RouterTransaction.with(routesController))
        );
    }
}

package micdm.transportlive.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.stores.SelectedRoutesStore;

public class SelectedRoutesPresenter extends BasePresenter<SelectedRoutesPresenter.View> implements SelectedRoutesStore.Client {

    public interface View extends BasePresenter.View {

        Observable<Collection<String>> getSelectRoutesRequests();
    }

    @Inject
    SelectedRoutesStore selectedRoutesStore;

    @Override
    void initMore() {
        selectedRoutesStore.attach(this);
    }

    public Observable<Collection<String>> getSelectedRoutes() {
        return selectedRoutesStore.getSelectedRoutes();
    }

    @Override
    public Observable<Collection<String>> getSelectRoutesRequests() {
        return getViewInput(View::getSelectRoutesRequests);
    }
}

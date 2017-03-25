package micdm.transportlive.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.SelectedRoutesStore;

public class SelectedRoutesPresenter extends BasePresenter<SelectedRoutesPresenter.View> implements SelectedRoutesStore.Client {

    interface View extends BasePresenter.View {

        Observable<Collection<String>> getSelectRoutesRequests();
    }

    @Inject
    SelectedRoutesStore selectedRoutesStore;

    @Override
    void initMore() {
        selectedRoutesStore.attach(this);
    }

    Observable<Collection<String>> getSelectedRoutes() {
        return selectedRoutesStore.getSelectedRoutes();
    }

    @Override
    public Observable<Collection<String>> getSelectRoutesRequests() {
        return getViewInput(View::getSelectRoutesRequests);
    }
}
package micdm.transportlive.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import micdm.transportlive.data.stores.SelectedRoutesStore;
import micdm.transportlive.misc.Id;

public class SelectedRoutesPresenter extends BasePresenter<SelectedRoutesPresenter.View> implements SelectedRoutesStore.Client {

    public interface View {

        Observable<Collection<Id>> getSelectRoutesRequests();
    }

    @Inject
    SelectedRoutesStore selectedRoutesStore;

    @Override
    void initMore() {
        selectedRoutesStore.attach(this);
    }

    public Observable<Collection<Id>> getSelectedRoutes() {
        return selectedRoutesStore.getSelectedRoutes();
    }

    @Override
    public Observable<Collection<Id>> getSelectRoutesRequests() {
        return getViewInput(View::getSelectRoutesRequests);
    }
}

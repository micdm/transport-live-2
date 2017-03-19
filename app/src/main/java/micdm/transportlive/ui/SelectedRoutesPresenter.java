package micdm.transportlive.ui;

import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import micdm.transportlive.data.DataRepository;

public class SelectedRoutesPresenter extends BasePresenter<SelectedRoutesPresenter.View> {

    interface View extends BasePresenter.View {

        Observable<Set<String>> getSelectRoutesRequests();
    }

    @Inject
    DataRepository dataRepository;

    @Override
    Disposable subscribeForEvents() {
        return subscribeForSelectRoutesRequest();
    }

    private Disposable subscribeForSelectRoutesRequest() {
        return getViews().flatMap(View::getSelectRoutesRequests).subscribe(dataRepository::putSelectedRoutes);
    }

    Observable<Set<String>> getSelectedRoutes() {
        return dataRepository.getSelectedRoutes();
    }
}

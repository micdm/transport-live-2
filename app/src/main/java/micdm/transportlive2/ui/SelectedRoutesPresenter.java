package micdm.transportlive2.ui;

import java.util.Collection;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import micdm.transportlive2.data.stores.SelectedRoutesStore;
import micdm.transportlive2.misc.Id;

public class SelectedRoutesPresenter extends BasePresenter<SelectedRoutesPresenter.View, SelectedRoutesPresenter.ViewInput> implements SelectedRoutesStore.Client {

    public interface View {

        Observable<Collection<Id>> getSelectRoutesRequests();
    }

    static class ViewInput extends BasePresenter.ViewInput<View> {

        private final Subject<Collection<Id>> selectRoutesRequests = PublishSubject.create();

        Observable<Collection<Id>> getSelectRoutesRequests() {
            return selectRoutesRequests;
        }

        @Override
        Disposable subscribeForInput(View view) {
            return view.getSelectRoutesRequests().subscribe(selectRoutesRequests::onNext);
        }
    }

    @Inject
    SelectedRoutesStore selectedRoutesStore;

    @Override
    ViewInput newViewInput() {
        return new ViewInput();
    }

    @Override
    void attachToLoaders() {
        selectedRoutesStore.attach(this);
    }

    public Observable<Collection<Id>> getSelectedRoutes() {
        return selectedRoutesStore.getSelectedRoutes();
    }

    @Override
    public Observable<Collection<Id>> getSelectRoutesRequests() {
        return viewInput.getSelectRoutesRequests();
    }
}

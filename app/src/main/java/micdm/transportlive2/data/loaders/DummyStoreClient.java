package micdm.transportlive2.data.loaders;

class DummyStoreClient<Data> implements BaseLoader.StoreClient<Data> {

    @Override
    public void attach() {}

    @Override
    public void setData(Data data) {}
}

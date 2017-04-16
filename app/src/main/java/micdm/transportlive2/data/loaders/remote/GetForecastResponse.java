package micdm.transportlive2.data.loaders.remote;

import java.util.Collection;

public class GetForecastResponse {

    public static class Forecast {

        public final String fid;
        public final String pid;
        public final int tba;

        public Forecast(String fid, String pid, int tba) {
            this.fid = fid;
            this.pid = pid;
            this.tba = tba;
        }
    }

    public final String name;
    public final String desc;
    public final Collection<Forecast> forecasts;

    public GetForecastResponse(String name, String desc, Collection<Forecast> forecasts) {
        this.name = name;
        this.desc = desc;
        this.forecasts = forecasts;
    }
}

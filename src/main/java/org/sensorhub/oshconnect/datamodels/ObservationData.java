package org.sensorhub.oshconnect.datamodels;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.opengis.swe.v20.DataBlock;
import org.vast.util.Asserts;
import org.vast.util.BaseBuilder;

import java.time.Instant;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ObservationData {
    /**
     * Local ID of the observation.
     */
    protected String id;
    /**
     * Local ID of the datastream that the observation is part of.
     */
    @SerializedName("datastream@id")
    protected String datastreamId;
    /**
     * Local ID of the sampling feature that is the target of the observation.
     */
    @SerializedName("samplingFeature@id")
    protected String samplingFeatureId;
    /**
     * Link to the procedure/method used to make the observation.
     */
    @SerializedName("procedure@link")
    protected Link procedureLink;
    /**
     * Time at which the observation result is a valid estimate of the sampling feature property(ies).
     * Defaults to the same value as {@link #resultTime}.
     */
    protected Instant phenomenonTime;
    /**
     * The time at which the observation result was generated.
     */
    protected Instant resultTime;
    /**
     * Result of the observation.
     * Must be valid, according to the result schema provided in the datastream metadata.
     */
    protected DataBlock result;
    /**
     * Link to external result data (e.g. large raster dataset served by a tiling service).
     */
    @SerializedName("result@link")
    protected Link resultLink;
    /**
     * Links to related resources.
     */
    protected List<Link> links;

    public static ObservationData.ObservationDataBuilder newBuilder() {
        return new ObservationData.ObservationDataBuilder();
    }

    public static class ObservationDataBuilder extends BaseBuilder<ObservationData> {
        ObservationDataBuilder() {
            this.instance = new ObservationData();
        }

        public ObservationDataBuilder id(String id) {
            instance.id = id;
            return this;
        }

        public ObservationDataBuilder datastreamId(String datastreamId) {
            instance.datastreamId = datastreamId;
            return this;
        }

        public ObservationDataBuilder samplingFeatureId(String samplingFeatureId) {
            instance.samplingFeatureId = samplingFeatureId;
            return this;
        }

        public ObservationDataBuilder procedureLink(Link procedureLink) {
            instance.procedureLink = procedureLink;
            return this;
        }

        public ObservationDataBuilder phenomenonTime(Instant phenomenonTime) {
            instance.phenomenonTime = phenomenonTime;
            return this;
        }

        public ObservationDataBuilder resultTime(Instant resultTime) {
            instance.resultTime = resultTime;
            return this;
        }

        public ObservationDataBuilder result(DataBlock result) {
            instance.result = result;
            return this;
        }

        public ObservationDataBuilder resultLink(Link resultLink) {
            instance.resultLink = resultLink;
            return this;
        }

        public ObservationDataBuilder links(List<Link> links) {
            instance.links = links;
            return this;
        }

        @Override
        public ObservationData build() {
            Asserts.checkNotNull(instance.phenomenonTime, "phenomenonTime");
            Asserts.checkNotNull(instance.result, "result");
            return instance;
        }
    }
}

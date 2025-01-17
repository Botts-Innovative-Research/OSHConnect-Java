package org.sensorhub.oshconnect.datamodels;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.common.IdEncoders;
import org.sensorhub.api.data.IDataStreamInfo;
import org.sensorhub.impl.service.consys.ResourceParseException;
import org.sensorhub.impl.service.consys.SWECommonUtils;
import org.sensorhub.impl.service.consys.obs.ObsHandler.ObsHandlerContextData;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.ResourceBindingJson;
import org.sensorhub.impl.service.consys.resource.ResourceLink;
import org.sensorhub.utils.SWEDataUtils;
import org.vast.cdm.common.DataStreamWriter;
import org.vast.swe.BinaryDataWriter;
import org.vast.swe.ScalarIndexer;
import org.vast.swe.fast.JsonDataParserGson;
import org.vast.swe.fast.JsonDataWriterGson;
import org.vast.util.ReaderException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.sensorhub.impl.service.consys.SWECommonUtils.OM_COMPONENTS_FILTER;

public class ObservationBindingOmJson extends ResourceBindingJson<String, ObservationData> {
    ObsHandlerContextData contextData;
    JsonDataParserGson resultReader;
    Map<BigId, DataStreamWriter> resultWriters;
    ScalarIndexer timeStampIndexer;

    public ObservationBindingOmJson(RequestContext ctx, IdEncoders idEncoders, boolean forReading) throws IOException {
        super(ctx, idEncoders, forReading);
        this.contextData = (ObsHandlerContextData) ctx.getData();

        if (forReading) {
            resultReader = getSweCommonParser(contextData.dsInfo, reader);
            resultReader.setRenewDataBlock(true);
            timeStampIndexer = SWEDataUtils.getTimeStampIndexer(contextData.dsInfo.getRecordStructure());
        } else {
            this.resultWriters = new HashMap<>();

            // init result writer only in case of single datastream
            // otherwise we'll do it later
            if (contextData != null && contextData.dsInfo != null) {
                var resultWriter = getSweCommonWriter(contextData.dsInfo, writer);
                resultWriters.put(ctx.getParentID(), resultWriter);
            }
        }
    }

    @Override
    public ObservationData deserialize(JsonReader reader) throws IOException {
        // if it's an array, prepare to parse the first element
        if (reader.peek() == JsonToken.BEGIN_ARRAY)
            reader.beginArray();

        if (reader.peek() == JsonToken.END_DOCUMENT || !reader.hasNext())
            return null;

        var obs = ObservationData.newBuilder();

        try {
            reader.beginObject();

            while (reader.hasNext()) {
                var propName = reader.nextName();
                if ("id".equals(propName)) {
                    obs.id(reader.nextString());
                } else if ("datastream@id".equals(propName)) {
                    obs.datastreamId(reader.nextString());
                } else if ("phenomenonTime".equals(propName))
                    obs.phenomenonTime(OffsetDateTime.parse(reader.nextString()).toInstant());
                else if ("resultTime".equals(propName))
                    obs.resultTime(OffsetDateTime.parse(reader.nextString()).toInstant());
                else if ("samplingFeature@id".equals(propName)) {
                    obs.samplingFeatureId(reader.nextString());
                } else if ("result".equals(propName)) {
                    var result = resultReader.parseNextBlock();
                    obs.result(result);
                } else
                    reader.skipValue();
            }

            reader.endObject();
        } catch (DateTimeParseException e) {
            throw new ResourceParseException(INVALID_JSON_ERROR_MSG + "Invalid ISO8601 date/time at " + reader.getPath());
        } catch (IllegalStateException | ReaderException e) {
            throw new ResourceParseException(INVALID_JSON_ERROR_MSG + e.getMessage());
        }

        var newObs = obs.build();

        // set timestamp in result data if present in the schema
        if (timeStampIndexer != null) {
            var phenomenonTimeIdx = timeStampIndexer.getDataIndex(newObs.getResult());
            newObs.getResult().setDoubleValue(phenomenonTimeIdx, newObs.getPhenomenonTime().toEpochMilli() / 1000.0);
        }

        return newObs;
    }

    @Override
    public void serialize(String observationId, ObservationData observationData, boolean showLinks, JsonWriter writer) throws IOException {
        writer.beginObject();

        if (observationId != null) {
            writer.name("id").value(observationId);
        }

        if (observationData.getDatastreamId() != null) {
            writer.name("datastream@id").value(observationData.getDatastreamId());
        }

        writer.name("phenomenonTime").value(observationData.getPhenomenonTime().toString());
        if (observationData.getResultTime() != null) {
            writer.name("resultTime").value(observationData.getResultTime().toString());
        }

        // create or reuse the existing result writer and write result data
        writer.name("result");
        var resultWriter = getSweCommonWriter(contextData.dsInfo, writer);

        // write if JSON is supported, otherwise print a warning message
        if (resultWriter instanceof JsonDataWriterGson)
            resultWriter.write(observationData.getResult());
        else
            writer.value("Compressed binary result not shown in JSON");

        writer.endObject();
        writer.flush();
    }

    @Override
    public void startCollection() throws IOException {
        startJsonCollection(writer);
    }

    protected DataStreamWriter getSweCommonWriter(IDataStreamInfo dsInfo, JsonWriter writer) {
        if (!SWECommonUtils.allowNonBinaryFormat(dsInfo.getRecordStructure(), dsInfo.getRecordEncoding()))
            return new BinaryDataWriter();

        // create JSON SWE writer
        var sweWriter = new JsonDataWriterGson(writer);
        sweWriter.setDataComponents(dsInfo.getRecordStructure());

        // filter out components that are already included in O&M
        sweWriter.setDataComponentFilter(OM_COMPONENTS_FILTER);
        return sweWriter;
    }

    protected JsonDataParserGson getSweCommonParser(IDataStreamInfo dsInfo, JsonReader reader) {
        // create JSON SWE parser
        var sweParser = new JsonDataParserGson(reader);
        sweParser.setDataComponents(dsInfo.getRecordStructure());

        // filter out components that are already included in O&M
        sweParser.setDataComponentFilter(OM_COMPONENTS_FILTER);
        return sweParser;
    }

    @Override
    public void endCollection(Collection<ResourceLink> links) throws IOException {
        endJsonCollection(writer, links);
    }
}

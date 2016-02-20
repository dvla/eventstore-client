package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.api.EventDeserialiser;
import gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;
import gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import gov.dvla.osl.eventsourcing.api.EventStoreEvent;
import gov.dvla.osl.eventsourcing.exception.EventStoreClientUnknownStreamException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EventStoreStream {

    private static final Logger logger = LoggerFactory.getLogger(EventStoreStream.class);

    private String stream;
    private boolean keepAlive;
    private EventDeserialiser eventDeserialiser;
    private JSONObject payload;
    private boolean result;
    private List<EventStoreEvent> data;
    private String error;
    private String mimetype;
    private boolean startLongPoll = false;

    public EventStoreStream(String stream, boolean keepAlive, EventDeserialiser eventDeserialiser ) throws IOException {
        this.stream = stream;
        this.keepAlive = keepAlive;
        this.eventDeserialiser = eventDeserialiser;
        this.mimetype = "application/vnd.eventstore.atom+json";
    }

    public Observable<EventStoreEvent> readStreamEventsForward() {
        return Observable.create(subscribeFunction);
    }

    Observable.OnSubscribe<EventStoreEvent> subscribeFunction = (s) -> {

        Subscriber subscriber = (Subscriber) s;

        try {
            processData(subscriber);
        } catch (Exception e) {
            subscriber.onError(e);
        }
    };

    private void processData(Subscriber subscriber) throws Exception {

        getLastPageOfStream();

        if (!getResult()) {
            logger.error("Get Head of Stream failed: " + getErrorMessage());
            throw new EventStoreClientTechnicalException(getErrorMessage());
        }

        processEvents(subscriber);

        String previous;

        do {

            previous = getPreviousLink();

            do {
                getPreviousPayload(previous);

                while (getPreviousLink().equals("")) {

                    if (keepAlive) {
                        getPreviousPayload(previous);
                    } else {
                        subscriber.onCompleted();
                        return;
                    }
                }

                processEvents(subscriber);

            } while (!dataExists());

        } while (!previous.equals(""));
    }

    private void processEvents(Subscriber subscriber) throws Exception {

        this.data = getEvents(this.payload);

        if (dataExists()) {
            getData().forEach(subscriber::onNext);
        }
    }

    public void getLastPageOfStream() throws IOException {

        // Start at the head of the stream
        //
        logger.debug("Getting head of stream...");
        this.payload = extractPayload(getURL(stream));

        JSONArray entries = (JSONArray) payload.get("entries");

        if (entries.size() > 0) {
            String last = getLink(payload, "last");
            if (!last.equals("")) {
                this.payload = extractPayload(getURL(last));
                result = true;
            } else {
                result = true;  // This one is the last one
            }
        } else
            result = true;
    }

    public String getErrorMessage() {
        return this.error;
    }

    public boolean dataExists() {
        return this.data.size() > 0;
    }

    public List<EventStoreEvent> getData() {
        return this.data;
    }

    public void getPreviousPayload(String previous) throws IOException {
        logger.debug("Extracting previous payload...");
        this.payload = extractPayload(getURL(previous));
    }

    public String getPreviousLink() {

        String link = getLink(this.payload, "previous");

        if (link.equals(""))
            link = this.stream;

        return link;
    }

    private List<EventStoreEvent> getEvents(JSONObject payload) throws IOException, ClassNotFoundException {

        JSONArray entries = (JSONArray) payload.get("entries");

        List<EventStoreEvent> events = new ArrayList<>();

        for (int i = entries.size() - 1; i > -1; i--) {
            JSONObject entry = (JSONObject) entries.get(i);
            EventStoreEvent eventStoreEvent = new EventStoreEvent(eventDeserialiser);
            eventStoreEvent.setEventType(entry.get("eventType").toString());
            eventStoreEvent.setData(entry.get("data").toString());
            eventStoreEvent.setEventNumber(Integer.parseInt(entry.get("eventNumber").toString()));
            eventStoreEvent.setPositionEventNumber(Integer.parseInt(entry.get("positionEventNumber").toString()));
            events.add(eventStoreEvent);
        }
        return events;
    }

    public boolean getResult() {
        return this.result;
    }

    private String getLink(JSONObject payload, String linkType) {

        JSONArray links = (JSONArray) payload.get("links");
        String responseURI = "";

        for (Object link1 : links) {
            JSONObject link = (JSONObject) link1;
            if (link.get("relation").toString().equals(linkType)) {
                responseURI = link.get("uri").toString();
            }
        }
        return responseURI;
    }

    /**
     * reads the response from URL endpoint.
     *
     * @param url the URL of the GET request
     * @return the response as an string
     * @throws IOException
     * @throws EventStoreClientUnknownStreamException unable to connect to url
     */
    private String getURL(String url) throws IOException, EventStoreClientUnknownStreamException {

        logger.debug(url);

        int responseCode = 0;
        URL urlObj = new URL(url + "?embed=body");

        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

        con.setRequestMethod("GET");
        if (startLongPoll) {
            con.setRequestProperty("ES-LongPoll", "30");
        }

        con.setRequestProperty("Accept" , this.mimetype );

        responseCode = con.getResponseCode();

        if (responseCode != 200) {
            if (responseCode == 406) {logger.error("\nhttp response 406: unacceptable content type specified: " + this.mimetype);}
            if (responseCode == 404) {logger.error("\nhttp response 404: unable to locate stream: " + url);}
            throw new EventStoreClientUnknownStreamException("http response:" + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine, response = "";

        while ((inputLine = in.readLine()) != null) {
            response = response + inputLine;
        }

        in.close();
        return response;
    }

    private JSONObject extractPayload(String response) {

        JSONParser parser = new JSONParser();
        JSONObject payload = null;

        try {
            payload = (JSONObject) parser.parse(response);

            JSONArray entries = (JSONArray) payload.get("entries");

            startLongPoll = entries != null && entries.size() == 0;

        } catch (ParseException e) {
            logger.error("Input file has JSON parse error: " + e.getPosition() + " "
                    + e.toString());
        }
        return payload;
    }
}
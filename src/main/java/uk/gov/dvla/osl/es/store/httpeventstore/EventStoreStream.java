package uk.gov.dvla.osl.es.store.httpeventstore;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import uk.gov.dvla.osl.es.api.EventStoreEvent;
import uk.gov.dvla.osl.es.exception.EventStoreClientTechnicalException;
import uk.gov.dvla.osl.es.exception.EventStoreClientUnknownStreamException;

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
    private JSONObject payload;
    private boolean result;
    private List<EventStoreEvent> data;
    private String error;
    private String mimetype;
    private boolean embedData;

    public EventStoreStream (String stream, boolean embedData, boolean keepAlive) throws IOException {
        this.stream = stream;
        this.keepAlive = keepAlive;
        this.mimetype = "application/vnd.eventstore.atom+json";
        this.embedData = embedData;
    }

    public Observable<EventStoreEvent> all() {
        return Observable.create(subscribeFunction);
    }

    Observable.OnSubscribe<EventStoreEvent> subscribeFunction = (s) -> {

        Subscriber subscriber = (Subscriber)s;

        try {
            processData(subscriber);
        } catch (Exception e) {
            subscriber.onError(e);
        }
    };

    private void processData(Subscriber subscriber) throws Exception {

        getHeadOfStream();

        if (!getResult()) {
            logger.error("Get Head of Stream failed" + getErrorMessage() );
            throw new EventStoreClientTechnicalException(getErrorMessage());
        }

        processEvents(subscriber);

        String previous;

        do {
            previous = getPreviousLink();

            do {
                gotoPrevious(previous);

                while (getPreviousLink().equals("")) {

                    gotoPrevious(previous);

                    if (keepAlive) {
                        while (getPreviousLink().equals("")) {
                            gotoPrevious(previous);
                        }
                    }
                    else {
                        subscriber.onCompleted();
                        return;
                    }
                }

                processEvents(subscriber);

            } while (!dataExists());

        } while (!previous.equals(""));
    }

    private void processEvents(Subscriber subscriber) throws Exception {

        this.data = getTheData(this.payload);

        if (dataExists()) {
            for (EventStoreEvent eventStoreEvent : getData()) {
                subscriber.onNext(eventStoreEvent);
            }
        }
    }

    public void getHeadOfStream () throws IOException {

        // Start at the head of the stream
        //
        this.payload = extract(getURL(stream, true));

        if (payload.containsKey("headOfStream")) {

            if (payload.get("headOfStream").equals(true)) {
                String last = getLink(payload,"last");
                if (!last.equals("")) {
                    this.payload = extract(getURL(last, true));
                    result = true;
                }
                else {
                    result = true;  // This one is the last one
                }
            }
            else {
                result = false;
                error = stream + " - is not head of stream";
            }
        }
        else {
            result = false;
            error = stream + " - is not head of stream";

        }
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

    public void gotoPrevious(String previous) throws IOException {
        this.payload = extract(getURL(previous, true));
    }

    public String getPreviousLink() {
        return getLink(this.payload,"previous");
    }

    private List<EventStoreEvent> getTheData(JSONObject payload) throws IOException, ClassNotFoundException {

        JSONArray entries = (JSONArray) payload.get("entries");

        List<EventStoreEvent> events = new ArrayList<>();

        String entryResponse;

        for (int i = entries.size() - 1; i > -1; i--) {
            JSONObject entry = (JSONObject) entries.get(i);

            if (embedData) {
                EventStoreEvent eventStoreEvent = new EventStoreEvent();
                eventStoreEvent.setEventType(entry.get("eventType").toString());
                eventStoreEvent.setData(entry.get("data").toString());
                eventStoreEvent.setEventNumber(Integer.parseInt(entry.get("eventNumber").toString()));
                events.add(eventStoreEvent);
            } else {
                JSONArray entryLinks = (JSONArray) entry.get("links");

                for (Object entryLink1 : entryLinks) {
                    JSONObject entryLink = (JSONObject) entryLink1;
                    if (entryLink.get("relation").equals("alternate")) {

                        entryResponse = getURL(entryLink.get("uri").toString(), false);

                        EventStoreEvent eventStoreEvent = new EventStoreEvent();

                        JSONObject entryPayload = extract(entryResponse);

                        String content = entryPayload.get("content").toString();

                        JSONObject cnt = extract(content);

                        eventStoreEvent.setData(cnt.get("data").toString());
                        eventStoreEvent.setEventType(cnt.get("eventType").toString());
                        eventStoreEvent.setEventNumber(Integer.parseInt(cnt.get("eventNumber").toString()));

                        events.add(eventStoreEvent);
                    }
                }
            }
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
     * @param longPoll - NOT USED (need to refactor)
     * @return the response as an string
     * @throws IOException
     * @throws EventStoreClientUnknownStreamException unable to connect to url
     */
    private String getURL(String url, boolean longPoll) throws IOException, EventStoreClientUnknownStreamException {

        int responseCode = 0;
        URL urlObj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

        con.setRequestMethod("GET");
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

    private JSONObject extract(String response) {
        JSONParser parser = new JSONParser();
        JSONObject payload = null;

        try {
            payload = (JSONObject) parser.parse(response);
        } catch (ParseException e) {
            logger.error("Input file has JSON parse error: " + e.getPosition() + " "
                    + e.toString());
        }
        return payload;
    }
}
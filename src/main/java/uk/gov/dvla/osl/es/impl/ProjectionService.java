package uk.gov.dvla.osl.es.impl;

import uk.gov.dvla.osl.es.api.Event;

import java.lang.reflect.InvocationTargetException;

@Deprecated
public class ProjectionService {
    private Object dataStore;
    private Class<?> aggregateType;
    private Object aggregate;
    private boolean eventsProcessed = false;

    public ProjectionService(Object dataStore, Class dataStoreType, Class aggregateType) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.dataStore = dataStore;
        this.aggregateType = aggregateType;
        this.aggregate = aggregateType.getConstructor(dataStoreType).newInstance(dataStore);
    }

    public void handle(Event event) throws Exception {
        ReflectionUtil.invokeHandleMethod(aggregate, event);
        eventsProcessed = true;
    }

    public void save(int version) {
        if (eventsProcessed) ReflectionUtil.invokeSaveMethod(aggregate, version);
    }
}

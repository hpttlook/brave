package com.github.kristofa.brave;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.twitter.zipkin.gen.Annotation;
import com.twitter.zipkin.gen.Endpoint;
import com.twitter.zipkin.gen.Span;
import com.twitter.zipkin.gen.zipkinCoreConstants;

public class ServerTracerImplTest {

    private final static long CURRENT_TIME_MICROSECONDS = System.currentTimeMillis() * 1000;
    private final static String ANNOTATION_NAME = "Annotation name";
    private final static int DURATION = 13;
    private final static long TRACE_ID = 1l;
    private final static long SPAN_ID = 2l;
    private final static Long PARENT_SPANID = 3l;
    private final static String SPAN_NAME = "span name";

    private ServerTracerImpl serverTracer;
    private ServerSpanState mockServerSpanState;
    private SpanCollector mockSpanCollector;
    private Span mockSpan;
    private Endpoint mockEndPoint;

    @Before
    public void setup() {

        mockServerSpanState = mock(ServerSpanState.class);
        mockSpanCollector = mock(SpanCollector.class);
        mockSpan = mock(Span.class);
        mockEndPoint = new Endpoint();
        serverTracer = new ServerTracerImpl(mockServerSpanState, mockSpanCollector) {

            @Override
            long currentTimeMicroseconds() {
                return CURRENT_TIME_MICROSECONDS;
            }
        };
        when(mockServerSpanState.shouldTrace()).thenReturn(true);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullState() {
        new ServerTracerImpl(null, mockSpanCollector);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCollector() {
        new ServerTracerImpl(mockServerSpanState, null);
    }

    @Test
    public void testClearCurrentSpan() {
        serverTracer.clearCurrentSpan();
        verify(mockServerSpanState).setCurrentServerSpan(null);
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSetSpan() {

        serverTracer.setSpan(TRACE_ID, SPAN_ID, PARENT_SPANID, SPAN_NAME);

        final Span expectedSpan = new Span();
        expectedSpan.setTrace_id(TRACE_ID);
        expectedSpan.setId(SPAN_ID);
        expectedSpan.setParent_id(PARENT_SPANID);
        expectedSpan.setName(SPAN_NAME);
        verify(mockServerSpanState).setCurrentServerSpan(expectedSpan);
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSetShouldTrace() {

        serverTracer.setShouldTrace(false);
        verify(mockServerSpanState).setTracing(false);
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSubmitAnnotationStringLongShouldNotTrace() {
        when(mockServerSpanState.shouldTrace()).thenReturn(false);
        serverTracer.submitAnnotation(ANNOTATION_NAME, DURATION);
        verify(mockServerSpanState).shouldTrace();
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSubmitAnnotationStringLongNoServerSpan() {

        when(mockServerSpanState.getCurrentServerSpan()).thenReturn(null);
        serverTracer.submitAnnotation(ANNOTATION_NAME, DURATION);
        verify(mockServerSpanState).shouldTrace();
        verify(mockServerSpanState).getCurrentServerSpan();
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSubmitAnnotationStringLongNoEndPoint() {

        when(mockServerSpanState.getCurrentServerSpan()).thenReturn(mockSpan);
        serverTracer.submitAnnotation(ANNOTATION_NAME, DURATION);
        verify(mockServerSpanState).shouldTrace();
        verify(mockServerSpanState).getCurrentServerSpan();
        verify(mockServerSpanState).getEndPoint();
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSubmitAnnotationStringShouldNotTrace() {

        when(mockServerSpanState.shouldTrace()).thenReturn(false);
        serverTracer.submitAnnotation(ANNOTATION_NAME);
        verify(mockServerSpanState).shouldTrace();
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSubmitAnnotationString() {
        when(mockServerSpanState.getCurrentServerSpan()).thenReturn(mockSpan);
        when(mockServerSpanState.getEndPoint()).thenReturn(mockEndPoint);
        serverTracer.submitAnnotation(ANNOTATION_NAME);
        verify(mockServerSpanState).shouldTrace();
        verify(mockServerSpanState).getCurrentServerSpan();
        verify(mockServerSpanState).getEndPoint();

        final Annotation expectedAnnotation = new Annotation();
        expectedAnnotation.setHost(mockEndPoint);
        expectedAnnotation.setTimestamp(CURRENT_TIME_MICROSECONDS);
        expectedAnnotation.setValue(ANNOTATION_NAME);

        verify(mockSpan).addToAnnotations(expectedAnnotation);
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSetServerReceivedShouldNotTrace() {

        when(mockServerSpanState.shouldTrace()).thenReturn(false);
        serverTracer.setServerReceived();
        verify(mockServerSpanState).shouldTrace();
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSetServerReceived() {
        when(mockServerSpanState.getCurrentServerSpan()).thenReturn(mockSpan);
        when(mockServerSpanState.getEndPoint()).thenReturn(mockEndPoint);
        serverTracer.setServerReceived();
        verify(mockServerSpanState).shouldTrace();
        verify(mockServerSpanState).getCurrentServerSpan();
        verify(mockServerSpanState).getEndPoint();

        final Annotation expectedAnnotation = new Annotation();
        expectedAnnotation.setHost(mockEndPoint);
        expectedAnnotation.setTimestamp(CURRENT_TIME_MICROSECONDS);
        expectedAnnotation.setValue(zipkinCoreConstants.SERVER_RECV);
        verify(mockSpan).addToAnnotations(expectedAnnotation);
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSetServerSendShouldNotTrace() {

        when(mockServerSpanState.shouldTrace()).thenReturn(false);
        serverTracer.setServerSend();
        verify(mockServerSpanState).shouldTrace();
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

    @Test
    public void testSetServerSend() {
        when(mockServerSpanState.getCurrentServerSpan()).thenReturn(mockSpan);
        when(mockServerSpanState.getEndPoint()).thenReturn(mockEndPoint);
        serverTracer.setServerSend();
        verify(mockServerSpanState).shouldTrace();
        verify(mockServerSpanState).getCurrentServerSpan();
        verify(mockServerSpanState).getEndPoint();

        final Annotation expectedAnnotation = new Annotation();
        expectedAnnotation.setHost(mockEndPoint);
        expectedAnnotation.setTimestamp(CURRENT_TIME_MICROSECONDS);
        expectedAnnotation.setValue(zipkinCoreConstants.SERVER_SEND);
        verify(mockSpan).addToAnnotations(expectedAnnotation);
        verify(mockSpanCollector).collect(mockSpan);
        verify(mockServerSpanState).setCurrentServerSpan(null);
        verifyNoMoreInteractions(mockServerSpanState, mockSpanCollector);
    }

}

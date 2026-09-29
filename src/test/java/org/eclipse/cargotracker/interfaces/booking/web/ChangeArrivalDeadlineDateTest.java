package org.eclipse.cargotracker.interfaces.booking.web;

import org.eclipse.cargotracker.interfaces.booking.facade.BookingServiceFacade;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.CargoRoute;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.Location;
import org.eclipse.cargotracker.interfaces.booking.facade.dto.RouteCandidate;
import org.junit.Test;
import org.primefaces.PrimeFaces;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class ChangeArrivalDeadlineDateTest {

    @Test
    public void loadUsesTrackingIdAndConvertsArrivalDeadline() throws Exception {
        RecordingBookingServiceFacade facade = new RecordingBookingServiceFacade(
                cargoWithArrivalDeadline("03/15/2024 12:00 AM UTC"));
        ChangeArrivalDeadlineDate editor = editorWithFacade(facade);
        editor.setTrackingId("ABC123");

        editor.load();

        assertEquals("ABC123", facade.loadedTrackingId);
        assertSame(facade.cargo, editor.getCargo());
        assertEquals("03/15/2024",
                new SimpleDateFormat("MM/dd/yyyy").format(editor.getArrivalDeadlineDate()));
    }

    @Test
    public void loadSurfacesMalformedArrivalDeadline() throws Exception {
        RecordingBookingServiceFacade facade = new RecordingBookingServiceFacade(
                cargoWithArrivalDeadline("not a deadline"));
        ChangeArrivalDeadlineDate editor = editorWithFacade(facade);
        editor.setTrackingId("ABC123");

        try {
            editor.load();
            fail("Expected malformed arrival deadline to fail");
        } catch (IllegalStateException e) {
            assertEquals("Unable to parse arrival deadline for cargo ABC123",
                    e.getMessage());
            assertNull(editor.getArrivalDeadlineDate());
        }
    }

    @Test
    public void changeArrivalDeadlineDelegatesSelectedDate() throws Exception {
        RecordingBookingServiceFacade facade = new RecordingBookingServiceFacade(null);
        RuntimeException failure = new RuntimeException("stop before dialog close");
        facade.changeDeadlineFailure = failure;
        ChangeArrivalDeadlineDate editor = editorWithFacade(facade);
        Date selectedDate = new Date();
        editor.setTrackingId("ABC123");
        editor.setArrivalDeadlineDate(selectedDate);

        try {
            editor.changeArrivalDeadline();
            fail("Expected facade failure to remain visible");
        } catch (RuntimeException e) {
            assertSame(failure, e);
        }

        assertEquals(1, facade.changeDeadlineCalls);
        assertEquals("ABC123", facade.changedTrackingId);
        assertSame(selectedDate, facade.changedArrivalDeadline);
    }

    @Test
    public void changeArrivalDeadlineClosesAfterFacadeSucceeds() throws Exception {
        RecordingBookingServiceFacade facade = new RecordingBookingServiceFacade(null);
        ChangeArrivalDeadlineDate editor = editorWithFacade(facade);
        editor.setTrackingId("ABC123");
        editor.setArrivalDeadlineDate(new Date());

        PrimeFaces previousPrimeFaces = PrimeFaces.current();
        PrimeFaces.setCurrent(new RecordingPrimeFaces(facade.events));
        try {
            editor.changeArrivalDeadline();
        } finally {
            PrimeFaces.setCurrent(previousPrimeFaces);
        }

        assertEquals(Arrays.asList("facade", "close:DONE"), facade.events);
    }

    @Test
    public void changeArrivalDeadlineRejectsNull() throws Exception {
        RecordingBookingServiceFacade facade = new RecordingBookingServiceFacade(null);
        ChangeArrivalDeadlineDate editor = editorWithFacade(facade);
        editor.setTrackingId("ABC123");

        try {
            editor.changeArrivalDeadline();
            fail("Expected null arrival deadline to be rejected");
        } catch (IllegalArgumentException e) {
            assertEquals("Arrival deadline date must not be null", e.getMessage());
        }

        assertEquals(0, facade.changeDeadlineCalls);
    }

    private ChangeArrivalDeadlineDate editorWithFacade(
            RecordingBookingServiceFacade facade) throws Exception {
        ChangeArrivalDeadlineDate editor = new ChangeArrivalDeadlineDate();
        Field field = ChangeArrivalDeadlineDate.class
                .getDeclaredField("bookingServiceFacade");
        field.setAccessible(true);
        field.set(editor, facade);
        return editor;
    }

    private CargoRoute cargoWithArrivalDeadline(final String arrivalDeadline) {
        return new CargoRoute("ABC123", "origin", "destination", new Date(0),
                false, false, "location", "status") {
            @Override
            public String getArrivalDeadline() {
                return arrivalDeadline;
            }
        };
    }

    private static class RecordingBookingServiceFacade implements BookingServiceFacade {

        private final CargoRoute cargo;
        private String loadedTrackingId;
        private int changeDeadlineCalls;
        private String changedTrackingId;
        private Date changedArrivalDeadline;
        private RuntimeException changeDeadlineFailure;
        private final List<String> events = new ArrayList<>();

        private RecordingBookingServiceFacade(CargoRoute cargo) {
            this.cargo = cargo;
        }

        @Override
        public String bookNewCargo(String origin, String destination, Date arrivalDeadline) {
            return null;
        }

        @Override
        public CargoRoute loadCargoForRouting(String trackingId) {
            loadedTrackingId = trackingId;
            return cargo;
        }

        @Override
        public void assignCargoToRoute(String trackingId, RouteCandidate route) {
        }

        @Override
        public void changeDestination(String trackingId, String destinationUnLocode) {
        }

        @Override
        public void changeDeadline(String trackingId, Date arrivalDeadline) {
            changeDeadlineCalls++;
            changedTrackingId = trackingId;
            changedArrivalDeadline = arrivalDeadline;
            events.add("facade");
            if (changeDeadlineFailure != null) {
                throw changeDeadlineFailure;
            }
        }

        @Override
        public List<RouteCandidate> requestPossibleRoutesForCargo(String trackingId) {
            return new ArrayList<>();
        }

        @Override
        public List<Location> listShippingLocations() {
            return new ArrayList<>();
        }

        @Override
        public List<CargoRoute> listAllCargos() {
            return new ArrayList<>();
        }
    }

    private static class RecordingPrimeFaces extends PrimeFaces {

        private final List<String> events;

        private RecordingPrimeFaces(List<String> events) {
            this.events = events;
        }

        @Override
        public Dialog dialog() {
            return new Dialog() {
                @Override
                public void closeDynamic(Object outcome) {
                    events.add("close:" + outcome);
                }
            };
        }
    }
}

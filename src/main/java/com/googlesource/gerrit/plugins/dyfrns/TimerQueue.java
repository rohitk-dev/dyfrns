package com.googlesource.gerrit.plugins.dyfrns;

import com.google.gerrit.common.errors.EmailException;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.ApprovalsUtil;
import com.google.gerrit.server.account.AccountsCollection;
import com.google.gerrit.server.change.ChangesCollection;
import com.google.gerrit.server.mail.Address;
import com.google.gerrit.server.mail.EmailHeader;
import com.google.gerrit.server.mail.EmailSender;
import com.google.gwtorm.server.SchemaFactory;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TimerQueue {
    private static final Logger log = LoggerFactory.getLogger(TimerQueue.class);

    private PriorityQueue<TimerEvent> queue;
    private HashMap<String, TimerEvent> map;
    private Timer timer;
    private boolean timerRunning;

    public int version;

    private final SchemaFactory<ReviewDb> schemaFactory;
    private final ChangesCollection changes;
    private final AccountsCollection accountsCollection;
    private final ApprovalsUtil approvalsUtil;
    private final EmailSender emailSender;

    public interface Factory {
        TimerQueue create();
    }

    @Inject
    TimerQueue(
            SchemaFactory<ReviewDb> schemaFactory,
            ChangesCollection changes,
            AccountsCollection accountsCollection,
            ApprovalsUtil approvalsUtil,
            EmailSender emailSender) {

        Comparator<TimerEvent> timerEventComparator = new Comparator<TimerEvent>() {
            @Override
            public int compare(TimerEvent o1, TimerEvent o2) {
                return o1.compareTo(o2);
            }
        };

        queue = new PriorityQueue<>(10, timerEventComparator);
        map = new HashMap<>(10);
        timer = new Timer();
        version = (int)(1000 * Math.random());
        this.schemaFactory = schemaFactory;
        this.changes = changes;
        this.accountsCollection = accountsCollection;
        this.approvalsUtil = approvalsUtil;
        this.emailSender = emailSender;
    }

    private void startNewTimer(final TimerEvent event) {
        log.info("Starting a new timer");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                event.run();
            }
        };
        timer.schedule(task, event.getExpire());
        timerRunning = true;
    }

    public synchronized void add(TimerEvent event) throws Exception {
        log.info("Adding " + event);
        printQueue();

        if (map.containsKey(event.getId())) {
            throw new Exception("Duplicate element");
        }

        event.setTimerQueue(this);

        queue.add(event);
        map.put(event.getId(), event);

        if (queue.peek().equals(event)) {
            if (timerRunning) {
                timer.cancel();
                timerRunning = false;
                timer = new Timer();
            }
            startNewTimer(event);
        }
    }

    public synchronized void cancel(String id) throws Exception {
        log.info("Canceling event with ID " + id);
        printQueue();

        if (!map.containsKey(id)) {
            throw new Exception("No such element");
        }

        boolean removingFirstEntry = queue.peek().getId().equals(id);

        queue.remove(new TimerEvent(id, null, null));
        map.remove(id);

        if (removingFirstEntry) {
            if (timerRunning) {
                timer.cancel();
                timerRunning = false;
                timer = new Timer();
            }
            if (!queue.isEmpty()) {
                startNewTimer(queue.peek());
            }
        }
    }

    public synchronized void reschedule(TimerEvent event) throws Exception {
        log.info("Rescheduling " + event);
        //printQueue();

        if (!map.containsKey(event.getId())) {
            // this event has been canceled, so we are not going to reschedule it
            log.info("Event has been canceled");
            return;
        }

        sendReminderEmail(event);

        queue.remove(event);
        map.remove(event.getId());

        add(event);
        log.info("readded the event");
    }

    public synchronized void addReviewer(String id, String email, String name) throws Exception {
        if (map.containsKey(id)) {
            TimerEvent timerEvent = map.get(id);
            timerEvent.addReviewer(email, name);
        } else {
            TimerEvent timerEvent = new TimerEvent(id, email, name);
            add(timerEvent);
        }
    }

    public synchronized void removeReviewer(String id, String email) throws Exception {
        if (map.containsKey(id)) {
            TimerEvent timerEvent = map.get(id);
            timerEvent.removeReviewer(email);
        } else {
            log.warn("No entry with id " + id);
        }
    }

    private void getReviewers() {
        /*
        ReviewDb reviewDb = null;

        try {
            reviewDb = schemaFactory.open();
            Change.Id changeId = new Change.Id(Integer.parseInt(event.getId()));
            final Change change = reviewDb.changes().get(changeId);

            if (change == null) {
                log.warn("Change " + changeId.get() + " not found.");
                return;
            }

            boolean stillReviewer = false;


            ChangeResource changeResource = changes.parse(change.getId());
            ImmutableSet<Account.Id> reviewerSet = changeResource.getNotes().getReviewers().get(ReviewerState.REVIEWER);


            //approvalsUtil.getReviewers(reviewDb, change.)
            //ImmutableSet<Account.Id> reviewerSet = changeResource.getNotes() .getReviewers().get(ReviewerState.REVIEWER);
            log.info("There are " + reviewerSet.size() + " reviewers");
            Account.Id[] ids = reviewerSet.toArray(new Account.Id[reviewerSet.size()]);
            for (Account.Id id : ids) {
                if (accountsCollection.parseId(Integer.toString(id.get())).getEmailAddresses().contains(event.getEmail())) {
                    stillReviewer = true;
                }
            }

            queue.remove(event);

            if (!stillReviewer) {
                log.warn(event.getEmail() + " is no longer a reviewer.");
                return;
            }
        } catch (Exception e) {
            log.info("Got an error:", e);
            return;
        } finally {
            if (reviewDb != null) {
                try {
                    reviewDb.close();
                } catch (Exception e) {
                    log.info("Got another error: ", e);
                }
            }
        }
        */

    }

    private void sendReminderEmail(TimerEvent event) throws EmailException {
        Address from = new Address("Gerrit", "dyfrns@gerrit.com");
        Map<String, EmailHeader> headers = new HashMap<>();
        headers.put("Date", new EmailHeader.Date(new Date()));
        headers.put("From", new EmailHeader.AddressList(from));
        headers.put("To", new EmailHeader.AddressList());
        headers.put("CC", new EmailHeader.AddressList());
        headers.put("Message-ID", new EmailHeader.String(""));
        headers.put("Reply-To", new EmailHeader.String(from.getEmail()));

        ArrayList<TimerEvent.Info> infos = event.getInfos();
        log.info("Sending emails to " + infos);
        for (TimerEvent.Info info : infos) {
            Collection<Address> to = new LinkedList<>();
            to.add(new Address(info.name, info.email));
            headers.put(
                    "Subject",
                    new EmailHeader.String(
                            "(Un)friendly reminder to " + info.name + ": do you review!"));
            emailSender.send(from, to, headers, "This is reminder #" + info.count);
        }
    }

    private void printQueue() {
        StringBuilder stringBuilder = new StringBuilder("\n========================\n");

        Iterator<TimerEvent> iterator = queue.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (!first) {
                stringBuilder.append("----------------------\n");
            }
            stringBuilder.append("- ");
            stringBuilder.append(iterator.next());
            stringBuilder.append("\n");
            first = false;
        }

        stringBuilder.append("========================");
        log.info(stringBuilder.toString());
    }
}

package com.ittm.service;

import com.ittm.model.Note;
import com.ittm.model.Role;
import com.ittm.model.Ticket;
import com.ittm.model.TicketHistoryEntry;
import com.ittm.model.TicketStatus;
import com.ittm.model.User;
import com.ittm.repository.DataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TicketService {
    private static final EnumSet<TicketStatus> ACTIVE_STATUSES = EnumSet.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.AWAITING_RESPONSE);
    private final DataStore dataStore;

    public TicketService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Ticket createTicket(User requester, String title, String description, String category) {
        Ticket ticket = dataStore.createTicket(requester, title, description, category, LocalDateTime.now());
        logHistory(ticket, "Ticket raised", requester.getName());
        assignAgent(ticket, "System assignment");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setUpdatedAt(LocalDateTime.now());
        dataStore.updateTicket(ticket);
        return ticket;
    }

    private void logHistory(Ticket ticket, String action, String actor) {
        TicketHistoryEntry entry = new TicketHistoryEntry(LocalDateTime.now(), action, actor);
        ticket.getHistory().add(entry);
        dataStore.addHistory(ticket.getId(), entry);
    }

    private long activeLoad(User agent) {
        return dataStore.getTickets().stream()
                .filter(t -> t.getAssignedAgent() != null && t.getAssignedAgent().getId() == agent.getId())
                .filter(t -> ACTIVE_STATUSES.contains(t.getStatus()))
                .count();
    }

    public void assignAgent(Ticket ticket, String actor) {
        List<User> agents = dataStore.getAllUsers().stream()
                .filter(u -> u.getRole() == Role.AGENT)
                .sorted(Comparator.comparingInt(User::getId))
                .collect(Collectors.toList());
        Optional<User> chosen = agents.stream()
                .min(Comparator.comparingLong(this::activeLoad).thenComparingInt(User::getId));
        chosen.ifPresent(agent -> {
            ticket.setAssignedAgent(agent);
            logHistory(ticket, "Assigned to agent " + agent.getName(), actor);
            dataStore.updateTicket(ticket);
        });
    }

    public List<Ticket> viewTicketsForUser(User user) {
        if (user.getRole() == Role.ADMIN) {
            return new ArrayList<>(dataStore.getTickets());
        }
        if (user.getRole() == Role.AGENT) {
            return dataStore.getTickets().stream()
                    .filter(t -> t.getAssignedAgent() != null && t.getAssignedAgent().getId() == user.getId())
                    .collect(Collectors.toList());
        }
        return dataStore.getTickets().stream()
                .filter(t -> t.getRequester().getId() == user.getId())
                .collect(Collectors.toList());
    }

    public void updateDescription(User user, int ticketId, String newDescription) {
        Ticket ticket = findTicket(ticketId);
        if (ticket.getRequester().getId() != user.getId()) {
            throw new IllegalStateException("Only requester can edit description");
        }
        if (!(ticket.getStatus() == TicketStatus.OPEN || ticket.getStatus() == TicketStatus.REOPENED)) {
            throw new IllegalStateException("Description editable only when Open or Reopened");
        }
        ticket.setDescription(newDescription);
        ticket.setUpdatedAt(LocalDateTime.now());
        logHistory(ticket, "Description updated", user.getName());
        dataStore.updateTicket(ticket);
    }

    public void updateStatus(User actor, int ticketId, TicketStatus newStatus) {
        Ticket ticket = findTicket(ticketId);
        if (actor.getRole() == Role.USER && ticket.getRequester().getId() != actor.getId()) {
            throw new IllegalStateException("User cannot change others' tickets");
        }
        if (actor.getRole() == Role.USER && newStatus == TicketStatus.AWAITING_RESPONSE) {
            throw new IllegalStateException("User cannot move to awaiting");
        }
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        logHistory(ticket, "Status changed to " + newStatus, actor.getName());
        dataStore.updateTicket(ticket);
    }

    public void closeOrAwait(User actor, int ticketId, boolean confirmClose, String awaitMessage) {
        Ticket ticket = findTicket(ticketId);
        if (confirmClose) {
            updateStatus(actor, ticketId, TicketStatus.RESOLVED);
        } else {
            ticket.setStatus(TicketStatus.AWAITING_RESPONSE);
            ticket.setUpdatedAt(LocalDateTime.now());
            logHistory(ticket, "Moved to awaiting response: " + awaitMessage, actor.getName());
            dataStore.updateTicket(ticket);
        }
    }

    public void reopen(User actor, int ticketId, String reason) {
        Ticket ticket = findTicket(ticketId);
        ticket.setStatus(TicketStatus.REOPENED);
        ticket.setUpdatedAt(LocalDateTime.now());
        logHistory(ticket, "Reopened: " + reason, actor.getName());
        dataStore.updateTicket(ticket);
    }

    public void addNote(User actor, int ticketId, String message) {
        if (actor.getRole() == Role.USER && ticketId < 0) {
            throw new IllegalStateException("Invalid ticket");
        }
        Ticket ticket = findTicket(ticketId);
        if (actor.getRole() == Role.USER || actor.getRole() == Role.AGENT || actor.getRole() == Role.ADMIN) {
            ticket.getNotes().add(new Note(actor.getId(), actor.getName(), message, LocalDateTime.now()));
            logHistory(ticket, "Note added", actor.getName());
            Note latest = ticket.getNotes().get(ticket.getNotes().size() - 1);
            dataStore.addNote(ticketId, latest);
            dataStore.updateTicket(ticket);
        }
    }

    public void addRating(User user, int ticketId, int rating) {
        Ticket ticket = findTicket(ticketId);
        if (ticket.getRequester().getId() != user.getId()) {
            throw new IllegalStateException("Only requester can rate");
        }
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new IllegalStateException("Rating allowed after resolution");
        }
        ticket.setRating(rating);
        if (rating < 2 && ticket.getAssignedAgent() != null) {
            ticket.setAgentFlagged(true);
        }
        logHistory(ticket, "Rated with score " + rating, user.getName());
        dataStore.updateTicket(ticket);
    }

    public void reassign(User admin, int ticketId, User newAgent, String reason) {
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only admin can reassign");
        }
        Ticket ticket = findTicket(ticketId);
        ticket.setAssignedAgent(newAgent);
        ticket.setUpdatedAt(LocalDateTime.now());
        logHistory(ticket, "Reassigned to " + newAgent.getName() + " reason: " + reason, admin.getName());
        dataStore.updateTicket(ticket);
    }

    public List<Ticket> search(TicketStatus status, LocalDate from, LocalDate to) {
        return dataStore.getTickets().stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> {
                    LocalDate created = t.getCreatedAt().toLocalDate();
                    boolean afterFrom = from == null || !created.isBefore(from);
                    boolean beforeTo = to == null || !created.isAfter(to);
                    return afterFrom && beforeTo;
                })
                .collect(Collectors.toList());
    }

    public Map<String, String> monthlyReport() {
        Map<String, Long> resolved = dataStore.getTickets().stream()
                .filter(t -> t.getStatus() == TicketStatus.RESOLVED)
                .collect(Collectors.groupingBy(t -> t.getUpdatedAt().getYear() + "-" + t.getUpdatedAt().getMonthValue(), Collectors.counting()));
        Map<String, Long> reopened = dataStore.getTickets().stream()
                .filter(t -> t.getStatus() == TicketStatus.REOPENED)
                .collect(Collectors.groupingBy(t -> t.getUpdatedAt().getYear() + "-" + t.getUpdatedAt().getMonthValue(), Collectors.counting()));
        return Stream.concat(resolved.keySet().stream(), reopened.keySet().stream())
                .distinct()
                .collect(Collectors.toMap(month -> month,
                        month -> "resolved=" + resolved.getOrDefault(month, 0L) + ", reopened=" + reopened.getOrDefault(month, 0L)));
    }

    public List<Ticket> escalations(LocalDateTime now) {
        return dataStore.getTickets().stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED)
                .filter(t -> t.getCreatedAt().isBefore(now.minusHours(24)))
                .collect(Collectors.toList());
    }

    public Ticket findTicket(int id) {
        return dataStore.findTicket(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }
}

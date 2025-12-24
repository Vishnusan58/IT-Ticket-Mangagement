package com.ittm.ui;

import com.ittm.model.ChangeRequest;
import com.ittm.model.Role;
import com.ittm.model.Ticket;
import com.ittm.model.TicketStatus;
import com.ittm.model.User;
import com.ittm.repository.DataStore;
import com.ittm.service.ChangeRequestService;
import com.ittm.service.TicketService;
import com.ittm.service.UserService;
import com.ittm.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleApp {
    private final DataStore dataStore = new DataStore();
    private final UserService userService = new UserService(dataStore);
    private final TicketService ticketService = new TicketService(dataStore);
    private final ChangeRequestService changeRequestService = new ChangeRequestService(dataStore);
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        ConsoleApp app = new ConsoleApp();
        app.seed();
        app.run();
    }

    private void seed() {
        userService.createUser(1, "Alice", Role.USER);
        userService.createUser(2, "Bob", Role.USER);
        userService.createUser(100, "AgentOne", Role.AGENT);
        userService.createUser(101, "AgentTwo", Role.AGENT);
        userService.createUser(900, "Admin", Role.ADMIN);
    }

    private void run() {
        System.out.println("Welcome to IT Ticket Management Console");
        boolean exit = false;
        while (!exit) {
            System.out.println("Select user id to login (1-Alice, 2-Bob, 100-AgentOne, 101-AgentTwo, 900-Admin, 0-exit):");
            int id = Integer.parseInt(scanner.nextLine());
            if (id == 0) {
                exit = true;
                continue;
            }
            Optional<User> user = userService.findUser(id);
            if (user.isPresent()) {
                handleUser(user.get());
            } else {
                System.out.println("Unknown user");
            }
        }
    }

    private void handleUser(User user) {
        switch (user.getRole()) {
            case USER -> userMenu(user);
            case AGENT -> agentMenu(user);
            case ADMIN -> adminMenu(user);
            default -> System.out.println("Unsupported role");
        }
    }

    private void userMenu(User user) {
        boolean back = false;
        while (!back) {
            System.out.println("\nUser Menu: 1-Create Ticket 2-View My Tickets 3-Edit Description 4-Add Note 5-Close/Await 6-Reopen 7-Rate 8-Raise Change 9-Logout");
            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> createTicketFlow(user);
                    case "2" -> listTickets(ticketService.viewTicketsForUser(user));
                    case "3" -> editDescriptionFlow(user);
                    case "4" -> addNoteFlow(user);
                    case "5" -> closeAwaitFlow(user);
                    case "6" -> reopenFlow(user);
                    case "7" -> ratingFlow(user);
                    case "8" -> raiseChangeFlow(user);
                    case "9" -> back = true;
                    default -> System.out.println("Invalid option");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void agentMenu(User agent) {
        boolean back = false;
        while (!back) {
            System.out.println("\nAgent Menu: 1-View Assigned 2-Update Status 3-Move to Awaiting 4-Add Note 5-Implement Change 6-Logout");
            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> listTickets(ticketService.viewTicketsForUser(agent));
                    case "2" -> updateStatusFlow(agent);
                    case "3" -> moveAwaitingFlow(agent);
                    case "4" -> addNoteFlow(agent);
                    case "5" -> implementChangeFlow(agent);
                    case "6" -> back = true;
                    default -> System.out.println("Invalid option");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void adminMenu(User admin) {
        boolean back = false;
        while (!back) {
            System.out.println("\nAdmin Menu: 1-View All Tickets 2-Reassign 3-Reports 4-Escalations 5-Approve Change 6-View Agent Ratings 7-Archive Changes 8-Logout");
            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> listTickets(ticketService.viewTicketsForUser(admin));
                    case "2" -> reassignFlow(admin);
                    case "3" -> reportFlow();
                    case "4" -> escalationFlow();
                    case "5" -> approveChangeFlow(admin);
                    case "6" -> viewAgentRatings();
                    case "7" -> archiveChanges();
                    case "8" -> back = true;
                    default -> System.out.println("Invalid option");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void createTicketFlow(User requester) {
        System.out.println("Title:");
        String title = scanner.nextLine();
        System.out.println("Description:");
        String desc = scanner.nextLine();
        System.out.println("Category:");
        String cat = scanner.nextLine();
        Ticket ticket = ticketService.createTicket(requester, title, desc, cat);
        System.out.println("Ticket created with id " + ticket.getId());
    }

    private void editDescriptionFlow(User user) {
        System.out.println("Ticket id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("New description:");
        String desc = scanner.nextLine();
        ticketService.updateDescription(user, id, desc);
    }

    private void addNoteFlow(User actor) {
        System.out.println("Ticket id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Note:");
        String note = scanner.nextLine();
        ticketService.addNote(actor, id, note);
    }

    private void closeAwaitFlow(User user) {
        System.out.println("Ticket id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Close? y/n:");
        boolean close = scanner.nextLine().equalsIgnoreCase("y");
        String message = "";
        if (!close) {
            System.out.println("Awaiting message:");
            message = scanner.nextLine();
        }
        ticketService.closeOrAwait(user, id, close, message);
    }

    private void reopenFlow(User user) {
        System.out.println("Ticket id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Reason:");
        String reason = scanner.nextLine();
        ticketService.reopen(user, id, reason);
    }

    private void ratingFlow(User user) {
        System.out.println("Ticket id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Rating 1-5:");
        int rating = Integer.parseInt(scanner.nextLine());
        ticketService.addRating(user, id, rating);
    }

    private void raiseChangeFlow(User requester) {
        System.out.println("Title:");
        String title = scanner.nextLine();
        System.out.println("Description:");
        String desc = scanner.nextLine();
        System.out.println("Expiry (yyyy-MM-dd):");
        LocalDate expiry = DateTimeUtil.parseDate(scanner.nextLine());
        ChangeRequest cr = changeRequestService.raise(requester, title, desc, expiry);
        System.out.println("Change request raised id " + cr.getId());
    }

    private void updateStatusFlow(User actor) {
        System.out.println("Ticket id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("New status (IN_PROGRESS/AWAITING_RESPONSE/RESOLVED):");
        TicketStatus status = TicketStatus.valueOf(scanner.nextLine());
        ticketService.updateStatus(actor, id, status);
    }

    private void moveAwaitingFlow(User actor) {
        System.out.println("Ticket id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Awaiting message:");
        String msg = scanner.nextLine();
        ticketService.closeOrAwait(actor, id, false, msg);
    }

    private void implementChangeFlow(User agent) {
        System.out.println("Change request id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Implementation note:");
        String note = scanner.nextLine();
        changeRequestService.implement(agent, id, note);
    }

    private void reassignFlow(User admin) {
        System.out.println("Ticket id:");
        int ticketId = Integer.parseInt(scanner.nextLine());
        System.out.println("New agent id:");
        int agentId = Integer.parseInt(scanner.nextLine());
        User agent = userService.findUser(agentId).orElseThrow();
        System.out.println("Reason:");
        String reason = scanner.nextLine();
        ticketService.reassign(admin, ticketId, agent, reason);
    }

    private void reportFlow() {
        Map<String, String> monthly = ticketService.monthlyReport();
        System.out.println("Monthly resolved vs reopened counts: " + monthly);
        System.out.println("Change requests expiring in 15 days: ");
        changeRequestService.expiringWithin(15).forEach(cr -> System.out.println("CR " + cr.getId() + " expires " + cr.getExpiryDate()));
    }

    private void escalationFlow() {
        List<Ticket> escalations = ticketService.escalations(LocalDateTime.now());
        if (escalations.isEmpty()) {
            System.out.println("No escalations pending");
        } else {
            System.out.println("Escalations (>24h unresolved):");
            listTickets(escalations);
        }
    }

    private void approveChangeFlow(User admin) {
        System.out.println("Change request id:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Approve? y/n:");
        boolean approve = scanner.nextLine().equalsIgnoreCase("y");
        changeRequestService.approve(admin, id, approve);
    }

    private void viewAgentRatings() {
        dataStore.getTickets().stream()
                .filter(t -> t.getAssignedAgent() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignedAgent().getName()))
                .forEach((agent, list) -> {
                    long flagged = list.stream().filter(Ticket::isAgentFlagged).count();
                    double avg = list.stream().filter(t -> t.getRating() != null)
                            .mapToInt(Ticket::getRating)
                            .average().orElse(0);
                    System.out.println(agent + " -> avg rating " + String.format("%.2f", avg) + " flagged low ratings: " + flagged);
                });
    }

    private void archiveChanges() {
        changeRequestService.archiveOld(LocalDate.now());
        System.out.println("Archiving completed. Active changes: " + dataStore.getChangeRequests().size());
    }

    private void listTickets(List<Ticket> tickets) {
        tickets.forEach(t -> {
            System.out.println("Ticket " + t.getId() + " [" + t.getStatus() + "] " + t.getTitle() +
                    " assigned:" + (t.getAssignedAgent() != null ? t.getAssignedAgent().getName() : "N/A") +
                    " rating:" + t.getRating());
            System.out.println("  Notes:");
            t.getNotes().forEach(n -> System.out.println("   - " + n));
            System.out.println("  History:");
            t.getHistory().forEach(h -> System.out.println("   * " + h));
        });
    }
}

package com.ittm.repository;

import com.ittm.model.ChangeRequest;
import com.ittm.model.ChangeRequestStatus;
import com.ittm.model.Note;
import com.ittm.model.Role;
import com.ittm.model.Ticket;
import com.ittm.model.TicketHistoryEntry;
import com.ittm.model.TicketStatus;
import com.ittm.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataStore implements AutoCloseable {
    private final Connection connection;

    public DataStore() {
        this("jdbc:sqlite:ittm.db");
    }

    public DataStore(String url) {
        try {
            this.connection = DriverManager.getConnection(url);
            initSchema();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialize database", ex);
        }
    }

    private void initSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT, role TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS tickets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, requester_id INTEGER, assigned_agent_id INTEGER, category TEXT, " +
                    "title TEXT, description TEXT, status TEXT, created_at TEXT, updated_at TEXT, rating INTEGER, agent_flagged INTEGER)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS notes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, ticket_id INTEGER, author_id INTEGER, author_name TEXT, message TEXT, created_at TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS ticket_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, ticket_id INTEGER, timestamp TEXT, action TEXT, performed_by TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS change_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, requester_id INTEGER, title TEXT, description TEXT, status TEXT, " +
                    "expiry_date TEXT, archived INTEGER, implementation_note TEXT, created_at TEXT)");
        }
    }

    // region Users
    public void saveUser(User user) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT OR REPLACE INTO users(id, name, role) VALUES(?,?,?)")) {
            ps.setInt(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getRole().name());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to save user", ex);
        }
    }

    public Optional<User> findUser(int id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id, name, role FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to load user", ex);
        }
        return Optional.empty();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT id, name, role FROM users")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to load users", ex);
        }
        return users;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        Role role = Role.valueOf(rs.getString("role"));
        return new User(id, name, role);
    }
    // endregion

    // region Tickets
    public Ticket createTicket(User requester, String title, String description, String category, LocalDateTime createdAt) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO tickets(requester_id, assigned_agent_id, category, title, description, status, created_at, updated_at, rating, agent_flagged) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, requester.getId());
            ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, category);
            ps.setString(4, title);
            ps.setString(5, description);
            ps.setString(6, TicketStatus.RAISED.name());
            ps.setString(7, createdAt.toString());
            ps.setString(8, createdAt.toString());
            ps.setNull(9, java.sql.Types.INTEGER);
            ps.setInt(10, 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new Ticket(id, requester, title, description, category, createdAt);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to create ticket", ex);
        }
        throw new IllegalStateException("Ticket id not generated");
    }

    public void updateTicket(Ticket ticket) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE tickets SET assigned_agent_id=?, category=?, title=?, description=?, status=?, updated_at=?, rating=?, agent_flagged=? WHERE id=?")) {
            if (ticket.getAssignedAgent() != null) {
                ps.setInt(1, ticket.getAssignedAgent().getId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, ticket.getCategory());
            ps.setString(3, ticket.getTitle());
            ps.setString(4, ticket.getDescription());
            ps.setString(5, ticket.getStatus().name());
            ps.setString(6, ticket.getUpdatedAt().toString());
            if (ticket.getRating() != null) {
                ps.setInt(7, ticket.getRating());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setInt(8, ticket.isAgentFlagged() ? 1 : 0);
            ps.setInt(9, ticket.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to update ticket", ex);
        }
    }

    public List<Ticket> getTickets() {
        List<Ticket> tickets = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM tickets")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to load tickets", ex);
        }
        return tickets;
    }

    public Optional<Ticket> findTicket(int id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM tickets WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTicket(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to find ticket", ex);
        }
        return Optional.empty();
    }

    public void addNote(int ticketId, Note note) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO notes(ticket_id, author_id, author_name, message, created_at) VALUES(?,?,?,?,?)")) {
            ps.setInt(1, ticketId);
            ps.setInt(2, note.getAuthorId());
            ps.setString(3, note.getAuthorName());
            ps.setString(4, note.getMessage());
            ps.setString(5, note.getCreatedAt().toString());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to add note", ex);
        }
    }

    public void addHistory(int ticketId, TicketHistoryEntry entry) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO ticket_history(ticket_id, timestamp, action, performed_by) VALUES(?,?,?,?)")) {
            ps.setInt(1, ticketId);
            ps.setString(2, entry.getTimestamp().toString());
            ps.setString(3, entry.getAction());
            ps.setString(4, entry.getPerformedBy());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to add history entry", ex);
        }
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int requesterId = rs.getInt("requester_id");
        Optional<User> requesterOpt = findUser(requesterId);
        User requester = requesterOpt.orElseThrow(() -> new IllegalStateException("Requester missing for ticket " + id));
        String title = rs.getString("title");
        String description = rs.getString("description");
        String category = rs.getString("category");
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
        Ticket ticket = new Ticket(id, requester, title, description, category, createdAt);
        int assignedAgentId = rs.getInt("assigned_agent_id");
        if (!rs.wasNull()) {
            findUser(assignedAgentId).ifPresent(ticket::setAssignedAgent);
        }
        ticket.setStatus(TicketStatus.valueOf(rs.getString("status")));
        ticket.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at")));
        int rating = rs.getInt("rating");
        if (!rs.wasNull()) {
            ticket.setRating(rating);
        }
        ticket.setAgentFlagged(rs.getInt("agent_flagged") == 1);
        ticket.getNotes().addAll(loadNotes(id));
        ticket.getHistory().addAll(loadHistory(id));
        return ticket;
    }

    private List<Note> loadNotes(int ticketId) throws SQLException {
        List<Note> notes = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM notes WHERE ticket_id=? ORDER BY created_at")) {
            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notes.add(new Note(
                            rs.getInt("author_id"),
                            rs.getString("author_name"),
                            rs.getString("message"),
                            LocalDateTime.parse(rs.getString("created_at"))
                    ));
                }
            }
        }
        return notes;
    }

    private List<TicketHistoryEntry> loadHistory(int ticketId) throws SQLException {
        List<TicketHistoryEntry> history = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM ticket_history WHERE ticket_id=? ORDER BY timestamp")) {
            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(new TicketHistoryEntry(
                            LocalDateTime.parse(rs.getString("timestamp")),
                            rs.getString("action"),
                            rs.getString("performed_by")
                    ));
                }
            }
        }
        return history;
    }
    // endregion

    // region Change requests
    public ChangeRequest createChangeRequest(User requester, String title, String description, LocalDate expiry, LocalDateTime createdAt) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO change_requests(requester_id, title, description, status, expiry_date, archived, implementation_note, created_at) " +
                        "VALUES(?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, requester.getId());
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setString(4, ChangeRequestStatus.RAISED.name());
            ps.setString(5, expiry.toString());
            ps.setInt(6, 0);
            ps.setNull(7, java.sql.Types.VARCHAR);
            ps.setString(8, createdAt.toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new ChangeRequest(id, requester, title, description, expiry, createdAt);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to create change request", ex);
        }
        throw new IllegalStateException("Change request id not generated");
    }

    public List<ChangeRequest> getChangeRequests() {
        List<ChangeRequest> requests = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM change_requests")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapChangeRequest(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to load change requests", ex);
        }
        return requests;
    }

    public List<ChangeRequest> getArchivedChanges() {
        List<ChangeRequest> requests = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM change_requests WHERE archived = 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapChangeRequest(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to load archived changes", ex);
        }
        return requests;
    }

    public Optional<ChangeRequest> findChangeRequest(int id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM change_requests WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapChangeRequest(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to find change request", ex);
        }
        return Optional.empty();
    }

    public void updateChangeRequest(ChangeRequest cr) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE change_requests SET title=?, description=?, status=?, expiry_date=?, archived=?, implementation_note=? WHERE id=?")) {
            ps.setString(1, cr.getTitle());
            ps.setString(2, cr.getDescription());
            ps.setString(3, cr.getStatus().name());
            ps.setString(4, cr.getExpiryDate().toString());
            ps.setInt(5, cr.isArchived() ? 1 : 0);
            ps.setString(6, cr.getImplementationNote());
            ps.setInt(7, cr.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to update change request", ex);
        }
    }

    public void removeChangeRequest(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM change_requests WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to remove change request", ex);
        }
    }

    private ChangeRequest mapChangeRequest(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        User requester = findUser(rs.getInt("requester_id")).orElseThrow(() -> new IllegalStateException("Missing requester for change request " + id));
        LocalDate expiry = LocalDate.parse(rs.getString("expiry_date"));
        LocalDateTime created = LocalDateTime.parse(rs.getString("created_at"));
        ChangeRequest cr = new ChangeRequest(id, requester, rs.getString("title"), rs.getString("description"), expiry, created);
        cr.setStatus(ChangeRequestStatus.valueOf(rs.getString("status")));
        cr.setArchived(rs.getInt("archived") == 1);
        cr.setImplementationNote(rs.getString("implementation_note"));
        return cr;
    }
    // endregion

    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }
}

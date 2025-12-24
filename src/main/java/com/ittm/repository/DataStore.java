package com.ittm.repository;

import com.ittm.model.ChangeRequest;
import com.ittm.model.Ticket;
import com.ittm.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {
    private final Map<Integer, User> users = new HashMap<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<ChangeRequest> changeRequests = new ArrayList<>();
    private final List<ChangeRequest> archivedChanges = new ArrayList<>();

    public Map<Integer, User> getUsers() {
        return users;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public List<ChangeRequest> getChangeRequests() {
        return changeRequests;
    }

    public List<ChangeRequest> getArchivedChanges() {
        return archivedChanges;
    }
}

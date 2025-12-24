package com.ittm.service;

import com.ittm.model.ChangeRequest;
import com.ittm.model.ChangeRequestStatus;
import com.ittm.model.Role;
import com.ittm.model.User;
import com.ittm.repository.DataStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ChangeRequestService {
    private final DataStore dataStore;
    private int changeSeq = 1;

    public ChangeRequestService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public ChangeRequest raise(User requester, String title, String description, LocalDate expiry) {
        ChangeRequest request = new ChangeRequest(changeSeq++, requester, title, description, expiry, LocalDateTime.now());
        dataStore.getChangeRequests().add(request);
        return request;
    }

    public void renew(User actor, int changeId, LocalDate newExpiry) {
        ChangeRequest cr = findChange(changeId);
        if (actor.getRole() != Role.ADMIN && actor.getId() != cr.getRequester().getId()) {
            throw new IllegalStateException("Only admin or requester can renew");
        }
        cr.setExpiryDate(newExpiry);
    }

    public void remove(User actor, int changeId) {
        ChangeRequest cr = findChange(changeId);
        if (actor.getRole() != Role.ADMIN && actor.getId() != cr.getRequester().getId()) {
            throw new IllegalStateException("Only admin or requester can remove");
        }
        dataStore.getChangeRequests().remove(cr);
    }

    public void approve(User admin, int changeId, boolean approve) {
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only admin can approve");
        }
        ChangeRequest cr = findChange(changeId);
        cr.setStatus(approve ? ChangeRequestStatus.APPROVED : ChangeRequestStatus.REJECTED);
    }

    public void implement(User agent, int changeId, String implementationNote) {
        ChangeRequest cr = findChange(changeId);
        if (agent.getRole() != Role.AGENT && agent.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only agent/admin can implement");
        }
        if (cr.getStatus() != ChangeRequestStatus.APPROVED) {
            throw new IllegalStateException("Change must be approved");
        }
        cr.setImplementationNote(implementationNote);
        cr.setStatus(ChangeRequestStatus.IMPLEMENTED);
    }

    public List<ChangeRequest> expiringWithin(int days) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(days);
        return dataStore.getChangeRequests().stream()
                .filter(cr -> !cr.isArchived())
                .filter(cr -> !cr.getExpiryDate().isAfter(threshold))
                .collect(Collectors.toList());
    }

    public List<ChangeRequest> quarterlyReport(int quarter) {
        return dataStore.getChangeRequests().stream()
                .filter(cr -> (cr.getCreatedAt().getMonthValue() - 1) / 3 + 1 == quarter)
                .collect(Collectors.toList());
    }

    public void archiveOld(LocalDate today) {
        List<ChangeRequest> toArchive = dataStore.getChangeRequests().stream()
                .filter(cr -> cr.getCreatedAt().toLocalDate().isBefore(today.minusYears(1)))
                .collect(Collectors.toList());
        toArchive.forEach(cr -> {
            cr.setArchived(true);
            cr.setStatus(ChangeRequestStatus.ARCHIVED);
            dataStore.getArchivedChanges().add(cr);
        });
        dataStore.getChangeRequests().removeAll(toArchive);
    }

    private ChangeRequest findChange(int id) {
        return dataStore.getChangeRequests().stream()
                .filter(cr -> cr.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Change request not found"));
    }
}

package com.ittm.service;

import com.ittm.model.Role;
import com.ittm.model.User;
import com.ittm.repository.DataStore;

import java.util.Collection;
import java.util.Optional;

public class UserService {
    private final DataStore dataStore;

    public UserService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public User createUser(int id, String name, Role role) {
        User user = new User(id, name, role);
        dataStore.getUsers().put(id, user);
        return user;
    }

    public Optional<User> findUser(int id) {
        return Optional.ofNullable(dataStore.getUsers().get(id));
    }

    public Collection<User> getAll() {
        return dataStore.getUsers().values();
    }

    public void changeRole(User admin, int userId, Role newRole) {
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only admins can change roles");
        }
        User target = dataStore.getUsers().get(userId);
        if (target != null) {
            target.setRole(newRole);
        }
    }
}

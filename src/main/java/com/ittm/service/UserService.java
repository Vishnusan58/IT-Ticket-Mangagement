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
        dataStore.saveUser(user);
        return user;
    }

    public Optional<User> findUser(int id) {
        return dataStore.findUser(id);
    }

    public Collection<User> getAll() {
        return dataStore.getAllUsers();
    }

    public void changeRole(User admin, int userId, Role newRole) {
        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only admins can change roles");
        }
        dataStore.findUser(userId).ifPresent(target -> {
            target.setRole(newRole);
            dataStore.saveUser(target);
        });
    }
}

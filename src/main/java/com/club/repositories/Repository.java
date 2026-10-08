package com.club.repositories;

import java.util.Optional;

public interface Repository<ID, T> {
    void save(T entity);
    Optional<T> findById(ID id);

    void remove(ID orderId);
}

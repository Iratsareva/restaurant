package com.example.demo.repository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public abstract class AbstractRepository<T> {

    @PersistenceContext
    protected EntityManager entityManager;

    private final Class<T> entityClass;

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Transactional
    public T create(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    public T findById(Long id) {
        return entityManager.find(entityClass, id);
    }

    @Transactional
    public T update(T entity) {
        return entityManager.merge(entity);
    }

    @Transactional
    public void delete(Long id) {
        T entity = findById(id);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    public List<T> findAll() {
        TypedQuery<T> query = entityManager.createQuery("from " + entityClass.getName(), entityClass);
        return query.getResultList();
    }
}

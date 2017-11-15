package org.baeldung.persistence.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.baeldung.persistence.model.User;
import org.baeldung.web.util.SearchCriteria;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UserDAO implements IUserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long id) {
    	try {
			
    		Query delete = this.entityManager.createNativeQuery("DELETE FROM User where id = :id");
    		delete.setParameter("id", id);
    		
    		delete.executeUpdate();
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Override
    public List<User> findAll() {
    	try {
		
			TypedQuery<User> query = this.entityManager.createQuery("SELECT u from User u ORDER BY u.id DESC",
					User.class);
			return query.getResultList();
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return new ArrayList<>();
    }
    
    @Override
    public List<User> searchUser(final List<SearchCriteria> params) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<User> query = builder.createQuery(User.class);
        final Root r = query.from(User.class);

        Predicate predicate = builder.conjunction();

        for (final SearchCriteria param : params) {
            if (param.getOperation().equalsIgnoreCase(">")) {
                predicate = builder.and(predicate, builder.greaterThanOrEqualTo(r.get(param.getKey()), param.getValue().toString()));
            } else if (param.getOperation().equalsIgnoreCase("<")) {
                predicate = builder.and(predicate, builder.lessThanOrEqualTo(r.get(param.getKey()), param.getValue().toString()));
            } else if (param.getOperation().equalsIgnoreCase(":")) {
                if (r.get(param.getKey()).getJavaType() == String.class) {
                    predicate = builder.and(predicate, builder.like(r.get(param.getKey()), "%" + param.getValue() + "%"));
                } else {
                    predicate = builder.and(predicate, builder.equal(r.get(param.getKey()), param.getValue()));
                }
            }
        }
        query.where(predicate);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void save(final User entity) {
        entityManager.persist(entity);
    }

}

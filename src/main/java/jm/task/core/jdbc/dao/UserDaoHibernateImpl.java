package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class UserDaoHibernateImpl implements UserDao {
    public UserDaoHibernateImpl() {

    }


    @Override
    public void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS User (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255), lastName VARCHAR(255), age TINYINT)";

        Util.executeWithSession(session -> {
            Util.executeWithTransaction(session, () -> {
                session.createSQLQuery(sql).executeUpdate();
            });
        });

    }

    @Override
    public void dropUsersTable() {
        String sql = "DROP TABLE IF EXISTS User";

        Util.executeWithSession(session -> {
            Util.executeWithTransaction(session, () -> {
                session.createSQLQuery(sql).executeUpdate();
            });
        });
    }

    @Override
    public void saveUser(String name, String lastName, byte age) {
        User newUser = new User(name, lastName, (byte) age);

        Util.executeWithSession(session -> {
            Util.executeWithTransaction(session, () -> {
                session.save(newUser);
            });
        });
    }

    @Override
    public void removeUserById(long id) {
        Util.executeWithSession(session -> {
            Util.executeWithTransaction(session, () -> {
                session.createQuery("delete from User where id = :userfordeleteid").setParameter("userfordeleteid", id).executeUpdate();
            });
        });
    }

    @Override
    public List<User> getAllUsers() {
        return Util.executeWithSession(session -> {
            return Util.executeWithTransaction(session, () ->
                    session.createQuery("from User", User.class).list());
        });
    }

    @Override
    public void cleanUsersTable() {
        Util.executeWithSession(session -> {
            Util.executeWithTransaction(session, () -> {
                session.createNativeQuery("TRUNCATE TABLE IF EXIST User").executeUpdate();
            });
        });
    }

    /**
     * @deprecated
     */
    public void saveUserOld(String name, String lastname, byte age) {
        User newUser = new User(name, lastname, (byte) age);

        try (Session session = Util.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(newUser);
            try {
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
            }
        }
    }
}

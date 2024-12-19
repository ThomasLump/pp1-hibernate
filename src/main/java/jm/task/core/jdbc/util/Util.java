package jm.task.core.jdbc.util;

import jm.task.core.jdbc.model.User;
import org.h2.util.json.JSONStringTarget;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Утилитный класс для работы с Hibernate.
 * Предоставляет методы для выполнения операций с сессиями и транзакциями Hibernate,
 * а также для настройки логирования и конфигурации соединения с базой данных.
 *
 * Этот класс управляет созданием сессий, выполнением операций в рамках транзакций,
 * а также логирует работу с Hibernate. Методы, включающие работу с сессиями и транзакциями,
 * автоматически управляют их открытием и закрытием, обеспечивая корректную работу с базой данных.
 *
 * Пример использования:
 * <pre>
 *     // Выполнение операции без возврата результата
 *     Util.executeWithSession(session -> {
 *         // Логика работы с сессией
 *     });
 *
 *     // Выполнение операции с результатом
 *     String result = Util.executeWithSession(session -> {
 *         // Логика работы с сессией
 *         return "result";
 *     });
 * </pre>
 */
public class Util {
    //private static Configuration configuration = new Configuration().configure();//подключение через xml
    private static Configuration configuration;
    private static final SessionFactory sessionFactory;

    static {
        //configuration = new Configuration().configure();
        configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        //configuration.setProperty("")
        configuration.setProperty("hibernate.connection.url","jdbc:mysql://localhost:7777/mysql" );
        configuration.setProperty("hibernate.connection.username", "root");
        configuration.setProperty("hibernate.connection.password", "");
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "none");
        configuration.addAnnotatedClass(User.class);

        sessionFactory = configuration.buildSessionFactory(); //нужно ли это в трай блок?

    }

    /**
     * @deprecated
     * @return
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    /**
     * Выполняет операцию в рамках сессии Hibernate и возвращает результат выполнения.
     * Метод открывает сессию, выполняет переданный {@link Function} с этой сессией и возвращает результат.
     *
     * @param func Объект {@link Function}, который принимает сессию Hibernate и возвращает результат.
     *             Эта операция может быть любой логикой, которая использует сессию и возвращает результат.
     * @param <R> Тип возвращаемого значения, которое будет получено от выполнения {@code func.apply(session)}.
     *
     * @return Результат выполнения переданного {@code Function}, полученный с помощью сессии Hibernate.
     *
     * @throws IllegalArgumentException если переданный {@code func} является {@code null}.
     */
    public static <R> R executeWithSession(Function<Session, R> func) {
        try (Session session = sessionFactory.openSession()) {
            return func.apply(session);
        }
    }

    /**
     * Выполняет операцию в рамках сессии Hibernate без возврата результата.
     * Метод открывает сессию, выполняет переданный {@link Consumer} с этой сессией.
     *
     * @param cons Объект {@link Consumer}, который принимает сессию Hibernate и выполняет операцию без возвращения результата.
     *             Эта операция может быть любым действием, которое использует сессию, но не требует результата(например, сохранение, обновление, удаление данных).
     *
     * @throws IllegalArgumentException если переданный {@code cons} является {@code null}.
     */
    public static void executeWithSession(Consumer<Session> cons) {
        try (Session session = sessionFactory.openSession()) {
            cons.accept(session);
        }
    }

    /**
     * Выполняет операцию в рамках транзакции Hibernate.
     * Метод создает транзакцию, выполняет переданный Runnable и коммитит транзакцию.
     * В случае возникновения ошибки, транзакция будет откатана.
     *
     * @param session Сессия Hibernate, в рамках которой выполняется операция.
     * @param runnable Объект, содержащий операцию, которую нужно выполнить в рамках транзакции.
     *                 Обычно это операция, которая изменяет состояние базы данных (например, сохранение, обновление, удаление).
     *
     * @throws IllegalArgumentException если переданный {@code runnable} является {@code null}.
     */
    public static void executeWithTransaction(Session session, Runnable runnable) {
        Transaction transaction = session.beginTransaction();
        try {
            runnable.run();
            transaction.commit();
        } catch (Exception e) { //я тут прям общим шарахнул, хз как правильно
            if (transaction != null) {
                transaction.rollback();
            }
        }
    }

    /**
     * Выполняет операцию в рамках транзакции Hibernate и возвращает результат выполнения.
     * Метод создает транзакцию, выполняет переданный {@link Supplier} и коммитит транзакцию.
     * В случае возникновения ошибки, транзакция будет откатана, а метод вернет {@code null}.
     *
     * @param session Сессия Hibernate, в рамках которой выполняется операция.
     * @param supp Объект {@link Supplier}, который представляет операцию, возвращающую результат,
     *             выполняемую в рамках транзакции. Эта операция может быть любым действием,
     *             которое изменяет состояние базы данных и возвращает результат.
     * @param <R> Тип возвращаемого значения, которое будет получено от выполнения {@code supp.get()}.
     *
     * @return Результат выполнения переданного {@code Supplier}, если операция прошла успешно.
     *         Если возникла ошибка и транзакция была откатана, возвращается {@code null}.
     *
     * @throws IllegalArgumentException если переданный {@code supp} является {@code null}.
     */
    public static <R> R executeWithTransaction(Session session, Supplier<R> supp) {
        Transaction transaction = session.beginTransaction();
        try {
            R result = supp.get();
            transaction.commit();
            return result;
        } catch (Exception e) {
            transaction.rollback();
        }
        return null;//что лучше тут возвращать в случае ошибки?
    }
    // реализуйте настройку соеденения с БД

    //надо бы глянуть нестандартные логгеры для гибернации
    public static void setup() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.WARNING);

        // Создаём консольный хэндлер
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter());

        // Очищаем старые хэндлеры и добавляем новый
        rootLogger.getHandlers()[0].setLevel(Level.OFF); // Отключаем дефолтный хэндлер
        rootLogger.addHandler(consoleHandler);

        // Логирование Hibernate
        Logger hibernateLogger = Logger.getLogger("org.hibernate");
        hibernateLogger.setLevel(Level.WARNING);

        Logger sqlLogger = Logger.getLogger("org.hibernate.SQL");
        sqlLogger.setLevel(Level.INFO);

        Logger typeLogger = Logger.getLogger("org.hibernate.type.descriptor.sql");
        typeLogger.setLevel(Level.FINE);
    }
}



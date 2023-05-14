package eformer.back.eformer_backend.model;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * User class Entity representing the `user` table.
 * User for customers & employees.
 * Distinction is made using the adLevel.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private final Integer userId;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private final Timestamp createTime;

    @Column(name = "ad_level")
    private final Integer adLevel;

    protected User() {
        this("", "", "", -1);
    }

    protected User(Integer userId, String username, String email,
                   String password, Timestamp createTime, Integer adLevel) {
        setUsername(username);
        setEmail(email);
        setPassword(password);
        this.adLevel = adLevel;
        this.userId = userId;
        this.createTime = createTime;
    }

    public User(String username, String email, String password, Integer adLevel) {
        this(-1, username, email, password, new Timestamp(new Date().getTime()), adLevel);
    }

    public User(String username, String email, String password) {
        this(username, email, password, 0);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getUserId() {
        return userId;
    }

    public LocalDateTime getCreateTime() {
        return createTime.toLocalDateTime();
    }

    public Integer getAdLevel() {
        return adLevel;
    }

    public boolean isCustomer() {
        return getAdLevel() == 0;
    }

    public boolean isGuest() {
        return getAdLevel() < 0 || getUserId() < 0;
    }
}

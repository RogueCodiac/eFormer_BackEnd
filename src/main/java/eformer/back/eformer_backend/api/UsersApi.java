package eformer.back.eformer_backend.api;


import eformer.back.eformer_backend.model.User;
import eformer.back.eformer_backend.repository.UserRepository;
import eformer.back.eformer_backend.utility.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/users/")
public class UsersApi extends BaseApi {
    private static final String emailPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    private static final String usernamePattern = "^\\w+$";

    final UserRepository manager;

    final PasswordEncoder encoder;

     public UsersApi(UserRepository manager, JwtService jService, PasswordEncoder encoder) {
         super(jService, manager);
         this.manager = manager;
         this.encoder = encoder;
     }

     public boolean isNotValidEmail(String email) {
         return email == null || !email.matches(emailPattern);
     }

     public boolean isNotValidUsername(String username) {
         return username == null || !username.matches(usernamePattern);
     }

     public boolean isNotValidPassword(String password) {
         return password == null || password.length() < 8;
     }

     public StringBuilder checkUser(User user) {
         var error = new StringBuilder();

         String email = user.getEmail();
         String username = user.getUsername();

         if (isNotValidEmail(email) || manager.existsByEmail(email)) {
             error.append("Email already in use or is invalid\n");
         }

         if (isNotValidUsername(username) || manager.existsByUsername(username)) {
             error.append("Username already in use or is invalid (Must consist of alphanumeric characters only)\n");
         }

        if (!User.isValidAdLevel(user.getAdLevel())) {
            error.append("Administrative level ")
                    .append(user.getAdLevel())
                    .append(" is invalid, must be <= ")
                    .append(User.getMaxAdLevel())
                    .append('\n');
        }

        if (isNotValidPassword(user.getPassword())) {
            error.append("Invalid password must 8 chars at least");
        }

         return error;
     }

     public ResponseEntity<Object> getUsers(HashMap<String, String> header,
                                            HashMap<String, Object> body,
                                            boolean isAfter) {
         try {
             var date = (Date) body.getOrDefault("date", null);

             if (date == null || header == null) {
                 /* 422 */
                 return new ResponseEntity<>("Missing sender or date fields",
                         HttpStatus.UNPROCESSABLE_ENTITY);
             } else if (!isManager(header)) {
                 /* 423 */
                 return new ResponseEntity<>("Sender is not a manager",
                         HttpStatus.FORBIDDEN);
             }

             /* 200 */
             return new ResponseEntity<>(
                     isAfter ? manager.findAllByCreateTimeAfter(date) :
                             manager.findAllByCreateTimeBefore(date),
                     HttpStatus.OK
             );
         } catch (Exception ignored) {
             /* Prevent potential server crash */
             return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
         }
     }

     /**
      * Response Body must contain:
      *     sender: User sending the request;
      *     date: Date after which to get the Users;
      * */
    @PostMapping("getAllAfter")
    @ResponseBody
    public ResponseEntity<Object> getUsersAfter(
            @RequestHeader HashMap<String, String> header,
            @RequestBody HashMap<String, Object> body
    ) {
        return getUsers(header, body, true);
    }

    /**
     * Response Body must contain:
     *     sender: User sending the request;
     *     date: Date before which to get the Users;
     * */
    @PostMapping("getAllBefore")
    @ResponseBody
    public ResponseEntity<Object> getUsersBefore(
            @RequestHeader HashMap<String, String> header,
            @RequestBody HashMap<String, Object> body
    ) {
        return getUsers(header, body, false);
    }

    /**
     * Must contain the following entries:
     *  creator: User creating the new user, must be a manager at least;
     *  user: New User.
     * */
    @PostMapping("create")
    @ResponseBody
    public ResponseEntity<Object> create(
            @RequestHeader HashMap<String, String> header,
            @RequestBody User user
    ) {
        try {
            if (user == null || header == null) {
                /* 422 */
                return new ResponseEntity<>("No user and/or invalid token", HttpStatus.UNPROCESSABLE_ENTITY);
            } else if (!isManager(header)) {
                /* 423 */
                return new ResponseEntity<>("Sender not manager", HttpStatus.FORBIDDEN);
            }

            if (user.getAdLevel() >= User.getMaxAdLevel()) {
                user.setAdLevel(1);
            }

            var error = checkUser(user);

            if (error.length() > 0) {
                /* 422 */
                return new ResponseEntity<>(error.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
            }

            /* Encode the password */
            user.setPassword(encoder.encode(user.getPassword()));

            var response = new HashMap<String, Object>();
            user = manager.save(user);

            response.put("userId", user.getUserId());
            response.put("adLevel", user.getAdLevel());
            response.put("role", user.getRole());

            /* 200 */
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ignored) {
            /* 400 */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

package eformer.back.eformer_backend.api;


import eformer.back.eformer_backend.model.User;
import eformer.back.eformer_backend.repository.UserRepository;
import eformer.back.eformer_backend.utility.auth.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/users/")
public class UsersApi {
    private static final String emailPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    private static final String usernamePattern = "^\\w+$";

    final UserRepository manager;

    final JwtService jService;

     public UsersApi(UserRepository manager, JwtService jService) {
         this.manager = manager;
         this.jService = jService;
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

        if (User.isValidAdLevel(user.getAdLevel())) {
            error.append("Administrative level is invalid, must be <= ").append(User.getMaxAdLevel()).append('\n');
        }

        if (isNotValidPassword(user.getPassword())) {
            error.append("Invalid password must 8 chars at least");
        }

         return error;
     }

     public ResponseEntity<Object> getUsers(HashMap<String, Object> parameters,
                                            boolean isAfter) {
         try {
             var token = (String) parameters.getOrDefault("token", null);
             var date = (Date) parameters.getOrDefault("date", null);

             if (token == null || date == null || jService.isTokenExpired(token)) {
                 return new ResponseEntity<>("Missing sender or date fields",
                         HttpStatus.UNPROCESSABLE_ENTITY);
             }

             var sender = manager.findByUsername(jService.extractUsername(token)).orElseThrow();

             var actualSender = manager.findByUsernameAndPassword(sender.getUsername(),
                     sender.getPassword());

             if (actualSender.isPresent() && actualSender.get().isManager()) {
                 /* 200 */
                 return new ResponseEntity<>(
                         isAfter ? manager.findAllByCreateTimeAfter(date) :
                                 manager.findAllByCreateTimeBefore(date),
                         HttpStatus.OK
                 );
             }

             /* 403 */
             return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
    public ResponseEntity<Object> getUsersAfter(@RequestBody HashMap<String, Object> parameters) {
        return getUsers(parameters, true);
    }

    /**
     * Response Body must contain:
     *     sender: User sending the request;
     *     date: Date before which to get the Users;
     * */
    @PostMapping("getAllBefore")
    @ResponseBody
    public ResponseEntity<Object> getUsersBefore(@RequestBody HashMap<String, Object> parameters) {
        return getUsers(parameters, false);
    }

    /**
     * Must contain the following entries:
     *  creator: User creating the new user, must be a manager at least;
     *  user: New User.
     * */
    @PostMapping("create")
    @ResponseBody
    public ResponseEntity<Object> create(@RequestBody HashMap<String, Object> parameters) {
        try {
            var user = (User) parameters.getOrDefault("user", null);
            var token = (String) parameters.getOrDefault("token", null);

            if (user == null || token == null || jService.isTokenExpired(token)) {
                /* 422 */
                return new ResponseEntity<>("No user and/or token", HttpStatus.UNPROCESSABLE_ENTITY);
            }

            if (user.getAdLevel() >= User.getMaxAdLevel()) {
                user.setAdLevel(1);
            }

            var creator = manager.findByUsername(jService.extractUsername(token));

            if (creator.isEmpty() || !creator.get().isManager()) {
                /* 403 */
                return new ResponseEntity<>("Creator not manager", HttpStatus.FORBIDDEN);
            }

            var error = checkUser(user);

            if (error.length() > 0) {
                /* 422 */
                return new ResponseEntity<>(error.toString(), HttpStatus.UNPROCESSABLE_ENTITY);
            }

            /* 200 */
            return new ResponseEntity<>(manager.save(user), HttpStatus.OK);
        } catch (Exception ignored) {
            /* Prevent potential server crash */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); /* 400 */
        }
    }
}
